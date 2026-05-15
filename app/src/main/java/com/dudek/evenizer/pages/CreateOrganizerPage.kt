package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R

@Composable
fun CreateOrganizerPage(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    CreateOrganizerPageContent(
        onBack = onBack,
        onRegister = { _, _, _ -> 
            // Static UI: Simulate success
            onSuccess()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrganizerPageContent(
    onBack: () -> Unit,
    onRegister: (String, String, String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val contact = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

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
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text(stringResource(R.string.create_organizer_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description.value,
                onValueChange = { description.value = it },
                label = { Text(stringResource(R.string.create_organizer_field_desc)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contact.value,
                onValueChange = { contact.value = it },
                label = { Text(stringResource(R.string.create_organizer_field_contact)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRegister(name.value, description.value, contact.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                enabled = name.value.isNotBlank() && contact.value.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.create_organizer_btn_create),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateOrganizerPagePreview() {
    CreateOrganizerPageContent(
        onBack = {},
        onRegister = { _, _, _ -> }
    )
}
