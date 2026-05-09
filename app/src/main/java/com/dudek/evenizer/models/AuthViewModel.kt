package com.dudek.evenizer.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.repository.AuthRepository
import com.dudek.evenizer.data.network.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserData?>(null)
    val userProfile: StateFlow<UserData?> = _userProfile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repository.login(email, password)
            result.onSuccess {
                _loginState.value = LoginState.Success
                fetchProfile() // Fetch profile after successful login
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileLoading.value = true
            val result = repository.getUserProfile()
            result.onSuccess { profile ->
                _userProfile.value = profile
            }.onFailure {
                // If profile fetch fails, we might still be logged in, 
                // but let's clear profile data.
                _userProfile.value = null
            }
            _profileLoading.value = false
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _userProfile.value = null
            _loginState.value = LoginState.Idle
            onSuccess()
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
