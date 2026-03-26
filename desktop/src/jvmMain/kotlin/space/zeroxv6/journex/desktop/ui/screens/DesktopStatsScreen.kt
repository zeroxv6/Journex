package space.zeroxv6.journex.desktop.ui.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import space.zeroxv6.journex.desktop.viewmodel.JournalViewModel
import space.zeroxv6.journex.shared.model.*
@Composable
fun DesktopStatsScreen(viewModel: JournalViewModel) {
    val stats by viewModel.stats.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val moodDistribution = entries.groupingBy { it.mood }.eachCount()
    val maxMoodCount = moodDistribution.values.maxOrNull() ?: 1
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(40.dp)
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
        Spacer(modifier = Modifier.height(4.dp))
        Text("Your journaling insights", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(modifier = Modifier.weight(1f), value = "${stats.totalEntries}", label = "Total Entries")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.currentStreak}", label = "Current Streak", suffix = "days")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.longestStreak}", label = "Longest Streak", suffix = "days")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.totalWords}", label = "Total Words")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(modifier = Modifier.weight(1f), value = "${stats.averageWordsPerEntry}", label = "Avg Words/Entry")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.entriesThisMonth}", label = "This Month")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.entriesThisYear}", label = "This Year")
            StatCard(modifier = Modifier.weight(1f), value = "${stats.completedTasks}", label = "Tasks Done")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Mood Distribution", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(24.dp))
                if (moodDistribution.isEmpty()) {
                    Text("No entries yet to analyze", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textDisabled)
                } else {
                    Row(modifier = Modifier.fillMaxWidth().height(220.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                        Mood.entries.forEach { mood ->
                            val count = moodDistribution[mood] ?: 0
                            val heightFraction = if (maxMoodCount > 0) count.toFloat() / maxMoodCount else 0f
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                                if (count > 0) {
                                    Text("$count", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = AppColors.current.textPrimary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Box(modifier = Modifier.width(28.dp).fillMaxHeight(heightFraction.coerceAtLeast(if (count > 0) 0.05f else 0.03f)).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(if (count > 0) AppColors.current.textPrimary else AppColors.current.border))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(if (count > 0) mood.label else mood.label.take(6), style = MaterialTheme.typography.labelSmall, color = if (count > 0) AppColors.current.textPrimary else AppColors.current.textDisabled, maxLines = 1)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = AppColors.current.border)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Mood.entries.chunked(5).forEach { chunk ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                chunk.forEach { mood ->
                                    val count = moodDistribution[mood] ?: 0
                                    Text(
                                        "${mood.label}: $count",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.current.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Top Tags", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(16.dp))
                    if (stats.mostUsedTags.isEmpty()) {
                        Text("No tags used yet", style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textDisabled)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            stats.mostUsedTags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(8.dp), color = AppColors.current.surfaceTertiary) {
                                    Text("#$tag", modifier = Modifier.padding(12.dp, 8.dp), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = AppColors.current.textPrimary) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Most Common Mood", style = MaterialTheme.typography.titleMedium, color = AppColors.current.textDisabled)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (stats.mostUsedMood != null) {
                        Text(stats.mostUsedMood!!.label, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = AppColors.current.background)
                    } else {
                        Text("No data yet", style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textDisabled)
                    }
                }
            }
        }
    }
}
@Composable
private fun StatCard(modifier: Modifier = Modifier, value: String, label: String, suffix: String? = null) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = AppColors.current.cardBackground, border = BorderStroke(1.dp, AppColors.current.border)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(value, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = AppColors.current.textPrimary)
                if (suffix != null) Text(suffix, style = MaterialTheme.typography.bodyMedium, color = AppColors.current.textSecondary, modifier = Modifier.padding(bottom = 6.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = AppColors.current.textSecondary)
        }
    }
}
