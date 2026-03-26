package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getIncompleteTodos(limit: Int): Flow<List<TodoEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()
}
