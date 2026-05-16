package com.dudek.evenizer.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel

@Composable
fun OrganizerDetailPage(
    uuid: String,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    organizerViewModel: OrganizerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val organizer by organizerViewModel.organizerDetail.collectAsState()
    val isLoading by organizerViewModel.isLoading.collectAsState()
    val userProfile by userViewModel.userProfile.collectAsState()

    LaunchedEffect(uuid) {
        organizerViewModel.fetchOrganizerDetail(context, uuid)
    }

    OrganizerDetailPageContent(
        organizer = organizer,
        isLoading = isLoading,
        userProfile = userProfile,
        onBack = onBack,
        onAddMember = { /* TODO: Implement add member */ },
        onAddRole = { /* TODO: Implement add role */ },
        onEditOrganizer = { /* TODO: Implement edit */ }
    )
}

@Composable
fun OrganizerDetailPageContent(
    organizer: OrganizerData?,
    isLoading: Boolean,
    userProfile: UserData?,
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onAddRole: () -> Unit,
    onEditOrganizer: () -> Unit
) {
    val isOwner = userProfile != null && organizer != null && userProfile.uuid == organizer.userUuid
    val scrollState = rememberScrollState()
    var showFabMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF2196F3)
                    )
                }
                Text(
                    text = stringResource(R.string.nav_organizer),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else if (organizer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Profile-style Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo (Circular like ProfilePage)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!organizer.logo.isNullOrEmpty()) {
                                AsyncImage(
                                    model = organizer.logo,
                                    contentDescription = "Organizer Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = organizer.name.take(1),
                                    color = Color(0xFF2196F3),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            // Name
                            Text(
                                text = organizer.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Stats / Rating
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "4.5 • ${organizer._count?.eventOrganizers ?: 0} " + stringResource(R.string.home_stat_total_events),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        // Description Section
                        Text(
                            text = stringResource(R.string.create_organizer_field_desc),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = organizer.description ?: "No description available.",
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Verification Status
                        if (organizer.isVerified) {
                            Surface(
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ Verified Organizer",
                                    modifier = Modifier.padding(12.dp),
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // Background Dim when FAB is open
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showFabMenu = false }
            )
        }

        if (isOwner) {
            // Expanded FAB Menu
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = fadeIn() + expandVertically() + slideInVertically { it / 2 },
                    exit = fadeOut() + shrinkVertically() + slideOutVertically { it / 2 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FabMenuItem(
                            label = stringResource(R.string.menu_add_member),
                            onClick = {
                                showFabMenu = false
                                onAddMember()
                            }
                        )
                        FabMenuItem(
                            label = stringResource(R.string.menu_add_role),
                            onClick = {
                                showFabMenu = false
                                onAddRole()
                            }
                        )
                        FabMenuItem(
                            label = stringResource(R.string.menu_edit_organizer),
                            onClick = {
                                showFabMenu = false
                                onEditOrganizer()
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Crossfade(targetState = showFabMenu, label = "FabIcon") { isOpen ->
                        Icon(
                            imageVector = if (isOpen) Icons.Default.Close else Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrganizerDetailPagePreview() {
    OrganizerDetailPageContent(
        organizer = null,
        isLoading = true,
        userProfile = null,
        onBack = {},
        onAddMember = {},
        onAddRole = {},
        onEditOrganizer = {}
    )
}
