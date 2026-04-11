package space.zeroxv6.journex.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import space.zeroxv6.journex.MainActivity
import space.zeroxv6.journex.R

/**
 * NotificationHelper
 *
 * Core rule for sounds on Android O+:
 *   The CHANNEL controls the sound, not the builder's .setSound().
 *   So we maintain TWO reminder channels:
 *     - CHANNEL_TASK_NOTIF   → plays notification_sound.mp3  (banner mode)
 *     - CHANNEL_TASK_ALARM   → plays callincoming.mp3        (alarm mode)
 *     - CHANNEL_SCHED_NOTIF  → plays notification_sound.mp3  (banner mode)
 *     - CHANNEL_SCHED_ALARM  → plays callincoming.mp3        (alarm mode)
 *
 *   At trigger time we read the user preference and pick the right channel.
 *   The sound set on the builder acts as a fallback for pre-O devices only.
 */
object NotificationHelper {

    // ── Journal ────────────────────────────────────────────────────────────────
    const val CHANNEL_ID_JOURNAL = "journal_reminder_v4"

    // ── Task / Reminder ────────────────────────────────────────────────────────
    const val CHANNEL_TASK_NOTIF  = "task_notif_v5"   // notification_sound
    const val CHANNEL_TASK_ALARM  = "task_alarm_v5"   // SILENT (service handles it)

    // ── Schedule ───────────────────────────────────────────────────────────────
    const val CHANNEL_SCHED_NOTIF = "sched_notif_v5"  // notification_sound
    const val CHANNEL_SCHED_ALARM = "sched_alarm_v5"  // SILENT (service handles it)

    // ── General ────────────────────────────────────────────────────────────────
    const val CHANNEL_GENERAL = "general_v5"

    // ── Notification IDs ───────────────────────────────────────────────────────
    const val NOTIFICATION_ID_JOURNAL  = 1001
    const val NOTIFICATION_ID_TASK     = 2001
    const val NOTIFICATION_ID_SCHEDULE = 3001

    // ── Sound URIs ─────────────────────────────────────────────────────────────
    private fun notifSoundUri(context: Context): Uri =
        Uri.parse("android.resource://${context.packageName}/raw/notification_sound")

    // ── Audio attributes ───────────────────────────────────────────────────────
    private val notifAudioAttr: AudioAttributes
        get() = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

    // ─────────────────────────────────────────────────────────────────────────
    // Channel creation
    // ─────────────────────────────────────────────────────────────────────────
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Delete ALL previous channel IDs so Android recreates them
        listOf(
            "journal_reminder", "journal_reminder_v2", "journal_reminder_v3", "journal_reminder_v4",
            "task_reminder", "task_reminder_v2", "task_reminder_v3",
            "task_notif_v4", "task_alarm_v4",
            "schedule_reminder", "schedule_reminder_v2", "schedule_reminder_v3",
            "sched_notif_v4", "sched_alarm_v4",
            "general", "general_v2", "general_v3", "general_v4"
        ).forEach { nm.deleteNotificationChannel(it) }

        // Journal  — always uses notification_sound
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID_JOURNAL, "Journal Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily reminders to write in your journal"
                setSound(notifSoundUri(context), notifAudioAttr)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
        )

        // Task / Reminder — Notification mode (notification_sound)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_TASK_NOTIF, "Reminder Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Standard reminder notifications"
                setSound(notifSoundUri(context), notifAudioAttr)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
        )

        // Task / Reminder — Alarm mode (SILENT, handled by Foreground Service)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_TASK_ALARM, "Reminder Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Full-screen alarm reminders"
                setSound(null, null)
                enableVibration(false) // Service vibrates
            }
        )

        // Schedule — Notification mode (notification_sound)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_SCHED_NOTIF, "Schedule Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Standard schedule notifications"
                setSound(notifSoundUri(context), notifAudioAttr)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
        )

        // Schedule — Alarm mode (SILENT, handled by Foreground Service)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_SCHED_ALARM, "Schedule Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Full-screen schedule alarms"
                setSound(null, null)
                enableVibration(false) // Service vibrates
            }
        )

        // General
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "General app notifications"
                setSound(notifSoundUri(context), notifAudioAttr)
            }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun readUseAlarm(context: Context): Boolean {
        val settingsRepo = space.zeroxv6.journex.repository.SettingsRepository(
            space.zeroxv6.journex.data.AppDatabase.getDatabase(context).settingsDao()
        )
        return kotlinx.coroutines.runBlocking { settingsRepo.getSettingsOnce().useFullScreenAlarm }
    }

    private fun mainPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun alarmPendingIntent(context: Context, title: String, message: String, type: String, requestCode: Int): PendingIntent {
        val intent = AlarmActivity.buildIntent(context, title, message, type)
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Journal reminder  (always notification_sound, no alarm mode)
    // ─────────────────────────────────────────────────────────────────────────
    fun showJournalReminderNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_JOURNAL)
            .setSmallIcon(R.drawable.journex)
            .setContentTitle("Time to Journal")
            .setContentText("Take a moment to reflect on your day")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainPendingIntent(context, 0))
            .setAutoCancel(true)
            // pre-O fallback sound
            .setSound(notifSoundUri(context))
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setColor(0xFFC05A28.toInt())
            .build()
        nm.notify(NOTIFICATION_ID_JOURNAL, notification)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Task / Reminder  (picks channel based on user setting)
    // ─────────────────────────────────────────────────────────────────────────
    fun showTaskReminderNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_TASK
    ) {
        val nm       = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val useAlarm = readUseAlarm(context)

        if (useAlarm) {
            AlarmService.start(context, title, message, notificationId, "reminder")
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_NOTIF)
            .setSmallIcon(R.drawable.journex)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(mainPendingIntent(context, notificationId))
            .setAutoCancel(true)
            .setSound(notifSoundUri(context))
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setColor(0xFFC05A28.toInt())

        nm.notify(notificationId, builder.build())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Schedule  (picks channel based on user setting)
    // ─────────────────────────────────────────────────────────────────────────
    fun showScheduleNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_SCHEDULE
    ) {
        val nm       = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val useAlarm = readUseAlarm(context)

        if (useAlarm) {
            AlarmService.start(context, title, message, notificationId, "schedule")
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_SCHED_NOTIF)
            .setSmallIcon(R.drawable.journex)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(mainPendingIntent(context, notificationId))
            .setAutoCancel(true)
            .setSound(notifSoundUri(context))
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setColor(0xFFC05A28.toInt())

        nm.notify(notificationId, builder.build())
    }
}
