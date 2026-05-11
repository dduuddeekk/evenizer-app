package com.dudek.evenizer.data.network.authenticator

import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.model.RefreshRequest
import com.dudek.evenizer.data.network.service.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking {
            tokenManager.getRefreshTokenBlocking()
        } ?: return null

        synchronized(this) {
            val currentToken = runBlocking { tokenManager.getAccessTokenBlocking() }
            val authHeader = response.request.header("Authorization")

            // If the token has already been refreshed by another call, retry with new token
            if (authHeader != "Bearer $currentToken") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Otherwise, refresh the token
            val refreshResponse = authService.refreshTokens(RefreshRequest(refreshToken)).execute()

            if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                val newAccessToken = refreshResponse.body()?.data?.accessToken ?: return null
                runBlocking {
                    tokenManager.saveAccessToken(newAccessToken)
                }
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                runBlocking {
                    tokenManager.clearTokens()
                }
                return null
            }
        }
    }
}
