package com.dudek.evenizer.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.ui.components.GradientButton
import com.dudek.evenizer.ui.components.ModernBackground
import com.dudek.evenizer.ui.theme.LocalGradients
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventRundownPage(
    eventUuid: String,
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val eventDetail by eventViewModel.eventDetail.collectAsState()
    val isLoading by eventViewModel.isLoading.collectAsState()
    val error by eventViewModel.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("DRAFT") }
    var visibility by remember { mutableStateOf("PUBLIC") }
    var selectedLocationUuid by remember { mutableStateOf<String?>(null) }

    val locations = eventDetail?.eventLocations ?: emptyList()
    var locationDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(primary = Color(0xFF4CAF50))
    ) {
        ModernBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                        text = stringResource(R.string.rundown_add_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.rundown_field_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Date Picker
                    OutlinedTextField(
                        value = date,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.rundown_field_date)) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = Calendar.getInstance().apply { set(y, m, d) }
                                        date = SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                            Locale.getDefault()
                                        ).format(cal.time)
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Time
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.rundown_field_start)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    TimePickerDialog(context, { _, h, min ->
                                        val time = String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d:00.000Z",
                                            h,
                                            min
                                        )
                                        val datePart = date.take(11).ifEmpty { "2026-05-17T" }
                                        startTime = datePart + time
                                    }, 0, 0, true).show()
                                }) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        // End Time
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.rundown_field_end)) },
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    TimePickerDialog(context, { _, h, min ->
                                        val time = String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d:00.000Z",
                                            h,
                                            min
                                        )
                                        val datePart = date.take(11).ifEmpty { "2026-05-17T" }
                                        endTime = datePart + time
                                    }, 0, 0, true).show()
                                }) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }

                    // Visibility Selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.rundown_field_visibility),
                            fontWeight = FontWeight.Bold
                        )
                        RadioButton(
                            selected = visibility == "PUBLIC",
                            onClick = { visibility = "PUBLIC" },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("PUBLIC")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(
                            selected = visibility == "PRIVATE",
                            onClick = { visibility = "PRIVATE" },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("PRIVATE")
                    }

                    // Location Dropdown
                    ExposedDropdownMenuBox(
                        expanded = locationDropdownExpanded,
                        onExpandedChange = { locationDropdownExpanded = !locationDropdownExpanded }
                    ) {
                        val selectedLocName =
                            locations.find { it.uuid == selectedLocationUuid }?.location
                                ?: stringResource(R.string.rundown_field_location)
                        OutlinedTextField(
                            value = selectedLocName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.rundown_field_location)) },
                            modifier = Modifier.menuAnchor(
                                ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                true
                            ).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationDropdownExpanded) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = locationDropdownExpanded,
                            onDismissRequest = { locationDropdownExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text("${loc.location} (${loc.type})") },
                                    onClick = {
                                        selectedLocationUuid = loc.uuid
                                        locationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.rundown_field_desc)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    val successMsg = stringResource(R.string.rundown_save_success)
                    GradientButton(
                        onClick = {
                            eventViewModel.createRundown(
                                context = context,
                                eventUuid = eventUuid,
                                title = title,
                                date = date,
                                start = startTime,
                                end = endTime,
                                status = status,
                                visibility = visibility,
                                description = description,
                                locationUuid = selectedLocationUuid
                            ) {
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                onSuccess()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && title.isNotBlank() && date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && selectedLocationUuid != null,
                        gradient = LocalGradients.current.secondary
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                stringResource(R.string.rundown_save_btn),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
