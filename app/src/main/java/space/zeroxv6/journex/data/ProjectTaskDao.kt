package space.zeroxv6.journex.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface ProjectTaskDao {
    @Query("SELECT * FROM project_tasks ORDER BY updatedAt DESC")
    fun getAllTasks(): Flow<List<ProjectTaskEntity>>
    @Query("SELECT * FROM project_tasks ORDER BY updatedAt DESC")
    suspend fun getAllTasksOnce(): List<ProjectTaskEntity>
    @Query("SELECT * FROM project_tasks WHERE status = :status ORDER BY priority DESC, updatedAt DESC")
    fun getTasksByStatus(status: String): Flow<List<ProjectTaskEntity>>
    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getTasksByProject(projectId: String): Flow<List<ProjectTaskEntity>>
    @Query("SELECT * FROM project_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): ProjectTaskEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ProjectTaskEntity)
    @Update
    suspend fun updateTask(task: ProjectTaskEntity)
    @Delete
    suspend fun deleteTask(task: ProjectTaskEntity)
    @Query("DELETE FROM project_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)
    @Query("DELETE FROM project_tasks")
    suspend fun deleteAllTasks()
    @Query("SELECT * FROM task_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<TaskProjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: TaskProjectEntity)
    @Update
    suspend fun updateProject(project: TaskProjectEntity)
    @Delete
    suspend fun deleteProject(project: TaskProjectEntity)
    @Query("DELETE FROM task_projects")
    suspend fun deleteAllProjects()
}
