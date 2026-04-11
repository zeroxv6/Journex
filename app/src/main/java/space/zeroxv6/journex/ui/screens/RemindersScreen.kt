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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.Reminder
import space.zeroxv6.journex.model.ReminderCategory
import space.zeroxv6.journex.model.RepeatType
import space.zeroxv6.journex.ui.theme.FeatureColors
import space.zeroxv6.journex.ui.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    viewModel: space.zeroxv6.journex.viewmodel.JournalViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {}
) {
    val reminders by taskViewModel.activeReminders.collectAsState()
    val use24Hour = viewModel.use24HourFormat
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
                        OutlinedButton(
                            onClick = { showUpcoming = !showUpcoming },
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha=0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (!showUpcoming) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(if (!showUpcoming) "View Active" else "View Completed", style = MaterialTheme.typography.labelLarge)
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
                containerColor = FeatureColors.RemindersAccentDark,
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
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Reminder", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    ) { padding ->
        val coroutineScope = rememberCoroutineScope()
        var completingReminders by remember { mutableStateOf(setOf<String>()) }
        val context = androidx.compose.ui.platform.LocalContext.current
        
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
                        items(upcomingReminders, key = { it.id }) { reminder ->
                            AnimatedVisibility(
                                visible = !completingReminders.contains(reminder.id),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(animationSpec = tween(500, delayMillis = 400))
                            ) {
                                ReminderCard(
                                    reminder = reminder,
                                    isCompleting = completingReminders.contains(reminder.id),
                                    onToggleComplete = {
                                        if (!reminder.isCompleted && !completingReminders.contains(reminder.id)) {
                                            completingReminders = completingReminders + reminder.id
                                            HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(800)
                                                taskViewModel.toggleReminderCompletion(reminder)
                                                completingReminders = completingReminders - reminder.id
                                            }
                                        } else {
                                            taskViewModel.toggleReminderCompletion(reminder)
                                        }
                                    },
                                    onDelete = {
                                        taskViewModel.deleteReminder(reminder)
                                    }
                                )
                            }
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
                        items(pastReminders, key = { it.id }) { reminder ->
                            AnimatedVisibility(
                                visible = !completingReminders.contains(reminder.id),
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut(animationSpec = tween(500)) + shrinkVertically(animationSpec = tween(500, delayMillis = 400))
                            ) {
                                ReminderCard(
                                    reminder = reminder,
                                    isOverdue = true,
                                    isCompleting = completingReminders.contains(reminder.id),
                                    onToggleComplete = {
                                        if (!reminder.isCompleted && !completingReminders.contains(reminder.id)) {
                                            completingReminders = completingReminders + reminder.id
                                            HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(800)
                                                taskViewModel.toggleReminderCompletion(reminder)
                                                completingReminders = completingReminders - reminder.id
                                            }
                                        } else {
                                            taskViewModel.toggleReminderCompletion(reminder)
                                        }
                                    },
                                    onDelete = {
                                        taskViewModel.deleteReminder(reminder)
                                    }
                                )
                            }
                        }
                    }
                    // Removed inline OutlinedButton
                } else {
                    // Removed inline OutlinedButton
                    if (completedReminders.isNotEmpty()) {
                        items(completedReminders, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                isCompleting = false,
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
                        placeholder = { Text("Title", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                                    label = {
                                        Text(
                                            category.label, maxLines = 1,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderCategory.entries.drop(3).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            category.label, maxLines = 1,
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
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Time", style = MaterialTheme.typography.labelMedium)
                        space.zeroxv6.journex.ui.components.ImprovedTimePicker(
                            hour = selectedHour,
                            minute = selectedMinute,
                            use24Hour = use24Hour,
                            onHourChange = { selectedHour = it },
                            onMinuteChange = { selectedMinute = it }
                        )
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
                                    label = { Text(repeat.label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2C2825),
                                        selectedLabelColor = Color.White
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
                                    label = { Text(repeat.label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2C2825),
                                        selectedLabelColor = Color.White
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
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
    isCompleting: Boolean = false,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val effectiveCompleted = reminder.isCompleted || isCompleting
    val alpha by animateFloatAsState(
        targetValue = if (effectiveCompleted) 0.52f else 1f,
        animationSpec = tween(380), label = "reminderAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (effectiveCompleted) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "reminderScale"
    )

    val accentColor = when {
        effectiveCompleted -> Color(0xFF9A9892)
        isOverdue          -> Color(0xFFC04040)
        else               -> Color(0xFF3D7A5C)
    }
    // Single ambient tint — the whole card breathes the status color
    val cardBg = when {
        effectiveCompleted -> Color(0xFFF6F3EE)
        isOverdue          -> Color(0xFFFFF9F9)
        else               -> Color(0xFFF9FFFC)
    }
    val borderColor = when {
        effectiveCompleted -> Color(0xFFE0DAD2)
        isOverdue          -> Color(0xFFEDCFCF)
        else               -> Color(0xFFCCE8D9)
    }

    // Parse time parts for the editorial split display
    val hourStr   = reminder.dateTime.format(DateTimeFormatter.ofPattern("h"))
    val minStr    = reminder.dateTime.format(DateTimeFormatter.ofPattern("mm"))
    val amPmStr   = reminder.dateTime.format(DateTimeFormatter.ofPattern("a"))
    val dateStr   = reminder.dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d"))

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
            // ── TIME block — the editorial hero ───────────────────────────
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Category + repeat superscript
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = amPmStr,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = accentColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "·",
                        fontSize = 9.sp,
                        color = accentColor.copy(alpha = 0.3f)
                    )
                    Text(
                        text = reminder.category.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = accentColor.copy(alpha = 0.6f)
                    )
                    if (reminder.repeatType != RepeatType.NONE) {
                        Icon(
                            Icons.Outlined.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(9.dp),
                            tint = accentColor.copy(alpha = 0.5f)
                        )
                    }
                }
                // Giant H:MM display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = hourStr,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = if (effectiveCompleted) Color(0xFFB0ABA5) else accentColor,
                        lineHeight = 38.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = ":",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Light,
                        color = accentColor.copy(alpha = 0.35f),
                        modifier = Modifier.offset(y = (-4).dp),
                        lineHeight = 30.sp
                    )
                    Text(
                        text = minStr,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = if (effectiveCompleted) Color(0xFFB0ABA5) else accentColor.copy(alpha = 0.75f),
                        lineHeight = 38.sp,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                // Date
                Text(
                    text = dateStr,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverdue && !reminder.isCompleted) Color(0xFFC04040) else accentColor.copy(alpha = 0.5f),
                    letterSpacing = 0.2.sp
                )
                if (isOverdue && !reminder.isCompleted) {
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

            // ── Title + description ───────────────────────────────────────
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = if (effectiveCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = if (effectiveCompleted) Color(0xFFA8A4A0) else Color(0xFF1A1714),
                    textDecoration = if (effectiveCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    lineHeight = 21.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 12.sp,
                        color = Color(0xFFC2BAB2),
                        maxLines = 1,
                        lineHeight = 16.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // ── Right column: toggle + menu ───────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circle toggle button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            color = if (effectiveCompleted) accentColor.copy(alpha = 0.88f) else Color.Transparent,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (effectiveCompleted) Color.Transparent else accentColor.copy(alpha = 0.4f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = onToggleComplete
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = effectiveCompleted,
                        enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Menu icon
                Box {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            ) { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = Color(0xFFCEC8C0), modifier = Modifier.size(28.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFFFFFDF9))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3A3630)) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFC04040), modifier = Modifier.size(28.dp)) }
                        )
                    }
                }
            }
        }
    }
}

