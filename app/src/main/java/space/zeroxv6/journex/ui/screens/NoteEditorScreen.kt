package space.zeroxv6.journex.ui.screens
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import space.zeroxv6.journex.model.*
import space.zeroxv6.journex.viewmodel.NoteViewModel
import java.time.format.DateTimeFormatter
import java.util.UUID
private val BackgroundColor = Color(0xFFF8F8F8)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFF0A0A0A)
private val SecondaryColor = Color(0xFF6B6B6B)
private val TertiaryColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE8E8E8)
private val DividerColorr = Color(0xFFF0F0F0)
private val AccentColor = Color(0xFF1F1F1F)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    noteId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var showChecklistDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDrawingCanvas by remember { mutableStateOf(false) }
    var showVoiceRecorder by remember { mutableStateOf(false) }
    var showVersionHistory by remember { mutableStateOf(false) }
    var showTemplateSelector by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val imagePermissionState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
            }
            val attachment = NoteAttachment(
                type = AttachmentType.IMAGE,
                uri = it.toString(),
                name = "Image_${System.currentTimeMillis()}.jpg"
            )
            viewModel.addAttachment(attachment)
        }
    }
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.createNewNote()
        }
    }
    val note = viewModel.currentNote ?: return
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 0.dp
            ) {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (noteId == null) "New Note" else "Edit Note",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    letterSpacing = (-0.3).sp
                                ),
                                fontWeight = FontWeight.Normal,
                                color = PrimaryColor
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (note.title.isNotEmpty() || note.content.isNotEmpty()) {
                                    showDiscardDialog = true
                                } else {
                                    onNavigateBack()
                                }
                            }) {
                                Icon(
                                    Icons.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = PrimaryColor
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                viewModel.updateCurrentNote(note.copy(isPinned = !note.isPinned))
                            }) {
                                Icon(
                                    if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (note.isPinned) AccentColor else SecondaryColor
                                )
                            }
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(
                                        Icons.Outlined.MoreVert,
                                        contentDescription = "More",
                                        tint = SecondaryColor
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Export", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                        onClick = {
                                            showExportMenu = true
                                            showMoreMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Outlined.Share, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (note.isFavorite) "Remove from Favorites" else "Add to Favorites", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                        onClick = {
                                            viewModel.updateCurrentNote(note.copy(isFavorite = !note.isFavorite))
                                            showMoreMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                null
                                            )
                                        }
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.saveNote()
                                    onNavigateBack()
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Save",
                                    tint = AccentColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SurfaceColor
                        ),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )
                    HorizontalDivider(
                        color = DividerColorr,
                        thickness = 1.dp
                    )
                    FormattingToolbar(
                        note = note,
                        onCategoryClick = { showCategoryPicker = true },
                        onTagClick = { showTagDialog = true },
                        onChecklistClick = { showChecklistDialog = true },
                        onImageClick = {
                            if (imagePermissionState.status.isGranted) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                imagePermissionState.launchPermissionRequest()
                            }
                        },
                        onDrawingClick = { showDrawingCanvas = true },
                        onVoiceClick = { showVoiceRecorder = true },
                        onTemplateClick = { showTemplateSelector = true }
                    )
                    HorizontalDivider(
                        color = DividerColorr,
                        thickness = 1.dp
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                TextField(
                    value = note.title,
                    onValueChange = { viewModel.updateCurrentNote(note.copy(title = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    placeholder = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = TertiaryColor
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp,
                        color = PrimaryColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = false,
                    maxLines = 3
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { showCategoryPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(
                            text = note.category.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = SecondaryColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = "${note.wordCount} words",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = TertiaryColor
                    )
                    if (note.readingTime > 0) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = TertiaryColor
                        )
                        Text(
                            text = "${note.readingTime} min read",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = TertiaryColor
                        )
                    }
                }
            }
            if (note.tags.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(note.tags) { tag ->
                            NoteEditorTagChip(
                                tag = tag,
                                onRemove = {
                                    viewModel.updateCurrentNote(
                                        note.copy(tags = note.tags - tag)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (note.checklistItems.isNotEmpty()) {
                items(note.checklistItems) { item ->
                    ChecklistItemRow(
                        item = item,
                        onCheckedChange = { checked ->
                            viewModel.updateChecklistItem(item.id, checked)
                        },
                        onDelete = {
                            viewModel.removeChecklistItem(item.id)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            item {
                TextField(
                    value = note.content,
                    onValueChange = { viewModel.updateCurrentNote(note.copy(content = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 400.dp)
                        .padding(horizontal = 20.dp),
                    placeholder = {
                        Text(
                            "Start writing...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 26.sp
                            ),
                            color = TertiaryColor
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 26.sp,
                        color = PrimaryColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            if (note.attachments.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "ATTACHMENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = TertiaryColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(note.attachments) { attachment ->
                    AttachmentCard(
                        attachment = attachment,
                        onDelete = { viewModel.removeAttachment(attachment.id) },
                        onClick = {
                            if (attachment.type == AttachmentType.IMAGE) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    val uri = if (attachment.uri.startsWith("/")) {
                                        androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            java.io.File(attachment.uri)
                                        )
                                    } else {
                                        android.net.Uri.parse(attachment.uri)
                                    }
                                    setDataAndType(uri, "image/*")
                                    flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
            }
            if (note.voiceNotes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "VOICE NOTES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = TertiaryColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(note.voiceNotes) { voiceNote ->
                    VoiceNoteCard(
                        voiceNote = voiceNote,
                        onDelete = {
                            viewModel.updateCurrentNote(note.copy(
                                voiceNotes = note.voiceNotes.filter { it.id != voiceNote.id }
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
            if (note.drawings.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "DRAWINGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = TertiaryColor,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(note.drawings) { drawing ->
                    DrawingCard(
                        drawing = drawing,
                        onDelete = {
                            viewModel.updateCurrentNote(note.copy(
                                drawings = note.drawings.filter { it.id != drawing.id }
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Created ${note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = TertiaryColor
                    )
                    if (note.updatedAt != note.createdAt) {
                        Text(
                            text = "Updated ${note.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = TertiaryColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    if (showCategoryPicker) {
        CategoryPickerDialog(
            currentCategory = note.category,
            onDismiss = { showCategoryPicker = false },
            onSelect = { category ->
                viewModel.updateCurrentNote(note.copy(category = category))
                showCategoryPicker = false
            }
        )
    }
    if (showTagDialog) {
        AddTagDialog(
            existingTags = viewModel.getAllTags(),
            onDismiss = { showTagDialog = false },
            onAdd = { tag ->
                if (!note.tags.contains(tag)) {
                    viewModel.updateCurrentNote(note.copy(tags = note.tags + tag))
                }
                showTagDialog = false
            }
        )
    }
    if (showChecklistDialog) {
        AddChecklistDialog(
            onDismiss = { showChecklistDialog = false },
            onAdd = { text ->
                val item = ChecklistItem(
                    text = text,
                    order = note.checklistItems.size
                )
                viewModel.addChecklistItem(item)
                showChecklistDialog = false
            }
        )
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    "Unsaved Changes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "Do you want to save your changes?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveNote()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor
                    )
                ) {
                    Text("Save", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Cancel", color = SecondaryColor, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    TextButton(
                        onClick = {
                            viewModel.clearCurrentNote()
                            onNavigateBack()
                        }
                    ) {
                        Text("Discard", color = SecondaryColor, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        )
    }
    if (showDrawingCanvas) {
        Surface(modifier = Modifier.fillMaxSize()) {
            space.zeroxv6.journex.ui.components.DrawingCanvas(
                modifier = Modifier.fillMaxSize(),
                onSave = { paths, fileName ->
                    try {
                        val drawingsDir = java.io.File(context.filesDir, "note_drawings")
                        val drawingFile = java.io.File(drawingsDir, fileName)
                        val drawing = Drawing(
                            id = UUID.randomUUID().toString(),
                            imageData = drawingFile.absolutePath,
                            strokes = paths.map { drawPath ->
                                DrawingStroke(
                                    points = emptyList(),
                                    color = drawPath.color.toString(),
                                    width = drawPath.strokeWidth,
                                    tool = DrawingTool.PEN
                                )
                            }
                        )
                        viewModel.updateCurrentNote(note.copy(
                            drawings = note.drawings + drawing
                        ))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(
                            context,
                            "Failed to save drawing",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                    showDrawingCanvas = false
                },
                onCancel = { showDrawingCanvas = false }
            )
        }
    }
    if (showVoiceRecorder) {
        space.zeroxv6.journex.ui.components.VoiceRecorderDialog(
            onDismiss = { showVoiceRecorder = false },
            onSave = { filePath, duration, waveform ->
                val voiceNote = space.zeroxv6.journex.model.VoiceNote(
                    filePath = filePath,
                    duration = duration,
                    waveformData = waveform
                )
                viewModel.updateCurrentNote(note.copy(
                    voiceNotes = note.voiceNotes + voiceNote
                ))
                showVoiceRecorder = false
            }
        )
    }
    if (showVersionHistory) {
        space.zeroxv6.journex.ui.components.VersionHistoryDialog(
            versions = note.versionHistory,
            currentContent = note.content,
            onRestore = { version ->
                viewModel.updateCurrentNote(note.copy(
                    content = version.content,
                    versionHistory = note.versionHistory + NoteVersion(
                        content = note.content,
                        timestamp = java.time.LocalDateTime.now(),
                        changeDescription = "Restored from version"
                    )
                ))
            },
            onDismiss = { showVersionHistory = false }
        )
    }
    if (showTemplateSelector) {
        space.zeroxv6.journex.ui.components.TemplateSelector(
            onTemplateSelected = { template ->
                viewModel.updateCurrentNote(note.copy(
                    content = template.content,
                    category = template.category
                ))
            },
            onDismiss = { showTemplateSelector = false }
        )
    }
    if (showExportMenu) {
        ExportDialog(
            note = note,
            context = context,
            onDismiss = { showExportMenu = false }
        )
    }
}
@Composable
fun FormattingToolbar(
    note: Note,
    onCategoryClick: () -> Unit,
    onTagClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onImageClick: () -> Unit,
    onDrawingClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onTemplateClick: () -> Unit
) {
    Surface(
        color = SurfaceColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                IconButton(
                    onClick = onCategoryClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Category,
                        contentDescription = "Category",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onTagClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Tag,
                        contentDescription = "Tags",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onChecklistClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.CheckBox,
                        contentDescription = "Checklist",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onImageClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        contentDescription = "Image",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onDrawingClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Draw,
                        contentDescription = "Drawing",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Mic,
                        contentDescription = "Voice",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
            item {
                IconButton(
                    onClick = onTemplateClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = "Template",
                        modifier = Modifier.size(20.dp),
                        tint = SecondaryColor
                    )
                }
            }
        }
    }
}
@Composable
private fun NoteEditorTagChip(
    tag: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BackgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp
                ),
                color = SecondaryColor
            )
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                tint = TertiaryColor
            )
        }
    }
}
@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentColor,
                    uncheckedColor = BorderColor
                )
            )
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp
                ),
                modifier = Modifier.weight(1f),
                textDecoration = if (item.isChecked) {
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                } else null,
                color = if (item.isChecked) TertiaryColor else PrimaryColor
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = TertiaryColor
                )
            }
        }
    }
}
@Composable
fun AttachmentCard(
    attachment: NoteAttachment,
    onDelete: () -> Unit,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (attachment.type) {
                AttachmentType.IMAGE -> {
                    AsyncImage(
                        model = if (attachment.uri.startsWith("/")) {
                            java.io.File(attachment.uri)
                        } else {
                            Uri.parse(attachment.uri)
                        },
                        contentDescription = attachment.name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundColor,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Outlined.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = SecondaryColor
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor,
                    maxLines = 1
                )
                Text(
                    text = attachment.type.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TertiaryColor
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = SecondaryColor
                )
            }
        }
    }
}
@Composable
fun CategoryPickerDialog(
    currentCategory: NoteCategory,
    onDismiss: () -> Unit,
    onSelect: (NoteCategory) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryColor
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(NoteCategory.entries.toList()) { category ->
                    Surface(
                        onClick = { onSelect(category) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (category == currentCategory) AccentColor else BackgroundColor,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (category == currentCategory) AccentColor else BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp
                            ),
                            fontWeight = if (category == currentCategory) FontWeight.Medium else FontWeight.Normal,
                            color = if (category == currentCategory) SurfaceColor else PrimaryColor,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SecondaryColor, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    )
}
@Composable
fun AddTagDialog(
    existingTags: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var tagInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Tag",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = {
                        Text(
                            "Tag name",
                            color = TertiaryColor
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundColor,
                        unfocusedContainerColor = BackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                if (existingTags.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "EXISTING TAGS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = TertiaryColor
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(existingTags) { tag ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onAdd(tag) },
                                    enabled = true,
                                    label = { Text("#$tag", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = BackgroundColor,
                                        labelColor = SecondaryColor
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = false,
                                        borderColor = BorderColor,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagInput.isNotBlank()) {
                        onAdd(tagInput.trim())
                    }
                },
                enabled = tagInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor
                )
            ) {
                Text("Add", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryColor)
            }
        }
    )
}
@Composable
fun AddChecklistDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var itemText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Checklist Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryColor
            )
        },
        text = {
            TextField(
                value = itemText,
                onValueChange = { itemText = it },
                placeholder = {
                    Text(
                        "Item text",
                        color = TertiaryColor
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundColor,
                    unfocusedContainerColor = BackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemText.isNotBlank()) {
                        onAdd(itemText.trim())
                    }
                },
                enabled = itemText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor
                )
            ) {
                Text("Add", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryColor)
            }
        }
    )
}
@Composable
fun ExportDialog(
    note: Note,
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Export Note",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryColor
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportOption(
                    icon = Icons.Outlined.Description,
                    title = "Markdown",
                    description = "Export as .md file",
                    onClick = {
                        val markdown = space.zeroxv6.journex.utils.ExportUtils.exportToMarkdown(note)
                        val file = space.zeroxv6.journex.utils.ExportUtils.saveToFile(
                            context,
                            markdown,
                            "${note.title.ifEmpty { "note" }}.md"
                        )
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/markdown"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Note"))
                        onDismiss()
                    }
                )
                ExportOption(
                    icon = Icons.Outlined.Code,
                    title = "HTML",
                    description = "Export as .html file",
                    onClick = {
                        val html = space.zeroxv6.journex.utils.ExportUtils.exportToHtml(note)
                        val file = space.zeroxv6.journex.utils.ExportUtils.saveToFile(
                            context,
                            html,
                            "${note.title.ifEmpty { "note" }}.html"
                        )
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/html"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Note"))
                        onDismiss()
                    }
                )
                ExportOption(
                    icon = Icons.Outlined.TextSnippet,
                    title = "Plain Text",
                    description = "Export as .txt file",
                    onClick = {
                        val text = space.zeroxv6.journex.utils.ExportUtils.exportToText(note)
                        val file = space.zeroxv6.journex.utils.ExportUtils.saveToFile(
                            context,
                            text,
                            "${note.title.ifEmpty { "note" }}.txt"
                        )
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Note"))
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SecondaryColor)
            }
        }
    )
}
@Composable
fun ExportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = BackgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = AccentColor
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = SecondaryColor
                )
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TertiaryColor
            )
        }
    }
}
@Composable
fun VoiceNoteCard(
    voiceNote: space.zeroxv6.journex.model.VoiceNote,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.apply {
                            if (isPlaying) stop()
                            release()
                        }
                        mediaPlayer = null
                        isPlaying = false
                    } else {
                        try {
                            mediaPlayer = android.media.MediaPlayer().apply {
                                setDataSource(voiceNote.filePath)
                                prepare()
                                setOnCompletionListener {
                                    isPlaying = false
                                    release()
                                    mediaPlayer = null
                                }
                                start()
                            }
                            isPlaying = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(
                                context,
                                "Failed to play voice note",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentColor, CircleShape)
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    tint = SurfaceColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Voice Note",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor
                )
                Text(
                    text = formatDuration(voiceNote.duration),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TertiaryColor
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = SecondaryColor
                )
            }
        }
    }
}
@Composable
fun DrawingCard(
    drawing: space.zeroxv6.journex.model.Drawing,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (drawing.imageData.startsWith("/")) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        val uri = try {
                            androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                java.io.File(drawing.imageData)
                            )
                        } catch (e: Exception) {
                            android.net.Uri.fromFile(java.io.File(drawing.imageData))
                        }
                        setDataAndType(uri, "image/*")
                        flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(
                            context,
                            "Failed to open drawing",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (drawing.imageData.startsWith("/")) {
                AsyncImage(
                    model = java.io.File(drawing.imageData),
                    contentDescription = "Drawing",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BackgroundColor,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.Draw,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = SecondaryColor
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Drawing",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = PrimaryColor
                )
                Text(
                    text = "${drawing.strokes.size} strokes • Tap to view",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TertiaryColor
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp),
                    tint = SecondaryColor
                )
            }
        }
    }
}
