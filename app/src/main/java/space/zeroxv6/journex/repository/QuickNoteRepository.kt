package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.QuickNoteDao
import space.zeroxv6.journex.data.QuickNoteEntity
import kotlinx.coroutines.flow.Flow
class QuickNoteRepository(private val quickNoteDao: QuickNoteDao) {
    val allQuickNotes: Flow<List<QuickNoteEntity>> = quickNoteDao.getAllQuickNotes()
    suspend fun getAllQuickNotesOnce(): List<QuickNoteEntity> {
        return quickNoteDao.getAllQuickNotesOnce()
    }
    suspend fun insertQuickNote(note: QuickNoteEntity) {
        quickNoteDao.insertQuickNote(note)
    }
    suspend fun deleteQuickNote(note: QuickNoteEntity) {
        quickNoteDao.deleteQuickNote(note)
    }
    suspend fun deleteAllQuickNotes() {
        quickNoteDao.deleteAllQuickNotes()
    }
    suspend fun getQuickNoteById(id: String): QuickNoteEntity? {
        return quickNoteDao.getQuickNoteById(id)
    }
}
