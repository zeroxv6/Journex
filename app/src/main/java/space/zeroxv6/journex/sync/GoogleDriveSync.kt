package space.zeroxv6.journex.sync
import android.content.Context
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import space.zeroxv6.journex.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import android.webkit.MimeTypeMap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
class GoogleDriveSync(
    private val context: Context,
    private val database: AppDatabase
) {
    private val BACKUP_FILE_NAME = "journaling_backup.json"
    private val MEDIA_FOLDER_NAME = "JournalingAppMedia"
    private fun getDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Journaling App")
            .build()
    }
    private suspend fun getOrCreateMediaFolder(driveService: Drive): String? = withContext(Dispatchers.IO) {
        try {
            val existingFolders = driveService.files().list()
                .setQ("name='$MEDIA_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
                .files
            if (existingFolders.isNotEmpty()) {
                return@withContext existingFolders[0].id
            }
            val folderMetadata = File().apply {
                name = MEDIA_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
            }
            val folder = driveService.files()
                .create(folderMetadata)
                .setFields("id")
                .execute()
            folder.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private suspend fun uploadMediaFile(
        driveService: Drive,
        folderId: String,
        filePath: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(filePath)
            if (!file.exists()) return@withContext null
            val pathHash = filePath.hashCode().toString().replace("-", "n")
            val ext = fileName.substringAfterLast(".", "")
            val driveFileName = if (ext.isNotEmpty()) "${pathHash}_${fileName}" else pathHash
            val existingFiles = driveService.files().list()
                .setQ("name='$driveFileName' and '$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .setPageSize(10)
                .execute()
                .files
            val mimeType = when {
                fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                fileName.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
                fileName.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
                fileName.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                fileName.endsWith(".3gp", ignoreCase = true) -> "audio/3gpp"
                else -> "application/octet-stream"
            }
            val fileMetadata = File().apply {
                name = driveFileName
                parents = listOf(folderId)
                this.mimeType = mimeType
            }
            val mediaContent = com.google.api.client.http.FileContent(mimeType, file)
            val uploadedFile = if (existingFiles.isNotEmpty()) {
                val updateMetadata = File().apply {
                    name = driveFileName
                    this.mimeType = mimeType
                }
                driveService.files()
                    .update(existingFiles[0].id, updateMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            } else {
                driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }
            uploadedFile.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private suspend fun uploadContentUriToDrive(
        driveService: Drive,
        folderId: String,
        contentUriString: String
    ): String? = withContext(Dispatchers.IO) {
        var inputStream: java.io.InputStream? = null
        try {
            val uri = android.net.Uri.parse(contentUriString)
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).orEmpty()
            val lastSegment = uri.lastPathSegment.orEmpty()
            val extFromName = lastSegment.substringAfterLast(".", "").lowercase()
            val ext = if (extFromMime.isNotBlank()) extFromMime else extFromName
            val pathHash = contentUriString.hashCode().toString().replace("-", "n")
            val driveFileName = if (ext.isNotBlank()) "${pathHash}.${ext}" else pathHash
            val existingFiles = driveService.files().list()
                .setQ("name='$driveFileName' and '$folderId' in parents and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .setPageSize(10)
                .execute()
                .files
            val fileMetadata = File().apply {
                name = driveFileName
                parents = listOf(folderId)
                this.mimeType = mimeType
            }
            inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val mediaContent = com.google.api.client.http.InputStreamContent(mimeType, inputStream)
            val uploadedFile = if (existingFiles.isNotEmpty()) {
                val updateMetadata = File().apply {
                    name = driveFileName
                    this.mimeType = mimeType
                }
                driveService.files()
                    .update(existingFiles[0].id, updateMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            } else {
                driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }
            uploadedFile.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
        }
    }
    private suspend fun downloadMediaFile(
        driveService: Drive,
        fileId: String,
        destinationPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val outputFile = java.io.File(destinationPath)
            outputFile.parentFile?.mkdirs()
            val outputStream = java.io.FileOutputStream(outputFile)
            try {
                driveService.files()
                    .get(fileId)
                    .executeMediaAndDownloadTo(outputStream)
                outputStream.flush()
                true
            } catch (e: Exception) {
                outputStream.close()
                outputFile.delete() 
                e.printStackTrace()
                false
            } finally {
                try { outputStream.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    /**
     * Extracts ALL media file paths from JSON backup data.
     * Covers: journal photos, journal voice notes, note attachments (all types),
     * note drawings, note voice notes, and project task attachments.
     */
    private fun extractMediaPaths(jsonData: String): List<String> {
        val paths = mutableSetOf<String>()
        fun isBackupableMediaRef(path: String): Boolean {
            if (path.isBlank()) return false
            if (path.startsWith("content://")) return true
            return path.startsWith("/") && !path.startsWith("/proc")

        }
        try {
            val json = JSONObject(jsonData)
            json.optJSONArray("journals")?.let { journals ->
                for (i in 0 until journals.length()) {
                    val journal = journals.getJSONObject(i)
                    try {
                        val voiceNotes = JSONArray(journal.optString("voiceNotes", "[]"))
                        for (j in 0 until voiceNotes.length()) {
                            val voiceNote = voiceNotes.optJSONObject(j)
                            voiceNote?.optString("filePath")?.let { if (isBackupableMediaRef(it)) paths.add(it) }
                        }
                    } catch (e: Exception) {  }
                    try {
                        val photos = JSONArray(journal.optString("photos", "[]"))
                        for (j in 0 until photos.length()) {
                            val photo = photos.optString(j)
                            if (isBackupableMediaRef(photo)) paths.add(photo)
                        }
                    } catch (e: Exception) {  }
                }
            }
            json.optJSONArray("notes")?.let { notes ->
                for (i in 0 until notes.length()) {
                    val note = notes.getJSONObject(i)
                    try {
                        val attachments = JSONArray(note.optString("attachments", "[]"))
                        for (j in 0 until attachments.length()) {
                            val attachment = attachments.optJSONObject(j)
                            attachment?.optString("uri")?.let { 
                                if (isBackupableMediaRef(it)) paths.add(it)
                            }
                            attachment?.optString("thumbnailUri")?.let { 
                                if (isBackupableMediaRef(it)) paths.add(it)
                            }
                        }
                    } catch (e: Exception) {  }
                    try {
                        val drawings = JSONArray(note.optString("drawings", "[]"))
                        for (j in 0 until drawings.length()) {
                            val drawing = drawings.optJSONObject(j)
                            drawing?.optString("imageData")?.let { 
                                if (isBackupableMediaRef(it)) paths.add(it)
                            }
                        }
                    } catch (e: Exception) {  }
                    try {
                        val voiceNotes = JSONArray(note.optString("voiceNotes", "[]"))
                        for (j in 0 until voiceNotes.length()) {
                            val voiceNote = voiceNotes.optJSONObject(j)
                            voiceNote?.optString("filePath")?.let { if (isBackupableMediaRef(it)) paths.add(it) }
                        }
                    } catch (e: Exception) {  }
                }
            }
            json.optJSONArray("projectTasks")?.let { tasks ->
                for (i in 0 until tasks.length()) {
                    val task = tasks.getJSONObject(i)
                    try {
                        val attachments = JSONArray(task.optString("attachments", "[]"))
                        for (j in 0 until attachments.length()) {
                            val attachment = attachments.optString(j)
                            if (isBackupableMediaRef(attachment)) paths.add(attachment)
                        }
                    } catch (e: Exception) {  }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return paths.toList()
    }
    /**
     * Determines the appropriate local destination directory for a media file
     * based on the original path structure.
     */
    private fun getLocalMediaDir(originalPath: String, fileName: String): java.io.File {
        return when {
            originalPath.contains("journal_images") -> {
                java.io.File(context.filesDir, "journal_images").also { it.mkdirs() }
            }
            originalPath.contains("note_images") -> {
                java.io.File(context.filesDir, "note_images").also { it.mkdirs() }
            }
            originalPath.contains("note_drawings") -> {
                java.io.File(context.filesDir, "note_drawings").also { it.mkdirs() }
            }
            originalPath.contains("note_voice") || originalPath.contains("voice_notes") || 
            originalPath.endsWith(".m4a") || originalPath.endsWith(".mp3") || 
            originalPath.endsWith(".wav") || originalPath.endsWith(".3gp") -> {
                java.io.File(context.filesDir, "note_voice").also { it.mkdirs() }
            }
            else -> {
                val ext = fileName.substringAfterLast(".", "")
                when (ext.lowercase()) {
                    "jpg", "jpeg", "png", "webp" -> java.io.File(context.filesDir, "note_images").also { it.mkdirs() }
                    "m4a", "mp3", "wav", "3gp" -> java.io.File(context.filesDir, "note_voice").also { it.mkdirs() }
                    else -> (context.getExternalFilesDir(null) ?: context.filesDir).also { it.mkdirs() }
                }
            }
        }
    }
    /**
     * Remaps a single path using the mapping, returning the new path or the original if no mapping exists.
     */
    private fun remapPath(path: String, pathMapping: Map<String, String>): String {
        return pathMapping[path] ?: path
    }
    /**
     * Remaps all media paths inside a JSON string field that contains
     * a serialized JSON array or object. This operates at the parsed JSON level
     * to avoid issues with double-escaped strings.
     */
    private fun remapJsonStringField(jsonString: String, pathMapping: Map<String, String>): String {
        if (pathMapping.isEmpty() || jsonString.isBlank()) return jsonString
        try {
            val arr = JSONArray(jsonString)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                when {
                    !arr.isNull(i) && arr.optJSONObject(i) == null -> {
                        val str = arr.optString(i, "")
                        newArr.put(remapPath(str, pathMapping))
                    }
                    arr.optJSONObject(i) != null -> {
                        val obj = arr.getJSONObject(i)
                        val newObj = remapJsonObject(obj, pathMapping)
                        newArr.put(newObj)
                    }
                    else -> newArr.put(arr.get(i))
                }
            }
            return newArr.toString()
        } catch (e: Exception) {
        }
        try {
            val obj = JSONObject(jsonString)
            return remapJsonObject(obj, pathMapping).toString()
        } catch (e: Exception) {
        }
        return remapPath(jsonString, pathMapping)
    }
    /**
     * Remaps file path fields inside a JSON object.
     * Targets known fields: filePath, uri, thumbnailUri, imageData
     */
    private fun remapJsonObject(obj: JSONObject, pathMapping: Map<String, String>): JSONObject {
        val pathFields = listOf("filePath", "uri", "thumbnailUri", "imageData")
        val newObj = JSONObject(obj.toString()) 
        pathFields.forEach { field ->
            if (newObj.has(field) && !newObj.isNull(field)) {
                val oldValue = newObj.optString(field, "")
                if (oldValue.isNotEmpty()) {
                    val newValue = remapPath(oldValue, pathMapping)
                    newObj.put(field, newValue)
                }
            }
        }
        return newObj
    }
    suspend fun syncToGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService() 
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))
            val data = exportAllData()
            val mediaFolderId = getOrCreateMediaFolder(driveService)
                ?: return@withContext Result.failure(Exception("Failed to create media folder"))
            val mediaPaths = extractMediaPaths(data)
            val uploadedFiles = mutableMapOf<String, String>() 
            var uploadedCount = 0
            var skippedCount = 0  
            var failedCount = 0   
            android.util.Log.d("GoogleDriveSync", "Found ${mediaPaths.size} media paths to upload")
            mediaPaths.forEach { path ->
                if (path.startsWith("content://")){
                    val driveFileId = uploadContentUriToDrive(driveService, mediaFolderId, path)
                    if (driveFileId != null) {
                        uploadedFiles[path] = driveFileId
                        uploadedCount++
                        android.util.Log.d("GoogleDriveSync", "Uploaded content: $path -> $driveFileId")
                    } else {
                        failedCount++
                        android.util.Log.w("GoogleDriveSync", "Upload FAILED for content URI: $path")
                    }
                } else {
                    val file = java.io.File(path)
                    if (file.exists()) {
                        val fileName = file.name
                        val driveFileId = uploadMediaFile(driveService, mediaFolderId, path, fileName)
                        if (driveFileId != null) {
                            uploadedFiles[path] = driveFileId
                            uploadedCount++
                            android.util.Log.d("GoogleDriveSync", "Uploaded: $path -> $driveFileId")
                        } else {
                            failedCount++
                            android.util.Log.w("GoogleDriveSync", "Upload FAILED for: $path")
                        }
                    } else {
                        skippedCount++
                        android.util.Log.w("GoogleDriveSync", "File not found on disk: $path")
                    }
                }
            }
            val mediaMapping = JSONObject()
            uploadedFiles.forEach { (originalPath, driveFileId) ->
                mediaMapping.put(originalPath, driveFileId)
            }
            val backupJson = JSONObject(data)
            backupJson.put("mediaMapping", mediaMapping)
            val finalData = backupJson.toString()
            val existingFiles = driveService.files().list()
                .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
                .files
            val fileMetadata = File().apply {
                name = BACKUP_FILE_NAME
                mimeType = "application/json"
            }
            val contentStream = ByteArrayInputStream(finalData.toByteArray())
            val mediaContent = com.google.api.client.http.InputStreamContent(
                "application/json",
                contentStream
            )
            if (existingFiles.isNotEmpty()) {
                driveService.files()
                    .update(existingFiles[0].id, fileMetadata, mediaContent)
                    .execute()
            } else {
                driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            }
            val msg = buildString {
                append("Backup uploaded (${mediaPaths.size} media found")
                append(", $uploadedCount uploaded")
                if (skippedCount > 0) append(", $skippedCount missing from disk")
                if (failedCount > 0) append(", $failedCount failed")
                append(")")
            }
            Result.success(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Sync failed: ${e.message}"))
        }
    }
    suspend fun fetchFromGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService()
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))
            val files = driveService.files().list()
                .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .setPageSize(10)
                .execute()
                .files
            if (files.isEmpty()) {
                return@withContext Result.failure(Exception("No backup found on Google Drive"))
            }
            val outputStream = ByteArrayOutputStream()
            driveService.files()
                .get(files[0].id)
                .executeMediaAndDownloadTo(outputStream)
            val data = outputStream.toString("UTF-8")
            val backupJson = JSONObject(data)
            val mediaMapping = backupJson.optJSONObject("mediaMapping")
            var downloadedCount = 0
            var failedCount = 0
            val pathRemapping = mutableMapOf<String, String>() 
            mediaMapping?.let { mapping ->
                val keys = mapping.keys()
                while (keys.hasNext()) {
                    val originalPath = keys.next()
                    val driveFileId = mapping.getString(originalPath)
                    val driveFileName = driveService.files()
                        .get(driveFileId)
                        .setFields("name")
                        .execute()
                        .name
                        ?.takeIf { it.isNotBlank() }
                        ?: java.io.File(originalPath).name
                    val targetDir = getLocalMediaDir(originalPath, driveFileName)
                    val destinationPath = java.io.File(targetDir, driveFileName).absolutePath
                    if (downloadMediaFile(driveService, driveFileId, destinationPath)) {
                        downloadedCount++
                        pathRemapping[originalPath] = destinationPath
                    } else {
                        failedCount++
                        android.util.Log.w("GoogleDriveSync", "Failed to download: $originalPath (Drive ID: $driveFileId)")
                    }
                }
            }
            backupJson.remove("mediaMapping")
            val cleanData = backupJson.toString()
            importAllData(cleanData, pathRemapping)
            val msg = buildString {
                append("Data restored successfully")
                if (downloadedCount > 0) append(" ($downloadedCount media files)")
                if (failedCount > 0) append(" [$failedCount files failed]")
            }
            Result.success(msg)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Fetch failed: ${e.message}"))
        }
    }
    private suspend fun exportAllData(): String {
        val json = JSONObject()
        val journals = database.journalDao().getAllEntriesOnce()
        val journalsArray = JSONArray()
        journals.forEach { journal ->
            journalsArray.put(JSONObject().apply {
                put("id", journal.id)
                put("title", journal.title)
                put("content", journal.content)
                put("mood", journal.mood)
                put("tags", journal.tags)
                put("isFavorite", journal.isFavorite)
                put("isArchived", journal.isArchived)
                put("isPinned", journal.isPinned)
                put("createdAt", journal.createdAt)
                put("updatedAt", journal.updatedAt)
                put("voiceNotes", journal.voiceNotes)
                put("weather", journal.weather)
                put("location", journal.location)
                put("photos", journal.photos)
                put("wordCount", journal.wordCount)
                put("readingTime", journal.readingTime)
                put("color", journal.color)
                put("reminder", journal.reminder)
                put("linkedEntries", journal.linkedEntries)
                put("characterCount", journal.characterCount)
                put("paragraphCount", journal.paragraphCount)
                put("sentenceCount", journal.sentenceCount)
            })
        }
        json.put("journals", journalsArray)
        val notes = database.noteDao().getAllNotesOnce()
        val notesArray = JSONArray()
        notes.forEach { note ->
            notesArray.put(JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("plainTextContent", note.plainTextContent)
                put("category", note.category)
                put("tags", note.tags)
                put("color", note.color)
                put("isPinned", note.isPinned)
                put("isArchived", note.isArchived)
                put("isFavorite", note.isFavorite)
                put("isLocked", note.isLocked)
                put("lockPassword", note.lockPassword)
                put("attachments", note.attachments)
                put("checklistItems", note.checklistItems)
                put("tables", note.tables)
                put("codeBlocks", note.codeBlocks)
                put("drawings", note.drawings)
                put("voiceNotes", note.voiceNotes)
                put("links", note.links)
                put("reminder", note.reminder)
                put("recurringReminder", note.recurringReminder)
                put("createdAt", note.createdAt)
                put("updatedAt", note.updatedAt)
                put("lastAccessedAt", note.lastAccessedAt)
                put("collaborators", note.collaborators)
                put("folderId", note.folderId)
                put("parentNoteId", note.parentNoteId)
                put("templateId", note.templateId)
                put("wordCount", note.wordCount)
                put("characterCount", note.characterCount)
                put("readingTime", note.readingTime)
                put("version", note.version)
                put("versionHistory", note.versionHistory)
                put("formatting", note.formatting)
                put("layout", note.layout)
                put("priority", note.priority)
                put("status", note.status)
                put("location", note.location)
                put("weather", note.weather)
                put("mood", note.mood)
                put("customFields", note.customFields)
                put("aiSummary", note.aiSummary)
                put("aiKeywords", note.aiKeywords)
                put("relatedNotes", note.relatedNotes)
                put("exportFormats", note.exportFormats)
            })
        }
        json.put("notes", notesArray)
        try {
            val folders = database.noteFolderDao().getAllFolders().first()
            val foldersArray = JSONArray()
            folders.forEach { folder ->
                foldersArray.put(JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("color", folder.color)
                    put("icon", folder.icon)
                    put("parentId", folder.parentId)
                    put("createdAt", folder.createdAt)
                    put("isEncrypted", folder.isEncrypted)
                    put("sortOrder", folder.sortOrder)
                    put("viewMode", folder.viewMode)
                })
            }
            json.put("noteFolders", foldersArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val projectTasks = database.projectTaskDao().getAllTasksOnce()
        val projectTasksArray = JSONArray()
        projectTasks.forEach { task ->
            projectTasksArray.put(JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("status", task.status)
                put("priority", task.priority)
                put("tags", task.tags)
                put("assignee", task.assignee)
                put("dueDate", task.dueDate)
                put("estimatedHours", task.estimatedHours)
                put("actualHours", task.actualHours)
                put("createdAt", task.createdAt)
                put("updatedAt", task.updatedAt)
                put("completedAt", task.completedAt)
                put("projectId", task.projectId)
                put("subtasks", task.subtasks)
                put("attachments", task.attachments)
                put("comments", task.comments)
            })
        }
        json.put("projectTasks", projectTasksArray)
        try {
            val projects = database.projectTaskDao().getAllProjects().first()
            val projectsArray = JSONArray()
            projects.forEach { project ->
                projectsArray.put(JSONObject().apply {
                    put("id", project.id)
                    put("name", project.name)
                    put("description", project.description)
                    put("color", project.color)
                    put("icon", project.icon)
                    put("createdAt", project.createdAt)
                })
            }
            json.put("taskProjects", projectsArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val todos = database.todoDao().getAllTodos().first()
        val todosArray = JSONArray()
        todos.forEach { todo ->
            todosArray.put(JSONObject().apply {
                put("id", todo.id)
                put("title", todo.title)
                put("description", todo.description)
                put("isCompleted", todo.isCompleted)
                put("priority", todo.priority)
                put("dueDate", todo.dueDate)
                put("createdAt", todo.createdAt)
            })
        }
        json.put("todos", todosArray)
        val schedules = database.scheduleDao().getAllSchedules().first()
        val schedulesArray = JSONArray()
        schedules.forEach { schedule ->
            schedulesArray.put(JSONObject().apply {
                put("id", schedule.id)
                put("title", schedule.title)
                put("description", schedule.description)
                put("timeHour", schedule.timeHour)
                put("timeMinute", schedule.timeMinute)
                put("daysOfWeek", schedule.daysOfWeek)
                put("isEnabled", schedule.isEnabled)
            })
        }
        json.put("schedules", schedulesArray)
        val reminders = database.reminderDao().getAllReminders().first()
        val remindersArray = JSONArray()
        reminders.forEach { reminder ->
            remindersArray.put(JSONObject().apply {
                put("id", reminder.id)
                put("title", reminder.title)
                put("description", reminder.description)
                put("dateTime", reminder.dateTime)
                put("category", reminder.category)
                put("isCompleted", reminder.isCompleted)
                put("repeatType", reminder.repeatType)
            })
        }
        json.put("reminders", remindersArray)
        val prompts = database.promptDao().getAllPrompts().first()
        val promptsArray = JSONArray()
        prompts.forEach { prompt ->
            promptsArray.put(JSONObject().apply {
                put("id", prompt.id)
                put("promptText", prompt.promptText)
                put("category", prompt.category)
                put("response", prompt.response)
                put("createdAt", prompt.createdAt)
                put("updatedAt", prompt.updatedAt)
            })
        }
        json.put("prompts", promptsArray)
        val quickNotes = database.quickNoteDao().getAllQuickNotesOnce()
        val quickNotesArray = JSONArray()
        quickNotes.forEach { quickNote ->
            quickNotesArray.put(JSONObject().apply {
                put("id", quickNote.id)
                put("content", quickNote.content)
                put("createdAt", quickNote.createdAt)
            })
        }
        json.put("quickNotes", quickNotesArray)
        val settings = database.settingsDao().getSettingsOnce()
        settings?.let {
            json.put("settings", JSONObject().apply {
                put("pinCode", it.pinCode)
                put("notificationsEnabled", it.notificationsEnabled)
                put("journalReminderEnabled", it.journalReminderEnabled)
                put("journalReminderHour", it.journalReminderHour)
                put("journalReminderMinute", it.journalReminderMinute)
                put("quickNoteNotificationEnabled", it.quickNoteNotificationEnabled)
            })
        }
        return json.toString()
    }
    private suspend fun importAllData(jsonString: String, pathMapping: Map<String, String> = emptyMap()) {
        val json = JSONObject(jsonString)
        database.journalDao().deleteAllEntries()
        val journalsArray = json.optJSONArray("journals")
        journalsArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                val remappedPhotos = remapJsonStringField(obj.optString("photos", "[]"), pathMapping)
                val remappedVoiceNotes = remapJsonStringField(obj.optString("voiceNotes", "[]"), pathMapping)
                database.journalDao().insertEntry(JournalEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    content = obj.getString("content"),
                    mood = obj.optString("mood", ""),
                    tags = obj.optString("tags", ""),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    isArchived = obj.optBoolean("isArchived", false),
                    isPinned = obj.optBoolean("isPinned", false),
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                    voiceNotes = remappedVoiceNotes,
                    weather = if (obj.has("weather") && !obj.isNull("weather")) obj.getString("weather") else null,
                    location = if (obj.has("location") && !obj.isNull("location")) obj.getString("location") else null,
                    photos = remappedPhotos,
                    wordCount = obj.optInt("wordCount", 0),
                    readingTime = obj.optInt("readingTime", 0),
                    color = obj.optString("color", ""),
                    reminder = if (obj.has("reminder") && !obj.isNull("reminder")) obj.getString("reminder") else null,
                    linkedEntries = obj.optString("linkedEntries", "[]"),
                    characterCount = obj.optInt("characterCount", 0),
                    paragraphCount = obj.optInt("paragraphCount", 0),
                    sentenceCount = obj.optInt("sentenceCount", 0)
                ))
            }
        }
        database.noteDao().deleteAllNotes()
        val notesArray = json.optJSONArray("notes")
        notesArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                val remappedAttachments = remapJsonStringField(obj.optString("attachments", "[]"), pathMapping)
                val remappedDrawings = remapJsonStringField(obj.optString("drawings", "[]"), pathMapping)
                val remappedVoiceNotes = remapJsonStringField(obj.optString("voiceNotes", "[]"), pathMapping)
                database.noteDao().insertNote(NoteEntity(
                    id = obj.getString("id"),
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    plainTextContent = obj.optString("plainTextContent", ""),
                    category = obj.optString("category", "PERSONAL"),
                    tags = obj.optString("tags", "[]"),
                    color = obj.optString("color", "#FFFFFF"),
                    isPinned = obj.optBoolean("isPinned", false),
                    isArchived = obj.optBoolean("isArchived", false),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    isLocked = obj.optBoolean("isLocked", false),
                    lockPassword = if (obj.has("lockPassword") && !obj.isNull("lockPassword")) obj.getString("lockPassword") else null,
                    attachments = remappedAttachments,
                    checklistItems = obj.optString("checklistItems", "[]"),
                    tables = obj.optString("tables", "[]"),
                    codeBlocks = obj.optString("codeBlocks", "[]"),
                    drawings = remappedDrawings,
                    voiceNotes = remappedVoiceNotes,
                    links = obj.optString("links", "[]"),
                    reminder = if (obj.has("reminder") && !obj.isNull("reminder")) obj.getLong("reminder") else null,
                    recurringReminder = if (obj.has("recurringReminder") && !obj.isNull("recurringReminder")) obj.getString("recurringReminder") else null,
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                    lastAccessedAt = obj.optString("lastAccessedAt", ""),
                    collaborators = obj.optString("collaborators", "[]"),
                    folderId = if (obj.has("folderId") && !obj.isNull("folderId")) obj.getString("folderId") else null,
                    parentNoteId = if (obj.has("parentNoteId") && !obj.isNull("parentNoteId")) obj.getString("parentNoteId") else null,
                    templateId = if (obj.has("templateId") && !obj.isNull("templateId")) obj.getString("templateId") else null,
                    wordCount = obj.optInt("wordCount", 0),
                    characterCount = obj.optInt("characterCount", 0),
                    readingTime = obj.optInt("readingTime", 0),
                    version = obj.optInt("version", 1),
                    versionHistory = obj.optString("versionHistory", "[]"),
                    formatting = obj.optString("formatting", "{}"),
                    layout = obj.optString("layout", "STANDARD"),
                    priority = obj.optString("priority", "NONE"),
                    status = obj.optString("status", "ACTIVE"),
                    location = if (obj.has("location") && !obj.isNull("location")) obj.getString("location") else null,
                    weather = if (obj.has("weather") && !obj.isNull("weather")) obj.getString("weather") else null,
                    mood = if (obj.has("mood") && !obj.isNull("mood")) obj.getString("mood") else null,
                    customFields = obj.optString("customFields", "{}"),
                    aiSummary = if (obj.has("aiSummary") && !obj.isNull("aiSummary")) obj.getString("aiSummary") else null,
                    aiKeywords = obj.optString("aiKeywords", "[]"),
                    relatedNotes = obj.optString("relatedNotes", "[]"),
                    exportFormats = obj.optString("exportFormats", "[]")
                ))
            }
        }
        database.noteFolderDao().deleteAllFolders()
        val foldersArray = json.optJSONArray("noteFolders")
        foldersArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.noteFolderDao().insertFolder(NoteFolderEntity(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    color = obj.optString("color", "#FFFFFF"),
                    icon = obj.optString("icon", "📁"),
                    parentId = if (obj.has("parentId") && !obj.isNull("parentId")) obj.getString("parentId") else null,
                    createdAt = obj.optString("createdAt", ""),
                    isEncrypted = obj.optBoolean("isEncrypted", false),
                    sortOrder = obj.optInt("sortOrder", 0),
                    viewMode = obj.optString("viewMode", "GRID")
                ))
            }
        }
        database.projectTaskDao().deleteAllTasks()
        val projectTasksArray = json.optJSONArray("projectTasks")
        projectTasksArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                val remappedAttachments = remapJsonStringField(obj.optString("attachments", "[]"), pathMapping)
                database.projectTaskDao().insertTask(ProjectTaskEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    status = obj.optString("status", "NOT_STARTED"),
                    priority = obj.optString("priority", "MEDIUM"),
                    tags = obj.optString("tags", "[]"),
                    assignee = if (obj.has("assignee") && !obj.isNull("assignee")) obj.getString("assignee") else null,
                    dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.getString("dueDate") else null,
                    estimatedHours = if (obj.has("estimatedHours") && !obj.isNull("estimatedHours")) obj.getDouble("estimatedHours").toFloat() else null,
                    actualHours = if (obj.has("actualHours") && !obj.isNull("actualHours")) obj.getDouble("actualHours").toFloat() else null,
                    createdAt = obj.optString("createdAt", ""),
                    updatedAt = obj.optString("updatedAt", ""),
                    completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.getString("completedAt") else null,
                    projectId = if (obj.has("projectId") && !obj.isNull("projectId")) obj.getString("projectId") else null,
                    subtasks = obj.optString("subtasks", "[]"),
                    attachments = remappedAttachments,
                    comments = obj.optString("comments", "[]")
                ))
            }
        }
        database.projectTaskDao().deleteAllProjects()
        val taskProjectsArray = json.optJSONArray("taskProjects")
        taskProjectsArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.projectTaskDao().insertProject(TaskProjectEntity(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.optString("description", ""),
                    color = obj.optLong("color", 0xFF000000),
                    icon = obj.optString("icon", "📁"),
                    createdAt = obj.optString("createdAt", "")
                ))
            }
        }
        database.todoDao().deleteAllTodos()
        val todosArray = json.optJSONArray("todos")
        todosArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.todoDao().insertTodo(TodoEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    isCompleted = obj.getBoolean("isCompleted"),
                    priority = obj.optString("priority", "Medium"),
                    dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.getLong("dueDate") else null,
                    createdAt = obj.getLong("createdAt")
                ))
            }
        }
        database.scheduleDao().deleteAllSchedules()
        val schedulesArray = json.optJSONArray("schedules")
        schedulesArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.scheduleDao().insertSchedule(ScheduleEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    timeHour = obj.optInt("timeHour", 0),
                    timeMinute = obj.optInt("timeMinute", 0),
                    daysOfWeek = obj.optString("daysOfWeek", ""),
                    isEnabled = obj.optBoolean("isEnabled", true)
                ))
            }
        }
        database.reminderDao().deleteAllReminders()
        val remindersArray = json.optJSONArray("reminders")
        remindersArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.reminderDao().insertReminder(ReminderEntity(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.optString("description", ""),
                    dateTime = obj.getLong("dateTime"),
                    category = obj.optString("category", "General"),
                    isCompleted = obj.getBoolean("isCompleted"),
                    repeatType = obj.optString("repeatType", "None")
                ))
            }
        }
        database.promptDao().deleteAllPrompts()
        val promptsArray = json.optJSONArray("prompts")
        promptsArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.promptDao().insertPrompt(PromptEntity(
                    id = obj.getString("id"),
                    promptText = obj.getString("promptText"),
                    category = obj.getString("category"),
                    response = obj.optString("response", ""),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                ))
            }
        }
        database.quickNoteDao().deleteAllQuickNotes()
        val quickNotesArray = json.optJSONArray("quickNotes")
        quickNotesArray?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                database.quickNoteDao().insertQuickNote(QuickNoteEntity(
                    id = obj.getString("id"),
                    content = obj.getString("content"),
                    createdAt = obj.getLong("createdAt")
                ))
            }
        }
        val settingsObj = json.optJSONObject("settings")
        settingsObj?.let {
            database.settingsDao().insertSettings(SettingsEntity(
                id = 1,
                pinCode = it.optString("pinCode", ""),
                notificationsEnabled = it.optBoolean("notificationsEnabled", true),
                journalReminderEnabled = it.optBoolean("journalReminderEnabled", false),
                journalReminderHour = it.optInt("journalReminderHour", 20),
                journalReminderMinute = it.optInt("journalReminderMinute", 0),
                quickNoteNotificationEnabled = it.optBoolean("quickNoteNotificationEnabled", false)
            ))
        }
    }
    fun scheduleAutoSync(intervalHours: Int) {
        val hour = 20 
        val minute = 0
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val dailyWorkRequest = PeriodicWorkRequestBuilder<AutoSyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "auto_sync",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
    fun cancelAutoSync() {
        WorkManager.getInstance(context).cancelUniqueWork("auto_sync")
    }
    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        if (calendar.timeInMillis <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis - currentTime
    }
}
class AutoSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val sync = GoogleDriveSync(applicationContext, database)
        return try {
            val result = sync.syncToGoogleDrive()
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
