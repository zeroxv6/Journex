package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.ReminderDao
import space.zeroxv6.journex.data.ReminderEntity
import kotlinx.coroutines.flow.Flow
class ReminderRepository(private val reminderDao: ReminderDao) {
    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    fun getUpcomingReminders(limit: Int): Flow<List<ReminderEntity>> = reminderDao.getUpcomingReminders(limit)
    suspend fun insertReminder(reminder: ReminderEntity) = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun deleteAllReminders() = reminderDao.deleteAllReminders()
}
