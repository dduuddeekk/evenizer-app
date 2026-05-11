package com.dudek.evenizer.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
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
    onNavigateToLogin: () -> Unit,
) {
    val userProfile by userViewModel.userProfile.collectAsState()
    val isLoading by userViewModel.profileLoading.collectAsState()
    val isUploading by userViewModel.uploadLoading.collectAsState()

    val isRefreshing = remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current

    val showCropDialog = remember { mutableStateOf<Uri?>(null) }
    val showFullPreview = remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { showCropDialog.value = it }
    }

    LaunchedEffect(Unit) {
        if (userProfile == null) {
            userViewModel.fetchProfile()
        }
    }

    if (showFullPreview.value != null) {
        ProfileImagePreviewDialog(
            imageUrl = showFullPreview.value!!
        ) {
            showFullPreview.value = null
        }
    }

    if (showCropDialog.value != null) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        AlertDialog(
            onDismissRequest = { showCropDialog.value = null },
            title = { Text(text = stringResource(R.string.profile_update_title)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.profile_update_desc))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale *= zoomChange
                        offset += offsetChange
                    }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .transformable(state = state),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = showCropDialog.value,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                val containerSizePx = with(density) { 200.dp.toPx() }
                TextButton(onClick = {
                    showCropDialog.value?.let { uri ->
                        userViewModel.updateProfileImage(uri, context, scale, offset, containerSizePx)
                    }
                    showCropDialog.value = null
                }) {
                    Text(text = stringResource(R.string.btn_upload))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCropDialog.value = null }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing.value,
        onRefresh = {
            scope.launch {
                isRefreshing.value = true
                userViewModel.clearProfile()
                val fetchJob = launch { userViewModel.fetchProfile() }
                delay(2000)
                fetchJob.join()
                isRefreshing.value = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val settingsTitle = stringResource(R.string.settings_title)
        val logoutText = stringResource(R.string.profile_logout)
        val loginText = stringResource(R.string.profile_login)

        val menuItems = remember(userProfile, settingsTitle, logoutText, loginText) {
            val list = mutableListOf(
                ProfileMenuItem(settingsTitle, Icons.Default.Settings, onNavigateToSettings)
            )
            if (userProfile != null) {
                list.add(
                    ProfileMenuItem(logoutText, Icons.AutoMirrored.Filled.Logout) {
                        authViewModel.logout { 
                            userViewModel.clearProfile()
                            onNavigateToLogin() 
                        }
                    }
                )
            } else {
                list.add(
                    ProfileMenuItem(loginText, Icons.AutoMirrored.Filled.Login) {
                        onNavigateToLogin()
                    }
                )
            }
            list
        }

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
            
            if (isLoading && !isRefreshing.value) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF44336))
                }
            } else {
                UserProfileSection(
                    user = userProfile,
                    isUploading = isUploading,
                    onEditClick = { imagePicker.launch("image/*") },
                    onImageClick = { url -> showFullPreview.value = url }
                )
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
fun UserProfileSection(
    user: UserData?,
    isUploading: Boolean,
    onEditClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, Color(0xFFF44336), CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF44336).copy(alpha = 0.1f))
                    .clickable(enabled = user?.profile != null) {
                        user?.profile?.let { url ->
                            val buster = user.updatedAt ?: System.currentTimeMillis().toString()
                            val fullUrl = if (url.contains("?")) "$url&t=$buster" else "$url?t=$buster"
                            onImageClick(fullUrl)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (user?.profile != null) {
                    val imageUrl = remember(user.profile, user.updatedAt) {
                        val buster = user.updatedAt ?: System.currentTimeMillis().toString()
                        if (user.profile.contains("?")) {
                            "${user.profile}&t=$buster"
                        } else {
                            "${user.profile}?t=$buster"
                        }
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (!isUploading && user != null) {
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .clickable { onEditClick() },
                    color = Color(0xFFF44336),
                    tonalElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(20.dp))
        
        Column {
            Text(
                text = user?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }.takeIf { !it.isNullOrBlank() } ?: stringResource(R.string.profile_guest_user),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!user?.username.isNullOrBlank()) {
                Text(
                    text = "@${user.username}",
                    fontSize = 14.sp,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = user?.email ?: stringResource(R.string.profile_no_email),
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

@Composable
fun ProfileImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
