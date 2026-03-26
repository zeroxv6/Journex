package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
@Composable
fun DesktopDashboardScreen(
    journalViewModel: JournalViewModel,
    taskViewModel: TaskViewModel,
    onNavigate: (String) -> Unit
) {
    val stats by journalViewModel.stats.collectAsState()
    val todayEntries by journalViewModel.todayEntries.collectAsState()
    val allTasks by taskViewModel.allTasks.collectAsState()
    val incompleteTasks by taskViewModel.incompleteTasks.collectAsState()
    val upcomingReminders by taskViewModel.upcomingReminders.collectAsState()
    val enabledSchedules by taskViewModel.enabledSchedules.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dashboard", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.current.textSecondary
                )
            }
            IconButton(onClick = { onNavigate("settings") }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Settings, null, tint = AppColors.current.textSecondary, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        if (enabledSchedules.isNotEmpty()) {
            ScheduleHorizontalRow(
                schedules = enabledSchedules,
                onClick = { onNavigate("schedule") }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        TodayJournalCard(
            hasEntry = todayEntries.isNotEmpty(),
            entryCount = todayEntries.size,
            onClick = { onNavigate("journal") }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TasksPreviewCard(
                allTasks = allTasks,
                modifier = Modifier.weight(1f),
                onTaskClick = { taskViewModel.toggleTaskCompletion(it) },
                onCardClick = { onNavigate("tasks") }
            )
            QuickActionsCard(
                modifier = Modifier.weight(1f),
                onQuickNoteClick = { onNavigate("notes") },
                onPromptClick = { onNavigate("prompts") },
                onNewEntryClick = { onNavigate("editor/new") }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RemindersPreviewCard(
                reminders = upcomingReminders.take(4),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("reminders") }
            )
            SchedulePreviewCard(
                schedules = enabledSchedules.take(4),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("schedule") }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        TemplatesCard(onClick = { onNavigate("templates") })
    }
}
@Composable
private fun TodayJournalCard(hasEntry: Boolean, entryCount: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (hasEntry) AppColors.current.textSecondary else AppColors.current.border
                ) {
                    Icon(
                        if (hasEntry) Icons.Filled.CheckCircle else Icons.Outlined.EditNote,
                        null,
                        modifier = Modifier.padding(12.dp).size(24.dp),
                        tint = AppColors.current.background
                    )
                }
                Column {
                    Text("Today's Journal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        if (hasEntry) "$entryCount ${if (entryCount == 1) "entry" else "entries"} written" else "No entries yet - start writing!",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.current.textTertiary
                    )
                }
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(if (hasEntry) "View" else "Write", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
@Composable
private fun TasksPreviewCard(
    allTasks: List<TodoTask>,
    modifier: Modifier = Modifier,
    onTaskClick: (TodoTask) -> Unit,
    onCardClick: () -> Unit
) {
    val incompleteTasks = allTasks.filter { !it.isCompleted }.sortedByDescending { it.priority.ordinal }
    val completedTasks = allTasks.filter { it.isCompleted }.sortedByDescending { it.createdAt }
    Surface(
        modifier = modifier.height(320.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.background,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tasks", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp))
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.textDisabled)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (allTasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.TaskAlt, null, modifier = Modifier.size(48.dp), tint = AppColors.current.border)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All caught up!", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textDisabled)
                    Text("No pending tasks", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.borderFocused)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    incompleteTasks.forEach { task ->
                        TaskItem(task = task, onClick = { onTaskClick(task) })
                    }
                    if (completedTasks.isNotEmpty()) {
                        if (incompleteTasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = AppColors.current.border)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        completedTasks.forEach { task ->
                            TaskItem(task = task, onClick = { onTaskClick(task) })
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun TaskItem(task: TodoTask, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = AppColors.current.cardBackground
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                null,
                modifier = Modifier.size(24.dp),
                tint = if (task.isCompleted) AppColors.current.textSecondary else AppColors.current.textPrimary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) AppColors.current.textDisabled else AppColors.current.textPrimary
                )
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        task.dueDate!!.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.current.textDisabled
                    )
                }
            }
            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.surfaceTertiary) {
                Text(
                    task.priority.label.first().toString(),
                    modifier = Modifier.padding(8.dp, 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.current.textTertiary
                )
            }
        }
    }
}
@Composable
private fun QuickActionsCard(
    modifier: Modifier = Modifier,
    onQuickNoteClick: () -> Unit,
    onPromptClick: () -> Unit,
    onNewEntryClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(320.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.background,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onQuickNoteClick),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.current.cardBackground
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = AppColors.current.border) {
                            Icon(Icons.Outlined.StickyNote2, null, modifier = Modifier.padding(10.dp).size(22.dp), tint = AppColors.current.textSecondary)
                        }
                        Column {
                            Text("Quick Note", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium))
                            Text("Capture a thought", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onPromptClick),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.current.cardBackground
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = AppColors.current.border) {
                            Icon(Icons.Outlined.Lightbulb, null, modifier = Modifier.padding(10.dp).size(22.dp), tint = AppColors.current.textSecondary)
                        }
                        Column {
                            Text("Get Prompt", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium))
                            Text("Find inspiration", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary)
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun RemindersPreviewCard(reminders: List<Reminder>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(280.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.background,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reminders", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp))
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.textDisabled)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (reminders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.NotificationsOff, null, modifier = Modifier.size(40.dp), tint = AppColors.current.border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No reminders", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textDisabled)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reminders.forEach { reminder ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.current.cardBackground
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(AppColors.current.textSecondary))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reminder.title, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        reminder.dateTime.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.current.textDisabled
                                    )
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.surfaceTertiary) {
                                    Text(
                                        reminder.category.label,
                                        modifier = Modifier.padding(8.dp, 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
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
}
@Composable
private fun SchedulePreviewCard(schedules: List<ScheduleItem>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val now = LocalTime.now()
    val sortedSchedules = schedules.sortedBy { it.time }
    Surface(
        modifier = modifier.height(280.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.background,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.textDisabled)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (sortedSchedules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.EventBusy, null, modifier = Modifier.size(40.dp), tint = AppColors.current.border)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No schedules", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textDisabled)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    sortedSchedules.forEachIndexed { index, schedule ->
                        val isPast = schedule.time.isBefore(now)
                        val isNext = !isPast && (index == 0 || sortedSchedules.take(index).all { it.time.isBefore(now) })
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isNext) 12.dp else 10.dp)
                                        .clip(CircleShape)
                                        .background(if (isNext) AppColors.current.textPrimary else if (isPast) AppColors.current.border else AppColors.current.textTertiary)
                                )
                                if (index < sortedSchedules.size - 1) {
                                    Box(modifier = Modifier.width(2.dp).height(40.dp).background(AppColors.current.border))
                                }
                            }
                            Column(modifier = Modifier.weight(1f).padding(bottom = if (index < sortedSchedules.size - 1) 8.dp else 0.dp)) {
                                Text(
                                    schedule.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isPast) AppColors.current.textDisabled else AppColors.current.textPrimary
                                )
                                Text(
                                    schedule.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPast) AppColors.current.textDisabled else AppColors.current.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun ScheduleHorizontalRow(schedules: List<ScheduleItem>, onClick: () -> Unit) {
    val now = LocalTime.now()
    val sortedSchedules = schedules.sortedBy { it.time }
    val ongoing = sortedSchedules.filter { schedule ->
        val endTime = schedule.time.plusHours(1)
        now.isAfter(schedule.time) && now.isBefore(endTime)
    }
    val upcoming = sortedSchedules.filter { it.time.isAfter(now) }.take(3)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, AppColors.current.border)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Schedule, "Schedule", tint = AppColors.current.textSecondary, modifier = Modifier.size(24.dp))
            if (ongoing.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ongoing", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ongoing.forEach { schedule ->
                            Surface(shape = RoundedCornerShape(8.dp), color = AppColors.current.textPrimary) {
                                Row(modifier = Modifier.padding(12.dp, 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AppColors.current.background))
                                    Text(schedule.title, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.background, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            if (upcoming.isNotEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Upcoming", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        upcoming.forEach { schedule ->
                            Surface(shape = RoundedCornerShape(8.dp), color = AppColors.current.surfaceTertiary) {
                                Row(modifier = Modifier.padding(12.dp, 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(schedule.time.format(DateTimeFormatter.ofPattern("h:mm a")), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                                    Text(schedule.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            if (ongoing.isEmpty() && upcoming.isEmpty()) {
                Text("No schedules for today", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textDisabled, modifier = Modifier.weight(1f))
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.textDisabled)
        }
    }
}
@Composable
private fun TemplatesCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.textPrimary
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(10.dp), color = AppColors.current.background.copy(alpha = 0.15f)) {
                    Icon(Icons.Outlined.Description, null, modifier = Modifier.padding(12.dp).size(24.dp), tint = AppColors.current.background)
                }
                Column {
                    Text("Templates", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.background)
                    Text("10+ templates to get started", style = MaterialTheme.typography.bodySmall, color = AppColors.current.background.copy(alpha = 0.7f))
                }
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.background.copy(alpha = 0.7f))
        }
    }
}
