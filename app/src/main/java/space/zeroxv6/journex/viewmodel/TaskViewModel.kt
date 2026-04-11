package space.zeroxv6.journex.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import space.zeroxv6.journex.data.*
import space.zeroxv6.journex.model.Priority
import space.zeroxv6.journex.model.Reminder
import space.zeroxv6.journex.model.ReminderCategory
import space.zeroxv6.journex.model.RepeatType
import space.zeroxv6.journex.model.ScheduleItem
import space.zeroxv6.journex.model.TodoTask
import space.zeroxv6.journex.repository.*
import space.zeroxv6.journex.notification.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(application)
    private val todoRepository = TodoRepository(database.todoDao())
    private val scheduleRepository = ScheduleRepository(database.scheduleDao())
    private val reminderRepository = ReminderRepository(database.reminderDao())
    private val promptRepository = PromptRepository(database.promptDao())
    val allTodos: StateFlow<List<TodoTask>> = todoRepository.getAllTodos()
        .map { entities -> entities.map { it.toTodoTask() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incompleteTodos: StateFlow<List<TodoTask>> = todoRepository.getIncompleteTodos(10)
        .map { entities -> entities.map { it.toTodoTask() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSchedules: StateFlow<List<ScheduleItem>> = scheduleRepository.getAllSchedules()
        .map { entities -> entities.map { it.toScheduleItem() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val enabledSchedules: StateFlow<List<ScheduleItem>> = scheduleRepository.getEnabledSchedules(10)
        .map { entities -> 
            val today = java.time.DayOfWeek.from(java.time.LocalDate.now())
            entities.map { it.toScheduleItem() }.filter { it.daysOfWeek.contains(today) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeReminders: StateFlow<List<Reminder>> = reminderRepository.getAllReminders()
        .map { entities -> entities.map { it.toReminder() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcomingReminders: StateFlow<List<Reminder>> = reminderRepository.getUpcomingReminders(10)
        .map { entities -> entities.map { it.toReminder() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPrompts: StateFlow<List<SavedPrompt>> = promptRepository.getAllPrompts()
        .map { entities -> entities.map { it.toSavedPrompt() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun addTodo(title: String, description: String, priority: Priority, dueDate: LocalDate? = null) {
        viewModelScope.launch {
            val todo = TodoEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                isCompleted = false,
                priority = priority.name,
                dueDate = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
                createdAt = System.currentTimeMillis()
            )
            todoRepository.insertTodo(todo)
        }
    }
    fun updateTodo(todo: TodoTask) {
        viewModelScope.launch {
            todoRepository.updateTodo(todo.toEntity())
        }
    }
    fun toggleTodoCompletion(todo: TodoTask) {
        viewModelScope.launch {
            val updated = todo.copy(isCompleted = !todo.isCompleted)
            todoRepository.updateTodo(updated.toEntity())
        }
    }
    fun deleteTodo(todo: TodoTask) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todo.toEntity())
        }
    }
    fun addSchedule(title: String, description: String, time: LocalTime, endTime: LocalTime? = null, daysOfWeek: Set<DayOfWeek>) {
        viewModelScope.launch {
            val schedule = ScheduleEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                timeHour = time.hour,
                timeMinute = time.minute,
                endTimeHour = endTime?.hour,
                endTimeMinute = endTime?.minute,
                daysOfWeek = daysOfWeek.joinToString(",") { it.name },
                isEnabled = true
            )
            scheduleRepository.insertSchedule(schedule)
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
            AlarmScheduler.scheduleScheduleReminder(
                context,
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
        viewModelScope.launch {
            scheduleRepository.updateSchedule(schedule.toEntity())
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
                AlarmScheduler.scheduleScheduleReminder(
                    context,
                    schedule.id,
                    schedule.title,
                    schedule.description,
                    schedule.time.hour,
                    schedule.time.minute,
                    daysString
                )
            } else {
                AlarmScheduler.cancelScheduleReminder(context, schedule.id, schedule.daysOfWeek.joinToString(",") { it.name })
            }
        }
    }
    fun toggleScheduleEnabled(schedule: ScheduleItem) {
        viewModelScope.launch {
            val updated = schedule.copy(isEnabled = !schedule.isEnabled)
            scheduleRepository.updateSchedule(updated.toEntity())
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
                AlarmScheduler.scheduleScheduleReminder(
                    context,
                    updated.id,
                    updated.title,
                    updated.description,
                    updated.time.hour,
                    updated.time.minute,
                    daysString
                )
            } else {
                AlarmScheduler.cancelScheduleReminder(context, updated.id, updated.daysOfWeek.joinToString(",") { it.name })
            }
        }
    }
    fun deleteSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule.toEntity())
            AlarmScheduler.cancelScheduleReminder(context, schedule.id, schedule.daysOfWeek.joinToString(",") { it.name })
        }
    }
    fun addReminder(title: String, description: String, dateTime: LocalDateTime, category: ReminderCategory, repeatType: RepeatType = RepeatType.NONE) {
        viewModelScope.launch {
            val reminderId = UUID.randomUUID().toString()
            val reminder = ReminderEntity(
                id = reminderId,
                title = title,
                description = description,
                dateTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                category = category.name,
                isCompleted = false,
                repeatType = repeatType.name
            )
            reminderRepository.insertReminder(reminder)
            val timeInMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (timeInMillis > System.currentTimeMillis()) {
                space.zeroxv6.journex.notification.AlarmScheduler.scheduleTaskReminder(
                    context,
                    reminderId.hashCode(),
                    title,
                    description,
                    timeInMillis,
                    repeatType.name
                )
            }
        }
    }
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.updateReminder(reminder.toEntity())
            if (!reminder.isCompleted) {
                val timeInMillis = reminder.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (timeInMillis > System.currentTimeMillis()) {
                    space.zeroxv6.journex.notification.AlarmScheduler.scheduleTaskReminder(
                        context,
                        reminder.id.hashCode(),
                        reminder.title,
                        reminder.description,
                        timeInMillis,
                        reminder.repeatType.name
                    )
                }
            } else {
                space.zeroxv6.journex.notification.AlarmScheduler.cancelTaskReminder(
                    context,
                    reminder.id.hashCode()
                )
            }
        }
    }
    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            reminderRepository.updateReminder(updated.toEntity())
        }
    }
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(reminder.toEntity())
            space.zeroxv6.journex.notification.AlarmScheduler.cancelTaskReminder(
                context,
                reminder.id.hashCode()
            )
        }
    }
    fun addPrompt(promptText: String, category: String, response: String = "") {
        viewModelScope.launch {
            val prompt = space.zeroxv6.journex.data.PromptEntity(
                id = UUID.randomUUID().toString(),
                promptText = promptText,
                category = category,
                response = response,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            promptRepository.insertPrompt(prompt)
        }
    }
    fun updatePrompt(prompt: SavedPrompt) {
        viewModelScope.launch {
            promptRepository.updatePrompt(prompt.toEntity())
        }
    }
    fun deletePrompt(prompt: SavedPrompt) {
        viewModelScope.launch {
            promptRepository.deletePrompt(prompt.toEntity())
        }
    }
}
data class SavedPrompt(
    val id: String,
    val promptText: String,
    val category: String,
    val response: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
private fun TodoEntity.toTodoTask() = TodoTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.valueOf(priority),
    dueDate = dueDate?.let { LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000)) },
    createdAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(createdAt), ZoneId.systemDefault())
)
private fun TodoTask.toEntity() = TodoEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority.name,
    dueDate = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
    createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
private fun ScheduleEntity.toScheduleItem() = ScheduleItem(
    id = id,
    title = title,
    description = description,
    time = LocalTime.of(timeHour, timeMinute),
    endTime = if (endTimeHour != null && endTimeMinute != null) LocalTime.of(endTimeHour, endTimeMinute) else null,
    daysOfWeek = daysOfWeek.split(",").map { DayOfWeek.valueOf(it) }.toSet(),
    isEnabled = isEnabled
)
private fun ScheduleItem.toEntity() = ScheduleEntity(
    id = id,
    title = title,
    description = description,
    timeHour = time.hour,
    timeMinute = time.minute,
    endTimeHour = endTime?.hour,
    endTimeMinute = endTime?.minute,
    daysOfWeek = daysOfWeek.joinToString(",") { it.name },
    isEnabled = isEnabled
)
private fun ReminderEntity.toReminder() = Reminder(
    id = id,
    title = title,
    description = description,
    dateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(dateTime), ZoneId.systemDefault()),
    category = ReminderCategory.valueOf(category),
    isCompleted = isCompleted,
    repeatType = RepeatType.valueOf(repeatType)
)
private fun Reminder.toEntity() = ReminderEntity(
    id = id,
    title = title,
    description = description,
    dateTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    category = category.name,
    isCompleted = isCompleted,
    repeatType = repeatType.name
)
private fun space.zeroxv6.journex.data.PromptEntity.toSavedPrompt() = SavedPrompt(
    id = id,
    promptText = promptText,
    category = category,
    response = response,
    createdAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
    updatedAt = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(updatedAt), ZoneId.systemDefault())
)
private fun SavedPrompt.toEntity() = space.zeroxv6.journex.data.PromptEntity(
    id = id,
    promptText = promptText,
    category = category,
    response = response,
    createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
