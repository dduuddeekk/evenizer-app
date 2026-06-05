package com.dudek.evenizer.models

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dudek.evenizer.data.local.dao.ReviewWithMedias
import com.dudek.evenizer.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: ReviewRepository) : ViewModel() {

    private val _reviews = MutableStateFlow<List<ReviewWithMedias>>(emptyList())
    val reviews: StateFlow<List<ReviewWithMedias>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchReviews(organizerUuid: String) {
        viewModelScope.launch {
            repository.getReviews(organizerUuid).collectLatest {
                _reviews.value = it
            }
        }
        refreshReviews(organizerUuid)
    }

    fun refreshReviews(organizerUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshReviews(organizerUuid)
            _isLoading.value = false
        }
    }

    fun createReview(
        context: Context,
        organizerUuid: String,
        rating: Int,
        comment: String?,
        mediaUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createReview(context, organizerUuid, rating, comment, mediaUris)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
                refreshReviews(organizerUuid)
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun updateReview(
        context: Context,
        reviewUuid: String,
        organizerUuid: String,
        rating: Int?,
        comment: String?,
        mediaUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateReview(context, reviewUuid, organizerUuid, rating, comment, mediaUris)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
                refreshReviews(organizerUuid)
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun deleteReview(reviewUuid: String, organizerUuid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteReview(reviewUuid)
            _isLoading.value = false
            result.onSuccess {
                refreshReviews(organizerUuid)
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
