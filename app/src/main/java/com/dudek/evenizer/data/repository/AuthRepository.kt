package com.dudek.evenizer.data.repository

import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.model.LoginRequest
import com.dudek.evenizer.data.network.model.LoginResponse
import com.dudek.evenizer.data.network.service.AuthService

class AuthRepository(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {
    suspend fun login(identifier: String, password: String): Result<LoginResponse> {
        return try {
            val response = authService.login(LoginRequest(identifier, password))
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
            val response = authService.logout()
            if (response.success) {
                tokenManager.clearTokens()
                Result.success(Unit)
            } else {
                val errorMsg = response.error ?: response.message
                Result.failure(Exception(errorMsg))
            }
        } catch (_: Exception) {
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }
}
