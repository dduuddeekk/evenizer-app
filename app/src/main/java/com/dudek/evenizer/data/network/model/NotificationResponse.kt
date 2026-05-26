package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val statusCode: Int,
    val message: String,
    val data: NotificationData? = null
)
