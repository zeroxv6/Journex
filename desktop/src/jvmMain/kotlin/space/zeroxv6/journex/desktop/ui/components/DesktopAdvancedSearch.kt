package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.shared.model.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesktopAdvancedSearch(
    currentFilter: NoteFilter,
    availableTags: List<String>,
    onApply: (NoteFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategories by remember { mutableStateOf(currentFilter.categories.toMutableList()) }
    var selectedTags by remember { mutableStateOf(currentFilter.tags.toMutableList()) }
    var selectedPriorities by remember { mutableStateOf(currentFilter.priorities.toMutableList()) }
    var hasAttachments by remember { mutableStateOf(currentFilter.hasAttachments) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(24.dp))
                Text("Advanced Search", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Categories", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NoteCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = cat in selectedCategories,
                            onClick = {
                                selectedCategories = if (cat in selectedCategories) (selectedCategories - cat).toMutableList()
                                else (selectedCategories + cat).toMutableList()
                            },
                            label = { Text(cat.label, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.textPrimary,
                                selectedLabelColor = colors.background
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Text("Priority", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NotePriority.entries.forEach { pri ->
                        FilterChip(
                            selected = pri in selectedPriorities,
                            onClick = {
                                selectedPriorities = if (pri in selectedPriorities) (selectedPriorities - pri).toMutableList()
                                else (selectedPriorities + pri).toMutableList()
                            },
                            label = { Text(pri.label, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.textPrimary,
                                selectedLabelColor = colors.background
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                if (availableTags.isNotEmpty()) {
                    Text("Tags", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableTags.forEach { tag ->
                            FilterChip(
                                selected = tag in selectedTags,
                                onClick = {
                                    selectedTags = if (tag in selectedTags) (selectedTags - tag).toMutableList()
                                    else (selectedTags + tag).toMutableList()
                                },
                                label = { Text("#$tag", style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.textPrimary,
                                    selectedLabelColor = colors.background
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Has Attachments", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(null to "Any", true to "Yes", false to "No").forEach { (value, label) ->
                            FilterChip(
                                selected = hasAttachments == value,
                                onClick = { hasAttachments = value },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.textPrimary,
                                    selectedLabelColor = colors.background
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }
                if (selectedCategories.isNotEmpty() || selectedTags.isNotEmpty() || selectedPriorities.isNotEmpty() || hasAttachments != null) {
                    TextButton(onClick = {
                        selectedCategories = mutableListOf()
                        selectedTags = mutableListOf()
                        selectedPriorities = mutableListOf()
                        hasAttachments = null
                    }) {
                        Text("Clear All Filters", color = colors.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(NoteFilter(
                        categories = selectedCategories,
                        tags = selectedTags,
                        priorities = selectedPriorities,
                        hasAttachments = hasAttachments
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)
            ) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
