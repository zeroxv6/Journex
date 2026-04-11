package space.zeroxv6.journex.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val notificationId = intent.getIntExtra("notificationId", NotificationHelper.NOTIFICATION_ID_TASK)
        NotificationHelper.showTaskReminderNotification(context, title, message, notificationId)
    }
}
