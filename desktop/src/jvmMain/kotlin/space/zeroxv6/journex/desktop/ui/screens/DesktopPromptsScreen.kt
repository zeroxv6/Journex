package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import space.zeroxv6.journex.shared.util.PromptGenerator
import java.time.format.DateTimeFormatter
@Composable
fun DesktopPromptsScreen(taskViewModel: TaskViewModel, onWriteWithPrompt: (String) -> Unit, onNavigate: (String) -> Unit = {}) {
    val colors = AppColors.current
    val savedPrompts by taskViewModel.savedPrompts.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var currentPrompt by remember { mutableStateOf(PromptGenerator.getRandomPrompt()) }
    var showSavedPrompts by remember { mutableStateOf(false) }
    var editingPrompt by remember { mutableStateOf<SavedPrompt?>(null) }
    val filteredPrompts = if (selectedCategory != null) PromptGenerator.getPromptsByCategory(selectedCategory!!) else PromptGenerator.allPrompts
    Column(modifier = Modifier.fillMaxSize().background(colors.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Prompts", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Get inspired to write", style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
            }
            TextButton(onClick = { showSavedPrompts = !showSavedPrompts }) {
                Text(if (showSavedPrompts) "Browse Prompts" else "Saved (${savedPrompts.size})", color = colors.textSecondary, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (showSavedPrompts) {
            if (savedPrompts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = colors.inputBackground) { Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = colors.textDisabled) }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("No saved responses", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Save your prompt responses here", style = MaterialTheme.typography.bodyLarge, color = colors.textTertiary)
                    }
                }
            } else {
                Text("${savedPrompts.size} saved ${if (savedPrompts.size == 1) "response" else "responses"}", style = MaterialTheme.typography.titleMedium, color = colors.textTertiary)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(savedPrompts, key = { it.id }) { prompt ->
                        SavedPromptCard(
                            prompt = prompt,
                            onEdit = { editingPrompt = it },
                            onDelete = { taskViewModel.deletePrompt(prompt.id) }
                        )
                    }
                }
            }
        } else {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = colors.textPrimary) {
                Column(modifier = Modifier.padding(28.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(8.dp), color = colors.background.copy(alpha = 0.15f)) {
                            Text(currentPrompt.category, modifier = Modifier.padding(10.dp, 6.dp), style = MaterialTheme.typography.labelMedium, color = colors.background.copy(alpha = 0.8f))
                        }
                        IconButton(onClick = { currentPrompt = PromptGenerator.getRandomPrompt() }) { Icon(Icons.Outlined.Refresh, "New prompt", tint = colors.background.copy(alpha = 0.7f)) }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(currentPrompt.text, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light, lineHeight = 32.sp), color = colors.background)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { onWriteWithPrompt(currentPrompt.text) }, colors = ButtonDefaults.buttonColors(containerColor = colors.background, contentColor = colors.textPrimary), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Write with this prompt")
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.textPrimary, selectedLabelColor = colors.background)) }
                items(PromptGenerator.categories) { category -> FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = colors.textPrimary, selectedLabelColor = colors.background)) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("${filteredPrompts.size} prompts available", style = MaterialTheme.typography.titleMedium, color = colors.textTertiary)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredPrompts, key = { it.id }) { prompt ->
                    PromptCard(prompt) {
                        taskViewModel.savePromptResponse(prompt.text, prompt.category, "")
                        showSavedPrompts = true
                    }
                }
            }
        }
    }
    editingPrompt?.let { prompt ->
        PromptResponseDialog(
            prompt = prompt,
            onDismiss = { editingPrompt = null },
            onSave = { response ->
                taskViewModel.updatePrompt(prompt.copy(response = response))
                editingPrompt = null
            }
        )
    }
}
@Composable
private fun PromptCard(prompt: Prompt, onSave: () -> Unit) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, onClick = onSave).hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) colors.borderFocused else colors.border)
    ) {
        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) {
                    Text(prompt.category, modifier = Modifier.padding(10.dp, 5.dp), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(prompt.text, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp))
            }
            Icon(Icons.Outlined.BookmarkAdd, "Save", tint = colors.textSecondary, modifier = Modifier.size(24.dp))
        }
    }
}
@Composable
private fun SavedPromptCard(prompt: SavedPrompt, onEdit: (SavedPrompt) -> Unit, onDelete: () -> Unit) {
    val colors = AppColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, onClick = { onEdit(prompt) }).hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) colors.borderFocused else colors.border)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) {
                        Text(prompt.category, modifier = Modifier.padding(10.dp, 5.dp), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(prompt.promptText, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.MoreVert, null, tint = colors.textTertiary, modifier = Modifier.size(24.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = colors.background) {
                        DropdownMenuItem(
                            text = { Text("Edit Response", style = MaterialTheme.typography.bodyLarge) },
                            onClick = { onEdit(prompt); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) },
                            onClick = { onDelete(); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                        )
                    }
                }
            }
            if (prompt.response.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = colors.surfaceTertiary)
                Spacer(modifier = Modifier.height(20.dp))
                Text(prompt.response, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Click to add your response", style = MaterialTheme.typography.bodyMedium, color = colors.textDisabled)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(prompt.createdAt.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
        }
    }
}
@Composable
private fun PromptResponseDialog(prompt: SavedPrompt, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val colors = AppColors.current
    var response by remember { mutableStateOf(prompt.response) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your Response", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.inputBackground) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceTertiary) {
                            Text(prompt.category, modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(prompt.promptText, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                }
                TextField(
                    value = response,
                    onValueChange = { response = it },
                    placeholder = { Text("Write your response...", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.inputBackground,
                        unfocusedContainerColor = colors.inputBackground,
                        focusedIndicatorColor = colors.textPrimary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(response) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.titleMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary, style = MaterialTheme.typography.titleMedium)
            }
        },
        containerColor = colors.background,
        shape = RoundedCornerShape(20.dp)
    )
}
