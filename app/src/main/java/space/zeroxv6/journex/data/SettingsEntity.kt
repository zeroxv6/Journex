package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1, 
    val pinCode: String = "",
    val notificationsEnabled: Boolean = true,
    val journalReminderEnabled: Boolean = false,
    val journalReminderHour: Int = 20,
    val journalReminderMinute: Int = 0,
    val quickNoteNotificationEnabled: Boolean = false,
    val use24HourFormat: Boolean = false,
    val persistentScheduleNotificationEnabled: Boolean = false,
    val useFullScreenAlarm: Boolean = true
)
