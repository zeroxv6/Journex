package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.ui.components.DesktopAdvancedSearch
import space.zeroxv6.journex.desktop.ui.components.DesktopKanbanBoard
import space.zeroxv6.journex.desktop.viewmodel.NoteViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopFullNotesScreen(
    viewModel: NoteViewModel,
    onNavigateToEditor: (String?) -> Unit
) {
    val filteredNotes by viewModel.filteredNotes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAdvancedSearch by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Notes", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp), color = AppColors.current.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${filteredNotes.size} notes", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NoteViewMode.entries.forEach { mode ->
                        val icon = when (mode) {
                            NoteViewMode.GRID -> Icons.Outlined.GridView
                            NoteViewMode.LIST -> Icons.Outlined.ViewList
                            NoteViewMode.KANBAN -> Icons.Outlined.ViewKanban
                        }
                        IconButton(
                            onClick = { viewModel.setViewMode(mode) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(icon, mode.name, modifier = Modifier.size(22.dp), tint = if (viewMode == mode) AppColors.current.textPrimary else AppColors.current.textDisabled)
                        }
                    }
                }
                Button(
                    onClick = { onNavigateToEditor(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Note", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search notes...", color = AppColors.current.textDisabled) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = AppColors.current.textTertiary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Outlined.Clear, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.current.textPrimary,
                    unfocusedBorderColor = AppColors.current.border,
                    focusedContainerColor = AppColors.current.inputBackground,
                    unfocusedContainerColor = AppColors.current.inputBackground
                )
            )
            OutlinedButton(
                onClick = { showAdvancedSearch = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppColors.current.border)
            ) {
                Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Advanced", color = AppColors.current.textSecondary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { viewModel.setSelectedCategory(null) },
                label = { Text("All", style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background, containerColor = AppColors.current.cardBackground),
                shape = RoundedCornerShape(8.dp)
            )
            NoteCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { viewModel.setSelectedCategory(if (selectedCategory == cat) null else cat) },
                    label = { Text(cat.label, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background, containerColor = AppColors.current.cardBackground),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) {
                        Icon(Icons.Outlined.NoteAdd, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No notes yet", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium), color = AppColors.current.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Create your first note to get started", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onNavigateToEditor(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Note")
                    }
                }
            }
        } else {
            when (viewMode) {
                NoteViewMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            NoteGridCard(note, onClick = { onNavigateToEditor(note.id) }, onActions = viewModel)
                        }
                    }
                }
                NoteViewMode.LIST -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredNotes, key = { it.id }) { note ->
                            NoteListCard(note, onClick = { onNavigateToEditor(note.id) }, onActions = viewModel)
                        }
                    }
                }
                NoteViewMode.KANBAN -> {
                    DesktopKanbanBoard(
                        notes = filteredNotes,
                        onNoteClick = { onNavigateToEditor(it.id) },
                        onStatusChange = { noteId, status -> viewModel.updateNoteStatus(noteId, status) }
                    )
                }
            }
        }
    }
    if (showAdvancedSearch) {
        DesktopAdvancedSearch(
            currentFilter = viewModel.filter.collectAsState().value,
            availableTags = viewModel.getAllTags(),
            onApply = { filter -> viewModel.setFilter(filter); showAdvancedSearch = false },
            onDismiss = { showAdvancedSearch = false }
        )
    }
}
@Composable
private fun NoteGridCard(note: FullNote, onClick: () -> Unit, onActions: NoteViewModel) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    val checkedCount = note.checklistItems.count { it.isChecked }
    val totalChecklist = note.checklistItems.size
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.surfaceVariant) {
                    Text(note.category.label, modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) Icon(Icons.Filled.PushPin, null, modifier = Modifier.size(14.dp), tint = AppColors.current.textTertiary)
                    if (note.isFavorite) Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(14.dp), tint = AppColors.current.error)
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(16.dp))
                        }
                        NoteActionsMenu(showMenu, { showMenu = false }, note, onActions)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                note.title.ifEmpty { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.current.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            if (totalChecklist > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Checklist, null, modifier = Modifier.size(14.dp), tint = AppColors.current.textTertiary)
                    Text("$checkedCount / $totalChecklist", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    LinearProgressIndicator(
                        progress = { if (totalChecklist > 0) checkedCount.toFloat() / totalChecklist else 0f },
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = AppColors.current.success,
                        trackColor = AppColors.current.surfaceVariant
                    )
                }
            }
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.take(3).forEach { tag ->
                        Surface(shape = RoundedCornerShape(4.dp), color = AppColors.current.surfaceVariant) {
                            Text("#$tag", modifier = Modifier.padding(4.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                        }
                    }
                    if (note.tags.size > 3) Text("+${note.tags.size - 3}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(note.updatedAt.format(DateTimeFormatter.ofPattern("MMM d")), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                if (note.wordCount > 0) Text("${note.wordCount} words", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                if (note.attachments.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AttachFile, null, modifier = Modifier.size(12.dp), tint = AppColors.current.textDisabled)
                        Text("${note.attachments.size}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                    }
                }
            }
        }
    }
}
@Composable
private fun NoteListCard(note: FullNote, onClick: () -> Unit, onActions: NoteViewModel) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(12.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.surfaceVariant) {
                Text(note.category.label, modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (note.isPinned) Icon(Icons.Filled.PushPin, null, modifier = Modifier.size(12.dp), tint = AppColors.current.textTertiary)
                    Text(note.title.ifEmpty { "Untitled Note" }, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = AppColors.current.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (note.isFavorite) Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(12.dp), tint = AppColors.current.error)
                }
                if (note.content.isNotEmpty()) {
                    Text(note.content, style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (note.checklistItems.isNotEmpty()) {
                Text("${note.checklistItems.count { it.isChecked }}/${note.checklistItems.size}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
            }
            Text(note.updatedAt.format(DateTimeFormatter.ofPattern("MMM d")), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
            if (note.priority != NotePriority.NONE) {
                val priorityColor = when(note.priority) {
                    NotePriority.LOW -> AppColors.current.success
                    NotePriority.MEDIUM -> AppColors.current.warning
                    NotePriority.HIGH -> AppColors.current.error
                    NotePriority.URGENT -> AppColors.current.error
                    else -> AppColors.current.textTertiary
                }
                Surface(shape = RoundedCornerShape(4.dp), color = priorityColor.copy(alpha = 0.15f)) {
                    Text(note.priority.label, modifier = Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = priorityColor)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(16.dp))
                }
                NoteActionsMenu(showMenu, { showMenu = false }, note, onActions)
            }
        }
    }
}
@Composable
private fun NoteActionsMenu(expanded: Boolean, onDismiss: () -> Unit, note: FullNote, viewModel: NoteViewModel) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss, containerColor = AppColors.current.background) {
        DropdownMenuItem(text = { Text(if (note.isFavorite) "Unfavorite" else "Favorite") }, onClick = { viewModel.toggleFavorite(note.id); onDismiss() }, leadingIcon = { Icon(if (note.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(18.dp)) })
        DropdownMenuItem(text = { Text(if (note.isPinned) "Unpin" else "Pin") }, onClick = { viewModel.togglePin(note.id); onDismiss() }, leadingIcon = { Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp)) })
        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { viewModel.duplicateNote(note.id); onDismiss() }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp)) })
        DropdownMenuItem(text = { Text("Archive") }, onClick = { viewModel.archiveNote(note.id); onDismiss() }, leadingIcon = { Icon(Icons.Outlined.Archive, null, modifier = Modifier.size(18.dp)) })
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        DropdownMenuItem(text = { Text("Delete", color = AppColors.current.error) }, onClick = { viewModel.deleteNote(note.id); onDismiss() }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = AppColors.current.error, modifier = Modifier.size(18.dp)) })
    }
}
