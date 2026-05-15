package com.dudek.evenizer.pages

import androidx.compose.foundation.background
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
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.utils.DateUtils

@Composable
fun EventDetailPage(
    uuid: String,
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val event by eventViewModel.eventDetail.collectAsState()
    val isFavourited by eventViewModel.isFavourited.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    val userProfile by userViewModel.userProfile.collectAsState()

    LaunchedEffect(uuid) {
        eventViewModel.fetchEventDetail(context, uuid)
    }

    EventDetailPageContent(
        event = event,
        isFavourited = isFavourited,
        isLoading = isLoading,
        language = language,
        userProfile = userProfile,
        onBack = onBack,
        onToggleFavourite = { eventViewModel.toggleFavourite(context, uuid) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailPageContent(
    event: EventData?,
    isFavourited: Boolean,
    isLoading: Boolean,
    language: String,
    userProfile: UserData?,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val isOrganizer = userProfile != null && event != null && userProfile.uuid == event.userUuid
    val isLoggedIn = userProfile != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoggedIn && !isOrganizer) {
                        IconButton(onClick = onToggleFavourite) {
                            Icon(
                                imageVector = if (isFavourited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (isFavourited) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
        } else if (event != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
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
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))
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
        isFavourited = false,
        isLoading = true,
        language = "id",
        userProfile = null,
        onBack = {},
        onToggleFavourite = {}
    )
}
