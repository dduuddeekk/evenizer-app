package com.dudek.evenizer.models

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeViewModel(context: Context) : ViewModel() {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val languageKey = stringPreferencesKey("language")
    
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[darkModeKey] ?: false
        }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[languageKey] ?: "id" // Default to Indonesian
        }

    fun toggleDarkMode(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                val current = preferences[darkModeKey] ?: false
                preferences[darkModeKey] = !current
            }
        }
    }

    fun setLanguage(context: Context, languageCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[languageKey] = languageCode
            }
            withContext(Dispatchers.Main) {
                updateLocale(context, languageCode)
            }
        }
    }

    fun updateLocale(context: Context, languageCode: String) {
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
