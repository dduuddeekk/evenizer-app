package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.LoginRequest
import com.dudek.evenizer.data.network.model.RefreshRequest
import com.dudek.evenizer.data.network.response.HealthResponse
import com.dudek.evenizer.data.network.response.LoginResponse
import com.dudek.evenizer.data.network.response.LogoutResponse
import com.dudek.evenizer.data.network.response.RefreshResponse
import com.dudek.evenizer.data.network.response.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    fun refreshTokens(@Body request: RefreshRequest): Call<RefreshResponse>

    @DELETE("auth/logout")
    suspend fun logout(): LogoutResponse

    @GET("user/{uuid}")
    suspend fun getUserProfile(@Path("uuid") uuid: String): UserResponse
}