package space.zeroxv6.journex.ui.screens
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DeveloperInfoCard(context)
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                // ── Reminder style: Notification or Alarm ─────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.AlarmOn, null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Reminder Style", style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text(
                                if (viewModel.useFullScreenAlarm) "Alarm — full screen, loud" else "Notification — silent banner",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Notification option
                        val notifSelected = !viewModel.useFullScreenAlarm
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .background(if (notifSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (notifSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateUseFullScreenAlarm(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    Icons.Outlined.Notifications, null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (notifSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Notification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (notifSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        // Alarm option
                        val alarmSelected = viewModel.useFullScreenAlarm
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                .background(if (alarmSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (alarmSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateUseFullScreenAlarm(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    Icons.Outlined.AlarmOn, null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (alarmSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Alarm",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (alarmSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
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
                SettingCard(
                    icon = Icons.Outlined.Event,
                    title = "Schedules Notification",
                    subtitle = "Shows ongoing and upcoming schedule",
                    trailing = {
                        Switch(
                            checked = viewModel.persistentScheduleNotificationEnabled,
                            onCheckedChange = { viewModel.updatePersistentScheduleNotificationEnabled(it) },
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
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.AccessTime,
                    title = "24-Hour Time Format",
                    subtitle = if (viewModel.use24HourFormat) "Using 24-hour format (14:00)" else "Using 12-hour format (2:00 PM)",
                    trailing = {
                        Switch(
                            checked = viewModel.use24HourFormat,
                            onCheckedChange = { viewModel.updateUse24HourFormat(it) },
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
                    text = "App Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFD94F2A),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            item {
                SettingCard(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.13"
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
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
                        placeholder = { Text("Enter 4-digit PIN", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                        placeholder = { Text("Confirm PIN", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    Text("Set PIN", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                        placeholder = { Text("Enter PIN", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    Text("Cancel", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                                    fontFamily = space.zeroxv6.journex.ui.theme.GeistFontFamily
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
                    Text("Close", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
                    Text("Clear All", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataDialog = false },
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
                    space.zeroxv6.journex.ui.components.ImprovedTimePicker(
                        hour = selectedHour,
                        minute = selectedMinute,
                        use24Hour = viewModel.use24HourFormat,
                        onHourChange = { selectedHour = it },
                        onMinuteChange = { selectedMinute = it }
                    )
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
                    Text("Set Reminder", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReminderTimeDialog = false },
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
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("Sync to Google Drive", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will backup all your journal entries, notes, todos, and settings to Google Drive. Continue?", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    Text("Sync", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSyncDialog = false },
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
    if (showFetchDialog) {
        AlertDialog(
            onDismissRequest = { showFetchDialog = false },
            title = { Text("Restore from Google Drive", style = MaterialTheme.typography.titleLarge) },
            text = { Text("This will replace all local data with your backup from Google Drive. This action cannot be undone. Continue?", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    Text("Restore", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFetchDialog = false },
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
    if (showAutoSyncDialog) {
        AlertDialog(
            onDismissRequest = { showAutoSyncDialog = false },
            title = { Text("Auto Sync Settings", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Set sync interval (hours):", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(6, 12, 24).forEach { hours ->
                            FilterChip(
                                selected = autoSyncInterval == hours,
                                onClick = { autoSyncInterval = hours },
                                label = { Text("${hours}h", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    Text("Enable", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAutoSyncDialog = false },
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
                    Text("OK", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }
        ) {
            Text(message)
        }
    }
}
@Composable
fun DeveloperInfoCard(context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2825)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        "Raman Mann",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "@zeroxv6",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Journex is an open-source project dedicated to premium journaling experiences.",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DeveloperLink(
                    label = "GitHub",
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/zeroxv6/Journex"))
                        context.startActivity(intent)
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeveloperLink(
                        label = "x.com",
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://x.com/zeroxv6_rmn"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DeveloperLink(
                        label = "zeroxv6.space",
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://zeroxv6.space"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.White)
            }
            Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (trailing != null) {
                trailing()
            } else if (onClick != null) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = "Open",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
