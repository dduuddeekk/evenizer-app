package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.AuthViewModel
import com.dudek.evenizer.models.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.profileLoading.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (userProfile == null) {
            userViewModel.fetchProfile()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                userViewModel.fetchProfile()
                delay(1000) // Ensure spinner is visible for a moment
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val menuItems = listOf(
            ProfileMenuItem(stringResource(R.string.settings_title), Icons.Default.Settings, onNavigateToSettings),
            ProfileMenuItem(stringResource(R.string.profile_logout), Icons.AutoMirrored.Filled.Logout) {
                authViewModel.logout { 
                    userViewModel.clearProfile()
                    onNavigateToLogin() 
                }
            }
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.nav_profile),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading && !isRefreshing) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF44336))
                }
            } else {
                UserProfileSection(userProfile)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(menuItems) { item ->
                    ProfileMenuButton(item)
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun UserProfileSection(user: UserData?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(80.dp).clip(CircleShape),
            color = Color(0xFFF44336).copy(alpha = 0.1f)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column {
            Text(
                text = user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }.takeIf { !it.isNullOrBlank() } ?: "Guest User",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user?.email ?: "No email provided",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            if (user?.isEmailVerified == true) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.profile_email_verified),
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            } else if (user != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.profile_email_unverified),
                        fontSize = 12.sp,
                        color = Color(0xFFF44336)
                    )
                }
            }
        }
    }
}

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileMenuButton(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFFF44336),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}
