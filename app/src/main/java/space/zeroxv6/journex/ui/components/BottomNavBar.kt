package space.zeroxv6.journex.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomNavItem(
            icon = if (currentRoute == "dashboard") Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
            label = "Dashboard",
            selected = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") }
        )
        BottomNavItem(
            icon = if (currentRoute == "home") Icons.Filled.Article else Icons.Outlined.Article,
            label = "Journal",
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        BottomNavItem(
            icon = if (currentRoute == "task-management") Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
            label = "Board",
            selected = currentRoute == "task-management",
            onClick = { onNavigate("task-management") }
        )
        BottomNavItem(
            icon = if (currentRoute == "todo") Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
            label = "Tasks",
            selected = currentRoute == "todo",
            onClick = { onNavigate("todo") }
        )
        BottomNavItem(
            icon = if (currentRoute == "reminders") Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
            label = "Reminders",
            selected = currentRoute == "reminders",
            onClick = { onNavigate("reminders") }
        )
    }
}
@Composable
private fun RowScope.BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    NavigationBarItem(
        icon = { 
            Icon(
                icon, 
                contentDescription = null,
                modifier = Modifier
                    .scale(scale)
                    .size(24.dp)
            ) 
        },
        label = { 
            Text(
                text = label,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = false,
                style = MaterialTheme.typography.labelSmall
            ) 
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    )
}
@Composable
fun AppBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToTodo: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(if (selectedTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = null) },
            label = { 
                Text(
                    "Dashboard",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall
                ) 
            },
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
                onNavigateToDashboard()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(if (selectedTab == 1) Icons.Filled.Article else Icons.Outlined.Article, contentDescription = null) },
            label = { 
                Text(
                    "Journal",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall
                ) 
            },
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
                onNavigateToJournal()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(if (selectedTab == 2) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline, contentDescription = null) },
            label = { 
                Text(
                    "Tasks",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall
                ) 
            },
            selected = selectedTab == 2,
            onClick = {
                onTabSelected(2)
                onNavigateToTodo()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(if (selectedTab == 3) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone, contentDescription = null) },
            label = { 
                Text(
                    "Reminders",
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.labelSmall
                ) 
            },
            selected = selectedTab == 3,
            onClick = {
                onTabSelected(3)
                onNavigateToReminders()
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSurface,
                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
    }
}
