package com.dudek.evenizer.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.items.NavItem
import com.dudek.evenizer.R
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.pages.EventPage
import com.dudek.evenizer.pages.HomePage
import com.dudek.evenizer.pages.OrganizerPage
import com.dudek.evenizer.pages.ProfilePage
import com.dudek.evenizer.pages.SettingsPage

@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    val navItemList = listOf(
        NavItem(
            label = stringResource(R.string.nav_home),
            icon = Icons.Default.Home,
            0,
            Color(0xFF9C27B0)
        ),
        NavItem(
            label = stringResource(R.string.nav_event),
            icon = Icons.Default.CalendarMonth,
            0,
            Color(0xFF4CAF50)
        ),
        NavItem(
            label = stringResource(R.string.nav_organizer),
            icon = Icons.Default.People,
            0,
            Color(0xFF2196F3)
        ),
        NavItem(
            label = stringResource(R.string.nav_profile),
            icon = Icons.Default.Person,
            0,
            Color(0xFFF44336)
        )
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    var isSettingsVisible by remember {
        mutableStateOf(false)
    }

    // Handle system back button
    BackHandler(enabled = isSettingsVisible || selectedIndex != 0) {
        if (isSettingsVisible) {
            isSettingsVisible = false
        } else {
            selectedIndex = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isSettingsVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(110.dp)
                ) {
                    navItemList.forEachIndexed { index, navItem ->
                        val isSelected = selectedIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedIndex = index
                            },
                            icon = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .padding(vertical = 0.dp)
                                        .animateContentSize()
                                ) {
                                    BadgedBox(badge = {
                                        if (navItem.badgeCount > 0) {
                                            Badge {
                                                Text(text = navItem.badgeCount.toString())
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = navItem.icon,
                                            contentDescription = navItem.label,
                                            modifier = Modifier.size(26.dp),
                                            tint = if (isSelected) navItem.color else Color.LightGray
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isSelected,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Spacer(modifier = Modifier.height(0.5.dp))
                                            Text(
                                                text = navItem.label,
                                                fontSize = 12.sp,
                                                color = navItem.color,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = navItem.color,
                                unselectedIconColor = Color.LightGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isSettingsVisible) {
            SettingsPage(
                modifier = Modifier.padding(innerPadding),
                onBack = { isSettingsVisible = false },
                themeViewModel = themeViewModel
            )
        } else {
            Box(modifier = Modifier.padding(innerPadding)) {
                ContentScreen(
                    selectedIndex = selectedIndex,
                    onNavigateToSettings = { isSettingsVisible = true }
                )
            }
        }
    }
}

@Composable
fun ContentScreen(
    selectedIndex: Int,
    onNavigateToSettings: () -> Unit
) {
    when (selectedIndex) {
        0 -> HomePage()
        1 -> EventPage()
        2 -> OrganizerPage()
        3 -> ProfilePage(onNavigateToSettings = onNavigateToSettings)
    }
}
