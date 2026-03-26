package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey val id: String,
    val promptText: String,
    val category: String,
    val response: String,
    val createdAt: Long,
    val updatedAt: Long
)
