package space.zeroxv6.journex.desktop.sync
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import space.zeroxv6.journex.shared.data.LocalDateAdapter
import space.zeroxv6.journex.shared.data.LocalDateTimeAdapter
import space.zeroxv6.journex.shared.data.LocalTimeAdapter
import space.zeroxv6.journex.shared.model.FullNote
import space.zeroxv6.journex.shared.model.JournalEntry
import space.zeroxv6.journex.shared.model.NoteAttachment
import space.zeroxv6.journex.shared.model.ProjectTask
import space.zeroxv6.journex.shared.model.NoteDrawing
import space.zeroxv6.journex.shared.model.NoteVoiceNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
class DesktopGoogleDriveSync(private val dataDir: java.io.File) {
    companion object {
        private const val APPLICATION_NAME = "Journal App Desktop"
        private const val FOLDER_NAME = "JournalAppBackup"
        private const val BACKUP_FILE_NAME = "journal_backup.zip"
        private val SCOPES = listOf(DriveScopes.DRIVE_FILE)
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"
    }
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(java.time.LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(java.time.LocalTime::class.java, LocalTimeAdapter())
        .create()
    private fun getCredentials(): Credential {
        val inputStream = javaClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw IllegalStateException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets, SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(java.io.File(System.getProperty("user.home"), ".journal_credentials")))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return try {
            val authApp = object : AuthorizationCodeInstalledApp(flow, receiver) {
                override fun onAuthorization(authorizationUrl: com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl) {
                    val url = authorizationUrl.build()
                    println("Opening browser for authentication...")
                    println("If browser doesn't open, please visit: $url")
                    try {
                        val os = System.getProperty("os.name").lowercase()
                        when {
                            os.contains("linux") -> {
                                val browsers = listOf("xdg-open", "google-chrome", "firefox", "chromium", "brave")
                                var opened = false
                                for (browser in browsers) {
                                    try {
                                        Runtime.getRuntime().exec(arrayOf(browser, url))
                                        opened = true
                                        break
                                    } catch (e: Exception) {
                                    }
                                }
                                if (!opened) {
                                    println("Could not automatically open browser. Please open the URL manually.")
                                }
                            }
                            os.contains("mac") -> {
                                Runtime.getRuntime().exec(arrayOf("open", url))
                            }
                            os.contains("win") -> {
                                Runtime.getRuntime().exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", url))
                            }
                            else -> {
                                println("Unknown OS. Please open the URL manually.")
                            }
                        }
                    } catch (e: Exception) {
                        println("Failed to open browser: ${e.message}")
                        println("Please open the URL manually.")
                    }
                }
            }
            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeout(120000) { 
                    authApp.authorize("user")
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            receiver.stop()
            throw IllegalStateException("Authentication timed out. Please complete the sign-in process.")
        } catch (e: Exception) {
            receiver.stop()
            throw e
        }
    }
    fun getDriveService(): Drive {
        val credential = getCredentials()
        return Drive.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }
    suspend fun syncToGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            val folderId = getOrCreateFolder(driveService)
                ?: return@withContext Result.failure(Exception("Failed to create backup folder"))
            val backupFile = createBackupFile()
                ?: return@withContext Result.failure(Exception("Failed to create backup file"))
            val fileMetadata = File().apply {
                name = BACKUP_FILE_NAME
                parents = listOf(folderId)
            }
            val mediaContent = FileContent("application/zip", backupFile)
            val existingFiles = driveService.files().list()
                .setQ("name='$BACKUP_FILE_NAME' and '$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .execute()
                .files
            existingFiles.forEach { file ->
                driveService.files().delete(file.id).execute()
            }
            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
            backupFile.delete()
            Result.success("Backup uploaded successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    suspend fun fetchFromGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
            val folders = driveService.files().list()
                .setQ("name='$FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .execute()
                .files
            if (folders.isEmpty()) {
                return@withContext Result.failure(Exception("No backup found"))
            }
            val folderId = folders[0].id
            val files = driveService.files().list()
                .setQ("name='$BACKUP_FILE_NAME' and '$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .execute()
                .files
            if (files.isEmpty()) {
                return@withContext Result.failure(Exception("No backup file found"))
            }
            val fileId = files[0].id
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val localFile = java.io.File(dataDir, "downloaded_backup.zip")
            FileOutputStream(localFile).use { fos ->
                fos.write(outputStream.toByteArray())
            }
            restoreFromBackup(localFile)
            localFile.delete()
            Result.success("Data restored successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    private fun getOrCreateFolder(driveService: Drive): String? {
        return try {
            val folders = driveService.files().list()
                .setQ("name='$FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .execute()
                .files
            if (folders.isNotEmpty()) {
                folders[0].id
            } else {
                val folderMetadata = File().apply {
                    name = FOLDER_NAME
                    mimeType = "application/vnd.google-apps.folder"
                }
                val folder = driveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute()
                folder.id
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun copyMediaToStaging(
        originalPath: String,
        category: String,
        stagingMediaDir: java.io.File,
        mediaMap: MutableMap<String, String>
    ): String {
        if (originalPath.isBlank()) return originalPath
        val resolvedOriginalPath = if (originalPath.startsWith("media/")) {
            java.io.File(dataDir, originalPath).absolutePath
        } else originalPath
        mediaMap[resolvedOriginalPath]?.let { return it }
        val source = java.io.File(resolvedOriginalPath)
        if (!source.exists() || !source.isFile) return originalPath
        val pathHash = resolvedOriginalPath.hashCode().toString().replace("-", "n")
        val destDir = java.io.File(stagingMediaDir, category).apply { mkdirs() }
        val destFileName = "${pathHash}_${source.name}"
        val destFile = java.io.File(destDir, destFileName)
        source.inputStream().use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val relative = "media/$category/$destFileName"
        mediaMap[resolvedOriginalPath] = relative
        return relative
    }
    private fun rewriteRelativeUrisToAbsolute() {
        val journalsFile = java.io.File(dataDir, "journals.json")
        val fullNotesFile = java.io.File(dataDir, "full_notes.json")
        val projectTasksFile = java.io.File(dataDir, "project_tasks.json")
        if (journalsFile.exists()) {
            val type = object : TypeToken<List<JournalEntry>>() {}.type
            val journals: List<JournalEntry> = gson.fromJson(journalsFile.readText(), type)
            val updated = journals.map { entry ->
                entry.copy(photos = entry.photos.map { p ->
                    if (p.startsWith("media/")) java.io.File(dataDir, p).absolutePath else p
                })
            }
            journalsFile.writeText(gson.toJson(updated))
        }
        if (fullNotesFile.exists()) {
            val type = object : TypeToken<List<FullNote>>() {}.type
            val notes: List<FullNote> = gson.fromJson(fullNotesFile.readText(), type)
            val updated = notes.map { n ->
                n.copy(
                    attachments = n.attachments.map { att ->
                        val newUri = if (att.uri.startsWith("media/")) java.io.File(dataDir, att.uri).absolutePath else att.uri
                        att.copy(uri = newUri)
                    },
                    voiceNotes = n.voiceNotes.map { vn ->
                        val newPath = if (vn.filePath.startsWith("media/")) java.io.File(dataDir, vn.filePath).absolutePath else vn.filePath
                        vn.copy(filePath = newPath)
                    },
                    drawings = n.drawings.map { d ->
                        val newImageData = if (d.imageData.startsWith("media/")) java.io.File(dataDir, d.imageData).absolutePath else d.imageData
                        d.copy(imageData = newImageData)
                    }
                )
            }
            fullNotesFile.writeText(gson.toJson(updated))
        }
        if (projectTasksFile.exists()) {
            val type = object : TypeToken<List<ProjectTask>>() {}.type
            val tasks: List<ProjectTask> = gson.fromJson(projectTasksFile.readText(), type)
            val updated = tasks.map { t ->
                t.copy(attachments = t.attachments.map { a ->
                    if (a.startsWith("media/")) java.io.File(dataDir, a).absolutePath else a
                })
            }
            projectTasksFile.writeText(gson.toJson(updated))
        }
    }
    private fun zipDirectory(stagingDir: java.io.File, zos: ZipOutputStream) {
        val files = stagingDir.listFiles() ?: return
        files.forEach { file ->
            if (file.isDirectory) {
                zipDirectory(file, zos)
            } else {
                val entryName = stagingDir.toPath().relativize(file.toPath()).toString().replace("\\", "/")
                zos.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
    private fun createBackupFile(): java.io.File? {
        return try {
            val stagingDir = java.io.File(dataDir, "backup_staging_${System.currentTimeMillis()}")
            stagingDir.deleteRecursively()
            stagingDir.mkdirs()
            val stagingMediaDir = java.io.File(stagingDir, "media").apply { mkdirs() }
            dataDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "json") {
                    file.copyTo(java.io.File(stagingDir, file.name), overwrite = true)
                }
            }
            val mediaMap = mutableMapOf<String, String>() 
            val journalsFile = java.io.File(dataDir, "journals.json")
            if (journalsFile.exists()) {
                val type = object : TypeToken<List<JournalEntry>>() {}.type
                val journals: List<JournalEntry> = gson.fromJson(journalsFile.readText(), type)
                val updated = journals.map { entry ->
                    entry.copy(photos = entry.photos.map { p ->
                    copyMediaToStaging(p, "images", stagingMediaDir, mediaMap)
                    })
                }
                java.io.File(stagingDir, "journals.json").writeText(gson.toJson(updated))
            }
            val fullNotesFile = java.io.File(dataDir, "full_notes.json")
            if (fullNotesFile.exists()) {
                val type = object : TypeToken<List<FullNote>>() {}.type
                val notes: List<FullNote> = gson.fromJson(fullNotesFile.readText(), type)
                val updatedNotes = notes.map { n ->
                    n.copy(
                        attachments = n.attachments.map { att ->
                            val uri = att.uri
                            val ext = java.io.File(if (uri.startsWith("media/")) java.io.File(dataDir, uri).absolutePath else uri).extension.lowercase()
                            val category = when (ext) {
                                "jpg", "jpeg", "png", "webp" -> "images"
                                "wav", "mp3", "m4a", "m4b", "ogg", "3gp" -> "audio"
                                else -> "files"
                            }
                            att.copy(uri = copyMediaToStaging(uri, category, stagingMediaDir, mediaMap))
                        },
                        voiceNotes = n.voiceNotes.map { vn ->
                            val path = vn.filePath
                            vn.copy(filePath = copyMediaToStaging(path, "audio", stagingMediaDir, mediaMap))
                        },
                        drawings = n.drawings.map { d ->
                            val data = d.imageData
                            if (data.startsWith("/") || data.startsWith("media/")) {
                                val file = java.io.File(if (data.startsWith("media/")) java.io.File(dataDir, data).absolutePath else data)
                                if (file.exists() && file.isFile) {
                                    d.copy(imageData = copyMediaToStaging(data, "drawings", stagingMediaDir, mediaMap))
                                } else d
                            } else d
                        }
                    )
                }
                java.io.File(stagingDir, "full_notes.json").writeText(gson.toJson(updatedNotes))
            }
            val projectTasksFile = java.io.File(dataDir, "project_tasks.json")
            if (projectTasksFile.exists()) {
                val type = object : TypeToken<List<ProjectTask>>() {}.type
                val tasks: List<ProjectTask> = gson.fromJson(projectTasksFile.readText(), type)
                val updatedTasks = tasks.map { t ->
                    t.copy(attachments = t.attachments.map { a ->
                        copyMediaToStaging(a, "files", stagingMediaDir, mediaMap)
                    })
                }
                java.io.File(stagingDir, "project_tasks.json").writeText(gson.toJson(updatedTasks))
            }
            val backupFile = java.io.File(dataDir, "backup.zip")
            backupFile.delete()
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                val mediaDir = java.io.File(stagingDir, "media")
                if (mediaDir.exists()) {
                    addFilesToZip(mediaDir, stagingDir, zos)
                }
                stagingDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension == "json") {
                        val entryName = file.name
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            stagingDir.deleteRecursively()
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun restoreFromBackup(backupFile: java.io.File) {
        try {
            dataDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "json") file.delete()
            }
            java.io.File(dataDir, "media").deleteRecursively()
            ZipInputStream(backupFile.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = java.io.File(dataDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            rewriteRelativeUrisToAbsolute()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun addFilesToZip(root: java.io.File, stagingRoot: java.io.File, zos: ZipOutputStream) {
        root.listFiles()?.forEach { file ->
            if (file.isDirectory) addFilesToZip(file, stagingRoot, zos)
            else {
                val entryName = stagingRoot.toPath().relativize(file.toPath()).toString().replace("\\", "/")
                zos.putNextEntry(ZipEntry(entryName))
                file.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
    fun isAuthenticated(): Boolean {
        return try {
            val credentialsDir = java.io.File(System.getProperty("user.home"), ".journal_credentials")
            credentialsDir.exists() && credentialsDir.listFiles()?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }
    fun getUserEmail(): String? {
        return try {
            if (!isAuthenticated()) return null
            val credentialsDir = java.io.File(System.getProperty("user.home"), ".journal_credentials")
            if (!credentialsDir.exists()) return null
            val inputStream = javaClass.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: return "Signed in to Google"
            val clientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(inputStream))
            val flow = GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES
            )
                .setDataStoreFactory(FileDataStoreFactory(credentialsDir))
                .setAccessType("offline")
                .build()
            val credential = flow.loadCredential("user")
            if (credential == null) return "Signed in to Google"
            val driveService = Drive.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
            val about = driveService.about().get()
                .setFields("user(emailAddress)")
                .execute()
            about.user?.emailAddress ?: "Signed in to Google"
        } catch (e: Exception) {
            e.printStackTrace()
            "Signed in to Google"
        }
    }
    fun signOut() {
        try {
            val credentialsDir = java.io.File(System.getProperty("user.home"), ".journal_credentials")
            credentialsDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
