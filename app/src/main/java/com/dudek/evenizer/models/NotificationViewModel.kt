package com.dudek.evenizer.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.NotificationData
import com.dudek.evenizer.utils.notifications.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications

    private val _notificationDetail = MutableStateFlow<NotificationData?>(null)
    val notificationDetail: StateFlow<NotificationData?> = _notificationDetail

    private val _latestNotification = MutableStateFlow<NotificationData?>(null)
    val latestNotification: StateFlow<NotificationData?> = _latestNotification

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var pollingJob: kotlinx.coroutines.Job? = null
    private var lastNotifiedUuid: String? = null

    fun startPolling(context: Context) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchNotifications(context, isSilent = true)
                delay(15000) // Poll every 15 seconds
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun fetchNotifications(context: Context, isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) _isLoading.value = true
            try {
                val service = NetworkModule.getNotificationService(context)
                val response = service.getNotifications()
                if (response.statusCode == 200 && response.data != null) {
                    val newList = response.data.data
                    _notifications.value = newList
                    
                    // Trigger "pop up" logic for the newest unread notification
                    val newest = newList.firstOrNull()
                    if (newest != null && !newest.isRead && newest.uuid != lastNotifiedUuid) {
                        _latestNotification.value = newest
                        lastNotifiedUuid = newest.uuid
                        
                        // Show system notification
                        NotificationHelper.showAppUpdate(
                            context = context,
                            id = newest.uuid.hashCode(),
                            title = newest.title,
                            message = newest.message
                        )
                    }
                }
            } catch (_: Exception) {
                // Silent
            } finally {
                if (!isSilent) _isLoading.value = false
            }
        }
    }

    fun fetchNotificationDetail(context: Context, uuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _notificationDetail.value = null
            try {
                val service = NetworkModule.getNotificationService(context)
                val response = service.getNotificationDetail(uuid)
                if (response.statusCode == 200) {
                    _notificationDetail.value = response.data
                    // Mark as read in the main list if updated locally
                    _notifications.value = _notifications.value.map {
                        if (it.uuid == uuid) it.copy(isRead = true) else it
                    }
                }
            } catch (_: Exception) {
                // Silent
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearLatestNotification() {
        _latestNotification.value = null
    }

    fun markAsRead(context: Context, uuid: String) {
        viewModelScope.launch {
            try {
                val service = NetworkModule.getNotificationService(context)
                val response = service.markAsRead(uuid)
                if (response.statusCode == 200) {
                    _notifications.value = _notifications.value.map {
                        if (it.uuid == uuid) it.copy(isRead = true) else it
                    }
                }
            } catch (_: Exception) {
                // Silent
            }
        }
    }

    fun markAllAsRead(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getNotificationService(context)
                val response = service.markAllAsRead()
                if (response.statusCode == 200) {
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                }
            } catch (_: Exception) {
                // Silent
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun respondToOrganizerRequest(
        context: Context,
        eventUuid: String,
        organizerUuid: String,
        status: String,
        rejectReason: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val eventService = NetworkModule.getEventService(context)
                val request = com.dudek.evenizer.data.network.model.RespondOrganizerRequest(status, rejectReason)
                val response = eventService.respondToOrganizerRequest(eventUuid, organizerUuid, request)
                if (response.statusCode == 200 || response.success == true) {
                    onSuccess()
                    fetchNotifications(context, isSilent = true)
                }
            } catch (_: Exception) {
                // Silent
            } finally {
                _isLoading.value = false
            }
        }
    }
}
