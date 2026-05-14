package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsPage(
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }
    val myEvents by eventViewModel.myEvents.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    val userProfile by userViewModel.userProfile.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf<EventData?>(null) }

    LaunchedEffect(Unit) {
        eventViewModel.fetchMyEvents(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { eventViewModel.fetchMyEvents(context) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header (Algorithm from SettingsPage)
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
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    Text(
                        text = stringResource(R.string.my_events_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                if (showDeleteDialog != null) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = null },
                        title = { Text(stringResource(R.string.delete_event_title)) },
                        text = { Text(stringResource(R.string.delete_event_desc, showDeleteDialog?.title ?: "")) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog?.let { event ->
                                        eventViewModel.deleteEvent(context, event.uuid)
                                    }
                                    showDeleteDialog = null
                                }
                            ) {
                                Text(stringResource(R.string.btn_delete), color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = null }) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    val filteredEvents = remember(searchQuery.value, myEvents) {
                        myEvents.filter {
                            searchQuery.value.isEmpty() || it.title.contains(searchQuery.value, ignoreCase = true)
                        }
                    }

                    if (filteredEvents.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(R.string.my_events_empty), color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(filteredEvents) { event ->
                                EventCard(
                                    event = event,
                                    languageCode = language,
                                    userProfile = userProfile,
                                    eventViewModel = eventViewModel,
                                    onNavigateToDetail = { onNavigateToDetail(event.uuid) },
                                    onDelete = { showDeleteDialog = event }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreate,
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Event")
        }
    }
}
