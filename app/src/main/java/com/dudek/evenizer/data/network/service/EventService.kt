package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.CreateEventRequest
import com.dudek.evenizer.data.network.model.CreateRundownRequest
import com.dudek.evenizer.data.network.model.EventListResponse
import com.dudek.evenizer.data.network.model.EventResponse
import com.dudek.evenizer.data.network.model.FavouriteResponse
import com.dudek.evenizer.data.network.model.RundownListResponse
import com.dudek.evenizer.data.network.model.RundownResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface EventService {
    @GET("event")
    suspend fun getAllEvents(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String? = null,
        @Query("groupBy") groupBy: String? = null
    ): EventListResponse

    @POST("event")
    suspend fun createEvent(@Body request: CreateEventRequest): EventResponse

    @DELETE("event/{uuid}")
    suspend fun deleteEvent(@Path("uuid") uuid: String): com.dudek.evenizer.data.network.model.DeleteEventResponse

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
        @Query("status") status: String? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc",
        @Query("groupBy") groupBy: String? = null
    ): EventListResponse

    @GET("event/{uuid}")
    suspend fun getEventDetail(@Path("uuid") uuid: String): EventResponse

    @POST("event/{uuid}/favourite")
    suspend fun favouriteEvent(@Path("uuid") uuid: String): FavouriteResponse

    @DELETE("event/{uuid}/favourite")
    suspend fun unfavouriteEvent(@Path("uuid") uuid: String): FavouriteResponse

    @POST("event/{uuid}/organizers")
    suspend fun inviteOrganizer(
        @Path("uuid") uuid: String,
        @Body request: com.dudek.evenizer.data.network.model.InviteOrganizerRequest
    ): com.dudek.evenizer.data.network.model.EventOrganizerResponse

    @PATCH("event/{uuid}/organizers/{organizerUuid}/respond")
    suspend fun respondToOrganizerRequest(
        @Path("uuid") uuid: String,
        @Path("organizerUuid") organizerUuid: String,
        @Body request: com.dudek.evenizer.data.network.model.RespondOrganizerRequest
    ): com.dudek.evenizer.data.network.model.EventOrganizerResponse

    @GET("event/{uuid}/rundowns")
    suspend fun getEventRundowns(
        @Path("uuid") uuid: String,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("visibility") visibility: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "start",
        @Query("sortOrder") sortOrder: String? = "asc",
        @Query("groupBy") groupBy: String? = null
    ): RundownListResponse

    @POST("event/{uuid}/rundowns")
    suspend fun createRundown(
        @Path("uuid") uuid: String,
        @Body request: CreateRundownRequest
    ): RundownResponse
}
