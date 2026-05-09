package com.dudek.evenizer.data.repository

import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.service.ApiService
import com.dudek.evenizer.data.network.model.LoginRequest
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.data.network.response.LoginResponse
import com.dudek.evenizer.utils.JwtUtils

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(identifier: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(identifier, password))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(
                    accessToken = response.data.accessToken,
                    refreshToken = response.data.refreshToken
                )
                Result.success(response)
            } else {
                val errorMsg = response.error ?: response.message
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = apiService.logout()
            if (response.success) {
                tokenManager.clearTokens()
                Result.success(Unit)
            } else {
                val errorMsg = response.error ?: response.message
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Even if logout fails on server, we should probably clear local tokens?
            // User requested to logout, so we clear them.
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }

    suspend fun getUserProfile(): Result<UserData> {
        return try {
            val token = tokenManager.getAccessTokenBlocking() ?: return Result.failure(Exception("No token"))
            val uuid = JwtUtils.getSubFromToken(token) ?: return Result.failure(Exception("Invalid token"))
            
            val response = apiService.getUserProfile(uuid)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                val errorMsg = response.error ?: response.message
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAccessToken() = tokenManager.accessToken
}
