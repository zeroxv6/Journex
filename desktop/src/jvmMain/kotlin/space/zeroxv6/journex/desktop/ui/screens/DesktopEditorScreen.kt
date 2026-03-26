package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.shared.model.*
import kotlinx.coroutines.delay
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import javax.sound.sampled.*
data class VoiceNote(val id: String, val filePath: String, val duration: Long)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopEditorScreen(
    viewModel: JournalViewModel,
    entryId: String?,
    initialPrompt: String? = null,
    initialTitle: String? = null,
    onNavigateBack: () -> Unit
) {
    val currentEntry by viewModel.currentEntry.collectAsState()
    var showMoodPicker by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTag by remember { mutableStateOf("") }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var attachedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var voiceNotes by remember { mutableStateOf<List<VoiceNote>>(emptyList()) }
    var locationText by remember { mutableStateOf("") }
    var weatherText by remember { mutableStateOf("") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var audioLine by remember { mutableStateOf<TargetDataLine?>(null) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var recordingStartTime by remember { mutableStateOf(0L) }
    var playingNoteId by remember { mutableStateOf<String?>(null) }
    var playbackPosition by remember { mutableStateOf(0L) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    LaunchedEffect(entryId) {
        if (entryId != null) viewModel.loadEntry(entryId)
        else viewModel.createNewEntry()
    }
    val entry = currentEntry ?: return
    LaunchedEffect(entry, initialPrompt, initialTitle) {
        if (entry.content.isEmpty() && initialPrompt != null) {
            viewModel.updateCurrentEntry(entry.copy(content = initialPrompt))
        }
        if (entry.title.isEmpty() && initialTitle != null) {
            viewModel.updateCurrentEntry(entry.copy(title = initialTitle))
        }
        attachedImages = entry.photos
        locationText = entry.location ?: ""
        weatherText = entry.weather ?: ""
    }
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingStartTime = System.currentTimeMillis()
            while (isRecording) {
                recordingTime = System.currentTimeMillis() - recordingStartTime
                delay(100)
            }
        }
    }
    fun handleBack() {
        if (entry.title.isNotEmpty() || entry.content.isNotEmpty()) showDiscardDialog = true
        else { viewModel.clearCurrentEntry(); onNavigateBack() }
    }
    fun openImagePicker() {
        val dialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> name.lowercase().let { it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(".jpeg") } }
        dialog.isVisible = true
        if (dialog.file != null) {
            val path = "${dialog.directory}${dialog.file}"
            attachedImages = attachedImages + path
            viewModel.updateCurrentEntry(entry.copy(photos = attachedImages))
        }
    }
    fun startRecording() {
        try {
            val format = AudioFormat(44100f, 16, 1, true, true)
            val info = DataLine.Info(TargetDataLine::class.java, format)
            if (!AudioSystem.isLineSupported(info)) return
            val line = AudioSystem.getLine(info) as TargetDataLine
            line.open(format)
            line.start()
            audioLine = line
            val tempFile = File.createTempFile("voice_", ".wav")
            recordedFile = tempFile
            Thread {
                try { AudioSystem.write(AudioInputStream(line), AudioFileFormat.Type.WAVE, tempFile) }
                catch (_: Exception) {}
            }.start()
            isRecording = true
            recordingTime = 0L
        } catch (e: Exception) { println("Recording error: ${e.message}") }
    }
    fun stopRecording() {
        val duration = recordingTime
        isRecording = false
        audioLine?.stop()
        audioLine?.close()
        audioLine = null
        recordedFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                val note = VoiceNote(id = java.util.UUID.randomUUID().toString(), filePath = file.absolutePath, duration = duration)
                voiceNotes = voiceNotes + note
            }
        }
        recordedFile = null
        recordingTime = 0L
    }
    fun formatDuration(ms: Long): String {
        val secs = (ms / 1000).toInt()
        val mins = secs / 60
        val s = secs % 60
        return "%d:%02d".format(mins, s)
    }
    Row(modifier = Modifier.fillMaxSize().background(AppColors.current.background)) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.current.background) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { handleBack() }) { Icon(Icons.Outlined.ArrowBack, "Back") }
                            Column {
                                Text(if (entryId == null) "New Entry" else "Edit Entry", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                                Text(entry.createdAt.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TextButton(onClick = { handleBack() }) { Text("Cancel", color = AppColors.current.textSecondary) }
                            Button(
                                onClick = {
                                    viewModel.updateCurrentEntry(entry.copy(location = locationText.ifEmpty { null }, weather = weatherText.ifEmpty { null }))
                                    viewModel.saveEntry()
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Save") }
                        }
                    }
                    HorizontalDivider(color = AppColors.current.border)
                }
            }
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(40.dp)) {
                TextField(
                    value = entry.title,
                    onValueChange = { viewModel.updateCurrentEntry(entry.copy(title = it)) },
                    placeholder = { Text("Title", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium), color = AppColors.current.borderFocused) },
                    textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Surface(modifier = Modifier.clickable { showMoodPicker = true }, shape = RoundedCornerShape(12.dp), color = AppColors.current.inputBackground) {
                            Row(modifier = Modifier.padding(14.dp, 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(entry.mood.label, style = MaterialTheme.typography.bodyLarge)
                                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp), AppColors.current.textTertiary)
                            }
                        }
                        DropdownMenu(expanded = showMoodPicker, onDismissRequest = { showMoodPicker = false }, containerColor = AppColors.current.background) {
                            Mood.entries.forEach { mood ->
                                DropdownMenuItem(text = { Text(mood.label) }, onClick = { viewModel.updateCurrentEntry(entry.copy(mood = mood)); showMoodPicker = false })
                            }
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        items(entry.tags) { tag ->
                            Surface(shape = RoundedCornerShape(10.dp), color = AppColors.current.inputBackground) {
                                Row(modifier = Modifier.padding(14.dp, 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("#$tag", style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.Filled.Close, "Remove", modifier = Modifier.size(16.dp).clickable { viewModel.updateCurrentEntry(entry.copy(tags = entry.tags - tag)) }, tint = AppColors.current.textTertiary)
                                }
                            }
                        }
                        item {
                            Surface(modifier = Modifier.clickable { showTagDialog = true }, shape = RoundedCornerShape(10.dp), color = AppColors.current.inputBackground) {
                                Row(modifier = Modifier.padding(14.dp, 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                                    Text("Tag", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Attachments", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MediaButton(icon = Icons.Outlined.Image, label = "Image", onClick = { openImagePicker() })
                            MediaButton(icon = Icons.Outlined.LocationOn, label = if (locationText.isEmpty()) "Location" else locationText.take(15), onClick = { showLocationDialog = true })
                            MediaButton(icon = Icons.Outlined.WbSunny, label = if (weatherText.isEmpty()) "Weather" else weatherText.take(15), onClick = { showWeatherDialog = true })
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = if (isRecording) AppColors.current.cardBackground else AppColors.current.background, border = BorderStroke(1.dp, AppColors.current.border)) {
                            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (isRecording) stopRecording() else startRecording() },
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(if (isRecording) AppColors.current.textSecondary else AppColors.current.textPrimary)
                                ) {
                                    Icon(if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic, null, tint = AppColors.current.background, modifier = Modifier.size(28.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isRecording) "Recording..." else "Voice Memo", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium))
                                    Text(if (isRecording) formatDuration(recordingTime) else "Tap to record", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textTertiary)
                                }
                                if (isRecording) Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppColors.current.textSecondary))
                            }
                        }
                        if (voiceNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Voice Notes (${voiceNotes.size})", style = MaterialTheme.typography.labelLarge, color = AppColors.current.textSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                voiceNotes.forEach { note ->
                                    VoiceNoteCard(
                                        voiceNote = note,
                                        isPlaying = playingNoteId == note.id,
                                        currentPosition = if (playingNoteId == note.id) playbackPosition else 0L,
                                        playbackSpeed = playbackSpeed,
                                        onPlayPause = { playingNoteId = if (playingNoteId == note.id) null else note.id },
                                        onSeek = { playbackPosition = it },
                                        onSpeedChange = { playbackSpeed = it },
                                        onDelete = { voiceNotes = voiceNotes.filter { it.id != note.id } },
                                        formatDuration = ::formatDuration
                                    )
                                }
                            }
                        }
                        if (attachedImages.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("Images (${attachedImages.size})", style = MaterialTheme.typography.labelLarge, color = AppColors.current.textSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(attachedImages) { path ->
                                    ImageThumbnail(path) { attachedImages = attachedImages - path; viewModel.updateCurrentEntry(entry.copy(photos = attachedImages)) }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                TextField(
                    value = entry.content,
                    onValueChange = { viewModel.updateCurrentEntry(entry.copy(content = it)) },
                    placeholder = { Text("Start writing...", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.borderFocused) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp)
                )
            }
        }
        Surface(modifier = Modifier.width(280.dp).fillMaxHeight(), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Entry Stats", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(20.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AppColors.current.background, border = BorderStroke(1.dp, AppColors.current.border)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatRow("Words", "${entry.wordCount}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppColors.current.surfaceTertiary)
                        StatRow("Characters", "${entry.characterCount}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppColors.current.surfaceTertiary)
                        StatRow("Sentences", "${entry.sentenceCount}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppColors.current.surfaceTertiary)
                        StatRow("Paragraphs", "${entry.paragraphCount}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppColors.current.surfaceTertiary)
                        StatRow("Reading Time", "${entry.readingTime} min")
                    }
                }
            }
        }
    }
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false; newTag = "" },
            title = { Text("Add Tag", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { OutlinedTextField(value = newTag, onValueChange = { newTag = it.replace(" ", "").lowercase() }, placeholder = { Text("Tag name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.current.textPrimary, focusedLabelColor = AppColors.current.textPrimary)) },
            confirmButton = { Button(onClick = { if (newTag.isNotEmpty() && !entry.tags.contains(newTag)) viewModel.updateCurrentEntry(entry.copy(tags = entry.tags + newTag)); newTag = ""; showTagDialog = false }, enabled = newTag.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary)) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showTagDialog = false; newTag = "" }) { Text("Cancel", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { OutlinedTextField(value = locationText, onValueChange = { locationText = it }, placeholder = { Text("e.g., New York, NY") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.current.textPrimary, focusedLabelColor = AppColors.current.textPrimary)) },
            confirmButton = { Button(onClick = { showLocationDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary)) { Text("Done") } },
            dismissButton = { TextButton(onClick = { locationText = ""; showLocationDialog = false }) { Text("Clear", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showWeatherDialog) {
        AlertDialog(
            onDismissRequest = { showWeatherDialog = false },
            title = { Text("Weather", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { OutlinedTextField(value = weatherText, onValueChange = { weatherText = it }, placeholder = { Text("e.g., Sunny, 72°F") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.current.textPrimary, focusedLabelColor = AppColors.current.textPrimary)) },
            confirmButton = { Button(onClick = { showWeatherDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary)) { Text("Done") } },
            dismissButton = { TextButton(onClick = { weatherText = ""; showWeatherDialog = false }) { Text("Clear", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { Text("You have unsaved changes that will be lost.") },
            confirmButton = { Button(onClick = { viewModel.clearCurrentEntry(); showDiscardDialog = false; onNavigateBack() }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary)) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
}
@Composable
private fun VoiceNoteCard(
    voiceNote: VoiceNote,
    isPlaying: Boolean,
    currentPosition: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDelete: () -> Unit,
    formatDuration: (Long) -> String
) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = AppColors.current.inputBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Voice Note", style = MaterialTheme.typography.bodyMedium)
                    Text(formatDuration(voiceNote.duration), style = MaterialTheme.typography.bodySmall, color = AppColors.current.textDisabled)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = { onSpeedChange(when (playbackSpeed) { 1.0f -> 1.25f; 1.25f -> 1.5f; 1.5f -> 1.75f; 1.75f -> 2.0f; else -> 1.0f }) },
                        shape = RoundedCornerShape(8.dp), color = AppColors.current.surfaceTertiary
                    ) { Text("${playbackSpeed}x", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) }
                    IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete", tint = AppColors.current.textDisabled) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = currentPosition.toFloat().coerceIn(0f, voiceNote.duration.toFloat()),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..voiceNote.duration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(thumbColor = AppColors.current.textPrimary, activeTrackColor = AppColors.current.textPrimary, inactiveTrackColor = AppColors.current.border)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall, color = AppColors.current.textDisabled)
                Text(formatDuration(voiceNote.duration), style = MaterialTheme.typography.bodySmall, color = AppColors.current.textDisabled)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onSeek(maxOf(0L, currentPosition - 10000)) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Replay, "Rewind 10s", tint = AppColors.current.textPrimary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onPlayPause, modifier = Modifier.size(56.dp).background(AppColors.current.textPrimary, CircleShape)) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) "Pause" else "Play", tint = AppColors.current.background, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { onSeek(minOf(voiceNote.duration, currentPosition + 10000)) }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.Forward10, "Forward 10s", tint = AppColors.current.textPrimary)
                }
            }
        }
    }
}
@Composable
private fun MediaButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(10.dp), color = AppColors.current.background, border = BorderStroke(1.dp, AppColors.current.border)) {
        Row(modifier = Modifier.padding(14.dp, 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppColors.current.textSecondary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}
@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textTertiary)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}
@Composable
private fun ImageThumbnail(imagePath: String, onRemove: () -> Unit) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var loadError by remember { mutableStateOf(false) }
    LaunchedEffect(imagePath) {
        try {
            val file = File(imagePath)
            if (file.exists()) imageBitmap = ImageIO.read(file)?.toComposeImageBitmap()
            else loadError = true
        } catch (e: Exception) { loadError = true }
    }
    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(AppColors.current.inputBackground).clickable {
        try {
            val file = java.io.File(imagePath)
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }) {
        if (imageBitmap != null) Image(bitmap = imageBitmap!!, contentDescription = null, modifier = Modifier.fillMaxSize())
        else if (loadError) Icon(Icons.Outlined.BrokenImage, null, Modifier.align(Alignment.Center), AppColors.current.borderFocused)
        else CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.Center), strokeWidth = 2.dp)
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(AppColors.current.textPrimary.copy(alpha = 0.7f))) {
            Icon(Icons.Filled.Close, "Remove", tint = AppColors.current.background, modifier = Modifier.size(14.dp))
        }
    }
}
