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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.viewmodel.TaskViewModel
import space.zeroxv6.journex.shared.model.*
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopScheduleScreen(viewModel: TaskViewModel, dataStore: space.zeroxv6.journex.shared.data.JsonDataStore) {
    val allSchedules by viewModel.allSchedules.collectAsState()
    val settings by dataStore.settings.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ScheduleItem?>(null) }
    val enabledSchedules = allSchedules.filter { it.isEnabled }.sortedBy { it.time }
    val disabledSchedules = allSchedules.filter { !it.isEnabled }.sortedBy { it.time }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).padding(40.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Schedule", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${enabledSchedules.size} active · ${disabledSchedules.size} disabled", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
            }
            Button(onClick = { showAddDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("New Schedule", style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (allSchedules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(shape = CircleShape, color = AppColors.current.inputBackground) { Icon(Icons.Outlined.Schedule, null, modifier = Modifier.padding(32.dp).size(56.dp), tint = AppColors.current.textDisabled) }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("No schedules", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Create a recurring schedule", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textTertiary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (enabledSchedules.isNotEmpty()) {
                    item { Text("Active Schedules", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(enabledSchedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { viewModel.toggleScheduleEnabled(schedule) },
                            onDelete = { viewModel.deleteSchedule(schedule.id) },
                            onEdit = { editingSchedule = it }
                        )
                    }
                }
                if (disabledSchedules.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(20.dp)); Text("Disabled", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textTertiary, modifier = Modifier.padding(bottom = 8.dp)) }
                    items(disabledSchedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = { viewModel.toggleScheduleEnabled(schedule) },
                            onDelete = { viewModel.deleteSchedule(schedule.id) },
                            onEdit = { editingSchedule = it }
                        )
                    }
                }
            }
        }
    }
    if (showAddDialog) {
        ScheduleDialog(
            schedule = null,
            use24HourFormat = settings.use24HourFormat,
            onDismiss = { showAddDialog = false }
        ) { t, d, time, endTime, days ->
            viewModel.addSchedule(t, d, time, endTime, days)
            showAddDialog = false
        }
    }
    editingSchedule?.let { schedule ->
        ScheduleDialog(
            schedule = schedule,
            use24HourFormat = settings.use24HourFormat,
            onDismiss = { editingSchedule = null }
        ) { t, d, time, endTime, days ->
            viewModel.updateSchedule(schedule.copy(title = t, description = d, time = time, endTime = endTime, daysOfWeek = days))
            editingSchedule = null
        }
    }
}
@Composable
private fun ScheduleCard(schedule: ScheduleItem, onToggle: () -> Unit, onDelete: () -> Unit, onEdit: ((ScheduleItem) -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMenu by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.current.cardBackground,
        border = BorderStroke(1.dp, if (isHovered) AppColors.current.borderFocused else AppColors.current.border)
    ) {
        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = if (schedule.isEnabled) AppColors.current.textPrimary else AppColors.current.border) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        schedule.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (schedule.isEnabled) AppColors.current.background else AppColors.current.textTertiary
                    )
                    if (schedule.endTime != null) {
                        Text(
                            "to",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (schedule.isEnabled) AppColors.current.background.copy(alpha = 0.6f) else AppColors.current.textDisabled
                        )
                        Text(
                            schedule.endTime!!.format(DateTimeFormatter.ofPattern("h:mm a")),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (schedule.isEnabled) AppColors.current.background else AppColors.current.textTertiary
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (schedule.isEnabled) AppColors.current.textPrimary else AppColors.current.textDisabled
                )
                if (schedule.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(schedule.description, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayOfWeek.entries.forEach { day ->
                        val isActive = schedule.daysOfWeek.contains(day)
                        Surface(shape = CircleShape, color = if (isActive) AppColors.current.textPrimary else AppColors.current.inputBackground) {
                            Text(
                                day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (isActive) AppColors.current.background else AppColors.current.borderFocused
                            )
                        }
                    }
                }
            }
            Switch(
                checked = schedule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppColors.current.background,
                    checkedTrackColor = AppColors.current.textPrimary,
                    uncheckedThumbColor = AppColors.current.background,
                    uncheckedTrackColor = AppColors.current.border,
                    uncheckedBorderColor = AppColors.current.border
                )
            )
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(40.dp)) { 
                    Icon(Icons.Outlined.MoreVert, null, tint = AppColors.current.textTertiary, modifier = Modifier.size(24.dp)) 
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = AppColors.current.background) {
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text("Edit", style = MaterialTheme.typography.bodyLarge) },
                            onClick = { onEdit(schedule); showMenu = false },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                        )
                    }
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDialog(schedule: ScheduleItem?, use24HourFormat: Boolean, onDismiss: () -> Unit, onAdd: (String, String, LocalTime, LocalTime?, Set<DayOfWeek>) -> Unit) {
    var title by remember { mutableStateOf(schedule?.title ?: "") }
    var description by remember { mutableStateOf(schedule?.description ?: "") }
    var startTime by remember { mutableStateOf(schedule?.time ?: LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(schedule?.endTime ?: LocalTime.of(10, 0)) }
    var hasEndTime by remember { mutableStateOf(schedule?.endTime != null) }
    var selectedDays by remember { mutableStateOf(schedule?.daysOfWeek ?: setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) }
    val colors = AppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "New Schedule" else "Edit Schedule", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text("Start Time", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    space.zeroxv6.journex.desktop.ui.components.TimePicker(
                        time = startTime,
                        use24HourFormat = use24HourFormat,
                        onTimeChange = { startTime = it },
                        key = "start-time"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add End Time", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Switch(
                        checked = hasEndTime,
                        onCheckedChange = { hasEndTime = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.background,
                            checkedTrackColor = colors.textPrimary
                        )
                    )
                }
                if (hasEndTime) {
                    space.zeroxv6.journex.desktop.ui.components.TimePicker(
                        time = endTime,
                        use24HourFormat = use24HourFormat,
                        onTimeChange = { endTime = it },
                        key = "end-time"
                    )
                }
                Column {
                    Text("Days", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        DayOfWeek.entries.forEach { day ->
                            FilterChip(
                                selected = selectedDays.contains(day),
                                onClick = {
                                    selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                                },
                                label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) },
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
                    onAdd(title, description, startTime, if (hasEndTime) endTime else null, selectedDays)
                },
                enabled = title.isNotEmpty() && selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.textPrimary)
            ) {
                Text(if (schedule == null) "Add" else "Save")
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
