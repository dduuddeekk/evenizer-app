package com.dudek.evenizer.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
import com.dudek.evenizer.data.network.model.RoleData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.GradientFAB
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients

@Composable
fun AddOrganizerMemberPage(
    organizerUuid: String,
    organizerViewModel: OrganizerViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by organizerViewModel.isLoading.collectAsState()
    val allUsers by organizerViewModel.allUsers.collectAsState()
    val availableRoles by organizerViewModel.organizerRoles.collectAsState()
    val error by organizerViewModel.error.collectAsState()

    // List of (SelectedUser, SelectedRole)
    var memberEntries by remember { mutableStateOf(listOf<Pair<UserData?, RoleData?>>(Pair(null, null))) }

    LaunchedEffect(Unit) {
        organizerViewModel.fetchAllUsers(context)
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF2196F3))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ModernBackground {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header (Identical to OrganizerDetailPage)
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
                            text = stringResource(R.string.member_add_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 24.dp)
                    ) {
                        itemsIndexed(memberEntries) { index, entry ->
                            MemberInputCard(
                                selectedUser = entry.first,
                                selectedRole = entry.second,
                                allUsers = allUsers,
                                availableRoles = availableRoles,
                                onUserSelected = { user ->
                                    val newList = memberEntries.toMutableList()
                                    newList[index] = Pair(user, entry.second)
                                    memberEntries = newList
                                },
                                onRoleSelected = { role ->
                                    val newList = memberEntries.toMutableList()
                                    newList[index] = Pair(entry.first, role)
                                    memberEntries = newList
                                },
                                onDelete = if (memberEntries.size > 1) {
                                    {
                                        val newList = memberEntries.toMutableList()
                                        newList.removeAt(index)
                                        memberEntries = newList
                                    }
                                } else null
                            )
                        }
                    }

                    val successMsg = stringResource(R.string.member_invite_success)
                    GradientButton(
                        onClick = {
                            val finalMembers = memberEntries.filter { it.first != null && it.second != null }
                                .map { it.first!!.uuid to it.second!!.uuid }

                            organizerViewModel.addMultipleMembers(context, organizerUuid, finalMembers) {
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        gradient = LocalGradients.current.tertiary,
                        enabled = !isLoading && memberEntries.any { it.first != null && it.second != null }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = stringResource(R.string.member_invite_all_btn), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FAB to add more entries, styled like OrganizerDetailPage
            GradientFAB(
                onClick = { memberEntries = memberEntries + Pair(null, null) },
                gradient = LocalGradients.current.tertiary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.member_add_title))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberInputCard(
    selectedUser: UserData?,
    selectedRole: RoleData?,
    allUsers: List<UserData>,
    availableRoles: List<RoleData>,
    onUserSelected: (UserData) -> Unit,
    onRoleSelected: (RoleData) -> Unit,
    onDelete: (() -> Unit)?
) {
    var userSearchQuery by remember { mutableStateOf("") }
    var userDropdownExpanded by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val filteredUsers = remember(userSearchQuery, allUsers) {
        allUsers.filter {
            it.username?.contains(userSearchQuery, ignoreCase = true) == true ||
            it.firstName?.contains(userSearchQuery, ignoreCase = true) == true ||
            it.lastName?.contains(userSearchQuery, ignoreCase = true) == true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                Text(text = stringResource(R.string.member_input_select_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Selection
            ExposedDropdownMenuBox(
                expanded = userDropdownExpanded,
                onExpandedChange = { userDropdownExpanded = !userDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = if (selectedUser != null) "${selectedUser.firstName} ${selectedUser.lastName ?: ""} (@${selectedUser.username})" else userSearchQuery,
                    onValueChange = { 
                        userSearchQuery = it
                        userDropdownExpanded = true
                    },
                    label = { Text(stringResource(R.string.member_search_user_label)) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                ExposedDropdownMenu(
                    expanded = userDropdownExpanded,
                    onDismissRequest = { userDropdownExpanded = false }
                ) {
                    filteredUsers.take(10).forEach { user ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text("${user.firstName} ${user.lastName ?: ""}", fontWeight = FontWeight.Medium)
                                    Text("@${user.username}", fontSize = 12.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onUserSelected(user)
                                userSearchQuery = ""
                                userDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Role Selection
            ExposedDropdownMenuBox(
                expanded = roleDropdownExpanded,
                onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRole?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.member_select_role_label)) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                ExposedDropdownMenu(
                    expanded = roleDropdownExpanded,
                    onDismissRequest = { roleDropdownExpanded = false }
                ) {
                    availableRoles.forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.name) },
                            onClick = {
                                onRoleSelected(role)
                                roleDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
