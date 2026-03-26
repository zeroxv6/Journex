package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY timeHour, timeMinute")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>
    @Query("SELECT * FROM schedules ORDER BY timeHour, timeMinute")
    suspend fun getAllSchedulesOnce(): List<ScheduleEntity>
    @Query("SELECT * FROM schedules WHERE isEnabled = 1 ORDER BY timeHour, timeMinute LIMIT :limit")
    fun getEnabledSchedules(limit: Int): Flow<List<ScheduleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()
}
