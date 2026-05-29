package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.Event
import com.dudek.evenizer.data.MockData
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import com.dudek.evenizer.utils.DateUtils
import com.dudek.evenizer.utils.EventCardSkeleton
import com.dudek.evenizer.utils.OrganizerCardSkeleton
import com.dudek.evenizer.utils.StatCardSkeleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomePage(
    themeViewModel: ThemeViewModel,
    eventViewModel: EventViewModel,
    organizerViewModel: OrganizerViewModel,
    onNavigateToNotifications: () -> Unit
) {
    val language by themeViewModel.language.collectAsState(initial = "id")
    val events by eventViewModel.events.collectAsState()
    val organizers by organizerViewModel.organizers.collectAsState()
    val isLoadingEvents by eventViewModel.isLoading.collectAsState()
    val isLoadingOrganizers by organizerViewModel.isLoading.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current

    DisposableEffect(Unit) {
        eventViewModel.startRealtimeEvents(context)
        organizerViewModel.startRealtimeOrganizers(context)
        onDispose {
            eventViewModel.stopRealtimeEvents()
            organizerViewModel.stopRealtimeOrganizers()
        }
    }

    HomePageContent(
        language = language,
        events = events,
        organizers = organizers,
        isLoading = isLoadingEvents || isLoadingOrganizers,
        onRefresh = {
            eventViewModel.fetchEvents(context)
            organizerViewModel.fetchOrganizers(context)
        },
        onNotificationClick = onNavigateToNotifications
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageContent(
    language: String,
    events: List<EventData>,
    organizers: List<OrganizerData>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        ModernBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.nav_home),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        text = stringResource(R.string.profile_welcome),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF9C27B0))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Stats Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isLoading && events.isEmpty()) {
                    StatCardSkeleton(modifier = Modifier.weight(1f))
                    StatCardSkeleton(modifier = Modifier.weight(1f))
                } else {
                    StatCard(
                        title = stringResource(R.string.home_stat_total_events),
                        value = events.size.toString(),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = stringResource(R.string.home_stat_organizers),
                        value = organizers.size.toString(),
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Events Preview
            SectionHeader(title = stringResource(R.string.home_section_upcoming), actionText = stringResource(R.string.home_see_all))
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                if (isLoading && events.isEmpty()) {
                    items(3) {
                        EventCardSkeleton(modifier = Modifier.width(200.dp))
                    }
                } else {
                    items(events.take(3)) { event ->
                        HomeEventCardFromData(event, language)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Top Organizers Preview
            SectionHeader(title = stringResource(R.string.home_section_available_organizers), actionText = stringResource(R.string.home_see_all))
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading && organizers.isEmpty()) {
                repeat(2) {
                    OrganizerCardSkeleton()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                organizers.take(2).forEach { organizer ->
                    OrganizerCard(
                        organizer = organizer,
                        languageCode = language,
                        currentUserUuid = null,
                        onToggleFollow = { /* No-op on home page */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
}

@Composable
fun HomeEventCardFromData(event: EventData, languageCode: String) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = DateUtils.formatLocaleDateTime(event.start, languageCode),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = event.eventLocations?.firstOrNull()?.location ?: stringResource(R.string.create_event_loc_online),
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val gradient = when (color) {
        Color(0xFF4CAF50) -> LocalGradients.current.secondary
        else -> LocalGradients.current.primary
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.background(gradient).fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = actionText, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HomeEventCard(event: Event, languageCode: String) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = event.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = DateUtils.formatLocaleDate(event.date, languageCode),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(text = event.location, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePageContent(
        language = "id",
        events = emptyList(),
        organizers = emptyList(),
        isLoading = false,
        onRefresh = {},
        onNotificationClick = {}
    )
}
