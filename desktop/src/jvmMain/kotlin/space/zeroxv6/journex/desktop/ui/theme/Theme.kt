package space.zeroxv6.journex.desktop.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
val LocalAppColorScheme = staticCompositionLocalOf { ClassicWhiteScheme }
private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF5F5F5),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF1C1C1C),
    tertiary = Color(0xFF616161),
    onTertiary = Color.White,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5),
    scrim = Color.Black,
    inverseSurface = Color.Black,
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFE0E0E0),
    surfaceTint = Color.Black
)
object AppColors {
    val current: AppColorScheme
        @Composable
        get() = LocalAppColorScheme.current
    @Composable
    fun colors(): AppColorScheme = LocalAppColorScheme.current
}
@Composable
fun JournalingDesktopTheme(
    appTheme: AppTheme = AppTheme.CLASSIC_WHITE,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColorScheme provides appTheme.colorScheme
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
