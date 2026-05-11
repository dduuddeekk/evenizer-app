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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _createStep = MutableStateFlow(CreateEventStep.IDLE)
    val createStep: StateFlow<CreateEventStep> = _createStep

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

    fun createEvent(
        context: Context,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        categories: List<String>,
        location: String,
        locationType: String,
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
                    status = "DRAFT",
                    isPublic = isPublic,
                    description = description,
                    categories = categories,
                    locations = listOf(EventLocationRequest(locationType, location))
                )
                
                val response = eventService.createEvent(request)
                
                if (response.success && response.data != null) {
                    if (bannerUri != null) {
                        _createStep.value = CreateEventStep.UPLOADING_BANNER
                        uploadBanner(context, response.data.uuid, bannerUri)
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

    private suspend fun uploadBanner(context: Context, uuid: String, bannerUri: Uri) {
        try {
            val eventService = NetworkModule.getEventService(context)
            val file = getFileFromUri(context, bannerUri)
            
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = eventService.uploadBanner(uuid, body)
                if (response.success) {
                    _createStep.value = CreateEventStep.SUCCESS
                } else {
                    _error.value = response.message
                    _createStep.value = CreateEventStep.ERROR
                }
            }
        } catch (e: Exception) {
            _error.value = "Failed to upload banner: ${e.message}"
            _createStep.value = CreateEventStep.ERROR
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_banner_${System.currentTimeMillis()}.jpg")
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
}
