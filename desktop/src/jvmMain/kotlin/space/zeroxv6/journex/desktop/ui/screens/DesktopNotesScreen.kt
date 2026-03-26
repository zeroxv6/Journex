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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.QuickNote
import java.time.format.DateTimeFormatter
@Composable
fun DesktopNotesScreen(dataStore: JsonDataStore, autoOpenDialog: Boolean = false, onConvertToEntry: (String) -> Unit) {
    val colors = AppColors.current
    val notes by dataStore.notes.collectAsState()
    var showAddDialog by remember { mutableStateOf(autoOpenDialog) }
    var editingNote by remember { mutableStateOf<QuickNote?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredNotes = if (searchQuery.isEmpty()) notes else notes.filter { it.content.contains(searchQuery, ignoreCase = true) }
    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Quick Notes", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Capture fleeting thoughts instantly", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("New Note", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = searchQuery, onValueChange = { searchQuery = it },
            placeholder = { Text("Search notes...", color = colors.textDisabled, style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = colors.textSecondary, modifier = Modifier.size(24.dp)) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Outlined.Clear, null, tint = colors.textSecondary) } },
            modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, unfocusedBorderColor = colors.border, focusedContainerColor = colors.cardBackground, unfocusedContainerColor = colors.cardBackground),
            textStyle = MaterialTheme.typography.bodyLarge, singleLine = true
        )
        Spacer(modifier = Modifier.height(28.dp))
        if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = colors.inputBackground) { Icon(Icons.Outlined.StickyNote2, null, modifier = Modifier.padding(32.dp).size(64.dp), tint = colors.textDisabled) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(if (searchQuery.isEmpty()) "No Quick Notes" else "No notes found", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(if (searchQuery.isEmpty()) "Capture fleeting thoughts instantly" else "Try different keywords", style = MaterialTheme.typography.bodyLarge, color = colors.textTertiary)
                }
            }
        } else {
            Text("${filteredNotes.size} ${if (filteredNotes.size == 1) "note" else "notes"}", style = MaterialTheme.typography.titleMedium, color = colors.textTertiary)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredNotes.sortedByDescending { it.createdAt }, key = { it.id }) { note ->
                    NoteCard(note, { editingNote = note }, { dataStore.deleteNote(note.id) }, { onConvertToEntry(note.content); dataStore.deleteNote(note.id) })
                }
            }
        }
    }
    if (showAddDialog) NoteDialog(null, { showAddDialog = false }) { content -> dataStore.saveNote(QuickNote(content = content)); showAddDialog = false }
    editingNote?.let { note -> NoteDialog(note, { editingNote = null }) { content -> dataStore.saveNote(note.copy(content = content)); editingNote = null } }
}
@Composable
private fun NoteCard(note: QuickNote, onEdit: () -> Unit, onDelete: () -> Unit, onConvert: () -> Unit) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, onClick = onEdit).hoverable(interactionSource), shape = RoundedCornerShape(16.dp), color = colors.cardBackground, border = BorderStroke(1.dp, if (isHovered) colors.borderFocused else colors.border)) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(note.content, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp), modifier = Modifier.weight(1f).padding(end = 16.dp))
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.MoreVert, null, tint = colors.textSecondary, modifier = Modifier.size(24.dp)) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = colors.background) {
                        DropdownMenuItem(text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) }, onClick = { onEdit(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Edit, null) })
                        DropdownMenuItem(text = { Text("Convert to Entry", style = MaterialTheme.typography.bodyLarge) }, onClick = { onConvert(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Article, null) })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(note.createdAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy • h:mm a")), style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
        }
    }
}
@Composable
private fun NoteDialog(note: QuickNote?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val colors = AppColors.current
    var content by remember { mutableStateOf(note?.content ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) "New Note" else "Edit Note", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            TextField(value = content, onValueChange = { content = it }, placeholder = { Text("What's on your mind?", style = MaterialTheme.typography.bodyLarge) }, modifier = Modifier.fillMaxWidth().height(180.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = colors.inputBackground, unfocusedContainerColor = colors.inputBackground, focusedIndicatorColor = colors.textPrimary, unfocusedIndicatorColor = Color.Transparent),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp), shape = RoundedCornerShape(12.dp))
        },
        confirmButton = { Button(onClick = { if (content.isNotEmpty()) onSave(content) }, enabled = content.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(44.dp)) { Text("Save", style = MaterialTheme.typography.titleMedium) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary, style = MaterialTheme.typography.titleMedium) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
