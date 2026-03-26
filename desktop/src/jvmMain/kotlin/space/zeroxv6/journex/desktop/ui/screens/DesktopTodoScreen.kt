package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopTodoScreen(
    viewModel: TaskViewModel,
    onNavigate: (String) -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val incompleteTasks by viewModel.incompleteTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoTask?>(null) }
    var selectedFilter by remember { mutableStateOf("all") }
    val filteredIncompleteTasks = when (selectedFilter) {
        "high" -> incompleteTasks.filter { it.priority == Priority.HIGH }
        "medium" -> incompleteTasks.filter { it.priority == Priority.MEDIUM }
        "low" -> incompleteTasks.filter { it.priority == Priority.LOW }
        "today" -> incompleteTasks.filter { it.dueDate == LocalDate.now() }
        "overdue" -> incompleteTasks.filter { it.dueDate != null && it.dueDate!! < LocalDate.now() }
        else -> incompleteTasks
    }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Todo", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp), color = AppColors.current.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${incompleteTasks.size} pending · ${completedTasks.size} completed", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onNavigate("schedule") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.current.textSecondary),
                    border = BorderStroke(1.dp, AppColors.current.border)
                ) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule")
                }
                OutlinedButton(
                    onClick = { onNavigate("reminders") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.current.textSecondary),
                    border = BorderStroke(1.dp, AppColors.current.border)
                ) {
                    Icon(Icons.Outlined.NotificationsNone, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reminders")
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Task", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all" to "All", "high" to "High Priority", "medium" to "Medium", "low" to "Low", "today" to "Due Today", "overdue" to "Overdue").forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.current.textPrimary,
                        selectedLabelColor = AppColors.current.background,
                        containerColor = AppColors.current.cardBackground,
                        labelColor = AppColors.current.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = AppColors.current.border,
                        selectedBorderColor = Color.Transparent,
                        enabled = true,
                        selected = selectedFilter == key
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = { showCompleted = !showCompleted }) {
                Text(if (showCompleted) "Hide Done" else "Show Done", color = AppColors.current.textTertiary, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (filteredIncompleteTasks.isEmpty() && (!showCompleted || completedTasks.isEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) {
                        Icon(Icons.Outlined.TaskAlt, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("All caught up!", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium), color = AppColors.current.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (selectedFilter == "all") "No pending tasks" else "No tasks matching filter", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (filteredIncompleteTasks.isNotEmpty()) {
                    item {
                        Text("Active Tasks (${filteredIncompleteTasks.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(filteredIncompleteTasks, key = { it.id }) { task ->
                        TodoTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { editingTask = task },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
                if (showCompleted && completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Completed (${completedTasks.size})", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textTertiary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(completedTasks, key = { it.id }) { task ->
                        TodoTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onEdit = { editingTask = task },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }
    if (showAddDialog) {
        TodoAddDialog(
            onDismiss = { showAddDialog = false },
            onSave = { t, d, p, dd ->
                viewModel.addTask(t, d, p, dd)
                showAddDialog = false
            }
        )
    }
    editingTask?.let { task ->
        TodoAddDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { t, d, p, dd ->
                viewModel.updateTask(task.copy(title = t, description = d, priority = p, dueDate = dd))
                editingTask = null
            }
        )
    }
}
@Composable
private fun TodoTaskCard(task: TodoTask, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(14.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    null,
                    tint = if (task.isCompleted) AppColors.current.success else AppColors.current.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) AppColors.current.textDisabled else AppColors.current.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(task.description, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            task.dueDate?.let { date ->
                val isOverdue = !task.isCompleted && date < LocalDate.now()
                val isToday = date == LocalDate.now()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isOverdue -> AppColors.current.error.copy(alpha = 0.1f)
                        isToday -> AppColors.current.warning.copy(alpha = 0.1f)
                        else -> AppColors.current.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = when {
                                isOverdue -> AppColors.current.error
                                isToday -> AppColors.current.warning
                                else -> AppColors.current.textSecondary
                            }
                        )
                        Text(
                            when {
                                isToday -> "Today"
                                isOverdue -> "Overdue"
                                else -> date.format(DateTimeFormatter.ofPattern("MMM d"))
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = when {
                                isOverdue -> AppColors.current.error
                                isToday -> AppColors.current.warning
                                else -> AppColors.current.textSecondary
                            }
                        )
                    }
                }
            }
            if (!task.isCompleted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (task.priority) {
                        Priority.HIGH -> AppColors.current.error.copy(alpha = 0.15f)
                        Priority.MEDIUM -> AppColors.current.warning.copy(alpha = 0.15f)
                        Priority.LOW -> AppColors.current.success.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when (task.priority) {
                                        Priority.HIGH -> AppColors.current.error
                                        Priority.MEDIUM -> AppColors.current.warning
                                        Priority.LOW -> AppColors.current.success
                                    }
                                )
                        )
                        Text(
                            task.priority.label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = when (task.priority) {
                                Priority.HIGH -> AppColors.current.error
                                Priority.MEDIUM -> AppColors.current.warning
                                Priority.LOW -> AppColors.current.success
                            }
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text("Delete", color = AppColors.current.error) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = AppColors.current.error, modifier = Modifier.size(18.dp)) })
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoAddDialog(task: TodoTask? = null, onDismiss: () -> Unit, onSave: (String, String, Priority, LocalDate?) -> Unit) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var hasDueDate by remember { mutableStateOf(task?.dueDate != null) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: LocalDate.now().plusDays(1)) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "New Task" else "Edit Task", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
                Text("Priority", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p, onClick = { priority = p },
                            label = { Text(p.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.textPrimary, selectedLabelColor = colors.background)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Due Date", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Switch(checked = hasDueDate, onCheckedChange = { hasDueDate = it }, colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.textPrimary))
                }
                if (hasDueDate) {
                    OutlinedTextField(
                        value = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = { try { dueDate = LocalDate.parse(it) } catch (_: Exception) {} },
                        label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description, priority, if (hasDueDate) dueDate else null) },
                enabled = title.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
