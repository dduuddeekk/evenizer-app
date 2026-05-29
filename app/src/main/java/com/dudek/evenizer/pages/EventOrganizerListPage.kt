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
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.EventOrganizerData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.ui.components.GradientFAB
import com.dudek.evenizer.ui.components.ModernBackground

@Composable
fun EventOrganizerListPage(
    eventUuid: String,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onNavigateToAdd: (String) -> Unit
) {
    val context = LocalContext.current
    val organizers by eventViewModel.eventOrganizers.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()

    LaunchedEffect(eventUuid) {
        eventViewModel.fetchEventOrganizers(context, eventUuid)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        text = stringResource(R.string.nav_organizer),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                if (organizers.isEmpty() && !isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.event_organizer_empty), color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Group by status
                        val statusMap = mapOf(
                            "ACCEPTED" to R.string.status_accepted,
                            "PENDING" to R.string.status_pending,
                            "FINISHED" to R.string.status_finished
                        )
                        
                        statusMap.forEach { (statusKey, stringId) ->
                            val listByStatus = organizers.filter { it.status == statusKey }
                            if (listByStatus.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(stringId),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getStatusColor(statusKey),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                items(listByStatus) { item ->
                                    EventOrganizerCard(item)
                                }
                            }
                        }
                    }
                }
            }
        }

        GradientFAB(
            onClick = { onNavigateToAdd(eventUuid) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.organizer_invite_title))
        }
    }
}

@Composable
fun EventOrganizerCard(data: EventOrganizerData) {
    val organizer = data.organizer ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (!organizer.logo.isNullOrEmpty()) {
                    AsyncImage(
                        model = organizer.logo,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.People, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organizer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Roles
                val roles = data.eventOrganizerDetails?.mapNotNull { it.role?.name }?.joinToString(", ") ?: stringResource(R.string.organizer_role_empty)
                Text(
                    text = roles,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            val statusText = when(data.status) {
                "ACCEPTED" -> stringResource(R.string.status_accepted)
                "PENDING" -> stringResource(R.string.status_pending)
                "FINISHED" -> stringResource(R.string.status_finished)
                else -> data.status
            }

            Surface(
                color = getStatusColor(data.status).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(data.status)
                )
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "ACCEPTED" -> Color(0xFF4CAF50)
        "PENDING" -> Color(0xFFFF9800)
        "FINISHED" -> Color(0xFF2196F3)
        "REJECTED" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
