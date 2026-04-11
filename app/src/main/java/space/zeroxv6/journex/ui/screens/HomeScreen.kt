package space.zeroxv6.journex.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.JournalEntry
import space.zeroxv6.journex.model.Mood
import space.zeroxv6.journex.ui.utils.HapticFeedback
import space.zeroxv6.journex.viewmodel.JournalViewModel
import space.zeroxv6.journex.viewmodel.SortOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToQuickNotes: () -> Unit = {},
    onNavigateToPrompts: () -> Unit = {},
    onNavigateToTodo: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.showArchived = false
        }
    }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val filteredEntries = viewModel.getFilteredEntries()
    val entriesByDate = filteredEntries.groupBy { it.createdAt.toLocalDate() }
    val today = LocalDate.now()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    if (viewModel.showArchived) {
        androidx.activity.compose.BackHandler {
            viewModel.showArchived = false
        }
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (viewModel.showArchived) "Archived" else "Journals",
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (viewModel.showArchived) {
                            IconButton(onClick = { viewModel.showArchived = false }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (!viewModel.showArchived) {
                            if (!showSearchBar) {
                                IconButton(onClick = { showSearchBar = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search")
                                }
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                                }
                                IconButton(onClick = { showSortSheet = true }) {
                                    Icon(Icons.Outlined.Sort, contentDescription = "Sort")
                                }
                                IconButton(onClick = { viewModel.showArchived = true }) {
                                    Icon(Icons.Outlined.Archive, contentDescription = "Archive")
                                }
                                Box {
                                    IconButton(onClick = { showMoreMenu = true }) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                                    }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Quick Notes", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToQuickNotes()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.StickyNote2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Writing Prompts", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToPrompts()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Templates", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToTemplates()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                DropdownMenuItem(
                                    text = { Text("Tasks", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToTodo()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Daily Schedule", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToSchedule()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reminders", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToReminders()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Complete Journal", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        showMoreMenu = false
                                        val exportText = viewModel.getExportText()
                                        val file = space.zeroxv6.journex.utils.ExportUtils.saveToFile(
                                            context,
                                            exportText,
                                            "Journal_Export_${System.currentTimeMillis()}.txt"
                                        )
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TITLE, "My Journal Export")
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Complete Journal"))
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                DropdownMenuItem(
                                    text = { Text("Settings", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    onClick = {
                                        onNavigateToSettings()
                                        showMoreMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                )
                            }
                        }
                            } else {
                                TextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.searchQuery = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    placeholder = { Text("Search...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    )
                                )
                                IconButton(onClick = { 
                                    showSearchBar = false
                                    viewModel.searchQuery = ""
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close search")
                                }
                            }
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
            if (!viewModel.showArchived) {
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
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            HapticFeedback.perform(context, HapticFeedback.FeedbackType.MEDIUM)
                            viewModel.createNewEntry()
                            onNavigateToEditor(null)
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
                        Icon(Icons.Filled.Add, contentDescription = "New Entry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Entry", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    ) { padding ->
        if (viewModel.showArchived) {
            if (filteredEntries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Archived Entries",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Archived entries will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
                    items(filteredEntries) { entry ->
                        ArchivedEntryCard(
                            entry = entry,
                            onClick = { onNavigateToEditor(entry.id) },
                            onUnarchiveClick = { viewModel.unarchiveEntry(entry.id) },
                            onDeleteClick = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    DailyEntrySection(
                        date = today,
                        entries = entriesByDate[today] ?: emptyList(),
                        isToday = true,
                        onEntryClick = onNavigateToEditor,
                        onNewEntryClick = {
                            viewModel.createNewEntry()
                            onNavigateToEditor(null)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onPinClick = { viewModel.togglePin(it) },
                        onArchiveClick = { viewModel.archiveEntry(it) },
                        onDuplicateClick = { viewModel.duplicateEntry(it) },
                        onDeleteClick = { viewModel.deleteEntry(it) }
                    )
                }
                val previousDates = entriesByDate.keys
                    .filter { it.isBefore(today) }
                    .sortedDescending()
                items(previousDates) { date ->
                    DailyEntrySection(
                        date = date,
                        entries = entriesByDate[date] ?: emptyList(),
                        isToday = false,
                        onEntryClick = onNavigateToEditor,
                        onNewEntryClick = null,
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onPinClick = { viewModel.togglePin(it) },
                        onArchiveClick = { viewModel.archiveEntry(it) },
                        onDuplicateClick = { viewModel.duplicateEntry(it) },
                        onDeleteClick = { viewModel.deleteEntry(it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    if (showFilterSheet) {
        FilterBottomSheet(
            viewModel = viewModel,
            onDismiss = { showFilterSheet = false }
        )
    }
    if (showSortSheet) {
        SortBottomSheet(
            viewModel = viewModel,
            onDismiss = { showSortSheet = false }
        )
    }
}
@Composable
fun DailyEntrySection(
    date: LocalDate,
    entries: List<JournalEntry>,
    isToday: Boolean,
    onEntryClick: (String?) -> Unit,
    onNewEntryClick: (() -> Unit)?,
    onFavoriteClick: (String) -> Unit,
    onPinClick: (String) -> Unit,
    onArchiveClick: (String) -> Unit,
    onDuplicateClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isToday) "Today" else date.format(DateTimeFormatter.ofPattern("EEEE")),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entries.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${entries.size} ${if (entries.size == 1) "entry" else "entries"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (entries.isEmpty()) {
            EmptyDayCard(
                isToday = isToday,
                onNewEntryClick = onNewEntryClick
            )
        } else {
            entries.forEach { entry ->
                JournalEntryCard(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) },
                    onFavoriteClick = { onFavoriteClick(entry.id) },
                    onPinClick = { onPinClick(entry.id) },
                    onArchiveClick = { onArchiveClick(entry.id) },
                    onDuplicateClick = { onDuplicateClick(entry.id) },
                    onDeleteClick = { onDeleteClick(entry.id) }
                )
            }
        }
    }
}
@Composable
fun ArchivedEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onUnarchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.title.isNotEmpty()) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Unarchive",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            onClick = {
                                onUnarchiveClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Unarchive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Delete",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = entry.mood.icon,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${entry.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Delete Entry",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to permanently delete this entry?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun EmptyDayCard(
    isToday: Boolean,
    onNewEntryClick: (() -> Unit)?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emptyCardScale"
    )
    val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isToday) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(if (isToday && onNewEntryClick != null) scale * pulseScale else scale)
            .then(
                if (isToday && onNewEntryClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                vibrator?.let {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        it.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        it.vibrate(40)
                                    }
                                }
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onNewEntryClick() }
                        )
                    }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.EditNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = if (isToday) "No entry for today" else "No entries",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isToday && onNewEntryClick != null) {
                Text(
                    text = "Tap to write your first entry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    // ── Editorial color tokens ────────────────────────────────────────────
    val CardBg     = Color(0xFFFFFDF9)
    val CardBorder = Color(0xFFE5DED4)
    val DateInk    = Color(0xFF2C2825)
    val BodyInk    = Color(0xFF2C2825)
    val MuteInk    = Color(0xFFA09892)
    val AccentRust = Color(0xFFC05A28)
    val PinAmber   = Color(0xFFD4920A)

    val dayStr   = entry.createdAt.format(DateTimeFormatter.ofPattern("d"))
    val monStr   = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM")).uppercase()
    val timeStr  = entry.createdAt.format(DateTimeFormatter.ofPattern("h:mm a"))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                        vibrator?.let {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                it.vibrate(android.os.VibrationEffect.createOneShot(22, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION") it.vibrate(22)
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // ── DATE COLUMN ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .padding(top = 20.dp, bottom = 20.dp, start = 16.dp, end = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Optional pin/favourite badges above date
                if (entry.isPinned || entry.isFavorite) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (entry.isPinned) {
                            Icon(
                                Icons.Filled.PushPin,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = PinAmber
                            )
                        }
                        if (entry.isFavorite) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = AccentRust
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = dayStr,
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = DateInk,
                    lineHeight = 28.sp
                )
                Text(
                    text = monStr,
                    fontSize = 10.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = MuteInk,
                    lineHeight = 14.sp
                )
            }

            // ── DIVIDER ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(CardBorder)
            )

            // ── CONTENT ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 18.dp, bottom = 16.dp, end = 4.dp)
            ) {
                // Title (if exists)
                if (entry.title.isNotEmpty()) {
                    Text(
                        text = entry.title,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 17.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = BodyInk,
                            lineHeight = 22.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                     )
                    Spacer(Modifier.height(6.dp))
                }

                // Excerpt
                Text(
                    text = entry.content,
                    style = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = BodyInk.copy(alpha = 0.65f)
                    ),
                    maxLines = if (entry.title.isEmpty()) 3 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Any photos strip
                if (entry.photos.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        entry.photos.take(3).forEach { photoUri ->
                            coil.compose.AsyncImage(
                                model = if (photoUri.startsWith("/")) java.io.File(photoUri) else android.net.Uri.parse(photoUri),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(7.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        if (entry.photos.size > 3) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(CardBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+${entry.photos.size - 3}", fontSize = 13.sp, color = MuteInk)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Meta strip ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mood + time + word count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(entry.mood.icon, fontSize = 14.sp)
                        Box(Modifier.size(3.dp).background(MuteInk.copy(alpha = 0.4f), CircleShape))
                        Text(timeStr, fontSize = 11.sp, color = MuteInk, letterSpacing = 0.3.sp)
                        Box(Modifier.size(3.dp).background(MuteInk.copy(alpha = 0.4f), CircleShape))
                        Text("${entry.wordCount}w", fontSize = 11.sp, color = MuteInk)
                        if (entry.voiceNotes.isNotEmpty()) {
                            Icon(Icons.Outlined.Mic, contentDescription = null, modifier = Modifier.size(12.dp), tint = MuteInk)
                        }
                    }
                    // Tags (first one)
                    if (entry.tags.isNotEmpty()) {
                        Text(
                            "#${entry.tags.first()}${if (entry.tags.size > 1) " +${entry.tags.size - 1}" else ""}",
                            fontSize = 11.sp,
                            color = AccentRust.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ── MENU ──────────────────────────────────────────────────────
            Box(modifier = Modifier.padding(top = 8.dp, end = 4.dp)) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "More options", tint = MuteInk, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (entry.isFavorite) "Remove from Favorites" else "Add to Favorites", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onFavoriteClick(); showMenu = false },
                        leadingIcon = { Icon(if (entry.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (entry.isPinned) "Unpin" else "Pin to Top", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onPinClick(); showMenu = false },
                        leadingIcon = { Icon(if (entry.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                    DropdownMenuItem(
                        text = { Text("Archive", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onArchiveClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Outlined.Archive, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onDuplicateClick(); showMenu = false },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    DropdownMenuItem(
                        text = { Text("Delete", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showMenu = false; showDeleteDialog = true },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurface) }
                    )
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    "Delete Entry",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete this entry? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    viewModel: JournalViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filter Entries",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Mood",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Mood.entries.chunked(2).forEach { moodRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodRow.forEach { mood ->
                            FilterChip(
                                selected = viewModel.selectedMoodFilter == mood,
                                onClick = {
                                    viewModel.selectedMoodFilter = if (viewModel.selectedMoodFilter == mood) null else mood
                                },
                                label = { Text("${mood.icon} ${mood.label}", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            val allTags = viewModel.getAllTags()
            if (allTags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allTags.chunked(3).forEach { tagRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tagRow.forEach { tag ->
                                FilterChip(
                                    selected = viewModel.selectedTagFilter == tag,
                                    onClick = {
                                        viewModel.selectedTagFilter = if (viewModel.selectedTagFilter == tag) null else tag
                                    },
                                    label = { Text("#$tag", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                            repeat(3 - tagRow.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No tags available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.selectedMoodFilter = null
                    viewModel.selectedTagFilter = null
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Clear Filters", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    viewModel: JournalViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            SortOption.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.sortBy = option
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (viewModel.sortBy == option) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
