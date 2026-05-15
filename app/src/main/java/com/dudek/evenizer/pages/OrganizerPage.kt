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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.MockData
import com.dudek.evenizer.data.Organizer
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.utils.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrganizerPage(
    themeViewModel: ThemeViewModel,
    userViewModel: UserViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyOrganizers: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val language by themeViewModel.language.collectAsState(initial = "id")
    val userProfile by userViewModel.userProfile.collectAsState()

    OrganizerPageContent(
        language = language,
        userProfile = userProfile,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToMyOrganizers = onNavigateToMyOrganizers,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerPageContent(
    language: String,
    userProfile: UserData?,
    onNavigateToCreate: () -> Unit,
    onNavigateToMyOrganizers: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val selectedDate = remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    
    val isRefreshing = remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()
    var showFabMenu by remember { mutableStateOf(false) }
    val showLoginDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing.value,
            onRefresh = {
                scope.launch {
                    isRefreshing.value = true
                    selectedDate.value = ""
                    delay(2000) // Simulate data reload
                    isRefreshing.value = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val datePickerState = rememberDatePickerState()

            val availableOrganizers = remember(selectedDate.value) {
                MockData.organizers.filter {
                    selectedDate.value.isEmpty() || it.availableDates.contains(selectedDate.value)
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
                            Text(text = stringResource(R.string.btn_ok), color = Color(0xFF2196F3))
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
                            Text(stringResource(R.string.btn_go_to_login), color = Color(0xFF2196F3))
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
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.nav_organizer),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
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
                        tint = Color(0xFF2196F3),
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
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(availableOrganizers) { organizer ->
                        OrganizerCard(organizer, language)
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
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
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

@Composable
fun OrganizerCard(organizer: Organizer, languageCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            // Placeholder for Profile/Logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(organizer.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = organizer.name.take(1),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
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
                        text = stringResource(R.string.organizer_projects_rating, organizer.projectsCompleted, organizer.rating),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val formattedDates = organizer.availableDates.joinToString(", ") { 
                    DateUtils.formatLocaleDate(it, languageCode) 
                }
                Text(
                    text = stringResource(R.string.organizer_available, formattedDates),
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3)
                )
            }

            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.btn_hire))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrganizerPagePreview() {
    OrganizerPageContent(
        language = "id",
        userProfile = null,
        onNavigateToCreate = {},
        onNavigateToMyOrganizers = {},
        onNavigateToLogin = {}
    )
}
