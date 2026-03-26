package space.zeroxv6.journex.viewmodel
import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import space.zeroxv6.journex.data.AppDatabase
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.notification.AlarmScheduler
import space.zeroxv6.journex.notification.NotificationHelper
import space.zeroxv6.journex.notification.QuickNoteNotificationService
import space.zeroxv6.journex.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
class JournalViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val settingsRepository: SettingsRepository
    private val journalRepository: space.zeroxv6.journex.repository.JournalRepository
    init {
        val database = AppDatabase.getDatabase(context)
        settingsRepository = SettingsRepository(database.settingsDao())
        journalRepository = space.zeroxv6.journex.repository.JournalRepository(database.journalDao())
        NotificationHelper.createNotificationChannels(context)
        loadSettings()
        loadEntries()
    }
    var entries by mutableStateOf<List<JournalEntry>>(emptyList())
        private set
    private fun loadEntries() {
        viewModelScope.launch {
            journalRepository.allEntries.collect { entriesList ->
                entries = entriesList
            }
        }
    }
    var currentEntry by mutableStateOf<JournalEntry?>(null)
        private set
    var searchQuery by mutableStateOf("")
    var selectedMoodFilter by mutableStateOf<Mood?>(null)
    var selectedTagFilter by mutableStateOf<String?>(null)
    var sortBy by mutableStateOf(SortOption.DATE_DESC)
    var showArchived by mutableStateOf(false)
    var journalReminderEnabled by mutableStateOf(false)
    var journalReminderTime by mutableStateOf(java.time.LocalTime.of(20, 0))
    var notificationsEnabled by mutableStateOf(true)
    var quickNoteNotificationEnabled by mutableStateOf(false)
    var isLocked by mutableStateOf(false)
    var pinCode by mutableStateOf("")
        private set
    var isUnlockedThisSession by mutableStateOf(false)
        private set
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                settings?.let {
                    pinCode = it.pinCode
                    isLocked = it.pinCode.isNotEmpty()
                    notificationsEnabled = it.notificationsEnabled
                    journalReminderEnabled = it.journalReminderEnabled
                    quickNoteNotificationEnabled = it.quickNoteNotificationEnabled
                    journalReminderTime = java.time.LocalTime.of(it.journalReminderHour, it.journalReminderMinute)
                    if (journalReminderEnabled && notificationsEnabled) {
                        AlarmScheduler.scheduleJournalReminder(context, it.journalReminderHour, it.journalReminderMinute)
                    } else {
                        AlarmScheduler.cancelJournalReminder(context)
                    }
                    if (quickNoteNotificationEnabled) {
                        QuickNoteNotificationService.start(context)
                    } else {
                        QuickNoteNotificationService.stop(context)
                    }
                }
            }
        }
    }
    fun setPin(pin: String) {
        pinCode = pin
        isLocked = pin.isNotEmpty()
        viewModelScope.launch {
            settingsRepository.updatePinCode(pin)
        }
    }
    fun updateNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
            if (!enabled) {
                AlarmScheduler.cancelJournalReminder(context)
            } else if (journalReminderEnabled) {
                AlarmScheduler.scheduleJournalReminder(
                    context,
                    journalReminderTime.hour,
                    journalReminderTime.minute
                )
            }
        }
    }
    fun updateJournalReminderEnabled(enabled: Boolean) {
        journalReminderEnabled = enabled
        viewModelScope.launch {
            settingsRepository.updateJournalReminderEnabled(enabled)
            val prefs = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("reminder_enabled", enabled)
                putInt("reminder_hour", journalReminderTime.hour)
                putInt("reminder_minute", journalReminderTime.minute)
                apply()
            }
            if (enabled && notificationsEnabled) {
                AlarmScheduler.scheduleJournalReminder(
                    context,
                    journalReminderTime.hour,
                    journalReminderTime.minute
                )
            } else {
                AlarmScheduler.cancelJournalReminder(context)
            }
        }
    }
    fun updateJournalReminderTime(hour: Int, minute: Int) {
        journalReminderTime = java.time.LocalTime.of(hour, minute)
        viewModelScope.launch {
            settingsRepository.updateJournalReminderTime(hour, minute)
            val prefs = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt("reminder_hour", hour)
                putInt("reminder_minute", minute)
                apply()
            }
            if (journalReminderEnabled && notificationsEnabled) {
                AlarmScheduler.scheduleJournalReminder(context, hour, minute)
            }
        }
    }
    fun updateQuickNoteNotificationEnabled(enabled: Boolean) {
        quickNoteNotificationEnabled = enabled
        viewModelScope.launch {
            settingsRepository.updateQuickNoteNotificationEnabled(enabled)
            if (enabled) {
                QuickNoteNotificationService.start(context)
            } else {
                QuickNoteNotificationService.stop(context)
            }
        }
    }
    fun verifyPin(pin: String): Boolean {
        return pin == pinCode
    }
    fun unlock() {
        isLocked = false
        isUnlockedThisSession = true
    }
    fun lock() {
        if (pinCode.isNotEmpty()) {
            isLocked = true
            isUnlockedThisSession = false
        }
    }
    fun shouldShowPinLock(): Boolean {
        return pinCode.isNotEmpty() && !isUnlockedThisSession
    }
    var isRecording by mutableStateOf(false)
        private set
    var recordingDuration by mutableStateOf(0L)
        private set
    var isPlayingVoiceNote by mutableStateOf<String?>(null)
        private set
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingPath: String? = null
    private var recordingStartTime: Long = 0L
    fun createNewEntry() {
        currentEntry = JournalEntry()
    }
    fun updateCurrentEntry(entry: JournalEntry) {
        val words = entry.content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val sentences = entry.content.split("[.!?]+".toRegex()).filter { it.isNotBlank() }
        val paragraphs = entry.content.split("\n\n+".toRegex()).filter { it.isNotBlank() }
        currentEntry = entry.copy(
            updatedAt = LocalDateTime.now(),
            wordCount = words.size,
            characterCount = entry.content.length,
            sentenceCount = sentences.size,
            paragraphCount = paragraphs.size,
            readingTime = (words.size / 200).coerceAtLeast(1)
        )
    }
    fun saveEntry() {
        currentEntry?.let { entry ->
            viewModelScope.launch {
                journalRepository.insertEntry(entry)
            }
            currentEntry = null
        }
    }
    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { entry ->
                entry.photos.forEach { photoPath ->
                    try {
                        val file = File(photoPath)
                        if (file.exists() && file.absolutePath.contains("journal_images")) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                entry.voiceNotes.forEach { voiceNote ->
                    try {
                        val file = File(voiceNote.filePath)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                journalRepository.deleteEntry(entry)
            }
        }
    }
    fun toggleFavorite(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { entry ->
                journalRepository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
            }
        }
    }
    fun togglePin(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { entry ->
                journalRepository.updateEntry(entry.copy(isPinned = !entry.isPinned))
            }
        }
    }
    fun archiveEntry(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { entry ->
                journalRepository.updateEntry(entry.copy(isArchived = true))
            }
        }
    }
    fun unarchiveEntry(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { entry ->
                journalRepository.updateEntry(entry.copy(isArchived = false))
            }
        }
    }
    fun duplicateEntry(entryId: String) {
        viewModelScope.launch {
            entries.find { it.id == entryId }?.let { original ->
                val duplicate = original.copy(
                    id = UUID.randomUUID().toString(),
                    title = "${original.title} (Copy)",
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    voiceNotes = emptyList() 
                )
                journalRepository.insertEntry(duplicate)
            }
        }
    }
    fun loadEntry(entryId: String) {
        currentEntry = entries.find { it.id == entryId }
    }
    fun clearCurrentEntry() {
        currentEntry = null
    }
    fun getFilteredEntries(): List<JournalEntry> {
        var filtered = entries.filter { it.isArchived == showArchived }
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
            }
        }
        selectedMoodFilter?.let { mood ->
            filtered = filtered.filter { it.mood == mood }
        }
        selectedTagFilter?.let { tag ->
            filtered = filtered.filter { it.tags.contains(tag) }
        }
        val pinned = filtered.filter { it.isPinned }
        val unpinned = filtered.filter { !it.isPinned }
        val sortedPinned = sortEntries(pinned)
        val sortedUnpinned = sortEntries(unpinned)
        return sortedPinned + sortedUnpinned
    }
    private fun sortEntries(list: List<JournalEntry>): List<JournalEntry> {
        return when (sortBy) {
            SortOption.DATE_DESC -> list.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> list.sortedBy { it.createdAt }
            SortOption.TITLE_ASC -> list.sortedBy { it.title }
            SortOption.TITLE_DESC -> list.sortedByDescending { it.title }
            SortOption.MOOD -> list.sortedBy { it.mood.ordinal }
            SortOption.WORD_COUNT -> list.sortedByDescending { it.wordCount }
            SortOption.UPDATED -> list.sortedByDescending { it.updatedAt }
        }
    }
    fun getAllTags(): List<String> {
        return entries.flatMap { it.tags }.distinct().sorted()
    }
    fun getStats(): JournalStats {
        if (entries.isEmpty()) return JournalStats()
        val totalWords = entries.sumOf { it.wordCount }
        val moodCounts = entries.groupingBy { it.mood }.eachCount()
        val tagCounts = entries.flatMap { it.tags }.groupingBy { it }.eachCount()
        val now = LocalDate.now()
        val entriesThisMonth = entries.count {
            it.createdAt.toLocalDate().month == now.month &&
            it.createdAt.toLocalDate().year == now.year
        }
        val entriesThisYear = entries.count {
            it.createdAt.toLocalDate().year == now.year
        }
        return JournalStats(
            totalEntries = entries.size,
            currentStreak = calculateCurrentStreak(),
            longestStreak = calculateLongestStreak(),
            totalWords = totalWords,
            averageWordsPerEntry = if (entries.isNotEmpty()) totalWords / entries.size else 0,
            mostUsedMood = moodCounts.maxByOrNull { it.value }?.key,
            mostUsedTags = tagCounts.entries.sortedByDescending { it.value }.take(5).map { it.key },
            entriesThisMonth = entriesThisMonth,
            entriesThisYear = entriesThisYear
        )
    }
    private fun calculateCurrentStreak(): Int {
        if (entries.isEmpty()) return 0
        val sortedDates = entries.map { it.createdAt.toLocalDate() }.distinct().sortedDescending()
        var streak = 0
        var currentDate = LocalDate.now()
        for (date in sortedDates) {
            val daysDiff = ChronoUnit.DAYS.between(date, currentDate)
            if (daysDiff <= 1) {
                streak++
                currentDate = date
            } else {
                break
            }
        }
        return streak
    }
    private fun calculateLongestStreak(): Int {
        if (entries.isEmpty()) return 0
        val sortedDates = entries.map { it.createdAt.toLocalDate() }.distinct().sorted()
        var longestStreak = 1
        var currentStreak = 1
        for (i in 1 until sortedDates.size) {
            val daysDiff = ChronoUnit.DAYS.between(sortedDates[i - 1], sortedDates[i])
            if (daysDiff == 1L) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        return longestStreak
    }
    fun startRecording(outputPath: String) {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputPath)
                prepare()
                start()
            }
            currentRecordingPath = outputPath
            recordingStartTime = System.currentTimeMillis()
            recordingDuration = 0L
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun updateRecordingDuration() {
        if (isRecording) {
            recordingDuration = System.currentTimeMillis() - recordingStartTime
        }
    }
    fun stopRecording(): Pair<String?, Long>? {
        try {
            val duration = System.currentTimeMillis() - recordingStartTime
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordingDuration = 0L
            return currentRecordingPath?.let { it to duration }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    fun addVoiceNoteToCurrentEntry(voiceNote: JournalVoiceNote) {
        currentEntry?.let { entry ->
            updateCurrentEntry(entry.copy(
                voiceNotes = entry.voiceNotes + voiceNote
            ))
        }
    }
    fun playVoiceNote(filePath: String) {
        try {
            if (isPlayingVoiceNote == filePath && mediaPlayer != null) {
                mediaPlayer?.apply {
                    if (!isPlaying) start()
                }
                return
            }
            stopVoiceNote()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    isPlayingVoiceNote = null
                }
            }
            isPlayingVoiceNote = filePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun stopVoiceNote() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        isPlayingVoiceNote = null
    }
    fun pauseVoiceNote() {
        mediaPlayer?.apply {
            if (isPlaying) pause()
        }
    }
    fun resumeVoiceNote() {
        mediaPlayer?.apply {
            if (!isPlaying) start()
        }
    }
    fun togglePlayPause(filePath: String) {
        if (isPlayingVoiceNote == filePath) {
            mediaPlayer?.apply {
                if (isPlaying) {
                    pause()
                } else {
                    start()
                }
            }
        } else {
            playVoiceNote(filePath)
        }
    }
    fun isCurrentlyPlaying(filePath: String): Boolean {
        return isPlayingVoiceNote == filePath && mediaPlayer?.isPlaying == true
    }
    fun seekVoiceNote(position: Long) {
        try {
            mediaPlayer?.apply {
                val seekPos = position.toInt().coerceIn(0, duration)
                seekTo(seekPos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getVoiceNotePosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    fun getVoiceNoteDuration(): Long {
        return try {
            mediaPlayer?.duration?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    fun setPlaybackSpeed(speed: Float) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun deleteVoiceNote(voiceNoteId: String) {
        currentEntry?.let { entry ->
            updateCurrentEntry(entry.copy(
                voiceNotes = entry.voiceNotes.filter { it.id != voiceNoteId }
            ))
        }
    }
    fun addPhotoToCurrentEntry(photoUri: String) {
        currentEntry?.let { entry ->
            val savedPath = copyImageToInternalStorage(photoUri)
            if (savedPath != null) {
                updateCurrentEntry(entry.copy(
                    photos = entry.photos + savedPath
                ))
            }
        }
    }
    fun copyImageToInternalStorage(uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "journal_image_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, "journal_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val imageFile = File(imagesDir, fileName)
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
    fun removePhotoFromCurrentEntry(photoUri: String) {
        currentEntry?.let { entry ->
            try {
                val file = File(photoUri)
                if (file.exists() && file.absolutePath.contains("journal_images")) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateCurrentEntry(entry.copy(
                photos = entry.photos.filter { it != photoUri }
            ))
        }
    }
    fun exportEntryAsText(entryId: String): String {
        val entry = entries.find { it.id == entryId } ?: return ""
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
            if (entry.voiceNotes.isNotEmpty()) {
                appendLine("Voice Notes: ${entry.voiceNotes.size}")
            }
        }
    }
    fun exportAllEntriesAsText(): String {
        return buildString {
            appendLine("JOURNAL EXPORT")
            appendLine("=" .repeat(50))
            appendLine("Total Entries: ${entries.size}")
            appendLine("Export Date: ${LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))}")
            appendLine("=" .repeat(50))
            appendLine()
            appendLine()
            entries.sortedByDescending { it.createdAt }.forEach { entry ->
                append(exportEntryAsText(entry.id))
                appendLine()
                appendLine()
            }
        }
    }
    fun getEntriesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<JournalEntry> {
        return entries.filter { entry ->
            entry.createdAt.isAfter(startDate) && entry.createdAt.isBefore(endDate)
        }
    }
    fun getEntriesByTag(tag: String): List<JournalEntry> {
        return entries.filter { it.tags.contains(tag) }
    }
    fun getEntriesByMood(mood: Mood): List<JournalEntry> {
        return entries.filter { it.mood == mood }
    }
    fun getFavoriteEntries(): List<JournalEntry> {
        return entries.filter { it.isFavorite }
    }
    fun exportAllEntriesToFile(): String {
        val exportText = exportAllEntriesAsText()
        return exportText
    }
    fun getExportText(): String {
        return exportAllEntriesAsText()
    }
    fun clearAllData() {
        viewModelScope.launch {
            val database = space.zeroxv6.journex.data.AppDatabase.getDatabase(context)
            entries.forEach { entry ->
                entry.photos.forEach { photoPath ->
                    try {
                        val file = File(photoPath)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                entry.voiceNotes.forEach { voiceNote ->
                    try {
                        val file = File(voiceNote.filePath)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            try {
                val journalImagesDir = File(context.filesDir, "journal_images")
                if (journalImagesDir.exists()) journalImagesDir.deleteRecursively()
                val noteImagesDir = File(context.filesDir, "note_images")
                if (noteImagesDir.exists()) noteImagesDir.deleteRecursively()
                val noteDrawingsDir = File(context.filesDir, "note_drawings")
                if (noteDrawingsDir.exists()) noteDrawingsDir.deleteRecursively()
                val voiceNotesDir = File(context.filesDir, "voice_notes")
                if (voiceNotesDir.exists()) voiceNotesDir.deleteRecursively()
                val noteVoiceDir = File(context.filesDir, "note_voice")
                if (noteVoiceDir.exists()) noteVoiceDir.deleteRecursively()
                context.getExternalFilesDir(null)?.let { extDir ->
                    extDir.listFiles()?.forEach { file ->
                        if (file.isFile && (file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || 
                            file.name.endsWith(".png") || file.name.endsWith(".webp") ||
                            file.name.endsWith(".mp3") || file.name.endsWith(".m4a") || 
                            file.name.endsWith(".wav") || file.name.endsWith(".3gp"))) {
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            journalRepository.deleteAllEntries()
            database.noteDao().deleteAllNotes()
            database.noteFolderDao().deleteAllFolders()
            database.todoDao().deleteAllTodos()
            database.scheduleDao().deleteAllSchedules()
            database.reminderDao().deleteAllReminders()
            database.promptDao().deleteAllPrompts()
            database.projectTaskDao().deleteAllTasks()
            database.projectTaskDao().deleteAllProjects()
            database.settingsDao().insertSettings(space.zeroxv6.journex.data.SettingsEntity(
                id = 1,
                pinCode = "",
                notificationsEnabled = true,
                journalReminderEnabled = false,
                journalReminderHour = 20,
                journalReminderMinute = 0
            ))
        }
        currentEntry = null
        searchQuery = ""
        selectedMoodFilter = null
        selectedTagFilter = null
        pinCode = ""
        isLocked = false
        isUnlockedThisSession = false
        journalReminderEnabled = false
        notificationsEnabled = true
    }
    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }
    private fun addSampleEntries() {
        entries = emptyList()
    }
}
enum class SortOption(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    UPDATED("Recently Updated"),
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    MOOD("By Mood"),
    WORD_COUNT("By Word Count")
}
