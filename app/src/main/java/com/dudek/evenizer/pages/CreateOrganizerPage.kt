package com.dudek.evenizer.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dudek.evenizer.R
import com.dudek.evenizer.models.OrganizerViewModel

@Composable
fun CreateOrganizerPage(
    organizerViewModel: OrganizerViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by organizerViewModel.isLoading.collectAsState()

    CreateOrganizerPageContent(
        isLoading = isLoading,
        onBack = onBack,
        onRegister = { name, desc, isPublic, logoUri, scale, offset, containerSize -> 
            organizerViewModel.createOrganizerWithLogo(
                context, name, desc, isPublic, logoUri, scale, offset, containerSize
            ) {
                onSuccess()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrganizerPageContent(
    isLoading: Boolean,
    onBack: () -> Unit,
    onRegister: (String, String, Boolean, Uri?, Float, Offset, Float) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val isPublic = remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    var selectedLogoUri by remember { mutableStateOf<Uri?>(null) }
    var showCropDialog by remember { mutableStateOf<Uri?>(null) }
    var logoScale by remember { mutableFloatStateOf(1f) }
    var logoOffset by remember { mutableStateOf(Offset.Zero) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { showCropDialog = it }
    }

    if (showCropDialog != null) {
        var tempScale by remember { mutableFloatStateOf(1f) }
        var tempOffset by remember { mutableStateOf(Offset.Zero) }

        AlertDialog(
            onDismissRequest = { showCropDialog = null },
            title = { Text(text = stringResource(R.string.profile_update_title)) }, // Reuse string or add new
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.profile_update_desc))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
                        tempScale *= zoomChange
                        tempOffset += offsetChange
                    }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .transformable(state = state),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = showCropDialog,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = tempScale,
                                    scaleY = tempScale,
                                    translationX = tempOffset.x,
                                    translationY = tempOffset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedLogoUri = showCropDialog
                    logoScale = tempScale
                    logoOffset = tempOffset
                    showCropDialog = null
                }) {
                    Text(text = stringResource(R.string.btn_upload))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCropDialog = null }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    contentDescription = "Back",
                    tint = Color(0xFF2196F3)
                )
            }
            Text(
                text = stringResource(R.string.create_organizer_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Selection Area
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.1f))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedLogoUri != null) {
                    AsyncImage(
                        model = selectedLogoUri,
                        contentDescription = "Selected Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = logoScale,
                                scaleY = logoScale,
                                translationX = logoOffset.x,
                                translationY = logoOffset.y
                            ),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.create_organizer_add_logo),
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text(stringResource(R.string.create_organizer_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text(stringResource(R.string.create_organizer_field_desc)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // isPublic Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.create_organizer_public_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPublic.value) stringResource(R.string.create_organizer_public_desc) else stringResource(R.string.create_organizer_private_desc),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isPublic.value,
                    onCheckedChange = { isPublic.value = it },
                    enabled = !isLoading,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2196F3)
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    val containerSizePx = with(density) { 200.dp.toPx() }
                    onRegister(name.value, description.value, isPublic.value, selectedLogoUri, logoScale, logoOffset, containerSizePx) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                enabled = name.value.isNotBlank() && description.value.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(R.string.create_organizer_btn_create),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateOrganizerPagePreview() {
    CreateOrganizerPageContent(
        isLoading = false,
        onBack = {},
        onRegister = { _, _, _, _, _, _, _ -> }
    )
}
