package space.zeroxv6.journex.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.model.Mood
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.ui.theme.FeatureColors
import space.zeroxv6.journex.viewmodel.JournalViewModel
data class JournalTemplate(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: String
)
val journalTemplates = listOf(
    JournalTemplate(
        id = "daily_reflection",
        title = "Daily Reflection",
        description = "Reflect on your day with guided prompts",
        content = """What happened today?
What am I grateful for?
What did I learn?
What could I improve tomorrow?
How am I feeling?""",
        category = "Daily"
    ),
    JournalTemplate(
        id = "morning_pages",
        title = "Morning Pages",
        description = "Start your day with free writing",
        content = """Today's Date: 
Morning thoughts and intentions:
What I want to accomplish today:
How I'm feeling this morning:""",
        category = "Daily"
    ),
    JournalTemplate(
        id = "gratitude",
        title = "Gratitude Journal",
        description = "Focus on what you're thankful for",
        content = """Three things I'm grateful for today:
1. 
2. 
3. 
Why these matter to me:
Someone who made my day better:
A small moment I appreciated:""",
        category = "Wellness"
    ),
    JournalTemplate(
        id = "goal_setting",
        title = "Goal Setting",
        description = "Plan and track your goals",
        content = """Goal:
Why this matters to me:
Action steps:
1. 
2. 
3. 
Timeline:
Potential obstacles:
How I'll overcome them:""",
        category = "Productivity"
    ),
    JournalTemplate(
        id = "weekly_review",
        title = "Weekly Review",
        description = "Reflect on your week",
        content = """Week of:
Highlights:
Challenges:
Lessons learned:
Next week's priorities:
What I want to focus on:""",
        category = "Productivity"
    ),
    JournalTemplate(
        id = "dream_journal",
        title = "Dream Journal",
        description = "Record and analyze your dreams",
        content = """Date:
Dream description:
Emotions felt:
Recurring themes or symbols:
Possible meanings:""",
        category = "Personal"
    ),
    JournalTemplate(
        id = "mood_tracker",
        title = "Mood Tracker",
        description = "Track your emotional patterns",
        content = """Current mood:
Energy level (1-10):
What influenced my mood today:
Coping strategies that helped:
Notes:""",
        category = "Wellness"
    ),
    JournalTemplate(
        id = "creative_writing",
        title = "Creative Writing",
        description = "Free form creative expression",
        content = """Title:
Genre/Theme:
Story/Poem/Thoughts:
""",
        category = "Creative"
    ),
    JournalTemplate(
        id = "problem_solving",
        title = "Problem Solving",
        description = "Work through challenges systematically",
        content = """Problem:
Current situation:
Possible solutions:
1. 
2. 
3. 
Pros and cons:
Best approach:
Action plan:""",
        category = "Productivity"
    ),
    JournalTemplate(
        id = "self_care",
        title = "Self-Care Check-in",
        description = "Monitor your wellbeing",
        content = """Physical health:
Mental health:
Emotional state:
Social connections:
What I need today:
Self-care activities planned:""",
        category = "Wellness"
    )
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    onTemplateSelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var customTemplates by remember { mutableStateOf<List<JournalTemplate>>(emptyList()) }
    val allTemplates = journalTemplates + customTemplates
    val categories = allTemplates.map { it.category }.distinct().sorted()
    val filteredTemplates = if (selectedCategory != null) {
        allTemplates.filter { it.category == selectedCategory }
    } else {
        allTemplates
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Templates",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Create Template")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Choose a template to start writing",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val allCategories = listOf(null) + categories
                        allCategories.chunked(3).forEach { categoryRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categoryRow.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { 
                                            Text(
                                                category ?: "All",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            ) 
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                            selectedLabelColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                                repeat(3 - categoryRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            viewModel.createNewEntry()
                            viewModel.currentEntry?.let { entry ->
                                viewModel.updateCurrentEntry(
                                    entry.copy(
                                        title = template.title,
                                        content = template.content
                                    )
                                )
                            }
                            onTemplateSelected(template.id)
                        }
                    )
                }
            }
        }
    }
    if (showCreateDialog) {
        var titleInput by remember { mutableStateOf("") }
        var descriptionInput by remember { mutableStateOf("") }
        var contentInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Custom") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    "Create Custom Template",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Template title", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        placeholder = { Text("Description", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        placeholder = { Text("Category", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        placeholder = { Text("Template content...", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                        modifier = Modifier.height(200.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                            val newTemplate = JournalTemplate(
                                id = "custom_${System.currentTimeMillis()}",
                                title = titleInput,
                                description = descriptionInput,
                                content = contentInput,
                                category = categoryInput
                            )
                            customTemplates = customTemplates + newTemplate
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
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
fun TemplateCard(
    template: JournalTemplate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Outlined.ArrowForward,
                    contentDescription = "Use template",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = FeatureColors.TemplatesAccent
            ) {
                Text(
                    text = template.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
