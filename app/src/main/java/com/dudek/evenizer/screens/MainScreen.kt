package com.dudek.evenizer.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dudek.evenizer.R
import com.dudek.evenizer.models.AuthViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.models.NotificationViewModel
import com.dudek.evenizer.pages.CreateEventPage
import com.dudek.evenizer.pages.EventDetailPage
import com.dudek.evenizer.pages.EventPage
import com.dudek.evenizer.pages.EventRundownPage
import com.dudek.evenizer.pages.AddEventRundownPage
import com.dudek.evenizer.pages.MyEventsPage
import com.dudek.evenizer.pages.HomePage
import com.dudek.evenizer.pages.NotificationPage
import com.dudek.evenizer.pages.OrganizerPage
import com.dudek.evenizer.pages.OrganizerDetailPage
import com.dudek.evenizer.pages.CreateOrganizerRolesPage
import com.dudek.evenizer.pages.UpdateOrganizerRolePage
import com.dudek.evenizer.pages.AddOrganizerMemberPage
import com.dudek.evenizer.pages.ProfilePage
import com.dudek.evenizer.pages.SettingsPage
import com.dudek.evenizer.pages.CreateOrganizerPage
import com.dudek.evenizer.pages.MyOrganizersPage
import com.dudek.evenizer.pages.TicketPage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    organizerViewModel: OrganizerViewModel,
    notificationViewModel: NotificationViewModel,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = androidx.compose.ui.platform.LocalContext.current

    val latestNotification by notificationViewModel.latestNotification.collectAsState()

    DisposableEffect(Unit) {
        notificationViewModel.startPolling(context)
        onDispose { notificationViewModel.stopPolling() }
    }

    // "System Popup" logic using Snackbar or specific UI overlay
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(latestNotification) {
        latestNotification?.let { notification ->
            snackbarHostState.showSnackbar(
                message = "${notification.title}: ${notification.message}",
                duration = SnackbarDuration.Long
            )
            notificationViewModel.clearLatestNotification()
        }
    }

    MainScreenContent(
        currentRoute = currentRoute,
        snackbarHostState = snackbarHostState,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        content = { innerPadding ->
            NavHost(
                navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { 
                    HomePage(
                        themeViewModel = themeViewModel,
                        eventViewModel = eventViewModel,
                        organizerViewModel = organizerViewModel,
                        onNavigateToNotifications = { navController.navigate("notification") }
                    ) 
                }
                composable("notification") {
                    NotificationPage(
                        notificationViewModel = notificationViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("event") { 
                    EventPage(
                        themeViewModel = themeViewModel,
                        userViewModel = userViewModel,
                        eventViewModel = eventViewModel,
                        onNavigateToCreate = { navController.navigate("create_event") },
                        onNavigateToMyEvents = { navController.navigate("my_events") },
                        onNavigateToDetail = { uuid -> navController.navigate("event_detail/$uuid") },
                        onNavigateToLogin = onNavigateToLogin
                    ) 
                }
                composable("event_detail/{uuid}") { backStackEntry ->
                    val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                    EventDetailPage(
                        uuid = uuid,
                        themeViewModel = themeViewModel,
                        userViewModel = userViewModel,
                        eventViewModel = eventViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToEdit = { /* TODO */ },
                        onNavigateToRundown = { eventUuid -> navController.navigate("event_rundown/$eventUuid") }
                    )
                }
                composable("event_rundown/{uuid}") { backStackEntry ->
                    val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                    EventRundownPage(
                        eventUuid = uuid,
                        eventViewModel = eventViewModel,
                        onBack = { navController.popBackStack() },
                        onNavigateToAddRundown = { eventUuid -> navController.navigate("add_event_rundown/$eventUuid") }
                    )
                }
                composable("add_event_rundown/{uuid}") { backStackEntry ->
                    val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                    AddEventRundownPage(
                        eventUuid = uuid,
                        eventViewModel = eventViewModel,
                        onBack = { navController.popBackStack() },
                        onSuccess = { navController.popBackStack() }
                    )
                }
                composable("create_event") {
                    CreateEventPage(
                        eventViewModel = eventViewModel,
                        onBack = { navController.popBackStack() },
                        onSuccess = { 
                            navController.navigate("my_events") {
                                popUpTo("event") { inclusive = false }
                            }
                        }
                    )
                }
                composable("my_events") {
                    MyEventsPage(
                        themeViewModel = themeViewModel,
                        userViewModel = userViewModel,
                        eventViewModel = eventViewModel,
                        onNavigateToDetail = { uuid -> navController.navigate("event_detail/$uuid") },
                        onNavigateToCreate = { navController.navigate("create_event") },
                        onBack = { navController.popBackStack() }
                    )
                }
            composable("organizer") { 
                OrganizerPage(
                    themeViewModel = themeViewModel,
                    userViewModel = userViewModel,
                    organizerViewModel = organizerViewModel,
                    onNavigateToCreate = { navController.navigate("create_organizer") },
                    onNavigateToMyOrganizers = { navController.navigate("my_organizers") },
                    onNavigateToDetail = { uuid -> navController.navigate("organizer_detail/$uuid") },
                    onNavigateToLogin = onNavigateToLogin
                ) 
            }
            composable("organizer_detail/{uuid}") { backStackEntry ->
                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                OrganizerDetailPage(
                    uuid = uuid,
                    themeViewModel = themeViewModel,
                    userViewModel = userViewModel,
                    organizerViewModel = organizerViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToAddRole = { navController.navigate("create_organizer_roles/$uuid") },
                    onNavigateToUpdateRole = { organizerUuid, roleUuid, name, desc ->
                        navController.navigate("update_organizer_role/$organizerUuid/$roleUuid/$name/$desc")
                    },
                    onNavigateToAddMember = { navController.navigate("add_organizer_member/$uuid") }
                )
            }
            composable("add_organizer_member/{uuid}") { backStackEntry ->
                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                AddOrganizerMemberPage(
                    organizerUuid = uuid,
                    organizerViewModel = organizerViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable("update_organizer_role/{organizerUuid}/{roleUuid}/{name}/{description}") { backStackEntry ->
                val organizerUuid = backStackEntry.arguments?.getString("organizerUuid") ?: ""
                val roleUuid = backStackEntry.arguments?.getString("roleUuid") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val description = backStackEntry.arguments?.getString("description") ?: ""
                
                UpdateOrganizerRolePage(
                    organizerUuid = organizerUuid,
                    roleUuid = roleUuid,
                    initialName = name,
                    initialDescription = description,
                    organizerViewModel = organizerViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable("create_organizer_roles/{uuid}") { backStackEntry ->
                val uuid = backStackEntry.arguments?.getString("uuid") ?: ""
                CreateOrganizerRolesPage(
                    organizerUuid = uuid,
                    organizerViewModel = organizerViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable("create_organizer") {
                CreateOrganizerPage(
                    organizerViewModel = organizerViewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { 
                        navController.navigate("my_organizers") {
                            popUpTo("organizer") { inclusive = false }
                        }
                    }
                )
            }
            composable("my_organizers") {
                MyOrganizersPage(
                    themeViewModel = themeViewModel,
                    organizerViewModel = organizerViewModel,
                    onNavigateToCreate = { navController.navigate("create_organizer") },
                    onNavigateToDetail = { uuid -> navController.navigate("organizer_detail/$uuid") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("ticket") { TicketPage(themeViewModel = themeViewModel) }
                composable("profile") {
                    ProfilePage(
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
                composable("settings") {
                    SettingsPage(
                        themeViewModel = themeViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    )
}

@Composable
fun MainScreenContent(
    currentRoute: String?,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val navItems = listOf(
        NavigationData("home", R.string.nav_home, Icons.Default.Home, Color(0xFF9C27B0)),
        NavigationData("event", R.string.nav_event, Icons.Default.Event, Color(0xFF4CAF50)),
        NavigationData("organizer", R.string.nav_organizer, Icons.Default.People, Color(0xFF2196F3)),
        NavigationData("ticket", R.string.nav_ticket, Icons.Default.ConfirmationNumber, Color(0xFFFF9800)),
        NavigationData("profile", R.string.nav_profile, Icons.Default.Person, Color(0xFFF44336))
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route || 
                                   (item.route == "profile" && (currentRoute == "settings")) ||
                                   (item.route == "event" && (currentRoute?.startsWith("event_detail") == true || currentRoute == "create_event" || currentRoute == "my_events" || currentRoute?.startsWith("event_rundown") == true || currentRoute?.startsWith("add_event_rundown") == true)) ||
                                   (item.route == "organizer" && (currentRoute?.startsWith("organizer_detail") == true || currentRoute == "create_organizer" || currentRoute == "my_organizers" || currentRoute?.startsWith("create_organizer_roles") == true || currentRoute?.startsWith("add_organizer_member") == true || currentRoute?.startsWith("update_organizer_role") == true))
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { 
                            Text(
                                text = stringResource(item.labelRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        selected = selected,
                        alwaysShowLabel = false,
                        onClick = { onNavigate(item.route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = item.activeColor,
                            selectedTextColor = item.activeColor,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

private data class NavigationData(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val activeColor: Color
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreenContent(
        currentRoute = "home",
        snackbarHostState = remember { SnackbarHostState() },
        onNavigate = {},
        content = { innerPadding ->
            Text("Content Area", modifier = Modifier.padding(innerPadding))
        }
    )
}
