package space.zeroxv6.journex.ui.components
import android.media.MediaRecorder
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID
import kotlin.math.abs
import kotlin.math.sin
@Composable
fun VoiceRecorderDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long, List<Float>) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0L) }
    var waveformData by remember { mutableStateOf<List<Float>>(emptyList()) }
    var currentAmplitude by remember { mutableStateOf(0f) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (isRecording && !isPaused) {
                recordingDuration += 100
                val amplitude = (mediaRecorder?.maxAmplitude ?: 0) / 32768f
                currentAmplitude = amplitude
                if (waveformData.size < 100) {
                    waveformData = waveformData + amplitude
                } else {
                    waveformData = waveformData.drop(1) + amplitude
                }
                delay(100)
            }
        }
    }
    AlertDialog(
        onDismissRequest = {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                    release()
                }
            }
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Voice Recording", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(
                    text = formatDuration(recordingDuration),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (waveformData.isNotEmpty()) {
                            WaveformVisualizer(
                                waveformData = waveformData,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                "Tap record to start",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording || recordingDuration > 0) {
                        IconButton(
                            onClick = {
                                mediaRecorder?.apply {
                                    if (isRecording) {
                                        stop()
                                        release()
                                    }
                                }
                                outputFile?.delete()
                                isRecording = false
                                recordingDuration = 0
                                waveformData = emptyList()
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    FloatingActionButton(
                        onClick = {
                            if (!isRecording) {
                                val voiceDir = File(context.filesDir, "note_voice")
                                if (!voiceDir.exists()) {
                                    voiceDir.mkdirs()
                                }
                                outputFile = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")
                                mediaRecorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    MediaRecorder(context)
                                } else {
                                    @Suppress("DEPRECATION")
                                    MediaRecorder()
                                }.apply {
                                    try {
                                        setAudioSource(MediaRecorder.AudioSource.MIC)
                                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                        setOutputFile(outputFile?.absolutePath)
                                        prepare()
                                        start()
                                        isRecording = true
                                        isPaused = false
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                try {
                                    mediaRecorder?.apply {
                                        stop()
                                        release()
                                    }
                                    isRecording = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    if (isRecording && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        IconButton(
                            onClick = {
                                try {
                                    isPaused = !isPaused
                                    if (isPaused) {
                                        mediaRecorder?.pause()
                                    } else {
                                        mediaRecorder?.resume()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            Icon(
                                if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = if (isPaused) "Resume" else "Pause"
                            )
                        }
                    }
                }
                if (isRecording && !isPaused) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Text(
                            "Recording...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (isPaused) {
                    Text(
                        "Paused",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    outputFile?.let { file ->
                        onSave(file.absolutePath, recordingDuration, waveformData)
                    }
                    onDismiss()
                },
                enabled = !isRecording && recordingDuration > 0
            ) {
                Text("Save", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                mediaRecorder?.apply {
                    if (isRecording) {
                        stop()
                        release()
                    }
                }
                outputFile?.delete()
                onDismiss()
            }) {
                Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
@Composable
fun WaveformVisualizer(
    waveformData: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.padding(8.dp)) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val barWidth = width / waveformData.size.coerceAtLeast(1)
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * barWidth
            val barHeight = (amplitude * height / 2).coerceIn(2f, height / 2)
            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = barWidth * 0.8f
            )
        }
    }
}
private fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
