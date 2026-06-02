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
import com.dudek.evenizer.models.OrganizerViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.GradientFAB
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients

@Composable
fun CreateOrganizerRolesPage(
    organizerUuid: String,
    organizerViewModel: OrganizerViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by organizerViewModel.isLoading.collectAsState()
    val error by organizerViewModel.error.collectAsState()
    
    // List of (Name, Description) pairs
    var roles by remember { mutableStateOf(listOf(Pair("", ""))) }

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
                            text = stringResource(R.string.role_add_title),
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
                        itemsIndexed(roles) { index, role ->
                            RoleInputCard(
                                name = role.first,
                                description = role.second,
                                onNameChange = { newName ->
                                    val newList = roles.toMutableList()
                                    newList[index] = Pair(newName, role.second)
                                    roles = newList
                                },
                                onDescriptionChange = { newDesc ->
                                    val newList = roles.toMutableList()
                                    newList[index] = Pair(role.first, newDesc)
                                    roles = newList
                                },
                                onDelete = if (roles.size > 1) {
                                    {
                                        val newList = roles.toMutableList()
                                        newList.removeAt(index)
                                        roles = newList
                                    }
                                } else null
                            )
                        }
                    }

                    val successMsg = stringResource(R.string.role_save_success)
                    GradientButton(
                        onClick = {
                            organizerViewModel.addMultipleRoles(context, organizerUuid, roles) {
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        gradient = LocalGradients.current.tertiary,
                        enabled = !isLoading && roles.any { it.first.isNotBlank() }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(text = stringResource(R.string.role_save_all), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FAB to add more roles, styled like OrganizerDetailPage
            GradientFAB(
                onClick = { roles = roles + Pair("", "") },
                gradient = LocalGradients.current.tertiary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 16.dp) // Adjusted to not cover the save button
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.menu_add_role))
            }
        }
    }
}

@Composable
fun RoleInputCard(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDelete: (() -> Unit)?
) {
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
                Text(text = stringResource(R.string.role_input_detail_title), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.role_input_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.role_input_desc_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
