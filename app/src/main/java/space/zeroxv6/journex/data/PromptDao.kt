package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY createdAt DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>
    @Query("SELECT * FROM prompts WHERE category = :category ORDER BY createdAt DESC")
    fun getPromptsByCategory(category: String): Flow<List<PromptEntity>>
    @Query("SELECT * FROM prompts WHERE id = :id")
    suspend fun getPromptById(id: String): PromptEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)
    @Update
    suspend fun updatePrompt(prompt: PromptEntity)
    @Delete
    suspend fun deletePrompt(prompt: PromptEntity)
    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()
}
