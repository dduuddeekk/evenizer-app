package com.dudek.evenizer.models

import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.CreateOrganizerRequest
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class OrganizerViewModel : ViewModel() {
    private val _organizers = MutableStateFlow<List<OrganizerData>>(emptyList())
    val organizers: StateFlow<List<OrganizerData>> = _organizers

    private val _myOrganizers = MutableStateFlow<List<OrganizerData>>(emptyList())
    val myOrganizers: StateFlow<List<OrganizerData>> = _myOrganizers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _organizerDetail = MutableStateFlow<OrganizerData?>(null)
    val organizerDetail: StateFlow<OrganizerData?> = _organizerDetail

    fun fetchOrganizers(context: Context, search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.getAllOrganizers(search = search)
                if (response.success) {
                    _organizers.value = response.data?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch organizers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteOrganizer(context: Context, uuid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.deleteOrganizer(uuid)
                if (response.success) {
                    _organizers.value = _organizers.value.filter { it.uuid != uuid }
                    _myOrganizers.value = _myOrganizers.value.filter { it.uuid != uuid }
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete organizer: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyOrganizers(context: Context, search: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.getMyOrganizers(search = search)
                if (response.success) {
                    _myOrganizers.value = response.data?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch your organizers: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createOrganizer(context: Context, name: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val request = CreateOrganizerRequest(name, description)
                val response = service.createOrganizer(request)
                if (response.success) {
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to register organizer: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createOrganizerWithLogo(
        context: Context,
        name: String,
        description: String,
        isPublic: Boolean,
        logoUri: Uri?,
        scale: Float,
        offset: Offset,
        containerSize: Float,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val request = CreateOrganizerRequest(name, description, isPublic)
                val response = service.createOrganizer(request)
                
                if (response.success && response.data != null) {
                    val organizerUuid = response.data.uuid
                    
                    if (logoUri != null) {
                        val croppedUri = ImageUtils.cropImage(context, logoUri, scale, offset, containerSize)
                        if (croppedUri != null) {
                            val file = File(croppedUri.path!!)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            
                            val uploadResponse = service.uploadLogo(organizerUuid, body)
                            if (uploadResponse.success) {
                                onSuccess()
                            } else {
                                _error.value = "Organizer created but logo upload failed: ${uploadResponse.message}"
                                onSuccess() // Still call onSuccess because organizer was created
                            }
                        } else {
                            _error.value = "Organizer created but image cropping failed"
                            onSuccess()
                        }
                    } else {
                        onSuccess()
                    }
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to create organizer: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOrganizerDetail(context: Context, uuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.getOrganizerDetail(uuid)
                if (response.success) {
                    _organizerDetail.value = response.data
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch organizer details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFollow(context: Context, uuid: String) {
        viewModelScope.launch {
            try {
                val service = NetworkModule.getOrganizerService(context)
                val organizer = _organizers.value.find { it.uuid == uuid } ?: return@launch
                
                val response = if (organizer.isFollow) {
                    service.unfollowOrganizer(uuid)
                } else {
                    service.followOrganizer(uuid)
                }

                if (response.success) {
                    val newState = !organizer.isFollow
                    _organizers.value = _organizers.value.map { 
                        if (it.uuid == uuid) it.copy(isFollow = newState) else it 
                    }
                }
            } catch (e: Exception) {
                _error.value = "Follow action failed: ${e.message}"
            }
        }
    }
}
