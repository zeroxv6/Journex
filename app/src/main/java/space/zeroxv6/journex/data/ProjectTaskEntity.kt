package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(
    tableName = "project_tasks",
    indices = [
        Index(value = ["status"]),
        Index(value = ["priority"]),
        Index(value = ["projectId"]),
        Index(value = ["dueDate"])
    ]
)
data class ProjectTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val tags: String, 
    val assignee: String?,
    val dueDate: String?,
    val estimatedHours: Float?,
    val actualHours: Float?,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val projectId: String?,
    val subtasks: String, 
    val attachments: String, 
    val comments: String 
)
@Entity(tableName = "task_projects")
data class TaskProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val color: Long,
    val icon: String,
    val createdAt: String
)
