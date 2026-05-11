package com.dudek.evenizer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.repository.AuthRepository
import com.dudek.evenizer.data.repository.UserRepository
import com.dudek.evenizer.models.AuthViewModel
import com.dudek.evenizer.models.EventViewModel
import com.dudek.evenizer.models.ThemeViewModel
import com.dudek.evenizer.models.UserViewModel
import com.dudek.evenizer.screens.MainScreen
import com.dudek.evenizer.screens.SplashScreen
import com.dudek.evenizer.ui.theme.EvenizerTheme
import kotlinx.coroutines.launch
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

            val authRepository = remember {
                AuthRepository(
                    NetworkModule.getAuthService(applicationContext),
                    NetworkModule.getTokenManager(applicationContext)
                )
            }

            val userRepository = remember {
                UserRepository(
                    NetworkModule.getUserService(applicationContext),
                    NetworkModule.getTokenManager(applicationContext)
                )
            }
            
            val authViewModel: AuthViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return AuthViewModel(authRepository) as T
                    }
                }
            )

            val userViewModel: UserViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return UserViewModel(userRepository) as T
                    }
                }
            )

            val eventViewModel: EventViewModel = viewModel()
            
            val isDarkMode by themeViewModel.isDarkMode.collectAsState(initial = false)
            val language by themeViewModel.language.collectAsState(initial = "id")
            
            applyLocale(this, language)

            var showSplash by remember { mutableStateOf(true) }
            var isAuthenticated by remember { mutableStateOf(false) }
            var currentAuthScreen by remember { mutableStateOf("login") } // "login" or "register"

            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                authViewModel.checkAuthStatus { loggedIn ->
                    if (loggedIn) {
                        scope.launch {
                            userViewModel.fetchProfile()
                        }
                        isAuthenticated = true
                    }
                }
            }
            
            EvenizerTheme(darkTheme = isDarkMode) {
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else if (!isAuthenticated) {
                    if (currentAuthScreen == "login") {
                        com.dudek.evenizer.screens.LoginScreen(
                            onLoginSuccess = { 
                                scope.launch {
                                    userViewModel.fetchProfile()
                                }
                                isAuthenticated = true 
                            },
                            onNavigateToRegister = { currentAuthScreen = "register" },
                            onLoginAsGuest = { isAuthenticated = true },
                            authViewModel = authViewModel
                        )
                    } else {
                        com.dudek.evenizer.screens.RegisterScreen(
                            onRegisterSuccess = { isAuthenticated = true },
                            onNavigateToLogin = { currentAuthScreen = "login" },
                            authViewModel = authViewModel,
                            userViewModel = userViewModel
                        )
                    }
                } else {
                    MainScreen(
                        themeViewModel = themeViewModel,
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        eventViewModel = eventViewModel,
                        onNavigateToLogin = {
                            authViewModel.logout {
                                userViewModel.clearProfile()
                                isAuthenticated = false
                                currentAuthScreen = "login"
                            }
                        }
                    )
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
