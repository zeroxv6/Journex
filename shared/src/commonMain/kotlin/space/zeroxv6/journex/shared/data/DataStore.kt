package space.zeroxv6.journex.shared.data
import space.zeroxv6.journex.shared.model.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.time.*
import java.time.temporal.ChronoUnit
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(`in`: JsonReader): LocalDateTime? {
        val str = `in`.nextString()
        return if (str.isNullOrEmpty()) null else LocalDateTime.parse(str)
    }
}
class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(`in`: JsonReader): LocalDate? {
        val str = `in`.nextString()
        return if (str.isNullOrEmpty()) null else LocalDate.parse(str)
    }
}
class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(`in`: JsonReader): LocalTime? {
        val str = `in`.nextString()
        return if (str.isNullOrEmpty()) null else LocalTime.parse(str)
    }
}
class DayOfWeekSetAdapter : TypeAdapter<Set<DayOfWeek>>() {
    override fun write(out: JsonWriter, value: Set<DayOfWeek>?) {
        if (value == null) out.nullValue()
        else out.value(value.joinToString(",") { it.name })
    }
    override fun read(`in`: JsonReader): Set<DayOfWeek> {
        val str = `in`.nextString()
        return if (str.isNullOrEmpty()) emptySet()
        else str.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }
}
/**
 * Cross-platform data store using JSON file persistence
 * Works on both Android (with context) and Desktop
 */
class JsonDataStore(private val dataDir: File) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .create()
    private val journalFile = File(dataDir, "journals.json")
    private val tasksFile = File(dataDir, "tasks.json")
    private val remindersFile = File(dataDir, "reminders.json")
    private val schedulesFile = File(dataDir, "schedules.json")
    private val promptsFile = File(dataDir, "prompts.json")
    private val notesFile = File(dataDir, "notes.json")
    private val templatesFile = File(dataDir, "templates.json")
    private val settingsFile = File(dataDir, "settings.json")
    private val projectTasksFile = File(dataDir, "project_tasks.json")
    private val taskProjectsFile = File(dataDir, "task_projects.json")
    private val fullNotesFile = File(dataDir, "full_notes.json")
    private val noteFoldersFile = File(dataDir, "note_folders.json")
    private val _journals = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journals: StateFlow<List<JournalEntry>> = _journals.asStateFlow()
    private val _tasks = MutableStateFlow<List<TodoTask>>(emptyList())
    val tasks: StateFlow<List<TodoTask>> = _tasks.asStateFlow()
    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()
    private val _schedules = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val schedules: StateFlow<List<ScheduleItem>> = _schedules.asStateFlow()
    private val _prompts = MutableStateFlow<List<SavedPrompt>>(emptyList())
    val prompts: StateFlow<List<SavedPrompt>> = _prompts.asStateFlow()
    private val _notes = MutableStateFlow<List<QuickNote>>(emptyList())
    val notes: StateFlow<List<QuickNote>> = _notes.asStateFlow()
    private val _templates = MutableStateFlow<List<CustomTemplate>>(emptyList())
    val templates: StateFlow<List<CustomTemplate>> = _templates.asStateFlow()
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    private val _projectTasks = MutableStateFlow<List<ProjectTask>>(emptyList())
    val projectTasks: StateFlow<List<ProjectTask>> = _projectTasks.asStateFlow()
    private val _taskProjects = MutableStateFlow<List<TaskProject>>(emptyList())
    val taskProjects: StateFlow<List<TaskProject>> = _taskProjects.asStateFlow()
    private val _fullNotes = MutableStateFlow<List<FullNote>>(emptyList())
    val fullNotes: StateFlow<List<FullNote>> = _fullNotes.asStateFlow()
    private val _noteFolders = MutableStateFlow<List<NoteFolder>>(emptyList())
    val noteFolders: StateFlow<List<NoteFolder>> = _noteFolders.asStateFlow()
    init {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        loadAll()
    }
    private fun loadAll() {
        _journals.value = loadList(journalFile) ?: emptyList()
        _tasks.value = loadList(tasksFile) ?: emptyList()
        _reminders.value = loadList(remindersFile) ?: emptyList()
        _schedules.value = loadList(schedulesFile) ?: emptyList()
        _prompts.value = loadList(promptsFile) ?: emptyList()
        _notes.value = loadList(notesFile) ?: emptyList()
        _templates.value = loadList(templatesFile) ?: emptyList()
        _settings.value = loadSettings() ?: AppSettings()
        _projectTasks.value = loadList(projectTasksFile) ?: emptyList()
        _taskProjects.value = loadList(taskProjectsFile) ?: emptyList()
        _fullNotes.value = loadList(fullNotesFile) ?: emptyList()
        _noteFolders.value = loadList(noteFoldersFile) ?: emptyList()
    }
    private inline fun <reified T> loadList(file: File): List<T>? {
        return try {
            if (file.exists()) {
                val type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(file.readText(), type)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun loadSettings(): AppSettings? {
        return try {
            if (settingsFile.exists()) {
                gson.fromJson(settingsFile.readText(), AppSettings::class.java)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun <T> saveList(file: File, list: List<T>) {
        try {
            file.writeText(gson.toJson(list))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun saveJournal(entry: JournalEntry) {
        val current = _journals.value.toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            current[index] = entry
        } else {
            current.add(entry)
        }
        _journals.value = current
        saveList(journalFile, current)
    }
    fun deleteJournal(id: String) {
        val current = _journals.value.filter { it.id != id }
        _journals.value = current
        saveList(journalFile, current)
    }
    fun getJournal(id: String): JournalEntry? = _journals.value.find { it.id == id }
    fun saveTask(task: TodoTask) {
        val current = _tasks.value.toMutableList()
        val index = current.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            current[index] = task
        } else {
            current.add(task)
        }
        _tasks.value = current
        saveList(tasksFile, current)
    }
    fun deleteTask(id: String) {
        val current = _tasks.value.filter { it.id != id }
        _tasks.value = current
        saveList(tasksFile, current)
    }
    fun saveReminder(reminder: Reminder) {
        val current = _reminders.value.toMutableList()
        val index = current.indexOfFirst { it.id == reminder.id }
        if (index >= 0) {
            current[index] = reminder
        } else {
            current.add(reminder)
        }
        _reminders.value = current
        saveList(remindersFile, current)
    }
    fun deleteReminder(id: String) {
        val current = _reminders.value.filter { it.id != id }
        _reminders.value = current
        saveList(remindersFile, current)
    }
    fun saveSchedule(schedule: ScheduleItem) {
        val current = _schedules.value.toMutableList()
        val index = current.indexOfFirst { it.id == schedule.id }
        if (index >= 0) {
            current[index] = schedule
        } else {
            current.add(schedule)
        }
        _schedules.value = current
        saveList(schedulesFile, current)
    }
    fun deleteSchedule(id: String) {
        val current = _schedules.value.filter { it.id != id }
        _schedules.value = current
        saveList(schedulesFile, current)
    }
    fun savePrompt(prompt: SavedPrompt) {
        val current = _prompts.value.toMutableList()
        val index = current.indexOfFirst { it.id == prompt.id }
        if (index >= 0) {
            current[index] = prompt
        } else {
            current.add(prompt)
        }
        _prompts.value = current
        saveList(promptsFile, current)
    }
    fun deletePrompt(id: String) {
        val current = _prompts.value.filter { it.id != id }
        _prompts.value = current
        saveList(promptsFile, current)
    }
    fun saveNote(note: QuickNote) {
        val current = _notes.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            current[index] = note
        } else {
            current.add(note)
        }
        _notes.value = current
        saveList(notesFile, current)
    }
    fun deleteNote(id: String) {
        val current = _notes.value.filter { it.id != id }
        _notes.value = current
        saveList(notesFile, current)
    }
    fun saveTemplate(template: CustomTemplate) {
        val current = _templates.value.toMutableList()
        val index = current.indexOfFirst { it.id == template.id }
        if (index >= 0) {
            current[index] = template
        } else {
            current.add(template)
        }
        _templates.value = current
        saveList(templatesFile, current)
    }
    fun deleteTemplate(id: String) {
        val current = _templates.value.filter { it.id != id }
        _templates.value = current
        saveList(templatesFile, current)
    }
    fun saveSettings(settings: AppSettings) {
        _settings.value = settings
        try {
            settingsFile.writeText(gson.toJson(settings))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun calculateStats(): JournalStats {
        val entries = _journals.value
        val tasks = _tasks.value
        if (entries.isEmpty()) {
            return JournalStats(completedTasks = tasks.count { it.isCompleted })
        }
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
            currentStreak = calculateCurrentStreak(entries),
            longestStreak = calculateLongestStreak(entries),
            totalWords = totalWords,
            averageWordsPerEntry = if (entries.isNotEmpty()) totalWords / entries.size else 0,
            mostUsedMood = moodCounts.maxByOrNull { it.value }?.key,
            mostUsedTags = tagCounts.entries.sortedByDescending { it.value }.take(5).map { it.key },
            entriesThisMonth = entriesThisMonth,
            entriesThisYear = entriesThisYear,
            completedTasks = tasks.count { it.isCompleted }
        )
    }
    private fun calculateCurrentStreak(entries: List<JournalEntry>): Int {
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
    private fun calculateLongestStreak(entries: List<JournalEntry>): Int {
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
    fun clearAllData() {
        _journals.value = emptyList()
        _tasks.value = emptyList()
        _reminders.value = emptyList()
        _schedules.value = emptyList()
        _prompts.value = emptyList()
        _notes.value = emptyList()
        _templates.value = emptyList()
        _settings.value = AppSettings()
        _projectTasks.value = emptyList()
        _taskProjects.value = emptyList()
        _fullNotes.value = emptyList()
        _noteFolders.value = emptyList()
        journalFile.delete()
        tasksFile.delete()
        remindersFile.delete()
        schedulesFile.delete()
        promptsFile.delete()
        notesFile.delete()
        templatesFile.delete()
        settingsFile.delete()
        projectTasksFile.delete()
        taskProjectsFile.delete()
        fullNotesFile.delete()
        noteFoldersFile.delete()
    }
    fun saveProjectTask(task: ProjectTask) {
        val current = _projectTasks.value.toMutableList()
        val index = current.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            current[index] = task
        } else {
            current.add(task)
        }
        _projectTasks.value = current
        saveList(projectTasksFile, current)
    }
    fun deleteProjectTask(id: String) {
        val current = _projectTasks.value.filter { it.id != id }
        _projectTasks.value = current
        saveList(projectTasksFile, current)
    }
    fun getProjectTask(id: String): ProjectTask? = _projectTasks.value.find { it.id == id }
    fun saveTaskProject(project: TaskProject) {
        val current = _taskProjects.value.toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            current[index] = project
        } else {
            current.add(project)
        }
        _taskProjects.value = current
        saveList(taskProjectsFile, current)
    }
    fun deleteTaskProject(id: String) {
        val current = _taskProjects.value.filter { it.id != id }
        _taskProjects.value = current
        saveList(taskProjectsFile, current)
    }
    fun saveFullNote(note: FullNote) {
        val current = _fullNotes.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            current[index] = note
        } else {
            current.add(note)
        }
        _fullNotes.value = current
        saveList(fullNotesFile, current)
    }
    fun deleteFullNote(id: String) {
        val current = _fullNotes.value.filter { it.id != id }
        _fullNotes.value = current
        saveList(fullNotesFile, current)
    }
    fun getFullNote(id: String): FullNote? = _fullNotes.value.find { it.id == id }
    fun saveNoteFolder(folder: NoteFolder) {
        val current = _noteFolders.value.toMutableList()
        val index = current.indexOfFirst { it.id == folder.id }
        if (index >= 0) {
            current[index] = folder
        } else {
            current.add(folder)
        }
        _noteFolders.value = current
        saveList(noteFoldersFile, current)
    }
    fun deleteNoteFolder(id: String) {
        val current = _noteFolders.value.filter { it.id != id }
        _noteFolders.value = current
        saveList(noteFoldersFile, current)
    }
}
