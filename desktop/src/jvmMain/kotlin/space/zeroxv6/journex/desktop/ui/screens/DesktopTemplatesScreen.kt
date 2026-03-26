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
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.shared.model.CustomTemplate
data class JournalTemplate(val id: String, val title: String, val description: String, val content: String, val category: String, val isCustom: Boolean = false)
val defaultTemplates = listOf(
    JournalTemplate("daily", "Daily Reflection", "Reflect on your day with guided prompts", "What happened today?\n\nWhat am I grateful for?\n\nWhat did I learn?\n\nWhat could I improve tomorrow?\n\nHow am I feeling?", "Daily"),
    JournalTemplate("morning", "Morning Pages", "Start your day with free writing", "Today's Date:\n\nMorning thoughts and intentions:\n\nWhat I want to accomplish today:\n\nHow I'm feeling this morning:", "Daily"),
    JournalTemplate("gratitude", "Gratitude Journal", "Focus on what you're thankful for", "Three things I'm grateful for today:\n1.\n2.\n3.\n\nWhy these matter to me:\n\nSomeone who made my day better:\n\nA small moment I appreciated:", "Wellness"),
    JournalTemplate("goals", "Goal Setting", "Plan and track your goals", "Goal:\n\nWhy this matters to me:\n\nAction steps:\n1.\n2.\n3.\n\nTimeline:\n\nPotential obstacles:\n\nHow I'll overcome them:", "Productivity"),
    JournalTemplate("weekly", "Weekly Review", "Reflect on your week", "Week of:\n\nHighlights:\n\nChallenges:\n\nLessons learned:\n\nNext week's priorities:\n\nWhat I want to focus on:", "Productivity"),
    JournalTemplate("mood", "Mood Tracker", "Track your emotional patterns", "Current mood:\n\nEnergy level (1-10):\n\nWhat influenced my mood today:\n\nCoping strategies that helped:\n\nNotes:", "Wellness"),
    JournalTemplate("creative", "Creative Writing", "Free form creative expression", "Title:\n\nGenre/Theme:\n\nStory/Poem/Thoughts:\n\n", "Creative"),
    JournalTemplate("selfcare", "Self-Care Check-in", "Monitor your wellbeing", "Physical health:\n\nMental health:\n\nEmotional state:\n\nSocial connections:\n\nWhat I need today:\n\nSelf-care activities planned:", "Wellness"),
    JournalTemplate("dream", "Dream Journal", "Record and analyze your dreams", "Date:\n\nDream description:\n\nEmotions felt:\n\nRecurring themes or symbols:\n\nPossible meanings:", "Personal"),
    JournalTemplate("problem", "Problem Solving", "Work through challenges systematically", "Problem:\n\nCurrent situation:\n\nPossible solutions:\n1.\n2.\n3.\n\nPros and cons:\n\nBest approach:\n\nAction plan:", "Productivity")
)
@Composable
fun DesktopTemplatesScreen(dataStore: JsonDataStore, onUseTemplate: (String, String) -> Unit) {
    val customTemplates by dataStore.templates.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val allTemplates = defaultTemplates + customTemplates.map { 
        JournalTemplate(it.id, it.title, it.description, it.content, it.category, isCustom = true) 
    }
    val categories = allTemplates.map { it.category }.distinct().sorted()
    val filtered = if (selectedCategory != null) allTemplates.filter { it.category == selectedCategory } else allTemplates
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Templates", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Choose a template to start writing", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Button(onClick = { showCreateDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Create Template", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item { FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All", style = MaterialTheme.typography.bodyMedium) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background)) }
            items(categories) { cat -> FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat, style = MaterialTheme.typography.bodyMedium) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.current.textPrimary, selectedLabelColor = AppColors.current.background)) }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("${filtered.size} ${if (filtered.size == 1) "template" else "templates"}", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textTertiary)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filtered.chunked(2)) { rowTemplates -> 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowTemplates.forEach { template ->
                        Box(modifier = Modifier.weight(1f)) {
                            TemplateCard(
                                template = template,
                                onClick = { onUseTemplate(template.title, template.content) },
                                onDelete = if (template.isCustom) {{ dataStore.deleteTemplate(template.id) }} else null
                            )
                        }
                    }
                    if (rowTemplates.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (showCreateDialog) {
        CreateTemplateDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, content, category -> 
                dataStore.saveTemplate(CustomTemplate(title = title, description = desc, content = content, category = category))
                showCreateDialog = false 
            }
        )
    }
}
@Composable
private fun TemplateCard(template: JournalTemplate, onClick: () -> Unit, onDelete: (() -> Unit)?) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(interactionSource = interactionSource, indication = null, onClick = onClick).hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(template.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                        if (template.isCustom) {
                            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.current.border) {
                                Text("Custom", modifier = Modifier.padding(8.dp, 4.dp), style = MaterialTheme.typography.labelMedium, color = AppColors.current.textPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(template.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(10.dp), color = AppColors.current.surfaceTertiary) { 
                        Icon(Icons.Outlined.ArrowForward, null, modifier = Modifier.padding(10.dp).size(22.dp), tint = AppColors.current.textSecondary) 
                    }
                    if (onDelete != null) {
                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(42.dp)) { 
                                Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(24.dp)) 
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                                DropdownMenuItem(
                                    text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) },
                                    onClick = { onDelete(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = AppColors.current.inputBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
                Text(
                    template.content.take(180) + if (template.content.length > 180) "..." else "",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                    color = AppColors.current.textSecondary,
                    maxLines = 4
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = AppColors.current.surfaceTertiary) { 
                Text(template.category, modifier = Modifier.padding(12.dp, 6.dp), style = MaterialTheme.typography.labelLarge, color = AppColors.current.textSecondary) 
            }
        }
    }
}
@Composable
private fun CreateTemplateDialog(onDismiss: () -> Unit, onCreate: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom") }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Template", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Template Title") },
                    placeholder = { Text("e.g., My Daily Routine") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Description") },
                    placeholder = { Text("Brief description of this template") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
                OutlinedTextField(
                    value = category, onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g., Personal, Work, Health") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    label = { Text("Template Content") },
                    placeholder = { Text("Write your template prompts here...") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colors.textPrimary, focusedLabelColor = colors.textPrimary)
                )
            }
        },
        confirmButton = { 
            Button(
                onClick = { if (title.isNotEmpty() && content.isNotEmpty()) onCreate(title, desc, content, category) },
                enabled = title.isNotEmpty() && content.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(44.dp)
            ) { Text("Create", style = MaterialTheme.typography.titleMedium) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.textSecondary, style = MaterialTheme.typography.titleMedium) } },
        containerColor = colors.background,
        shape = RoundedCornerShape(20.dp)
    )
}
