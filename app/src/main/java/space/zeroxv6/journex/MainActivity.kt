package space.zeroxv6.journex
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.unit.IntOffset
import space.zeroxv6.journex.ui.animations.*
import space.zeroxv6.journex.ui.components.BottomNavBar
import space.zeroxv6.journex.ui.screens.*
import space.zeroxv6.journex.ui.theme.JournalingTheme
import space.zeroxv6.journex.viewmodel.JournalViewModel
import space.zeroxv6.journex.notification.NotificationHelper
import space.zeroxv6.journex.notification.NotificationPermissionHelper
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        NotificationHelper.createNotificationChannels(this)
        if (!NotificationPermissionHelper.hasNotificationPermission(this)) {
            NotificationPermissionHelper.requestNotificationPermission(this)
        }
        setContent {
            JournalApp()
        }
    }
}
@Composable
fun JournalApp() {
    val viewModel: JournalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val taskViewModel: space.zeroxv6.journex.viewmodel.TaskViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val noteViewModel: space.zeroxv6.journex.viewmodel.NoteViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val projectTaskViewModel: space.zeroxv6.journex.viewmodel.ProjectTaskViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val isUnlocked = viewModel.isUnlockedThisSession
    val hasPinCode = viewModel.pinCode.isNotEmpty()
    val showPinLock = hasPinCode && !isUnlocked
    JournalingTheme {
        if (showPinLock) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                PinLockScreen(
                    viewModel = viewModel,
                    onUnlocked = {
                    }
                )
            }
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val screensWithoutNavBar = listOf("editor/{entryId}", "note-editor/{noteId}")
            val shouldShowNavBar = currentRoute !in screensWithoutNavBar
            Scaffold(
                bottomBar = {
                    if (shouldShowNavBar) {
                        BottomNavBar(
                            currentRoute = currentRoute ?: "dashboard",
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("dashboard") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(
                        "dashboard",
                        enterTransition = {
                            fadeIn(animationSpec = tween(150)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(150))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(100)) +
                                    scaleOut(targetScale = 0.98f, animationSpec = tween(100))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(150)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(150))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(100)) +
                                    scaleOut(targetScale = 0.98f, animationSpec = tween(100))
                        }
                    ) {
                        DashboardScreen(
                            viewModel = viewModel,
                            taskViewModel = taskViewModel,
                            onNavigateToJournal = {
                                navController.navigate("home")
                            },
                            onNavigateToStats = {
                                navController.navigate("stats")
                            },
                            onNavigateToTemplates = {
                                navController.navigate("templates")
                            },
                            onNavigateToQuickNotes = {
                                navController.navigate("quicknotes")
                            },
                            onNavigateToNotes = {
                                navController.navigate("notes")
                            },
                            onNavigateToPrompts = {
                                navController.navigate("prompts")
                            },
                            onNavigateToPromptMoment = {
                                navController.navigate("prompt-moment")
                            },
                            onNavigateToTodo = {
                                navController.navigate("todo")
                            },
                            onNavigateToSchedule = {
                                navController.navigate("schedule")
                            },
                            onNavigateToReminders = {
                                navController.navigate("reminders")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            },
                            onNavigateToTaskManagement = {
                                navController.navigate("task-management")
                            }
                        )
                    }
                    composable(
                        "home",
                        enterTransition = {
                            fadeIn(animationSpec = tween(150)) +
                                    slideInHorizontally(
                                        initialOffsetX = { it / 4 },
                                        animationSpec = tween(150)
                                    )
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(100)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { -it / 4 },
                                        animationSpec = tween(100)
                                    )
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(150)) +
                                    slideInHorizontally(
                                        initialOffsetX = { -it / 4 },
                                        animationSpec = tween(150)
                                    )
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(100)) +
                                    slideOutHorizontally(
                                        targetOffsetX = { it / 4 },
                                        animationSpec = tween(100)
                                    )
                        }
                    ) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToEditor = { entryId ->
                                if (entryId != null) {
                                    navController.navigate("editor/$entryId")
                                } else {
                                    navController.navigate("editor/new")
                                }
                            },
                            onNavigateToStats = {
                                navController.navigate("stats")
                            },
                            onNavigateToTemplates = {
                                navController.navigate("templates")
                            },
                            onNavigateToQuickNotes = {
                                navController.navigate("quicknotes")
                            },
                            onNavigateToPrompts = {
                                navController.navigate("prompts")
                            },
                            onNavigateToTodo = {
                                navController.navigate("todo")
                            },
                            onNavigateToSchedule = {
                                navController.navigate("schedule")
                            },
                            onNavigateToReminders = {
                                navController.navigate("reminders")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable(
                        route = "editor/{entryId}",
                        arguments = listOf(
                            navArgument("entryId") { type = NavType.StringType }
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(200)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                                    )
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(150))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(150))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(150)) +
                                    slideOutVertically(
                                        targetOffsetY = { it / 2 },
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                        }
                    ) { backStackEntry ->
                        val entryId = backStackEntry.arguments?.getString("entryId")
                        EditorScreen(
                            viewModel = viewModel,
                            entryId = if (entryId == "new") null else entryId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        "stats",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        StatsScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        "templates",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        TemplatesScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onTemplateSelected = {
                                navController.navigate("editor/new") {
                                    popUpTo("home")
                                }
                            }
                        )
                    }
                    composable(
                        "quicknotes",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        QuickNotesScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        "prompt-moment",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        PromptOfMomentScreen(
                            viewModel = viewModel,
                            taskViewModel = taskViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToEditor = { entryId ->
                                if (entryId != null) {
                                    navController.navigate("editor/$entryId")
                                } else {
                                    navController.navigate("editor/new")
                                }
                            },
                            onNavigateToAllPrompts = {
                                navController.navigate("prompts")
                            }
                        )
                    }
                    composable(
                        "prompts",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        PromptsScreen(
                            viewModel = viewModel,
                            taskViewModel = taskViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onPromptSelected = { promptText ->
                                navController.navigate("editor/new")
                            }
                        )
                    }
                    composable(
                        "todo",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        TodoScreen(
                            taskViewModel = taskViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToDashboard = {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToJournal = {
                                navController.navigate("home") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToReminders = {
                                navController.navigate("reminders") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(
                        "schedule",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        ScheduleScreen(
                            taskViewModel = taskViewModel,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToDashboard = {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToJournal = {
                                navController.navigate("home") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToTodo = {
                                navController.navigate("todo") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToReminders = {
                                navController.navigate("reminders") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(
                        "settings",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        "reminders",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        RemindersScreen(
                            taskViewModel = taskViewModel,
                            viewModel = viewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onNavigateToDashboard = {
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToJournal = {
                                navController.navigate("home") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            onNavigateToTodo = {
                                navController.navigate("todo") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(
                        "notes",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        NotesScreen(
                            viewModel = noteViewModel,
                            onNavigateToEditor = { noteId ->
                                if (noteId != null) {
                                    navController.navigate("note-editor/$noteId")
                                } else {
                                    navController.navigate("note-editor/new")
                                }
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = "note-editor/{noteId}",
                        arguments = listOf(
                            navArgument("noteId") { type = NavType.StringType }
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(200)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 2 },
                                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                                    )
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(150))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(150))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(150)) +
                                    slideOutVertically(
                                        targetOffsetY = { it / 2 },
                                        animationSpec = tween(150, easing = FastOutSlowInEasing)
                                    )
                        }
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")
                        NoteEditorScreen(
                            viewModel = noteViewModel,
                            noteId = if (noteId == "new") null else noteId,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        "task-management",
                        enterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        },
                        popEnterTransition = {
                            fadeIn(tween(150)) + scaleIn(
                                initialScale = 0.98f,
                                animationSpec = tween(150)
                            )
                        },
                        popExitTransition = {
                            fadeOut(tween(100)) + scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(100)
                            )
                        }
                    ) {
                        TaskManagementScreen(
                            viewModel = projectTaskViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}