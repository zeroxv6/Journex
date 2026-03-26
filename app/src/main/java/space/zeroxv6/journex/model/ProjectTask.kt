package space.zeroxv6.journex.model
import java.time.LocalDateTime
import java.util.UUID
data class ProjectTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val tags: List<String> = emptyList(),
    val assignee: String? = null,
    val dueDate: LocalDateTime? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val projectId: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val attachments: List<String> = emptyList(),
    val comments: List<TaskComment> = emptyList()
)
enum class TaskStatus(val label: String, val color: Long) {
    NOT_STARTED("Not Started", 0xFF9E9E9E),
    IN_PROGRESS("In Progress", 0xFF2196F3),
    DONE("Done", 0xFF4CAF50)
}
enum class TaskPriority(val label: String, val color: Long) {
    LOW("Low", 0xFF9E9E9E),
    MEDIUM("Medium", 0xFFFF9800),
    HIGH("High", 0xFFF44336),
    URGENT("Urgent", 0xFFD32F2F)
}
data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)
data class TaskComment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val author: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
data class TaskProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val color: Long = 0xFF000000,
    val icon: String = "📁",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
