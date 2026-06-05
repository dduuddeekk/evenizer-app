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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.network.model.RoleData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import com.dudek.evenizer.utils.DateUtils
import com.dudek.evenizer.utils.DetailSkeleton

@Composable
fun EventDetailPage(
    uuid: String,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToRundown: (String) -> Unit,
    onNavigateToOrganizers: (String) -> Unit
) {
    val context = LocalContext.current
    val event by eventViewModel.eventDetail.collectAsState()
    val eventRoles by eventViewModel.eventRoles.collectAsState()
    val isFavourited by eventViewModel.isFavourited.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    val userProfile by userViewModel.userProfile.collectAsState()

    LaunchedEffect(uuid) {
        eventViewModel.fetchEventDetail(context, uuid)
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF4CAF50))
    ) {
        EventDetailPageContent(
            event = event,
            eventRoles = eventRoles,
            isFavourited = isFavourited,
            isLoading = isLoading,
            language = language,
            userProfile = userProfile,
            onBack = onBack,
            onToggleFavourite = { eventViewModel.toggleFavourite(context, uuid) },
            onNavigateToEdit = { onNavigateToEdit(uuid) },
            onNavigateToRundown = { onNavigateToRundown(uuid) },
            onNavigateToOrganizers = { onNavigateToOrganizers(uuid) }
        )
    }
}

@Composable
fun EventDetailPageContent(
    event: EventData?,
    eventRoles: List<RoleData>,
    isFavourited: Boolean,
    isLoading: Boolean,
    language: String,
    userProfile: UserData?,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToRundown: () -> Unit,
    onNavigateToOrganizers: () -> Unit
) {
    val isOrganizer = userProfile != null && event != null && userProfile.uuid == event.userUuid
    val isLoggedIn = userProfile != null
    val scrollState = rememberScrollState()
    var showFabMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        ModernBackground {
            Column(
                modifier = Modifier.fillMaxSize()
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
                            contentDescription = stringResource(R.string.create_event_back_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = stringResource(R.string.event_detail_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                    
                    if (isLoggedIn && !isOrganizer && event != null) {
                        IconButton(onClick = onToggleFavourite) {
                            Icon(
                                imageVector = if (isFavourited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavourited) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                if (isLoading && event == null) {
                    DetailSkeleton()
                } else if (event != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        // Banner
                        AsyncImage(
                            model = event.banner,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(24.dp)) {
                            // Title
                            Text(
                                text = event.title,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Date & Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${stringResource(R.string.event_detail_start)}: ${DateUtils.formatLocaleDateTime(event.start, language)}",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${stringResource(R.string.event_detail_end)}:   ${DateUtils.formatLocaleDateTime(event.end, language)}",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Location
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    val loc = event.eventLocations?.firstOrNull()
                                    Text(
                                        text = loc?.location ?: stringResource(R.string.create_event_loc_online),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (loc != null) {
                                        Text(
                                            text = when(loc.type) {
                                                "ONLINE" -> stringResource(R.string.create_event_loc_online)
                                                "OFFLINE" -> stringResource(R.string.create_event_loc_offline)
                                                else -> stringResource(R.string.create_event_loc_hybrid)
                                            },
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Description
                            Text(
                                text = stringResource(R.string.event_detail_desc_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = event.description,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (eventRoles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = stringResource(R.string.event_detail_sie_title),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                eventRoles.forEach { role ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = role.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = role.description,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }

        // Background Dim
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

        if (isOrganizer) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
                Column(
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
                            Surface(
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToOrganizers()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Text(
                                    text = stringResource(R.string.nav_organizer),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToEdit()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Text(
                                    text = stringResource(R.string.menu_edit_event),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                onClick = {
                                    showFabMenu = false
                                    onNavigateToRundown()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp
                            ) {
                                Text(
                                    text = stringResource(R.string.rundown_title),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { showFabMenu = !showFabMenu },
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(LocalGradients.current.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Crossfade(targetState = showFabMenu, label = "FabIcon") { isOpen ->
                                Icon(
                                    imageVector = if (isOpen) Icons.Default.Close else Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventDetailPagePreview() {
    EventDetailPageContent(
        event = null,
        eventRoles = emptyList(),
        isFavourited = false,
        isLoading = true,
        language = "id",
        userProfile = null,
        onBack = {},
        onToggleFavourite = {},
        onNavigateToEdit = {},
        onNavigateToRundown = {},
        onNavigateToOrganizers = {}
    )
}
