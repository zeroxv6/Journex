package space.zeroxv6.journex.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.Priority
import space.zeroxv6.journex.model.TodoTask
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.ui.theme.FeatureColors
import space.zeroxv6.journex.ui.utils.HapticFeedback
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tasks by taskViewModel.allTodos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskInput by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var showCompleted by remember { mutableStateOf(false) }
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        OutlinedButton(
                            onClick = { showCompleted = !showCompleted },
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showCompleted) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(if (showCompleted) "View Active" else "View Completed", style = MaterialTheme.typography.labelLarge)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        },
        floatingActionButton = {
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "fabScale"
            )
            val rotation by animateFloatAsState(
                targetValue = if (isPressed) 90f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "fabRotation"
            )
            ExtendedFloatingActionButton(
                onClick = { 
                    HapticFeedback.perform(context, HapticFeedback.FeedbackType.MEDIUM)
                    showAddDialog = true 
                },
                containerColor = FeatureColors.TodoAccentDark,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer { rotationZ = rotation }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Task", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    ) { padding ->
        val coroutineScope = rememberCoroutineScope()
        var completingTasks by remember { mutableStateOf(setOf<String>()) }
        
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Tasks",
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your first task to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!showCompleted) {
                    if (activeTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active (${activeTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        items(
                            items = activeTasks.sortedByDescending { it.priority.ordinal },
                            key = { it.id }
                        ) { task ->
                            AnimatedVisibility(
                                visible = !completingTasks.contains(task.id),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(animationSpec = tween(500, delayMillis = 400))
                            ) {
                                TaskCard(
                                    task = task,
                                    isCompleting = completingTasks.contains(task.id),
                                    onToggleComplete = {
                                        if (!task.isCompleted && !completingTasks.contains(task.id)) {
                                            completingTasks = completingTasks + task.id
                                            HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(800)
                                                taskViewModel.toggleTodoCompletion(task)
                                                completingTasks = completingTasks - task.id
                                            }
                                        } else {
                                            taskViewModel.toggleTodoCompletion(task)
                                        }
                                    },
                                    onDelete = {
                                        taskViewModel.deleteTodo(task)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    if (completedTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed (${completedTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        items(completedTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                isCompleting = false,
                                onToggleComplete = {
                                    taskViewModel.toggleTodoCompletion(task)
                                },
                                onDelete = {
                                    taskViewModel.deleteTodo(task)
                                }
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            } // end LazyColumn
        } // end Column
    } // end Scaffold
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { 
                Text(
                    "New Task",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = taskInput,
                        onValueChange = { taskInput = it },
                        placeholder = { Text("Task title", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        placeholder = { Text("Description (optional)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        modifier = Modifier.height(80.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.entries.forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = {
                                    Text(
                                        priority.label,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2C2825),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskInput.isNotEmpty()) {
                            taskViewModel.addTodo(
                                title = taskInput,
                                description = taskDescription,
                                priority = selectedPriority
                            )
                            taskInput = ""
                            taskDescription = ""
                            selectedPriority = Priority.MEDIUM
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2825),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        taskInput = ""
                        taskDescription = ""
                        selectedPriority = Priority.MEDIUM
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun TaskCard(
    task: TodoTask,
    isCompleting: Boolean = false,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val effectiveCompleted = task.isCompleted || isCompleting
    val alpha by animateFloatAsState(
        targetValue = if (effectiveCompleted) 0.3f else 1f,
        animationSpec = tween(500), label = "taskAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (effectiveCompleted) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "taskScale"
    )

    val priorityLabel = when (task.priority) {
        Priority.HIGH   -> "HIGH"
        Priority.MEDIUM -> "MED"
        Priority.LOW    -> "LOW"
    }

    val isOverdue = task.dueDate != null && task.dueDate < java.time.LocalDate.now()
    val accentColor = when {
        task.isCompleted -> Color(0xFF9A9892)
        isOverdue        -> Color(0xFFC04040)
        task.priority == Priority.HIGH   -> Color(0xFFD94F2A)
        task.priority == Priority.MEDIUM -> Color(0xFFF0920A)
        else             -> Color(0xFF4A9E72)
    }

    val cardBg = when {
        task.isCompleted -> Color(0xFFF6F3EE)
        isOverdue        -> Color(0xFFFFF9F9)
        else             -> Color(0xFFF9FFFC)
    }
    val borderColor = when {
        task.isCompleted -> Color(0xFFE0DAD2)
        isOverdue        -> Color(0xFFEDCFCF)
        else             -> Color(0xFFCCE8D9)
    }

    val hasTime = task.dueDate != null
    var dayStr = ""
    var monthStr = ""
    var fullDateStr = ""
    if (hasTime) {
        val dt = task.dueDate!!
        dayStr = String.format("%02d", dt.dayOfMonth)
        monthStr = dt.format(java.time.format.DateTimeFormatter.ofPattern("MMM")).uppercase()
        fullDateStr = dt.format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            .graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg, RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ── LEFT HERO BLOCK (Time or Priority) ───────────────────────────
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.width(80.dp) // Fixed width to keep separator aligned
            ) {
                // Top sub-label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasTime) {
                        Text(
                            text = monthStr,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = accentColor.copy(alpha = 0.6f)
                        )
                        Text(text = "·", fontSize = 9.sp, color = accentColor.copy(alpha = 0.3f))
                        Text(
                            text = priorityLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = accentColor.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (hasTime) {
                    Text(
                        text = dayStr,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = if (effectiveCompleted) Color(0xFFB0ABA5) else accentColor,
                        lineHeight = 42.sp
                    )
                    // Date
                    Text(
                        text = fullDateStr,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverdue && !task.isCompleted) Color(0xFFC04040) else accentColor.copy(alpha = 0.5f),
                        letterSpacing = 0.2.sp
                    )
                } else {
                    Text(
                        text = priorityLabel,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        color = if (effectiveCompleted) Color(0xFFB0ABA5) else accentColor,
                        lineHeight = 38.sp
                    )
                    Text(
                        text = "PRIORITY",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = accentColor.copy(alpha = 0.5f),
                        letterSpacing = 0.2.sp
                    )
                }
                
                if (isOverdue && !task.isCompleted) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "OVERDUE",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFC04040).copy(alpha = 0.7f)
                    )
                }
            }

            // Thin separator
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .background(borderColor)
            )

            // ── RIGHT BLOCK (Title + Checkbox) ───────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = task.title,
                            fontSize = 15.5.sp,
                            fontWeight = if (effectiveCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = if (effectiveCompleted) Color(0xFFADA9A4) else Color(0xFF1A1714),
                            textDecoration = if (effectiveCompleted) TextDecoration.LineThrough else null,
                            lineHeight = 22.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (task.description.isNotEmpty()) {
                            Text(
                                text = task.description,
                                fontSize = 12.5.sp,
                                color = Color(0xFFC2BAB2),
                                maxLines = 1,
                                lineHeight = 17.sp,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Controls (Checkbox + Menu)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = 8.dp, y = (-8).dp) // Shifted up/right slightly to sit in corner
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) { showMenu = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = "More",
                                    tint = if (effectiveCompleted) Color(0xFFC0BCB6) else Color(0xFFCEC8C1),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color(0xFFFFFEFC))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3A3630)) },
                                    onClick = { onDelete(); showMenu = false },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD94F2A), modifier = Modifier.size(28.dp))
                                    }
                                )
                            }
                        }
                        
                        // Animated Circular Checkbox
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    color = if (effectiveCompleted) accentColor.copy(alpha = 0.92f) else Color.Transparent,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (effectiveCompleted) Color.Transparent else accentColor.copy(alpha = 0.5f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) { onToggleComplete() },
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = effectiveCompleted,
                                enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

