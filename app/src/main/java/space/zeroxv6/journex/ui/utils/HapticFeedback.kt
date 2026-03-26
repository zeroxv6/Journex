package space.zeroxv6.journex.ui.utils
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
object HapticFeedback {
    /**
     * Provides subtle, professional haptic feedback
     * @param context Android context
     * @param type Type of feedback: LIGHT, MEDIUM, STRONG
     */
    fun perform(context: Context, type: FeedbackType = FeedbackType.LIGHT) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        val (duration, amplitude) = when (type) {
            FeedbackType.LIGHT -> Pair(10L, 30)    
            FeedbackType.MEDIUM -> Pair(15L, 50)   
            FeedbackType.STRONG -> Pair(25L, 70)   
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    enum class FeedbackType {
        LIGHT,    
        MEDIUM,   
        STRONG    
    }
}
