package space.zeroxv6.journex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.viewmodel.NoteViewModel
import java.time.format.DateTimeFormatter

// ─── Palette ─────────────────────────────────────────────────────────────────
private val BgMain      = Color(0xFFF7F4EF)
private val BgSurface   = Color(0xFFFFFDF9)
private val InkPrimary  = Color(0xFF2C2825)
private val InkSecond   = Color(0xFF5A554F)
private val InkMuted    = Color(0xFFA09892)
private val BorderColor = Color(0xFFE5DED4)
private val AccentRust  = Color(0xFFE96525)

// Favorite card: warm gold tint
private val FavBg     = Color(0xFFFFFAEC)
private val FavBorder = Color(0xFFEDD99A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NoteViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val filteredNotes = viewModel.getFilteredNotes()
    var showSortMenu  by remember { mutableStateOf(false) }
    var showViewMenu  by remember { mutableStateOf(false) }
    var isSearching   by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgMain,
        topBar = {
            Surface(color = BgSurface, shadowElevation = 0.dp) {
                Column {
                    TopAppBar(
                        title = {
                            if (isSearching) {
                                TextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.searchQuery = it },
                                    placeholder = { Text("Search Notes…", color = InkMuted) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor   = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        cursorColor             = InkPrimary,
                                        focusedIndicatorColor   = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier  = Modifier.fillMaxWidth().padding(end = 8.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = InkPrimary),
                                    singleLine = true
                                )
                            } else {
                                Text(
                                    "Notes",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize      = 28.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    fontWeight = FontWeight.Normal,
                                    color      = InkPrimary,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            if (isSearching) {
                                IconButton(onClick = { isSearching = false; viewModel.searchQuery = "" }) {
                                    Icon(Icons.Outlined.Close, "Close Search", tint = InkPrimary)
                                }
                            } else {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.Outlined.ArrowBack, "Back", tint = InkPrimary)
                                }
                            }
                        },
                        actions = {
                            if (!isSearching) {
                                // Favorites toggle
                                IconButton(onClick = { viewModel.showFavorites = !viewModel.showFavorites }) {
                                    Icon(
                                        if (viewModel.showFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        "Favorites",
                                        tint = if (viewModel.showFavorites) Color(0xFFD4920A) else InkSecond
                                    )
                                }
                                IconButton(onClick = { isSearching = true }) {
                                    Icon(Icons.Outlined.Search, "Search", tint = InkSecond)
                                }
                                // Sort menu
                                Box {
                                    IconButton(onClick = { showSortMenu = true }) {
                                        Icon(Icons.Outlined.Sort, "Sort", tint = InkSecond)
                                    }
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        modifier = Modifier.background(BgSurface)
                                    ) {
                                        listOf(
                                            NoteSortOption.DATE_UPDATED    to "Recently Updated",
                                            NoteSortOption.DATE_CREATED_DESC to "Newest First",
                                            NoteSortOption.DATE_CREATED_ASC  to "Oldest First",
                                            NoteSortOption.TITLE_ASC         to "Title A–Z",
                                            NoteSortOption.CATEGORY          to "By Category"
                                        ).forEach { (opt, label) ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        label,
                                                        color    = if (viewModel.sortOption == opt) AccentRust else InkPrimary,
                                                        fontSize = 14.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                },
                                                onClick = { viewModel.sortOption = opt; showSortMenu = false },
                                                leadingIcon = {
                                                    if (viewModel.sortOption == opt)
                                                        Icon(Icons.Filled.Check, null, tint = AccentRust, modifier = Modifier.size(16.dp))
                                                    else
                                                        Spacer(Modifier.size(16.dp))
                                                }
                                            )
                                        }
                                    }
                                }
                                // View mode menu
                                Box {
                                    IconButton(onClick = { showViewMenu = true }) {
                                        Icon(
                                            when (viewModel.viewMode) {
                                                NoteViewMode.GRID    -> Icons.Outlined.GridView
                                                NoteViewMode.LIST    -> Icons.Outlined.ViewAgenda
                                                else                 -> Icons.Outlined.ViewComfy
                                            },
                                            "View",
                                            tint = InkSecond
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showViewMenu,
                                        onDismissRequest = { showViewMenu = false },
                                        modifier = Modifier.background(BgSurface)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Grid", fontSize = 14.sp, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            onClick = { viewModel.viewMode = NoteViewMode.GRID; showViewMenu = false },
                                            leadingIcon = { Icon(Icons.Outlined.GridView, null, tint = InkSecond, modifier = Modifier.size(18.dp)) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("List", fontSize = 14.sp, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                            onClick = { viewModel.viewMode = NoteViewMode.LIST; showViewMenu = false },
                                            leadingIcon = { Icon(Icons.Outlined.ViewAgenda, null, tint = InkSecond, modifier = Modifier.size(18.dp)) }
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSurface),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )

                    // ── Category filter chips ─────────────────────────────────────────
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            CategoryChip(
                                label   = "All",
                                selected = viewModel.selectedCategory == null,
                                onClick  = { viewModel.selectedCategory = null }
                            )
                        }
                        items(NoteCategory.entries) { cat ->
                            CategoryChip(
                                label    = cat.label,
                                selected = viewModel.selectedCategory == cat,
                                onClick  = {
                                    viewModel.selectedCategory =
                                        if (viewModel.selectedCategory == cat) null else cat
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = { onNavigateToEditor(null) },
                containerColor = InkPrimary,
                contentColor   = BgSurface,
                elevation      = FloatingActionButtonDefaults.elevation(2.dp, 4.dp)
            ) {
                Icon(Icons.Outlined.Add, "New Note", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("New Note", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(BgMain)) {
            if (filteredNotes.isEmpty()) {
                NotesEmptyState()
            } else {
                when (viewModel.viewMode) {
                    NoteViewMode.GRID, NoteViewMode.MASONRY -> {
                        LazyVerticalGrid(
                            columns             = GridCells.Adaptive(minSize = 160.dp),
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement   = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredNotes) { note ->
                                NoteCard(
                                    note            = note,
                                    onClick         = { onNavigateToEditor(note.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(note.id) },
                                    onPinClick      = { viewModel.togglePin(note.id) },
                                    onDuplicateClick = { viewModel.duplicateNote(note.id) },
                                    onArchiveClick  = { viewModel.archiveNote(note.id) },
                                    onDelete        = { viewModel.deleteNote(note.id) }
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier       = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredNotes) { note ->
                                NoteListItem(
                                    note            = note,
                                    onClick         = { onNavigateToEditor(note.id) },
                                    onFavoriteClick = { viewModel.toggleFavorite(note.id) },
                                    onPinClick      = { viewModel.togglePin(note.id) },
                                    onDuplicateClick = { viewModel.duplicateNote(note.id) },
                                    onArchiveClick  = { viewModel.archiveNote(note.id) },
                                    onDelete        = { viewModel.deleteNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Chip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg     = if (selected) InkPrimary else BgSurface
    val fg     = if (selected) BgSurface  else InkSecond
    val border = if (selected) InkPrimary else BorderColor

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            color      = fg,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            textAlign  = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Note Card (Grid)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit  = {},
    onPinClick: () -> Unit       = {},
    onDuplicateClick: () -> Unit = {},
    onArchiveClick: () -> Unit   = {},
    onDelete: () -> Unit         = {}
) {
    var showMenu        by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Favourite → gold; else per-hash ambient tint
    val cardBg: Color
    val cardBorder: Color
    if (note.isFavorite) {
        cardBg     = FavBg
        cardBorder = FavBorder
    } else {
        val bg = listOf(
            Color(0xFFFFFAF4), Color(0xFFF4FFF8), Color(0xFFF7F4FF),
            Color(0xFFFFF4F4), Color(0xFFF4F8FF), Color(0xFFFFFDF0)
        )
        val bdr = listOf(
            Color(0xFFEEE0CC), Color(0xFFCCE8D7), Color(0xFFD8D0EE),
            Color(0xFFEED0D0), Color(0xFFCCD8EE), Color(0xFFEEE8CC)
        )
        val idx = Math.abs(note.id.hashCode()) % bg.size
        cardBg     = bg[idx]
        cardBorder = bdr[idx]
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Top action row ────────────────────────────────────────────
            Row(
                modifier    = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Category microtype
                Text(
                    text          = note.category.label.uppercase(),
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp,
                    color         = AccentRust.copy(alpha = 0.65f),
                    maxLines      = 1,
                    overflow      = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (note.isFavorite) {
                        Icon(Icons.Filled.Star, null, Modifier.size(13.dp), tint = Color(0xFFD4920A))
                    }
                    if (note.isPinned) {
                        Icon(Icons.Filled.PushPin, null, Modifier.size(13.dp), tint = AccentRust.copy(alpha = 0.6f))
                    }
                    // Context menu
                    Box {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                ) { showMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.MoreVert, null, Modifier.size(16.dp), tint = InkMuted)
                        }
                        NoteContextMenu(
                            expanded        = showMenu,
                            note            = note,
                            onDismiss       = { showMenu = false },
                            onFavoriteClick = { onFavoriteClick(); showMenu = false },
                            onPinClick      = { onPinClick(); showMenu = false },
                            onDuplicateClick = { onDuplicateClick(); showMenu = false },
                            onArchiveClick  = { onArchiveClick(); showMenu = false },
                            onDeleteClick   = { showMenu = false; showDeleteDialog = true }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Title + body ──────────────────────────────────────────────
            if (note.title.isNotEmpty()) {
                Text(
                    text       = note.title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color      = InkPrimary,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(5.dp))
            }
            if (note.content.isNotEmpty()) {
                Text(
                    text       = note.plainTextContent.ifEmpty { note.content },
                    fontSize   = 13.sp,
                    color      = InkMuted,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Footer ────────────────────────────────────────────────────
            Row(
                modifier    = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (note.wordCount > 0) {
                    Text("${note.wordCount}w", fontSize = 10.sp, color = InkMuted)
                }
                Text(
                    note.updatedAt.format(DateTimeFormatter.ofPattern("MMM d")),
                    fontSize = 10.sp,
                    color    = InkMuted
                )
            }
        }

        if (showDeleteDialog) {
            NoteDeleteDialog(
                onConfirm = { onDelete(); showDeleteDialog = false },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Note List Item
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit  = {},
    onPinClick: () -> Unit       = {},
    onDuplicateClick: () -> Unit = {},
    onArchiveClick: () -> Unit   = {},
    onDelete: () -> Unit         = {}
) {
    var showMenu         by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardBg     = if (note.isFavorite) FavBg     else BgSurface
    val cardBorder = if (note.isFavorite) FavBorder else BorderColor

    // Date column values
    val day   = note.updatedAt.dayOfMonth.toString()
    val month = note.updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM")).uppercase()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left date column ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    day,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color      = if (note.isFavorite) Color(0xFFB07A00) else InkPrimary,
                    textAlign  = TextAlign.Center
                )
                Text(
                    month,
                    fontSize      = 8.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color         = if (note.isFavorite) Color(0xFFD4920A).copy(alpha = 0.7f) else InkMuted,
                    textAlign     = TextAlign.Center
                )
            }

            // ── Vertical ink divider ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(64.dp)
                    .background(if (note.isFavorite) FavBorder else BorderColor)
            )

            // ── Content block ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Category microtype
                Text(
                    note.category.label.uppercase(),
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp,
                    color         = AccentRust.copy(alpha = 0.65f),
                    maxLines      = 1,
                    overflow      = TextOverflow.Ellipsis
                )

                // Title
                if (note.title.isNotEmpty()) {
                    Text(
                        note.title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color      = InkPrimary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                // Body preview
                if (note.content.isNotEmpty()) {
                    Text(
                        note.plainTextContent.ifEmpty { note.content },
                        fontSize   = 12.sp,
                        color      = InkMuted,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                // Meta strip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (note.isFavorite) {
                        Icon(Icons.Filled.Star, null, Modifier.size(10.dp), tint = Color(0xFFD4920A))
                    }
                    if (note.isPinned) {
                        Icon(Icons.Filled.PushPin, null, Modifier.size(10.dp), tint = AccentRust.copy(alpha = 0.6f))
                    }
                    if (note.wordCount > 0) {
                        Text("${note.wordCount}w", fontSize = 10.sp, color = InkMuted)
                        Box(
                            Modifier
                                .size(2.dp)
                                .background(InkMuted.copy(alpha = 0.4f), CircleShape)
                        )
                    }
                }
            }

            // ── Right: context menu ───────────────────────────────────────
            Box(
                modifier = Modifier.padding(end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.MoreVert, null, Modifier.size(16.dp), tint = InkMuted)
                }
                NoteContextMenu(
                    expanded         = showMenu,
                    note             = note,
                    onDismiss        = { showMenu = false },
                    onFavoriteClick  = { onFavoriteClick(); showMenu = false },
                    onPinClick       = { onPinClick(); showMenu = false },
                    onDuplicateClick = { onDuplicateClick(); showMenu = false },
                    onArchiveClick   = { onArchiveClick(); showMenu = false },
                    onDeleteClick    = { showMenu = false; showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        NoteDeleteDialog(
            onConfirm = { onDelete(); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared Context Menu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NoteContextMenu(
    expanded: Boolean,
    note: Note,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onPinClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    DropdownMenu(
        expanded          = expanded,
        onDismissRequest  = onDismiss,
        modifier          = Modifier.background(BgSurface)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (note.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    fontSize = 14.sp, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            },
            onClick = onFavoriteClick,
            leadingIcon = {
                Icon(
                    if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    null,
                    tint     = if (note.isFavorite) Color(0xFFD4920A) else InkSecond,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (note.isPinned) "Unpin" else "Pin to Top",
                    fontSize = 14.sp, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            },
            onClick = onPinClick,
            leadingIcon = {
                Icon(
                    if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    null,
                    tint     = AccentRust,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Duplicate", fontSize = 14.sp, color = InkPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            onClick = onDuplicateClick,
            leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, tint = InkSecond, modifier = Modifier.size(18.dp)) }
        )
        HorizontalDivider(color = BorderColor)
        DropdownMenuItem(
            text = { Text("Delete", fontSize = 14.sp, color = Color(0xFFD94F2A), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            onClick = onDeleteClick,
            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFD94F2A), modifier = Modifier.size(18.dp)) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Delete dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NoteDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Delete Note", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text    = { Text("This note will be permanently removed.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFD94F2A)),
                shape   = RoundedCornerShape(10.dp)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = BgSurface,
        shape  = RoundedCornerShape(16.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NotesEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Outlined.NoteAdd, null, modifier = Modifier.size(80.dp), tint = BorderColor)
            Text(
                "No notes yet",
                style      = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, letterSpacing = (-0.3).sp),
                fontWeight = FontWeight.Normal,
                color      = InkSecond
            )
            Text(
                "Create your first note to get started",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = InkMuted
            )
        }
    }
}

// ─── Legacy alias kept so callers that reference EmptyState() still compile ──
@Composable
fun EmptyState() = NotesEmptyState()
