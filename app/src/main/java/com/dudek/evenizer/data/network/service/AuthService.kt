package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.LoginRequest
import com.dudek.evenizer.data.network.model.LoginResponse
import com.dudek.evenizer.data.network.model.LogoutResponse
import com.dudek.evenizer.data.network.model.RefreshRequest
import com.dudek.evenizer.data.network.model.RefreshResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    fun refreshTokens(@Body request: RefreshRequest): Call<RefreshResponse>

    @DELETE("auth/logout")
    suspend fun logout(): LogoutResponse
}
