package com.dudek.evenizer.models

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.CreateEventRequest
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.network.model.EventLocationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

enum class CreateEventStep {
    IDLE, CREATING_EVENT, UPLOADING_BANNER, SUCCESS, ERROR
}

class EventViewModel : ViewModel() {
    private val _events = MutableStateFlow<List<EventData>>(emptyList())
    val events: StateFlow<List<EventData>> = _events

    private val _myEvents = MutableStateFlow<List<EventData>>(emptyList())
    val myEvents: StateFlow<List<EventData>> = _myEvents

    private val _eventDetail = MutableStateFlow<EventData?>(null)
    val eventDetail: StateFlow<EventData?> = _eventDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _createStep = MutableStateFlow(CreateEventStep.IDLE)
    val createStep: StateFlow<CreateEventStep> = _createStep

    private val _isFavourited = MutableStateFlow(false)
    val isFavourited: StateFlow<Boolean> = _isFavourited

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchEvents(context: Context, search: String? = null, category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getEventService(context)
                val response = service.getAllEvents(search = search, category = category)
                if (response.success) {
                    _events.value = response.data?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyEvents(context: Context, search: String? = null, category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getEventService(context)
                val response = service.getMyEvents(search = search, category = category)
                if (response.success) {
                    _myEvents.value = response.data?.data ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch your events: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchEventDetail(context: Context, uuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _eventDetail.value = null
            try {
                val service = NetworkModule.getEventService(context)
                val response = service.getEventDetail(uuid)
                if (response.success) {
                    _eventDetail.value = response.data
                    // Assuming we track favorite status locally or API provides it
                    // For now let's say it's based on user data which we'll implement later
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch event details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavourite(context: Context, uuid: String) {
        viewModelScope.launch {
            try {
                val service = NetworkModule.getEventService(context)
                if (_isFavourited.value) {
                    val response = service.unfavouriteEvent(uuid)
                    if (response.success) _isFavourited.value = false
                } else {
                    val response = service.favouriteEvent(uuid)
                    if (response.success) _isFavourited.value = true
                }
            } catch (e: Exception) {
                _error.value = "Favourite action failed: ${e.message}"
            }
        }
    }

    fun createEvent(
        context: Context,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        categories: List<String>,
        location: String,
        locationType: String,
        status: String,
        isPublic: Boolean,
        bannerUri: Uri?
    ) {
        viewModelScope.launch {
            _createStep.value = CreateEventStep.CREATING_EVENT
            _error.value = null
            
            try {
                val eventService = NetworkModule.getEventService(context)
                
                val request = CreateEventRequest(
                    title = title,
                    start = startTime,
                    end = endTime,
                    status = status,
                    isPublic = isPublic,
                    description = description,
                    categories = categories,
                    locations = listOf(EventLocationRequest(locationType, location))
                )
                
                val response = eventService.createEvent(request)
                
                if (response.success && response.data != null) {
                    val eventUuid = response.data.uuid
                    if (bannerUri != null) {
                        _createStep.value = CreateEventStep.UPLOADING_BANNER
                        val uploadSuccess = uploadBanner(context, eventUuid, bannerUri)
                        if (uploadSuccess) {
                            _createStep.value = CreateEventStep.SUCCESS
                        } else {
                            // Cleanup: Delete the event if banner upload failed
                            deleteEventSilently(context, eventUuid)
                            _createStep.value = CreateEventStep.ERROR
                        }
                    } else {
                        _createStep.value = CreateEventStep.SUCCESS
                    }
                } else {
                    _error.value = response.message
                    _createStep.value = CreateEventStep.ERROR
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                _createStep.value = CreateEventStep.ERROR
            }
        }
    }

    private suspend fun uploadBanner(context: Context, uuid: String, bannerUri: Uri): Boolean {
        return try {
            val eventService = NetworkModule.getEventService(context)
            val mimeType = context.contentResolver.getType(bannerUri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            
            val file = getFileFromUri(context, bannerUri, extension)
            
            if (file != null) {
                val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = eventService.uploadBanner(uuid, body)
                if (response.success) {
                    true
                } else {
                    _error.value = response.message
                    false
                }
            } else {
                _error.value = "Could not process image file"
                false
            }
        } catch (e: Exception) {
            _error.value = "Failed to upload banner: ${e.message}"
            false
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri, extension: String): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_banner_${System.currentTimeMillis()}.$extension")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return if (file.exists()) file else null
    }

    fun resetState() {
        _createStep.value = CreateEventStep.IDLE
        _error.value = null
    }

    fun deleteEvent(context: Context, uuid: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getEventService(context)
                val response = service.deleteEvent(uuid)
                if (response.success) {
                    // Update the lists
                    _myEvents.value = _myEvents.value.filter { it.uuid != uuid }
                    _events.value = _events.value.filter { it.uuid != uuid }
                    onSuccess()
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete event: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun deleteEventSilently(context: Context, uuid: String) {
        try {
            val service = NetworkModule.getEventService(context)
            service.deleteEvent(uuid)
        } catch (e: Exception) {
            // Log error but don't show to user as we are already showing a creation error
            e.printStackTrace()
        }
    }
}
