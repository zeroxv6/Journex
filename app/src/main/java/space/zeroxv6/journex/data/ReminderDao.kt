package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dateTime ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND dateTime >= :currentTime ORDER BY dateTime ASC")
    suspend fun getActiveReminders(currentTime: Long): List<ReminderEntity>
    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dateTime ASC LIMIT :limit")
    fun getUpcomingReminders(limit: Int): Flow<List<ReminderEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
