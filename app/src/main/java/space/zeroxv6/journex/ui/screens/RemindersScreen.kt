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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.Reminder
import space.zeroxv6.journex.model.ReminderCategory
import space.zeroxv6.journex.model.RepeatType
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.ui.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {}
) {
    val reminders by taskViewModel.activeReminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ReminderCategory.GENERAL) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedRepeat by remember { mutableStateOf(RepeatType.NONE) }
    var showUpcoming by remember { mutableStateOf(true) }
    val now = LocalDateTime.now()
    val upcomingReminders = reminders.filter { !it.isCompleted && it.dateTime.isAfter(now) }.sortedBy { it.dateTime }
    val pastReminders = reminders.filter { !it.isCompleted && it.dateTime.isBefore(now) }.sortedByDescending { it.dateTime }
    val completedReminders = reminders.filter { it.isCompleted }.sortedByDescending { it.dateTime }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Reminders",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showUpcoming = !showUpcoming }) {
                            Icon(
                                if (showUpcoming) Icons.Filled.Upcoming else Icons.Filled.History,
                                contentDescription = "Toggle view"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        },
        floatingActionButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
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
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
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
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Reminder")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Reminders",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add reminders for important events",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                if (showUpcoming) {
                    if (upcomingReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Upcoming (${upcomingReminders.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(upcomingReminders) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onToggleComplete = {
                                    taskViewModel.toggleReminderCompletion(reminder)
                                },
                                onDelete = {
                                    taskViewModel.deleteReminder(reminder)
                                }
                            )
                        }
                    }
                    if (pastReminders.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Overdue (${pastReminders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        items(pastReminders) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                isOverdue = true,
                                onToggleComplete = {
                                    taskViewModel.toggleReminderCompletion(reminder)
                                },
                                onDelete = {
                                    taskViewModel.deleteReminder(reminder)
                                }
                            )
                        }
                    }
                } else {
                    if (completedReminders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed (${completedReminders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        items(completedReminders) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onToggleComplete = {
                                    taskViewModel.toggleReminderCompletion(reminder)
                                },
                                onDelete = {
                                    taskViewModel.deleteReminder(reminder)
                                }
                            )
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No completed reminders",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { 
                Text(
                    "New Reminder",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Title") },
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
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        placeholder = { Text("Description (optional)") },
                        modifier = Modifier.height(80.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderCategory.entries.take(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.label, maxLines = 1) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderCategory.entries.drop(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.label, maxLines = 1) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Time", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedHour = (selectedHour - 1 + 24) % 24 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text(String.format("%02d", selectedHour), style = MaterialTheme.typography.titleMedium)
                                    IconButton(
                                        onClick = { selectedHour = (selectedHour + 1) % 24 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Text(":")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedMinute = (selectedMinute - 15 + 60) % 60 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text(String.format("%02d", selectedMinute), style = MaterialTheme.typography.titleMedium)
                                    IconButton(
                                        onClick = { selectedMinute = (selectedMinute + 15) % 60 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Repeat", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RepeatType.entries.take(2).forEach { repeat ->
                                FilterChip(
                                    selected = selectedRepeat == repeat,
                                    onClick = { selectedRepeat = repeat },
                                    label = { Text(repeat.label) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RepeatType.entries.drop(2).forEach { repeat ->
                                FilterChip(
                                    selected = selectedRepeat == repeat,
                                    onClick = { selectedRepeat = repeat },
                                    label = { Text(repeat.label) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotEmpty()) {
                            taskViewModel.addReminder(
                                title = titleInput,
                                description = descriptionInput,
                                dateTime = LocalDateTime.of(selectedDate, LocalTime.of(selectedHour, selectedMinute)),
                                category = selectedCategory,
                                repeatType = selectedRepeat
                            )
                            titleInput = ""
                            descriptionInput = ""
                            selectedCategory = ReminderCategory.GENERAL
                            selectedHour = 9
                            selectedMinute = 0
                            selectedRepeat = RepeatType.NONE
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        titleInput = ""
                        descriptionInput = ""
                        selectedCategory = ReminderCategory.GENERAL
                        selectedHour = 9
                        selectedMinute = 0
                        selectedRepeat = RepeatType.NONE
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun ReminderCard(
    reminder: Reminder,
    isOverdue: Boolean = false,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val alpha by animateFloatAsState(
        targetValue = if (reminder.isCompleted) 0.6f else 1f,
        animationSpec = tween(300),
        label = "reminderAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (reminder.isCompleted) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "reminderScale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            reminder.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
            isOverdue -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "reminderBg"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = { 
                        onToggleComplete()
                        if (!reminder.isCompleted) {
                            HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.description.isNotEmpty()) {
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = reminder.category.label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = reminder.category.color
                            )
                        }
                        Text(
                            text = reminder.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a")),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        if (reminder.repeatType != RepeatType.NONE) {
                            Icon(
                                Icons.Outlined.Repeat,
                                contentDescription = "Repeating",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    )
                }
            }
        }
    }
}
