package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long
)
