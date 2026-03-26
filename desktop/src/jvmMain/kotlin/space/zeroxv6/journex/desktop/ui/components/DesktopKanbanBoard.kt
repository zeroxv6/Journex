package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.shared.model.*
import java.time.format.DateTimeFormatter
@Composable
fun DesktopKanbanBoard(
    notes: List<FullNote>,
    onNoteClick: (FullNote) -> Unit,
    onStatusChange: (String, NoteStatus) -> Unit
) {
    val columns = listOf(
        NoteStatus.DRAFT to "Draft",
        NoteStatus.ACTIVE to "Active",
        NoteStatus.COMPLETED to "Completed"
    )
    Row(
        modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        columns.forEach { (status, label) ->
            val columnNotes = notes.filter { it.status == status }
            Surface(
                modifier = Modifier.width(320.dp).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.current.surfaceVariant,
                border = BorderStroke(1.dp, AppColors.current.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val icon = when (status) {
                                NoteStatus.DRAFT -> Icons.Outlined.EditNote
                                NoteStatus.ACTIVE -> Icons.Outlined.PlayCircleOutline
                                NoteStatus.COMPLETED -> Icons.Outlined.CheckCircleOutline
                                else -> Icons.Outlined.Circle
                            }
                            Icon(icon, null, modifier = Modifier.size(20.dp), tint = AppColors.current.textSecondary)
                            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textPrimary)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.current.background) {
                            Text("${columnNotes.size}", modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (columnNotes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No notes", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textDisabled)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(columnNotes, key = { it.id }) { note ->
                                KanbanNoteCard(
                                    note = note,
                                    onClick = { onNoteClick(note) },
                                    availableStatuses = columns.map { it.first }.filter { it != status },
                                    onStatusChange = { newStatus -> onStatusChange(note.id, newStatus) }
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
private fun KanbanNoteCard(
    note: FullNote,
    onClick: () -> Unit,
    availableStatuses: List<NoteStatus>,
    onStatusChange: (NoteStatus) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showStatusMenu by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(12.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(4.dp), color = AppColors.current.surfaceVariant) {
                    Text(note.category.label, modifier = Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                }
                Box {
                    IconButton(onClick = { showStatusMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreHoriz, null, modifier = Modifier.size(16.dp), tint = AppColors.current.textTertiary)
                    }
                    DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }, containerColor = AppColors.current.background) {
                        Text("Move to", modifier = Modifier.padding(12.dp, 4.dp), style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                        availableStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = { onStatusChange(status); showStatusMenu = false }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                note.title.ifEmpty { "Untitled Note" },
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = AppColors.current.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.content, style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (note.checklistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Checklist, null, modifier = Modifier.size(14.dp), tint = AppColors.current.textTertiary)
                    Text("${note.checklistItems.count { it.isChecked }}/${note.checklistItems.size}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textTertiary)
                }
            }
            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.take(2).forEach { tag ->
                        Surface(shape = RoundedCornerShape(4.dp), color = AppColors.current.surfaceVariant) {
                            Text("#$tag", modifier = Modifier.padding(4.dp, 2.dp), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(note.updatedAt.format(DateTimeFormatter.ofPattern("MMM d")), style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
        }
    }
}
