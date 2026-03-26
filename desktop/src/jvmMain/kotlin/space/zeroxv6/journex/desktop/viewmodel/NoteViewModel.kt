package space.zeroxv6.journex.desktop.viewmodel
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.util.UUID
class NoteViewModel(
    private val dataStore: JsonDataStore
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    val allNotes: StateFlow<List<FullNote>> = dataStore.fullNotes
    val allFolders: StateFlow<List<NoteFolder>> = dataStore.noteFolders
    private val _currentNote = MutableStateFlow<FullNote?>(null)
    val currentNote: StateFlow<FullNote?> = _currentNote.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow<NoteCategory?>(null)
    val selectedCategory: StateFlow<NoteCategory?> = _selectedCategory.asStateFlow()
    private val _sortBy = MutableStateFlow(NoteSortOption.DATE_CREATED_DESC)
    val sortBy: StateFlow<NoteSortOption> = _sortBy.asStateFlow()
    private val _viewMode = MutableStateFlow(NoteViewMode.GRID)
    val viewMode: StateFlow<NoteViewMode> = _viewMode.asStateFlow()
    private val _filter = MutableStateFlow(NoteFilter())
    val filter: StateFlow<NoteFilter> = _filter.asStateFlow()
    val filteredNotes: StateFlow<List<FullNote>> = combine(
        allNotes,
        combine(searchQuery, selectedCategory) { q, c -> Pair(q, c) },
        combine(sortBy, filter) { s, f -> Pair(s, f) }
    ) { notesList, searchAndCat, sortAndFilter ->
        val (query, category) = searchAndCat
        val (sort, noteFilter) = sortAndFilter
        var filtered = notesList.filter { it.status != NoteStatus.ARCHIVED }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.any { t -> t.contains(query, ignoreCase = true) }
            }
        }
        category?.let { c -> filtered = filtered.filter { it.category == c } }
        if (noteFilter.categories.isNotEmpty()) {
            filtered = filtered.filter { it.category in noteFilter.categories }
        }
        if (noteFilter.tags.isNotEmpty()) {
            filtered = filtered.filter { note -> note.tags.any { it in noteFilter.tags } }
        }
        if (noteFilter.priorities.isNotEmpty()) {
            filtered = filtered.filter { it.priority in noteFilter.priorities }
        }
        noteFilter.hasAttachments?.let { has ->
            filtered = filtered.filter { (it.attachments.isNotEmpty()) == has }
        }
        val pinned = filtered.filter { it.isPinned }
        val unpinned = filtered.filter { !it.isPinned }
        sortNotes(pinned, sort) + sortNotes(unpinned, sort)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private fun sortNotes(list: List<FullNote>, sort: NoteSortOption): List<FullNote> {
        return when (sort) {
            NoteSortOption.DATE_CREATED_DESC -> list.sortedByDescending { it.createdAt }
            NoteSortOption.DATE_CREATED_ASC -> list.sortedBy { it.createdAt }
            NoteSortOption.DATE_UPDATED -> list.sortedByDescending { it.updatedAt }
            NoteSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            NoteSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            NoteSortOption.CATEGORY -> list.sortedBy { it.category.ordinal }
            NoteSortOption.PRIORITY -> list.sortedByDescending { it.priority.ordinal }
        }
    }
    fun createNewNote(): FullNote {
        val note = FullNote()
        _currentNote.value = note
        return note
    }
    fun loadNote(noteId: String) {
        _currentNote.value = dataStore.getFullNote(noteId)
    }
    fun updateCurrentNote(note: FullNote) {
        val words = note.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        _currentNote.value = note.copy(
            updatedAt = LocalDateTime.now(),
            wordCount = words.size,
            characterCount = note.content.length,
            readingTime = (words.size / 200).coerceAtLeast(1),
            plainTextContent = note.content
        )
    }
    fun saveNote() {
        _currentNote.value?.let { note ->
            dataStore.saveFullNote(note)
            _currentNote.value = null
        }
    }
    fun saveNoteAndReturn(note: FullNote) {
        val words = note.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val updated = note.copy(
            updatedAt = LocalDateTime.now(),
            wordCount = words.size,
            characterCount = note.content.length,
            readingTime = (words.size / 200).coerceAtLeast(1),
            plainTextContent = note.content
        )
        dataStore.saveFullNote(updated)
    }
    fun deleteNote(noteId: String) {
        dataStore.deleteFullNote(noteId)
    }
    fun togglePin(noteId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(isPinned = !note.isPinned))
        }
    }
    fun toggleFavorite(noteId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(isFavorite = !note.isFavorite))
        }
    }
    fun archiveNote(noteId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(status = NoteStatus.ARCHIVED, isArchived = true))
        }
    }
    fun unarchiveNote(noteId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(status = NoteStatus.ACTIVE, isArchived = false))
        }
    }
    fun duplicateNote(noteId: String) {
        dataStore.getFullNote(noteId)?.let { original ->
            val duplicate = original.copy(
                id = UUID.randomUUID().toString(),
                title = "${original.title} (Copy)",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            dataStore.saveFullNote(duplicate)
        }
    }
    fun updateNoteStatus(noteId: String, newStatus: NoteStatus) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(status = newStatus, updatedAt = LocalDateTime.now()))
        }
    }
    fun addTagToNote(noteId: String, tag: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            if (tag !in note.tags) {
                dataStore.saveFullNote(note.copy(tags = note.tags + tag, updatedAt = LocalDateTime.now()))
            }
        }
    }
    fun removeTagFromNote(noteId: String, tag: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(tags = note.tags - tag, updatedAt = LocalDateTime.now()))
        }
    }
    fun addChecklistItem(noteId: String, text: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            val item = ChecklistItem(text = text, order = note.checklistItems.size)
            dataStore.saveFullNote(note.copy(checklistItems = note.checklistItems + item, updatedAt = LocalDateTime.now()))
        }
    }
    fun toggleChecklistItem(noteId: String, itemId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            val updated = note.checklistItems.map {
                if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
            }
            dataStore.saveFullNote(note.copy(checklistItems = updated, updatedAt = LocalDateTime.now()))
        }
    }
    fun removeChecklistItem(noteId: String, itemId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            val updated = note.checklistItems.filter { it.id != itemId }
            dataStore.saveFullNote(note.copy(checklistItems = updated, updatedAt = LocalDateTime.now()))
        }
    }
    fun addAttachment(noteId: String, attachment: NoteAttachment) {
        dataStore.getFullNote(noteId)?.let { note ->
            dataStore.saveFullNote(note.copy(attachments = note.attachments + attachment, updatedAt = LocalDateTime.now()))
        }
    }
    fun removeAttachment(noteId: String, attachmentId: String) {
        dataStore.getFullNote(noteId)?.let { note ->
            val updated = note.attachments.filter { it.id != attachmentId }
            dataStore.saveFullNote(note.copy(attachments = updated, updatedAt = LocalDateTime.now()))
        }
    }
    fun createFolder(name: String, color: String = "#FFFFFF", icon: String = "📁") {
        val folder = NoteFolder(name = name, color = color, icon = icon)
        dataStore.saveNoteFolder(folder)
    }
    fun deleteFolder(folderId: String) {
        dataStore.deleteNoteFolder(folderId)
    }
    fun clearCurrentNote() {
        _currentNote.value = null
    }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(category: NoteCategory?) { _selectedCategory.value = category }
    fun setSortBy(sort: NoteSortOption) { _sortBy.value = sort }
    fun setViewMode(mode: NoteViewMode) { _viewMode.value = mode }
    fun setFilter(filter: NoteFilter) { _filter.value = filter }
    fun getAllTags(): List<String> {
        return allNotes.value.flatMap { it.tags }.distinct().sorted()
    }
    fun exportNoteAsText(noteId: String): String {
        val note = dataStore.getFullNote(noteId) ?: return ""
        return buildString {
            appendLine("=" .repeat(50))
            appendLine(note.title.ifEmpty { "Untitled Note" })
            appendLine("=" .repeat(50))
            appendLine()
            appendLine("Category: ${note.category.label}")
            if (note.tags.isNotEmpty()) appendLine("Tags: ${note.tags.joinToString(", ") { "#$it" }}")
            appendLine("Status: ${note.status.label}")
            appendLine()
            appendLine("-" .repeat(50))
            appendLine()
            appendLine(note.content)
            if (note.checklistItems.isNotEmpty()) {
                appendLine()
                appendLine("Checklist:")
                note.checklistItems.forEach {
                    appendLine("  ${if (it.isChecked) "☑" else "☐"} ${it.text}")
                }
            }
            appendLine()
            appendLine("-" .repeat(50))
            appendLine("Words: ${note.wordCount} | Characters: ${note.characterCount}")
        }
    }
}
