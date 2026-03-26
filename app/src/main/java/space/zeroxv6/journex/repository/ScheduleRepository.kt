package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.ScheduleDao
import space.zeroxv6.journex.data.ScheduleEntity
import kotlinx.coroutines.flow.Flow
class ScheduleRepository(private val scheduleDao: ScheduleDao) {
    fun getAllSchedules(): Flow<List<ScheduleEntity>> = scheduleDao.getAllSchedules()
    fun getEnabledSchedules(limit: Int): Flow<List<ScheduleEntity>> = scheduleDao.getEnabledSchedules(limit)
    suspend fun insertSchedule(schedule: ScheduleEntity) = scheduleDao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = scheduleDao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = scheduleDao.deleteSchedule(schedule)
    suspend fun deleteAllSchedules() = scheduleDao.deleteAllSchedules()
}
