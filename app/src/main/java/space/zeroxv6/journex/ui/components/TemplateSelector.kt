package space.zeroxv6.journex.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.model.NoteCategory
import space.zeroxv6.journex.model.NoteTemplate
private val BackgroundColor = Color(0xFFF8F8F8)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFF0A0A0A)
private val SecondaryColor = Color(0xFF6B6B6B)
private val TertiaryColor = Color(0xFF9E9E9E)
private val BorderColor = Color(0xFFE8E8E8)
private val AccentColor = Color(0xFF1F1F1F)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelector(
    onTemplateSelected: (NoteTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val templates = remember { getDefaultTemplates() }
    var selectedCategory by remember { mutableStateOf<NoteCategory?>(null) }
    val filteredTemplates = if (selectedCategory != null) {
        templates.filter { it.category == selectedCategory }
    } else {
        templates
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                "Choose Template",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                fontWeight = FontWeight.Normal,
                color = PrimaryColor
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = {
                            Text(
                                "All",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedCategory == null) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = BackgroundColor,
                            selectedContainerColor = AccentColor,
                            labelColor = SecondaryColor,
                            selectedLabelColor = SurfaceColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = BorderColor,
                            selectedBorderColor = AccentColor,
                            borderWidth = 1.dp
                        )
                    )
                }
                items(NoteCategory.entries.toList()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                category.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedCategory == category) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = BackgroundColor,
                            selectedContainerColor = AccentColor,
                            labelColor = SecondaryColor,
                            selectedLabelColor = SurfaceColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = BorderColor,
                            selectedBorderColor = AccentColor,
                            borderWidth = 1.dp
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                items(filteredTemplates) { template ->
                    TemplateCard(
                        template = template,
                        onClick = {
                            onTemplateSelected(template)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
@Composable
fun TemplateCard(
    template: NoteTemplate,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceColor,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        template.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AccentColor
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryColor
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        ),
                        color = SecondaryColor,
                        maxLines = 1
                    )
                }
            }
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TertiaryColor
            )
        }
    }
}
fun getDefaultTemplates(): List<NoteTemplate> {
    return listOf(
        NoteTemplate(
            name = "Meeting Notes",
            description = "Structured template for meeting documentation",
            content = """# Meeting Notes
**Date:** ${java.time.LocalDate.now()}
**Attendees:** 
**Duration:** 
## Agenda
- 
## Discussion Points
- 
## Action Items
- [ ] 
## Next Steps
- 
## Notes
""",
            category = NoteCategory.MEETINGS,
            icon = Icons.Outlined.Groups
        ),
        NoteTemplate(
            name = "Project Plan",
            description = "Comprehensive project planning template",
            content = """# Project Plan
## Overview
**Project Name:** 
**Start Date:** 
**End Date:** 
**Status:** 
## Objectives
- 
## Milestones
- [ ] 
## Resources
- 
## Risks
- 
## Timeline
""",
            category = NoteCategory.PROJECTS,
            icon = Icons.Outlined.Assignment
        ),
        NoteTemplate(
            name = "Daily Journal",
            description = "Reflect on your day",
            content = """# Daily Journal - ${java.time.LocalDate.now()}
## Morning Thoughts
- 
## Today's Goals
- [ ] 
- [ ] 
- [ ] 
## Gratitude
- 
## Evening Reflection
- 
## Tomorrow's Plan
- 
""",
            category = NoteCategory.PERSONAL,
            icon = Icons.Outlined.Book
        ),
        NoteTemplate(
            name = "Research Notes",
            description = "Organize research findings",
            content = """# Research Notes
**Topic:** 
**Date:** ${java.time.LocalDate.now()}
**Source:** 
## Key Findings
- 
## Methodology
- 
## Data
- 
## Analysis
- 
## Conclusions
- 
## References
- 
""",
            category = NoteCategory.RESEARCH,
            icon = Icons.Outlined.Science
        ),
        NoteTemplate(
            name = "Recipe",
            description = "Document your favorite recipes",
            content = """# Recipe Name
**Prep Time:** 
**Cook Time:** 
**Servings:** 
**Difficulty:** 
## Ingredients
- 
## Instructions
1. 
## Notes
- 
## Tags
#recipe
""",
            category = NoteCategory.RECIPES,
            icon = Icons.Outlined.Restaurant
        ),
        NoteTemplate(
            name = "Travel Itinerary",
            description = "Plan your trips",
            content = """# Travel Itinerary
**Destination:** 
**Dates:** 
**Budget:** 
## Day 1
- [ ] 
## Day 2
- [ ] 
## Accommodation
- 
## Transportation
- 
## Packing List
- [ ] 
## Important Contacts
- 
""",
            category = NoteCategory.TRAVEL,
            icon = Icons.Outlined.Flight
        ),
        NoteTemplate(
            name = "Study Notes",
            description = "Structured learning notes",
            content = """# Study Notes
**Subject:** 
**Topic:** 
**Date:** ${java.time.LocalDate.now()}
## Key Concepts
- 
## Definitions
- 
## Examples
- 
## Practice Questions
1. 
## Summary
- 
## Review Date
""",
            category = NoteCategory.STUDY,
            icon = Icons.Outlined.School
        ),
        NoteTemplate(
            name = "Brainstorm",
            description = "Capture creative ideas",
            content = """# Brainstorm Session
**Topic:** 
**Date:** ${java.time.LocalDate.now()}
## Ideas
- 
- 
- 
## Pros & Cons
**Pros:**
- 
**Cons:**
- 
## Next Actions
- [ ] 
""",
            category = NoteCategory.IDEAS,
            icon = Icons.Outlined.Lightbulb
        ),
        NoteTemplate(
            name = "Budget Tracker",
            description = "Track expenses and income",
            content = """# Budget Tracker - ${java.time.LocalDate.now()}
## Income
- 
## Expenses
### Fixed
- 
### Variable
- 
## Savings Goals
- [ ] 
## Notes
- 
""",
            category = NoteCategory.FINANCE,
            icon = Icons.Outlined.AccountBalance
        ),
        NoteTemplate(
            name = "Workout Log",
            description = "Track your fitness journey",
            content = """# Workout Log - ${java.time.LocalDate.now()}
## Warm-up
- 
## Exercises
1. 
## Cool-down
- 
## Notes
- **Duration:** 
- **Intensity:** 
- **How I felt:** 
## Next Session
- 
""",
            category = NoteCategory.HEALTH,
            icon = Icons.Outlined.FitnessCenter
        ),
        NoteTemplate(
            name = "Book Notes",
            description = "Capture insights from reading",
            content = """# Book Notes
**Title:** 
**Author:** 
**Date Started:** 
**Date Finished:** 
## Summary
- 
## Key Takeaways
- 
## Favorite Quotes
> 
## My Thoughts
- 
## Rating
5/5
""",
            category = NoteCategory.STUDY,
            icon = Icons.Outlined.MenuBook
        ),
        NoteTemplate(
            name = "Blank Note",
            description = "Start from scratch",
            content = "",
            category = NoteCategory.OTHER,
            icon = Icons.Outlined.Note
        )
    )
}
