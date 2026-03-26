package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopJournalScreen(viewModel: JournalViewModel, onNavigateToEditor: (String?) -> Unit) {
    val colors = AppColors.current
    val entries by viewModel.filteredEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMood by viewModel.selectedMoodFilter.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(if (showArchived) "Archive" else "Journal", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${entries.size} ${if (entries.size == 1) "entry" else "entries"}", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { viewModel.setShowArchived(!showArchived) }) { Text(if (showArchived) "Active" else "Archive", color = colors.textSecondary, style = MaterialTheme.typography.titleMedium) }
                Button(onClick = { onNavigateToEditor(null) }, colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("New Entry", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search entries...", color = colors.textDisabled) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = colors.textTertiary) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.setSearchQuery("") }) { Icon(Icons.Outlined.Clear, null, tint = colors.textTertiary) } },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, unfocusedBorderColor = colors.border, focusedContainerColor = colors.cardBackground, unfocusedContainerColor = colors.cardBackground),
                singleLine = true
            )
            Box {
                OutlinedButton(onClick = { showSortMenu = true }, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, colors.border), colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)) {
                    Text(sortBy.label, style = MaterialTheme.typography.bodySmall); Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }, containerColor = colors.background) {
                    SortOption.entries.forEach { option -> DropdownMenuItem(text = { Text(option.label) }, onClick = { viewModel.setSortBy(option); showSortMenu = false }, leadingIcon = { if (sortBy == option) Icon(Icons.Filled.Check, null) }) }
                }
            }
            Box {
                OutlinedButton(onClick = { showFilterMenu = true }, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, colors.border), colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)) {
                    Text(selectedMood?.label ?: "All Moods", style = MaterialTheme.typography.bodySmall); Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }, containerColor = colors.background) {
                    DropdownMenuItem(text = { Text("All Moods") }, onClick = { viewModel.setMoodFilter(null); showFilterMenu = false }, leadingIcon = { if (selectedMood == null) Icon(Icons.Filled.Check, null) })
                    HorizontalDivider()
                    Mood.entries.forEach { mood -> DropdownMenuItem(text = { Text(mood.label) }, onClick = { viewModel.setMoodFilter(mood); showFilterMenu = false }, leadingIcon = { if (selectedMood == mood) Icon(Icons.Filled.Check, null) }) }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = colors.inputBackground) { Icon(if (searchQuery.isNotEmpty()) Icons.Outlined.SearchOff else Icons.Outlined.Article, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = colors.textDisabled) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(if (searchQuery.isNotEmpty()) "No results" else "No entries", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (searchQuery.isNotEmpty()) "Try different keywords" else "Start writing your first entry", style = MaterialTheme.typography.bodyLarge, color = colors.textTertiary)
                }
            }
        } else {
            val groupedEntries = entries.groupBy { it.createdAt.toLocalDate() }.toSortedMap(compareByDescending { it })
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                groupedEntries.forEach { (date, dateEntries) ->
                    item(key = "header_$date") {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    items(dateEntries, key = { it.id }) { entry ->
                        EntryCard(
                            entry,
                            { onNavigateToEditor(entry.id) },
                            { viewModel.toggleFavorite(entry.id) },
                            { viewModel.togglePin(entry.id) },
                            { if (entry.isArchived) viewModel.unarchiveEntry(entry.id) else viewModel.archiveEntry(entry.id) },
                            { viewModel.deleteEntry(entry.id) },
                            { viewModel.duplicateEntry(entry.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
private fun EntryCard(entry: JournalEntry, onClick: () -> Unit, onToggleFavorite: () -> Unit, onTogglePin: () -> Unit, onArchive: () -> Unit, onDelete: () -> Unit, onDuplicate: () -> Unit) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, onClick = onClick).hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) colors.borderFocused else colors.border)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (entry.isPinned || entry.isFavorite) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (entry.isPinned) Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) { 
                                Text("Pinned", modifier = Modifier.padding(10.dp, 4.dp), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary) 
                            }
                            if (entry.isFavorite) Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) { 
                                Text("★ Favorite", modifier = Modifier.padding(10.dp, 4.dp), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary) 
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        entry.title.ifEmpty { "Untitled" },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.createdAt.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
                        Text("•", style = MaterialTheme.typography.bodyLarge, color = colors.textDisabled)
                        Text(entry.mood.label, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(44.dp)) { 
                        Icon(Icons.Outlined.MoreVert, null, tint = colors.textTertiary, modifier = Modifier.size(26.dp)) 
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = colors.background) {
                        DropdownMenuItem(text = { Text(if (entry.isFavorite) "Unfavorite" else "Favorite", style = MaterialTheme.typography.bodyLarge) }, onClick = { onToggleFavorite(); showMenu = false }, leadingIcon = { Icon(if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder, null) })
                        DropdownMenuItem(text = { Text(if (entry.isPinned) "Unpin" else "Pin", style = MaterialTheme.typography.bodyLarge) }, onClick = { onTogglePin(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.PushPin, null) })
                        DropdownMenuItem(text = { Text("Duplicate", style = MaterialTheme.typography.bodyLarge) }, onClick = { onDuplicate(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) })
                        DropdownMenuItem(text = { Text(if (entry.isArchived) "Unarchive" else "Archive", style = MaterialTheme.typography.bodyLarge) }, onClick = { onArchive(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Archive, null) })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                    }
                }
            }
            if (entry.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    entry.content.take(250).replace("\n", " "),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    entry.tags.take(4).forEach { tag -> 
                        Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) {
                            Text("#$tag", modifier = Modifier.padding(10.dp, 5.dp), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        }
                    }
                    if (entry.tags.size > 4) Text("+${entry.tags.size - 4} more", style = MaterialTheme.typography.bodyMedium, color = colors.textDisabled)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("${entry.wordCount} words", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
                Text("${entry.readingTime} min read", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
            }
        }
    }
}
