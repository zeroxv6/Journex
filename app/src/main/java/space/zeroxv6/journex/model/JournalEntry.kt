package space.zeroxv6.journex.model
import java.time.LocalDateTime
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
    val voiceNotes: List<JournalVoiceNote> = emptyList(),
    val weather: String? = null,
    val location: String? = null,
    val photos: List<String> = emptyList(),
    val wordCount: Int = 0,
    val readingTime: Int = 0, 
    val color: EntryColor = EntryColor.DEFAULT,
    val reminder: LocalDateTime? = null,
    val linkedEntries: List<String> = emptyList(),
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
data class JournalVoiceNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val filePath: String,
    val duration: Long, 
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val transcription: String? = null
)
enum class Mood(val label: String, val icon: String) {
    AMAZING("Amazing", "★"),
    HAPPY("Happy", "◆"),
    GOOD("Good", "●"),
    NEUTRAL("Neutral", "○"),
    SAD("Sad", "◇"),
    ANXIOUS("Anxious", "△"),
    ANGRY("Angry", "▲"),
    TIRED("Tired", "◐"),
    EXCITED("Excited", "◉"),
    GRATEFUL("Grateful", "◈")
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
    val entriesThisYear: Int = 0
)
