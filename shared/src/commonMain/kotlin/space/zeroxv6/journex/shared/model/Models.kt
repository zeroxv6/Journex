package space.zeroxv6.journex.shared.model
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val weather: String? = null,
    val location: String? = null,
    val photos: List<String> = emptyList(),
    val wordCount: Int = 0,
    val readingTime: Int = 0,
    val color: EntryColor = EntryColor.DEFAULT,
    val characterCount: Int = 0,
    val paragraphCount: Int = 0,
    val sentenceCount: Int = 0
)
enum class EntryColor(val backgroundColor: String, val label: String) {
    DEFAULT("#FAFAFA", "Default"),
    LIGHT_GRAY("#F5F5F5", "Light Gray"),
    WARM_WHITE("#FFF9F0", "Warm White"),
    COOL_WHITE("#F0F4F8", "Cool White"),
    SOFT_BEIGE("#FAF7F2", "Soft Beige")
}
enum class Mood(val label: String) {
    AMAZING("Amazing"),
    HAPPY("Happy"),
    GOOD("Good"),
    NEUTRAL("Neutral"),
    SAD("Sad"),
    ANXIOUS("Anxious"),
    ANGRY("Angry"),
    TIRED("Tired"),
    EXCITED("Excited"),
    GRATEFUL("Grateful")
}
data class JournalStats(
    val totalEntries: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWords: Int = 0,
    val averageWordsPerEntry: Int = 0,
    val mostUsedMood: Mood? = null,
    val mostUsedTags: List<String> = emptyList(),
    val entriesThisMonth: Int = 0,
    val entriesThisYear: Int = 0,
    val completedTasks: Int = 0
)
data class TodoTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
enum class Priority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}
data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dateTime: LocalDateTime,
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val isCompleted: Boolean = false,
    val repeatType: RepeatType = RepeatType.NONE
)
enum class ReminderCategory(val label: String) {
    MEETING("Meeting"),
    TASK("Task"),
    APPOINTMENT("Appointment"),
    CALL("Call"),
    GENERAL("General")
}
enum class RepeatType(val label: String) {
    NONE("Once"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}
data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val time: LocalTime,
    val endTime: LocalTime? = null,
    val daysOfWeek: Set<DayOfWeek>,
    val isEnabled: Boolean = true
)
data class Prompt(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val category: String
)
data class SavedPrompt(
    val id: String = UUID.randomUUID().toString(),
    val promptText: String,
    val category: String,
    val response: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
data class AppSettings(
    val pinCode: String = "",
    val notificationsEnabled: Boolean = true,
    val journalReminderEnabled: Boolean = false,
    val journalReminderHour: Int = 20,
    val journalReminderMinute: Int = 0,
    val theme: String = "WARM_CREAM",
    val use24HourFormat: Boolean = true
)
data class QuickNote(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
enum class SortOption(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    UPDATED("Recently Updated"),
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    MOOD("By Mood"),
    WORD_COUNT("By Word Count")
}
data class CustomTemplate(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val content: String,
    val category: String = "Custom",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
data class ProjectTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val tags: List<String> = emptyList(),
    val assignee: String? = null,
    val dueDate: LocalDateTime? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val projectId: String? = null,
    val subtasks: List<Subtask> = emptyList(),
    val attachments: List<String> = emptyList(),
    val comments: List<TaskComment> = emptyList()
)
enum class TaskStatus(val label: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    DONE("Done")
}
enum class TaskPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}
data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)
data class TaskComment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val author: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
data class TaskProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val color: String = "#000000",
    val icon: String = "📁",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
data class FullNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val plainTextContent: String = "",
    val category: NoteCategory = NoteCategory.PERSONAL,
    val tags: List<String> = emptyList(),
    val color: String = "#FFFFFF",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val lockPassword: String? = null,
    val attachments: List<NoteAttachment> = emptyList(),
    val checklistItems: List<ChecklistItem> = emptyList(),
    val drawings: List<NoteDrawing> = emptyList(),
    val voiceNotes: List<NoteVoiceNote> = emptyList(),
    val links: List<NoteLink> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastAccessedAt: LocalDateTime = LocalDateTime.now(),
    val folderId: String? = null,
    val parentNoteId: String? = null,
    val templateId: String? = null,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingTime: Int = 0,
    val version: Int = 1,
    val versionHistory: List<NoteVersion> = emptyList(),
    val layout: NoteLayout = NoteLayout.STANDARD,
    val priority: NotePriority = NotePriority.NONE,
    val status: NoteStatus = NoteStatus.ACTIVE,
    val mood: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val relatedNotes: List<String> = emptyList()
)
data class NoteAttachment(
    val id: String = UUID.randomUUID().toString(),
    val type: AttachmentType = AttachmentType.IMAGE,
    val uri: String = "",
    val name: String = "",
    val size: Long = 0,
    val thumbnailUri: String? = null,
    val mimeType: String? = null,
    val duration: Long? = null
)
enum class AttachmentType {
    IMAGE, AUDIO, VIDEO, DOCUMENT, LINK, PDF
}
data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false,
    val order: Int = 0
)
data class NoteDrawing(
    val id: String = UUID.randomUUID().toString(),
    val imageData: String = "",
    val backgroundColor: String = "#FFFFFF"
)
data class NoteVoiceNote(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String = "",
    val duration: Long = 0,
    val transcription: String? = null,
    val waveformData: List<Float> = emptyList()
)
data class NoteLink(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null
)
data class NoteVersion(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val changeDescription: String = ""
)
enum class NoteCategory(val label: String, val color: String) {
    PERSONAL("Personal", "#000000"),
    WORK("Work", "#1A1A1A"),
    STUDY("Study", "#2D2D2D"),
    IDEAS("Ideas", "#404040"),
    TRAVEL("Travel", "#525252"),
    HEALTH("Health", "#666666"),
    FINANCE("Finance", "#7A7A7A"),
    SHOPPING("Shopping", "#8C8C8C"),
    RECIPES("Recipes", "#9E9E9E"),
    PROJECTS("Projects", "#B0B0B0"),
    MEETINGS("Meetings", "#C2C2C2"),
    RESEARCH("Research", "#D4D4D4"),
    CREATIVE("Creative", "#E6E6E6"),
    TECH("Tech", "#F0F0F0"),
    OTHER("Other", "#FAFAFA")
}
enum class NoteStatus(val label: String) {
    ACTIVE("Active"),
    DRAFT("Draft"),
    COMPLETED("Completed"),
    ARCHIVED("Archived")
}
enum class NoteLayout {
    STANDARD, KANBAN, TIMELINE, OUTLINE
}
enum class NotePriority(val label: String, val color: String) {
    NONE("None", "#9E9E9E"),
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HIGH("High", "#F44336"),
    URGENT("Urgent", "#D32F2F")
}
data class NoteFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = "#FFFFFF",
    val icon: String = "📁",
    val parentId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val sortOrder: Int = 0,
    val viewMode: NoteViewMode = NoteViewMode.GRID
)
enum class NoteSortOption(val label: String) {
    DATE_CREATED_DESC("Newest First"),
    DATE_CREATED_ASC("Oldest First"),
    DATE_UPDATED("Recently Updated"),
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    CATEGORY("By Category"),
    PRIORITY("By Priority")
}
enum class NoteViewMode {
    LIST, GRID, KANBAN
}
data class NoteFilter(
    val categories: List<NoteCategory> = emptyList(),
    val tags: List<String> = emptyList(),
    val priorities: List<NotePriority> = emptyList(),
    val dateRange: Pair<LocalDateTime, LocalDateTime>? = null,
    val hasAttachments: Boolean? = null,
    val searchQuery: String = ""
)
