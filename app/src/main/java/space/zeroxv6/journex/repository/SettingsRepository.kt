package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.SettingsDao
import space.zeroxv6.journex.data.SettingsEntity
import kotlinx.coroutines.flow.Flow
class SettingsRepository(private val settingsDao: SettingsDao) {
    val settings: Flow<SettingsEntity?> = settingsDao.getSettings()
    suspend fun getSettingsOnce(): SettingsEntity {
        return settingsDao.getSettingsOnce() ?: SettingsEntity().also {
            settingsDao.insertSettings(it)
        }
    }
    suspend fun updatePinCode(pinCode: String) {
        ensureSettingsExist()
        settingsDao.updatePinCode(pinCode)
    }
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.updateNotificationsEnabled(enabled)
    }
    suspend fun updateJournalReminderEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.updateJournalReminderEnabled(enabled)
    }
    suspend fun updateJournalReminderTime(hour: Int, minute: Int) {
        ensureSettingsExist()
        settingsDao.updateJournalReminderTime(hour, minute)
    }
    suspend fun updateQuickNoteNotificationEnabled(enabled: Boolean) {
        ensureSettingsExist()
        settingsDao.updateQuickNoteNotificationEnabled(enabled)
    }
    private suspend fun ensureSettingsExist() {
        if (settingsDao.getSettingsOnce() == null) {
            settingsDao.insertSettings(SettingsEntity())
        }
    }
}
