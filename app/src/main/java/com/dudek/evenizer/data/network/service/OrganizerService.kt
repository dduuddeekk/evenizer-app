package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.CreateOrganizerRequest
import com.dudek.evenizer.data.network.model.CreateMemberRequest
import com.dudek.evenizer.data.network.model.CreateRoleRequest
import com.dudek.evenizer.data.network.model.OrganizerListResponse
import com.dudek.evenizer.data.network.model.OrganizerResponse
import com.dudek.evenizer.data.network.model.RoleListResponse
import com.dudek.evenizer.data.network.model.RoleResponse
import com.dudek.evenizer.data.network.model.MemberResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface OrganizerService {
    @GET("organizer")
    suspend fun getAllOrganizers(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("eventDescription") eventDescription: String? = null,
        @Query("isVerified") isVerified: Boolean? = null,
        @Query("isPublic") isPublic: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc"
    ): OrganizerListResponse

    @POST("organizer")
    suspend fun createOrganizer(@Body request: CreateOrganizerRequest): OrganizerResponse

    @POST("organizer/{uuid}/roles")
    suspend fun createRole(
        @Path("uuid") uuid: String,
        @Body request: CreateRoleRequest
    ): RoleResponse

    @GET("organizer/{uuid}/roles")
    suspend fun getRoles(@Path("uuid") uuid: String): RoleListResponse

    @PATCH("organizer/{uuid}/roles/{roleUuid}")
    suspend fun updateRole(
        @Path("uuid") uuid: String,
        @Path("roleUuid") roleUuid: String,
        @Body request: CreateRoleRequest
    ): RoleResponse

    @DELETE("organizer/{uuid}/roles/{roleUuid}")
    suspend fun deleteRole(
        @Path("uuid") uuid: String,
        @Path("roleUuid") roleUuid: String
    ): RoleResponse

    @GET("organizer/event/{eventUuid}")
    suspend fun getEventOrganizers(@Path("eventUuid") eventUuid: String): com.dudek.evenizer.data.network.model.EventOrganizerListResponse

    @POST("organizer/{uuid}/members")
    suspend fun addMember(
        @Path("uuid") uuid: String,
        @Body request: CreateMemberRequest
    ): MemberResponse

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
