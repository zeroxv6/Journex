package space.zeroxv6.journex.notification
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import space.zeroxv6.journex.MainActivity
import space.zeroxv6.journex.R
class QuickNoteNotificationService : Service() {
    companion object {
        const val CHANNEL_ID = "quick_note_persistent"
        const val NOTIFICATION_ID = 9999
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ACTION_SAVE_QUICK_NOTE = "space.zeroxv6.journex.SAVE_QUICK_NOTE"
        fun start(context: Context) {
            val intent = Intent(context, QuickNoteNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        fun stop(context: Context) {
            val intent = Intent(context, QuickNoteNotificationService::class.java)
            context.stopService(intent)
        }
    }
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quick Notes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for quick note input"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createNotification(): Notification {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Type your quick note...")
            .build()
        val saveIntent = Intent(this, QuickNoteReceiver::class.java).apply {
            action = ACTION_SAVE_QUICK_NOTE
        }
        val savePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            saveIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val saveAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            "Save Note",
            savePendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.journex)
            .setContentTitle("Quick Note")
            .setContentText("Tap 'Save Note' button below to add a note")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(saveAction)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Tap the 'Save Note' button below, type your note in the text field, and send"))
            .setColor(0xFFFFE8DC.toInt())
            .build()
    }
}
