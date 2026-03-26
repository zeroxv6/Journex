package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)
@Composable
fun DesktopDrawingCanvas(
    onSave: (List<DrawingPath>) -> Unit,
    onDismiss: () -> Unit
) {
    var paths by remember { mutableStateOf<List<DrawingPath>>(emptyList()) }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(3f) }
    val availableColors = listOf(
        Color.Black, Color.White, Color.Red, Color(0xFF2196F3),
        Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF795548)
    )
    val strokeWidths = listOf(1f, 2f, 3f, 5f, 8f, 12f)
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Draw, null, modifier = Modifier.size(24.dp))
                Text("Drawing", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth().height(350.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> currentPath = listOf(offset) },
                                onDrag = { change, _ ->
                                    currentPath = currentPath + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        paths = paths + DrawingPath(currentPath.toList(), selectedColor, strokeWidth)
                                        currentPath = emptyList()
                                    }
                                }
                            )
                        }
                    ) {
                        paths.forEach { path ->
                            if (path.points.size >= 2) {
                                val drawPath = Path().apply {
                                    moveTo(path.points.first().x, path.points.first().y)
                                    for (i in 1 until path.points.size) {
                                        lineTo(path.points[i].x, path.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = drawPath,
                                    color = path.color,
                                    style = Stroke(width = path.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                        if (currentPath.size >= 2) {
                            val drawPath = Path().apply {
                                moveTo(currentPath.first().x, currentPath.first().y)
                                for (i in 1 until currentPath.size) {
                                    lineTo(currentPath[i].x, currentPath[i].y)
                                }
                            }
                            drawPath(
                                path = drawPath,
                                color = selectedColor,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
                Text("Color", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableColors.forEach { color ->
                        Surface(
                            onClick = { selectedColor = color },
                            shape = CircleShape,
                            color = color,
                            border = BorderStroke(
                                if (selectedColor == color) 3.dp else 1.dp,
                                if (selectedColor == color) colors.textPrimary else colors.border
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {}
                    }
                }
                Text("Stroke Width", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    strokeWidths.forEach { width ->
                        FilterChip(
                            selected = strokeWidth == width,
                            onClick = { strokeWidth = width },
                            label = { Text("${width.toInt()}px", style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.textPrimary,
                                selectedLabelColor = colors.background
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { if (paths.isNotEmpty()) paths = paths.dropLast(1) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Icon(Icons.Outlined.Undo, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", color = colors.textSecondary)
                    }
                    OutlinedButton(
                        onClick = { paths = emptyList() },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", color = colors.textSecondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(paths) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)
            ) { Text("Save Drawing") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
