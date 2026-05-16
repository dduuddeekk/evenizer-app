package com.dudek.evenizer.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventPage(
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by userViewModel.userProfile.collectAsState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    val events by eventViewModel.events.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        if (events.isEmpty()) {
            eventViewModel.fetchEvents(context)
        }
    }

    EventPageContent(
        events = events,
        isLoading = isLoading,
        userProfile = userProfile,
        language = language,
        onRefresh = { eventViewModel.fetchEvents(context) },
        onToggleFavourite = { uuid -> eventViewModel.toggleFavourite(context, uuid) },
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToMyEvents = onNavigateToMyEvents,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPageContent(
    events: List<EventData>,
    isLoading: Boolean,
    userProfile: UserData?,
    language: String,
    onRefresh: () -> Unit,
    onToggleFavourite: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyEvents: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val searchQuery = remember { mutableStateOf("") }
    val selectedDate = remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    
    val showLoginDialog = remember { mutableStateOf(false) }
    val showVerifyDialog = remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            val datePickerState = rememberDatePickerState()

            val filteredEvents = remember(searchQuery.value, selectedDate.value, events) {
                events.filter {
                    (searchQuery.value.isEmpty() || it.title.contains(searchQuery.value, ignoreCase = true)) &&
                    (selectedDate.value.isEmpty() || it.start.startsWith(selectedDate.value))
                }
            }

            if (showDatePicker.value) {
                val onDismiss = { showDatePicker.value = false }
                DatePickerDialog(
                    onDismissRequest = onDismiss,
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                selectedDate.value = formatter.format(Date(millis))
                            }
                            onDismiss()
                        }) {
                            Text(text = stringResource(R.string.btn_ok), color = Color(0xFF4CAF50))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(R.string.btn_cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showLoginDialog.value) {
                AlertDialog(
                    onDismissRequest = { showLoginDialog.value = false },
                    title = { Text(stringResource(R.string.auth_login_required_title)) },
                    text = { Text(stringResource(R.string.auth_login_required_desc)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showLoginDialog.value = false
                            onNavigateToLogin()
                        }) {
                            Text(stringResource(R.string.btn_go_to_login), color = Color(0xFF4CAF50))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLoginDialog.value = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                )
            }

            if (showVerifyDialog.value) {
                AlertDialog(
                    onDismissRequest = { showVerifyDialog.value = false },
                    title = { Text(stringResource(R.string.auth_verify_required_title)) },
                    text = { Text(stringResource(R.string.auth_verify_required_desc)) },
                    confirmButton = {
                        TextButton(onClick = { showVerifyDialog.value = false }) {
                            Text(stringResource(R.string.btn_ok), color = Color(0xFF4CAF50))
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.nav_event),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = stringResource(R.string.search_events_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Filter Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showDatePicker.value = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedDate.value.isEmpty()) {
                            stringResource(R.string.filter_by_date)
                        } else {
                            DateUtils.formatLocaleDate(selectedDate.value, language)
                        },
                        color = if (selectedDate.value.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedDate.value.isNotEmpty()) {
                        IconButton(
                            onClick = { selectedDate.value = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredEvents.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.event_empty), color = Color.Gray)
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredEvents) { event ->
                            EventCard(
                                event = event,
                                languageCode = language,
                                userProfile = userProfile,
                                isFavorited = event.isFavorited,
                                onToggleFavourite = { onToggleFavourite(event.uuid) },
                                onNavigateToDetail = { onNavigateToDetail(event.uuid) },
                                onDelete = null
                            )
                        }
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
                    // Create Event Menu Item
                    FabMenuItem(
                        label = stringResource(R.string.event_menu_create),
                        onClick = {
                            showFabMenu = false
                            if (userProfile == null) {
                                showLoginDialog.value = true
                            } else if (userProfile?.isEmailVerified == false) {
                                showVerifyDialog.value = true
                            } else {
                                onNavigateToCreate()
                            }
                        }
                    )

                    // My Events Menu Item
                    FabMenuItem(
                        label = stringResource(R.string.event_menu_my_events),
                        onClick = {
                            showFabMenu = false
                            if (userProfile == null) {
                                showLoginDialog.value = true
                            } else {
                                onNavigateToMyEvents()
                            }
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu },
                containerColor = Color(0xFF4CAF50),
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

@Composable
fun FabMenuItem(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.wrapContentSize()
    ) {
        PaddingValues(horizontal = 16.dp, vertical = 8.dp).let {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EventCard(
    event: EventData,
    languageCode: String,
    userProfile: UserData?,
    isFavorited: Boolean = false,
    onToggleFavourite: () -> Unit = {},
    onNavigateToDetail: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isOrganizer = userProfile != null && userProfile.uuid == event.userUuid
    val isLoggedIn = userProfile != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (event.banner != null) {
                    AsyncImage(
                        model = event.banner,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isOrganizer && onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isLoggedIn && !isOrganizer) {
                        IconButton(
                            onClick = onToggleFavourite,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favourite",
                                tint = if (isFavorited) Color.Red else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date and Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val datePart = event.start.take(10)
                    val timePart = if (event.start.length >= 16) {
                        event.start.substring(11, 16)
                    } else ""
                    
                    Text(
                        text = "${DateUtils.formatLocaleDate(datePart, languageCode)} ${if (timePart.isNotEmpty()) "• $timePart" else ""}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.eventLocations?.firstOrNull()?.location ?: stringResource(R.string.create_event_loc_online),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventPagePreview() {
    EventPageContent(
        events = emptyList(),
        isLoading = false,
        userProfile = null,
        language = "id",
        onRefresh = {},
        onToggleFavourite = {},
        onNavigateToCreate = {},
        onNavigateToMyEvents = {},
        onNavigateToDetail = {},
        onNavigateToLogin = {}
    )
}
