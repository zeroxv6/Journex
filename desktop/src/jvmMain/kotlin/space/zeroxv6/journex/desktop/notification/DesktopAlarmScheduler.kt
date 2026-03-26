package space.zeroxv6.journex.desktop.notification
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
object DesktopAlarmScheduler {
    private val scheduledJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    /**
     * Schedule a journal reminder that repeats daily
     */
    fun scheduleJournalReminder(hour: Int, minute: Int) {
        cancelJournalReminder()
        val job = scope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                val nextTrigger = if (now.isAfter(targetTime)) {
                    targetTime.plusDays(1)
                } else {
                    targetTime
                }
                val delayMillis = ChronoUnit.MILLIS.between(now, nextTrigger)
                if (delayMillis > 0) {
                    delay(delayMillis)
                    if (isActive) {
                        DesktopNotificationManager.showJournalReminderNotification()
                    }
                }
            }
        }
        scheduledJobs["journal_reminder"] = job
    }
    fun cancelJournalReminder() {
        scheduledJobs.remove("journal_reminder")?.cancel()
    }
    /**
     * Schedule a one-time reminder
     */
    fun scheduleReminder(reminderId: String, title: String, description: String, dateTimeMillis: Long) {
        cancelReminder(reminderId)
        val now = System.currentTimeMillis()
        val delayMillis = dateTimeMillis - now
        if (delayMillis <= 0) {
            return 
        }
        val job = scope.launch {
            delay(delayMillis)
            if (isActive) {
                DesktopNotificationManager.showReminderNotification(title, description)
            }
        }
        scheduledJobs["reminder_$reminderId"] = job
    }
    fun cancelReminder(reminderId: String) {
        scheduledJobs.remove("reminder_$reminderId")?.cancel()
    }
    /**
     * Schedule a recurring schedule (by days of week)
     */
    fun scheduleRecurringSchedule(
        scheduleId: String,
        title: String,
        description: String,
        hour: Int,
        minute: Int,
        daysOfWeek: String
    ) {
        cancelSchedule(scheduleId)
        if (daysOfWeek.isEmpty()) {
            scheduleOneTimeSchedule(scheduleId, title, description, hour, minute)
            return
        }
        val days = daysOfWeek.split(",").map { it.trim() }
        val dayMap = mapOf(
            "Mon" to java.time.DayOfWeek.MONDAY,
            "Tue" to java.time.DayOfWeek.TUESDAY,
            "Wed" to java.time.DayOfWeek.WEDNESDAY,
            "Thu" to java.time.DayOfWeek.THURSDAY,
            "Fri" to java.time.DayOfWeek.FRIDAY,
            "Sat" to java.time.DayOfWeek.SATURDAY,
            "Sun" to java.time.DayOfWeek.SUNDAY
        )
        val job = scope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val currentDayOfWeek = now.dayOfWeek
                var nextTrigger: LocalDateTime? = null
                for (i in 0..6) {
                    val checkDay = now.plusDays(i.toLong())
                    val checkDayName = when (checkDay.dayOfWeek) {
                        java.time.DayOfWeek.MONDAY -> "Mon"
                        java.time.DayOfWeek.TUESDAY -> "Tue"
                        java.time.DayOfWeek.WEDNESDAY -> "Wed"
                        java.time.DayOfWeek.THURSDAY -> "Thu"
                        java.time.DayOfWeek.FRIDAY -> "Fri"
                        java.time.DayOfWeek.SATURDAY -> "Sat"
                        java.time.DayOfWeek.SUNDAY -> "Sun"
                    }
                    if (checkDayName in days) {
                        val candidate = checkDay.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                        if (candidate.isAfter(now)) {
                            nextTrigger = candidate
                            break
                        }
                    }
                }
                if (nextTrigger != null) {
                    val delayMillis = ChronoUnit.MILLIS.between(now, nextTrigger)
                    if (delayMillis > 0) {
                        delay(delayMillis)
                        if (isActive) {
                            DesktopNotificationManager.showScheduleNotification(title, description)
                        }
                    }
                } else {
                    delay(60000) 
                }
            }
        }
        scheduledJobs["schedule_$scheduleId"] = job
    }
    private fun scheduleOneTimeSchedule(
        scheduleId: String,
        title: String,
        description: String,
        hour: Int,
        minute: Int
    ) {
        val job = scope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val targetTime = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                val nextTrigger = if (now.isAfter(targetTime)) {
                    targetTime.plusDays(1)
                } else {
                    targetTime
                }
                val delayMillis = ChronoUnit.MILLIS.between(now, nextTrigger)
                if (delayMillis > 0) {
                    delay(delayMillis)
                    if (isActive) {
                        DesktopNotificationManager.showScheduleNotification(title, description)
                    }
                }
            }
        }
        scheduledJobs["schedule_$scheduleId"] = job
    }
    fun cancelSchedule(scheduleId: String) {
        scheduledJobs.remove("schedule_$scheduleId")?.cancel()
    }
    fun cancelAll() {
        scheduledJobs.values.forEach { it.cancel() }
        scheduledJobs.clear()
    }
    fun cleanup() {
        cancelAll()
        scope.cancel()
    }
}
