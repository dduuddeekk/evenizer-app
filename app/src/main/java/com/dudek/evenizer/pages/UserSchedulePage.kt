package com.dudek.evenizer.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
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
import com.dudek.evenizer.data.network.model.RundownData
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.utils.DateUtils
import com.dudek.evenizer.utils.DetailSkeleton

@Composable
fun UserSchedulePage(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit
) {
    val schedule by userViewModel.userSchedule.collectAsState()
    val isLoading by userViewModel.profileLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        userViewModel.fetchUserSchedule(context)
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFFF44336))
    ) {
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
                        text = stringResource(R.string.profile_menu_schedule),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                if (isLoading && schedule.isEmpty()) {
                    DetailSkeleton()
                } else if (schedule.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.rundown_empty), color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        schedule.forEach { yearData ->
                            yearData.months.forEach { monthData ->
                                item {
                                    Text(
                                        text = "${getMonthName(monthData.month)} ${yearData.year}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                monthData.days.forEach { dayData ->
                                    item {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Surface(
                                                modifier = Modifier.size(50.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = dayData.day.toString(),
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(16.dp))
                                            
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                dayData.rundowns.forEach { rundown ->
                                                    ScheduleItemCard(
                                                        rundown = rundown,
                                                        onClick = { rundown.event?.uuid?.let { onNavigateToEvent(it) } }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCard(rundown: RundownData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = rundown.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (rundown.event != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = rundown.event.title, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${DateUtils.formatTimeOnly(rundown.start)} - ${DateUtils.formatTimeOnly(rundown.end)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rundown.location != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = rundown.location.location, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return java.text.DateFormatSymbols().months[month - 1]
}
