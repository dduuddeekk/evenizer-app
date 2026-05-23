package com.dudek.evenizer.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.data.network.model.RoleData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.models.OrganizerViewModel

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        contentDescription = "Back",
                        tint = Color(0xFF2196F3)
                    )
                }
                Text(
                    text = "Tambah Anggota",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
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

            Button(
                onClick = {
                    val finalMembers = memberEntries.filter { it.first != null && it.second != null }
                        .map { it.first!!.uuid to it.second!!.uuid }
                    
                    organizerViewModel.addMultipleMembers(context, organizerUuid, finalMembers) {
                        Toast.makeText(context, "Anggota berhasil diundang", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && memberEntries.any { it.first != null && it.second != null },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Undang Semua Anggota", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // FAB to add more entries, styled like OrganizerDetailPage
        FloatingActionButton(
            onClick = { memberEntries = memberEntries + Pair(null, null) },
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Entry Anggota")
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
                Text(text = "Pilih Anggota", fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
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
                    label = { Text("Cari User (Username/Nama)") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userDropdownExpanded) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
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
                    label = { Text("Pilih Sie") },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) }
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
