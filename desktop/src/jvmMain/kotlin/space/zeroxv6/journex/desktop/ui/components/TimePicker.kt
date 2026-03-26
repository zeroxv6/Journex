package space.zeroxv6.journex.desktop.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.zeroxv6.journex.desktop.ui.theme.AppColors
import java.time.LocalTime
@Composable
fun TimePicker(
    time: LocalTime,
    use24HourFormat: Boolean,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    key: String = ""  
) {
    val stableKey = remember { "$key-${System.nanoTime()}" }
    var hourText by remember(stableKey, time, use24HourFormat) {
        mutableStateOf(
            if (use24HourFormat) {
                String.format("%02d", time.hour)
            } else {
                val hour12 = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
                String.format("%02d", hour12)
            }
        )
    }
    var minuteText by remember(stableKey, time) { mutableStateOf(String.format("%02d", time.minute)) }
    var isAM by remember(stableKey, time) { mutableStateOf(time.hour < 12) }
    fun updateTime(newIsAM: Boolean = isAM) {
        try {
            val hour = hourText.toIntOrNull() ?: return
            val minute = minuteText.toIntOrNull() ?: return
            if (minute !in 0..59) return
            val finalHour = if (use24HourFormat) {
                if (hour in 0..23) hour else return
            } else {
                if (hour !in 1..12) return
                when {
                    hour == 12 && newIsAM -> 0
                    hour == 12 && !newIsAM -> 12
                    !newIsAM -> hour + 12
                    else -> hour
                }
            }
            onTimeChange(LocalTime.of(finalHour, minute))
        } catch (_: Exception) {}
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeField(
            value = hourText,
            onValueChange = { 
                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                    hourText = it
                    if (it.length == 2) updateTime()
                }
            },
            onIncrement = {
                val current = hourText.toIntOrNull() ?: 0
                val max = if (use24HourFormat) 23 else 12
                val min = if (use24HourFormat) 0 else 1
                val next = if (current >= max) min else current + 1
                hourText = String.format("%02d", next)
                updateTime()
            },
            onDecrement = {
                val current = hourText.toIntOrNull() ?: 0
                val max = if (use24HourFormat) 23 else 12
                val min = if (use24HourFormat) 0 else 1
                val prev = if (current <= min) max else current - 1
                hourText = String.format("%02d", prev)
                updateTime()
            }
        )
        Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp))
        TimeField(
            value = minuteText,
            onValueChange = { 
                if (it.length <= 2 && (it.isEmpty() || it.all { c -> c.isDigit() })) {
                    minuteText = it
                    if (it.length == 2) updateTime()
                }
            },
            onIncrement = {
                val current = minuteText.toIntOrNull() ?: 0
                val next = (current + 5) % 60
                minuteText = String.format("%02d", next)
                updateTime()
            },
            onDecrement = {
                val current = minuteText.toIntOrNull() ?: 0
                val prev = (current - 5 + 60) % 60
                minuteText = String.format("%02d", prev)
                updateTime()
            }
        )
        if (!use24HourFormat) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    onClick = { 
                        isAM = true
                        updateTime(newIsAM = true)
                    },
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = if (isAM) AppColors.current.textPrimary else AppColors.current.inputBackground
                ) {
                    Text(
                        "AM",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isAM) AppColors.current.background else AppColors.current.textTertiary
                    )
                }
                Surface(
                    onClick = { 
                        isAM = false
                        updateTime(newIsAM = false)
                    },
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = if (!isAM) AppColors.current.textPrimary else AppColors.current.inputBackground
                ) {
                    Text(
                        "PM",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isAM) AppColors.current.background else AppColors.current.textTertiary
                    )
                }
            }
        }
    }
}
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.KeyboardArrowUp, "Increase", modifier = Modifier.size(20.dp))
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppColors.current.inputBackground,
            modifier = Modifier.width(70.dp).height(50.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = AppColors.current.textPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxSize().wrapContentHeight(Alignment.CenterVertically)
            )
        }
        IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.KeyboardArrowDown, "Decrease", modifier = Modifier.size(20.dp))
        }
    }
}
