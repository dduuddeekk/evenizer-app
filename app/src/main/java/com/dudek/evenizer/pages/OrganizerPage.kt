package com.dudek.evenizer.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import com.dudek.evenizer.utils.DateUtils
import com.dudek.evenizer.utils.OrganizerCardSkeleton
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrganizerPage(
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    organizerViewModel: OrganizerViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyOrganizers: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val language by themeViewModel.language.collectAsState(initial = "id")
    val userProfile by userViewModel.userProfile.collectAsState()
    val organizers = organizerViewModel.getPagedOrganizers(context).collectAsLazyPagingItems()

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF2196F3), tertiary = Color(0xFF2196F3))
    ) {
        OrganizerPageContent(
            language = language,
            userProfile = userProfile,
            organizers = organizers,
            onRefresh = { organizers.refresh() },
            onToggleFollow = { uuid -> organizerViewModel.toggleFollow(context, uuid) },
            onDelete = { uuid -> organizerViewModel.deleteOrganizer(context, uuid) { organizers.refresh() } },
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCreate = onNavigateToCreate,
            onNavigateToMyOrganizers = onNavigateToMyOrganizers,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerPageContent(
    language: String,
    userProfile: UserData?,
    organizers: LazyPagingItems<OrganizerData>,
    onRefresh: () -> Unit,
    onToggleFollow: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyOrganizers: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val selectedDate = remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    
    var showFabMenu by remember { mutableStateOf(false) }
    val showLoginDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        ModernBackground {
            PullToRefreshBox(
                isRefreshing = organizers.loadState.refresh is LoadState.Loading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                val datePickerState = rememberDatePickerState()

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
                                Text(text = stringResource(R.string.btn_ok), color = MaterialTheme.colorScheme.tertiary)
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

                if (showLoginDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showLoginDialog.value = false },
                        title = { Text(stringResource(R.string.auth_login_required_title)) },
                        text = { Text(stringResource(R.string.auth_login_required_desc)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showLoginDialog.value = false
                                onNavigateToLogin()
                            }) {
                                Text(stringResource(R.string.btn_go_to_login), color = MaterialTheme.colorScheme.tertiary)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLoginDialog.value = false }) {
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
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.nav_organizer),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.check_availability),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (selectedDate.value.isEmpty()) {
                                stringResource(R.string.filter_any_day)
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
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_clear), tint = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            count = organizers.itemCount,
                            key = organizers.itemKey { it.uuid },
                            contentType = organizers.itemContentType { "organizer" }
                        ) { index ->
                            val organizer = organizers[index]
                            if (organizer != null) {
                                OrganizerCard(
                                    organizer = organizer,
                                    currentUserUuid = userProfile?.uuid,
                                    onToggleFollow = { onToggleFollow(organizer.uuid) },
                                    onDelete = { onDelete(organizer.uuid) },
                                    onClick = { onNavigateToDetail(organizer.uuid) }
                                )
                            }
                        }

                        when (val state = organizers.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    Text(
                                        text = state.error.message ?: "Error loading more",
                                        color = Color.Red,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }

                        if (organizers.loadState.refresh is LoadState.Loading && organizers.itemCount == 0) {
                            items(5) {
                                OrganizerCardSkeleton()
                            }
                        }
                    }
                }
            }
        }

        // Background Dim when FAB is open
        if (showFabMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showFabMenu = false }
            )
        }

        // Expanded FAB Menu
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = showFabMenu,
                enter = fadeIn() + expandVertically() + slideInVertically { it / 2 },
                exit = fadeOut() + shrinkVertically() + slideOutVertically { it / 2 }
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Create Organizer Menu Item
                    FabMenuItem(
                        label = stringResource(R.string.organizer_menu_create),
                        onClick = {
                            showFabMenu = false
                            if (userProfile == null) {
                                showLoginDialog.value = true
                            } else {
                                onNavigateToCreate()
                            }
                        }
                    )

                    // My Organizers Menu Item
                    FabMenuItem(
                        label = stringResource(R.string.organizer_menu_my),
                        onClick = {
                            showFabMenu = false
                            if (userProfile == null) {
                                showLoginDialog.value = true
                            } else {
                                onNavigateToMyOrganizers()
                            }
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu },
                containerColor = Color.Transparent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LocalGradients.current.tertiary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = showFabMenu, label = "FabIcon") { isOpen ->
                        Icon(
                            imageVector = if (isOpen) Icons.Default.Close else Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizerCard(
    organizer: OrganizerData,
    currentUserUuid: String? = null,
    onToggleFollow: () -> Unit,
    onDelete: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = stringResource(R.string.delete_organizer_title)) },
            text = { Text(text = stringResource(R.string.delete_organizer_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog.value = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(text = stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile/Logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (!organizer.logo.isNullOrEmpty()) {
                    AsyncImage(
                        model = organizer.logo,
                        contentDescription = "Organizer Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = organizer.name.take(1),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = organizer.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.organizer_projects_rating, organizer.count?.eventOrganizers ?: 0, 4.5f), // Rating mock for now
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.home_stat_total_events) + ": ${organizer.count?.eventOrganizers ?: 0}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (currentUserUuid == organizer.userUuid) {
                IconButton(onClick = { showDeleteDialog.value = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Organizer",
                        tint = Color.Red
                    )
                }
            } else {
                GradientButton(
                    onClick = onToggleFollow,
                    modifier = Modifier.height(40.dp),
                    gradient = if (organizer.isFollow) Brush.verticalGradient(listOf(Color.LightGray, Color.Gray)) else LocalGradients.current.tertiary,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        if (organizer.isFollow) stringResource(R.string.btn_following) else stringResource(R.string.btn_follow),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrganizerPagePreview() {
    // Preview would need a mock of LazyPagingItems which is complex, 
    // so we just leave it for now or use a dummy content
}
