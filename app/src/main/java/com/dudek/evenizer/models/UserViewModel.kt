package com.dudek.evenizer.models

import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.data.repository.UserRepository
import com.dudek.evenizer.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserData?>(null)
    val userProfile: StateFlow<UserData?> = _userProfile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    private val _uploadLoading = MutableStateFlow(false)
    val uploadLoading: StateFlow<Boolean> = _uploadLoading.asStateFlow()

    fun updateProfileImage(uri: Uri, context: Context, scale: Float = 1f, offset: Offset = Offset.Zero, containerSizePx: Float = 0f) {
        viewModelScope.launch {
            _uploadLoading.value = true
            
            val finalUri = if (containerSizePx > 0) {
                ImageUtils.cropImage(context, uri, scale, offset, containerSizePx) ?: uri
            } else {
                uri
            }

            val result = userRepository.updateProfileImage(finalUri, context)
            result.onSuccess {
                delay(2000) // Ensure a "hard refresh" feel for uploads
                fetchProfile() // Refresh profile after upload
            }
            _uploadLoading.value = false
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String, onAutoLogin: () -> Unit) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = userRepository.register(firstName, lastName, email, password)
            result.onSuccess {
                _registerState.value = RegisterState.Success
                onAutoLogin()
            }.onFailure { error ->
                _registerState.value = RegisterState.Error(error.message ?: "Unknown error")
            }
        }
    }

    suspend fun fetchProfile() {
        _profileLoading.value = true
        val result = userRepository.getUserProfile()
        result.onSuccess { profile ->
            _userProfile.value = profile
        }.onFailure {
            _userProfile.value = null
        }
        _profileLoading.value = false
    }

    fun clearProfile() {
        _userProfile.value = null
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
