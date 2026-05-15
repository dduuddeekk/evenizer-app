package com.dudek.evenizer.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.models.CreateEventStep
import com.dudek.evenizer.models.EventViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateEventPage(
    eventViewModel: EventViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val createStep by eventViewModel.createStep.collectAsState()
    val error by eventViewModel.error.collectAsState()

    CreateEventPageContent(
        createStep = createStep,
        error = error,
        onBack = onBack,
        onSuccess = onSuccess,
        onCreateEvent = { title, desc, start, end, cats, loc, locType, status, isPub, uri, context ->
            eventViewModel.createEvent(context, title, desc, start, end, cats, loc, locType, status, isPub, uri)
        },
        onResetState = { eventViewModel.resetState() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPageContent(
    createStep: CreateEventStep,
    error: String?,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onCreateEvent: (String, String, String, String, List<String>, String, String, String, Boolean, Uri?, android.content.Context) -> Unit,
    onResetState: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val categories = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    val locationType = remember { mutableStateOf("ONLINE") }
    val status = remember { mutableStateOf("DRAFT") }
    val isPublic = remember { mutableStateOf(true) }
    val bannerUri = remember { mutableStateOf<Uri?>(null) }

    val startCalendar = remember { mutableStateOf<Calendar?>(null) }
    val endCalendar = remember { mutableStateOf<Calendar?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        bannerUri.value = uri
    }

    if (createStep != CreateEventStep.IDLE) {
        CreateEventProgressDialog(
            step = createStep,
            error = error,
            onDismiss = {
                if (createStep == CreateEventStep.SUCCESS) {
                    onSuccess()
                }
                onResetState()
            }
        )
    }

    fun showDateTimePicker(onDateTimeSelected: (Calendar) -> Unit) {
        val current = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selected = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                        }
                        onDateTimeSelected(selected)
                    },
                    current.get(Calendar.HOUR_OF_DAY),
                    current.get(Calendar.MINUTE),
                    true
                ).show()
            },
            current.get(Calendar.YEAR),
            current.get(Calendar.MONTH),
            current.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val displayFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    contentDescription = "Back",
                    tint = Color(0xFF4CAF50)
                )
            }
            Text(
                text = stringResource(R.string.create_event_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Banner Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (bannerUri.value != null) {
                    AsyncImage(
                        model = bannerUri.value,
                        contentDescription = stringResource(R.string.create_event_banner_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.create_event_add_banner), color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            OutlinedTextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text(stringResource(R.string.create_event_field_title)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text(stringResource(R.string.create_event_field_desc)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Start & End Time
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = startCalendar.value?.let { displayFormatter.format(it.time) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.create_event_field_start)) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDateTimePicker { startCalendar.value = it } },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = endCalendar.value?.let { displayFormatter.format(it.time) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.create_event_field_end)) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDateTimePicker { endCalendar.value = it } },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            OutlinedTextField(
                value = categories.value,
                onValueChange = { categories.value = it },
                label = { Text(stringResource(R.string.create_event_field_categories)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location Type
            var expanded by remember { mutableStateOf(false) }
            val options = listOf("ONLINE", "OFFLINE", "HYBRID")
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = when(locationType.value) {
                        "ONLINE" -> stringResource(R.string.create_event_loc_online)
                        "OFFLINE" -> stringResource(R.string.create_event_loc_offline)
                        else -> stringResource(R.string.create_event_loc_hybrid)
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.create_event_field_location_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    when(option) {
                                        "ONLINE" -> stringResource(R.string.create_event_loc_online)
                                        "OFFLINE" -> stringResource(R.string.create_event_loc_offline)
                                        else -> stringResource(R.string.create_event_loc_hybrid)
                                    }
                                ) 
                            },
                            onClick = {
                                locationType.value = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Details
            OutlinedTextField(
                value = location.value,
                onValueChange = { location.value = it },
                label = { Text(stringResource(R.string.create_event_field_location)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Publication (isPublic)
            var pubExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = pubExpanded,
                onExpandedChange = { pubExpanded = !pubExpanded }
            ) {
                OutlinedTextField(
                    value = if (isPublic.value) stringResource(R.string.create_event_pub_public) else stringResource(R.string.create_event_pub_private),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.create_event_field_publication)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pubExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = pubExpanded,
                    onDismissRequest = { pubExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_event_pub_public)) },
                        onClick = {
                            isPublic.value = true
                            pubExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_event_pub_private)) },
                        onClick = {
                            isPublic.value = false
                            pubExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status (Draft/Publish)
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = if (status.value == "DRAFT") stringResource(R.string.create_event_status_draft) else stringResource(R.string.create_event_status_publish),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.create_event_field_status)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_event_status_draft)) },
                        onClick = {
                            status.value = "DRAFT"
                            statusExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_event_status_publish)) },
                        onClick = {
                            status.value = "UPCOMING"
                            statusExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val categoryList = categories.value.split("\n").filter { it.isNotBlank() }
                    val startIso = startCalendar.value?.let { isoFormatter.format(it.time) } ?: ""
                    val endIso = endCalendar.value?.let { isoFormatter.format(it.time) } ?: ""
                    
                    onCreateEvent(title.value, description.value, startIso, endIso, categoryList, location.value, locationType.value, status.value, isPublic.value, bannerUri.value, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                enabled = startCalendar.value != null && endCalendar.value != null && title.value.isNotBlank()
            ) {
                Text(stringResource(R.string.create_event_btn_create), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CreateEventProgressDialog(
    step: CreateEventStep,
    error: String?,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (step == CreateEventStep.ERROR || step == CreateEventStep.SUCCESS) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    CreateEventStep.CREATING_EVENT -> {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.create_event_progress_creating), fontWeight = FontWeight.Medium)
                    }
                    CreateEventStep.UPLOADING_BANNER -> {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.create_event_progress_uploading), fontWeight = FontWeight.Medium)
                    }
                    CreateEventStep.SUCCESS -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.create_event_progress_success), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        
                        LaunchedEffect(Unit) {
                            delay(1500)
                            onDismiss()
                        }
                    }
                    CreateEventStep.ERROR -> {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.create_event_progress_error), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error ?: "Unknown error", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.btn_ok))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEventPagePreview() {
    CreateEventPageContent(
        createStep = CreateEventStep.IDLE,
        error = null,
        onBack = {},
        onSuccess = {},
        onCreateEvent = { _, _, _, _, _, _, _, _, _, _, _ -> },
        onResetState = {}
    )
}
