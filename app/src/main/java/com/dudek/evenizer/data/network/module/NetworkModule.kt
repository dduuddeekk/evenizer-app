package com.dudek.evenizer.data.network.module

import android.content.Context
import com.dudek.evenizer.BuildConfig
import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.interceptor.AuthInterceptor
import com.dudek.evenizer.data.network.authenticator.TokenAuthenticator
import com.dudek.evenizer.data.network.service.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = BuildConfig.BASE_URL

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private var apiServiceInstance: ApiService? = null
    private var tokenManagerInstance: TokenManager? = null

    fun getTokenManager(context: Context): TokenManager {
        return tokenManagerInstance ?: synchronized(this) {
            tokenManagerInstance ?: TokenManager(context.applicationContext).also { tokenManagerInstance = it }
        }
    }

    fun getApiService(context: Context): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: buildApiService(context).also { apiServiceInstance = it }
        }
    }

    private fun buildApiService(context: Context): ApiService {
        val tokenManager = getTokenManager(context)

        // We need a separate retrofit instance for authentication (to avoid circular dependency during refresh)
        // or just use the same one but be careful.
        // For refreshTokens, we'll use a simple retrofit instance without the interceptor/authenticator
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val authService = authRetrofit.create(ApiService::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager, authService))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    // Keep the old one for compatibility if needed, but it should ideally use the context-based one
    // Actually, since it's a singleton, we need to initialize it with context once.
    // Let's refactor this to be more robust.
}