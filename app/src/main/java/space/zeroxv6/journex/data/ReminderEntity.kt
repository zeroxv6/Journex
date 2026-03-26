package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTime: Long,
    val category: String,
    val isCompleted: Boolean,
    val repeatType: String
)
