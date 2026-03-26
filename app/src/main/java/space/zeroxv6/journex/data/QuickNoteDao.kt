package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes ORDER BY createdAt DESC")
    fun getAllQuickNotes(): Flow<List<QuickNoteEntity>>
    @Query("SELECT * FROM quick_notes ORDER BY createdAt DESC")
    suspend fun getAllQuickNotesOnce(): List<QuickNoteEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickNote(note: QuickNoteEntity)
    @Delete
    suspend fun deleteQuickNote(note: QuickNoteEntity)
    @Query("DELETE FROM quick_notes")
    suspend fun deleteAllQuickNotes()
    @Query("SELECT * FROM quick_notes WHERE id = :id")
    suspend fun getQuickNoteById(id: String): QuickNoteEntity?
}
