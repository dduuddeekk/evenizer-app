package com.dudek.evenizer.models

import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.CreateOrganizerRequest
import com.dudek.evenizer.data.network.model.CreateMemberRequest
import com.dudek.evenizer.data.network.model.CreateRoleRequest
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.data.network.model.RoleData
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.utils.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
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

    private val _organizerRoles = MutableStateFlow<List<RoleData>>(emptyList())
    val organizerRoles: StateFlow<List<RoleData>> = _organizerRoles

    private val _organizerOwner = MutableStateFlow<UserData?>(null)
    val organizerOwner: StateFlow<UserData?> = _organizerOwner

    private val _allUsers = MutableStateFlow<List<UserData>>(emptyList())
    val allUsers: StateFlow<List<UserData>> = _allUsers

    private var pollingJob: kotlinx.coroutines.Job? = null

    fun startRealtimeOrganizers(context: Context, eventDescription: String? = null) {
        stopRealtimeOrganizers()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val service = NetworkModule.getOrganizerService(context)
                    val response = service.getAllOrganizers(eventDescription = eventDescription)
                    if (response.success) {
                        _organizers.value = response.data?.data ?: emptyList()
                    }
                } catch (_: Exception) {
                    // Silent
                }
                delay(10000)
            }
        }
    }

    fun addMultipleRoles(
        context: Context,
        organizerUuid: String,
        roles: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val service = NetworkModule.getOrganizerService(context)
                var allSuccess = true
                var lastErrorMessage = ""
                
                roles.forEach { (name, description) ->
                    if (name.isNotBlank()) {
                        val response = service.createRole(organizerUuid, CreateRoleRequest(name, description))
                        if (!response.success) {
                            allSuccess = false
                            lastErrorMessage = response.message
                        }
                    }
                }
                
                if (allSuccess) {
                    onSuccess()
                } else {
                    _error.value = "Some roles failed to save: $lastErrorMessage"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add roles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRole(
        context: Context,
        organizerUuid: String,
        roleUuid: String,
        name: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.updateRole(organizerUuid, roleUuid, CreateRoleRequest(name, description))
                if (response.success) {
                    onSuccess()
                    fetchOrganizerDetail(context, organizerUuid)
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to update role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRole(
        context: Context,
        organizerUuid: String,
        roleUuid: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.deleteRole(organizerUuid, roleUuid)
                if (response.success) {
                    onSuccess()
                    fetchOrganizerDetail(context, organizerUuid)
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun stopRealtimeOrganizers() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun fetchAllUsers(context: Context, search: String? = null) {
        viewModelScope.launch {
            try {
                val userService = NetworkModule.getUserService(context)
                val response = userService.getAllUsers(search = search, limit = 100)
                if (response.success && response.data != null) {
                    _allUsers.value = response.data.data
                } else {
                    _error.value = response.error ?: response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch users: ${e.message}"
            }
        }
    }

    fun addMultipleMembers(
        context: Context,
        organizerUuid: String,
        members: List<Pair<String, String>>, // Pair<UserUuid, RoleUuid>
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val service = NetworkModule.getOrganizerService(context)
                var allSuccess = true
                var lastErrorMessage = ""

                members.forEach { (userUuid, roleUuid) ->
                    if (userUuid.isNotBlank() && roleUuid.isNotBlank()) {
                        val response = service.addMember(organizerUuid, CreateMemberRequest(userUuid, roleUuid))
                        if (!response.success) {
                            allSuccess = false
                            lastErrorMessage = response.message
                        }
                    }
                }

                if (allSuccess) {
                    onSuccess()
                    fetchOrganizerDetail(context, organizerUuid)
                } else {
                    _error.value = "Some members failed to invite: $lastErrorMessage"
                }
            } catch (e: Exception) {
                _error.value = "Failed to add members: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOrganizers(context: Context, search: String? = null, eventDescription: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.getAllOrganizers(search = search, eventDescription = eventDescription)
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
            _organizerRoles.value = emptyList()
            _organizerOwner.value = null
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.getOrganizerDetail(uuid)
                if (response.success && response.data != null) {
                    _organizerDetail.value = response.data
                    _organizerRoles.value = response.data.roles ?: emptyList()
                    
                    // Fetch owner profile
                    response.data.userUuid?.let { userUuid ->
                        fetchOwnerProfile(context, userUuid)
                    }

                    // IF roles are still empty, double check with the explicit endpoint
                    if (_organizerRoles.value.isEmpty()) {
                        fetchRolesExplicitly(context, uuid)
                    }
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

    private suspend fun fetchOwnerProfile(context: Context, userUuid: String) {
        try {
            val userService = NetworkModule.getUserService(context)
            val response = userService.getUserProfile(userUuid)
            if (response.success) {
                _organizerOwner.value = response.data
            }
        } catch (_: Exception) {
            // Silent
        }
    }

    private suspend fun fetchRolesExplicitly(context: Context, uuid: String) {
        try {
            val service = NetworkModule.getOrganizerService(context)
            val response = service.getRoles(uuid)
            if (response.success) {
                _organizerRoles.value = response.data
            }
        } catch (_: Exception) {
            // Silent
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
