package space.zeroxv6.journex.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.ui.components.AddTaskDialog
import space.zeroxv6.journex.ui.components.TaskDetailDialog
import space.zeroxv6.journex.viewmodel.ProjectTaskViewModel
import space.zeroxv6.journex.viewmodel.TaskFilter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    viewModel: ProjectTaskViewModel,
    onNavigateBack: () -> Unit
) {
    val notStartedTasks by viewModel.notStartedTasks.collectAsState()
    val inProgressTasks by viewModel.inProgressTasks.collectAsState()
    val doneTasks by viewModel.doneTasks.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<ProjectTask?>(null) }
    var selectedView by remember { mutableStateOf(TaskView.BOARD) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Task Management",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { selectedView = if (selectedView == TaskView.BOARD) TaskView.LIST else TaskView.BOARD }
                    ) {
                        Icon(
                            if (selectedView == TaskView.BOARD) Icons.Default.List else Icons.Default.Dashboard,
                            "Toggle View"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchAndFilterBar(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedFilter = selectedFilter,
                onFilterChange = { viewModel.setFilter(it) }
            )
            when (selectedView) {
                TaskView.BOARD -> {
                    TaskBoardView(
                        notStartedTasks = notStartedTasks,
                        inProgressTasks = inProgressTasks,
                        doneTasks = doneTasks,
                        onTaskClick = { selectedTask = it },
                        onStatusChange = { task, status ->
                            viewModel.updateTaskStatus(task.id, status)
                        }
                    )
                }
                TaskView.LIST -> {
                    TaskListView(
                        tasks = notStartedTasks + inProgressTasks + doneTasks,
                        onTaskClick = { selectedTask = it },
                        onStatusChange = { task, status ->
                            viewModel.updateTaskStatus(task.id, status)
                        }
                    )
                }
            }
        }
    }
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, priority, dueDate, tags ->
                viewModel.addTask(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    tags = tags
                )
                showAddDialog = false
            }
        )
    }
    selectedTask?.let { task ->
        val latestTask = remember(task.id) {
            derivedStateOf {
                (notStartedTasks + inProgressTasks + doneTasks).find { it.id == task.id } ?: task
            }
        }.value
        TaskDetailDialog(
            task = latestTask,
            onDismiss = { selectedTask = null },
            onUpdate = { updatedTask ->
                viewModel.updateTask(updatedTask)
                selectedTask = null
            },
            onDelete = {
                viewModel.deleteTask(latestTask)
                selectedTask = null
            },
            onAddSubtask = { subtaskTitle ->
                viewModel.addSubtask(latestTask.id, subtaskTitle)
            },
            onToggleSubtask = { subtaskId ->
                viewModel.toggleSubtask(latestTask.id, subtaskId)
            }
        )
    }
}
@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search tasks...", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterChange(filter) },
                    label = { 
                        Text(
                            filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (selectedFilter == filter) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
@Composable
fun TaskBoardView(
    notStartedTasks: List<ProjectTask>,
    inProgressTasks: List<ProjectTask>,
    doneTasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskColumn(
            title = "Not Started",
            count = notStartedTasks.size,
            color = Color(TaskStatus.NOT_STARTED.color),
            tasks = notStartedTasks,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange
        )
        TaskColumn(
            title = "In Progress",
            count = inProgressTasks.size,
            color = Color(TaskStatus.IN_PROGRESS.color),
            tasks = inProgressTasks,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange
        )
        TaskColumn(
            title = "Done",
            count = doneTasks.size,
            color = Color(TaskStatus.DONE.color),
            tasks = doneTasks,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange
        )
    }
}
@Composable
fun TaskColumn(
    title: String,
    count: Int,
    color: Color,
    tasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        count.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}
@Composable
fun TaskCard(
    task: ProjectTask,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(task.priority.color).copy(alpha = 0.1f)
            ) {
                Text(
                    task.priority.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(task.priority.color)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    task.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (task.dueDate != null || task.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    task.dueDate?.let { dueDate ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (dueDate.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE)
                                    Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                dueDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                                fontSize = 11.sp,
                                color = if (dueDate.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE)
                                    Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (task.subtasks.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${task.subtasks.count { it.isCompleted }}/${task.subtasks.size}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TaskListView(
    tasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskListItem(
                task = task,
                onClick = { onTaskClick(task) },
                onStatusChange = { status -> onStatusChange(task, status) }
            )
        }
    }
}
@Composable
fun TaskListItem(
    task: ProjectTask,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(task.status.color))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(task.priority.color).copy(alpha = 0.1f)
                    ) {
                        Text(
                            task.priority.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(task.priority.color)
                        )
                    }
                }
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        task.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(task.status.color).copy(alpha = 0.1f)
                    ) {
                        Text(
                            task.status.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(task.status.color)
                        )
                    }
                    task.dueDate?.let { dueDate ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
enum class TaskView {
    BOARD, LIST
}
