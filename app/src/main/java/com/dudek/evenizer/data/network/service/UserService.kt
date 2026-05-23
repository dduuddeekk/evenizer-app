package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.RegisterRequest
import com.dudek.evenizer.data.network.model.UserListResponse
import com.dudek.evenizer.data.network.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @POST("user/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @Multipart
    @PATCH("user/me/profile")
    suspend fun updateProfileImage(@Part file: MultipartBody.Part): UserResponse

    @GET("user")
    suspend fun getAllUsers(
        @Query("search") search: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): UserListResponse

    @GET("user/{uuid}")
    suspend fun getUserProfile(@Path("uuid") uuid: String): UserResponse
}
