package space.zeroxv6.journex.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.viewmodel.NoteViewModel
import java.time.format.DateTimeFormatter
private val BackgroundColor = Color(0xFFF8F8F8)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFF0A0A0A)
private val SecondaryColor = Color(0xFF6B6B6B)
private val TertiaryColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE8E8E8)
private val DividerColor = Color(0xFFF0F0F0)
private val AccentColor = Color(0xFF1F1F1F)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val filteredNotes = viewModel.getFilteredNotes()
    var showViewMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 0.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                fontWeight = FontWeight.Normal,
                                color = PrimaryColor
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PrimaryColor
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {  }) {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = "Search",
                                    tint = SecondaryColor
                                )
                            }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        Icons.Outlined.Sort,
                                        contentDescription = "Sort",
                                        tint = SecondaryColor
                                    )
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Recently Updated") },
                                        onClick = {
                                            viewModel.sortOption = NoteSortOption.DATE_UPDATED
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Newest First") },
                                        onClick = {
                                            viewModel.sortOption = NoteSortOption.DATE_CREATED_DESC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Oldest First") },
                                        onClick = {
                                            viewModel.sortOption = NoteSortOption.DATE_CREATED_ASC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Title A-Z") },
                                        onClick = {
                                            viewModel.sortOption = NoteSortOption.TITLE_ASC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("By Category") },
                                        onClick = {
                                            viewModel.sortOption = NoteSortOption.CATEGORY
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                            Box {
                                IconButton(onClick = { showViewMenu = true }) {
                                    Icon(
                                        when (viewModel.viewMode) {
                                            NoteViewMode.GRID -> Icons.Outlined.GridView
                                            NoteViewMode.LIST -> Icons.Outlined.ViewAgenda
                                            else -> Icons.Outlined.ViewComfy
                                        },
                                        contentDescription = "View",
                                        tint = SecondaryColor
                                    )
                                }
                                DropdownMenu(
                                    expanded = showViewMenu,
                                    onDismissRequest = { showViewMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Grid") },
                                        onClick = {
                                            viewModel.viewMode = NoteViewMode.GRID
                                            showViewMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.GridView, null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("List") },
                                        onClick = {
                                            viewModel.viewMode = NoteViewMode.LIST
                                            showViewMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.ViewAgenda, null)
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SurfaceColor
                        )
                    )
                    HorizontalDivider(
                        color = DividerColor,
                        thickness = 1.dp
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = PrimaryColor,
                contentColor = SurfaceColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = "New Note",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "New Note",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {
            if (filteredNotes.isEmpty()) {
                EmptyState()
            } else {
                when (viewModel.viewMode) {
                    NoteViewMode.GRID, NoteViewMode.MASONRY -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredNotes) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNavigateToEditor(note.id) },
                                    onDelete = { viewModel.deleteNote(note.id) }
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredNotes) { note ->
                                NoteListItem(
                                    note = note,
                                    onClick = { onNavigateToEditor(note.id) },
                                    onDelete = { viewModel.deleteNote(note.id) }
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
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.category.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    ),
                    color = TertiaryColor,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = TertiaryColor
                        )
                    }
                    if (note.isPinned) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(16.dp),
                            tint = AccentColor
                        )
                    }
                }
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Note") },
                    text = { Text("Are you sure you want to delete this note?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDelete()
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.2).sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.plainTextContent.ifEmpty { note.content },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    color = SecondaryColor,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = TertiaryColor
                )
                if (note.wordCount > 0) {
                    Text(
                        text = "${note.wordCount} words",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = TertiaryColor
                    )
                }
            }
        }
    }
}
@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        ),
                        color = TertiaryColor,
                        fontWeight = FontWeight.Medium
                    )
                    if (note.isPinned) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = AccentColor
                        )
                    }
                }
                if (note.title.isNotEmpty()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.plainTextContent.ifEmpty { note.content },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        ),
                        color = SecondaryColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = TertiaryColor
                    )
                }
                Text(
                    text = note.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = TertiaryColor
                )
                if (note.wordCount > 0) {
                    Text(
                        text = "${note.wordCount}w",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = TertiaryColor
                    )
                }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Outlined.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = BorderColor
            )
            Text(
                text = "No notes yet",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    letterSpacing = (-0.3).sp
                ),
                fontWeight = FontWeight.Normal,
                color = SecondaryColor
            )
            Text(
                text = "Create your first note to get started",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                ),
                color = TertiaryColor
            )
        }
    }
}
