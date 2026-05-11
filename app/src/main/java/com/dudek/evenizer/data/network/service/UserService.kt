package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.RegisterRequest
import com.dudek.evenizer.data.network.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @POST("user/register")
    suspend fun register(@Body request: RegisterRequest): UserResponse

    @GET("user/{uuid}")
    suspend fun getUserProfile(@Path("uuid") uuid: String): UserResponse
}
