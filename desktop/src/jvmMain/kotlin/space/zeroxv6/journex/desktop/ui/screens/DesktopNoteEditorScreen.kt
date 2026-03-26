package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.ui.components.DesktopDrawingCanvas
import androidx.compose.ui.graphics.toArgb
import space.zeroxv6.journex.desktop.viewmodel.NoteViewModel
import space.zeroxv6.journex.shared.model.*
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DesktopNoteEditorScreen(
    viewModel: NoteViewModel,
    noteId: String?,
    onNavigateBack: () -> Unit
) {
    var note by remember { mutableStateOf<FullNote?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NoteCategory.PERSONAL) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showChecklistInput by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    var newChecklistText by remember { mutableStateOf("") }
    var newTagText by remember { mutableStateOf("") }
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
            viewModel.currentNote.value?.let { loaded ->
                note = loaded
                title = loaded.title
                content = loaded.content
                selectedCategory = loaded.category
            }
        } else {
            val newNote = viewModel.createNewNote()
            note = newNote
            title = ""
            content = ""
            selectedCategory = NoteCategory.PERSONAL
        }
    }
    LaunchedEffect(title, content, selectedCategory) {
        note?.let { n ->
            val words = content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            note = n.copy(
                title = title,
                content = content,
                category = selectedCategory,
                updatedAt = LocalDateTime.now(),
                wordCount = words.size,
                characterCount = content.length,
                readingTime = (words.size / 200).coerceAtLeast(1),
                plainTextContent = content
            )
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background)) {
        Surface(
            color = AppColors.current.background,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = {
                    note?.let { viewModel.saveNoteAndReturn(it) }
                    onNavigateBack()
                }) {
                    Icon(Icons.Outlined.ArrowBack, "Back", modifier = Modifier.size(24.dp))
                }
                Text(
                    if (noteId == null) "New Note" else "Edit Note",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AppColors.current.textPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                note?.let { n ->
                    Text("${n.wordCount} words · ${n.characterCount} chars", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showExportDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.FileDownload, "Export", modifier = Modifier.size(20.dp), tint = AppColors.current.textSecondary)
                }
                Button(
                    onClick = {
                        note?.let { viewModel.saveNoteAndReturn(it) }
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save")
                }
            }
        }
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(40.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Note title...", style = MaterialTheme.typography.headlineSmall, color = AppColors.current.textDisabled) },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = AppColors.current.textPrimary),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.border,
                        unfocusedBorderColor = AppColors.current.divider,
                        focusedContainerColor = AppColors.current.background,
                        unfocusedContainerColor = AppColors.current.background
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                    placeholder = { Text("Start writing...", color = AppColors.current.textDisabled) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = AppColors.current.textPrimary, lineHeight = 28.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.current.border,
                        unfocusedBorderColor = AppColors.current.divider,
                        focusedContainerColor = AppColors.current.background,
                        unfocusedContainerColor = AppColors.current.background
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                note?.let { n ->
                    if (n.checklistItems.isNotEmpty() || showChecklistInput) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Checklist", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        n.checklistItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = {
                                        val updated = n.checklistItems.map { if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it }
                                        note = n.copy(checklistItems = updated)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.current.textPrimary)
                                )
                                Text(
                                    item.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                                    color = if (item.isChecked) AppColors.current.textDisabled else AppColors.current.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val updated = n.checklistItems.filter { it.id != item.id }
                                    note = n.copy(checklistItems = updated)
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp), tint = AppColors.current.textTertiary)
                                }
                            }
                        }
                    }
                    if (showChecklistInput) {
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newChecklistText,
                                onValueChange = { newChecklistText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("New item...") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.current.textPrimary),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Button(
                                onClick = {
                                    if (newChecklistText.isNotBlank()) {
                                        val item = ChecklistItem(text = newChecklistText.trim(), order = n.checklistItems.size)
                                        note = n.copy(checklistItems = n.checklistItems + item)
                                        newChecklistText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Add") }
                        }
                    }
                }
                note?.let { n ->
                    if (n.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Attachments (${n.attachments.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        n.attachments.forEach { att ->
                            Surface(
                                onClick = {
                                    try {
                                        val file = java.io.File(att.uri)
                                        if (file.exists()) {
                                            java.awt.Desktop.getDesktop().open(file)
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.current.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Outlined.AttachFile, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textTertiary)
                                    Text(att.name, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textPrimary, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        note = n.copy(attachments = n.attachments.filter { it.id != att.id })
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp), tint = AppColors.current.textTertiary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(
                color = AppColors.current.surface,
                modifier = Modifier.width(280.dp).fillMaxHeight(),
                border = BorderStroke(1.dp, AppColors.current.divider)
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text("Properties", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textPrimary)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        onClick = { showCategoryPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.current.inputBackground,
                        border = BorderStroke(1.dp, AppColors.current.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedCategory.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tags", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                        IconButton(onClick = { showTagDialog = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp), tint = AppColors.current.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    note?.let { n ->
                        if (n.tags.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                n.tags.forEach { tag ->
                                    Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.surfaceVariant) {
                                        Row(modifier = Modifier.padding(6.dp, 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("#$tag", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textSecondary)
                                            Icon(
                                                Icons.Outlined.Close, null,
                                                modifier = Modifier.size(12.dp).clickable { note = n.copy(tags = n.tags - tag) },
                                                tint = AppColors.current.textTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("No tags", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textDisabled)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Actions", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SidePanelButton(Icons.Outlined.Checklist, "Add Checklist") { showChecklistInput = !showChecklistInput }
                        SidePanelButton(Icons.Outlined.AttachFile, "Add Attachment") {
                            val fileDialog = java.awt.FileDialog(java.awt.Frame(), "Select File", java.awt.FileDialog.LOAD)
                            fileDialog.isVisible = true
                            fileDialog.file?.let { fileName ->
                                val fullPath = "${fileDialog.directory}$fileName"
                                note?.let { n ->
                                    val att = NoteAttachment(name = fileName, uri = fullPath, type = AttachmentType.DOCUMENT)
                                    note = n.copy(attachments = n.attachments + att)
                                }
                            }
                        }
                        SidePanelButton(Icons.Outlined.Brush, "Add Drawing") { showDrawingDialog = true }
                        SidePanelButton(Icons.Outlined.FileDownload, "Export") { showExportDialog = true }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Status", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                    Spacer(modifier = Modifier.height(6.dp))
                    note?.let { n ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            NoteStatus.entries.filter { it != NoteStatus.ARCHIVED }.forEach { status ->
                                FilterChip(
                                    selected = n.status == status,
                                    onClick = { note = n.copy(status = status) },
                                    label = { Text(status.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.current.textPrimary,
                                        selectedLabelColor = AppColors.current.background
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    note?.let { n ->
                        Text("Info", style = MaterialTheme.typography.labelMedium, color = AppColors.current.textTertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Created: ${n.createdAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"))}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                        Text("Updated: ${n.updatedAt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"))}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                        Text("Words: ${n.wordCount} · Chars: ${n.characterCount}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                        Text("Version: ${n.version}", style = MaterialTheme.typography.labelSmall, color = AppColors.current.textDisabled)
                    }
                }
            }
        }
    }
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(NoteCategory.entries.toList()) { cat ->
                        Surface(
                            onClick = {
                                selectedCategory = cat
                                note = note?.copy(category = cat)
                                showCategoryPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat) AppColors.current.selectedBackground else AppColors.current.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cat.label, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedCategory == cat) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showCategoryPicker = false }) { Text("Cancel", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    placeholder = { Text("Tag name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.current.textPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            note?.let { n -> note = n.copy(tags = n.tags + newTagText.trim()) }
                            newTagText = ""
                            showTagDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary)
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showTagDialog = false }) { Text("Cancel", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Note", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Copy as Text" to Icons.Outlined.ContentCopy, "Copy as Markdown" to Icons.Outlined.Code).forEach { (label, icon) ->
                        Surface(
                            onClick = {
                                note?.let { n ->
                                    val text = if (label.contains("Markdown")) {
                                        buildString {
                                            appendLine("# ${n.title}")
                                            appendLine()
                                            appendLine("**Category:** ${n.category.label}")
                                            if (n.tags.isNotEmpty()) appendLine("**Tags:** ${n.tags.joinToString(", ") { "#$it" }}")
                                            appendLine()
                                            appendLine(n.content)
                                        }
                                    } else {
                                        viewModel.exportNoteAsText(n.id)
                                    }
                                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                    clipboard.setContents(StringSelection(text), null)
                                }
                                showExportDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = AppColors.current.cardBackground,
                            border = BorderStroke(1.dp, AppColors.current.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(icon, null, modifier = Modifier.size(20.dp), tint = AppColors.current.textSecondary)
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Close", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showDrawingDialog) {
        DesktopDrawingCanvas(
            onSave = { paths ->
                showDrawingDialog = false
                if (paths.isNotEmpty()) {
                    try {
                        val width = 800
                        val height = 600
                        @Suppress("UseJBColor")
                        val image = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB)
                        val g2d = image.createGraphics()
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
                        g2d.color = java.awt.Color.WHITE
                        g2d.fillRect(0, 0, width, height)
                        paths.forEach { path ->
                            if (path.points.size >= 2) {
                                val awtPath = java.awt.geom.Path2D.Float()
                                awtPath.moveTo(path.points.first().x.toDouble(), path.points.first().y.toDouble())
                                for (i in 1 until path.points.size) {
                                    awtPath.lineTo(path.points[i].x.toDouble(), path.points[i].y.toDouble())
                                }
                                g2d.color = java.awt.Color(path.color.toArgb(), true)
                                g2d.stroke = java.awt.BasicStroke(path.strokeWidth, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND)
                                g2d.draw(awtPath)
                            }
                        }
                        g2d.dispose()
                        val tempFile = java.io.File.createTempFile("drawing_", ".png")
                        javax.imageio.ImageIO.write(image, "png", tempFile)
                        note?.let { n ->
                            val att = NoteAttachment(name = tempFile.name, uri = tempFile.absolutePath, type = AttachmentType.IMAGE)
                            note = n.copy(attachments = n.attachments + att)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onDismiss = { showDrawingDialog = false }
        )
    }
}
@Composable
private fun SidePanelButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = AppColors.current.inputBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary)
        }
    }
}
