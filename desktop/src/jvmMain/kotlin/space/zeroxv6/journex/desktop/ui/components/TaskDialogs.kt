package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun DesktopAddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, TaskPriority, LocalDateTime?, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(550.dp)
                .heightIn(max = 650.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Create New Task",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Title", fontSize = 14.sp) },
                    placeholder = { Text("Enter task title...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.primary,
                        unfocusedBorderColor = AppColors.current.divider
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("Description", fontSize = 14.sp) },
                    placeholder = { Text("Enter task description...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.primary,
                        unfocusedBorderColor = AppColors.current.divider
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Priority",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        DesktopPriorityChip(
                            priority = priority,
                            isSelected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Tags",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add tag...", fontSize = 13.sp) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
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
                        }
                    )
                }
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppColors.current.primary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        tag,
                                        fontSize = 12.sp,
                                        color = AppColors.current.textPrimary
                                    )
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { tags = tags - tag },
                                        tint = AppColors.current.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title, description, selectedPriority, dueDate, tags)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        )
                    ) {
                        Text("Create Task", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
@Composable
fun DesktopTaskDetailDialog(
    task: ProjectTask,
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(800.dp)
                .heightIn(max = 850.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.current.cardBackground
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.current.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Task Details",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.current.textPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFF44336)
                                )
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
                }
                HorizontalDivider(color = AppColors.current.divider)
                HorizontalDivider(color = AppColors.current.divider)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Title",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                                IconButton(
                                    onClick = { 
                                        if (isEditingTitle) {
                                            editedTask = editedTask.copy(title = editedTitle)
                                        }
                                        isEditingTitle = !isEditingTitle 
                                    },
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
                                    onValueChange = { editedTitle = it },
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.current.textPrimary
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Description",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                                IconButton(
                                    onClick = { 
                                        if (isEditingDescription) {
                                            editedTask = editedTask.copy(description = editedDescription)
                                        }
                                        isEditingDescription = !isEditingDescription 
                                    },
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
                                    onValueChange = { editedDescription = it },
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
                    item {
                        Column {
                            Text(
                                "Status",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.current.textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TaskStatus.values().forEach { status ->
                                    DesktopStatusChip(
                                        status = status,
                                        isSelected = editedTask.status == status,
                                        onClick = {
                                            editedTask = editedTask.copy(
                                                status = status,
                                                completedAt = if (status == TaskStatus.DONE)
                                                    LocalDateTime.now() else null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Text(
                                "Priority",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.current.textSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TaskPriority.values().forEach { priority ->
                                    DesktopPriorityChip(
                                        priority = priority,
                                        isSelected = editedTask.priority == priority,
                                        onClick = {
                                            editedTask = editedTask.copy(priority = priority)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (editedTask.dueDate != null) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = AppColors.current.textSecondary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Due: ${editedTask.dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                                    fontSize = 14.sp,
                                    color = AppColors.current.textSecondary
                                )
                            }
                        }
                    }
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tags",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = tagInput,
                                    onValueChange = { tagInput = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Add tag...", fontSize = 13.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (tagInput.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    editedTask = editedTask.copy(
                                                        tags = editedTask.tags + tagInput.trim()
                                                    )
                                                    tagInput = ""
                                                }
                                            ) {
                                                Icon(Icons.Default.Add, "Add tag", modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                )
                            }
                            if (editedTask.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    editedTask.tags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AppColors.current.primary.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    tag,
                                                    fontSize = 13.sp,
                                                    color = AppColors.current.primary
                                                )
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable {
                                                            editedTask = editedTask.copy(
                                                                tags = editedTask.tags - tag
                                                            )
                                                        },
                                                    tint = AppColors.current.textSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (editedTask.tags.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    "Tags",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    editedTask.tags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AppColors.current.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                tag,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                fontSize = 13.sp,
                                                color = AppColors.current.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Subtasks (${editedTask.subtasks.count { it.isCompleted }}/${editedTask.subtasks.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                                IconButton(
                                    onClick = { showSubtaskInput = !showSubtaskInput },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (showSubtaskInput) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = "Add Subtask",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (showSubtaskInput) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newSubtaskTitle,
                                        onValueChange = { newSubtaskTitle = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Subtask title...", fontSize = 14.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                    IconButton(
                                        onClick = {
                                            if (newSubtaskTitle.isNotBlank()) {
                                                onAddSubtask(newSubtaskTitle)
                                                newSubtaskTitle = ""
                                                showSubtaskInput = false
                                            }
                                        },
                                        enabled = newSubtaskTitle.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Check, "Add")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            editedTask.subtasks.forEach { subtask ->
                                DesktopSubtaskItem(
                                    subtask = subtask,
                                    onToggle = { onToggleSubtask(subtask.id) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Comments (${editedTask.comments.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.current.textSecondary
                                )
                                IconButton(
                                    onClick = { showCommentInput = !showCommentInput },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        if (showCommentInput) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = "Add Comment",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            if (showCommentInput) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    OutlinedTextField(
                                        value = newComment,
                                        onValueChange = { newComment = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(100.dp),
                                        placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        maxLines = 4
                                    )
                                    IconButton(
                                        onClick = {
                                            if (newComment.isNotBlank()) {
                                                val comment = TaskComment(
                                                    text = newComment,
                                                    author = "You",
                                                    timestamp = LocalDateTime.now()
                                                )
                                                editedTask = editedTask.copy(
                                                    comments = editedTask.comments + comment
                                                )
                                                newComment = ""
                                                showCommentInput = false
                                            }
                                        },
                                        enabled = newComment.isNotBlank()
                                    ) {
                                        Icon(Icons.Default.Send, "Post comment")
                                    }
                                }
                            }
                            if (editedTask.comments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                editedTask.comments.sortedByDescending { it.timestamp }.forEach { comment ->
                                    DesktopCommentItem(
                                        comment = comment,
                                        onDelete = {
                                            editedTask = editedTask.copy(
                                                comments = editedTask.comments - comment
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Created: ${editedTask.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                                fontSize = 12.sp,
                                color = AppColors.current.textTertiary
                            )
                            Text(
                                "Updated: ${editedTask.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                                fontSize = 12.sp,
                                color = AppColors.current.textTertiary
                            )
                            editedTask.completedAt?.let {
                                Text(
                                    "Completed: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
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
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
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
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.current.primary
                        )
                    ) {
                        Text("Save Changes", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task?", fontWeight = FontWeight.SemiBold) },
            text = { Text("This action cannot be undone.", fontSize = 14.sp) },
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
fun DesktopPriorityChip(
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
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            priorityColor.copy(alpha = 0.15f)
        else AppColors.current.background,
        border = if (isSelected)
            BorderStroke(2.dp, priorityColor)
        else BorderStroke(1.dp, AppColors.current.divider)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                priority.label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) priorityColor else AppColors.current.textSecondary
            )
        }
    }
}
@Composable
fun DesktopStatusChip(
    status: TaskStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (status) {
        TaskStatus.NOT_STARTED -> Color(0xFF9E9E9E)
        TaskStatus.IN_PROGRESS -> Color(0xFF2196F3)
        TaskStatus.DONE -> Color(0xFF4CAF50)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            statusColor.copy(alpha = 0.15f)
        else AppColors.current.background,
        border = if (isSelected)
            BorderStroke(2.dp, statusColor)
        else BorderStroke(1.dp, AppColors.current.divider)
    ) {
        Text(
            status.label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) statusColor else AppColors.current.textSecondary
        )
    }
}
@Composable
fun DesktopSubtaskItem(
    subtask: Subtask,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = subtask.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.current.primary
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            subtask.title,
            fontSize = 15.sp,
            color = if (subtask.isCompleted)
                AppColors.current.textTertiary
            else AppColors.current.textPrimary,
            textDecoration = if (subtask.isCompleted)
                TextDecoration.LineThrough
            else null
        )
    }
}
@Composable
fun DesktopCommentItem(
    comment: TaskComment,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = AppColors.current.background
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
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
                    Surface(
                        shape = CircleShape,
                        color = AppColors.current.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                comment.author.first().uppercase(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.current.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            comment.author,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.current.textPrimary
                        )
                        Text(
                            comment.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                            fontSize = 12.sp,
                            color = AppColors.current.textTertiary
                        )
                    }
                }
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                comment.text,
                fontSize = 14.sp,
                color = AppColors.current.textPrimary,
                lineHeight = 20.sp
            )
        }
    }
}
