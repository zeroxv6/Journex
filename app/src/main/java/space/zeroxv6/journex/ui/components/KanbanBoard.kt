package space.zeroxv6.journex.ui.components
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.Note
import space.zeroxv6.journex.model.NoteStatus
import java.time.format.DateTimeFormatter
@Composable
fun KanbanBoardView(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onStatusChange: (Note, NoteStatus) -> Unit
) {
    val notesByStatus = notes.groupBy { it.status }
    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(NoteStatus.entries.toList()) { status ->
            KanbanColumn(
                status = status,
                notes = notesByStatus[status] ?: emptyList(),
                onNoteClick = onNoteClick,
                onStatusChange = onStatusChange
            )
        }
    }
}
@Composable
fun KanbanColumn(
    status: NoteStatus,
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onStatusChange: (Note, NoteStatus) -> Unit
) {
    val (columnColor, columnIcon) = when (status) {
        NoteStatus.DRAFT -> Pair(MaterialTheme.colorScheme.secondaryContainer, Icons.Filled.Edit)
        NoteStatus.ACTIVE -> Pair(MaterialTheme.colorScheme.primaryContainer, Icons.Filled.PlayArrow)
        NoteStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiaryContainer, Icons.Filled.CheckCircle)
        NoteStatus.ARCHIVED -> Pair(MaterialTheme.colorScheme.surfaceVariant, Icons.Filled.Archive)
        NoteStatus.DELETED -> Pair(MaterialTheme.colorScheme.errorContainer, Icons.Filled.Delete)
    }
    Card(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = columnColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        columnIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = status.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = notes.size.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    KanbanCard(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onStatusChange = { newStatus ->
                            onStatusChange(note, newStatus)
                        }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanCard(
    note: Note,
    onClick: () -> Unit,
    onStatusChange: (NoteStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = try {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(note.color))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.isPinned) {
                        Icon(
                            Icons.Filled.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (note.priority != space.zeroxv6.journex.model.NotePriority.NONE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = try {
                                androidx.compose.ui.graphics.Color(
                                    android.graphics.Color.parseColor(note.priority.color)
                                )
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.error
                            }
                        ) {
                            Text(
                                text = note.priority.label,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
                Box {
                    IconButton(
                        onClick = { showStatusMenu = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        Text(
                            "Move to:",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        NoteStatus.entries.forEach { status ->
                            if (status != note.status) {
                                DropdownMenuItem(
                                    text = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        onStatusChange(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.plainTextContent.ifEmpty { note.content },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.tags.isNotEmpty()) {
                    Text(
                        text = note.tags.take(2).joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = note.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (note.checklistItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val completed = note.checklistItems.count { it.isChecked }
                val total = note.checklistItems.size
                val progress = if (total > 0) completed.toFloat() / total else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp),
                    )
                    Text(
                        text = "$completed/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (note.attachments.isNotEmpty() || note.voiceNotes.isNotEmpty() || note.drawings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (note.attachments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = note.attachments.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (note.voiceNotes.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = note.voiceNotes.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
