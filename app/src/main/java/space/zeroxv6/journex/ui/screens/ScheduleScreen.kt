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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.ScheduleItem
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.ui.utils.HapticFeedback
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val scheduleItems by taskViewModel.allSchedules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var selectedStartHour by remember { mutableStateOf(9) }
    var selectedStartMinute by remember { mutableStateOf(0) }
    var hasEndTime by remember { mutableStateOf(false) }
    var selectedEndHour by remember { mutableStateOf(10) }
    var selectedEndMinute by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(DayOfWeek.entries.toSet()) }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Daily Schedule",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                Icon(Icons.Filled.Add, contentDescription = "Add Schedule")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Schedule")
            }
        }
    ) { padding ->
        if (scheduleItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Schedules",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create a daily routine for your activities",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Your daily routine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(scheduleItems.sortedBy { it.time }) { item ->
                    ScheduleCard(
                        item = item,
                        onToggle = {
                            taskViewModel.toggleScheduleEnabled(item)
                        },
                        onDelete = {
                            taskViewModel.deleteSchedule(item)
                        }
                    )
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
                    "New Schedule",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Schedule name") },
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
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.labelMedium
                        )
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
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedStartHour = (selectedStartHour - 1 + 24) % 24 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease hour")
                                    }
                                    Text(
                                        text = String.format("%02d", selectedStartHour),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    IconButton(
                                        onClick = { selectedStartHour = (selectedStartHour + 1) % 24 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase hour")
                                    }
                                }
                            }
                            Text(":", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedStartMinute = (selectedStartMinute - 15 + 60) % 60 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease minute")
                                    }
                                    Text(
                                        text = String.format("%02d", selectedStartMinute),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    IconButton(
                                        onClick = { selectedStartMinute = (selectedStartMinute + 15) % 60 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase minute")
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add End Time",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Switch(
                            checked = hasEndTime,
                            onCheckedChange = { hasEndTime = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    if (hasEndTime) {
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
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedEndHour = (selectedEndHour - 1 + 24) % 24 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease hour")
                                    }
                                    Text(
                                        text = String.format("%02d", selectedEndHour),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    IconButton(
                                        onClick = { selectedEndHour = (selectedEndHour + 1) % 24 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase hour")
                                    }
                                }
                            }
                            Text(":", style = MaterialTheme.typography.titleLarge)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { selectedEndMinute = (selectedEndMinute - 15 + 60) % 60 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Decrease minute")
                                    }
                                    Text(
                                        text = String.format("%02d", selectedEndMinute),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    IconButton(
                                        onClick = { selectedEndMinute = (selectedEndMinute + 15) % 60 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Increase minute")
                                    }
                                }
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Days",
                                style = MaterialTheme.typography.labelMedium
                            )
                            TextButton(
                                onClick = {
                                    selectedDays = if (selectedDays.size == 7) {
                                        emptySet()
                                    } else {
                                        DayOfWeek.entries.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedDays.size == 7) "Deselect All" else "Select All",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                                listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                                listOf(DayOfWeek.SUNDAY)
                            ).forEach { dayRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    dayRow.forEach { day ->
                                        FilterChip(
                                            selected = selectedDays.contains(day),
                                            onClick = {
                                                selectedDays = if (selectedDays.contains(day)) {
                                                    selectedDays - day
                                                } else {
                                                    selectedDays + day
                                                }
                                            },
                                            label = { Text(day.name.take(3)) },
                                            modifier = Modifier.weight(1f),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                                selectedLabelColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                    repeat(3 - dayRow.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotEmpty() && selectedDays.isNotEmpty()) {
                            taskViewModel.addSchedule(
                                title = titleInput,
                                description = descriptionInput,
                                time = LocalTime.of(selectedStartHour, selectedStartMinute),
                                endTime = if (hasEndTime) LocalTime.of(selectedEndHour, selectedEndMinute) else null,
                                daysOfWeek = selectedDays
                            )
                            titleInput = ""
                            descriptionInput = ""
                            selectedStartHour = 9
                            selectedStartMinute = 0
                            hasEndTime = false
                            selectedEndHour = 10
                            selectedEndMinute = 0
                            selectedDays = DayOfWeek.entries.toSet()
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
                        selectedStartHour = 9
                        selectedStartMinute = 0
                        hasEndTime = false
                        selectedEndHour = 10
                        selectedEndMinute = 0
                        selectedDays = DayOfWeek.entries.toSet()
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
fun ScheduleCard(
    item: ScheduleItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (item.isEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "scheduleBg"
    )
    val alpha by animateFloatAsState(
        targetValue = if (item.isEnabled) 1f else 0.7f,
        animationSpec = tween(300),
        label = "scheduleAlpha"
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
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    if (item.endTime != null) {
                        Text(
                            text = "to",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = item.endTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item.daysOfWeek.sortedBy { it.value }.take(7).forEach { day ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.isEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.outline
                        ) {
                            Text(
                                text = day.name.take(3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
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
}
