package space.zeroxv6.journex.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.*
import java.time.LocalDateTime
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    currentFilter: NoteFilter,
    allTags: List<String>,
    onFilterApplied: (NoteFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(currentFilter.searchQuery) }
    var selectedCategories by remember { mutableStateOf(currentFilter.categories) }
    var selectedTags by remember { mutableStateOf(currentFilter.tags) }
    var selectedPriorities by remember { mutableStateOf(currentFilter.priorities) }
    var hasAttachments by remember { mutableStateOf(currentFilter.hasAttachments) }
    var hasReminders by remember { mutableStateOf(currentFilter.hasReminders) }
    var isLocked by remember { mutableStateOf(currentFilter.isLocked) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf(currentFilter.dateRange) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Advanced Search",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            searchQuery = ""
                            selectedCategories = emptyList()
                            selectedTags = emptyList()
                            selectedPriorities = emptyList()
                            hasAttachments = null
                            hasReminders = null
                            isLocked = null
                            dateRange = null
                        }
                    ) {
                        Text("Clear All")
                    }
                }
            }
            item {
                Column {
                    Text(
                        "Search Text",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search in title and content...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            item {
                Column {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NoteCategory.entries.toList()) { category ->
                            FilterChip(
                                selected = selectedCategories.contains(category),
                                onClick = {
                                    selectedCategories = if (selectedCategories.contains(category)) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                },
                                label = { Text(category.label) }
                            )
                        }
                    }
                }
            }
            if (allTags.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            "Tags",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allTags) { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag),
                                    onClick = {
                                        selectedTags = if (selectedTags.contains(tag)) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    },
                                    label = { Text("#$tag") }
                                )
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    Text(
                        "Priority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(NotePriority.entries.toList()) { priority ->
                            FilterChip(
                                selected = selectedPriorities.contains(priority),
                                onClick = {
                                    selectedPriorities = if (selectedPriorities.contains(priority)) {
                                        selectedPriorities - priority
                                    } else {
                                        selectedPriorities + priority
                                    }
                                },
                                label = { Text(priority.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = try {
                                        androidx.compose.ui.graphics.Color(
                                            android.graphics.Color.parseColor(priority.color)
                                        ).copy(alpha = 0.3f)
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                            )
                        }
                    }
                }
            }
            item {
                Column {
                    Text(
                        "Additional Filters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = hasAttachments == true,
                            onClick = {
                                hasAttachments = when (hasAttachments) {
                                    null -> true
                                    true -> false
                                    false -> null
                                }
                            },
                            label = { Text("Has Attachments") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        FilterChip(
                            selected = hasReminders == true,
                            onClick = {
                                hasReminders = when (hasReminders) {
                                    null -> true
                                    true -> false
                                    false -> null
                                }
                            },
                            label = { Text("Has Reminders") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterChip(
                        selected = isLocked == true,
                        onClick = {
                            isLocked = when (isLocked) {
                                null -> true
                                true -> false
                                false -> null
                            }
                        },
                        label = { Text("Locked Notes") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            item {
                Column {
                    Text(
                        "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (dateRange != null) {
                                "Custom Range Selected"
                            } else {
                                "Select Date Range"
                            }
                        )
                    }
                    if (dateRange != null) {
                        TextButton(
                            onClick = { dateRange = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Date Range")
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onFilterApplied(
                            NoteFilter(
                                searchQuery = searchQuery,
                                categories = selectedCategories,
                                tags = selectedTags,
                                priorities = selectedPriorities,
                                hasAttachments = hasAttachments,
                                hasReminders = hasReminders,
                                isLocked = isLocked,
                                dateRange = dateRange
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Filters")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
fun applyNoteFilter(notes: List<Note>, filter: NoteFilter): List<Note> {
    var filtered = notes
    if (filter.searchQuery.isNotEmpty()) {
        filtered = filtered.filter { note ->
            note.title.contains(filter.searchQuery, ignoreCase = true) ||
            note.content.contains(filter.searchQuery, ignoreCase = true) ||
            note.plainTextContent.contains(filter.searchQuery, ignoreCase = true) ||
            note.tags.any { it.contains(filter.searchQuery, ignoreCase = true) }
        }
    }
    if (filter.categories.isNotEmpty()) {
        filtered = filtered.filter { it.category in filter.categories }
    }
    if (filter.tags.isNotEmpty()) {
        filtered = filtered.filter { note ->
            filter.tags.any { tag -> note.tags.contains(tag) }
        }
    }
    if (filter.priorities.isNotEmpty()) {
        filtered = filtered.filter { it.priority in filter.priorities }
    }
    filter.hasAttachments?.let { hasAttach ->
        filtered = filtered.filter { note ->
            (note.attachments.isNotEmpty() || 
             note.voiceNotes.isNotEmpty() || 
             note.drawings.isNotEmpty()) == hasAttach
        }
    }
    filter.hasReminders?.let { hasRem ->
        filtered = filtered.filter { (it.reminder != null) == hasRem }
    }
    filter.isLocked?.let { locked ->
        filtered = filtered.filter { it.isLocked == locked }
    }
    filter.dateRange?.let { (start, end) ->
        filtered = filtered.filter { note ->
            note.createdAt.isAfter(start) && note.createdAt.isBefore(end)
        }
    }
    return filtered
}
