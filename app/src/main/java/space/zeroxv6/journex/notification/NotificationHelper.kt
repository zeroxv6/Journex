package space.zeroxv6.journex.notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import space.zeroxv6.journex.MainActivity
import space.zeroxv6.journex.R
object NotificationHelper {
    const val CHANNEL_ID_JOURNAL_REMINDER = "journal_reminder"
    const val CHANNEL_ID_TASK_REMINDER = "task_reminder"
    const val CHANNEL_ID_SCHEDULE = "schedule_reminder"
    const val CHANNEL_ID_GENERAL = "general"
    const val NOTIFICATION_ID_JOURNAL_REMINDER = 1001
    const val NOTIFICATION_ID_TASK_REMINDER = 2001
    const val NOTIFICATION_ID_SCHEDULE = 3001
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val journalChannel = NotificationChannel(
                CHANNEL_ID_JOURNAL_REMINDER,
                "Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to write in your journal"
            }
            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASK_REMINDER,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your tasks"
            }
            val scheduleChannel = NotificationChannel(
                CHANNEL_ID_SCHEDULE,
                "Schedule Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your scheduled events"
            }
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            notificationManager.createNotificationChannel(journalChannel)
            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(scheduleChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    fun showJournalReminderNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_JOURNAL_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to Journal")
            .setContentText("Take a moment to reflect on your day")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_JOURNAL_REMINDER, notification)
    }
    fun showTaskReminderNotification(context: Context, title: String, message: String, notificationId: Int = NOTIFICATION_ID_TASK_REMINDER) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
    fun showScheduleNotification(context: Context, title: String, message: String, notificationId: Int = NOTIFICATION_ID_SCHEDULE) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SCHEDULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
