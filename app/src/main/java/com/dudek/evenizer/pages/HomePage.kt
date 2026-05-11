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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.Event
import com.dudek.evenizer.data.MockData
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(themeViewModel: ThemeViewModel) {
    val scrollState = rememberScrollState()
    val language by themeViewModel.language.collectAsState(initial = "id")
    
    val isRefreshing = remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing.value,
        onRefresh = {
            scope.launch {
                isRefreshing.value = true
                delay(2000) // Simulate data reload
                isRefreshing.value = false
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF9C27B0))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Stats Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.home_stat_total_events),
                    value = MockData.events.size.toString(),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.home_stat_organizers),
                    value = MockData.organizers.size.toString(),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Events Preview
            SectionHeader(title = stringResource(R.string.home_section_upcoming), actionText = stringResource(R.string.home_see_all))
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(MockData.events.take(3)) { event ->
                    HomeEventCard(event, language)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Top Organizers Preview
            SectionHeader(title = stringResource(R.string.home_section_available_organizers), actionText = stringResource(R.string.home_see_all))
            Spacer(modifier = Modifier.height(12.dp))
            
            MockData.organizers.take(2).forEach { organizer ->
                OrganizerCard(organizer, language)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 12.sp, color = color.copy(alpha = 0.7f))
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
        Text(text = actionText, fontSize = 14.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Medium)
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
