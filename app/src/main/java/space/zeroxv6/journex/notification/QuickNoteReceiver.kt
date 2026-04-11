package space.zeroxv6.journex.notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.RemoteInput
import space.zeroxv6.journex.R
import space.zeroxv6.journex.data.AppDatabase
import space.zeroxv6.journex.data.QuickNoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
class QuickNoteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == QuickNoteNotificationService.ACTION_SAVE_QUICK_NOTE) {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            val noteText = remoteInput?.getCharSequence(QuickNoteNotificationService.KEY_TEXT_REPLY)?.toString()
            if (!noteText.isNullOrBlank()) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        val quickNote = QuickNoteEntity(
                            id = UUID.randomUUID().toString(),
                            content = noteText,
                            createdAt = System.currentTimeMillis()
                        )
                        database.quickNoteDao().insertQuickNote(quickNote)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Quick note saved!", Toast.LENGTH_SHORT).show()
                        }
                        showReadyNotification(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Tap 'Save Note' button and type your note", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun showReadyNotification(context: Context) {
        val remoteInput = RemoteInput.Builder(QuickNoteNotificationService.KEY_TEXT_REPLY)
            .setLabel("Type your next quick note...")
            .build()
        val saveIntent = Intent(context, QuickNoteReceiver::class.java).apply {
            action = QuickNoteNotificationService.ACTION_SAVE_QUICK_NOTE
        }
        val savePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            saveIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val saveAction = androidx.core.app.NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            "Save Note",
            savePendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
        val notification = androidx.core.app.NotificationCompat.Builder(context, QuickNoteNotificationService.CHANNEL_ID)
            .setSmallIcon(R.drawable.journex)
            .setContentTitle("Quick Note")
            .setContentText("Tap 'Save Note' button below to add a note")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(saveAction)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("Tap the 'Save Note' button below, type your note in the text field, and send"))
            .setColor(0xFFFFE8DC.toInt())
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(QuickNoteNotificationService.NOTIFICATION_ID, notification)
    }
}
