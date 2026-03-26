package space.zeroxv6.journex.repository
import space.zeroxv6.journex.data.ProjectTaskDao
import space.zeroxv6.journex.data.ProjectTaskEntity
import space.zeroxv6.journex.data.TaskProjectEntity
import kotlinx.coroutines.flow.Flow
class ProjectTaskRepository(private val dao: ProjectTaskDao) {
    fun getAllTasks(): Flow<List<ProjectTaskEntity>> = dao.getAllTasks()
    fun getTasksByStatus(status: String): Flow<List<ProjectTaskEntity>> = 
        dao.getTasksByStatus(status)
    fun getTasksByProject(projectId: String): Flow<List<ProjectTaskEntity>> = 
        dao.getTasksByProject(projectId)
    suspend fun getTaskById(id: String): ProjectTaskEntity? = dao.getTaskById(id)
    suspend fun insertTask(task: ProjectTaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: ProjectTaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: ProjectTaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: String) = dao.deleteTaskById(id)
    fun getAllProjects(): Flow<List<TaskProjectEntity>> = dao.getAllProjects()
    suspend fun insertProject(project: TaskProjectEntity) = dao.insertProject(project)
    suspend fun updateProject(project: TaskProjectEntity) = dao.updateProject(project)
    suspend fun deleteProject(project: TaskProjectEntity) = dao.deleteProject(project)
}
