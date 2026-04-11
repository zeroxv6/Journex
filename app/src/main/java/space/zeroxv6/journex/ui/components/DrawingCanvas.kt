package space.zeroxv6.journex.ui.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
data class DrawingPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val points: List<Offset> = emptyList() 
)
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    onSave: (List<DrawingPath>, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var paths by remember { mutableStateOf<List<DrawingPath>>(emptyList()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var currentStrokeWidth by remember { mutableStateOf(5f) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showStrokeWidthPicker by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf<androidx.compose.ui.geometry.Size?>(null) }
    val colors = listOf(
        Color.Black, Color.Red, Color.Blue, Color.Green,
        Color.Yellow, Color.Magenta, Color.Cyan, Color.Gray
    )
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showColorPicker = !showColorPicker },
                        modifier = Modifier
                            .size(40.dp)
                            .background(currentColor, CircleShape)
                    ) {}
                    IconButton(onClick = { showStrokeWidthPicker = !showStrokeWidthPicker }) {
                        Icon(Icons.Filled.LineWeight, contentDescription = "Stroke Width")
                    }
                    IconButton(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                paths = paths.dropLast(1)
                            }
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = { paths = emptyList() },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = {
                            val width = canvasSize?.width?.toInt() ?: 1080
                            val height = canvasSize?.height?.toInt() ?: 1920
                            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            val paint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                                style = android.graphics.Paint.Style.STROKE
                            }
                            paths.forEach { drawingPath ->
                                paint.color = drawingPath.color.toArgb()
                                paint.strokeWidth = drawingPath.strokeWidth
                                if (drawingPath.points.isNotEmpty()) {
                                    val androidPath = android.graphics.Path()
                                    val firstPoint = drawingPath.points.first()
                                    androidPath.moveTo(firstPoint.x, firstPoint.y)
                                    drawingPath.points.drop(1).forEach { point ->
                                        androidPath.lineTo(point.x, point.y)
                                    }
                                    canvas.drawPath(androidPath, paint)
                                }
                            }
                            try {
                                val fileName = "drawing_${System.currentTimeMillis()}.png"
                                val drawingsDir = java.io.File(context.filesDir, "note_drawings")
                                if (!drawingsDir.exists()) {
                                    drawingsDir.mkdirs()
                                }
                                val drawingFile = java.io.File(drawingsDir, fileName)
                                val outputStream = java.io.FileOutputStream(drawingFile)
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                                outputStream.flush()
                                outputStream.close()
                                onSave(paths, fileName)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Text("Save", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
        AnimatedVisibility(visible = showColorPicker) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { color ->
                    Surface(
                        onClick = {
                            currentColor = color
                            showColorPicker = false
                        },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = color,
                        border = if (currentColor == color) {
                            androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {}
                }
            }
        }
        AnimatedVisibility(visible = showStrokeWidthPicker) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text("Stroke Width: ${currentStrokeWidth.toInt()}px", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Slider(
                    value = currentStrokeWidth,
                    onValueChange = { currentStrokeWidth = it },
                    valueRange = 1f..50f,
                    onValueChangeFinished = { showStrokeWidthPicker = false }
                )
            }
        }
        var currentPathState by remember { mutableStateOf<Path?>(null) }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                            currentPoints = listOf(offset)
                            currentPathState = currentPath
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            currentPoints = currentPoints + change.position
                            currentPathState = Path().apply {
                                addPath(currentPath!!)
                            }
                        },
                        onDragEnd = {
                            currentPath?.let { path ->
                                paths = paths + DrawingPath(
                                    path = path,
                                    color = currentColor,
                                    strokeWidth = currentStrokeWidth,
                                    points = currentPoints
                                )
                                currentPath = null
                                currentPoints = emptyList()
                                currentPathState = null
                            }
                        }
                    )
                }
        ) {
            if (canvasSize == null) {
                canvasSize = size
            }
            paths.forEach { drawingPath ->
                drawPath(
                    path = drawingPath.path,
                    color = drawingPath.color,
                    style = Stroke(
                        width = drawingPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = drawingPath.alpha
                )
            }
            currentPathState?.let { path ->
                drawPath(
                    path = path,
                    color = currentColor,
                    style = Stroke(
                        width = currentStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
