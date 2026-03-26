package space.zeroxv6.journex.repository
import com.google.gson.Gson
import space.zeroxv6.journex.data.JournalDao
import space.zeroxv6.journex.data.JournalEntity
import space.zeroxv6.journex.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
class JournalRepository(private val journalDao: JournalDao) {
    private val gson = Gson()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries().map { entities ->
        entities.map { it.toJournalEntry() }
    }
    suspend fun getEntryById(id: String): JournalEntry? {
        return journalDao.getEntryById(id)?.toJournalEntry()
    }
    suspend fun insertEntry(entry: JournalEntry) {
        journalDao.insertEntry(entry.toEntity())
    }
    suspend fun updateEntry(entry: JournalEntry) {
        journalDao.updateEntry(entry.toEntity())
    }
    suspend fun deleteEntry(entry: JournalEntry) {
        journalDao.deleteEntryById(entry.id)
    }
    suspend fun deleteAllEntries() {
        journalDao.deleteAllEntries()
    }
    fun getFavoriteEntries(): Flow<List<JournalEntry>> {
        return journalDao.getFavoriteEntries().map { entities ->
            entities.map { it.toJournalEntry() }
        }
    }
    private fun JournalEntry.toEntity(): JournalEntity {
        return JournalEntity(
            id = id,
            title = title,
            content = content,
            mood = mood.name,
            tags = gson.toJson(tags),
            isFavorite = isFavorite,
            isArchived = isArchived,
            isPinned = isPinned,
            createdAt = createdAt.format(formatter),
            updatedAt = updatedAt.format(formatter),
            voiceNotes = gson.toJson(voiceNotes.map { 
                mapOf(
                    "id" to it.id,
                    "filePath" to it.filePath,
                    "duration" to it.duration,
                    "createdAt" to it.createdAt.format(formatter),
                    "transcription" to (it.transcription ?: "")
                )
            }),
            weather = weather,
            location = location,
            photos = gson.toJson(photos),
            wordCount = wordCount,
            readingTime = readingTime,
            color = color.name,
            reminder = reminder?.format(formatter),
            linkedEntries = gson.toJson(linkedEntries),
            characterCount = characterCount,
            paragraphCount = paragraphCount,
            sentenceCount = sentenceCount
        )
    }
    private fun JournalEntity.toJournalEntry(): JournalEntry {
        val voiceNotesList = try {
            val list = gson.fromJson<List<Map<String, Any>>>(
                voiceNotes,
                object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            )
            list.map { map ->
                JournalVoiceNote(
                    id = map["id"] as String,
                    filePath = map["filePath"] as String,
                    duration = (map["duration"] as Double).toLong(),
                    createdAt = LocalDateTime.parse(map["createdAt"] as String, formatter),
                    transcription = map["transcription"] as? String
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
        return JournalEntry(
            id = id,
            title = title,
            content = content,
            mood = Mood.valueOf(mood),
            tags = gson.fromJson(tags, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type),
            isFavorite = isFavorite,
            isArchived = isArchived,
            isPinned = isPinned,
            createdAt = LocalDateTime.parse(createdAt, formatter),
            updatedAt = LocalDateTime.parse(updatedAt, formatter),
            voiceNotes = voiceNotesList,
            weather = weather,
            location = location,
            photos = gson.fromJson(photos, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type),
            wordCount = wordCount,
            readingTime = readingTime,
            color = EntryColor.valueOf(color),
            reminder = reminder?.let { LocalDateTime.parse(it, formatter) },
            linkedEntries = gson.fromJson(linkedEntries, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type),
            characterCount = characterCount,
            paragraphCount = paragraphCount,
            sentenceCount = sentenceCount
        )
    }
}
