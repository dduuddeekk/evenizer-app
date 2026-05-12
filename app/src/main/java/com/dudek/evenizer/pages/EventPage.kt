package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.DpOffset
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val searchQuery = remember { mutableStateOf("") }
    val selectedDate = remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    
    val showLoginDialog = remember { mutableStateOf(false) }
    val showVerifyDialog = remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    
    val userProfile by userViewModel.userProfile.collectAsState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    val events by eventViewModel.events.collectAsState()
    val isRefreshing by eventViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        if (events.isEmpty()) {
            eventViewModel.fetchEvents(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                eventViewModel.fetchEvents(context)
            },
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

                if (filteredEvents.isEmpty() && !isRefreshing) {
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
                                eventViewModel = eventViewModel,
                                onClick = { onNavigateToDetail(event.uuid) }
                            )
                        }
                    }
                }
            }
        }

        // Expanded FAB Menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            DropdownMenu(
                expanded = showFabMenu,
                onDismissRequest = { showFabMenu = false },
                offset = DpOffset(0.dp, (-8).dp),
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.event_menu_create)) },
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
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.event_menu_my_events)) },
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

            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventData,
    languageCode: String,
    userProfile: UserData?,
    eventViewModel: EventViewModel,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isOrganizer = userProfile != null && userProfile.uuid == event.userUuid
    val isLoggedIn = userProfile != null
    
    // Check if this specific event is in favorites
    // Assuming EventData might have a field or we need to check a list
    // For now, let's use the toggleFavourite logic from ViewModel
    // Note: The ViewModel seems to only track ONE isFavourited state for detail.
    // We might need a more robust way to track favorites in a list, 
    // but I'll implement the UI as requested.
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                
                // Love Button
                if (isLoggedIn && !isOrganizer) {
                    IconButton(
                        onClick = { eventViewModel.toggleFavourite(context, event.uuid) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder, // Simple version as toggle state isn't per-item in VM yet
                            contentDescription = "Favourite",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
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
