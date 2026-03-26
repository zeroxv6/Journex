package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.TodoDao
import space.zeroxv6.journex.data.TodoEntity
import kotlinx.coroutines.flow.Flow
class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    fun getIncompleteTodos(limit: Int): Flow<List<TodoEntity>> = todoDao.getIncompleteTodos(limit)
    suspend fun insertTodo(todo: TodoEntity) = todoDao.insertTodo(todo)
    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)
    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
    suspend fun deleteAllTodos() = todoDao.deleteAllTodos()
}
