package space.zeroxv6.journex.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import space.zeroxv6.journex.data.AppDatabase
import space.zeroxv6.journex.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                val settingsRepository = SettingsRepository(database.settingsDao())
                val settings = settingsRepository.getSettingsOnce()
                if (settings.journalReminderEnabled && settings.notificationsEnabled) {
                    AlarmScheduler.scheduleJournalReminder(
                        context,
                        settings.journalReminderHour,
                        settings.journalReminderMinute
                    )
                }
                val reminders = database.reminderDao().getActiveReminders(System.currentTimeMillis())
                reminders.forEach { reminder ->
                    if (reminder.dateTime > System.currentTimeMillis()) {
                        AlarmScheduler.scheduleTaskReminder(
                            context,
                            reminder.id.hashCode(),
                            reminder.title,
                            reminder.description,
                            reminder.dateTime
                        )
                    }
                }
                val schedules = database.scheduleDao().getAllSchedulesOnce()
                schedules.filter { it.isEnabled }.forEach { schedule ->
                    AlarmScheduler.scheduleScheduleReminder(
                        context,
                        schedule.id,
                        schedule.title,
                        schedule.description,
                        schedule.timeHour,
                        schedule.timeMinute,
                        schedule.daysOfWeek
                    )
                }
            }
        }
    }
}
