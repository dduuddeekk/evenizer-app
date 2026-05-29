package com.dudek.evenizer.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
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
import com.dudek.evenizer.models.NotificationViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import com.dudek.evenizer.utils.DateUtils
import com.dudek.evenizer.utils.DetailSkeleton
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun NotificationDetailPage(
    uuid: String,
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notification by notificationViewModel.notificationDetail.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    LaunchedEffect(uuid) {
        notificationViewModel.fetchNotificationDetail(context, uuid)
        notificationViewModel.markAsRead(context, uuid)
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF9C27B0))
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
                    text = stringResource(R.string.notification_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            if (isLoading) {
                DetailSkeleton()
            } else if (notification != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Icon and Type
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = notification!!.type,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = DateUtils.formatLocaleDateTime(notification!!.createdAt, "id"),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = notification!!.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Message
                    Text(
                        text = notification!!.message,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (notification!!.type == "EVENT_ORGANIZER_REQUEST") {
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GradientButton(
                                onClick = {
                                    val meta = notification!!.metadata?.jsonObject
                                    val eUuid = meta?.get("eventUuid")?.jsonPrimitive?.content ?: ""
                                    val oUuid = meta?.get("organizerUuid")?.jsonPrimitive?.content ?: ""
                                    
                                    if (eUuid.isNotBlank() && oUuid.isNotBlank()) {
                                        notificationViewModel.respondToOrganizerRequest(context, eUuid, oUuid, "ACCEPTED") {
                                            Toast.makeText(context, "Undangan diterima", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                gradient = LocalGradients.current.secondary
                            ) {
                                Text("Terima", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("Tolak", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (showRejectDialog) {
                AlertDialog(
                    onDismissRequest = { showRejectDialog = false },
                    title = { Text("Tolak Undangan") },
                    text = {
                        Column {
                            Text("Berikan alasan penolakan:")
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = rejectReason,
                                onValueChange = { rejectReason = it },
                                label = { Text("Alasan") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val meta = notification!!.metadata?.jsonObject
                                val eUuid = meta?.get("eventUuid")?.jsonPrimitive?.content ?: ""
                                val oUuid = meta?.get("organizerUuid")?.jsonPrimitive?.content ?: ""
                                
                                if (eUuid.isNotBlank() && oUuid.isNotBlank()) {
                                    notificationViewModel.respondToOrganizerRequest(context, eUuid, oUuid, "REJECTED", rejectReason) {
                                        Toast.makeText(context, "Undangan ditolak", Toast.LENGTH_SHORT).show()
                                        showRejectDialog = false
                                        onBack()
                                    }
                                }
                            },
                            enabled = rejectReason.isNotBlank()
                        ) {
                            Text("Kirim", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRejectDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
}
