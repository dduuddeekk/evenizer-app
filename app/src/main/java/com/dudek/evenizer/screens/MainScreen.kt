package com.dudek.evenizer.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.dudek.evenizer.pages.CreateEventPage
import com.dudek.evenizer.pages.EventDetailPage
import com.dudek.evenizer.pages.EventPage
import com.dudek.evenizer.pages.MyEventsPage
import com.dudek.evenizer.pages.HomePage
import com.dudek.evenizer.pages.OrganizerPage
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
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    MainScreenContent(
        currentRoute = currentRoute,
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
                composable("home") { HomePage(themeViewModel = themeViewModel) }
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
                        onBack = { navController.popBackStack() }
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
                    onNavigateToCreate = { navController.navigate("create_organizer") },
                    onNavigateToMyOrganizers = { navController.navigate("my_organizers") },
                    onNavigateToLogin = onNavigateToLogin
                ) 
            }
            composable("create_organizer") {
                CreateOrganizerPage(
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
                    onNavigateToCreate = { navController.navigate("create_organizer") },
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
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route || 
                                   (item.route == "profile" && currentRoute == "settings") ||
                                   (item.route == "event" && (currentRoute == "create_event" || currentRoute == "my_events"))
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
        onNavigate = {},
        content = { innerPadding ->
            Text("Content Area", modifier = Modifier.padding(innerPadding))
        }
    )
}
