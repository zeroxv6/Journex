package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopRemindersScreen(viewModel: TaskViewModel, dataStore: space.zeroxv6.journex.shared.data.JsonDataStore) {
    val upcomingReminders by viewModel.upcomingReminders.collectAsState()
    val completedReminders by viewModel.completedReminders.collectAsState()
    val settings by dataStore.settings.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(false) }
    val todayReminders = upcomingReminders.filter { it.dateTime.toLocalDate() == LocalDate.now() }
    val tomorrowReminders = upcomingReminders.filter { it.dateTime.toLocalDate() == LocalDate.now().plusDays(1) }
    val laterReminders = upcomingReminders.filter { it.dateTime.toLocalDate().isAfter(LocalDate.now().plusDays(1)) }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Reminders", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${upcomingReminders.size} upcoming · ${completedReminders.size} completed", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { showCompleted = !showCompleted }) { Text(if (showCompleted) "Hide Done" else "Show Done", color = AppColors.current.textSecondary, style = MaterialTheme.typography.titleMedium) }
                Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("New Reminder", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (upcomingReminders.isEmpty() && (!showCompleted || completedReminders.isEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) { Icon(Icons.Outlined.NotificationsNone, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No reminders", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add a reminder to stay on track", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (todayReminders.isNotEmpty()) {
                    item { Text("Today", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(todayReminders, key = { it.id }) { reminder -> ReminderCard(reminder, { viewModel.toggleReminderCompletion(reminder) }, { viewModel.deleteReminder(reminder.id) }) }
                }
                if (tomorrowReminders.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(12.dp)); Text("Tomorrow", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(tomorrowReminders, key = { it.id }) { reminder -> ReminderCard(reminder, { viewModel.toggleReminderCompletion(reminder) }, { viewModel.deleteReminder(reminder.id) }) }
                }
                if (laterReminders.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(12.dp)); Text("Later", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textTertiary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(laterReminders, key = { it.id }) { reminder -> ReminderCard(reminder, { viewModel.toggleReminderCompletion(reminder) }, { viewModel.deleteReminder(reminder.id) }) }
                }
                if (showCompleted && completedReminders.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(12.dp)); Text("Completed", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textDisabled, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(completedReminders, key = { it.id }) { reminder -> ReminderCard(reminder, { viewModel.toggleReminderCompletion(reminder) }, { viewModel.deleteReminder(reminder.id) }) }
                }
            }
        }
    }
    if (showAddDialog) ReminderDialog(use24HourFormat = settings.use24HourFormat, onDismiss = { showAddDialog = false }) { t, d, dt, c, r -> viewModel.addReminder(t, d, dt, c, r); showAddDialog = false }
}
@Composable
private fun ReminderCard(reminder: Reminder, onToggle: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) { 
                Icon(
                    if (reminder.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    null,
                    tint = if (reminder.isCompleted) AppColors.current.textSecondary else AppColors.current.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    color = if (reminder.isCompleted) AppColors.current.textDisabled else AppColors.current.textPrimary
                )
                if (reminder.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(reminder.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textTertiary)
                        Text(
                            reminder.dateTime.format(DateTimeFormatter.ofPattern("MMMM d, h:mm a")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.current.textSecondary
                        )
                    }
                    if (reminder.repeatType != RepeatType.NONE) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Repeat, null, modifier = Modifier.size(18.dp), tint = AppColors.current.textTertiary)
                            Text(reminder.repeatType.label, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary)
                        }
                    }
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = AppColors.current.surfaceTertiary) {
                Text(
                    reminder.category.label,
                    modifier = Modifier.padding(12.dp, 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = AppColors.current.textSecondary
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) { 
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(24.dp)) 
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                    DropdownMenuItem(text = { Text("Delete", style = MaterialTheme.typography.bodyLarge) }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Outlined.Delete, null) })
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDialog(use24HourFormat: Boolean, onDismiss: () -> Unit, onAdd: (String, String, LocalDateTime, ReminderCategory, RepeatType) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var category by remember { mutableStateOf(ReminderCategory.GENERAL) }
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Reminder", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.textPrimary,
                        focusedLabelColor = colors.textPrimary
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.textPrimary,
                        focusedLabelColor = colors.textPrimary
                    )
                )
                Column {
                    Text("Date", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.textPrimary,
                            unfocusedBorderColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = { selectedDate = LocalDate.now() },
                            label = { Text("Today") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { selectedDate = LocalDate.now().plusDays(1) },
                            label = { Text("Tomorrow") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { selectedDate = LocalDate.now().plusWeeks(1) },
                            label = { Text("Next Week") }
                        )
                    }
                }
                Column {
                    Text("Time", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    space.zeroxv6.journex.desktop.ui.components.TimePicker(
                        time = selectedTime,
                        use24HourFormat = use24HourFormat,
                        onTimeChange = { selectedTime = it }
                    )
                }
                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        ReminderCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.textPrimary,
                                    selectedLabelColor = colors.background
                                )
                            )
                        }
                    }
                }
                Column {
                    Text("Repeat", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        RepeatType.entries.forEach { rt ->
                            FilterChip(
                                selected = repeatType == rt,
                                onClick = { repeatType = rt },
                                label = { Text(rt.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.textPrimary,
                                    selectedLabelColor = colors.background
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(title, description, LocalDateTime.of(selectedDate, selectedTime), category, repeatType)
                },
                enabled = title.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textSecondary)
            }
        },
        containerColor = colors.background,
        shape = RoundedCornerShape(20.dp)
    )
}
