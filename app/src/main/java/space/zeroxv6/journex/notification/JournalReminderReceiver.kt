package space.zeroxv6.journex.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class JournalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            NotificationHelper.showJournalReminderNotification(context)
            val prefs = context.getSharedPreferences("journal_prefs", Context.MODE_PRIVATE)
            val hour = prefs.getInt("reminder_hour", 20)
            val minute = prefs.getInt("reminder_minute", 0)
            val enabled = prefs.getBoolean("reminder_enabled", false)
            if (enabled) {
                AlarmScheduler.scheduleJournalReminder(context, hour, minute)
            }
        }
    }
}
