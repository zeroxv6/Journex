package space.zeroxv6.journex.repository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import space.zeroxv6.journex.data.NoteDao
import space.zeroxv6.journex.data.NoteEntity
import space.zeroxv6.journex.data.NoteFolderDao
import space.zeroxv6.journex.data.NoteFolderEntity
import space.zeroxv6.journex.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class NoteRepository(
    private val noteDao: NoteDao,
    private val folderDao: NoteFolderDao
) {
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes().map { entities ->
        entities.map { toNote(it) }
    }
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes().map { entities ->
        entities.map { toNote(it) }
    }
    val favoriteNotes: Flow<List<Note>> = noteDao.getFavoriteNotes().map { entities ->
        entities.map { toNote(it) }
    }
    val allFolders: Flow<List<NoteFolder>> = folderDao.getAllFolders().map { entities ->
        entities.map { toFolder(it) }
    }
    suspend fun insertNote(note: Note) {
        noteDao.insertNote(toEntity(note))
    }
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(toEntity(note))
    }
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(toEntity(note))
    }
    suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.let { toNote(it) }
    }
    fun getNotesByFolder(folderId: String): Flow<List<Note>> {
        return noteDao.getNotesByFolder(folderId).map { entities ->
            entities.map { toNote(it) }
        }
    }
    fun getNotesByCategory(category: NoteCategory): Flow<List<Note>> {
        return noteDao.getNotesByCategory(category.name).map { entities ->
            entities.map { toNote(it) }
        }
    }
    suspend fun insertFolder(folder: NoteFolder) {
        folderDao.insertFolder(toFolderEntity(folder))
    }
    suspend fun updateFolder(folder: NoteFolder) {
        folderDao.updateFolder(toFolderEntity(folder))
    }
    suspend fun deleteFolder(folder: NoteFolder) {
        folderDao.deleteFolder(toFolderEntity(folder))
    }
    suspend fun getAllTags(): List<String> {
        return noteDao.getAllTags()
            .flatMap { tagsJson ->
                try {
                    val type = object : TypeToken<List<String>>() {}.type
                    gson.fromJson<List<String>>(tagsJson, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            .distinct()
            .sorted()
    }
    private fun toEntity(note: Note): NoteEntity {
        return NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            plainTextContent = note.plainTextContent,
            category = note.category.name,
            tags = gson.toJson(note.tags),
            color = note.color,
            isPinned = note.isPinned,
            isArchived = note.isArchived,
            isFavorite = note.isFavorite,
            isLocked = note.isLocked,
            lockPassword = note.lockPassword,
            attachments = gson.toJson(note.attachments),
            checklistItems = gson.toJson(note.checklistItems),
            tables = gson.toJson(note.tables),
            codeBlocks = gson.toJson(note.codeBlocks),
            drawings = gson.toJson(note.drawings),
            voiceNotes = gson.toJson(note.voiceNotes),
            links = gson.toJson(note.links),
            reminder = note.reminder,
            recurringReminder = gson.toJson(note.recurringReminder),
            createdAt = note.createdAt.format(dateFormatter),
            updatedAt = note.updatedAt.format(dateFormatter),
            lastAccessedAt = note.lastAccessedAt.format(dateFormatter),
            collaborators = gson.toJson(note.collaborators),
            folderId = note.folderId,
            parentNoteId = note.parentNoteId,
            templateId = note.templateId,
            wordCount = note.wordCount,
            characterCount = note.characterCount,
            readingTime = note.readingTime,
            version = note.version,
            versionHistory = gson.toJson(note.versionHistory),
            formatting = gson.toJson(note.formatting),
            layout = note.layout.name,
            priority = note.priority.name,
            status = note.status.name,
            location = gson.toJson(note.location),
            weather = note.weather,
            mood = note.mood,
            customFields = gson.toJson(note.customFields),
            aiSummary = note.aiSummary,
            aiKeywords = gson.toJson(note.aiKeywords),
            relatedNotes = gson.toJson(note.relatedNotes),
            exportFormats = gson.toJson(note.exportFormats)
        )
    }
    private fun toNote(entity: NoteEntity): Note {
        val tagsType = object : TypeToken<List<String>>() {}.type
        val attachmentsType = object : TypeToken<List<NoteAttachment>>() {}.type
        val checklistType = object : TypeToken<List<ChecklistItem>>() {}.type
        val collaboratorsType = object : TypeToken<List<String>>() {}.type
        val tablesType = object : TypeToken<List<NoteTable>>() {}.type
        val codeBlocksType = object : TypeToken<List<CodeBlock>>() {}.type
        val drawingsType = object : TypeToken<List<Drawing>>() {}.type
        val voiceNotesType = object : TypeToken<List<VoiceNote>>() {}.type
        val linksType = object : TypeToken<List<NoteLink>>() {}.type
        val versionHistoryType = object : TypeToken<List<NoteVersion>>() {}.type
        val customFieldsType = object : TypeToken<Map<String, String>>() {}.type
        val exportFormatsType = object : TypeToken<List<ExportFormat>>() {}.type
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            plainTextContent = entity.plainTextContent,
            category = try { NoteCategory.valueOf(entity.category) } catch (e: Exception) { NoteCategory.OTHER },
            tags = try { gson.fromJson(entity.tags, tagsType) } catch (e: Exception) { emptyList() },
            color = entity.color,
            isPinned = entity.isPinned,
            isArchived = entity.isArchived,
            isFavorite = entity.isFavorite,
            isLocked = entity.isLocked,
            lockPassword = entity.lockPassword,
            attachments = try { gson.fromJson(entity.attachments, attachmentsType) } catch (e: Exception) { emptyList() },
            checklistItems = try { gson.fromJson(entity.checklistItems, checklistType) } catch (e: Exception) { emptyList() },
            tables = try { gson.fromJson(entity.tables, tablesType) } catch (e: Exception) { emptyList() },
            codeBlocks = try { gson.fromJson(entity.codeBlocks, codeBlocksType) } catch (e: Exception) { emptyList() },
            drawings = try { gson.fromJson(entity.drawings, drawingsType) } catch (e: Exception) { emptyList() },
            voiceNotes = try { gson.fromJson(entity.voiceNotes, voiceNotesType) } catch (e: Exception) { emptyList() },
            links = try { gson.fromJson(entity.links, linksType) } catch (e: Exception) { emptyList() },
            reminder = entity.reminder,
            recurringReminder = try { gson.fromJson(entity.recurringReminder, RecurringReminder::class.java) } catch (e: Exception) { null },
            createdAt = try { LocalDateTime.parse(entity.createdAt, dateFormatter) } catch (e: Exception) { LocalDateTime.now() },
            updatedAt = try { LocalDateTime.parse(entity.updatedAt, dateFormatter) } catch (e: Exception) { LocalDateTime.now() },
            lastAccessedAt = try { LocalDateTime.parse(entity.lastAccessedAt, dateFormatter) } catch (e: Exception) { LocalDateTime.now() },
            collaborators = try { gson.fromJson(entity.collaborators, collaboratorsType) } catch (e: Exception) { emptyList() },
            folderId = entity.folderId,
            parentNoteId = entity.parentNoteId,
            templateId = entity.templateId,
            wordCount = entity.wordCount,
            characterCount = entity.characterCount,
            readingTime = entity.readingTime,
            version = entity.version,
            versionHistory = try { gson.fromJson(entity.versionHistory, versionHistoryType) } catch (e: Exception) { emptyList() },
            formatting = try { gson.fromJson(entity.formatting, TextFormatting::class.java) } catch (e: Exception) { TextFormatting() },
            layout = try { NoteLayout.valueOf(entity.layout) } catch (e: Exception) { NoteLayout.STANDARD },
            priority = try { NotePriority.valueOf(entity.priority) } catch (e: Exception) { NotePriority.NONE },
            status = try { NoteStatus.valueOf(entity.status) } catch (e: Exception) { NoteStatus.ACTIVE },
            location = try { gson.fromJson(entity.location, NoteLocation::class.java) } catch (e: Exception) { null },
            weather = entity.weather,
            mood = entity.mood,
            customFields = try { gson.fromJson(entity.customFields, customFieldsType) } catch (e: Exception) { emptyMap() },
            aiSummary = entity.aiSummary,
            aiKeywords = try { gson.fromJson(entity.aiKeywords, tagsType) } catch (e: Exception) { emptyList() },
            relatedNotes = try { gson.fromJson(entity.relatedNotes, tagsType) } catch (e: Exception) { emptyList() },
            exportFormats = try { gson.fromJson(entity.exportFormats, exportFormatsType) } catch (e: Exception) { emptyList() }
        )
    }
    private fun toFolderEntity(folder: NoteFolder): NoteFolderEntity {
        return NoteFolderEntity(
            id = folder.id,
            name = folder.name,
            color = folder.color,
            icon = folder.icon,
            parentId = folder.parentId,
            createdAt = folder.createdAt.format(dateFormatter),
            isEncrypted = folder.isEncrypted,
            sortOrder = folder.sortOrder,
            viewMode = folder.viewMode.name
        )
    }
    private fun toFolder(entity: NoteFolderEntity): NoteFolder {
        return NoteFolder(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            icon = entity.icon,
            parentId = entity.parentId,
            createdAt = try { LocalDateTime.parse(entity.createdAt, dateFormatter) } catch (e: Exception) { LocalDateTime.now() },
            isEncrypted = entity.isEncrypted,
            sortOrder = entity.sortOrder,
            viewMode = try { NoteViewMode.valueOf(entity.viewMode) } catch (e: Exception) { NoteViewMode.GRID }
        )
    }
}
