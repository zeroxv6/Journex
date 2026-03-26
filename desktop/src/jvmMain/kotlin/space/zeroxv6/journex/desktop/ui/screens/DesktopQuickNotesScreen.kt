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
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.QuickNote
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
@Composable
fun DesktopQuickNotesScreen(
    dataStore: JsonDataStore,
    onConvertToEntry: (String) -> Unit
) {
    val notes by dataStore.notes.collectAsState()
    var newNoteText by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<QuickNote?>(null) }
    val sortedNotes = notes.sortedByDescending { it.createdAt }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Quick Notes", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp), color = AppColors.current.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${notes.size} notes", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppColors.current.cardBackground,
            border = BorderStroke(1.dp, AppColors.current.border)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Jot down a quick note...", color = AppColors.current.textDisabled) },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.textPrimary,
                        unfocusedBorderColor = AppColors.current.border,
                        focusedContainerColor = AppColors.current.inputBackground,
                        unfocusedContainerColor = AppColors.current.inputBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            dataStore.saveNote(QuickNote(id = UUID.randomUUID().toString(), content = newNoteText.trim()))
                            newNoteText = ""
                        }
                    },
                    enabled = newNoteText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (sortedNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) {
                        Icon(Icons.Outlined.StickyNote2, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No quick notes yet", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium), color = AppColors.current.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Jot down thoughts, ideas, or anything quick", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sortedNotes, key = { it.id }) { note ->
                    QuickNoteCard(
                        note = note,
                        onEdit = { editingNote = note },
                        onDelete = { dataStore.deleteNote(note.id) },
                        onConvert = { onConvertToEntry(note.content) }
                    )
                }
            }
        }
    }
    editingNote?.let { note ->
        QuickNoteEditDialog(
            note = note,
            onDismiss = { editingNote = null },
            onSave = { content ->
                dataStore.saveNote(note.copy(content = content))
                editingNote = null
            }
        )
    }
}
@Composable
private fun QuickNoteCard(note: QuickNote, onEdit: () -> Unit, onDelete: () -> Unit, onConvert: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource).clickable(onClick = onEdit),
        shape = RoundedCornerShape(14.dp),
        color = if (isHovered) AppColors.current.cardBackgroundHover else AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.StickyNote2, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.current.textPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    note.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.current.textTertiary
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) })
                    DropdownMenuItem(text = { Text("Convert to Journal Entry") }, onClick = { onConvert(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Article, null, modifier = Modifier.size(18.dp)) })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(text = { Text("Delete", color = AppColors.current.error) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = AppColors.current.error, modifier = Modifier.size(18.dp)) })
                }
            }
        }
    }
}
@Composable
private fun QuickNoteEditDialog(note: QuickNote, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var content by remember { mutableStateOf(note.content) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary) },
        text = {
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
            )
        },
        confirmButton = { Button(onClick = { onSave(content) }, enabled = content.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
