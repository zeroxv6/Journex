package space.zeroxv6.journex.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import space.zeroxv6.journex.data.*
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.repository.ProjectTaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
class ProjectTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectTaskRepository(database.projectTaskDao())
    private val gson = Gson().newBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    val allTasks: StateFlow<List<ProjectTask>> = repository.getAllTasks()
        .map { entities -> entities.map { it.toProjectTask() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allProjects: StateFlow<List<TaskProject>> = repository.getAllProjects()
        .map { entities -> entities.map { it.toTaskProject() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _selectedFilter = MutableStateFlow<TaskFilter>(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val filteredTasks: StateFlow<List<ProjectTask>> = combine(
        allTasks,
        selectedFilter,
        searchQuery
    ) { tasks, filter, query ->
        tasks.filter { task ->
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH || task.priority == TaskPriority.URGENT
                TaskFilter.DUE_SOON -> task.dueDate?.let { it.isBefore(LocalDateTime.now().plusDays(3)) } ?: false
                TaskFilter.OVERDUE -> task.dueDate?.let { it.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE } ?: false
            }
            val matchesSearch = query.isBlank() || 
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.tags.any { it.contains(query, ignoreCase = true) }
            matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notStartedTasks: StateFlow<List<ProjectTask>> = filteredTasks
        .map { tasks -> tasks.filter { it.status == TaskStatus.NOT_STARTED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inProgressTasks: StateFlow<List<ProjectTask>> = filteredTasks
        .map { tasks -> tasks.filter { it.status == TaskStatus.IN_PROGRESS } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val doneTasks: StateFlow<List<ProjectTask>> = filteredTasks
        .map { tasks -> tasks.filter { it.status == TaskStatus.DONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun addTask(
        title: String,
        description: String = "",
        status: TaskStatus = TaskStatus.NOT_STARTED,
        priority: TaskPriority = TaskPriority.MEDIUM,
        tags: List<String> = emptyList(),
        assignee: String? = null,
        dueDate: LocalDateTime? = null,
        estimatedHours: Float? = null,
        projectId: String? = null
    ) {
        viewModelScope.launch {
            val task = ProjectTask(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                status = status,
                priority = priority,
                tags = tags,
                assignee = assignee,
                dueDate = dueDate,
                estimatedHours = estimatedHours,
                projectId = projectId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            repository.insertTask(task.toEntity())
        }
    }
    fun updateTask(task: ProjectTask) {
        viewModelScope.launch {
            val updated = task.copy(updatedAt = LocalDateTime.now())
            repository.updateTask(updated.toEntity())
        }
    }
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)?.toProjectTask()
            task?.let {
                val updated = it.copy(
                    status = newStatus,
                    updatedAt = LocalDateTime.now(),
                    completedAt = if (newStatus == TaskStatus.DONE) LocalDateTime.now() else null
                )
                repository.updateTask(updated.toEntity())
            }
        }
    }
    fun deleteTask(task: ProjectTask) {
        viewModelScope.launch {
            repository.deleteTask(task.toEntity())
        }
    }
    fun addSubtask(taskId: String, subtaskTitle: String) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)?.toProjectTask()
            task?.let {
                val newSubtask = Subtask(title = subtaskTitle)
                val updated = it.copy(
                    subtasks = it.subtasks + newSubtask,
                    updatedAt = LocalDateTime.now()
                )
                repository.updateTask(updated.toEntity())
            }
        }
    }
    fun toggleSubtask(taskId: String, subtaskId: String) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)?.toProjectTask()
            task?.let {
                val updated = it.copy(
                    subtasks = it.subtasks.map { subtask ->
                        if (subtask.id == subtaskId) subtask.copy(isCompleted = !subtask.isCompleted)
                        else subtask
                    },
                    updatedAt = LocalDateTime.now()
                )
                repository.updateTask(updated.toEntity())
            }
        }
    }
    fun addProject(name: String, description: String = "", color: Long = 0xFF000000, icon: String = "📁") {
        viewModelScope.launch {
            val project = TaskProject(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                color = color,
                icon = icon,
                createdAt = LocalDateTime.now()
            )
            repository.insertProject(project.toEntity())
        }
    }
    fun updateProject(project: TaskProject) {
        viewModelScope.launch {
            repository.updateProject(project.toEntity())
        }
    }
    fun deleteProject(project: TaskProject) {
        viewModelScope.launch {
            repository.deleteProject(project.toEntity())
        }
    }
    private fun ProjectTaskEntity.toProjectTask(): ProjectTask {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return ProjectTask(
            id = id,
            title = title,
            description = description,
            status = TaskStatus.valueOf(status),
            priority = TaskPriority.valueOf(priority),
            tags = gson.fromJson(tags, object : TypeToken<List<String>>() {}.type) ?: emptyList(),
            assignee = assignee,
            dueDate = dueDate?.let { LocalDateTime.parse(it, formatter) },
            estimatedHours = estimatedHours,
            actualHours = actualHours,
            createdAt = LocalDateTime.parse(createdAt, formatter),
            updatedAt = LocalDateTime.parse(updatedAt, formatter),
            completedAt = completedAt?.let { LocalDateTime.parse(it, formatter) },
            projectId = projectId,
            subtasks = gson.fromJson(subtasks, object : TypeToken<List<Subtask>>() {}.type) ?: emptyList(),
            attachments = gson.fromJson(attachments, object : TypeToken<List<String>>() {}.type) ?: emptyList(),
            comments = gson.fromJson(comments, object : TypeToken<List<TaskComment>>() {}.type) ?: emptyList()
        )
    }
    private fun ProjectTask.toEntity(): ProjectTaskEntity {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return ProjectTaskEntity(
            id = id,
            title = title,
            description = description,
            status = status.name,
            priority = priority.name,
            tags = gson.toJson(tags),
            assignee = assignee,
            dueDate = dueDate?.format(formatter),
            estimatedHours = estimatedHours,
            actualHours = actualHours,
            createdAt = createdAt.format(formatter),
            updatedAt = updatedAt.format(formatter),
            completedAt = completedAt?.format(formatter),
            projectId = projectId,
            subtasks = gson.toJson(subtasks),
            attachments = gson.toJson(attachments),
            comments = gson.toJson(comments)
        )
    }
    private fun TaskProjectEntity.toTaskProject(): TaskProject {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return TaskProject(
            id = id,
            name = name,
            description = description,
            color = color,
            icon = icon,
            createdAt = LocalDateTime.parse(createdAt, formatter)
        )
    }
    private fun TaskProject.toEntity(): TaskProjectEntity {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return TaskProjectEntity(
            id = id,
            name = name,
            description = description,
            color = color,
            icon = icon,
            createdAt = createdAt.format(formatter)
        )
    }
}
enum class TaskFilter(val label: String) {
    ALL("All Tasks"),
    HIGH_PRIORITY("High Priority"),
    DUE_SOON("Due Soon"),
    OVERDUE("Overdue")
}
class LocalDateTimeAdapter : com.google.gson.TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    override fun write(out: com.google.gson.stream.JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }
    override fun read(`in`: com.google.gson.stream.JsonReader): LocalDateTime? {
        if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val dateString = `in`.nextString()
        return try {
            LocalDateTime.parse(dateString, formatter)
        } catch (e: Exception) {
            LocalDateTime.now() 
        }
    }
}
