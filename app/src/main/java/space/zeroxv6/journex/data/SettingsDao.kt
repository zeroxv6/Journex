package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)
    @Update
    suspend fun updateSettings(settings: SettingsEntity)
    @Query("UPDATE settings SET pinCode = :pinCode WHERE id = 1")
    suspend fun updatePinCode(pinCode: String)
    @Query("UPDATE settings SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    @Query("UPDATE settings SET journalReminderEnabled = :enabled WHERE id = 1")
    suspend fun updateJournalReminderEnabled(enabled: Boolean)
    @Query("UPDATE settings SET journalReminderHour = :hour, journalReminderMinute = :minute WHERE id = 1")
    suspend fun updateJournalReminderTime(hour: Int, minute: Int)
    @Query("UPDATE settings SET quickNoteNotificationEnabled = :enabled WHERE id = 1")
    suspend fun updateQuickNoteNotificationEnabled(enabled: Boolean)
    
    @Query("UPDATE settings SET use24HourFormat = :enabled WHERE id = 1")
    suspend fun updateUse24HourFormat(enabled: Boolean)
    
    @Query("UPDATE settings SET persistentScheduleNotificationEnabled = :enabled WHERE id = 1")
    suspend fun updatePersistentScheduleNotificationEnabled(enabled: Boolean)
    
    @Query("UPDATE settings SET useFullScreenAlarm = :enabled WHERE id = 1")
    suspend fun updateUseFullScreenAlarm(enabled: Boolean)
}
