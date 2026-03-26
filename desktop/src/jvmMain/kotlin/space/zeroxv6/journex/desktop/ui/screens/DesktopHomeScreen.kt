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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopHomeScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToStats: () -> Unit
) {
    val allEntries by viewModel.entries.collectAsState()
    val stats by viewModel.stats.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var selectedMoodFilter by remember { mutableStateOf<Mood?>(null) }
    var selectedSortOption by remember { mutableStateOf(SortOption.DATE_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    val filteredEntries = remember(allEntries, searchQuery, showArchived, selectedMoodFilter, selectedSortOption) {
        var entries = allEntries.filter { it.isArchived == showArchived }
        if (searchQuery.isNotEmpty()) {
            entries = entries.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { t -> t.contains(searchQuery, ignoreCase = true) }
            }
        }
        selectedMoodFilter?.let { mood -> entries = entries.filter { it.mood == mood } }
        when (selectedSortOption) {
            SortOption.DATE_DESC -> entries.sortedByDescending { it.createdAt }
            SortOption.DATE_ASC -> entries.sortedBy { it.createdAt }
            SortOption.UPDATED -> entries.sortedByDescending { it.updatedAt }
            SortOption.TITLE_ASC -> entries.sortedBy { it.title.lowercase() }
            SortOption.TITLE_DESC -> entries.sortedByDescending { it.title.lowercase() }
            SortOption.MOOD -> entries.sortedBy { it.mood.ordinal }
            SortOption.WORD_COUNT -> entries.sortedByDescending { it.wordCount }
        }
    }
    val groupedEntries = filteredEntries.groupBy { it.createdAt.toLocalDate() }
        .toSortedMap(compareByDescending { it })
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    if (showArchived) "Archived Entries" else "Journal",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${filteredEntries.size} entries" + if (!showArchived) " · ${stats.currentStreak} day streak" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.current.textSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showArchived = !showArchived },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppColors.current.border)
                ) {
                    Icon(if (showArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showArchived) "Active" else "Archived", color = AppColors.current.textSecondary)
                }
                OutlinedButton(onClick = onNavigateToStats, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AppColors.current.border)) {
                    Icon(Icons.Outlined.BarChart, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stats", color = AppColors.current.textSecondary)
                }
                Button(
                    onClick = { onNavigateToEditor(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Entry", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search entries...", color = AppColors.current.textDisabled) },
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = AppColors.current.textTertiary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
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
            OutlinedButton(onClick = { showFilterSheet = !showFilterSheet }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, if (selectedMoodFilter != null) AppColors.current.textPrimary else AppColors.current.border)) {
                Icon(Icons.Outlined.FilterList, null, modifier = Modifier.size(18.dp), tint = if (selectedMoodFilter != null) AppColors.current.textPrimary else AppColors.current.textSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Filter", color = if (selectedMoodFilter != null) AppColors.current.textPrimary else AppColors.current.textSecondary)
            }
            OutlinedButton(onClick = { showSortSheet = !showSortSheet }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AppColors.current.border)) {
                Icon(Icons.Outlined.Sort, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sort", color = AppColors.current.textSecondary)
            }
        }
        if (showFilterSheet) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Filter by Mood", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                FilterChip(
                    selected = selectedMoodFilter == null,
                    onClick = { selectedMoodFilter = null },
                    label = { Text("All", style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background),
                    shape = RoundedCornerShape(8.dp)
                )
                Mood.entries.forEach { mood ->
                    FilterChip(
                        selected = selectedMoodFilter == mood,
                        onClick = { selectedMoodFilter = if (selectedMoodFilter == mood) null else mood },
                        label = { Text(mood.label, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
        if (showSortSheet) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sort by", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                SortOption.entries.forEach { opt ->
                    FilterChip(
                        selected = selectedSortOption == opt,
                        onClick = { selectedSortOption = opt },
                        label = { Text(opt.label, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (filteredEntries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) {
                        Icon(Icons.Outlined.MenuBook, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        if (showArchived) "No archived entries" else "No journal entries yet",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                        color = AppColors.current.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Start writing to capture your thoughts", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                    if (!showArchived) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { onNavigateToEditor(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Writing")
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                groupedEntries.forEach { (date, entries) ->
                    item {
                        DaySectionHeader(date)
                    }
                    items(entries, key = { it.id }) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onNavigateToEditor(entry.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(entry.id) },
                            onTogglePin = { viewModel.togglePin(entry.id) },
                            onArchive = { if (entry.isArchived) viewModel.unarchiveEntry(entry.id) else viewModel.archiveEntry(entry.id) },
                            onDuplicate = { viewModel.duplicateEntry(entry.id) },
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}
@Composable
private fun DaySectionHeader(date: LocalDate) {
    val today = LocalDate.now()
    val dayLabel = when {
        date == today -> "Today"
        date == today.minusDays(1) -> "Yesterday"
        ChronoUnit.DAYS.between(date, today) < 7 -> date.format(DateTimeFormatter.ofPattern("EEEE"))
        else -> date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }
    val isToday = date == today
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isToday) {
            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.textPrimary) {
                Text("TODAY", modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AppColors.current.background)
            }
        }
        Text(dayLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = if (isToday) AppColors.current.textPrimary else AppColors.current.textSecondary)
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.current.divider)
    }
}
@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTogglePin: () -> Unit,
    onArchive: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    val moodEmoji = when (entry.mood) {
        Mood.AMAZING -> "🤩"
        Mood.HAPPY -> "😊"
        Mood.GOOD -> "🙂"
        Mood.NEUTRAL -> "😐"
        Mood.SAD -> "😢"
        Mood.ANXIOUS -> "😰"
        Mood.ANGRY -> "😠"
        Mood.TIRED -> "😴"
        Mood.EXCITED -> "🤗"
        Mood.GRATEFUL -> "🙏"
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(14.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(shape = CircleShape, color = AppColors.current.surfaceVariant) {
                Text(moodEmoji, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (entry.isPinned) Icon(Icons.Filled.PushPin, null, modifier = Modifier.size(14.dp), tint = AppColors.current.textTertiary)
                    Text(
                        entry.title.ifEmpty { "Untitled" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppColors.current.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.isFavorite) Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(14.dp), tint = AppColors.current.error)
                }
                if (entry.content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.current.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        entry.createdAt.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.current.textTertiary
                    )
                    Text("${entry.wordCount} words", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    Text(entry.mood.label, style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    if (entry.tags.isNotEmpty()) {
                        entry.tags.take(2).forEach { tag ->
                            Surface(shape = RoundedCornerShape(4.dp), color = AppColors.current.surfaceVariant) {
                                Text("#$tag", modifier = Modifier.padding(4.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                            }
                        }
                        if (entry.tags.size > 2) Text("+${entry.tags.size - 2}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                        DropdownMenuItem(
                            text = { Text(if (entry.isFavorite) "Unfavorite" else "Favorite") },
                            onClick = { onToggleFavorite(); showMenu = false },
                            leadingIcon = { Icon(if (entry.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (entry.isPinned) "Unpin" else "Pin") },
                            onClick = { onTogglePin(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (entry.isArchived) "Unarchive" else "Archive") },
                            onClick = { onArchive(); showMenu = false },
                            leadingIcon = { Icon(if (entry.isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = { onDuplicate(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp)) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Delete", color = AppColors.current.error) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = AppColors.current.error, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }
    }
}
