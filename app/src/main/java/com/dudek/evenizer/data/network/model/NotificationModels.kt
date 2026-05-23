package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val uuid: String,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val userUuid: String? = null
)

@Serializable
data class NotificationListResponse(
    val statusCode: Int,
    val message: String,
    val data: NotificationListData? = null
)

@Serializable
data class NotificationListData(
    val data: List<NotificationData>,
    val meta: Meta
)
