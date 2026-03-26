package space.zeroxv6.journex.desktop.viewmodel
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
class JournalViewModel(
    private val dataStore: JsonDataStore
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    val entries: StateFlow<List<JournalEntry>> = dataStore.journals
    private val _currentEntry = MutableStateFlow<JournalEntry?>(null)
    val currentEntry: StateFlow<JournalEntry?> = _currentEntry.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedMoodFilter = MutableStateFlow<Mood?>(null)
    val selectedMoodFilter: StateFlow<Mood?> = _selectedMoodFilter.asStateFlow()
    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter: StateFlow<String?> = _selectedTagFilter.asStateFlow()
    private val _sortBy = MutableStateFlow(SortOption.DATE_DESC)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()
    val stats: StateFlow<JournalStats> = entries.map { 
        dataStore.calculateStats() 
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), JournalStats())
    val todayEntries: StateFlow<List<JournalEntry>> = entries.map { list ->
        list.filter { it.createdAt.toLocalDate() == LocalDate.now() && !it.isArchived }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val filteredEntries: StateFlow<List<JournalEntry>> = combine(
        entries,
        combine(searchQuery, selectedMoodFilter, selectedTagFilter) { q, m, t -> Triple(q, m, t) },
        combine(sortBy, showArchived) { s, a -> Pair(s, a) }
    ) { entriesList, filters, sortAndArchive ->
        val (query, mood, tag) = filters
        val (sort, archived) = sortAndArchive
        var filtered = entriesList.filter { it.isArchived == archived }
        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.any { t -> t.contains(query, ignoreCase = true) }
            }
        }
        mood?.let { m -> filtered = filtered.filter { it.mood == m } }
        tag?.let { t -> filtered = filtered.filter { it.tags.contains(t) } }
        val pinned = filtered.filter { it.isPinned }
        val unpinned = filtered.filter { !it.isPinned }
        sortEntries(pinned, sort) + sortEntries(unpinned, sort)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private fun sortEntries(list: List<JournalEntry>, sort: SortOption): List<JournalEntry> {
        return when (sort) {
            SortOption.DATE_DESC -> list.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> list.sortedBy { it.createdAt }
            SortOption.TITLE_ASC -> list.sortedBy { it.title }
            SortOption.TITLE_DESC -> list.sortedByDescending { it.title }
            SortOption.MOOD -> list.sortedBy { it.mood.ordinal }
            SortOption.WORD_COUNT -> list.sortedByDescending { it.wordCount }
            SortOption.UPDATED -> list.sortedByDescending { it.updatedAt }
        }
    }
    fun createNewEntry() {
        _currentEntry.value = JournalEntry()
    }
    fun loadEntry(id: String) {
        _currentEntry.value = dataStore.getJournal(id)
    }
    fun updateCurrentEntry(entry: JournalEntry) {
        val words = entry.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val sentences = entry.content.split("[.!?]+".toRegex()).filter { it.isNotBlank() }
        val paragraphs = entry.content.split("\n\n+".toRegex()).filter { it.isNotBlank() }
        _currentEntry.value = entry.copy(
            updatedAt = LocalDateTime.now(),
            wordCount = words.size,
            characterCount = entry.content.length,
            sentenceCount = sentences.size,
            paragraphCount = paragraphs.size,
            readingTime = (words.size / 200).coerceAtLeast(1)
        )
    }
    fun saveEntry() {
        _currentEntry.value?.let { entry ->
            dataStore.saveJournal(entry)
            _currentEntry.value = null
        }
    }
    fun deleteEntry(id: String) {
        dataStore.deleteJournal(id)
    }
    fun toggleFavorite(id: String) {
        dataStore.getJournal(id)?.let { entry ->
            dataStore.saveJournal(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
    fun togglePin(id: String) {
        dataStore.getJournal(id)?.let { entry ->
            dataStore.saveJournal(entry.copy(isPinned = !entry.isPinned))
        }
    }
    fun archiveEntry(id: String) {
        dataStore.getJournal(id)?.let { entry ->
            dataStore.saveJournal(entry.copy(isArchived = true))
        }
    }
    fun unarchiveEntry(id: String) {
        dataStore.getJournal(id)?.let { entry ->
            dataStore.saveJournal(entry.copy(isArchived = false))
        }
    }
    fun duplicateEntry(id: String) {
        dataStore.getJournal(id)?.let { original ->
            val duplicate = original.copy(
                id = java.util.UUID.randomUUID().toString(),
                title = "${original.title} (Copy)",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            dataStore.saveJournal(duplicate)
        }
    }
    fun clearCurrentEntry() {
        _currentEntry.value = null
    }
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun setMoodFilter(mood: Mood?) {
        _selectedMoodFilter.value = mood
    }
    fun setTagFilter(tag: String?) {
        _selectedTagFilter.value = tag
    }
    fun setSortBy(sort: SortOption) {
        _sortBy.value = sort
    }
    fun setShowArchived(show: Boolean) {
        _showArchived.value = show
    }
    fun getAllTags(): List<String> {
        return entries.value.flatMap { it.tags }.distinct().sorted()
    }
    fun exportEntryAsText(id: String): String {
        val entry = dataStore.getJournal(id) ?: return ""
        return buildString {
            appendLine("=" .repeat(50))
            appendLine(entry.title.ifEmpty { "Untitled Entry" })
            appendLine("=" .repeat(50))
            appendLine()
            appendLine("Date: ${entry.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))}")
            appendLine("Mood: ${entry.mood.label}")
            if (entry.tags.isNotEmpty()) {
                appendLine("Tags: ${entry.tags.joinToString(", ") { "#$it" }}")
            }
            appendLine()
            appendLine("-" .repeat(50))
            appendLine()
            appendLine(entry.content)
            appendLine()
            appendLine("-" .repeat(50))
            appendLine()
            appendLine("Words: ${entry.wordCount}")
            appendLine("Characters: ${entry.characterCount}")
            appendLine("Reading Time: ${entry.readingTime} min")
        }
    }
}
