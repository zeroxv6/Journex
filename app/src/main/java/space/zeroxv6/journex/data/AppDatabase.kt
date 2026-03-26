package space.zeroxv6.journex.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [TodoEntity::class, ScheduleEntity::class, ReminderEntity::class, PromptEntity::class, SettingsEntity::class, JournalEntity::class, NoteEntity::class, NoteFolderEntity::class, ProjectTaskEntity::class, TaskProjectEntity::class, QuickNoteEntity::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun reminderDao(): ReminderDao
    abstract fun promptDao(): PromptDao
    abstract fun settingsDao(): SettingsDao
    abstract fun journalDao(): JournalDao
    abstract fun noteDao(): NoteDao
    abstract fun noteFolderDao(): NoteFolderDao
    abstract fun projectTaskDao(): ProjectTaskDao
    abstract fun quickNoteDao(): QuickNoteDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "journal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
