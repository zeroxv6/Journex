package space.zeroxv6.journex.desktop.notification
import java.awt.*
import java.awt.TrayIcon.MessageType
import javax.imageio.ImageIO
import java.io.File
object DesktopNotificationManager {
    private var trayIcon: TrayIcon? = null
    private var systemTray: SystemTray? = null
    fun initialize() {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported on this platform")
            return
        }
        try {
            systemTray = SystemTray.getSystemTray()
            val iconImage = loadAppIcon()
            trayIcon = TrayIcon(iconImage, "Journex").apply {
                isImageAutoSize = true
                toolTip = "Journex - Journaling App"
                popupMenu = createPopupMenu()
            }
            systemTray?.add(trayIcon)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to initialize system tray: ${e.message}")
        }
    }
    private fun loadAppIcon(): Image {
        return try {
            val iconUrl = this::class.java.getResource("/journex_icon.png")
            if (iconUrl != null) {
                ImageIO.read(iconUrl)
            } else {
                createDefaultIcon()
            }
        } catch (e: Exception) {
            createDefaultIcon()
        }
    }
    private fun createDefaultIcon(): Image {
        val size = 32
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.color = Color(98, 0, 238) 
        g.fillRect(0, 0, size, size)
        g.color = Color.WHITE
        g.font = Font("Arial", Font.BOLD, 20)
        g.drawString("J", 8, 24)
        g.dispose()
        return image
    }
    private fun createPopupMenu(): PopupMenu {
        val popup = PopupMenu()
        val openItem = MenuItem("Open Journex")
        openItem.addActionListener {
        }
        val exitItem = MenuItem("Exit")
        exitItem.addActionListener {
            cleanup()
            System.exit(0)
        }
        popup.add(openItem)
        popup.addSeparator()
        popup.add(exitItem)
        return popup
    }
    fun showNotification(title: String, message: String, type: NotificationType = NotificationType.INFO) {
        trayIcon?.displayMessage(
            title,
            message,
            when (type) {
                NotificationType.INFO -> MessageType.INFO
                NotificationType.WARNING -> MessageType.WARNING
                NotificationType.ERROR -> MessageType.ERROR
                NotificationType.NONE -> MessageType.NONE
            }
        )
    }
    fun showReminderNotification(title: String, description: String) {
        showNotification(
            "Reminder: $title",
            description.ifEmpty { "It's time for your reminder" },
            NotificationType.INFO
        )
    }
    fun showScheduleNotification(title: String, description: String) {
        showNotification(
            "Schedule: $title",
            description.ifEmpty { "It's time for: $title" },
            NotificationType.INFO
        )
    }
    fun showJournalReminderNotification() {
        showNotification(
            "Time to Journal",
            "Take a moment to reflect on your day",
            NotificationType.INFO
        )
    }
    fun cleanup() {
        trayIcon?.let { icon ->
            systemTray?.remove(icon)
        }
        trayIcon = null
        systemTray = null
    }
    enum class NotificationType {
        INFO,
        WARNING,
        ERROR,
        NONE
    }
}
