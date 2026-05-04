package com.dudek.evenizer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.screens.MainScreen
import com.dudek.evenizer.screens.SplashScreen
import com.dudek.evenizer.ui.theme.EvenizerTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ThemeViewModel(applicationContext) as T
                    }
                }
            )
            
            val isDarkMode by themeViewModel.isDarkMode.collectAsState(initial = false)
            val language by themeViewModel.language.collectAsState(initial = "id")
            
            applyLocale(this, language)

            var showSplash by remember { mutableStateOf(true) }
            
            EvenizerTheme(darkTheme = isDarkMode) {
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    MainScreen(themeViewModel = themeViewModel)
                }
            }
        }
    }

    private fun applyLocale(context: Context, languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
