package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.shared.data.JsonDataStore
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.desktop.ui.theme.AppTheme
import space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler
import kotlinx.coroutines.launch
import java.time.LocalTime
@Composable
fun DesktopSettingsScreen(dataStore: JsonDataStore, onClearData: () -> Unit) {
    val settings by dataStore.settings.collectAsState()
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showThemePickerDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showFetchDialog by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val driveSync = remember { 
        space.zeroxv6.journex.desktop.sync.DesktopGoogleDriveSync(
            java.io.File(System.getProperty("user.home"), ".journaling")
        )
    }
    var isSignedIn by remember { mutableStateOf(driveSync.isAuthenticated()) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val email = driveSync.getUserEmail() ?: "Signed in to Google"
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    userEmail = email
                }
            }
        } else {
            userEmail = null
        }
    }
    Column(modifier = Modifier.fillMaxSize().background(AppColors.current.background).verticalScroll(rememberScrollState()).padding(40.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text("Customize your experience", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
        Spacer(modifier = Modifier.height(32.dp))
        SettingsSection("Privacy & Security") {
            SettingsRow(
                icon = Icons.Outlined.Lock,
                title = "PIN Lock",
                subtitle = if (settings.pinCode.isNotEmpty()) "PIN is set" else "Protect your journal with a PIN",
                onClick = { showPinDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSection("General") {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = if (settings.notificationsEnabled) "Enabled" else "Disabled",
                trailing = {
                    Switch(checked = settings.notificationsEnabled, onCheckedChange = { dataStore.saveSettings(settings.copy(notificationsEnabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = AppColors.current.background, checkedTrackColor = AppColors.current.textPrimary, uncheckedThumbColor = AppColors.current.background, uncheckedTrackColor = AppColors.current.border, uncheckedBorderColor = AppColors.current.border))
                }
            )
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(
                icon = Icons.Outlined.Schedule,
                title = "Daily Reminder",
                subtitle = if (settings.journalReminderEnabled) "${String.format("%02d:%02d", settings.journalReminderHour, settings.journalReminderMinute)}" else "Disabled",
                onClick = if (settings.journalReminderEnabled) ({ showTimePickerDialog = true }) else null,
                trailing = {
                    Switch(
                        checked = settings.journalReminderEnabled, 
                        onCheckedChange = { enabled ->
                            dataStore.saveSettings(settings.copy(journalReminderEnabled = enabled))
                            if (enabled && settings.notificationsEnabled) {
                                DesktopAlarmScheduler.scheduleJournalReminder(
                                    settings.journalReminderHour,
                                    settings.journalReminderMinute
                                )
                            } else {
                                DesktopAlarmScheduler.cancelJournalReminder()
                            }
                        }, 
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.current.background, 
                            checkedTrackColor = AppColors.current.textPrimary, 
                            uncheckedThumbColor = AppColors.current.background, 
                            uncheckedTrackColor = AppColors.current.border, 
                            uncheckedBorderColor = AppColors.current.border
                        )
                    )
                }
            )
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(
                icon = Icons.Outlined.AccessTime,
                title = "Time Format",
                subtitle = if (settings.use24HourFormat) "24-hour" else "12-hour (AM/PM)",
                trailing = {
                    Switch(checked = settings.use24HourFormat, onCheckedChange = { dataStore.saveSettings(settings.copy(use24HourFormat = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = AppColors.current.background, checkedTrackColor = AppColors.current.textPrimary, uncheckedThumbColor = AppColors.current.background, uncheckedTrackColor = AppColors.current.border, uncheckedBorderColor = AppColors.current.border))
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSection("Data") {
            SettingsRow(Icons.Outlined.Folder, "Storage Location", "~/.journaling")
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            if (isSignedIn && userEmail != null) {
                SettingsRow(
                    Icons.Outlined.AccountCircle,
                    "Google Account",
                    userEmail ?: "Signed in",
                    trailing = {
                        TextButton(
                            onClick = {
                                driveSync.signOut()
                                isSignedIn = false
                                userEmail = null
                                syncMessage = "Signed out successfully"
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Sign Out")
                        }
                    }
                )
            } else {
                SettingsRow(
                    Icons.Outlined.AccountCircle,
                    "Google Account",
                    "Sign in to enable backup & restore",
                    onClick = { 
                        isSyncing = true
                        scope.launch {
                            try {
                                driveSync.getDriveService()
                                isSyncing = false
                                isSignedIn = driveSync.isAuthenticated()
                                if (isSignedIn) {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        val email = driveSync.getUserEmail()
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            userEmail = email
                                            syncMessage = "Signed in successfully!"
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                isSyncing = false
                                syncMessage = "Sign-in failed: ${e.message}"
                            }
                        }
                    }
                )
            }
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(
                Icons.Outlined.Upload, 
                "Sync to Google Drive", 
                if (isSyncing) "Syncing..." else if (!isSignedIn) "Sign in required" else "Backup your data", 
                onClick = if (isSignedIn && !isSyncing) ({ showSyncDialog = true }) else null
            )
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(
                Icons.Outlined.Download, 
                "Fetch from Google Drive", 
                if (!isSignedIn) "Sign in required" else "Restore from backup (overwrites local data)", 
                onClick = if (isSignedIn && !isSyncing) ({ showFetchDialog = true }) else null
            )
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(
                Icons.Outlined.Sync, 
                "Auto Sync", 
                "Not available on desktop (use manual sync)"
            )
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(Icons.Outlined.FileDownload, "Export Data", "Export as JSON file", onClick = {  })
            HorizontalDivider(color = AppColors.current.surfaceTertiary)
            SettingsRow(Icons.Outlined.DeleteForever, "Clear All Data", "Delete everything permanently", isDestructive = true, onClick = { showClearDataDialog = true })
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSection("Appearance") {
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = "Theme",
                subtitle = AppTheme.fromName(settings.theme).displayName,
                onClick = { showThemePickerDialog = true }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSection("About") {
            SettingsRow(Icons.Outlined.Info, "Journex", "Version 1.0.0", onClick = { showAboutDialog = true })
        }
        Spacer(modifier = Modifier.height(40.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Journex Desktop", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textTertiary)
            Text("Made with ❤️ by zeroxv6", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textDisabled)
        }
    }
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear all data?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { Text("This will permanently delete all your journal entries, tasks, reminders, and settings. This action cannot be undone.") },
            confirmButton = { Button(onClick = { onClearData(); showClearDataDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)) { Text("Delete Everything") } },
            dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel", color = AppColors.current.textSecondary) } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Journex", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("A beautiful journaling app built with Kotlin Multiplatform and Compose.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = AppColors.current.inputBackground) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Features:", style = MaterialTheme.typography.labelLarge)
                            Text("• Journal with mood tracking", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                            Text("• Tasks and reminders", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                            Text("• Writing prompts & templates", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                            Text("• Quick notes", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                            Text("• Statistics & insights", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showAboutDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)) { Text("Close") } },
            containerColor = AppColors.current.background, shape = RoundedCornerShape(20.dp)
        )
    }
    if (showTimePickerDialog) {
        var selectedTime by remember { mutableStateOf(LocalTime.of(settings.journalReminderHour, settings.journalReminderMinute)) }
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text("Set Reminder Time", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                space.zeroxv6.journex.desktop.ui.components.TimePicker(
                    time = selectedTime,
                    use24HourFormat = settings.use24HourFormat,
                    onTimeChange = { selectedTime = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        dataStore.saveSettings(settings.copy(
                            journalReminderHour = selectedTime.hour, 
                            journalReminderMinute = selectedTime.minute
                        ))
                        if (settings.journalReminderEnabled && settings.notificationsEnabled) {
                            DesktopAlarmScheduler.scheduleJournalReminder(
                                selectedTime.hour,
                                selectedTime.minute
                            )
                        }
                        showTimePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel", color = AppColors.current.textSecondary)
                }
            },
            containerColor = AppColors.current.background,
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (showThemePickerDialog) {
        AlertDialog(
            onDismissRequest = { showThemePickerDialog = false },
            title = { Text("Choose Theme", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AppTheme.entries.forEach { theme ->
                        val isSelected = settings.theme == theme.name
                        val isEnabled = theme == AppTheme.WARM_CREAM
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable(enabled = isEnabled) {
                                if (isEnabled) {
                                    dataStore.saveSettings(settings.copy(theme = theme.name))
                                    showThemePickerDialog = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AppColors.current.selectedBackground else AppColors.current.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        theme.displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                        color = if (isEnabled) AppColors.current.textPrimary else AppColors.current.textDisabled
                                    )
                                    Text(
                                        when (theme) {
                                            AppTheme.CLASSIC_WHITE -> "Coming soon"
                                            AppTheme.WARM_CREAM -> "Warm and cozy"
                                            AppTheme.DARK_GRAY -> "Coming soon"
                                            AppTheme.PITCH_BLACK -> "Coming soon"
                                            AppTheme.CHARCOAL -> "Coming soon"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isEnabled) AppColors.current.textSecondary else AppColors.current.textDisabled
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, "Selected", tint = AppColors.current.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemePickerDialog = false }) {
                    Text("Close", color = AppColors.current.textSecondary)
                }
            },
            containerColor = AppColors.current.background,
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("Sync to Google Drive", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This will backup all your data to Google Drive. Continue?", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSyncDialog = false
                        isSyncing = true
                        scope.launch {
                            try {
                                val result = driveSync.syncToGoogleDrive()
                                isSyncing = false
                                syncMessage = if (result.isSuccess) {
                                    result.getOrNull()
                                } else {
                                    "Sync failed: ${result.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                isSyncing = false
                                syncMessage = "Failed: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)
                ) {
                    Text("Sync")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSyncDialog = false
                }) {
                    Text("Cancel", color = AppColors.current.textSecondary)
                }
            },
            containerColor = AppColors.current.background,
            shape = RoundedCornerShape(20.dp)
        )
    }
    if (showFetchDialog) {
        var showRestartDialog by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showFetchDialog = false },
            title = { Text("Fetch from Google Drive", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚠️ Warning:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                    Text("This will overwrite all local data with data from Google Drive. The app will need to restart to load the restored data. Continue?", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFetchDialog = false
                        isSyncing = true
                        scope.launch {
                            try {
                                val result = driveSync.fetchFromGoogleDrive()
                                isSyncing = false
                                if (result.isSuccess) {
                                    showRestartDialog = true
                                } else {
                                    syncMessage = "Fetch failed: ${result.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                isSyncing = false
                                syncMessage = "Fetch failed: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)
                ) {
                    Text("Fetch & Restart")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFetchDialog = false }) {
                    Text("Cancel", color = AppColors.current.textSecondary)
                }
            },
            containerColor = AppColors.current.background,
            shape = RoundedCornerShape(20.dp)
        )
        if (showRestartDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Restart Required", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
                text = { Text("Data has been restored successfully. Please restart the application to see your restored data.") },
                confirmButton = {
                    Button(
                        onClick = {
                            kotlin.system.exitProcess(0)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)
                    ) {
                        Text("Restart Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestartDialog = false; showFetchDialog = false }) {
                        Text("Later", color = AppColors.current.textSecondary)
                    }
                },
                containerColor = AppColors.current.background,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false; pinInput = ""; confirmPinInput = "" },
            title = { Text(if (settings.pinCode.isEmpty()) "Set PIN" else "Change PIN", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (settings.pinCode.isNotEmpty()) {
                        Text("Leave empty to remove PIN", style = MaterialTheme.typography.bodySmall, color = AppColors.current.textSecondary)
                    }
                    TextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("Enter 4-digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppColors.current.inputBackground,
                            unfocusedContainerColor = AppColors.current.inputBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinInput = it },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppColors.current.inputBackground,
                            unfocusedContainerColor = AppColors.current.inputBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (pinInput.isNotEmpty() && confirmPinInput.isNotEmpty() && pinInput != confirmPinInput) {
                        Text("PINs do not match", style = MaterialTheme.typography.bodySmall, color = AppColors.current.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.isEmpty() || (pinInput.length == 4 && pinInput == confirmPinInput)) {
                            dataStore.saveSettings(settings.copy(pinCode = pinInput))
                            showPinDialog = false
                            pinInput = ""
                            confirmPinInput = ""
                        }
                    },
                    enabled = pinInput.isEmpty() || (pinInput.length == 4 && pinInput == confirmPinInput),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.current.textPrimary, contentColor = AppColors.current.background)
                ) {
                    Text(if (pinInput.isEmpty()) "Remove PIN" else "Set PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; pinInput = ""; confirmPinInput = "" }) {
                    Text("Cancel", color = AppColors.current.textSecondary)
                }
            },
            containerColor = AppColors.current.background,
            shape = RoundedCornerShape(20.dp)
        )
    }
    syncMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(5000)
            syncMessage = null
        }
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomCenter) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.current.cardBackground,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(message, modifier = Modifier.weight(1f))
                    TextButton(onClick = { syncMessage = null }) {
                        Text("OK", color = AppColors.current.primary)
                    }
                }
            }
        }
    }
}
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = AppColors.current.textTertiary, modifier = Modifier.padding(bottom = 12.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
            Column(modifier = Modifier.padding(8.dp), content = content)
        }
    }
}
@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, isDestructive: Boolean = false, trailing: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (isDestructive) AppColors.current.textSecondary else AppColors.current.textSecondary, modifier = Modifier.size(26.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (isDestructive) AppColors.current.textSecondary else AppColors.current.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary)
        }
        if (trailing != null) trailing()
        else if (onClick != null) Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.current.borderFocused, modifier = Modifier.size(24.dp))
    }
}
