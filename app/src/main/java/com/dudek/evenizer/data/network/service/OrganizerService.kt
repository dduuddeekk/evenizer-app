package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.CreateOrganizerRequest
import com.dudek.evenizer.data.network.model.OrganizerListResponse
import com.dudek.evenizer.data.network.model.OrganizerResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface OrganizerService {
    @GET("organizer")
    suspend fun getAllOrganizers(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("isVerified") isVerified: Boolean? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc"
    ): OrganizerListResponse

    @POST("organizer")
    suspend fun createOrganizer(@Body request: CreateOrganizerRequest): OrganizerResponse

    @GET("organizer/my-organizer")
    suspend fun getMyOrganizers(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc"
    ): OrganizerListResponse

    @GET("organizer/{uuid}")
    suspend fun getOrganizerDetail(@Path("uuid") uuid: String): OrganizerResponse

    @Multipart
    @PATCH("organizer/{uuid}/logo")
    suspend fun uploadLogo(
        @Path("uuid") uuid: String,
        @Part file: MultipartBody.Part
    ): OrganizerResponse

    @DELETE("organizer/{uuid}")
    suspend fun deleteOrganizer(@Path("uuid") uuid: String): OrganizerResponse

    @POST("organizer/{uuid}/follow")
    suspend fun followOrganizer(@Path("uuid") uuid: String): OrganizerResponse

    @DELETE("organizer/{uuid}/follow")
    suspend fun unfollowOrganizer(@Path("uuid") uuid: String): OrganizerResponse
}
