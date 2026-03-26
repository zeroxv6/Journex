package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun getAllNotesOnce(): List<NoteEntity>
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteEntity?
    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE folderId = :folderId AND isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderId: String): Flow<List<NoteEntity>>
    @Query("SELECT * FROM notes WHERE category = :category AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getNotesByCategory(category: String): Flow<List<NoteEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)
    @Update
    suspend fun updateNote(note: NoteEntity)
    @Delete
    suspend fun deleteNote(note: NoteEntity)
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
    @Query("SELECT DISTINCT tags FROM notes WHERE isArchived = 0")
    suspend fun getAllTags(): List<String>
}
@Dao
interface NoteFolderDao {
    @Query("SELECT * FROM note_folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<NoteFolderEntity>>
    @Query("SELECT * FROM note_folders WHERE parentId IS NULL ORDER BY createdAt DESC")
    fun getRootFolders(): Flow<List<NoteFolderEntity>>
    @Query("SELECT * FROM note_folders WHERE parentId = :parentId ORDER BY createdAt DESC")
    fun getSubFolders(parentId: String): Flow<List<NoteFolderEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: NoteFolderEntity)
    @Update
    suspend fun updateFolder(folder: NoteFolderEntity)
    @Delete
    suspend fun deleteFolder(folder: NoteFolderEntity)
    @Query("DELETE FROM note_folders")
    suspend fun deleteAllFolders()
}
