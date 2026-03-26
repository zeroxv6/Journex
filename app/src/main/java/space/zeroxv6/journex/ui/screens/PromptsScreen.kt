package space.zeroxv6.journex.ui.screens
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.ui.animations.bounceClick
import space.zeroxv6.journex.viewmodel.JournalViewModel
import kotlin.random.Random
data class WritingPrompt(
    val id: Int,
    val prompt: String,
    val category: String
)
val writingPrompts = listOf(
    WritingPrompt(1, "What made you smile today?", "Gratitude"),
    WritingPrompt(2, "Describe a challenge you overcame recently.", "Growth"),
    WritingPrompt(3, "What are you looking forward to?", "Future"),
    WritingPrompt(4, "Write about a person who inspires you.", "People"),
    WritingPrompt(5, "What did you learn today?", "Learning"),
    WritingPrompt(6, "Describe your perfect day.", "Dreams"),
    WritingPrompt(7, "What are you grateful for right now?", "Gratitude"),
    WritingPrompt(8, "What's something you want to improve about yourself?", "Growth"),
    WritingPrompt(9, "Write about a memorable conversation.", "People"),
    WritingPrompt(10, "What does success mean to you?", "Reflection"),
    WritingPrompt(11, "Describe a place that brings you peace.", "Places"),
    WritingPrompt(12, "What's a habit you want to develop?", "Goals"),
    WritingPrompt(13, "Write about a time you felt proud.", "Achievements"),
    WritingPrompt(14, "What's your biggest fear and why?", "Emotions"),
    WritingPrompt(15, "Describe your ideal future self.", "Dreams"),
    WritingPrompt(16, "What's something you need to let go of?", "Reflection"),
    WritingPrompt(17, "Write about a book or movie that changed you.", "Inspiration"),
    WritingPrompt(18, "What makes you feel alive?", "Passion"),
    WritingPrompt(19, "Describe a difficult decision you made.", "Growth"),
    WritingPrompt(20, "What are your core values?", "Identity"),
    WritingPrompt(21, "Write about a childhood memory.", "Past"),
    WritingPrompt(22, "What's something you're curious about?", "Learning"),
    WritingPrompt(23, "Describe your relationship with time.", "Reflection"),
    WritingPrompt(24, "What would you tell your younger self?", "Wisdom"),
    WritingPrompt(25, "Write about a moment of unexpected joy.", "Gratitude"),
    WritingPrompt(26, "What's your definition of happiness?", "Philosophy"),
    WritingPrompt(27, "Describe a skill you want to master.", "Goals"),
    WritingPrompt(28, "What's your relationship with failure?", "Growth"),
    WritingPrompt(29, "Write about someone you need to forgive.", "Healing"),
    WritingPrompt(30, "What does home mean to you?", "Identity")
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptsScreen(
    viewModel: JournalViewModel,
    taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel,
    onNavigateBack: () -> Unit,
    onPromptSelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var randomPrompt by remember { mutableStateOf(writingPrompts.random()) }
    var showSavedPrompts by remember { mutableStateOf(true) } 
    var showResponseDialog by remember { mutableStateOf(false) }
    var selectedPromptForResponse by remember { mutableStateOf<space.zeroxv6.journex.viewmodel.SavedPrompt?>(null) }
    var responseText by remember { mutableStateOf("") }
    val savedPrompts by taskViewModel.allPrompts.collectAsState()
    val categories = writingPrompts.map { it.category }.distinct().sorted()
    val filteredPrompts = if (selectedCategory != null) {
        writingPrompts.filter { it.category == selectedCategory }
    } else {
        writingPrompts
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Writing Prompts",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSavedPrompts = !showSavedPrompts }) {
                            Icon(
                                if (showSavedPrompts) Icons.Outlined.Lightbulb else Icons.Outlined.LibraryBooks,
                                contentDescription = if (showSavedPrompts) "Browse prompts" else "My prompts"
                            )
                        }
                        if (!showSavedPrompts) {
                            IconButton(onClick = { randomPrompt = writingPrompts.random() }) {
                                Icon(Icons.Outlined.Shuffle, contentDescription = "Random")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    ) { padding ->
        if (showSavedPrompts) {
            if (savedPrompts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.LibraryBooks,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "No Saved Prompts",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Save prompts with your responses here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "My Prompts (${savedPrompts.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(savedPrompts) { savedPrompt ->
                        SavedPromptCard(
                            prompt = savedPrompt,
                            onEdit = {
                                selectedPromptForResponse = savedPrompt
                                responseText = savedPrompt.response
                                showResponseDialog = true
                            },
                            onDelete = {
                                taskViewModel.deletePrompt(savedPrompt)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.createNewEntry()
                            viewModel.currentEntry?.let { entry ->
                                viewModel.updateCurrentEntry(
                                    entry.copy(
                                        title = "Prompt: ${randomPrompt.prompt}",
                                        content = ""
                                    )
                                )
                            }
                            onPromptSelected(randomPrompt.prompt)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            text = "Prompt of the Moment",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = randomPrompt.prompt,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = randomPrompt.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                        Text(
                            text = "Tap to start writing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Browse by Category",
                        style = MaterialTheme.typography.titleMedium
                    )
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
                                            maxLines = 1
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
                items(filteredPrompts) { prompt ->
                    PromptCard(
                        prompt = prompt,
                        onClick = {
                            selectedPromptForResponse = space.zeroxv6.journex.viewmodel.SavedPrompt(
                                id = "",
                                promptText = prompt.prompt,
                                category = prompt.category,
                                response = "",
                                createdAt = java.time.LocalDateTime.now(),
                                updatedAt = java.time.LocalDateTime.now()
                            )
                            responseText = ""
                            showResponseDialog = true
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
        if (showResponseDialog && selectedPromptForResponse != null) {
            AlertDialog(
                onDismissRequest = { showResponseDialog = false },
                title = {
                    Text(
                        "Write Your Response",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = selectedPromptForResponse!!.promptText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextField(
                            value = responseText,
                            onValueChange = { responseText = it },
                            placeholder = { Text("Write your thoughts...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
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
                            if (selectedPromptForResponse!!.id.isNotEmpty()) {
                                taskViewModel.updatePrompt(
                                    selectedPromptForResponse!!.copy(
                                        response = responseText,
                                        updatedAt = java.time.LocalDateTime.now()
                                    )
                                )
                            } else {
                                taskViewModel.addPrompt(
                                    promptText = selectedPromptForResponse!!.promptText,
                                    category = selectedPromptForResponse!!.category,
                                    response = responseText
                                )
                            }
                            showResponseDialog = false
                            showSavedPrompts = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResponseDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
@Composable
fun PromptCard(
    prompt: WritingPrompt,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = prompt.prompt,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = prompt.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Icon(
                Icons.Outlined.ArrowForward,
                contentDescription = "Use prompt",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
fun SavedPromptCard(
    prompt: space.zeroxv6.journex.viewmodel.SavedPrompt,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
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
                        text = prompt.promptText,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (prompt.response.isNotEmpty()) {
                        Text(
                            text = prompt.response,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "No response yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                androidx.compose.foundation.layout.Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Edit", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Delete", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = prompt.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = prompt.updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Prompt",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete this prompt and your response?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
