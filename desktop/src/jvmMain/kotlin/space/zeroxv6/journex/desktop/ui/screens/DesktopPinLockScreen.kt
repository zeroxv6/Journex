package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import space.zeroxv6.journex.shared.data.JsonDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun DesktopPinLockScreen(
    dataStore: JsonDataStore,
    onUnlocked: () -> Unit
) {
    val settings by dataStore.settings.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.current.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = AppColors.current.textPrimary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = AppColors.current.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your 4-digit PIN to access",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.current.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) { index ->
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = if (index < pinInput.length) AppColors.current.textPrimary else AppColors.current.border
                    ) {}
                }
            }
            if (showError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Incorrect PIN",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.current.error
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "⌫")
                ).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { number ->
                            if (number.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        when (number) {
                                            "⌫" -> {
                                                if (pinInput.isNotEmpty()) {
                                                    pinInput = pinInput.dropLast(1)
                                                    showError = false
                                                }
                                            }
                                            else -> {
                                                if (pinInput.length < 4) {
                                                    pinInput += number
                                                    showError = false
                                                    if (pinInput.length == 4) {
                                                        if (pinInput == settings.pinCode) {
                                                            onUnlocked()
                                                        } else {
                                                            showError = true
                                                            coroutineScope.launch {
                                                                delay(500)
                                                                pinInput = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.current.surface,
                                        contentColor = AppColors.current.textPrimary
                                    )
                                ) {
                                    Text(
                                        text = number,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                                        fontSize = 24.sp
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
