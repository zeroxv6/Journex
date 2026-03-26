package space.zeroxv6.journex.desktop
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.graphics.toComposeImageBitmap
import space.zeroxv6.journex.desktop.ui.DesktopJournalApp
import space.zeroxv6.journex.desktop.ui.theme.JournalingDesktopTheme
import space.zeroxv6.journex.desktop.notification.DesktopNotificationManager
import space.zeroxv6.journex.desktop.notification.DesktopAlarmScheduler
import space.zeroxv6.journex.shared.data.JsonDataStore
import java.io.File
fun main() = application {
    DesktopNotificationManager.initialize()
    val dataDir = File(System.getProperty("user.home"), ".journaling")
    val dataStore = JsonDataStore(dataDir)
    val icon = try {
        val iconUrl = object {}.javaClass.getResource("/journex_icon.png")
        if (iconUrl != null) {
            val originalImage = javax.imageio.ImageIO.read(iconUrl)
            val scaledImage = java.awt.image.BufferedImage(256, 256, java.awt.image.BufferedImage.TYPE_INT_ARGB)
            val graphics = scaledImage.createGraphics()
            graphics.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.drawImage(originalImage, 0, 0, 256, 256, null)
            graphics.dispose()
            androidx.compose.ui.graphics.painter.BitmapPainter(
                org.jetbrains.skia.Image.makeFromEncoded(
                    java.io.ByteArrayOutputStream().apply {
                        javax.imageio.ImageIO.write(scaledImage, "PNG", this)
                    }.toByteArray()
                ).toComposeImageBitmap()
            )
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
    Window(
        onCloseRequest = {
            DesktopAlarmScheduler.cleanup()
            DesktopNotificationManager.cleanup()
            exitApplication()
        },
        title = "Journex",
        icon = icon,
        state = rememberWindowState(
            size = DpSize(1400.dp, 900.dp)
        )
    ) {
        JournalingDesktopTheme {
            DesktopJournalApp(dataStore)
        }
    }
}
