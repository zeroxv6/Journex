package space.zeroxv6.journex.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
enum class AppTheme {
    LIGHT,
    DARK,
    SEPIA,
    OCEAN,
    FOREST,
    SUNSET,
    LAVENDER,
    MIDNIGHT
}
private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF5F5F5),
    onPrimaryContainer = Color.Black,
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF1C1C1C),
    tertiary = Color(0xFF424242),
    onTertiary = Color.White,
    error = Color.Black,
    onError = Color.White,
    errorContainer = Color(0xFFE0E0E0),
    onErrorContainer = Color(0xFF1C1C1C),
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.Black,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5),
    scrim = Color.Black,
    inverseSurface = Color.Black,
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFE0E0E0),
    surfaceTint = Color.Black
)
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF3C3C3C),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3C3C3C),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF3C3C3C),
    onTertiary = Color.Black,
    error = Color.White,
    onError = Color.Black,
    errorContainer = Color(0xFF3C3C3C),
    onErrorContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFF3C3C3C),
    outline = Color(0xFF3C3C3C),
    outlineVariant = Color(0xFF2C2C2C),
    scrim = Color.White,
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = Color.Black,
    surfaceTint = Color.White
)
private val SepiaColorScheme = lightColorScheme(
    primary = Color(0xFF5D4037),
    onPrimary = Color(0xFFFFF8E1),
    primaryContainer = Color(0xFFFFF8E1),
    onPrimaryContainer = Color(0xFF5D4037),
    secondary = Color(0xFF8D6E63),
    onSecondary = Color(0xFFFFF8E1),
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF5D4037),
    background = Color(0xFFFFFBF0),
    onBackground = Color(0xFF5D4037),
    surface = Color(0xFFFFF8E1),
    onSurface = Color(0xFF5D4037),
    surfaceVariant = Color(0xFFFFECB3),
    onSurfaceVariant = Color(0xFF8D6E63),
    outline = Color(0xFFD7CCC8),
    surfaceTint = Color(0xFF5D4037)
)
private val OceanColorScheme = lightColorScheme(
    primary = Color(0xFF006064),
    onPrimary = Color(0xFFE0F7FA),
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = Color(0xFF00838F),
    onSecondary = Color(0xFFE0F7FA),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF006064),
    background = Color(0xFFF0FEFF),
    onBackground = Color(0xFF006064),
    surface = Color(0xFFE0F7FA),
    onSurface = Color(0xFF006064),
    surfaceVariant = Color(0xFFB2EBF2),
    onSurfaceVariant = Color(0xFF00838F),
    outline = Color(0xFF80DEEA),
    surfaceTint = Color(0xFF006064)
)
private val ForestColorScheme = lightColorScheme(
    primary = Color(0xFF1B5E20),
    onPrimary = Color(0xFFE8F5E9),
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF388E3C),
    onSecondary = Color(0xFFE8F5E9),
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF1B5E20),
    background = Color(0xFFF1F8F1),
    onBackground = Color(0xFF1B5E20),
    surface = Color(0xFFE8F5E9),
    onSurface = Color(0xFF1B5E20),
    surfaceVariant = Color(0xFFC8E6C9),
    onSurfaceVariant = Color(0xFF388E3C),
    outline = Color(0xFFA5D6A7),
    surfaceTint = Color(0xFF1B5E20)
)
private val SunsetColorScheme = lightColorScheme(
    primary = Color(0xFFBF360C),
    onPrimary = Color(0xFFFBE9E7),
    primaryContainer = Color(0xFFFBE9E7),
    onPrimaryContainer = Color(0xFFBF360C),
    secondary = Color(0xFFD84315),
    onSecondary = Color(0xFFFBE9E7),
    secondaryContainer = Color(0xFFFFCCBC),
    onSecondaryContainer = Color(0xFFBF360C),
    background = Color(0xFFFFF5F3),
    onBackground = Color(0xFFBF360C),
    surface = Color(0xFFFBE9E7),
    onSurface = Color(0xFFBF360C),
    surfaceVariant = Color(0xFFFFCCBC),
    onSurfaceVariant = Color(0xFFD84315),
    outline = Color(0xFFFFAB91),
    surfaceTint = Color(0xFFBF360C)
)
private val LavenderColorScheme = lightColorScheme(
    primary = Color(0xFF4A148C),
    onPrimary = Color(0xFFF3E5F5),
    primaryContainer = Color(0xFFF3E5F5),
    onPrimaryContainer = Color(0xFF4A148C),
    secondary = Color(0xFF6A1B9A),
    onSecondary = Color(0xFFF3E5F5),
    secondaryContainer = Color(0xFFE1BEE7),
    onSecondaryContainer = Color(0xFF4A148C),
    background = Color(0xFFFAF5FC),
    onBackground = Color(0xFF4A148C),
    surface = Color(0xFFF3E5F5),
    onSurface = Color(0xFF4A148C),
    surfaceVariant = Color(0xFFE1BEE7),
    onSurfaceVariant = Color(0xFF6A1B9A),
    outline = Color(0xFFCE93D8),
    surfaceTint = Color(0xFF4A148C)
)
private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1A237E),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF64B5F6),
    onSecondary = Color(0xFF0D47A1),
    secondaryContainer = Color(0xFF283593),
    onSecondaryContainer = Color(0xFFBBDEFB),
    background = Color(0xFF0A0E27),
    onBackground = Color(0xFFBBDEFB),
    surface = Color(0xFF1A237E),
    onSurface = Color(0xFFBBDEFB),
    surfaceVariant = Color(0xFF283593),
    onSurfaceVariant = Color(0xFF90CAF9),
    outline = Color(0xFF3949AB),
    surfaceTint = Color(0xFF90CAF9)
)
@Composable
fun JournalingTheme(
    theme: AppTheme = AppTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.LIGHT -> LightColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SEPIA -> SepiaColorScheme
        AppTheme.OCEAN -> OceanColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
        AppTheme.LAVENDER -> LavenderColorScheme
        AppTheme.MIDNIGHT -> MidnightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
