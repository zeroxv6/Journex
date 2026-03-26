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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopTasksScreen(viewModel: TaskViewModel, onNavigate: (String) -> Unit) {
    val incompleteTasks by viewModel.incompleteTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoTask?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Tasks", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${incompleteTasks.size} active · ${completedTasks.size} completed", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { showCompleted = !showCompleted }) { Text(if (showCompleted) "Hide Done" else "Show Done", color = AppColors.current.textSecondary, style = MaterialTheme.typography.titleMedium) }
                Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("New Task", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (incompleteTasks.isEmpty() && (!showCompleted || completedTasks.isEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) { Icon(Icons.Outlined.TaskAlt, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("All caught up!", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pending tasks", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (incompleteTasks.isNotEmpty()) {
                    item { Text("Active Tasks", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(incompleteTasks, key = { it.id }) { task -> TaskCard(task, { viewModel.toggleTaskCompletion(task) }, { editingTask = task }, { viewModel.deleteTask(task.id) }) }
                }
                if (showCompleted && completedTasks.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(20.dp)); Text("Completed", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textTertiary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(completedTasks, key = { it.id }) { task -> TaskCard(task, { viewModel.toggleTaskCompletion(task) }, { editingTask = task }, { viewModel.deleteTask(task.id) }) }
                }
            }
        }
    }
    if (showAddDialog) TaskDialog("New Task", null, { showAddDialog = false }) { t, d, p, dd -> viewModel.addTask(t, d, p, dd); showAddDialog = false }
    editingTask?.let { task -> TaskDialog("Edit Task", task, { editingTask = null }) { t, d, p, dd -> viewModel.updateTask(task.copy(title = t, description = d, priority = p, dueDate = dd)); editingTask = null } }
}
@Composable
private fun TaskCard(task: TodoTask, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) { 
                Icon(
                    if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    null,
                    tint = if (task.isCompleted) AppColors.current.textSecondary else AppColors.current.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) AppColors.current.textDisabled else AppColors.current.textPrimary
                )
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(task.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
                }
                task.dueDate?.let { date ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textTertiary)
                        Text(date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary)
                    }
                }
            }
            if (!task.isCompleted) {
                Surface(shape = RoundedCornerShape(8.dp), color = when(task.priority) {
                    Priority.HIGH -> AppColors.current.error.copy(alpha = 0.1f)
                    Priority.MEDIUM -> AppColors.current.warning.copy(alpha = 0.1f)
                    Priority.LOW -> AppColors.current.success.copy(alpha = 0.1f)
                }) {
                    Text(
                        task.priority.label,
                        modifier = Modifier.padding(12.dp, 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = when(task.priority) {
                            Priority.HIGH -> AppColors.current.error
                            Priority.MEDIUM -> AppColors.current.warning
                            Priority.LOW -> AppColors.current.success
                        }
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) { 
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(24.dp)) 
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                    DropdownMenuItem(text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDialog(title: String, task: TodoTask?, onDismiss: () -> Unit, onSave: (String, String, Priority, LocalDate?) -> Unit) {
    var taskTitle by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: Priority.MEDIUM) }
    var hasDueDate by remember { mutableStateOf(task?.dueDate != null) }
    var dueDate by remember { mutableStateOf(task?.dueDate ?: LocalDate.now().plusDays(1)) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = taskTitle, onValueChange = { taskTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary))
                Text("Priority", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Priority.entries.forEach { p -> FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p.label) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.textPrimary, selectedLabelColor = colors.background)) } }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Due Date", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Switch(checked = hasDueDate, onCheckedChange = { hasDueDate = it }, colors = SwitchDefaults.colors(checkedThumbColor = colors.background, checkedTrackColor = colors.textPrimary))
                }
                if (hasDueDate) OutlinedTextField(value = dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), onValueChange = { try { dueDate = LocalDate.parse(it) } catch (_: Exception) {} }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary))
            }
        },
        confirmButton = { Button(onClick = { onSave(taskTitle, description, priority, if (hasDueDate) dueDate else null) }, enabled = taskTitle.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
