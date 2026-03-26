package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val timeHour: Int,
    val timeMinute: Int,
    val endTimeHour: Int? = null,
    val endTimeMinute: Int? = null,
    val daysOfWeek: String, 
    val isEnabled: Boolean
)
