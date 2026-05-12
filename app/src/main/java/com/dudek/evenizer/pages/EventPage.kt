package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
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
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredEvents) { event ->
                            EventCard(event, language)
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
fun EventCard(event: EventData, languageCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (event.banner != null) {
                AsyncImage(
                    model = event.banner,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val firstCategory = event.categories?.firstOrNull()?.categoryDetails?.firstOrNull()?.name
                if (firstCategory != null) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = firstCategory,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = DateUtils.formatLocaleDate(event.start.take(10), languageCode),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.eventLocations?.firstOrNull()?.location ?: stringResource(R.string.create_event_loc_online),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.create_event_status_label, event.status),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
        }
    }
}
