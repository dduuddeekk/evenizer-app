package com.dudek.evenizer.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.models.ThemeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val context = LocalContext.current
    val isDarkMode by themeViewModel.isDarkMode.collectAsState(initial = false)
    val language by themeViewModel.language.collectAsState(initial = "id")
    
    var expanded by remember { mutableStateOf(false) }
    
    val isRefreshing = remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing.value,
        onRefresh = {
            scope.launch {
                isRefreshing.value = true
                delay(2000)
                isRefreshing.value = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
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
                        tint = Color(0xFFF44336)
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            Column(modifier = Modifier.padding(24.dp)) {
                // Dark Mode Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dark_mode),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDarkMode) {
                                stringResource(R.string.settings_switch_to_light)
                            } else {
                                stringResource(R.string.settings_switch_to_dark)
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { themeViewModel.toggleDarkMode(context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF44336),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.3f)
                )

                // Language Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val currentLangName = when(language) {
                            "id" -> stringResource(R.string.lang_id)
                            "en" -> stringResource(R.string.lang_en)
                            "zh" -> stringResource(R.string.lang_zh)
                            "ru" -> stringResource(R.string.lang_ru)
                            "es" -> stringResource(R.string.lang_es)
                            else -> stringResource(R.string.lang_id)
                        }
                        Text(
                            text = currentLangName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_id)) },
                                onClick = {
                                    themeViewModel.setLanguage(context, "id")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_en)) },
                                onClick = {
                                    themeViewModel.setLanguage(context, "en")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_zh)) },
                                onClick = {
                                    themeViewModel.setLanguage(context, "zh")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_ru)) },
                                onClick = {
                                    themeViewModel.setLanguage(context, "ru")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.lang_es)) },
                                onClick = {
                                    themeViewModel.setLanguage(context, "es")
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
