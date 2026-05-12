package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.CreateEventRequest
import com.dudek.evenizer.data.network.model.EventListResponse
import com.dudek.evenizer.data.network.model.EventResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface EventService {
    @GET("event")
    suspend fun getAllEvents(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: Map<String, Boolean>? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("groupBy") groupBy: String? = null
    ): EventListResponse

    @POST("event")
    suspend fun createEvent(@Body request: CreateEventRequest): EventResponse

    @Multipart
    @PATCH("event/{uuid}/banner")
    suspend fun uploadBanner(
        @Path("uuid") uuid: String,
        @Part file: MultipartBody.Part
    ): EventResponse

    @GET("event/my-event")
    suspend fun getMyEvents(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: Map<String, Boolean>? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc",
        @Query("groupBy") groupBy: String? = null
    ): EventListResponse
}
