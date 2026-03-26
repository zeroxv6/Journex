package space.zeroxv6.journex.model
import java.time.LocalDateTime
import java.util.UUID
data class Note(
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
    val tables: List<NoteTable> = emptyList(),
    val codeBlocks: List<CodeBlock> = emptyList(),
    val drawings: List<Drawing> = emptyList(),
    val voiceNotes: List<VoiceNote> = emptyList(),
    val links: List<NoteLink> = emptyList(),
    val reminder: Long? = null,
    val recurringReminder: RecurringReminder? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastAccessedAt: LocalDateTime = LocalDateTime.now(),
    val collaborators: List<String> = emptyList(),
    val folderId: String? = null,
    val parentNoteId: String? = null, 
    val templateId: String? = null,
    val wordCount: Int = 0,
    val characterCount: Int = 0,
    val readingTime: Int = 0,
    val version: Int = 1,
    val versionHistory: List<NoteVersion> = emptyList(),
    val formatting: TextFormatting = TextFormatting(),
    val layout: NoteLayout = NoteLayout.STANDARD,
    val priority: NotePriority = NotePriority.NONE,
    val status: NoteStatus = NoteStatus.ACTIVE,
    val location: NoteLocation? = null,
    val weather: String? = null,
    val mood: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val aiSummary: String? = null,
    val aiKeywords: List<String> = emptyList(),
    val relatedNotes: List<String> = emptyList(),
    val exportFormats: List<ExportFormat> = emptyList()
)
data class NoteAttachment(
    val id: String = UUID.randomUUID().toString(),
    val type: AttachmentType,
    val uri: String,
    val name: String,
    val size: Long = 0,
    val thumbnailUri: String? = null,
    val mimeType: String? = null,
    val duration: Long? = null, 
    val dimensions: Pair<Int, Int>? = null, 
    val uploadProgress: Float = 1f,
    val cloudUrl: String? = null
)
enum class AttachmentType {
    IMAGE, AUDIO, VIDEO, DOCUMENT, LINK, PDF, SPREADSHEET, PRESENTATION, ARCHIVE
}
data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false,
    val order: Int = 0,
    val subItems: List<ChecklistItem> = emptyList(),
    val dueDate: Long? = null,
    val assignedTo: String? = null,
    val priority: NotePriority = NotePriority.NONE
)
data class NoteTable(
    val id: String = UUID.randomUUID().toString(),
    val rows: Int = 3,
    val columns: Int = 3,
    val data: List<List<String>> = emptyList(),
    val headers: List<String> = emptyList(),
    val style: TableStyle = TableStyle.BASIC
)
enum class TableStyle {
    BASIC, STRIPED, BORDERED, COMPACT
}
data class CodeBlock(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val language: String = "kotlin",
    val theme: CodeTheme = CodeTheme.DARK,
    val showLineNumbers: Boolean = true
)
enum class CodeTheme {
    LIGHT, DARK, MONOKAI, DRACULA
}
data class Drawing(
    val id: String = UUID.randomUUID().toString(),
    val imageData: String, 
    val strokes: List<DrawingStroke> = emptyList(),
    val backgroundColor: String = "#FFFFFF"
)
data class DrawingStroke(
    val points: List<Pair<Float, Float>>,
    val color: String,
    val width: Float,
    val tool: DrawingTool
)
enum class DrawingTool {
    PEN, HIGHLIGHTER, ERASER, SHAPE
}
data class VoiceNote(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val duration: Long,
    val transcription: String? = null,
    val waveformData: List<Float> = emptyList()
)
data class NoteLink(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val favicon: String? = null
)
data class RecurringReminder(
    val frequency: ReminderFrequency,
    val interval: Int = 1,
    val daysOfWeek: List<Int> = emptyList(),
    val endDate: Long? = null
)
enum class ReminderFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
}
data class NoteVersion(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: LocalDateTime,
    val changeDescription: String = ""
)
data class TextFormatting(
    val fontSize: Int = 16,
    val fontFamily: String = "default",
    val lineHeight: Float = 1.5f,
    val alignment: TextAlignment = TextAlignment.LEFT,
    val bulletStyle: BulletStyle = BulletStyle.DISC
)
enum class TextAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}
enum class BulletStyle {
    DISC, CIRCLE, SQUARE, DECIMAL, NONE
}
enum class NoteLayout {
    STANDARD, KANBAN, TIMELINE, MIND_MAP, CORNELL, OUTLINE
}
enum class NotePriority(val label: String, val color: String) {
    NONE("None", "#9E9E9E"),
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HIGH("High", "#F44336"),
    URGENT("Urgent", "#D32F2F")
}
enum class NoteStatus {
    ACTIVE, DRAFT, COMPLETED, ARCHIVED, DELETED
}
data class NoteLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val placeName: String? = null
)
enum class ExportFormat {
    PDF, MARKDOWN, HTML, DOCX, TXT, JSON
}
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
data class NoteFolder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String = "#FFFFFF",
    val icon: String = "📁",
    val parentId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isEncrypted: Boolean = false,
    val sortOrder: Int = 0,
    val viewMode: NoteViewMode = NoteViewMode.GRID
)
data class NoteTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val content: String,
    val category: NoteCategory,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val fields: List<TemplateField> = emptyList()
)
data class TemplateField(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val type: FieldType,
    val required: Boolean = false,
    val defaultValue: String = ""
)
enum class FieldType {
    TEXT, NUMBER, DATE, CHECKBOX, DROPDOWN, MULTILINE
}
enum class NoteSortOption(val label: String) {
    DATE_CREATED_DESC("Newest First"),
    DATE_CREATED_ASC("Oldest First"),
    DATE_UPDATED("Recently Updated"),
    DATE_ACCESSED("Recently Viewed"),
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    CATEGORY("By Category"),
    COLOR("By Color"),
    PRIORITY("By Priority"),
    SIZE("By Size"),
    CUSTOM("Custom Order")
}
enum class NoteViewMode {
    LIST, GRID, COMPACT, MASONRY, TIMELINE, KANBAN
}
data class NoteFilter(
    val categories: List<NoteCategory> = emptyList(),
    val tags: List<String> = emptyList(),
    val priorities: List<NotePriority> = emptyList(),
    val dateRange: Pair<LocalDateTime, LocalDateTime>? = null,
    val hasAttachments: Boolean? = null,
    val hasReminders: Boolean? = null,
    val isLocked: Boolean? = null,
    val searchQuery: String = ""
)
