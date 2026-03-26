package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import space.zeroxv6.journex.desktop.ui.components.*
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.ProjectTaskViewModel
import space.zeroxv6.journex.desktop.viewmodel.TaskFilter
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
enum class TaskView {
    BOARD, LIST, CALENDAR, TIMELINE, ANALYTICS
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopTaskManagementScreen(
    viewModel: ProjectTaskViewModel
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showProjectManagement by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<ProjectTask?>(null) }
    var selectedView by remember { mutableStateOf(TaskView.BOARD) }
    var selectedProject by remember { mutableStateOf<TaskProject?>(null) }
    var showQuickActions by remember { mutableStateOf(false) }
    var draggedTask by remember { mutableStateOf<ProjectTask?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val filteredTasks = remember(allTasks, searchQuery, selectedFilter, selectedProject) {
        allTasks.filter { task ->
            val matchesSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true) ||
                task.tags.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesFilter = when (selectedFilter) {
                TaskFilter.ALL -> true
                TaskFilter.HIGH_PRIORITY -> task.priority == TaskPriority.HIGH || task.priority == TaskPriority.URGENT
                TaskFilter.DUE_SOON -> task.dueDate?.let {
                    it.isAfter(LocalDateTime.now()) && it.isBefore(LocalDateTime.now().plusDays(7))
                } ?: false
                TaskFilter.OVERDUE -> task.dueDate?.let {
                    it.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE
                } ?: false
            }
            val matchesProject = selectedProject == null || task.projectId == selectedProject?.id
            matchesSearch && matchesFilter && matchesProject
        }
    }
    val notStartedTasks = filteredTasks.filter { it.status == TaskStatus.NOT_STARTED }
    val inProgressTasks = filteredTasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val doneTasks = filteredTasks.filter { it.status == TaskStatus.DONE }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.current.background,
                            AppColors.current.background.copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            EnhancedTaskHeader(
                selectedView = selectedView,
                onViewChange = { selectedView = it },
                onAddTask = { showAddDialog = true },
                onAddProject = { showAddProjectDialog = true },
                onManageProjects = { showProjectManagement = true },
                onQuickActions = { showQuickActions = !showQuickActions },
                taskCounts = Triple(notStartedTasks.size, inProgressTasks.size, doneTasks.size)
            )
            AdvancedSearchFilterBar(
                searchQuery = searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                selectedFilter = selectedFilter,
                onFilterChange = { viewModel.setFilter(it) },
                projects = allProjects,
                selectedProject = selectedProject,
                onProjectSelect = { selectedProject = it }
            )
            Box(modifier = Modifier.weight(1f)) {
                when (selectedView) {
                    TaskView.BOARD -> {
                        EnhancedKanbanBoard(
                            notStartedTasks = notStartedTasks,
                            inProgressTasks = inProgressTasks,
                            doneTasks = doneTasks,
                            onTaskClick = { selectedTask = it },
                            onStatusChange = { task, status ->
                                viewModel.updateTaskStatus(task.id, status)
                            },
                            draggedTask = draggedTask,
                            onDragStart = { draggedTask = it },
                            onDragEnd = { draggedTask = null }
                        )
                    }
                    TaskView.LIST -> {
                        EnhancedListView(
                            tasks = notStartedTasks + inProgressTasks + doneTasks,
                            onTaskClick = { selectedTask = it },
                            onStatusChange = { task, status ->
                                viewModel.updateTaskStatus(task.id, status)
                            },
                            onDelete = { viewModel.deleteTask(it) }
                        )
                    }
                    TaskView.CALENDAR -> {
                        CalendarView(
                            tasks = notStartedTasks + inProgressTasks + doneTasks,
                            onTaskClick = { selectedTask = it }
                        )
                    }
                    TaskView.TIMELINE -> {
                        TimelineView(
                            tasks = notStartedTasks + inProgressTasks + doneTasks,
                            onTaskClick = { selectedTask = it }
                        )
                    }
                    TaskView.ANALYTICS -> {
                        AnalyticsView(
                            notStartedTasks = notStartedTasks,
                            inProgressTasks = inProgressTasks,
                            doneTasks = doneTasks,
                            projects = allProjects
                        )
                    }
                }
            }
        }
    }
    if (showAddDialog) {
        EnhancedAddTaskDialog(
            onDismiss = { showAddDialog = false },
            projects = allProjects,
            onConfirm = { title, description, priority, dueDate, tags, projectId, estimatedHours ->
                viewModel.addTask(
                    title = title,
                    description = description,
                    priority = priority,
                    dueDate = dueDate,
                    tags = tags,
                    projectId = projectId,
                    estimatedHours = estimatedHours
                )
                showAddDialog = false
            }
        )
    }
    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name, description, color, icon ->
                viewModel.addProject(name, description, color, icon)
                showAddProjectDialog = false
            }
        )
    }
    selectedTask?.let { task ->
        val latestTask = remember(task.id) {
            derivedStateOf {
                allTasks.find { it.id == task.id } ?: task
            }
        }.value
        EnhancedTaskDetailDialog(
            task = latestTask,
            projects = allProjects,
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
    if (showProjectManagement) {
        ProjectManagementDialog(
            projects = allProjects,
            onDismiss = { showProjectManagement = false },
            onAddProject = { showAddProjectDialog = true },
            onDeleteProject = { project ->
                viewModel.deleteProject(project)
            }
        )
    }
}
@Composable
fun EnhancedTaskHeader(
    selectedView: TaskView,
    onViewChange: (TaskView) -> Unit,
    onAddTask: () -> Unit,
    onAddProject: () -> Unit,
    onManageProjects: () -> Unit,
    onQuickActions: () -> Unit,
    taskCounts: Triple<Int, Int, Int>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.current.cardBackground.copy(alpha = 0.7f),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.current.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = AppColors.current.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Task Management",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.current.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Organize, track, and complete your work efficiently",
                        fontSize = 14.sp,
                        color = AppColors.current.textTertiary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TaskStatsChip(
                        icon = Icons.Default.HourglassEmpty,
                        count = taskCounts.first,
                        label = "To Do",
                        color = Color(0xFF9E9E9E)
                    )
                    TaskStatsChip(
                        icon = Icons.Default.PlayArrow,
                        count = taskCounts.second,
                        label = "In Progress",
                        color = Color(0xFF2196F3)
                    )
                    TaskStatsChip(
                        icon = Icons.Default.CheckCircle,
                        count = taskCounts.third,
                        label = "Done",
                        color = Color(0xFF4CAF50)
                    )
                    Button(
                        onClick = onAddProject,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.background
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            "Add Project",
                            modifier = Modifier.size(20.dp),
                            tint = AppColors.current.textPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "New Project",
                            fontWeight = FontWeight.Medium,
                            color = AppColors.current.textPrimary
                        )
                    }
                    OutlinedButton(
                        onClick = onManageProjects,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            "Manage Projects",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Manage",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = onAddTask,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Add Task",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Task", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .background(
                        AppColors.current.background,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ViewTab(
                    icon = Icons.Default.Dashboard,
                    label = "Board",
                    isSelected = selectedView == TaskView.BOARD,
                    onClick = { onViewChange(TaskView.BOARD) }
                )
                ViewTab(
                    icon = Icons.Default.List,
                    label = "List",
                    isSelected = selectedView == TaskView.LIST,
                    onClick = { onViewChange(TaskView.LIST) }
                )
                ViewTab(
                    icon = Icons.Default.CalendarMonth,
                    label = "Calendar",
                    isSelected = selectedView == TaskView.CALENDAR,
                    onClick = { onViewChange(TaskView.CALENDAR) }
                )
                ViewTab(
                    icon = Icons.Default.Timeline,
                    label = "Timeline",
                    isSelected = selectedView == TaskView.TIMELINE,
                    onClick = { onViewChange(TaskView.TIMELINE) }
                )
                ViewTab(
                    icon = Icons.Default.Analytics,
                    label = "Analytics",
                    isSelected = selectedView == TaskView.ANALYTICS,
                    onClick = { onViewChange(TaskView.ANALYTICS) }
                )
            }
        }
    }
}
@Composable
fun TaskStatsChip(
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Text(
                count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 12.sp,
                color = AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun ViewTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            AppColors.current.primary
        else Color.Transparent,
        animationSpec = tween(300)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            Color.White
        else AppColors.current.textSecondary,
        animationSpec = tween(300)
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
@Composable
fun AdvancedSearchFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    projects: List<TaskProject>,
    selectedProject: TaskProject?,
    onProjectSelect: (TaskProject?) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.current.cardBackground.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Search tasks by title, description, or tags...",
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AppColors.current.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AppColors.current.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "Searching",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.current.primary
                                    )
                                }
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Clear",
                                        tint = AppColors.current.textTertiary
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.primary,
                        unfocusedBorderColor = AppColors.current.divider,
                        focusedContainerColor = AppColors.current.background,
                        unfocusedContainerColor = AppColors.current.background
                    ),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.values().forEach { filter ->
                        EnhancedFilterChip(
                            label = filter.label,
                            isSelected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            icon = when (filter) {
                                TaskFilter.ALL -> Icons.Default.Apps
                                TaskFilter.HIGH_PRIORITY -> Icons.Default.PriorityHigh
                                TaskFilter.DUE_SOON -> Icons.Default.Schedule
                                TaskFilter.OVERDUE -> Icons.Default.Warning
                            }
                        )
                    }
                    if (projects.isNotEmpty()) {
                        VerticalDivider(
                            modifier = Modifier.height(32.dp).padding(horizontal = 4.dp),
                            color = AppColors.current.divider
                        )
                        EnhancedFilterChip(
                            label = "All Projects",
                            isSelected = selectedProject == null,
                            onClick = { onProjectSelect(null) },
                            icon = Icons.Default.FolderOpen
                        )
                        projects.forEach { project ->
                            val colorString = project.color.removePrefix("#")
                            val colorInt = colorString.toLongOrNull(16) ?: 0xFF2196F3
                            val projectColor = Color(colorInt or 0xFF000000)
                            EnhancedFilterChip(
                                label = project.name,
                                isSelected = selectedProject?.id == project.id,
                                onClick = { onProjectSelect(project) },
                                color = projectColor
                            )
                        }
                    }
                }
                if (searchQuery.isNotEmpty() || selectedFilter != TaskFilter.ALL || selectedProject != null) {
                    TextButton(
                        onClick = {
                            onSearchChange("")
                            onFilterChange(TaskFilter.ALL)
                            onProjectSelect(null)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
@Composable
fun EnhancedFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    color: Color? = null
) {
    val chipColor = color ?: AppColors.current.primary
    val actualColor = if (color != null && color == chipColor) {
        chipColor
    } else {
        chipColor
    }
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                icon?.let {
                    Icon(
                        it,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = actualColor.copy(alpha = 0.15f),
            selectedLabelColor = actualColor,
            containerColor = AppColors.current.background
        ),
        border = if (isSelected)
            FilterChipDefaults.filterChipBorder(
                borderColor = actualColor.copy(alpha = 0.5f),
                borderWidth = 1.5.dp,
                enabled = true,
                selected = true
            )
        else null
    )
}
@Composable
fun EnhancedKanbanBoard(
    notStartedTasks: List<ProjectTask>,
    inProgressTasks: List<ProjectTask>,
    doneTasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit,
    draggedTask: ProjectTask?,
    onDragStart: (ProjectTask) -> Unit,
    onDragEnd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EnhancedKanbanColumn(
            title = "Not Started",
            count = notStartedTasks.size,
            color = Color(0xFF9E9E9E),
            tasks = notStartedTasks,
            status = TaskStatus.NOT_STARTED,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange,
            draggedTask = draggedTask,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd
        )
        EnhancedKanbanColumn(
            title = "In Progress",
            count = inProgressTasks.size,
            color = Color(0xFF2196F3),
            tasks = inProgressTasks,
            status = TaskStatus.IN_PROGRESS,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange,
            draggedTask = draggedTask,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd
        )
        EnhancedKanbanColumn(
            title = "Done",
            count = doneTasks.size,
            color = Color(0xFF4CAF50),
            tasks = doneTasks,
            status = TaskStatus.DONE,
            onTaskClick = onTaskClick,
            onStatusChange = onStatusChange,
            draggedTask = draggedTask,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd
        )
    }
}
@Composable
fun EnhancedKanbanColumn(
    title: String,
    count: Int,
    color: Color,
    tasks: List<ProjectTask>,
    status: TaskStatus,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit,
    draggedTask: ProjectTask?,
    onDragStart: (ProjectTask) -> Unit,
    onDragEnd: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .width(400.dp)
            .fillMaxHeight()
            .shadow(
                elevation = if (isHovered) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = color.copy(alpha = 0.2f),
                spotColor = color.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.cardBackground
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                            .shadow(4.dp, CircleShape)
                    )
                    Text(
                        title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.current.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        count.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = color.copy(alpha = 0.2f),
                thickness = 2.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    EnhancedTaskCard(
                        task = task,
                        onClick = { onTaskClick(task) },
                        onDragStart = { onDragStart(task) },
                        onDragEnd = onDragEnd,
                        isDragging = draggedTask?.id == task.id
                    )
                }
                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = color.copy(alpha = 0.3f)
                                )
                                Text(
                                    "No tasks here",
                                    fontSize = 14.sp,
                                    color = AppColors.current.textTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun EnhancedTaskCard(
    task: ProjectTask,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    isDragging: Boolean
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else if (isHovered) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.7f else 1f
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getPriorityColor(task.priority).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, getPriorityColor(task.priority).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when (task.priority) {
                                TaskPriority.URGENT -> Icons.Default.PriorityHigh
                                TaskPriority.HIGH -> Icons.Default.ArrowUpward
                                else -> Icons.Default.Remove
                            },
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = getPriorityColor(task.priority)
                        )
                        Text(
                            task.priority.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = getPriorityColor(task.priority)
                        )
                    }
                }
                task.estimatedHours?.let { hours ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.current.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = AppColors.current.primary
                            )
                            Text(
                                "${hours}h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.current.primary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                task.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.current.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    task.description,
                    fontSize = 13.sp,
                    color = AppColors.current.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            if (task.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    task.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppColors.current.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "#$tag",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.current.primary
                            )
                        }
                    }
                    if (task.tags.size > 3) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppColors.current.textTertiary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "+${task.tags.size - 3}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                color = AppColors.current.textTertiary
                            )
                        }
                    }
                }
            }
            if (task.dueDate != null || task.subtasks.isNotEmpty() || task.comments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = AppColors.current.divider.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        task.dueDate?.let { dueDate ->
                            val isOverdue = dueDate.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE
                            val daysUntil = ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isOverdue) Color(0xFFF44336) else AppColors.current.textTertiary
                                )
                                Text(
                                    when {
                                        isOverdue -> "Overdue"
                                        daysUntil == 0L -> "Today"
                                        daysUntil == 1L -> "Tomorrow"
                                        daysUntil < 7 -> "$daysUntil days"
                                        else -> dueDate.format(DateTimeFormatter.ofPattern("MMM dd"))
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isOverdue) Color(0xFFF44336) else AppColors.current.textTertiary
                                )
                            }
                        }
                        if (task.subtasks.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = AppColors.current.textTertiary
                                )
                                Text(
                                    "${task.subtasks.count { it.isCompleted }}/${task.subtasks.size}",
                                    fontSize = 12.sp,
                                    color = AppColors.current.textTertiary
                                )
                            }
                        }
                        if (task.comments.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = AppColors.current.textTertiary
                                )
                                Text(
                                    task.comments.size.toString(),
                                    fontSize = 12.sp,
                                    color = AppColors.current.textTertiary
                                )
                            }
                        }
                    }
                    task.assignee?.let { assignee ->
                        Surface(
                            shape = CircleShape,
                            color = AppColors.current.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    assignee.first().uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
fun getPriorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFF9E9E9E)
        TaskPriority.MEDIUM -> Color(0xFFFF9800)
        TaskPriority.HIGH -> Color(0xFFF44336)
        TaskPriority.URGENT -> Color(0xFFD32F2F)
    }
}
fun getStatusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.NOT_STARTED -> Color(0xFF9E9E9E)
        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
        TaskStatus.DONE -> Color(0xFF4CAF50)
    }
}
@Composable
fun EnhancedListView(
    tasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit,
    onStatusChange: (ProjectTask, TaskStatus) -> Unit,
    onDelete: (ProjectTask) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tasks.sortedByDescending { it.updatedAt }, key = { it.id }) { task ->
            EnhancedListItem(
                task = task,
                onClick = { onTaskClick(task) },
                onStatusChange = { onStatusChange(task, it) },
                onDelete = { onDelete(task) }
            )
        }
    }
}
@Composable
fun EnhancedListItem(
    task: ProjectTask,
    onClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onDelete: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(task.status))
                    .shadow(4.dp, CircleShape)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        task.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = getPriorityColor(task.priority).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, getPriorityColor(task.priority).copy(alpha = 0.3f))
                        ) {
                            Text(
                                task.priority.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = getPriorityColor(task.priority)
                            )
                        }
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Surface(
                                onClick = { expanded = true },
                                shape = RoundedCornerShape(8.dp),
                                color = getStatusColor(task.status).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, getStatusColor(task.status).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        task.status.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getStatusColor(task.status)
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = getStatusColor(task.status)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                TaskStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.label) },
                                        onClick = {
                                            onStatusChange(status)
                                            expanded = false
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(getStatusColor(status))
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        task.description,
                        fontSize = 14.sp,
                        color = AppColors.current.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    task.dueDate?.let { dueDate ->
                        val isOverdue = dueDate.isBefore(LocalDateTime.now()) && task.status != TaskStatus.DONE
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (isOverdue) Color(0xFFF44336) else AppColors.current.textTertiary
                            )
                            Text(
                                dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                fontSize = 13.sp,
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                                color = if (isOverdue) Color(0xFFF44336) else AppColors.current.textTertiary
                            )
                        }
                    }
                    if (task.subtasks.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = AppColors.current.textTertiary
                            )
                            Text(
                                "${task.subtasks.count { it.isCompleted }}/${task.subtasks.size} subtasks",
                                fontSize = 13.sp,
                                color = AppColors.current.textTertiary
                            )
                        }
                    }
                    if (task.comments.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Comment,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = AppColors.current.textTertiary
                            )
                            Text(
                                "${task.comments.size} comments",
                                fontSize = 13.sp,
                                color = AppColors.current.textTertiary
                            )
                        }
                    }
                    if (task.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            task.tags.take(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AppColors.current.primary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "#$tag",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        color = AppColors.current.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
@Composable
fun CalendarView(
    tasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDateTime.now()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.current.textPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { selectedDate = selectedDate.minusMonths(1) }
                ) {
                    Icon(Icons.Default.ChevronLeft, "Previous Month")
                }
                Button(
                    onClick = { selectedDate = LocalDateTime.now() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.current.primary
                    )
                ) {
                    Text("Today")
                }
                IconButton(
                    onClick = { selectedDate = selectedDate.plusMonths(1) }
                ) {
                    Icon(Icons.Default.ChevronRight, "Next Month")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Calendar view coming soon!",
                    fontSize = 16.sp,
                    color = AppColors.current.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Tasks with due dates: ${tasks.count { it.dueDate != null }}",
                    fontSize = 14.sp,
                    color = AppColors.current.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Composable
fun TimelineView(
    tasks: List<ProjectTask>,
    onTaskClick: (ProjectTask) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Task Timeline",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.current.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Chronological view of all tasks",
                fontSize = 14.sp,
                color = AppColors.current.textTertiary
            )
        }
        val sortedTasks = tasks.sortedByDescending { it.createdAt }
        items(sortedTasks, key = { it.id }) { task ->
            TimelineItem(task = task, onClick = { onTaskClick(task) })
        }
    }
}
@Composable
fun TimelineItem(
    task: ProjectTask,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(task.status))
                    .shadow(4.dp, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(AppColors.current.divider)
            )
        }
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getPriorityColor(task.priority).copy(alpha = 0.15f)
                    ) {
                        Text(
                            task.priority.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = getPriorityColor(task.priority)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Created ${task.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                    fontSize = 12.sp,
                    color = AppColors.current.textTertiary
                )
            }
        }
    }
}
@Composable
fun AnalyticsView(
    notStartedTasks: List<ProjectTask>,
    inProgressTasks: List<ProjectTask>,
    doneTasks: List<ProjectTask>,
    projects: List<TaskProject>
) {
    val allTasks = notStartedTasks + inProgressTasks + doneTasks
    val completionRate = if (allTasks.isNotEmpty())
        (doneTasks.size.toFloat() / allTasks.size * 100).toInt()
    else 0
    val overdueTasks = allTasks.filter {
        it.dueDate?.isBefore(LocalDateTime.now()) == true && it.status != TaskStatus.DONE
    }
    val highPriorityTasks = allTasks.filter {
        it.priority == TaskPriority.HIGH || it.priority == TaskPriority.URGENT
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                Text(
                    "Analytics Dashboard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Insights and statistics about your tasks",
                    fontSize = 14.sp,
                    color = AppColors.current.textTertiary
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModernStatCard(
                    title = "Total Tasks",
                    value = allTasks.size.toString(),
                    subtitle = "All time",
                    icon = Icons.Default.Assignment,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                ModernStatCard(
                    title = "Completed",
                    value = doneTasks.size.toString(),
                    subtitle = "${completionRate}% completion rate",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ModernStatCard(
                    title = "In Progress",
                    value = inProgressTasks.size.toString(),
                    subtitle = "Active tasks",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                ModernStatCard(
                    title = "Overdue",
                    value = overdueTasks.size.toString(),
                    subtitle = "Need attention",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.current.cardBackground
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Status Distribution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textPrimary
                            )
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = AppColors.current.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        StatusProgressBar(
                            label = "Not Started",
                            count = notStartedTasks.size,
                            total = allTasks.size,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StatusProgressBar(
                            label = "In Progress",
                            count = inProgressTasks.size,
                            total = allTasks.size,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StatusProgressBar(
                            label = "Done",
                            count = doneTasks.size,
                            total = allTasks.size,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.current.cardBackground
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Priority Distribution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textPrimary
                            )
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = AppColors.current.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        TaskPriority.values().forEach { priority ->
                            val count = allTasks.count { it.priority == priority }
                            PriorityProgressBar(
                                priority = priority,
                                count = count,
                                total = allTasks.size
                            )
                            if (priority != TaskPriority.URGENT) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
        if (projects.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.current.cardBackground
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Projects Overview",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textPrimary
                            )
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = AppColors.current.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        projects.forEach { project ->
                            val projectTasks = allTasks.filter { it.projectId == project.id }
                            val projectCompleted = projectTasks.count { it.status == TaskStatus.DONE }
                            ProjectStatItem(
                                project = project,
                                totalTasks = projectTasks.size,
                                completedTasks = projectCompleted
                            )
                            if (project != projects.last()) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InsightCard(
                    title = "High Priority",
                    value = highPriorityTasks.size.toString(),
                    description = "Tasks requiring immediate attention",
                    icon = Icons.Default.PriorityHigh,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                val tasksWithSubtasks = allTasks.filter { it.subtasks.isNotEmpty() }
                InsightCard(
                    title = "Complex Tasks",
                    value = tasksWithSubtasks.size.toString(),
                    description = "Tasks broken into subtasks",
                    icon = Icons.Default.AccountTree,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
                val tasksWithComments = allTasks.filter { it.comments.isNotEmpty() }
                InsightCard(
                    title = "Discussed",
                    value = tasksWithComments.size.toString(),
                    description = "Tasks with comments",
                    icon = Icons.Default.Comment,
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun ModernStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.cardBackground
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.current.textSecondary
                    )
                    Surface(
                        shape = CircleShape,
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Column {
                    Text(
                        value,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = AppColors.current.textTertiary
                    )
                }
            }
        }
    }
}
@Composable
fun StatusProgressBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.current.textPrimary
            )
            Text(
                "$count tasks ($percentage%)",
                fontSize = 13.sp,
                color = AppColors.current.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.current.divider.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
@Composable
fun PriorityProgressBar(
    priority: TaskPriority,
    count: Int,
    total: Int
) {
    val color = getPriorityColor(priority)
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    priority.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.current.textPrimary
                )
            }
            Text(
                "$count ($percentage%)",
                fontSize = 13.sp,
                color = AppColors.current.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.current.divider.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
@Composable
fun ProjectStatItem(
    project: TaskProject,
    totalTasks: Int,
    completedTasks: Int
) {
    val colorString = project.color.removePrefix("#")
    val colorInt = colorString.toLongOrNull(16) ?: 0xFF2196F3
    val projectColor = Color(colorInt or 0xFF000000)
    val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = projectColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = projectColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(
                    project.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.current.textPrimary
                )
                Text(
                    "$completedTasks of $totalTasks completed",
                    fontSize = 13.sp,
                    color = AppColors.current.textSecondary
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = projectColor.copy(alpha = 0.15f)
        ) {
            Text(
                "$completionRate%",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = projectColor
            )
        }
    }
}
@Composable
fun InsightCard(
    title: String,
    value: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.cardBackground
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.current.textSecondary
                )
                Text(
                    value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = AppColors.current.textTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun QuickActionsPanel(
    onClose: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBulkEdit: () -> Unit
) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.cardBackground
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textPrimary
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionItem(
                icon = Icons.Default.FileDownload,
                label = "Export Tasks",
                onClick = onExport
            )
            QuickActionItem(
                icon = Icons.Default.FileUpload,
                label = "Import Tasks",
                onClick = onImport
            )
            QuickActionItem(
                icon = Icons.Default.Edit,
                label = "Bulk Edit",
                onClick = onBulkEdit
            )
        }
    }
}
@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColors.current.textSecondary
            )
            Text(
                label,
                fontSize = 14.sp,
                color = AppColors.current.textPrimary
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementDialog(
    projects: List<TaskProject>,
    onDismiss: () -> Unit,
    onAddProject: () -> Unit,
    onDeleteProject: (TaskProject) -> Unit
) {
    var projectToDelete by remember { mutableStateOf<TaskProject?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(700.dp)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AppColors.current.primary
                            )
                            Column {
                                Text(
                                    "Project Management",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.textPrimary
                                )
                                Text(
                                    "${projects.size} project${if (projects.size != 1) "s" else ""}",
                                    fontSize = 13.sp,
                                    color = AppColors.current.textTertiary
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onAddProject,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.current.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Project")
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = AppColors.current.textTertiary.copy(alpha = 0.5f)
                            )
                            Text(
                                "No Projects Yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textSecondary
                            )
                            Text(
                                "Create your first project to organize tasks",
                                fontSize = 14.sp,
                                color = AppColors.current.textTertiary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddProject,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.current.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Project")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(projects, key = { it.id }) { project ->
                            ProjectManagementItem(
                                project = project,
                                onDelete = { projectToDelete = project }
                            )
                        }
                    }
                }
            }
        }
    }
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFF44336)
                )
            },
            title = {
                Text(
                    "Delete Project?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete \"${project.name}\"?",
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tasks in this project will not be deleted, but will become unassigned.",
                        fontSize = 14.sp,
                        color = AppColors.current.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(project)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { projectToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun ProjectManagementItem(
    project: TaskProject,
    onDelete: () -> Unit
) {
    val colorString = project.color.removePrefix("#")
    val colorInt = colorString.toLongOrNull(16) ?: 0xFF2196F3
    val projectColor = Color(colorInt or 0xFF000000)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.background
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = projectColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = projectColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        project.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textPrimary
                    )
                    if (project.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            project.description,
                            fontSize = 14.sp,
                            color = AppColors.current.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = projectColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(projectColor)
                                )
                                Text(
                                    "Color",
                                    fontSize = 11.sp,
                                    color = projectColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            "Created ${project.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                            fontSize = 12.sp,
                            color = AppColors.current.textTertiary
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(0xFFF44336).copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
