package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.NotificationListResponse
import com.dudek.evenizer.data.network.model.NotificationResponse
import retrofit2.http.*

interface NotificationService {
    @GET("notification")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): NotificationListResponse

    @GET("notification/{uuid}")
    suspend fun getNotificationDetail(@Path("uuid") uuid: String): NotificationResponse

    @PATCH("notification/{uuid}/read")
    suspend fun markAsRead(@Path("uuid") uuid: String): NotificationResponse

    @PATCH("notification/read-all")
    suspend fun markAllAsRead(): NotificationResponse
}
