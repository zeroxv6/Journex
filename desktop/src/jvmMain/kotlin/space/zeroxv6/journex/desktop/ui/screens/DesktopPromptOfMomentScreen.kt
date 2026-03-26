package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.util.PromptGenerator
@Composable
fun DesktopPromptOfMomentScreen(
    taskViewModel: TaskViewModel,
    onWriteWithPrompt: (String) -> Unit,
    onNavigateToAllPrompts: () -> Unit
) {
    val savedPrompts by taskViewModel.savedPrompts.collectAsState()
    val promptCategories = PromptGenerator.categories
    var selectedCategory by remember { mutableStateOf(promptCategories.firstOrNull() ?: "") }
    val categoryPrompts = PromptGenerator.getPromptsByCategory(selectedCategory).map { it.text }
    var currentPromptIndex by remember { mutableStateOf(0) }
    val currentPrompt = categoryPrompts.getOrNull(currentPromptIndex % categoryPrompts.size.coerceAtLeast(1)) ?: "Write about anything..."
    var showResponseDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Prompt of the Moment", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp), color = AppColors.current.textPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Get inspired to write", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            TextButton(onClick = onNavigateToAllPrompts) {
                Text("See All Prompts", color = AppColors.current.textSecondary, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textSecondary)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            promptCategories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat; currentPromptIndex = 0 },
                    label = { Text(cat, style = MaterialTheme.typography.bodyMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppColors.current.textPrimary,
                        selectedLabelColor = AppColors.current.background,
                        containerColor = AppColors.current.cardBackground,
                        labelColor = AppColors.current.textSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            shape = RoundedCornerShape(24.dp),
            color = AppColors.current.cardBackground,
            border = BorderStroke(1.dp, AppColors.current.border)
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(shape = CircleShape, color = AppColors.current.surfaceVariant) {
                    Icon(Icons.Outlined.Lightbulb, null, modifier = Modifier.padding(16.dp).size(32.dp), tint = AppColors.current.textSecondary)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(selectedCategory, style = MaterialTheme.typography.labelLarge, color = AppColors.current.textTertiary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    currentPrompt,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, lineHeight = 36.sp),
                    color = AppColors.current.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        onClick = { currentPromptIndex = (currentPromptIndex + 1) % categoryPrompts.size.coerceAtLeast(1) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp),
                        border = BorderStroke(1.dp, AppColors.current.border)
                    ) {
                        Icon(Icons.Outlined.Shuffle, null, modifier = Modifier.size(20.dp), tint = AppColors.current.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next Prompt", color = AppColors.current.textSecondary, style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = { onWriteWithPrompt(currentPrompt) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Write About This", style = MaterialTheme.typography.titleMedium)
                    }
                    OutlinedButton(
                        onClick = { showResponseDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(52.dp),
                        border = BorderStroke(1.dp, AppColors.current.border)
                    ) {
                        Icon(Icons.Outlined.Save, null, modifier = Modifier.size(20.dp), tint = AppColors.current.textSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Response", color = AppColors.current.textSecondary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (savedPrompts.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Saved Prompts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textPrimary)
                Text("${savedPrompts.size} saved", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textTertiary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(savedPrompts.sortedByDescending { it.createdAt }.take(3), key = { it.id }) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.current.cardBackground,
                        border = BorderStroke(1.dp, AppColors.current.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(prompt.promptText, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary, maxLines = 1)
                            if (prompt.response.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prompt.response, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textPrimary, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showResponseDialog) {
        SaveResponseDialog(
            promptText = currentPrompt,
            category = selectedCategory,
            onDismiss = { showResponseDialog = false },
            onSave = { response ->
                taskViewModel.savePromptResponse(currentPrompt, selectedCategory, response)
                showResponseDialog = false
            }
        )
    }
}
@Composable
private fun SaveResponseDialog(promptText: String, category: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var response by remember { mutableStateOf("") }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Response", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Prompt:", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                Text(promptText, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = response, onValueChange = { response = it },
                    label = { Text("Your response") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(response) }, enabled = response.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary) } },
        containerColor = colors.background, shape = RoundedCornerShape(20.dp)
    )
}
