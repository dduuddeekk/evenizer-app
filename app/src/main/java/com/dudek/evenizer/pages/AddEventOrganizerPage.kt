package com.dudek.evenizer.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.ModernBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventOrganizerPage(
    eventUuid: String,
    eventViewModel: EventViewModel,
    organizerViewModel: OrganizerViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val allOrganizers by organizerViewModel.organizers.collectAsState()
    val availableRoles by organizerViewModel.organizerRoles.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val error by eventViewModel.error.collectAsState()

    var selectedOrganizer by remember { mutableStateOf<OrganizerData?>(null) }
    var selectedRoleUuids by remember { mutableStateOf(setOf<String>()) }
    var organizerSearchQuery by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val eventDetail by eventViewModel.eventDetail.collectAsState()

    LaunchedEffect(eventDetail) {
        // Fetch organizers with event description filter once event detail is available
        val eventDesc = eventDetail?.description
        organizerViewModel.fetchOrganizers(context, eventDescription = eventDesc)
    }

    LaunchedEffect(selectedOrganizer) {
        selectedOrganizer?.let {
            organizerViewModel.fetchOrganizerDetail(context, it.uuid)
            selectedRoleUuids = emptySet()
        }
    }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF4CAF50))
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
                        text = stringResource(R.string.organizer_invite_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier
                        .padding(24.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Recommendation Section
                    if (selectedOrganizer == null) {
                        item {
                            Text(
                                text = stringResource(R.string.organizer_recommendation_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (allOrganizers.isEmpty() && !isLoading) {
                            item {
                                Text(stringResource(R.string.organizer_recommendation_empty), color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            items(allOrganizers.take(5)) { org ->
                                RecommendationCard(
                                    org,
                                    onClick = { selectedOrganizer = org }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        // Manual Search
                        item {
                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = organizerSearchQuery,
                                    onValueChange = {
                                        organizerSearchQuery = it
                                        dropdownExpanded = true
                                    },
                                    label = { Text(stringResource(R.string.organizer_search_other)) },
                                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    val filtered = allOrganizers.filter { it.name.contains(organizerSearchQuery, ignoreCase = true) }
                                    filtered.forEach { org ->
                                        DropdownMenuItem(
                                            text = { Text(org.name) },
                                            onClick = {
                                                selectedOrganizer = org
                                                organizerSearchQuery = ""
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Selected Organizer Context
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(R.string.organizer_selected_title, selectedOrganizer?.name ?: ""),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { selectedOrganizer = null }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cancel), tint = Color.Gray)
                                        }
                                    }
                                    Text(
                                        text = selectedOrganizer?.description ?: "",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = stringResource(R.string.organizer_role_selection_title, selectedOrganizer?.name ?: ""),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        if (availableRoles.isEmpty()) {
                            item {
                                Text(stringResource(R.string.organizer_role_empty), color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            items(availableRoles) { role ->
                                val isSelected = selectedRoleUuids.contains(role.uuid)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRoleUuids = if (isSelected) {
                                                selectedRoleUuids - role.uuid
                                            } else {
                                                selectedRoleUuids + role.uuid
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = role.name, fontWeight = FontWeight.Bold)
                                            Text(text = role.description, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val successMsg = stringResource(R.string.invitation_sent_success)
                GradientButton(
                    onClick = {
                        selectedOrganizer?.let { org ->
                            eventViewModel.inviteOrganizerToEvent(
                                context = context,
                                eventUuid = eventUuid,
                                organizerUuid = org.uuid,
                                roleUuids = selectedRoleUuids.toList()
                            ) {
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    enabled = !isLoading && selectedOrganizer != null && selectedRoleUuids.isNotEmpty(),
                    gradient = com.dudek.evenizer.ui.theme.LocalGradients.current.secondary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.btn_send_invitation), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(
    organizer: OrganizerData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo/Initial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = organizer.name.take(1),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organizer.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.organizer_rating_followers, 4.5f, organizer.followCount),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f)
            )
        }
    }
}
