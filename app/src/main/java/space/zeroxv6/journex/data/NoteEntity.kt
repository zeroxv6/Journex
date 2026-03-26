package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["folderId"]),
        Index(value = ["category"]),
        Index(value = ["isPinned", "updatedAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isArchived"])
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val plainTextContent: String,
    val category: String,
    val tags: String, 
    val color: String,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isFavorite: Boolean,
    val isLocked: Boolean,
    val lockPassword: String?,
    val attachments: String, 
    val checklistItems: String, 
    val tables: String, 
    val codeBlocks: String, 
    val drawings: String, 
    val voiceNotes: String, 
    val links: String, 
    val reminder: Long?,
    val recurringReminder: String?, 
    val createdAt: String,
    val updatedAt: String,
    val lastAccessedAt: String,
    val collaborators: String, 
    val folderId: String?,
    val parentNoteId: String?,
    val templateId: String?,
    val wordCount: Int,
    val characterCount: Int,
    val readingTime: Int,
    val version: Int,
    val versionHistory: String, 
    val formatting: String, 
    val layout: String,
    val priority: String,
    val status: String,
    val location: String?, 
    val weather: String?,
    val mood: String?,
    val customFields: String, 
    val aiSummary: String?,
    val aiKeywords: String, 
    val relatedNotes: String, 
    val exportFormats: String 
)
@Entity(
    tableName = "note_folders",
    indices = [Index(value = ["parentId"])]
)
data class NoteFolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val parentId: String?,
    val createdAt: String,
    val isEncrypted: Boolean,
    val sortOrder: Int,
    val viewMode: String
)
@Entity(tableName = "note_templates")
data class NoteTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val content: String,
    val category: String,
    val icon: String,
    val fields: String 
)
