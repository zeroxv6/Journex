package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntity>>
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    suspend fun getAllEntriesOnce(): List<JournalEntity>
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: String): JournalEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntity)
    @Update
    suspend fun updateEntry(entry: JournalEntity)
    @Delete
    suspend fun deleteEntry(entry: JournalEntity)
    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)
    @Query("SELECT * FROM journal_entries WHERE isArchived = :isArchived ORDER BY createdAt DESC")
    fun getEntriesByArchiveStatus(isArchived: Boolean): Flow<List<JournalEntity>>
    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteEntries(): Flow<List<JournalEntity>>
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()
}
