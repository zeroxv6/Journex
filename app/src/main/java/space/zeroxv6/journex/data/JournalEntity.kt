package space.zeroxv6.journex.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@Entity(tableName = "journal_entries")
@TypeConverters(JournalConverters::class)
data class JournalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val mood: String,
    val tags: String, 
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val voiceNotes: String, 
    val weather: String?,
    val location: String?,
    val photos: String, 
    val wordCount: Int,
    val readingTime: Int,
    val color: String,
    val reminder: String?,
    val linkedEntries: String, 
    val characterCount: Int,
    val paragraphCount: Int,
    val sentenceCount: Int
)
class JournalConverters {
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    @TypeConverter
    fun toStringList(list: List<String>): String {
        return gson.toJson(list)
    }
    @TypeConverter
    fun fromLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value, formatter)
    }
    @TypeConverter
    fun toLocalDateTime(date: LocalDateTime): String {
        return date.format(formatter)
    }
}
