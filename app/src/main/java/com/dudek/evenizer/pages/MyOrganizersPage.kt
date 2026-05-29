package com.dudek.evenizer.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.ui.components.GradientFAB
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import com.dudek.evenizer.utils.OrganizerCardSkeleton

@Composable
fun MyOrganizersPage(
    organizerViewModel: OrganizerViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val myOrganizers by organizerViewModel.myOrganizers.collectAsState()
    val isLoading by organizerViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        organizerViewModel.fetchMyOrganizers(context)
    }

    MyOrganizersPageContent(
        organizers = myOrganizers,
        isLoading = isLoading,
        onBack = onBack,
        onRefresh = { organizerViewModel.fetchMyOrganizers(context) },
        onToggleFollow = { uuid -> organizerViewModel.toggleFollow(context, uuid) },
        onDelete = { uuid -> organizerViewModel.deleteOrganizer(context, uuid) {} },
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToCreate = onNavigateToCreate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrganizersPageContent(
    organizers: List<OrganizerData>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFollow: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ModernBackground {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
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
                                contentDescription = stringResource(R.string.create_event_back_desc),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Text(
                            text = stringResource(R.string.my_organizers_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    if (organizers.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = stringResource(R.string.my_organizers_empty), color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            if (isLoading && organizers.isEmpty()) {
                                items(5) {
                                    OrganizerCardSkeleton()
                                }
                            } else {
                                items(organizers) { organizer ->
                                    OrganizerCard(
                                        organizer = organizer,
                                        currentUserUuid = organizer.userUuid, // In MyOrganizers, we are the owner
                                        onToggleFollow = { onToggleFollow(organizer.uuid) },
                                        onDelete = { onDelete(organizer.uuid) },
                                        onClick = { onNavigateToDetail(organizer.uuid) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        GradientFAB(
            onClick = onNavigateToCreate,
            gradient = LocalGradients.current.tertiary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.organizer_menu_create))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyOrganizersPagePreview() {
    MyOrganizersPageContent(
        organizers = emptyList(),
        isLoading = false,
        onBack = {},
        onRefresh = {},
        onToggleFollow = {},
        onDelete = {},
        onNavigateToDetail = {},
        onNavigateToCreate = {}
    )
}
