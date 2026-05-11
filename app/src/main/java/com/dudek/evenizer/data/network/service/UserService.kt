package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("user/{uuid}")
    suspend fun getUserProfile(@Path("uuid") uuid: String): UserResponse
}
