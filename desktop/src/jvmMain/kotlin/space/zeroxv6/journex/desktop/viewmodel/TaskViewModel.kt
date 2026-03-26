package space.zeroxv6.journex.desktop.viewmodel
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
class TaskViewModel(
    private val dataStore: JsonDataStore
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    val allTasks: StateFlow<List<TodoTask>> = dataStore.tasks
    val incompleteTasks: StateFlow<List<TodoTask>> = dataStore.tasks.map { list ->
        list.filter { !it.isCompleted }.sortedByDescending { it.priority.ordinal }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedTasks: StateFlow<List<TodoTask>> = dataStore.tasks.map { list ->
        list.filter { it.isCompleted }.sortedByDescending { it.createdAt }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReminders: StateFlow<List<Reminder>> = dataStore.reminders
    val upcomingReminders: StateFlow<List<Reminder>> = dataStore.reminders.map { list ->
        list.filter { !it.isCompleted && it.dateTime.isAfter(LocalDateTime.now()) }
            .sortedBy { it.dateTime }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val completedReminders: StateFlow<List<Reminder>> = dataStore.reminders.map { list ->
        list.filter { it.isCompleted }.sortedByDescending { it.dateTime }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSchedules: StateFlow<List<ScheduleItem>> = dataStore.schedules
    val enabledSchedules: StateFlow<List<ScheduleItem>> = dataStore.schedules.map { list ->
        val today = java.time.DayOfWeek.from(java.time.LocalDate.now())
        list.filter { it.isEnabled && it.daysOfWeek.contains(today) }.sortedBy { it.time }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedPrompts: StateFlow<List<SavedPrompt>> = dataStore.prompts
    fun addTask(title: String, description: String = "", priority: Priority = Priority.MEDIUM, dueDate: LocalDate? = null) {
        val task = TodoTask(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            createdAt = LocalDateTime.now()
        )
        dataStore.saveTask(task)
    }
    fun updateTask(task: TodoTask) {
        dataStore.saveTask(task)
    }
    fun toggleTaskCompletion(task: TodoTask) {
        dataStore.saveTask(task.copy(isCompleted = !task.isCompleted))
    }
    fun toggleTaskCompletionById(id: String) {
        dataStore.tasks.value.find { it.id == id }?.let { task ->
            dataStore.saveTask(task.copy(isCompleted = !task.isCompleted))
        }
    }
    fun deleteTask(id: String) {
        dataStore.deleteTask(id)
    }
    fun addReminder(
        title: String,
        description: String = "",
        dateTime: LocalDateTime,
        category: ReminderCategory = ReminderCategory.GENERAL,
        repeatType: RepeatType = RepeatType.NONE
    ) {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            dateTime = dateTime,
            category = category,
            repeatType = repeatType
        )
        dataStore.saveReminder(reminder)
        val dateTimeMillis = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleReminder(
            reminder.id,
            title,
            description,
            dateTimeMillis
        )
    }
    fun updateReminder(reminder: Reminder) {
        dataStore.saveReminder(reminder)
        if (!reminder.isCompleted) {
            val dateTimeMillis = reminder.dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleReminder(
                reminder.id,
                reminder.title,
                reminder.description,
                dateTimeMillis
            )
        } else {
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelReminder(reminder.id)
        }
    }
    fun toggleReminderCompletion(reminder: Reminder) {
        val updated = reminder.copy(isCompleted = !reminder.isCompleted)
        dataStore.saveReminder(updated)
        if (updated.isCompleted) {
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelReminder(reminder.id)
        }
    }
    fun deleteReminder(id: String) {
        dataStore.deleteReminder(id)
        space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelReminder(id)
    }
    fun addSchedule(
        title: String,
        description: String = "",
        time: LocalTime,
        endTime: LocalTime? = null,
        daysOfWeek: Set<DayOfWeek>
    ) {
        val schedule = ScheduleItem(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            time = time,
            endTime = endTime,
            daysOfWeek = daysOfWeek
        )
        dataStore.saveSchedule(schedule)
        if (schedule.isEnabled) {
            val daysString = daysOfWeek.joinToString(",") { day ->
                when (day) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                }
            }
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleRecurringSchedule(
                schedule.id,
                title,
                description,
                time.hour,
                time.minute,
                daysString
            )
        }
    }
    fun updateSchedule(schedule: ScheduleItem) {
        dataStore.saveSchedule(schedule)
        if (schedule.isEnabled) {
            val daysString = schedule.daysOfWeek.joinToString(",") { day ->
                when (day) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                }
            }
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleRecurringSchedule(
                schedule.id,
                schedule.title,
                schedule.description,
                schedule.time.hour,
                schedule.time.minute,
                daysString
            )
        } else {
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelSchedule(schedule.id)
        }
    }
    fun toggleScheduleEnabled(schedule: ScheduleItem) {
        val updated = schedule.copy(isEnabled = !schedule.isEnabled)
        dataStore.saveSchedule(updated)
        if (updated.isEnabled) {
            val daysString = updated.daysOfWeek.joinToString(",") { day ->
                when (day) {
                    DayOfWeek.MONDAY -> "Mon"
                    DayOfWeek.TUESDAY -> "Tue"
                    DayOfWeek.WEDNESDAY -> "Wed"
                    DayOfWeek.THURSDAY -> "Thu"
                    DayOfWeek.FRIDAY -> "Fri"
                    DayOfWeek.SATURDAY -> "Sat"
                    DayOfWeek.SUNDAY -> "Sun"
                }
            }
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleRecurringSchedule(
                updated.id,
                updated.title,
                updated.description,
                updated.time.hour,
                updated.time.minute,
                daysString
            )
        } else {
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelSchedule(updated.id)
        }
    }
    fun deleteSchedule(id: String) {
        dataStore.deleteSchedule(id)
        space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.cancelSchedule(id)
    }
    fun savePromptResponse(promptText: String, category: String, response: String) {
        val prompt = SavedPrompt(
            id = UUID.randomUUID().toString(),
            promptText = promptText,
            category = category,
            response = response,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        dataStore.savePrompt(prompt)
    }
    fun updatePrompt(prompt: SavedPrompt) {
        dataStore.savePrompt(prompt.copy(updatedAt = LocalDateTime.now()))
    }
    fun deletePrompt(id: String) {
        dataStore.deletePrompt(id)
    }
}
