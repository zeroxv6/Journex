package space.zeroxv6.journex.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import space.zeroxv6.journex.viewmodel.JournalViewModel
import space.zeroxv6.journex.sync.GoogleDriveSync
import space.zeroxv6.journex.sync.GoogleSignInHelper
import space.zeroxv6.journex.notification.QuickNoteNotificationService
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var showRemovePinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var showPinVerifyForExport by remember { mutableStateOf(false) }
    var showPinVerifyForClear by remember { mutableStateOf(false) }
    var showPinVerifyForRemove by remember { mutableStateOf(false) }
    var pinVerifyInput by remember { mutableStateOf("") }
    var pinVerifyError by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(viewModel.journalReminderTime.hour) }
    var selectedMinute by remember { mutableStateOf(viewModel.journalReminderTime.minute) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { space.zeroxv6.journex.data.AppDatabase.getDatabase(context) }
    val driveSync = remember { GoogleDriveSync(context, database) }
    var autoSyncEnabled by remember { mutableStateOf(false) }
    var showAutoSyncDialog by remember { mutableStateOf(false) }
    var autoSyncInterval by remember { mutableStateOf(24) }
    var isSyncing by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showFetchDialog by remember { mutableStateOf(false) }
    var isSignedIn by remember { mutableStateOf(GoogleSignInHelper.isSignedIn(context)) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            userEmail = GoogleSignInHelper.getSignedInAccount(context)?.email
        } else {
            userEmail = null
        }
    }
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val signInResult = GoogleSignInHelper.handleSignInResult(result.data)
        signInResult.onSuccess { account ->
            isSignedIn = true
            syncMessage = "Successfully signed in as ${account.email}"
        }.onFailure { error ->
            syncMessage = error.message ?: "Sign-in failed. Please try again."
            android.widget.Toast.makeText(
                context,
                syncMessage,
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Lock,
                    title = "PIN Lock",
                    subtitle = if (viewModel.pinCode.isNotEmpty()) "PIN is set" else "Protect your journal with a PIN",
                    onClick = {
                        if (viewModel.pinCode.isEmpty()) {
                            showPinDialog = true
                        } else {
                            showPinVerifyForRemove = true
                        }
                    },
                    trailing = {
                        if (viewModel.pinCode.isNotEmpty()) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "PIN set",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (isSignedIn && userEmail != null) {
                item {
                    SettingCard(
                        icon = Icons.Outlined.AccountCircle,
                        title = "Google Account",
                        subtitle = userEmail ?: "Signed in",
                        trailing = {
                            TextButton(onClick = {
                                GoogleSignInHelper.signOut(context)
                                isSignedIn = false
                                userEmail = null
                                autoSyncEnabled = false
                                driveSync.cancelAutoSync()
                                syncMessage = "Signed out successfully"
                            }) {
                                Text("Sign Out", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            } else {
                item {
                    SettingCard(
                        icon = Icons.Outlined.AccountCircle,
                        title = "Google Account",
                        subtitle = "Sign in to enable backup & restore",
                        onClick = {
                            signInLauncher.launch(GoogleSignInHelper.getSignInIntent(context))
                        }
                    )
                }
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Upload,
                    title = "Sync to Google Drive",
                    subtitle = if (isSyncing) "Syncing..." else if (!isSignedIn) "Sign in required" else "Backup your data to Google Drive",
                    onClick = if (isSignedIn && !isSyncing && !isFetching) ({ showSyncDialog = true }) else null
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Download,
                    title = "Fetch from Google Drive",
                    subtitle = if (isFetching) "Fetching..." else if (!isSignedIn) "Sign in required" else "Restore data from Google Drive (overwrites local data)",
                    onClick = if (isSignedIn && !isSyncing && !isFetching) ({ showFetchDialog = true }) else null
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Sync,
                    title = "Auto Sync",
                    subtitle = if (!isSignedIn) "Sign in required" else if (autoSyncEnabled) "Syncs every $autoSyncInterval hours" else "Automatically sync to Google Drive",
                    onClick = if (isSignedIn && !autoSyncEnabled) ({ showAutoSyncDialog = true }) else null,
                    trailing = {
                        Switch(
                            checked = autoSyncEnabled,
                            enabled = isSignedIn && !isSyncing && !isFetching,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showAutoSyncDialog = true
                                } else {
                                    autoSyncEnabled = false
                                    driveSync.cancelAutoSync()
                                    syncMessage = "Auto sync disabled"
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.FileDownload,
                    title = "Export All Entries",
                    subtitle = "Export your journal as text",
                    onClick = {
                        if (viewModel.pinCode.isNotEmpty()) {
                            showPinVerifyForExport = true
                        } else {
                            showExportDialog = true
                        }
                    }
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Delete,
                    title = "Clear All Data",
                    subtitle = "Permanently delete all entries",
                    onClick = {
                        if (viewModel.pinCode.isNotEmpty()) {
                            showPinVerifyForClear = true
                        } else {
                            showClearDataDialog = true
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Receive reminders and alerts",
                    trailing = {
                        Switch(
                            checked = viewModel.notificationsEnabled,
                            onCheckedChange = { viewModel.updateNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.EditNotifications,
                    title = "Journal Reminder",
                    subtitle = if (viewModel.journalReminderEnabled) 
                        "Daily at ${viewModel.journalReminderTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}"
                        else "Set a daily reminder to journal",
                    onClick = { showReminderTimeDialog = true },
                    trailing = {
                        Switch(
                            checked = viewModel.journalReminderEnabled,
                            onCheckedChange = { viewModel.updateJournalReminderEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.StickyNote2,
                    title = "Quick Note Notification",
                    subtitle = "Persistent notification for quick notes",
                    trailing = {
                        Switch(
                            checked = viewModel.quickNoteNotificationEnabled,
                            onCheckedChange = { viewModel.updateQuickNoteNotificationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.surface,
                                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                )
            }
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { 
                Text(
                    "Set PIN",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        placeholder = { Text("Enter 4-digit PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 4) confirmPinInput = it },
                        placeholder = { Text("Confirm PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (pinInput.isNotEmpty() && confirmPinInput.isNotEmpty() && pinInput != confirmPinInput) {
                        Text(
                            text = "PINs do not match",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4 && pinInput == confirmPinInput) {
                            viewModel.setPin(pinInput)
                            pinInput = ""
                            confirmPinInput = ""
                            showPinDialog = false
                        }
                    },
                    enabled = pinInput.length == 4 && pinInput == confirmPinInput,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set PIN")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pinInput = ""
                        confirmPinInput = ""
                        showPinDialog = false
                    },
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
    if (showPinVerifyForExport || showPinVerifyForClear || showPinVerifyForRemove) {
        AlertDialog(
            onDismissRequest = {
                showPinVerifyForExport = false
                showPinVerifyForClear = false
                showPinVerifyForRemove = false
                pinVerifyInput = ""
                pinVerifyError = false
            },
            title = {
                Text(
                    "Enter PIN",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter your current PIN to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextField(
                        value = pinVerifyInput,
                        onValueChange = {
                            if (it.length <= 4) {
                                pinVerifyInput = it
                                pinVerifyError = false
                            }
                        },
                        placeholder = { Text("Enter PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (pinVerifyError) {
                        Text(
                            "Incorrect PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyPin(pinVerifyInput)) {
                            when {
                                showPinVerifyForExport -> {
                                    showPinVerifyForExport = false
                                    showExportDialog = true
                                }
                                showPinVerifyForClear -> {
                                    showPinVerifyForClear = false
                                    showClearDataDialog = true
                                }
                                showPinVerifyForRemove -> {
                                    showPinVerifyForRemove = false
                                    viewModel.setPin("")
                                }
                            }
                            pinVerifyInput = ""
                            pinVerifyError = false
                        } else {
                            pinVerifyError = true
                            pinVerifyInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        when {
                            showPinVerifyForRemove -> "Remove PIN"
                            showPinVerifyForExport -> "Verify"
                            showPinVerifyForClear -> "Verify"
                            else -> "Verify"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinVerifyForExport = false
                        showPinVerifyForClear = false
                        showPinVerifyForRemove = false
                        pinVerifyInput = ""
                        pinVerifyError = false
                    },
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
    if (showExportDialog) {
        val exportText = viewModel.getExportText()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { 
                Text(
                    "Export Preview",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${viewModel.entries.size} entries • ${viewModel.getStats().totalWords} words",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        item {
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text(
                                    text = exportText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                    Text(
                        "Copy the text above to save your journal entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExportDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { 
                Text(
                    "Clear All Data",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = { 
                Text(
                    "This will permanently delete all your journal entries, notes, and data. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataDialog = false },
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
    if (showReminderTimeDialog) {
        AlertDialog(
            onDismissRequest = { showReminderTimeDialog = false },
            title = { 
                Text(
                    "Set Reminder Time",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Choose when you'd like to be reminded to journal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedHour = (selectedHour - 1 + 24) % 24 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease hour")
                                }
                                Text(
                                    text = String.format("%02d", selectedHour),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(
                                    onClick = { selectedHour = (selectedHour + 1) % 24 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase hour")
                                }
                            }
                        }
                        Text(":", style = MaterialTheme.typography.titleLarge)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedMinute = (selectedMinute - 15 + 60) % 60 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease minute")
                                }
                                Text(
                                    text = String.format("%02d", selectedMinute),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(
                                    onClick = { selectedMinute = (selectedMinute + 15) % 60 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase minute")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateJournalReminderTime(selectedHour, selectedMinute)
                        viewModel.updateJournalReminderEnabled(true)
                        showReminderTimeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set Reminder")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReminderTimeDialog = false },
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
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("Sync to Google Drive", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will backup all your journal entries, notes, todos, and settings to Google Drive. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSyncDialog = false
                        isSyncing = true
                        scope.launch {
                            val result = driveSync.syncToGoogleDrive()
                            isSyncing = false
                            syncMessage = if (result.isSuccess) {
                                result.getOrNull()
                            } else {
                                "Sync failed: ${result.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sync")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSyncDialog = false },
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
    if (showFetchDialog) {
        AlertDialog(
            onDismissRequest = { showFetchDialog = false },
            title = { Text("Restore from Google Drive", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will replace all local data with your backup from Google Drive. This action cannot be undone. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showFetchDialog = false
                        isFetching = true
                        scope.launch {
                            val result = driveSync.fetchFromGoogleDrive()
                            isFetching = false
                            syncMessage = if (result.isSuccess) {
                                result.getOrNull()
                            } else {
                                "Restore failed: ${result.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFetchDialog = false },
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
    if (showAutoSyncDialog) {
        AlertDialog(
            onDismissRequest = { showAutoSyncDialog = false },
            title = { Text("Auto Sync Settings", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Set sync interval (hours):")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(6, 12, 24).forEach { hours ->
                            FilterChip(
                                selected = autoSyncInterval == hours,
                                onClick = { autoSyncInterval = hours },
                                label = { Text("${hours}h") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.onSurface,
                                    selectedLabelColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        autoSyncEnabled = true
                        driveSync.scheduleAutoSync(autoSyncInterval)
                        syncMessage = "Auto sync enabled (every $autoSyncInterval hours)"
                        showAutoSyncDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAutoSyncDialog = false },
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
    if (!isSignedIn) {
        LaunchedEffect(Unit) {
            isSignedIn = GoogleSignInHelper.isSignedIn(context)
        }
    }
    syncMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            syncMessage = null
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { syncMessage = null }) {
                    Text("OK")
                }
            }
        ) {
            Text(message)
        }
    }
}
@Composable
fun SettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
