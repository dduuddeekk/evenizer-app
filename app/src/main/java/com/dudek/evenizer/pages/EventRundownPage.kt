package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.data.network.model.RundownData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.utils.DateUtils

@Composable
fun EventRundownPage(
    eventUuid: String,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onNavigateToAddRundown: (String) -> Unit
) {
    val context = LocalContext.current
    val rundowns by eventViewModel.eventRundowns.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()

    LaunchedEffect(eventUuid) {
        eventViewModel.fetchEventRundowns(context, eventUuid)
    }

    val publicRundowns = rundowns.filter { it.visibility == "PUBLIC" }
    val privateRundowns = rundowns.filter { it.visibility == "PRIVATE" }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("PUBLIC", "PRIVATE")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        contentDescription = "Back",
                        tint = Color(0xFF4CAF50)
                    )
                }
                Text(
                    text = "Rundown Acara",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = Color(0xFF4CAF50),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF4CAF50)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    )
                }
            }

            val currentList = if (selectedTab == 0) publicRundowns else privateRundowns

            if (currentList.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Belum ada rundown", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { rundown ->
                        RundownCard(rundown)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToAddRundown(eventUuid) },
            containerColor = Color(0xFF4CAF50),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Rundown")
        }
    }
}

@Composable
fun RundownCard(rundown: RundownData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    text = rundown.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = if (rundown.status == "DONE") Color(0xFF4CAF50).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = rundown.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rundown.status == "DONE") Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                val startTime = DateUtils.formatTimeOnly(rundown.start)
                val endTime = DateUtils.formatTimeOnly(rundown.end)
                Text(
                    text = "$startTime - $endTime",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rundown.location != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lokasi: ${rundown.location.location}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            if (!rundown.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rundown.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
