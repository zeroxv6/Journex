package space.zeroxv6.journex.ui.screens
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import space.zeroxv6.journex.model.Mood
import space.zeroxv6.journex.model.JournalVoiceNote
import space.zeroxv6.journex.viewmodel.JournalViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun EditorScreen(
    viewModel: JournalViewModel,
    entryId: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    var showMoodPicker by remember { mutableStateOf(false) }
    var showTagInput by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.loadEntry(entryId)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopVoiceNote()
        }
    }
    val entry = viewModel.currentEntry ?: return
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (entry.title.isNotEmpty() || entry.content.isNotEmpty()) {
                                showDiscardDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        var showExportMenu by remember { mutableStateOf(false) }
                        Text(
                            text = "${entry.wordCount} words",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Box {
                            IconButton(onClick = { showExportMenu = true }) {
                                Icon(Icons.Outlined.Share, contentDescription = "Share")
                            }
                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export as Text", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                    onClick = {
                                        showExportMenu = false
                                        val exportText = "Title: ${entry.title}\nDate: ${entry.createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n\n${entry.content}"
                                        val file = space.zeroxv6.journex.utils.ExportUtils.saveToFile(
                                            context,
                                            exportText,
                                            "Journal_Entry_${System.currentTimeMillis()}.txt"
                                        )
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TITLE, entry.title)
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            android.content.Intent.createChooser(shareIntent, "Export Journal Entry")
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.TextSnippet, contentDescription = null)
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveEntry()
                                onNavigateBack()
                            }
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Save")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color(0xFF2C2825),
                        navigationIconContentColor = Color(0xFF2C2825),
                        actionIconContentColor = Color(0xFF2C2825)
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            }
        },
        containerColor = Color(0xFFF7F4EF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            val editorialDate = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")).uppercase()
            val editorialDay = entry.createdAt.format(DateTimeFormatter.ofPattern("EEEE")).uppercase()
            
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                // Editorial Date
                Text(
                    text = "$editorialDay   ·   $editorialDate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFD94F2A),
                    fontFamily = space.zeroxv6.journex.ui.theme.GeistFontFamily
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Title Field
                androidx.compose.foundation.text.BasicTextField(
                    value = entry.title,
                    onValueChange = { viewModel.updateCurrentEntry(entry.copy(title = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2C2825),
                        lineHeight = 46.sp
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD94F2A)),
                    decorationBox = { innerTextField ->
                        if (entry.title.isEmpty()) {
                            Text(
                                text = "Untitled",
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2C2825).copy(alpha = 0.2f),
                                lineHeight = 46.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            // Thin aesthetic divider
            Box(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(1.dp).background(Color(0xFFE5DED4)))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Content Field
            androidx.compose.foundation.text.BasicTextField(
                value = entry.content,
                onValueChange = { viewModel.updateCurrentEntry(entry.copy(content = it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 300.dp)
                    .padding(horizontal = 24.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    color = Color(0xFF3A3630),
                    letterSpacing = 0.2.sp
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD94F2A)),
                decorationBox = { innerTextField ->
                    if (entry.content.isEmpty()) {
                        Text(
                            text = "Begin your story...",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = 19.sp,
                            lineHeight = 32.sp,
                            color = Color(0xFF3A3630).copy(alpha = 0.35f),
                            letterSpacing = 0.2.sp
                        )
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.height(48.dp))
            // MINIMAL METADATA ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFDF9))
                    .border(1.dp, Color(0xFFE5DED4))
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mood Dropdown
                Column {
                    Text(
                        text = "VIBE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF2C2825).copy(alpha = 0.5f),
                        fontFamily = space.zeroxv6.journex.ui.theme.GeistFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.clickable { showMoodPicker = !showMoodPicker }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = entry.mood.icon, fontSize = 20.sp)
                            Text(text = entry.mood.label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C2825))
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2C2825).copy(alpha = 0.5f))
                        }
                        DropdownMenu(
                            expanded = showMoodPicker,
                            onDismissRequest = { showMoodPicker = false },
                            modifier = Modifier.background(Color(0xFFFFFDF9))
                        ) {
                            Mood.entries.forEach { mood ->
                                DropdownMenuItem(
                                    text = { Text("${mood.icon}  ${mood.label}", color = Color(0xFF2C2825)) },
                                    onClick = { 
                                        viewModel.updateCurrentEntry(entry.copy(mood = mood))
                                        showMoodPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Tags
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TAGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF2C2825).copy(alpha = 0.5f),
                        fontFamily = space.zeroxv6.journex.ui.theme.GeistFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (entry.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showTagInput = true }
                        ) {
                            entry.tags.take(3).forEach { t ->
                                Text(
                                    "#$t",
                                    fontSize = 13.sp,
                                    color = Color(0xFFD94F2A),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (entry.tags.size > 3) {
                                Text("+${entry.tags.size - 3}", fontSize = 13.sp, color = Color(0xFF2C2825).copy(alpha = 0.5f))
                            }
                        }
                    } else {
                        Text(
                            text = "+ Add tags",
                            fontSize = 13.sp,
                            color = Color(0xFF2C2825).copy(alpha = 0.4f),
                            modifier = Modifier.clickable { showTagInput = true }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Notes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = {
                            if (audioPermissionState.status.isGranted) {
                                if (viewModel.isRecording) {
                                    val result = viewModel.stopRecording()
                                    result?.let { (path, duration) ->
                                        path?.let { filePath ->
                                            val voiceNote = JournalVoiceNote(
                                                filePath = filePath,
                                                duration = duration
                                            )
                                            viewModel.addVoiceNoteToCurrentEntry(voiceNote)
                                        }
                                    }
                                } else {
                                    val voiceDir = java.io.File(context.filesDir, "note_voice")
                                    if (!voiceDir.exists()) voiceDir.mkdirs()
                                    val outputPath = "${voiceDir.absolutePath}/${java.util.UUID.randomUUID()}.m4a"
                                    viewModel.startRecording(outputPath)
                                }
                            } else {
                                audioPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            if (viewModel.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (viewModel.isRecording) "Stop Recording" else "Start Recording",
                            tint = if (viewModel.isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (viewModel.isRecording) {
                    LaunchedEffect(Unit) {
                        while (viewModel.isRecording) {
                            viewModel.updateRecordingDuration()
                            kotlinx.coroutines.delay(100)
                        }
                    }
                    RecordingIndicator(duration = viewModel.recordingDuration)
                }
                if (entry.voiceNotes.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        entry.voiceNotes.forEach { voiceNote ->
                            VoiceNoteCard(
                                voiceNote = voiceNote,
                                isPlaying = viewModel.isPlayingVoiceNote == voiceNote.filePath,
                                viewModel = viewModel,
                                onPlayClick = {
                                    if (viewModel.isPlayingVoiceNote == voiceNote.filePath) {
                                        viewModel.stopVoiceNote()
                                    } else {
                                        viewModel.playVoiceNote(voiceNote.filePath)
                                    }
                                },
                                onDeleteClick = {
                                    viewModel.deleteVoiceNote(voiceNote.id)
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No voice notes recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            PhotosSection(
                photos = entry.photos,
                onAddPhoto = { uri ->
                    viewModel.addPhotoToCurrentEntry(uri)
                },
                onRemovePhoto = { uri ->
                    viewModel.removePhotoFromCurrentEntry(uri)
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DED4)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Entry Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2C2825)
                    )
                    HorizontalDivider(color = Color(0xFFE5DED4))
                    MetadataRow(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Created",
                        value = entry.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
                    )
                    if (entry.updatedAt != entry.createdAt) {
                        MetadataRow(
                            icon = Icons.Outlined.Edit,
                            label = "Updated",
                            value = entry.updatedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
                        )
                    }
                    MetadataRow(
                        icon = Icons.Outlined.TextFields,
                        label = "Words",
                        value = entry.wordCount.toString()
                    )
                    MetadataRow(
                        icon = Icons.Outlined.Notes,
                        label = "Characters",
                        value = entry.characterCount.toString()
                    )
                    MetadataRow(
                        icon = Icons.Outlined.FormatListNumbered,
                        label = "Sentences",
                        value = entry.sentenceCount.toString()
                    )
                    MetadataRow(
                        icon = Icons.Outlined.Subject,
                        label = "Paragraphs",
                        value = entry.paragraphCount.toString()
                    )
                    MetadataRow(
                        icon = Icons.Outlined.Timer,
                        label = "Reading Time",
                        value = "${entry.readingTime} min"
                    )
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    if (showTagInput) {
        AlertDialog(
            onDismissRequest = { showTagInput = false },
            title = {
                Text(
                    "Add Tag",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF2C2825))
                )
            },
            text = {
                TextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    placeholder = { Text("Enter tag name", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = Color(0xFF2C2825).copy(alpha = 0.5f)) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF2C2825)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF2C2825)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            containerColor = Color(0xFFF7F4EF),
            confirmButton = {
                Button(
                    onClick = {
                        if (tagInput.isNotEmpty() && !entry.tags.contains(tagInput)) {
                            viewModel.updateCurrentEntry(
                                entry.copy(tags = entry.tags + tagInput)
                            )
                            tagInput = ""
                            showTagInput = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tagInput = ""
                        showTagInput = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    "Unsaved Changes",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF2C2825))
                )
            },
            text = {
                Text(
                    "You have unsaved changes. What would you like to do?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2C2825).copy(alpha = 0.8f)
                )
            },
            containerColor = Color(0xFFF7F4EF),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveEntry()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C2825),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Exit", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { showDiscardDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF2C2825).copy(alpha = 0.6f)
                        )
                    ) {
                        Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    TextButton(
                        onClick = {
                            viewModel.clearCurrentEntry()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFD94F2A)
                        )
                    ) {
                        Text("Discard", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun RecordingIndicator(duration: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE5DED4), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
            Text(
                text = "Recording...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2C2825)
            )
        }
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2C2825)
        )
    }
}
@Composable
fun VoiceNoteCard(
    voiceNote: JournalVoiceNote,
    isPlaying: Boolean,
    viewModel: JournalViewModel,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var currentPosition by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            currentPosition = 0L
        }
    }
    LaunchedEffect(isPlaying, isDragging) {
        while (isPlaying && !isDragging) {
            currentPosition = viewModel.getVoiceNotePosition()
            kotlinx.coroutines.delay(100)
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5DED4)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text(
                            text = "Voice Note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2C2825)
                        )
                        Text(
                            text = formatDuration(voiceNote.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2C2825).copy(alpha = 0.6f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = {
                            playbackSpeed = when (playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 1.75f
                                1.75f -> 2.0f
                                else -> 1.0f
                            }
                            viewModel.setPlaybackSpeed(playbackSpeed)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${playbackSpeed}x",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Slider(
                    value = currentPosition.toFloat().coerceIn(0f, voiceNote.duration.toFloat()),
                    onValueChange = { 
                        isDragging = true
                        currentPosition = it.toLong()
                    },
                    onValueChangeFinished = {
                        viewModel.seekVoiceNote(currentPosition)
                        isDragging = false
                    },
                    valueRange = 0f..voiceNote.duration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatDuration(voiceNote.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        val newPosition = maxOf(0L, currentPosition - 10000)
                        viewModel.seekVoiceNote(newPosition)
                        currentPosition = newPosition
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Replay,
                        contentDescription = "Rewind 10s",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = {
                        viewModel.togglePlayPause(voiceNote.filePath)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                ) {
                    Icon(
                        if (viewModel.isCurrentlyPlaying(voiceNote.filePath)) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (viewModel.isCurrentlyPlaying(voiceNote.filePath)) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = { 
                        val newPosition = minOf(voiceNote.duration, currentPosition + 10000)
                        viewModel.seekVoiceNote(newPosition)
                        currentPosition = newPosition
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.Forward10,
                        contentDescription = "Forward 10s",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
@Composable
fun MetadataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotosSection(
    photos: List<String>,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    val context = LocalContext.current
    var showImagePicker by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    val imagePermissionState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            onAddPhoto(uri.toString())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Photos",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = {
                    if (imagePermissionState.status.isGranted) {
                        multipleImagePickerLauncher.launch("image/*")
                    } else {
                        imagePermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = "Add Photos",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (photos.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    photos.chunked(3).forEach { rowPhotos ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPhotos.forEach { photoUri ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    PhotoCard(
                                        photoUri = photoUri,
                                        onClick = { showFullImage = photoUri },
                                        onLongClick = { showDeleteDialog = photoUri }
                                    )
                                }
                            }
                            repeat(3 - rowPhotos.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (rowPhotos != photos.chunked(3).last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${photos.size} ${if (photos.size == 1) "photo" else "photos"}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (imagePermissionState.status.isGranted) {
                            multipleImagePickerLauncher.launch("image/*")
                        } else {
                            imagePermissionState.launchPermissionRequest()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Add photos to your entry",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap to select from gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    showFullImage?.let { photoUri ->
        AlertDialog(
            onDismissRequest = { showFullImage = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Photo",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(photoUri), "image/*")
                                    flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.OpenInNew,
                                contentDescription = "Open in gallery",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                showFullImage = null
                                showDeleteDialog = photoUri
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    AsyncImage(
                        model = if (photoUri.startsWith("/")) java.io.File(photoUri) else Uri.parse(photoUri),
                        contentDescription = "Full size photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFullImage = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Close", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    showDeleteDialog?.let { photoUri ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    "Remove Photo",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove this photo from the entry?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemovePhoto(photoUri)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
@Composable
fun PhotoCard(
    photoUri: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDeleteIcon by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            val uri = if (photoUri.startsWith("/")) {
                                androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    File(photoUri)
                                )
                            } else {
                                Uri.parse(photoUri)
                            }
                            setDataAndType(uri, "image/*")
                            flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onLongPress = { 
                        onLongClick()
                    },
                    onPress = {
                        isPressed = true
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (!released) {
                            showDeleteIcon = true
                        }
                    }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 0.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageModel = if (photoUri.startsWith("/")) {
                File(photoUri)
            } else {
                Uri.parse(photoUri)
            }
            AsyncImage(
                model = imageModel,
                contentDescription = "Journal photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Surface(
                    onClick = { onLongClick() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (isPressed) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                )
            }
        }
    }
}
