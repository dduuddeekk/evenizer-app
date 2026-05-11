package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.Event
import com.dudek.evenizer.data.MockData
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventPage(themeViewModel: ThemeViewModel) {
    val searchQuery = remember { mutableStateOf("") }
    val selectedDate = remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    
    val language by themeViewModel.language.collectAsState(initial = "id")

    val isRefreshing = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing.value,
        onRefresh = {
            scope.launch {
                isRefreshing.value = true
                searchQuery.value = ""
                selectedDate.value = ""
                delay(2000) // Simulate data reload
                isRefreshing.value = false
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val datePickerState = rememberDatePickerState()

        val filteredEvents = remember(searchQuery.value, selectedDate.value) {
            MockData.events.filter {
                (searchQuery.value.isEmpty() || it.title.contains(searchQuery.value, ignoreCase = true)) &&
                (selectedDate.value.isEmpty() || it.date == selectedDate.value)
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

            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.event_empty), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredEvents) { event ->
                        EventCard(event, language)
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, languageCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = Color.White
                    )
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
                    text = DateUtils.formatLocaleDate(event.date, languageCode),
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
                Text(text = event.location, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Organizer: ${event.organizer}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
        }
    }
}
