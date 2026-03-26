package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.PromptDao
import space.zeroxv6.journex.data.PromptEntity
import kotlinx.coroutines.flow.Flow
class PromptRepository(private val promptDao: PromptDao) {
    fun getAllPrompts(): Flow<List<PromptEntity>> = promptDao.getAllPrompts()
    fun getPromptsByCategory(category: String): Flow<List<PromptEntity>> = promptDao.getPromptsByCategory(category)
    suspend fun getPromptById(id: String): PromptEntity? = promptDao.getPromptById(id)
    suspend fun insertPrompt(prompt: PromptEntity) = promptDao.insertPrompt(prompt)
    suspend fun updatePrompt(prompt: PromptEntity) = promptDao.updatePrompt(prompt)
    suspend fun deletePrompt(prompt: PromptEntity) = promptDao.deletePrompt(prompt)
    suspend fun deleteAllPrompts() = promptDao.deleteAllPrompts()
}
