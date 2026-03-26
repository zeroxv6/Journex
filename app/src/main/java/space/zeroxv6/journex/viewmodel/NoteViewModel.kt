package space.zeroxv6.journex.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import space.zeroxv6.journex.data.AppDatabase
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.repository.NoteRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao(), database.noteFolderDao())
        loadNotes()
        loadFolders()
    }
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set
    var folders by mutableStateOf<List<NoteFolder>>(emptyList())
        private set
    var currentNote by mutableStateOf<Note?>(null)
        private set
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf<NoteCategory?>(null)
    var selectedFolder by mutableStateOf<NoteFolder?>(null)
    var selectedTags by mutableStateOf<List<String>>(emptyList())
    var sortOption by mutableStateOf(NoteSortOption.DATE_UPDATED)
    var viewMode by mutableStateOf(NoteViewMode.GRID)
    var showArchived by mutableStateOf(false)
    var showFavorites by mutableStateOf(false)
    private fun loadNotes() {
        viewModelScope.launch {
            repository.allNotes.collect { notesList ->
                notes = notesList
            }
        }
    }
    private fun loadFolders() {
        viewModelScope.launch {
            repository.allFolders.collect { foldersList ->
                folders = foldersList
            }
        }
    }
    fun createNewNote() {
        currentNote = Note()
    }
    fun loadNote(noteId: String) {
        viewModelScope.launch {
            currentNote = repository.getNoteById(noteId)?.copy(
                lastAccessedAt = LocalDateTime.now()
            )
            currentNote?.let {
                repository.updateNote(it)
            }
        }
    }
    fun updateCurrentNote(note: Note) {
        val words = note.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentNote = note.copy(
            updatedAt = LocalDateTime.now(),
            wordCount = words.size,
            characterCount = note.content.length,
            readingTime = (words.size / 200).coerceAtLeast(1)
        )
    }
    fun saveNote() {
        currentNote?.let { note ->
            viewModelScope.launch {
                repository.insertNote(note)
                currentNote = null
            }
        }
    }
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.deleteNote(note)
            }
        }
    }
    fun togglePin(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(isPinned = !note.isPinned))
            }
        }
    }
    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(isFavorite = !note.isFavorite))
            }
        }
    }
    fun archiveNote(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(isArchived = true))
            }
        }
    }
    fun unarchiveNote(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(isArchived = false))
            }
        }
    }
    fun duplicateNote(noteId: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { original ->
                val duplicate = original.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "${original.title} (Copy)",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    isPinned = false
                )
                repository.insertNote(duplicate)
            }
        }
    }
    fun moveToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(folderId = folderId))
            }
        }
    }
    fun addTagToNote(noteId: String, tag: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                if (!note.tags.contains(tag)) {
                    repository.updateNote(note.copy(tags = note.tags + tag))
                }
            }
        }
    }
    fun removeTagFromNote(noteId: String, tag: String) {
        viewModelScope.launch {
            notes.find { it.id == noteId }?.let { note ->
                repository.updateNote(note.copy(tags = note.tags - tag))
            }
        }
    }
    fun addChecklistItem(item: ChecklistItem) {
        currentNote?.let { note ->
            updateCurrentNote(note.copy(
                checklistItems = note.checklistItems + item
            ))
        }
    }
    fun updateChecklistItem(itemId: String, isChecked: Boolean) {
        currentNote?.let { note ->
            val updatedItems = note.checklistItems.map { item ->
                if (item.id == itemId) item.copy(isChecked = isChecked) else item
            }
            updateCurrentNote(note.copy(checklistItems = updatedItems))
        }
    }
    fun removeChecklistItem(itemId: String) {
        currentNote?.let { note ->
            updateCurrentNote(note.copy(
                checklistItems = note.checklistItems.filter { it.id != itemId }
            ))
        }
    }
    fun addAttachment(attachment: NoteAttachment) {
        currentNote?.let { note ->
            val finalAttachment = if (attachment.type == AttachmentType.IMAGE) {
                val internalPath = copyImageToInternalStorage(attachment.uri)
                if (internalPath != null) {
                    attachment.copy(uri = internalPath)
                } else {
                    attachment
                }
            } else {
                attachment
            }
            updateCurrentNote(note.copy(
                attachments = note.attachments + finalAttachment
            ))
        }
    }
    private fun copyImageToInternalStorage(uriString: String): String? {
        return try {
            val context = getApplication<Application>().applicationContext
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "note_image_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
            val imagesDir = java.io.File(context.filesDir, "note_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val imageFile = java.io.File(imagesDir, fileName)
            val outputStream = imageFile.outputStream()
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun removeAttachment(attachmentId: String) {
        currentNote?.let { note ->
            val attachment = note.attachments.find { it.id == attachmentId }
            attachment?.let { att ->
                if (att.type == AttachmentType.IMAGE) {
                    try {
                        val file = java.io.File(att.uri)
                        if (file.exists() && file.absolutePath.contains("note_images")) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            updateCurrentNote(note.copy(
                attachments = note.attachments.filter { it.id != attachmentId }
            ))
        }
    }
    fun createFolder(name: String, color: String, icon: String) {
        viewModelScope.launch {
            val folder = NoteFolder(
                name = name,
                color = color,
                icon = icon
            )
            repository.insertFolder(folder)
        }
    }
    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folders.find { it.id == folderId }?.let { folder ->
                repository.deleteFolder(folder)
                notes.filter { it.folderId == folderId }.forEach { note ->
                    repository.updateNote(note.copy(folderId = null))
                }
            }
        }
    }
    fun getFilteredNotes(): List<Note> {
        var filtered = if (showArchived) {
            notes.filter { it.isArchived }
        } else if (showFavorites) {
            notes.filter { it.isFavorite && !it.isArchived }
        } else {
            notes.filter { !it.isArchived }
        }
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
        selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        selectedFolder?.let { folder ->
            filtered = filtered.filter { it.folderId == folder.id }
        }
        if (selectedTags.isNotEmpty()) {
            filtered = filtered.filter { note ->
                selectedTags.any { tag -> note.tags.contains(tag) }
            }
        }
        val pinned = filtered.filter { it.isPinned }
        val unpinned = filtered.filter { !it.isPinned }
        val sortedPinned = sortNotes(pinned)
        val sortedUnpinned = sortNotes(unpinned)
        return sortedPinned + sortedUnpinned
    }
    private fun sortNotes(list: List<Note>): List<Note> {
        return when (sortOption) {
            NoteSortOption.DATE_CREATED_DESC -> list.sortedByDescending { it.createdAt }
            NoteSortOption.DATE_CREATED_ASC -> list.sortedBy { it.createdAt }
            NoteSortOption.DATE_UPDATED -> list.sortedByDescending { it.updatedAt }
            NoteSortOption.DATE_ACCESSED -> list.sortedByDescending { it.lastAccessedAt }
            NoteSortOption.TITLE_ASC -> list.sortedBy { it.title }
            NoteSortOption.TITLE_DESC -> list.sortedByDescending { it.title }
            NoteSortOption.CATEGORY -> list.sortedBy { it.category.ordinal }
            NoteSortOption.COLOR -> list.sortedBy { it.color }
            NoteSortOption.PRIORITY -> list.sortedByDescending { it.priority.ordinal }
            NoteSortOption.SIZE -> list.sortedByDescending { it.characterCount }
            NoteSortOption.CUSTOM -> list
        }
    }
    fun getAllTags(): List<String> {
        return notes.flatMap { it.tags }.distinct().sorted()
    }
    fun clearCurrentNote() {
        currentNote = null
    }
}
