package space.zeroxv6.journex.model
import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
data class TodoTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
enum class Priority(val label: String, val color: Color) {
    HIGH("High", Color(0xFF000000)),
    MEDIUM("Medium", Color(0xFF616161)),
    LOW("Low", Color(0xFF9E9E9E))
}
data class Reminder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dateTime: LocalDateTime,
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val isCompleted: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE
)
enum class ReminderCategory(val label: String, val color: Color) {
    MEETING("Meeting", Color(0xFF000000)),
    TASK("Task", Color(0xFF424242)),
    APPOINTMENT("Appointment", Color(0xFF616161)),
    CALL("Call", Color(0xFF757575)),
    GENERAL("General", Color(0xFF9E9E9E))
}
enum class RepeatType(val label: String) {
    NONE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}
data class ScheduleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val time: LocalTime,
    val endTime: LocalTime? = null,
    val daysOfWeek: Set<DayOfWeek>,
    val isEnabled: Boolean = true
)
