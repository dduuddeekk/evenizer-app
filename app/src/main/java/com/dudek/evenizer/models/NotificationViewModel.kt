package com.dudek.evenizer.models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.NotificationData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications: StateFlow<List<NotificationData>> = _notifications

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
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (!isSilent) _isLoading.value = false
            }
        }
    }
    
    fun clearLatestNotification() {
        _latestNotification.value = null
    }
}
