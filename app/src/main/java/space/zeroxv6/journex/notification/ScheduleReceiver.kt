package space.zeroxv6.journex.notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Schedule"
        val description = intent.getStringExtra("description") ?: ""
        val notificationId = intent.getIntExtra("notificationId", NotificationHelper.NOTIFICATION_ID_SCHEDULE)
        val message = if (description.isNotEmpty()) description else "It's time for: $title"
        NotificationHelper.showScheduleNotification(context, title, message, notificationId)
    }
}
