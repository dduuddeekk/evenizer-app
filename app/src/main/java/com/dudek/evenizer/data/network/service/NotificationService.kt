package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.NotificationListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NotificationService {
    @GET("notification")
    suspend fun getNotifications(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): NotificationListResponse
}
