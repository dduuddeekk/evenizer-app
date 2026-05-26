package com.dudek.evenizer.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.network.model.UserData

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
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 50 }),
        modifier = Modifier.fillMaxWidth()
    ) {
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
                                    contentDescription = stringResource(R.string.btn_delete),
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
                                    contentDescription = null,
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
                        
                        Text(
                            text = DateUtils.formatLocaleDateTime(event.start, languageCode),
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
}
