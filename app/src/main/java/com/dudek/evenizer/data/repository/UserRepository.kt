package com.dudek.evenizer.data.repository

import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.model.RegisterRequest
import com.dudek.evenizer.data.network.model.UserData
import com.dudek.evenizer.data.network.model.UserResponse
import com.dudek.evenizer.data.network.service.UserService
import com.dudek.evenizer.utils.JwtUtils

class UserRepository(
    private val userService: UserService,
    private val tokenManager: TokenManager
) {
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<UserResponse> {
        return try {
            val response = userService.register(
                RegisterRequest(firstName, lastName, email, password)
            )
            if (response.success && response.data != null) {
                Result.success(response)
            } else {
                val errorMsg = response.error ?: response.message
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserData> {
        return try {
            val token = tokenManager.getAccessTokenBlocking() ?: return Result.failure(Exception("No token"))
            val uuid = JwtUtils.getSubFromToken(token) ?: return Result.failure(Exception("Invalid token"))
            
            val response = userService.getUserProfile(uuid)
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
}
