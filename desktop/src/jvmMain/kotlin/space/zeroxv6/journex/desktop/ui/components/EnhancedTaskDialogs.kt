package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddTaskDialog(
    onDismiss: () -> Unit,
    projects: List<TaskProject>,
    onConfirm: (String, String, TaskPriority, LocalDateTime?, List<String>, String?, Float?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedProject by remember { mutableStateOf<TaskProject?>(null) }
    var estimatedHours by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(650.dp)
                .heightIn(max = 750.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppColors.current.primary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
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
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AppColors.current.primary
                            )
                            Text(
                                "Create New Task",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textPrimary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column {
                            Text(
                                "Task Title *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter a clear, actionable task title...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.current.primary,
                                    unfocusedBorderColor = AppColors.current.divider
                                )
                            )
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Description",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                placeholder = { Text("Add more details about this task...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.current.primary,
                                    unfocusedBorderColor = AppColors.current.divider
                                )
                            )
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Priority Level",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TaskPriority.values().forEach { priority ->
                                    EnhancedPriorityChip(
                                        priority = priority,
                                        isSelected = selectedPriority == priority,
                                        onClick = { selectedPriority = priority },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    if (projects.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "Project",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.current.textPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    ProjectChip(
                                        project = null,
                                        isSelected = selectedProject == null,
                                        onClick = { selectedProject = null }
                                    )
                                    projects.forEach { project ->
                                        ProjectChip(
                                            project = project,
                                            isSelected = selectedProject?.id == project.id,
                                            onClick = { selectedProject = project }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Due Date",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.current.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                                            ?: "Set due date",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Estimated Hours",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.current.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = estimatedHours,
                                    onValueChange = { estimatedHours = it.filter { char -> char.isDigit() || char == '.' } },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("0.0", fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.current.primary,
                                        unfocusedBorderColor = AppColors.current.divider
                                    )
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Tags",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tagInput,
                                onValueChange = { tagInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Add tags (press Enter)...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Tag,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (tagInput.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                tags = tags + tagInput.trim()
                                                tagInput = ""
                                            }
                                        ) {
                                            Icon(Icons.Default.Add, "Add tag", modifier = Modifier.size(20.dp))
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.current.primary,
                                    unfocusedBorderColor = AppColors.current.divider
                                )
                            )
                            if (tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    tags.forEach { tag ->
                                        TagChip(
                                            tag = tag,
                                            onRemove = { tags = tags - tag }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val hours = estimatedHours.toFloatOrNull()
                                onConfirm(
                                    title,
                                    description,
                                    selectedPriority,
                                    dueDate,
                                    tags,
                                    selectedProject?.id,
                                    hours
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Task", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Set Due Date") },
            text = {
                Column {
                    Text(
                        "Quick options:",
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            dueDate = LocalDateTime.now().plusDays(1)
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tomorrow")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            dueDate = LocalDateTime.now().plusDays(7)
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next Week")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            dueDate = LocalDateTime.now().plusMonths(1)
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next Month")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                if (dueDate != null) {
                    TextButton(onClick = {
                        dueDate = null
                        showDatePicker = false
                    }) {
                        Text("Clear")
                    }
                }
            }
        )
    }
}
@Composable
fun EnhancedPriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (priority) {
        TaskPriority.LOW -> Color(0xFF9E9E9E)
        TaskPriority.MEDIUM -> Color(0xFFFF9800)
        TaskPriority.HIGH -> Color(0xFFF44336)
        TaskPriority.URGENT -> Color(0xFFD32F2F)
    }
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected)
            priorityColor.copy(alpha = 0.15f)
        else AppColors.current.background,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) priorityColor else AppColors.current.divider
        ),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                priority.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) priorityColor else AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun ProjectChip(
    project: TaskProject?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = if (project != null) {
        val colorString = project.color.removePrefix("#")
        val colorInt = colorString.toLongOrNull(16) ?: 0xFF2196F3
        Color(colorInt or 0xFF000000)
    } else {
        AppColors.current.textSecondary
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected)
            chipColor.copy(alpha = 0.15f)
        else AppColors.current.background,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) chipColor else AppColors.current.divider
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (project != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(chipColor)
                )
            }
            Text(
                project?.name ?: "No Project",
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) chipColor else AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun TagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = AppColors.current.primary.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, AppColors.current.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "#$tag",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.current.primary
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = AppColors.current.primary
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTaskDetailDialog(
    task: ProjectTask,
    projects: List<TaskProject>,
    onDismiss: () -> Unit,
    onUpdate: (ProjectTask) -> Unit,
    onDelete: () -> Unit,
    onAddSubtask: (String) -> Unit,
    onToggleSubtask: (String) -> Unit
) {
    var editedTask by remember { mutableStateOf(task) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var showSubtaskInput by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var showCommentInput by remember { mutableStateOf(false) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(task.title) }
    var isEditingDescription by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(task.description) }
    var tagInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(900.dp)
                .heightIn(max = 850.dp),
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
                    color = AppColors.current.background
                ) {
                    Column {
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
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(getStatusColor(editedTask.status))
                                        .shadow(4.dp, CircleShape)
                                )
                                Text(
                                    "Task Details",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.current.textPrimary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { showDeleteConfirm = true },
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
                                        tint = Color(0xFFF44336)
                                    )
                                }
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            AppColors.current.background,
                                            RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                        }
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = AppColors.current.primary
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Overview", fontWeight = FontWeight.Medium) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Subtasks", fontWeight = FontWeight.Medium)
                                        if (editedTask.subtasks.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = AppColors.current.primary.copy(alpha = 0.2f),
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        editedTask.subtasks.size.toString(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppColors.current.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Comments", fontWeight = FontWeight.Medium)
                                        if (editedTask.comments.isNotEmpty()) {
                                            Surface(
                                                shape = CircleShape,
                                                color = AppColors.current.primary.copy(alpha = 0.2f),
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        editedTask.comments.size.toString(),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppColors.current.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                text = { Text("Activity", fontWeight = FontWeight.Medium) }
                            )
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            item {
                                TaskOverviewContent(
                                    editedTask = editedTask,
                                    editedTitle = editedTitle,
                                    editedDescription = editedDescription,
                                    isEditingTitle = isEditingTitle,
                                    isEditingDescription = isEditingDescription,
                                    tagInput = tagInput,
                                    projects = projects,
                                    onTitleChange = { editedTitle = it },
                                    onDescriptionChange = { editedDescription = it },
                                    onToggleEditTitle = {
                                        if (isEditingTitle) {
                                            editedTask = editedTask.copy(title = editedTitle)
                                        }
                                        isEditingTitle = !isEditingTitle
                                    },
                                    onToggleEditDescription = {
                                        if (isEditingDescription) {
                                            editedTask = editedTask.copy(description = editedDescription)
                                        }
                                        isEditingDescription = !isEditingDescription
                                    },
                                    onStatusChange = { editedTask = editedTask.copy(status = it) },
                                    onPriorityChange = { editedTask = editedTask.copy(priority = it) },
                                    onTagInputChange = { tagInput = it },
                                    onAddTag = {
                                        if (tagInput.isNotBlank()) {
                                            editedTask = editedTask.copy(tags = editedTask.tags + tagInput.trim())
                                            tagInput = ""
                                        }
                                    },
                                    onRemoveTag = { tag ->
                                        editedTask = editedTask.copy(tags = editedTask.tags - tag)
                                    }
                                )
                            }
                        }
                        1 -> {
                            item {
                                SubtasksContent(
                                    subtasks = editedTask.subtasks,
                                    newSubtaskTitle = newSubtaskTitle,
                                    showSubtaskInput = showSubtaskInput,
                                    onNewSubtaskChange = { newSubtaskTitle = it },
                                    onToggleInput = { showSubtaskInput = !showSubtaskInput },
                                    onAddSubtask = {
                                        if (newSubtaskTitle.isNotBlank()) {
                                            val newSubtask = Subtask(title = newSubtaskTitle)
                                            editedTask = editedTask.copy(
                                                subtasks = editedTask.subtasks + newSubtask
                                            )
                                            onAddSubtask(newSubtaskTitle)
                                            newSubtaskTitle = ""
                                            showSubtaskInput = false
                                        }
                                    },
                                    onToggleSubtask = { subtaskId ->
                                        editedTask = editedTask.copy(
                                            subtasks = editedTask.subtasks.map { subtask ->
                                                if (subtask.id == subtaskId) {
                                                    subtask.copy(isCompleted = !subtask.isCompleted)
                                                } else subtask
                                            }
                                        )
                                        onToggleSubtask(subtaskId)
                                    }
                                )
                            }
                        }
                        2 -> {
                            item {
                                CommentsContent(
                                    comments = editedTask.comments,
                                    newComment = newComment,
                                    showCommentInput = showCommentInput,
                                    onNewCommentChange = { newComment = it },
                                    onToggleInput = { showCommentInput = !showCommentInput },
                                    onAddComment = {
                                        if (newComment.isNotBlank()) {
                                            val comment = TaskComment(
                                                text = newComment,
                                                author = "You",
                                                timestamp = LocalDateTime.now()
                                            )
                                            editedTask = editedTask.copy(comments = editedTask.comments + comment)
                                            newComment = ""
                                            showCommentInput = false
                                        }
                                    },
                                    onDeleteComment = { comment ->
                                        editedTask = editedTask.copy(comments = editedTask.comments - comment)
                                    }
                                )
                            }
                        }
                        3 -> {
                            item {
                                ActivityContent(task = editedTask)
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            val updated = editedTask.copy(
                                title = editedTitle,
                                description = editedDescription
                            )
                            onUpdate(updated)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
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
                    "Delete Task?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete \"${task.title}\"?",
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        fontSize = 14.sp,
                        color = AppColors.current.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
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
                    onClick = { showDeleteConfirm = false },
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
fun TaskOverviewContent(
    editedTask: ProjectTask,
    editedTitle: String,
    editedDescription: String,
    isEditingTitle: Boolean,
    isEditingDescription: Boolean,
    tagInput: String,
    projects: List<TaskProject>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleEditTitle: () -> Unit,
    onToggleEditDescription: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onTagInputChange: (String) -> Unit,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.background
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textSecondary
                    )
                    IconButton(
                        onClick = onToggleEditTitle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isEditingTitle) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Edit title",
                            modifier = Modifier.size(18.dp),
                            tint = AppColors.current.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (isEditingTitle) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = onTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.current.primary
                        )
                    )
                } else {
                    Text(
                        editedTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.current.textPrimary
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.background
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Description",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textSecondary
                    )
                    IconButton(
                        onClick = onToggleEditDescription,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (isEditingDescription) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = "Edit description",
                            modifier = Modifier.size(18.dp),
                            tint = AppColors.current.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (isEditingDescription) {
                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = onDescriptionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.current.primary
                        )
                    )
                } else {
                    if (editedDescription.isNotEmpty()) {
                        Text(
                            editedDescription,
                            fontSize = 15.sp,
                            color = AppColors.current.textSecondary,
                            lineHeight = 22.sp
                        )
                    } else {
                        Text(
                            "No description",
                            fontSize = 15.sp,
                            color = AppColors.current.textTertiary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Status",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskStatus.values().forEach { status ->
                            StatusOption(
                                status = status,
                                isSelected = editedTask.status == status,
                                onClick = { onStatusChange(status) }
                            )
                        }
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Priority",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.current.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskPriority.values().forEach { priority ->
                            PriorityOption(
                                priority = priority,
                                isSelected = editedTask.priority == priority,
                                onClick = { onPriorityChange(priority) }
                            )
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.background
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Tags",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.current.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = tagInput,
                    onValueChange = onTagInputChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add tag...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (tagInput.isNotBlank()) {
                            IconButton(onClick = onAddTag) {
                                Icon(Icons.Default.Add, "Add tag", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                )
                if (editedTask.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        editedTask.tags.forEach { tag ->
                            TagChip(tag = tag, onRemove = { onRemoveTag(tag) })
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.background
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (editedTask.dueDate != null) {
                    MetadataRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Due Date",
                        value = editedTask.dueDate!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    )
                }
                MetadataRow(
                    icon = Icons.Default.AccessTime,
                    label = "Created",
                    value = editedTask.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                )
                MetadataRow(
                    icon = Icons.Default.Update,
                    label = "Updated",
                    value = editedTask.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                )
                editedTask.completedAt?.let {
                    MetadataRow(
                        icon = Icons.Default.CheckCircle,
                        label = "Completed",
                        value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        valueColor = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}
@Composable
fun StatusOption(
    status: TaskStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = getStatusColor(status)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) statusColor.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) statusColor else AppColors.current.divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                status.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) statusColor else AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun PriorityOption(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val priorityColor = getPriorityColor(priority)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) priorityColor.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) priorityColor else AppColors.current.divider
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            Text(
                priority.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) priorityColor else AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = AppColors.current.textPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppColors.current.textTertiary
            )
            Text(
                label,
                fontSize = 13.sp,
                color = AppColors.current.textSecondary
            )
        }
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
fun getStatusColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.NOT_STARTED -> Color(0xFF9E9E9E)
        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
        TaskStatus.DONE -> Color(0xFF4CAF50)
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
@Composable
fun SubtasksContent(
    subtasks: List<Subtask>,
    newSubtaskTitle: String,
    showSubtaskInput: Boolean,
    onNewSubtaskChange: (String) -> Unit,
    onToggleInput: () -> Unit,
    onAddSubtask: () -> Unit,
    onToggleSubtask: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Subtasks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textPrimary
                )
                if (subtasks.isNotEmpty()) {
                    Text(
                        "${subtasks.count { it.isCompleted }} of ${subtasks.size} completed",
                        fontSize = 13.sp,
                        color = AppColors.current.textSecondary
                    )
                }
            }
            Button(
                onClick = onToggleInput,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showSubtaskInput)
                        AppColors.current.background
                    else AppColors.current.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    if (showSubtaskInput) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (showSubtaskInput)
                        AppColors.current.textPrimary
                    else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (showSubtaskInput) "Cancel" else "Add Subtask",
                    color = if (showSubtaskInput)
                        AppColors.current.textPrimary
                    else Color.White
                )
            }
        }
        AnimatedVisibility(
            visible = showSubtaskInput,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = onNewSubtaskChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Subtask title...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = onAddSubtask,
                        enabled = newSubtaskTitle.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, "Add")
                    }
                }
            }
        }
        if (subtasks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.current.textTertiary.copy(alpha = 0.5f)
                        )
                        Text(
                            "No subtasks yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.current.textSecondary
                        )
                        Text(
                            "Break down this task into smaller steps",
                            fontSize = 13.sp,
                            color = AppColors.current.textTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                subtasks.forEach { subtask ->
                    EnhancedSubtaskItem(
                        subtask = subtask,
                        onToggle = { onToggleSubtask(subtask.id) }
                    )
                }
            }
        }
    }
}
@Composable
fun EnhancedSubtaskItem(
    subtask: Subtask,
    onToggle: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subtask.isCompleted)
                AppColors.current.background.copy(alpha = 0.5f)
            else AppColors.current.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = subtask.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AppColors.current.primary
                )
            )
            Text(
                subtask.title,
                fontSize = 15.sp,
                fontWeight = if (subtask.isCompleted) FontWeight.Normal else FontWeight.Medium,
                color = if (subtask.isCompleted)
                    AppColors.current.textTertiary
                else AppColors.current.textPrimary,
                textDecoration = if (subtask.isCompleted)
                    TextDecoration.LineThrough
                else null,
                modifier = Modifier.weight(1f)
            )
            if (subtask.isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}
@Composable
fun CommentsContent(
    comments: List<TaskComment>,
    newComment: String,
    showCommentInput: Boolean,
    onNewCommentChange: (String) -> Unit,
    onToggleInput: () -> Unit,
    onAddComment: () -> Unit,
    onDeleteComment: (TaskComment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Comments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.current.textPrimary
                )
                if (comments.isNotEmpty()) {
                    Text(
                        "${comments.size} comment${if (comments.size != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = AppColors.current.textSecondary
                    )
                }
            }
            Button(
                onClick = onToggleInput,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showCommentInput)
                        AppColors.current.background
                    else AppColors.current.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    if (showCommentInput) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (showCommentInput)
                        AppColors.current.textPrimary
                    else Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (showCommentInput) "Cancel" else "Add Comment",
                    color = if (showCommentInput)
                        AppColors.current.textPrimary
                    else Color.White
                )
            }
        }
        AnimatedVisibility(
            visible = showCommentInput,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = onNewCommentChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 5
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onAddComment,
                            enabled = newComment.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.current.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Post Comment")
                        }
                    }
                }
            }
        }
        if (comments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.current.background
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Comment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.current.textTertiary.copy(alpha = 0.5f)
                        )
                        Text(
                            "No comments yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.current.textSecondary
                        )
                        Text(
                            "Start a discussion about this task",
                            fontSize = 13.sp,
                            color = AppColors.current.textTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                comments.sortedByDescending { it.timestamp }.forEach { comment ->
                    EnhancedCommentItem(
                        comment = comment,
                        onDelete = { onDeleteComment(comment) }
                    )
                }
            }
        }
    }
}
@Composable
fun EnhancedCommentItem(
    comment: TaskComment,
    onDelete: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.current.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                comment.author.first().uppercase(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            comment.author,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.current.textPrimary
                        )
                        Text(
                            comment.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                            fontSize = 12.sp,
                            color = AppColors.current.textTertiary
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isHovered,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete comment",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                comment.text,
                fontSize = 14.sp,
                color = AppColors.current.textPrimary,
                lineHeight = 20.sp
            )
        }
    }
}
@Composable
fun ActivityContent(
    task: ProjectTask
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Activity Timeline",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.current.textPrimary
        )
        ActivityItem(
            icon = Icons.Default.Add,
            title = "Task Created",
            timestamp = task.createdAt,
            color = Color(0xFF2196F3)
        )
        if (task.updatedAt != task.createdAt) {
            ActivityItem(
                icon = Icons.Default.Edit,
                title = "Task Updated",
                timestamp = task.updatedAt,
                color = Color(0xFFFF9800)
            )
        }
        task.completedAt?.let {
            ActivityItem(
                icon = Icons.Default.CheckCircle,
                title = "Task Completed",
                timestamp = it,
                color = Color(0xFF4CAF50)
            )
        }
    }
}
@Composable
fun ActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    timestamp: LocalDateTime,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.current.background
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.current.textPrimary
                )
                Text(
                    timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                    fontSize = 13.sp,
                    color = AppColors.current.textSecondary
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var selectedIcon by remember { mutableStateOf("📁") }
    val colors = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#F44336",
        "#9C27B0", "#00BCD4", "#FFEB3B", "#795548"
    )
    val icons = listOf(
        "📁", "📊", "💼", "🎯", "🚀", "💡", "🎨", "📱",
        "🏆", "⚡", "🔥", "✨", "🌟", "💎", "🎪", "🎭"
    )
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(550.dp)
                .heightIn(max = 700.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            ),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
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
                            Text(
                                "Create New Project",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.current.textPrimary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column {
                            Text(
                                "Project Name *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Enter project name...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Description",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                placeholder = { Text("Project description...", fontSize = 14.sp) },
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Icon",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                icons.forEach { icon ->
                                    Surface(
                                        onClick = { selectedIcon = icon },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selectedIcon == icon)
                                            AppColors.current.primary.copy(alpha = 0.2f)
                                        else AppColors.current.background,
                                        border = BorderStroke(
                                            width = if (selectedIcon == icon) 2.dp else 1.dp,
                                            color = if (selectedIcon == icon)
                                                AppColors.current.primary
                                            else AppColors.current.divider
                                        ),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(icon, fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Color",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                colors.forEach { color ->
                                    val colorString = color.removePrefix("#")
                                    val colorInt = colorString.toLongOrNull(16) ?: 0xFF2196F3
                                    val colorValue = Color(colorInt or 0xFF000000)
                                    Surface(
                                        onClick = { selectedColor = color },
                                        shape = CircleShape,
                                        color = colorValue,
                                        border = if (selectedColor == color)
                                            BorderStroke(3.dp, AppColors.current.textPrimary)
                                        else null,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        if (selectedColor == color) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name, description, selectedColor, selectedIcon)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Project", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
