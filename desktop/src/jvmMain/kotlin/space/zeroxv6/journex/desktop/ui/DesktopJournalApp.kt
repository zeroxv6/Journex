package space.zeroxv6.journex.desktop.ui
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.desktop.ui.screens.*
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.ui.theme.AppTheme
import space.zeroxv6.journex.desktop.ui.theme.JournalingDesktopTheme
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.desktop.viewmodel.NoteViewModel
import space.zeroxv6.journex.shared.data.JsonDataStore
sealed class Screen {
    object Dashboard : Screen()
    object Journal : Screen()
    data class Editor(val entryId: String?, val prefillTitle: String? = null, val prefillContent: String? = null) : Screen()
    object Tasks : Screen()
    object Schedule : Screen()
    object Reminders : Screen()
    object Stats : Screen()
    object Prompts : Screen()
    data class Notes(val autoOpenDialog: Boolean = false) : Screen()
    object Templates : Screen()
    object Settings : Screen()
    object TaskManagement : Screen()
    object Todo : Screen()
    object QuickNotes : Screen()
    object PromptOfMoment : Screen()
    object Home : Screen()
    object FullNotes : Screen()
    data class NoteEditor(val noteId: String?) : Screen()
}
private const val PROMPT_TITLE = "Writing Prompt"
class NavigationState {
    var currentScreen by mutableStateOf<Screen>(Screen.Dashboard)
    private val backStack = mutableListOf<Screen>()
    fun navigate(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(currentScreen)
            currentScreen = screen
        }
    }
    fun navigateBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
            true
        } else {
            false
        }
    }
    fun navigateToRoot() {
        backStack.clear()
        currentScreen = Screen.Dashboard
    }
}
@Composable
fun DesktopJournalApp(dataStore: JsonDataStore) {
    val navigationState = remember { NavigationState() }
    val journalViewModel = remember { JournalViewModel(dataStore) }
    val taskViewModel = remember { TaskViewModel(dataStore) }
    val projectTaskViewModel = remember { space.zeroxv6.journex.desktop.viewmodel.ProjectTaskViewModel(dataStore) }
    val noteViewModel = remember { NoteViewModel(dataStore) }
    val settings by dataStore.settings.collectAsState()
    val currentTheme = remember(settings.theme) { AppTheme.fromName(settings.theme) }
    var isUnlocked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (settings.journalReminderEnabled && settings.notificationsEnabled) {
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleJournalReminder(
                settings.journalReminderHour,
                settings.journalReminderMinute
            )
        }
        val reminders = dataStore.reminders.value
        reminders.filter { !it.isCompleted }.forEach { reminder ->
            val dateTimeMillis = reminder.dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (dateTimeMillis > System.currentTimeMillis()) {
                space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleReminder(
                    reminder.id,
                    reminder.title,
                    reminder.description,
                    dateTimeMillis
                )
            }
        }
        val schedules = dataStore.schedules.value
        schedules.filter { it.isEnabled }.forEach { schedule ->
            val daysString = schedule.daysOfWeek.joinToString(",") { day ->
                when (day) {
                    java.time.DayOfWeek.MONDAY -> "Mon"
                    java.time.DayOfWeek.TUESDAY -> "Tue"
                    java.time.DayOfWeek.WEDNESDAY -> "Wed"
                    java.time.DayOfWeek.THURSDAY -> "Thu"
                    java.time.DayOfWeek.FRIDAY -> "Fri"
                    java.time.DayOfWeek.SATURDAY -> "Sat"
                    java.time.DayOfWeek.SUNDAY -> "Sun"
                }
            }
            space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler.scheduleRecurringSchedule(
                schedule.id,
                schedule.title,
                schedule.description,
                schedule.time.hour,
                schedule.time.minute,
                daysString
            )
        }
    }
    JournalingDesktopTheme(appTheme = currentTheme) {
    if (settings.pinCode.isNotEmpty() && !isUnlocked) {
        DesktopPinLockScreen(
            dataStore = dataStore,
            onUnlocked = { isUnlocked = true }
        )
    } else {
    Row(modifier = Modifier.fillMaxSize()) {
        DesktopSidebar(
            currentScreen = navigationState.currentScreen,
            onNavigate = { screen -> navigationState.navigate(screen) },
            modifier = Modifier.width(260.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(AppColors.current.background)
        ) {
            AnimatedContent(
                targetState = navigationState.currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                }
            ) { screen ->
                when (screen) {
                    is Screen.Dashboard -> {
                        DesktopDashboardScreen(
                            journalViewModel = journalViewModel,
                            taskViewModel = taskViewModel,
                            onNavigate = { route ->
                                when (route) {
                                    "journal" -> navigationState.navigate(Screen.Journal)
                                    "tasks" -> navigationState.navigate(Screen.Tasks)
                                    "stats" -> navigationState.navigate(Screen.Stats)
                                    "schedule" -> navigationState.navigate(Screen.Schedule)
                                    "reminders" -> navigationState.navigate(Screen.Reminders)
                                    "prompts" -> navigationState.navigate(Screen.Prompts)
                                    "notes" -> navigationState.navigate(Screen.Notes(autoOpenDialog = true))
                                    "templates" -> navigationState.navigate(Screen.Templates)
                                    "editor/new" -> navigationState.navigate(Screen.Editor(null))
                                    "settings" -> navigationState.navigate(Screen.Settings)
                                    "task-management" -> navigationState.navigate(Screen.TaskManagement)
                                    else -> {}
                                }
                            }
                        )
                    }
                    is Screen.Journal -> {
                        DesktopJournalScreen(
                            viewModel = journalViewModel,
                            onNavigateToEditor = { entryId -> navigationState.navigate(Screen.Editor(entryId)) }
                        )
                    }
                    is Screen.Editor -> {
                        DesktopEditorScreen(
                            viewModel = journalViewModel,
                            entryId = screen.entryId,
                            initialPrompt = screen.prefillContent,
                            initialTitle = screen.prefillTitle,
                            onNavigateBack = { 
                                if (!navigationState.navigateBack()) {
                                    navigationState.navigate(Screen.Journal)
                                }
                            }
                        )
                    }
                    is Screen.Tasks -> {
                        DesktopTasksScreen(
                            viewModel = taskViewModel,
                            onNavigate = { route ->
                                when (route) {
                                    "schedule" -> navigationState.navigate(Screen.Schedule)
                                    "reminders" -> navigationState.navigate(Screen.Reminders)
                                    else -> {}
                                }
                            }
                        )
                    }
                    is Screen.Schedule -> DesktopScheduleScreen(viewModel = taskViewModel, dataStore = dataStore)
                    is Screen.Reminders -> DesktopRemindersScreen(viewModel = taskViewModel, dataStore = dataStore)
                    is Screen.Stats -> DesktopStatsScreen(viewModel = journalViewModel)
                    is Screen.Prompts -> {
                        DesktopPromptsScreen(
                            taskViewModel = taskViewModel,
                            onWriteWithPrompt = { prompt ->
                                navigationState.navigate(Screen.Editor(null, prefillTitle = PROMPT_TITLE, prefillContent = "Prompt: $prompt\n\n"))
                            }
                        )
                    }
                    is Screen.Notes -> {
                        DesktopNotesScreen(
                            dataStore = dataStore,
                            autoOpenDialog = screen.autoOpenDialog,
                            onConvertToEntry = { content ->
                                navigationState.navigate(Screen.Editor(null, prefillContent = content))
                            }
                        )
                    }
                    is Screen.Templates -> {
                        DesktopTemplatesScreen(
                            dataStore = dataStore,
                            onUseTemplate = { title, content ->
                                navigationState.navigate(Screen.Editor(null, prefillTitle = title, prefillContent = content))
                            }
                        )
                    }
                    is Screen.Settings -> {
                        DesktopSettingsScreen(
                            dataStore = dataStore,
                            onClearData = { dataStore.clearAllData() }
                        )
                    }
                    is Screen.TaskManagement -> {
                        DesktopTaskManagementScreen(
                            viewModel = projectTaskViewModel
                        )
                    }
                    is Screen.Todo -> {
                        DesktopTodoScreen(
                            viewModel = taskViewModel,
                            onNavigate = { route ->
                                when (route) {
                                    "schedule" -> navigationState.navigate(Screen.Schedule)
                                    "reminders" -> navigationState.navigate(Screen.Reminders)
                                    else -> {}
                                }
                            }
                        )
                    }
                    is Screen.QuickNotes -> {
                        DesktopQuickNotesScreen(
                            dataStore = dataStore,
                            onConvertToEntry = { content ->
                                navigationState.navigate(Screen.Editor(null, prefillContent = content))
                            }
                        )
                    }
                    is Screen.PromptOfMoment -> {
                        DesktopPromptOfMomentScreen(
                            taskViewModel = taskViewModel,
                            onWriteWithPrompt = { prompt ->
                                navigationState.navigate(Screen.Editor(null, prefillTitle = PROMPT_TITLE, prefillContent = "Prompt: $prompt\n\n"))
                            },
                            onNavigateToAllPrompts = { navigationState.navigate(Screen.Prompts) }
                        )
                    }
                    is Screen.Home -> {
                        DesktopHomeScreen(
                            viewModel = journalViewModel,
                            onNavigateToEditor = { entryId -> navigationState.navigate(Screen.Editor(entryId)) },
                            onNavigateToStats = { navigationState.navigate(Screen.Stats) }
                        )
                    }
                    is Screen.FullNotes -> {
                        DesktopFullNotesScreen(
                            viewModel = noteViewModel,
                            onNavigateToEditor = { noteId -> navigationState.navigate(Screen.NoteEditor(noteId)) }
                        )
                    }
                    is Screen.NoteEditor -> {
                        DesktopNoteEditorScreen(
                            viewModel = noteViewModel,
                            noteId = screen.noteId,
                            onNavigateBack = {
                                if (!navigationState.navigateBack()) {
                                    navigationState.navigate(Screen.FullNotes)
                                }
                            }
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
fun DesktopSidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxHeight(), color = AppColors.current.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val logo = remember {
                    try {
                        val iconUrl = object {}.javaClass.getResource("/journex_icon.png")
                        if (iconUrl != null) {
                            val bufferedImage = javax.imageio.ImageIO.read(iconUrl)
                            androidx.compose.ui.graphics.painter.BitmapPainter(
                                org.jetbrains.skia.Image.makeFromEncoded(
                                    java.io.ByteArrayOutputStream().apply {
                                        javax.imageio.ImageIO.write(bufferedImage, "PNG", this)
                                    }.toByteArray()
                                ).toComposeImageBitmap()
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                if (logo != null) {
                    androidx.compose.foundation.Image(
                        painter = logo,
                        contentDescription = "Journex Logo",
                        modifier = Modifier.size(200.dp).clip(RoundedCornerShape(20.dp))
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.current.surface,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "J",
                                style = MaterialTheme.typography.displayLarge,
                                color = AppColors.current.textPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Journex", style = MaterialTheme.typography.headlineMedium, color = AppColors.current.textPrimary)
                Text("Your life, organized", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary)
            }
            HorizontalDivider(color = AppColors.current.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SidebarNavItem(Icons.Outlined.Dashboard, Icons.Filled.Dashboard, "Dashboard", currentScreen is Screen.Dashboard) { onNavigate(Screen.Dashboard) }
                SidebarNavItem(Icons.Outlined.Article, Icons.Filled.Article, "Journal", currentScreen is Screen.Journal || currentScreen is Screen.Editor) { onNavigate(Screen.Journal) }
                SidebarNavItem(Icons.Outlined.NoteAlt, Icons.Filled.NoteAlt, "Notes", currentScreen is Screen.FullNotes || currentScreen is Screen.NoteEditor) { onNavigate(Screen.FullNotes) }
                SidebarNavItem(Icons.Outlined.StickyNote2, Icons.Filled.StickyNote2, "Quick Notes", currentScreen is Screen.Notes || currentScreen is Screen.QuickNotes) { onNavigate(Screen.QuickNotes) }
                SidebarNavItem(Icons.Outlined.Checklist, Icons.Filled.Checklist, "Todo", currentScreen is Screen.Todo) { onNavigate(Screen.Todo) }
                SidebarNavItem(Icons.Outlined.ViewKanban, Icons.Filled.ViewKanban, "Task Board", currentScreen is Screen.TaskManagement) { onNavigate(Screen.TaskManagement) }
                SidebarNavItem(Icons.Outlined.Schedule, Icons.Filled.Schedule, "Schedule", currentScreen is Screen.Schedule) { onNavigate(Screen.Schedule) }
                SidebarNavItem(Icons.Outlined.NotificationsNone, Icons.Filled.Notifications, "Reminders", currentScreen is Screen.Reminders) { onNavigate(Screen.Reminders) }
                SidebarNavItem(Icons.Outlined.Lightbulb, Icons.Filled.Lightbulb, "Prompts", currentScreen is Screen.Prompts) { onNavigate(Screen.Prompts) }
                SidebarNavItem(Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome, "Prompt of Moment", currentScreen is Screen.PromptOfMoment) { onNavigate(Screen.PromptOfMoment) }
                SidebarNavItem(Icons.Outlined.Description, Icons.Filled.Description, "Templates", currentScreen is Screen.Templates) { onNavigate(Screen.Templates) }
                SidebarNavItem(Icons.Outlined.BarChart, Icons.Filled.BarChart, "Statistics", currentScreen is Screen.Stats) { onNavigate(Screen.Stats) }
            }
            HorizontalDivider(color = AppColors.current.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            SidebarNavItem(Icons.Outlined.Settings, Icons.Filled.Settings, "Settings", currentScreen is Screen.Settings) { onNavigate(Screen.Settings) }
        }
    }
}
@Composable
private fun SidebarNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppColors.current.primary.copy(alpha = 0.15f)
            isHovered -> AppColors.current.surface
            else -> Color.Transparent
        },
        animationSpec = tween(150)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.current.textPrimary else AppColors.current.textSecondary,
        animationSpec = tween(150)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(if (isSelected) selectedIcon else icon, label, tint = contentColor, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = contentColor)
    }
}
