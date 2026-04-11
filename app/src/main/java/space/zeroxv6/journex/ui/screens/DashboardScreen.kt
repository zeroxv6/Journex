package space.zeroxv6.journex.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.data.AppDatabase
import space.zeroxv6.journex.model.Reminder
import space.zeroxv6.journex.model.ScheduleItem
import space.zeroxv6.journex.model.TodoTask
import space.zeroxv6.journex.ui.theme.FeatureColors
import space.zeroxv6.journex.ui.theme.GeistFontFamily
import space.zeroxv6.journex.ui.utils.HapticFeedback
import space.zeroxv6.journex.viewmodel.JournalViewModel
import kotlinx.coroutines.launch
import space.zeroxv6.journex.ui.animations.bounceClick
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// Design Tokens
// ─────────────────────────────────────────────────────────────────────────────

private val BgMain    = Color(0xFFF7F4EF)
private val BgSurface = Color(0xFFEFEBE3)
private val BgMuted   = Color(0xFFE4DDD2)
private val Ink       = Color(0xFF1A1714)
private val Ink2      = Color(0xFF3D3830)
private val Ink3      = Color(0xFF7A7168)
private val Ink4      = Color(0xFFADA69D)
private val Border    = Color(0xFFC8BFB4)
private val Rust      = Color(0xFFC05A28)
private val Rust2     = Color(0xFFE8724A)
private val Rust3     = Color(0xFFF5E3D8)
private val Rust4     = Color(0xFFFBF0EB)
private val Sage      = Color(0xFF5C7A5E)
private val Amber     = Color(0xFFE8A030)

// ─────────────────────────────────────────────────────────────────────────────
// Screen Root
// ─────────────────────────────────────────────────────────────────────────────

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val stats = viewModel.getStats()
    val todayEntries = viewModel.entries.filter {
        it.createdAt.toLocalDate() == LocalDate.now() && !it.isArchived
    }
    val incompleteTodos   by taskViewModel.incompleteTodos.collectAsState()
    val enabledSchedules  by taskViewModel.enabledSchedules.collectAsState()
    val upcomingReminders by taskViewModel.upcomingReminders.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = BgMain) {
                NavigationDrawerContent(
                    onNavigateToJournal    = { scope.launch { drawerState.close() }; onNavigateToJournal() },
                    onNavigateToStats      = { scope.launch { drawerState.close() }; onNavigateToStats() },
                    onNavigateToTodo       = { scope.launch { drawerState.close() }; onNavigateToTodo() },
                    onNavigateToSchedule   = { scope.launch { drawerState.close() }; onNavigateToSchedule() },
                    onNavigateToReminders  = { scope.launch { drawerState.close() }; onNavigateToReminders() },
                    onNavigateToQuickNotes = { scope.launch { drawerState.close() }; onNavigateToQuickNotes() },
                    onNavigateToNotes      = { scope.launch { drawerState.close() }; onNavigateToNotes() },
                    onNavigateToPrompts    = { scope.launch { drawerState.close() }; onNavigateToPrompts() },
                    onNavigateToTemplates  = { scope.launch { drawerState.close() }; onNavigateToTemplates() },
                    onNavigateToSettings   = { scope.launch { drawerState.close() }; onNavigateToSettings() }
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgMain)
        ) {
                DashTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSettingsClick = onNavigateToSettings
                )

            LazyColumn(
                modifier       = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(bottom = 56.dp)
            ) {
                item {
                    JournalHeroCard(
                        hasEntry   = todayEntries.isNotEmpty(),
                        entryCount = todayEntries.size,
                        onClick    = onNavigateToJournal,
                        onPromptsClick = onNavigateToPrompts
                    )
                }
                item {
                    DashCaptureCard(
                        viewModel = viewModel
                    )
                }
                item {
                    DashStatsRow(
                        totalEntries  = stats.totalEntries,
                        currentStreak = stats.currentStreak,
                        totalWords    = stats.totalWords,
                        onStatsClick  = onNavigateToStats
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    DashSectionHeader(
                        title       = "Schedule",
                        accentColor = Rust,
                        onSeeAll    = onNavigateToSchedule,
                        modifier    = Modifier.padding(horizontal = 22.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SchedulePreviewCard(
                        schedules       = enabledSchedules.take(3),
                        onClick         = onNavigateToSchedule,
                        use24HourFormat = viewModel.use24HourFormat,
                        modifier        = Modifier.padding(horizontal = 22.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    DashSectionHeader(
                        title       = "Reminders",
                        accentColor = Sage,
                        onSeeAll    = onNavigateToReminders,
                        modifier    = Modifier.padding(horizontal = 22.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RemindersPreviewCard(
                        reminders       = upcomingReminders.take(3),
                        onClick         = onNavigateToReminders,
                        use24HourFormat = viewModel.use24HourFormat,
                        modifier        = Modifier.padding(horizontal = 22.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    DashSectionHeader(
                        title       = "Tasks",
                        accentColor = Ink2,
                        onSeeAll    = onNavigateToTodo,
                        modifier    = Modifier.padding(horizontal = 22.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TasksPreviewCard(
                        tasks       = incompleteTodos.sortedBy { it.priority },
                        onTaskClick = { task -> taskViewModel.toggleTodoCompletion(task) },
                        onCardClick = onNavigateToTodo,
                        modifier    = Modifier.padding(horizontal = 22.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    DashSectionHeader(
                        title       = "Library",
                        accentColor = Ink4,
                        onSeeAll    = null,
                        modifier    = Modifier.padding(horizontal = 22.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LibraryTilesRow(
                        onNotesClick     = onNavigateToNotes,
                        onTemplatesClick = onNavigateToTemplates,
                        onPromptsClick   = onNavigateToPrompts,
                        modifier         = Modifier.padding(horizontal = 22.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// Left: brand mark + wordmark + greeting/date line
// Right: stats icon button + menu button (dark pill)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashTopBar(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val today    = LocalDate.now()
    val dayStr   = today.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
    val hour     = java.time.LocalTime.now().hour
    val greeting = when {
        hour < 5  -> "Late night"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgMain)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Left ────────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                // Brand row
                Text(
                    text          = "Journex",
                    fontSize = 22.sp,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = (-0.4).sp,
                    color         = Ink
                )
                // Greeting + date
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(greeting, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink3, fontFamily = GeistFontFamily)
                    Box(modifier = Modifier.size(3.dp).background(Border, CircleShape))
                    Text(dayStr, fontSize = 15.sp, color = Ink4, fontFamily = GeistFontFamily)
                }
            }

            // ── Right ────────────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Settings Menu
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.Transparent, RoundedCornerShape(10.dp))
                        .border(1.dp, Border, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Ink3, modifier = Modifier.size(20.dp))
                }
                
                // Menu
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Ink, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BgMain, modifier = Modifier.size(20.dp))
                }
            }
        }
        // Hairline
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border.copy(alpha = 0.55f)))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JOURNAL HERO CARD
// Dark card, full-bleed. Content: header band / body / footer action bar.
// Ruled-line texture via Canvas. No floating layers or stacking offsets.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun JournalHeroCard(
    hasEntry   : Boolean,
    entryCount : Int,
    onClick    : () -> Unit,
    onPromptsClick: () -> Unit
) {
    val context    = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF23201D)) // Dark aesthetic
            .border(1.dp, Color(0xFF33302C), RoundedCornerShape(32.dp))
    ) {
        // Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Color(0xFF1E1C1A)) // Darker inset
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = space.zeroxv6.journex.R.drawable.journex),
                contentDescription = "Journex Cover",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit, // Fits without cutting
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Content Area
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
                    text = "WRITE DAILY JOURNALS",
                    color = Color.White.copy(alpha=0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = GeistFontFamily
                )
                Text(
                    text = if (hasEntry) "$entryCount entries" else "No entries today",
                    color = if(hasEntry) Rust else Color.White.copy(alpha=0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GeistFontFamily
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasEntry) "Continue your story." else "Capture your day.",
                    color = Color(0xFFF7F4EF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif
                )
                
                // Add Prompts Here (in the journal card somewhere)
                Text(
                    text = "Prompts",
                    fontFamily = GeistFontFamily,
                    fontSize = 13.sp,
                    color = Rust,
                    modifier = Modifier.clickable(onClick=onPromptsClick).padding(vertical = 4.dp),
                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            val coroutineScope = rememberCoroutineScope()
            val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
            var width by remember { mutableIntStateOf(0) }
            var isUnlocked by remember { mutableStateOf(false) }

            // Idle breathing ring + arrow drift
            val idlePulse = rememberInfiniteTransition(label = "idlePulse")
            val ringScale by idlePulse.animateFloat(
                initialValue  = 1f,
                targetValue   = 1.55f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ringScale"
            )
            val ringAlpha by idlePulse.animateFloat(
                initialValue  = 0.38f,
                targetValue   = 0f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ringAlpha"
            )
            val arrowOffsetX by idlePulse.animateFloat(
                initialValue  = 0f,
                targetValue   = 6f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "arrowOffset"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color(0xFF2C2825), RoundedCornerShape(32.dp))
                    .onGloballyPositioned { width = it.size.width }
                    .border(1.dp, Color.White.copy(alpha=0.04f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                val maxOffset = (width - with(LocalDensity.current) { 64.dp.toPx() }).coerceAtLeast(0f)
                val alphaAmt = if (maxOffset > 0) (1f - (offsetX.value / maxOffset)*1.2f).coerceIn(0f, 1f) else 1f
                // Whether the thumb is at rest (not dragged)
                val atRest = offsetX.value < 4f && !isUnlocked
                
                Text(
                    "Unlock your journal",
                    color = Color.White.copy(alpha = 0.4f * alphaAmt),
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp), // offset text slightly for balance
                    textAlign = TextAlign.Center,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GeistFontFamily
                )
                
                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator

                
                // Glow ring (visible at rest only)
                if (atRest) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = offsetX.value
                                scaleX = ringScale
                                scaleY = ringScale
                                alpha  = ringAlpha
                            }
                            .size(64.dp)
                            .background(Rust.copy(alpha = 0.55f), CircleShape)
                    )
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer { translationX = offsetX.value }
                        .size(64.dp)
                        .background(Rust, CircleShape)
                        .pointerInput(width) {
                            detectHorizontalDragGestures(
                                onDragStart = { },
                                onDragEnd = {
                                    if(isUnlocked) return@detectHorizontalDragGestures
                                    coroutineScope.launch {
                                        offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 800f))
                                    }
                                },
                                onDragCancel = {
                                    if(isUnlocked) return@detectHorizontalDragGestures
                                    coroutineScope.launch {
                                        offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 800f))
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    if(isUnlocked) return@detectHorizontalDragGestures
                                    change.consume()
                                    coroutineScope.launch {
                                        val newOffset = (offsetX.value + dragAmount).coerceIn(0f, maxOffset)
                                        offsetX.snapTo(newOffset)
                                        
                                        // Trigger exactly when hitting the end instantly
                                        if (newOffset >= maxOffset * 0.98f && maxOffset > 0) {
                                            isUnlocked = true
                                            offsetX.snapTo(maxOffset)
                                            try {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    vibrator.vibrate(50)
                                                }
                                            } catch (e: Exception) { }
                                            
                                            onClick()
                                            
                                            // Optional reset after delay
                                            kotlinx.coroutines.delay(800)
                                            offsetX.snapTo(0f)
                                            isUnlocked = false
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        tint = Color.White,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                translationX = if (atRest) arrowOffsetX else 0f
                            }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CAPTURE CARD — redesigned (cleaner input, no icons in shortcuts, more tactile feel)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashCaptureCard(
    viewModel: JournalViewModel
) {
    var noteText by remember { mutableStateOf("") }
    val scope      = rememberCoroutineScope()
    val context    = LocalContext.current
    val database   = remember { space.zeroxv6.journex.data.AppDatabase.getDatabase(context) }
    val repository = remember { space.zeroxv6.journex.repository.QuickNoteRepository(database.quickNoteDao()) }
    val canSave = noteText.trim().isNotEmpty()
    val view = androidx.compose.ui.platform.LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp)
    ) {
        // Tiny floating label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(Amber, RoundedCornerShape(1.dp)))
            Text(
                "QUICK NOTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Ink3,
                fontFamily = GeistFontFamily
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Open Canvas Text Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Elegant vertical line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(Color(0xFFE5DED4))
            )
            Spacer(modifier = Modifier.width(16.dp))
            
            Box(
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 80.dp)
            ) {
                if (noteText.isEmpty()) {
                    Text(
                        "What's on your mind?",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        color = Ink4,
                        fontStyle = FontStyle.Italic
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= 280) noteText = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        color = Ink,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = canSave) {
                Box(
                    modifier = Modifier
                        .background(Rust, RoundedCornerShape(8.dp))
                        .clickable {
                            val trimmed = noteText.trim()
                            scope.launch {
                                repository.insertQuickNote(
                                    space.zeroxv6.journex.data.QuickNoteEntity(
                                        id        = java.util.UUID.randomUUID().toString(),
                                        content   = trimmed,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                noteText = ""
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                    Text("SAVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// STATS ROW
// Single unified surface — 3 cells divided by vertical lines.
// Each cell: large value → bold label → muted sub-note.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashStatsRow(
    totalEntries  : Int,
    currentStreak : Int,
    totalWords    : Int,
    onStatsClick  : () -> Unit
) {
    val wordLabel = when {
        totalWords >= 1000 -> "${totalWords / 1000}.${(totalWords % 1000) / 100}k"
        else               -> "$totalWords"
    }
    data class Stat(val value: String, val label: String, val accent: Color, val sub: String)
    val stats = listOf(
        Stat("$totalEntries",     "Entries", Rust,  "this month"),
        Stat("${currentStreak}d", "Streak",  Amber, "personal best"),
        Stat(wordLabel,           "Words",   Ink2,  "total written")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .background(BgSurface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onStatsClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            stats.forEachIndexed { i, stat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (i < stats.lastIndex) Modifier.drawBehind {
                                drawLine(Border, Offset(size.width, 12.dp.toPx()), Offset(size.width, size.height - 12.dp.toPx()), 1.dp.toPx())
                            } else Modifier
                        )
                        .padding(vertical = 16.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text          = stat.value,
                        fontSize = 25.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color         = stat.accent,
                        lineHeight    = 26.sp
                    )
                    Text(
                        text          = stat.label,
                        fontSize = 15.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color         = Ink2
                    )
                    Text(stat.sub, fontSize = 14.sp, color = Ink4, textAlign = TextAlign.Center, fontFamily = GeistFontFamily)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashSectionHeader(
    title       : String,
    accentColor : Color,
    onSeeAll    : (() -> Unit)?,
    modifier    : Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(7.dp).background(accentColor, CircleShape))
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text          = title.uppercase(),
            fontSize = 14.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            color         = Ink2
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .drawBehind {
                    drawLine(
                        color       = Border,
                        start       = Offset(0f, 0f),
                        end         = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f)
                    )
                }
        )
        if (onSeeAll != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text          = "See all",
                fontSize = 15.sp,
                fontWeight    = FontWeight.Medium,
                color         = accentColor,
                letterSpacing = 0.3.sp,
                modifier      = Modifier.clickable(onClick = onSeeAll)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SCHEDULE CARD (unchanged — already feels solid and not empty)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SchedulePreviewCard(
    schedules       : List<ScheduleItem>,
    onClick         : () -> Unit,
    use24HourFormat : Boolean = false,
    modifier        : Modifier = Modifier
) {
    val now     = java.time.LocalTime.now()
    val timeFmt = DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm")
    val ampmFmt = DateTimeFormatter.ofPattern("a")

    val ongoingSchedule = schedules.find { s ->
        val diff = java.time.Duration.between(s.time, now).toMinutes()
        diff >= -5 && diff <= 60
    }
    val displaySchedules = if (ongoingSchedule != null) {
        listOf(ongoingSchedule) + schedules.sortedBy { it.time }.filter { it.id != ongoingSchedule.id }.take(2)
    } else {
        schedules.sortedBy { it.time }.take(3)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgSurface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp) // extra breathing room so it never feels empty
    ) {
        if (displaySchedules.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Nothing scheduled for today", fontSize = 14.sp, color = Ink3, fontFamily = GeistFontFamily)
            }
        } else {
            displaySchedules.forEachIndexed { index, schedule ->
                val isOngoing = schedule == ongoingSchedule
                val isFirst   = index == 0
                val isLast    = index == displaySchedules.lastIndex
                val dotColor  = when {
                    isOngoing  -> Rust
                    index == 1 -> Amber
                    else       -> Ink4
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isOngoing) Rust4 else Color.Transparent)
                        .then(
                            if (!isLast) Modifier.drawBehind {
                                drawLine(Border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                            } else Modifier
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time column
                    Column(
                        modifier = Modifier
                            .width(84.dp)
                            .drawBehind {
                                drawLine(Border, Offset(size.width, 8.dp.toPx()), Offset(size.width, size.height - 8.dp.toPx()), 1.dp.toPx())
                            }
                            .padding(start = 14.dp, top = 15.dp, bottom = 15.dp, end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            schedule.time.format(timeFmt),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (isOngoing) Rust else Ink2,
                            lineHeight = 16.sp
                        )
                        if (!use24HourFormat) {
                            Text(schedule.time.format(ampmFmt), fontSize = 14.sp, color = if (isOngoing) Rust2 else Ink4, fontFamily = GeistFontFamily)
                        }
                    }

                    // Timeline dot with connectors
                    Box(
                        modifier         = Modifier.width(20.dp).height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isFirst) {
                            Box(modifier = Modifier.width(1.dp).height(26.dp).align(Alignment.TopCenter).background(Border))
                        }
                        if (!isLast) {
                            Box(modifier = Modifier.width(1.dp).height(26.dp).align(Alignment.BottomCenter).background(Border))
                        }
                        Box(
                            modifier = Modifier
                                .size(if (isOngoing) 11.dp else 8.dp)
                                .background(dotColor, CircleShape)
                                .then(if (isOngoing) Modifier.border(2.dp, Rust4, CircleShape) else Modifier)
                        )
                    }

                    // Title + now indicator
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isOngoing) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(Rust, CircleShape))
                                Text(
                                    "Happening now",
                                    fontSize = 14.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    letterSpacing = 0.4.sp,
                                    color         = Rust
                                )
                            }
                        }
                        Text(
                            schedule.title,
                            fontSize = 14.sp,
                            fontWeight = if (isOngoing) FontWeight.SemiBold else FontWeight.Medium,
                            color      = if (isOngoing) Ink else Ink2,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REMINDERS CARD — fully redesigned (clean stacked list, no timeline, no AI-generated look)
// Each reminder sits in its own subtle row with colored time pill + urgency tag.
// More breathing room so the card never feels empty.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RemindersPreviewCard(
    reminders       : List<Reminder>,
    onClick         : () -> Unit,
    use24HourFormat : Boolean = false,
    modifier        : Modifier = Modifier
) {
    val timeFmt = DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm a")
    val dateFmt = DateTimeFormatter.ofPattern("d MMM")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgSurface)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(18.dp) // generous padding so it feels full even with 1-2 items
    ) {
        if (reminders.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "You're all caught up",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Ink2
                )
                Text(
                    "No reminders coming up",
                    fontSize = 13.sp,
                    color    = Ink4
                )
            }
            return@Column
        }

        reminders.forEachIndexed { index, reminder ->
            val nowDt     = java.time.LocalDateTime.now()
            val diffHours = java.time.Duration.between(nowDt, reminder.dateTime).toHours()

            val accentColor = when {
                diffHours <= 0   -> Ink3
                diffHours <= 12  -> Rust
                diffHours <= 48  -> Amber
                diffHours <= 168 -> Sage
                else             -> Ink4
            }
            val pillText = when {
                diffHours <= 0   -> "Due"
                diffHours <= 12  -> "Today"
                diffHours <= 48  -> "Tomorrow"
                diffHours <= 168 -> "This week"
                else             -> "Upcoming"
            }

            val isLast = index == reminders.lastIndex

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp) // extra vertical space between items
                    .then(
                        if (!isLast) Modifier.drawBehind {
                            drawLine(
                                color       = Border.copy(alpha = 0.6f),
                                start       = Offset(0f, size.height + 10.dp.toPx()),
                                end         = Offset(size.width, size.height + 10.dp.toPx()),
                                strokeWidth = 1.dp.toPx()
                            )
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time pill (left)
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        reminder.dateTime.format(timeFmt),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title
                Text(
                    reminder.title,
                    modifier   = Modifier.weight(1f),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = if (diffHours <= 0) Ink3 else Ink,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Urgency pill (right)
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        pillText,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = accentColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TASKS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TasksPreviewCard(
    tasks       : List<TodoTask>,
    onTaskClick : (TodoTask) -> Unit,
    onCardClick : () -> Unit,
    modifier    : Modifier = Modifier
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgSurface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(Border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .clickable(onClick = onCardClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CheckCircleOutline, null, tint = Ink2, modifier = Modifier.size(14.dp))
                Text("Active tasks", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink2, fontFamily = GeistFontFamily)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (tasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .background(Rust3, RoundedCornerShape(6.dp))
                            .border(1.dp, Rust.copy(alpha = 0.22f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text("${tasks.size} left", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Rust, fontFamily = GeistFontFamily)
                    }
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = Ink4, modifier = Modifier.size(14.dp))
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .then(
                    Modifier.nestedScroll(
                        remember {
                            object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: androidx.compose.ui.geometry.Offset,
                                    available: androidx.compose.ui.geometry.Offset,
                                    source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                                ): androidx.compose.ui.geometry.Offset {
                                    return available
                                }
                            }
                        }
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(
                visible = tasks.isEmpty(),
                enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)),
                exit  = fadeOut(animationSpec = tween(250)) + shrinkVertically(animationSpec = tween(250))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgMain)
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).background(Rust4, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Rust, modifier = Modifier.size(16.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text("All done!", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink, fontFamily = GeistFontFamily)
                        Text("Nothing left to tackle.", fontSize = 14.sp, color = Ink3, fontFamily = GeistFontFamily)
                    }
                }
            }
            tasks.forEachIndexed { index, task ->
                key(task.id) {
                    var isCompleting      by remember { mutableStateOf(false) }
                    var showStrikethrough by remember { mutableStateOf(task.isCompleted) }
                    val isLast            = index == tasks.lastIndex
                    val barColor = if (showStrikethrough) Border
                    else listOf(Rust, Amber, Sage, Ink3)[index % 4]

                    AnimatedVisibility(
                        visible = !isCompleting,
                        exit    = fadeOut(tween(350)) + shrinkVertically(tween(350))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgMain)
                                .then(
                                    if (!isLast) Modifier.drawBehind {
                                        drawLine(Border, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                    } else Modifier
                                )
                                .clickable {
                                    if (!task.isCompleted && !isCompleting) {
                                        HapticFeedback.perform(context, HapticFeedback.FeedbackType.STRONG)
                                        showStrikethrough = true
                                        scope.launch {
                                            kotlinx.coroutines.delay(180)
                                            isCompleting = true
                                            kotlinx.coroutines.delay(350)
                                            onTaskClick(task)
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            // removed decorative bar for cleaner UI
                            Box(
                                modifier = Modifier
                                    .size(19.dp)
                                    .border(1.5.dp, if (showStrikethrough) Rust else Border, RoundedCornerShape(5.dp))
                                    .background(if (showStrikethrough) Rust else BgMain, RoundedCornerShape(5.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showStrikethrough,
                                    enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                ) {
                                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
                                }
                            }
                            Text(
                                text           = task.title,
                                fontSize = 14.sp,
                                fontWeight     = if (showStrikethrough) FontWeight.Normal else FontWeight.Medium,
                                color          = if (showStrikethrough) Ink4 else Ink,
                                textDecoration = if (showStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
                                modifier       = Modifier.weight(1f),
                                maxLines       = 1,
                                overflow       = TextOverflow.Ellipsis
                            )
                            if (!showStrikethrough) {
                                // Priority badge: clean text label with a colored dot indicator
                                val priorityLabel = when (task.priority) {
                                    space.zeroxv6.journex.model.Priority.HIGH   -> "High"
                                    space.zeroxv6.journex.model.Priority.MEDIUM -> "Normal"
                                    space.zeroxv6.journex.model.Priority.LOW    -> "Low"
                                }
                                val priorityColor = when (task.priority) {
                                    space.zeroxv6.journex.model.Priority.HIGH   -> Color(0xFFD94F2A)
                                    space.zeroxv6.journex.model.Priority.MEDIUM -> Amber
                                    space.zeroxv6.journex.model.Priority.LOW    -> Sage
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(priorityColor, CircleShape)
                                    )
                                    Text(
                                        priorityLabel.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = priorityColor,
                                        letterSpacing = 0.5.sp,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                }
                            }
                            if (showStrikethrough) {
                                Box(
                                    modifier = Modifier
                                        .background(BgSurface, RoundedCornerShape(5.dp))
                                        .border(1.dp, Border, RoundedCornerShape(5.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text("done", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink4, fontFamily = GeistFontFamily)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIBRARY TILES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LibraryTilesRow(
    onNotesClick     : () -> Unit,
    onTemplatesClick : () -> Unit,
    onPromptsClick   : () -> Unit,
    modifier         : Modifier = Modifier
) {
    data class Tile(val label: String, val accent: Boolean, val onClick: () -> Unit)
    val tiles = listOf(
        Tile("Notes",     false, onNotesClick),
        Tile("Templates", true,  onTemplatesClick),
        Tile("Prompts",   false, onPromptsClick)
    )
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEach { tile ->
            val bg  = if (tile.accent) Rust4 else BgSurface
            val bdr = if (tile.accent) Rust.copy(alpha = 0.26f) else Border
            val fc  = if (tile.accent) Rust else Ink2
            val ic  = if (tile.accent) Rust else Ink4

            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(bg, RoundedCornerShape(12.dp))
                    .border(1.dp, bdr, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = tile.onClick)
                    .padding(horizontal = 13.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tile.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = fc, fontFamily = GeistFontFamily)
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Open", fontSize = 15.sp, color = if (tile.accent) Rust.copy(alpha = 0.6f) else Ink4, fontFamily = GeistFontFamily)
                        Icon(Icons.Outlined.ArrowOutward, null, tint = ic, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NAVIGATION DRAWER — now fully scrollable on every device
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NavigationDrawerContent(
    onNavigateToJournal    : () -> Unit,
    onNavigateToStats      : () -> Unit,
    onNavigateToTodo       : () -> Unit,
    onNavigateToSchedule   : () -> Unit,
    onNavigateToReminders  : () -> Unit,
    onNavigateToQuickNotes : () -> Unit,
    onNavigateToNotes      : () -> Unit,
    onNavigateToPrompts    : () -> Unit,
    onNavigateToTemplates  : () -> Unit,
    onNavigateToSettings   : () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(BgMain)
            .verticalScroll(scrollState)
            .padding(vertical = 24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("Journex", fontSize = 25.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp, color = Ink, fontFamily = GeistFontFamily)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your personal space", fontSize = 15.sp, color = Ink3, fontFamily = GeistFontFamily)
        }
        HorizontalDivider(color = Border, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))
        DrawerNavigationItem(Icons.Outlined.Article,            "Journal",     onNavigateToJournal)
        DrawerNavigationItem(Icons.Outlined.BarChart,           "Statistics",  onNavigateToStats)
        DrawerNavigationItem(Icons.Outlined.CheckCircleOutline, "Tasks",       onNavigateToTodo)
        DrawerNavigationItem(Icons.Outlined.Schedule,           "Schedule",    onNavigateToSchedule)
        DrawerNavigationItem(Icons.Outlined.NotificationsNone,  "Reminders",   onNavigateToReminders)
        DrawerNavigationItem(Icons.Outlined.StickyNote2,        "Quick Notes", onNavigateToQuickNotes)
        DrawerNavigationItem(Icons.Outlined.Note,               "Notes",       onNavigateToNotes)
        DrawerNavigationItem(Icons.Outlined.Lightbulb,          "Prompts",     onNavigateToPrompts)
        DrawerNavigationItem(Icons.Outlined.Description,        "Templates",   onNavigateToTemplates)
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(color = Border, thickness = 1.dp)
        DrawerNavigationItem(Icons.Outlined.Settings, "Settings", onNavigateToSettings)
    }
}

@Composable
fun DrawerNavigationItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(0.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 13.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Ink3, modifier = Modifier.size(19.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Ink2, fontFamily = GeistFontFamily)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LEGACY STUBS (font sizes also reduced for consistency)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatsOverviewCard(stats: space.zeroxv6.journex.model.JournalStats, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashStatsRow(totalEntries = stats.totalEntries, currentStreak = stats.currentStreak, totalWords = stats.totalWords, onStatsClick = onClick)
}

@Composable
fun CircularStatItem(value: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color.copy(alpha = 0.15f), radius = size.minDimension / 2)
            }
            Text(if (value > 999) "${value / 1000}k" else "$value", fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold, fontFamily = GeistFontFamily)
        }
        Text(label, fontSize = 15.sp, color = Ink3, fontFamily = GeistFontFamily)
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(shape = RoundedCornerShape(16.dp), color = Rust4, modifier = Modifier.size(56.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, null, tint = Rust, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, fontSize = 25.sp, fontWeight = FontWeight.Medium, color = Rust, fontFamily = GeistFontFamily)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 14.sp, color = Ink3, textAlign = TextAlign.Center, fontFamily = GeistFontFamily)
    }
}

@Composable
fun TemplatesCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(100.dp)
            .border(1.dp, Rust.copy(alpha = 0.26f), RoundedCornerShape(12.dp))
            .background(Rust4, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Templates", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink, fontFamily = GeistFontFamily)
            Icon(Icons.Outlined.ArrowOutward, null, tint = Ink4, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun NotesPreviewCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(100.dp)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .background(BgSurface, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink, fontFamily = GeistFontFamily)
            Icon(Icons.Outlined.ArrowOutward, null, tint = Ink4, modifier = Modifier.size(16.dp))
        }
    }
}

// Deprecated name aliases
@Composable
fun BrutalistTopBar(onMenuClick: () -> Unit) = DashTopBar(onMenuClick)

@Composable
fun TodayJournalCard(hasEntry: Boolean, entryCount: Int, onClick: () -> Unit) =
    JournalHeroCard(hasEntry, entryCount, onClick, onPromptsClick = {})

@Composable
fun QuickNoteInputCard(viewModel: JournalViewModel, modifier: Modifier = Modifier) {
    DashCaptureCard(viewModel = viewModel)
}

@Composable
fun QuickActionsCard(modifier: Modifier = Modifier, onSettingsClick: () -> Unit, onPromptsClick: () -> Unit, onTemplatesClick: () -> Unit) { /* merged into DashCaptureCard */ }

@Composable
fun StatsStrip(totalEntries: Int, currentStreak: Int, totalWords: Int) {
    DashStatsRow(totalEntries = totalEntries, currentStreak = currentStreak, totalWords = totalWords, onStatsClick = {})
}

@Composable
fun SectionBlock(railColor: Color, title: String, onSeeAll: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        DashSectionHeader(title = title, accentColor = railColor, onSeeAll = onSeeAll)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}