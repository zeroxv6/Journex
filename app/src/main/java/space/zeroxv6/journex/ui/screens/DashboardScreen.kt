package space.zeroxv6.journex.ui.screens
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.Reminder
import space.zeroxv6.journex.model.ScheduleItem
import space.zeroxv6.journex.model.TodoTask
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.ui.utils.HapticFeedback
import space.zeroxv6.journex.viewmodel.JournalViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: JournalViewModel,
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    onNavigateToJournal: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToQuickNotes: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPrompts: () -> Unit,
    onNavigateToPromptMoment: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTaskManagement: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val stats = viewModel.getStats()
    val todayEntries = viewModel.entries.filter {
        it.createdAt.toLocalDate() == LocalDate.now() && !it.isArchived
    }
    val incompleteTodos by taskViewModel.incompleteTodos.collectAsState()
    val enabledSchedules by taskViewModel.enabledSchedules.collectAsState()
    val upcomingReminders by taskViewModel.upcomingReminders.collectAsState()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationDrawerContent(
                    onNavigateToJournal = {
                        scope.launch { drawerState.close() }
                        onNavigateToJournal()
                    },
                    onNavigateToStats = {
                        scope.launch { drawerState.close() }
                        onNavigateToStats()
                    },
                    onNavigateToTodo = {
                        scope.launch { drawerState.close() }
                        onNavigateToTodo()
                    },
                    onNavigateToSchedule = {
                        scope.launch { drawerState.close() }
                        onNavigateToSchedule()
                    },
                    onNavigateToReminders = {
                        scope.launch { drawerState.close() }
                        onNavigateToReminders()
                    },
                    onNavigateToQuickNotes = {
                        scope.launch { drawerState.close() }
                        onNavigateToQuickNotes()
                    },
                    onNavigateToNotes = {
                        scope.launch { drawerState.close() }
                        onNavigateToNotes()
                    },
                    onNavigateToPrompts = {
                        scope.launch { drawerState.close() }
                        onNavigateToPrompts()
                    },
                    onNavigateToTemplates = {
                        scope.launch { drawerState.close() }
                        onNavigateToTemplates()
                    },
                    onNavigateToSettings = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    }
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Dashboard",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                LocalDate.now()
                                    .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd")),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    StatsOverviewCard(
                        stats = stats,
                        onClick = onNavigateToStats
                    )
                }
                item {
                    TodayJournalCard(
                        hasEntry = todayEntries.isNotEmpty(),
                        entryCount = todayEntries.size,
                        onClick = onNavigateToJournal
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TasksPreviewCard(
                            tasks = incompleteTodos.take(3),
                            modifier = Modifier.weight(1f),
                            onTaskClick = { task ->
                                taskViewModel.toggleTodoCompletion(task)
                            },
                            onCardClick = onNavigateToTodo
                        )
                        QuickActionsCard(
                            modifier = Modifier.weight(1f),
                            onQuickNoteClick = onNavigateToQuickNotes,
                            onPromptClick = onNavigateToPromptMoment,
                            onTaskManagementClick = onNavigateToTaskManagement
                        )
                    }
                }
                item {
                    NotesPreviewCard(
                        onClick = onNavigateToNotes
                    )
                }
                item {
                    RemindersPreviewCard(
                        reminders = upcomingReminders.take(2),
                        onClick = onNavigateToReminders
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SchedulePreviewCard(
                            schedules = enabledSchedules.take(2),
                            modifier = Modifier.weight(1.2f),
                            onClick = onNavigateToSchedule
                        )
                        TemplatesCard(
                            modifier = Modifier.weight(0.8f),
                            onClick = onNavigateToTemplates
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
@Composable
fun StatsOverviewCard(
    stats: space.zeroxv6.journex.model.JournalStats,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
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
                    text = "Your Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${stats.totalEntries}",
                    label = "Total Entries",
                    icon = Icons.Outlined.Article,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = "${stats.currentStreak}",
                    label = "Day Streak",
                    icon = Icons.Outlined.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    value = "${stats.totalWords}",
                    label = "Words Written",
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    }
}
@Composable
fun TodayJournalCard(
    hasEntry: Boolean,
    entryCount: Int,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (hasEntry) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(500),
        label = "cardBackground"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Journal",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasEntry) "$entryCount ${if (entryCount == 1) "entry" else "entries"} written" else "No entries yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    if (hasEntry) Icons.Filled.CheckCircle else Icons.Outlined.EditNote,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = if (hasEntry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    if (hasEntry) "View Entries" else "Start Writing",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
@Composable
fun TasksPreviewCard(
    tasks: List<TodoTask>,
    modifier: Modifier = Modifier,
    onTaskClick: (TodoTask) -> Unit,
    onCardClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = modifier.height(240.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onCardClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleLarge
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tasks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onTaskClick(task)
                                    if (!task.isCompleted) {
                                        HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else task.priority.color
                            )
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun QuickActionsCard(
    modifier: Modifier = Modifier,
    onQuickNoteClick: () -> Unit,
    onPromptClick: () -> Unit,
    onTaskManagementClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onQuickNoteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Outlined.StickyNote2,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Quick Note", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onPromptClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Get Prompt", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onTaskManagementClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Outlined.Dashboard,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Task Board", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
@Composable
fun NotesPreviewCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Note,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create and organize your notes with rich features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Go to Notes",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
@Composable
fun RemindersPreviewCard(
    reminders: List<Reminder>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
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
                Text(
                    text = "Upcoming Reminders",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (reminders.isEmpty()) {
                Text(
                    text = "No upcoming reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reminders.forEach { reminder ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Icon(
                                        Icons.Outlined.NotificationsNone,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp),
                                        tint = reminder.category.color
                                    )
                                }
                                Column {
                                    Text(
                                        text = reminder.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = reminder.dateTime.format(
                                            DateTimeFormatter.ofPattern(
                                                "MMM dd, h:mm a"
                                            )
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SchedulePreviewCard(
    schedules: List<ScheduleItem>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val now = java.time.LocalTime.now()
    val upcomingSchedule = schedules.filter { it.time.isAfter(now) }.minByOrNull { it.time }
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Schedule",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (upcomingSchedule != null) {
                Column {
                    Text(
                        text = "Next: ${upcomingSchedule.title}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = upcomingSchedule.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                Text(
                    text = "No upcoming schedule",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
@Composable
fun TemplatesCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Column {
                Text(
                    text = "Templates",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "10 templates",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
@Composable
fun NavigationDrawerContent(
    onNavigateToJournal: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToQuickNotes: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToPrompts: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Journex",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Your personal space",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        DrawerNavigationItem(
            icon = Icons.Outlined.Article,
            label = "Journal",
            onClick = onNavigateToJournal
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.BarChart,
            label = "Statistics",
            onClick = onNavigateToStats
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.CheckCircleOutline,
            label = "Tasks",
            onClick = onNavigateToTodo
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.Schedule,
            label = "Schedule",
            onClick = onNavigateToSchedule
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.NotificationsNone,
            label = "Reminders",
            onClick = onNavigateToReminders
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.StickyNote2,
            label = "Quick Notes",
            onClick = onNavigateToQuickNotes
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.Note,
            label = "Notes",
            onClick = onNavigateToNotes
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.Lightbulb,
            label = "Prompts",
            onClick = onNavigateToPrompts
        )
        DrawerNavigationItem(
            icon = Icons.Outlined.Description,
            label = "Templates",
            onClick = onNavigateToTemplates
        )
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        DrawerNavigationItem(
            icon = Icons.Outlined.Settings,
            label = "Settings",
            onClick = onNavigateToSettings
        )
    }
}
@Composable
fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        animationSpec = tween(150),
        label = "drawerItemBg"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .bounceClick(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}