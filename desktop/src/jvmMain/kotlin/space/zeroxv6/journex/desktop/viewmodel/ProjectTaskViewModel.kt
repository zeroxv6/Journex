package space.zeroxv6.journex.desktop.viewmodel
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
class ProjectTaskViewModel(private val dataStore: JsonDataStore) {
    val allTasks: StateFlow<List<ProjectTask>> = dataStore.projectTasks
    val notStartedTasks: StateFlow<List<ProjectTask>> = allTasks.map { tasks ->
        tasks.filter { it.status == TaskStatus.NOT_STARTED }
            .sortedByDescending { it.updatedAt }
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    val inProgressTasks: StateFlow<List<ProjectTask>> = allTasks.map { tasks ->
        tasks.filter { it.status == TaskStatus.IN_PROGRESS }
            .sortedByDescending { it.updatedAt }
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    val doneTasks: StateFlow<List<ProjectTask>> = allTasks.map { tasks ->
        tasks.filter { it.status == TaskStatus.DONE }
            .sortedByDescending { it.completedAt ?: it.updatedAt }
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    val allProjects: StateFlow<List<TaskProject>> = dataStore.taskProjects
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter.asStateFlow()
    val filteredTasks: StateFlow<List<ProjectTask>> = combine(
        allTasks,
        searchQuery,
        selectedFilter
    ) { tasks, query, filter ->
        tasks.filter { task ->
            val matchesSearch = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.tags.any { it.contains(query, ignoreCase = true) }
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH || task.priority == TaskPriority.URGENT
                TaskFilter.DUE_SOON -> task.dueDate?.let { it.isBefore(LocalDateTime.now().plusDays(3)) } ?: false
                TaskFilter.OVERDUE -> task.dueDate?.let { it.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE } ?: false
            }
            matchesSearch && matchesFilter
        }
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), SharingStarted.Eagerly, emptyList())
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
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
        val task = ProjectTask(
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
        dataStore.saveProjectTask(task)
    }
    fun updateTask(task: ProjectTask) {
        val updated = task.copy(updatedAt = LocalDateTime.now())
        dataStore.saveProjectTask(updated)
    }
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        val task = dataStore.getProjectTask(taskId)
        task?.let {
            val updated = it.copy(
                status = newStatus,
                updatedAt = LocalDateTime.now(),
                completedAt = if (newStatus == TaskStatus.DONE) LocalDateTime.now() else null
            )
            dataStore.saveProjectTask(updated)
        }
    }
    fun deleteTask(task: ProjectTask) {
        dataStore.deleteProjectTask(task.id)
    }
    fun addSubtask(taskId: String, subtaskTitle: String) {
        val task = dataStore.getProjectTask(taskId)
        task?.let {
            val newSubtask = Subtask(title = subtaskTitle)
            val updated = it.copy(
                subtasks = it.subtasks + newSubtask,
                updatedAt = LocalDateTime.now()
            )
            dataStore.saveProjectTask(updated)
        }
    }
    fun toggleSubtask(taskId: String, subtaskId: String) {
        val task = dataStore.getProjectTask(taskId)
        task?.let {
            val updated = it.copy(
                subtasks = it.subtasks.map { subtask ->
                    if (subtask.id == subtaskId) subtask.copy(isCompleted = !subtask.isCompleted)
                    else subtask
                },
                updatedAt = LocalDateTime.now()
            )
            dataStore.saveProjectTask(updated)
        }
    }
    fun addProject(name: String, description: String = "", color: String = "#000000", icon: String = "📁") {
        val project = TaskProject(
            name = name,
            description = description,
            color = color,
            icon = icon,
            createdAt = LocalDateTime.now()
        )
        dataStore.saveTaskProject(project)
    }
    fun updateProject(project: TaskProject) {
        dataStore.saveTaskProject(project)
    }
    fun deleteProject(project: TaskProject) {
        dataStore.deleteTaskProject(project.id)
    }
}
enum class TaskFilter(val label: String) {
    ALL("All Tasks"),
    HIGH_PRIORITY("High Priority"),
    DUE_SOON("Due Soon"),
    OVERDUE("Overdue")
}
