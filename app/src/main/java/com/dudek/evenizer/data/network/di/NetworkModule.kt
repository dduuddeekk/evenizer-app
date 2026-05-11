package com.dudek.evenizer.data.network.di

import android.content.Context
import com.dudek.evenizer.BuildConfig
import com.dudek.evenizer.data.local.TokenManager
import com.dudek.evenizer.data.network.authenticator.TokenAuthenticator
import com.dudek.evenizer.data.network.interceptor.AuthInterceptor
import com.dudek.evenizer.data.network.service.ApiService
import com.dudek.evenizer.data.network.service.AuthService
import com.dudek.evenizer.data.network.service.EventService
import com.dudek.evenizer.data.network.service.UserService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = BuildConfig.BASE_URL

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    private var apiServiceInstance: ApiService? = null

    @Volatile
    private var authServiceInstance: AuthService? = null
    
    @Volatile
    private var userServiceInstance: UserService? = null

    @Volatile
    private var eventServiceInstance: EventService? = null
    
    @Volatile
    private var tokenManagerInstance: TokenManager? = null

    fun getTokenManager(context: Context): TokenManager {
        return tokenManagerInstance ?: synchronized(this) {
            tokenManagerInstance ?: TokenManager(context.applicationContext).also { tokenManagerInstance = it }
        }
    }

    fun getApiService(): ApiService {
        return apiServiceInstance ?: synchronized(this) {
            apiServiceInstance ?: buildApiService().also { apiServiceInstance = it }
        }
    }

    fun getAuthService(context: Context): AuthService {
        return authServiceInstance ?: synchronized(this) {
            authServiceInstance ?: buildAuthService(context.applicationContext).also { authServiceInstance = it }
        }
    }

    fun getUserService(context: Context): UserService {
        return userServiceInstance ?: synchronized(this) {
            userServiceInstance ?: buildUserService(context.applicationContext).also { userServiceInstance = it }
        }
    }

    fun getEventService(context: Context): EventService {
        return eventServiceInstance ?: synchronized(this) {
            eventServiceInstance ?: buildEventService(context.applicationContext).also { eventServiceInstance = it }
        }
    }

    private fun buildApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    private fun buildAuthService(context: Context): AuthService {
        val tokenManager = getTokenManager(context)

        // Separate retrofit for auth to avoid circular dependency in Authenticator
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val simpleAuthService = authRetrofit.create(AuthService::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenManager, simpleAuthService))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthService::class.java)
    }

    private fun buildUserService(context: Context): UserService {
        val tokenManager = getTokenManager(context)
        
        // We need an AuthService for the TokenAuthenticator
        // To avoid redundant builds, we can use the simpleAuthService pattern or just use the same client
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val simpleAuthService = authRetrofit.create(AuthService::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenManager, simpleAuthService))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserService::class.java)
    }

    private fun buildEventService(context: Context): EventService {
        val tokenManager = getTokenManager(context)
        
        val authRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val simpleAuthService = authRetrofit.create(AuthService::class.java)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)
            .authenticator(TokenAuthenticator(tokenManager, simpleAuthService))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(EventService::class.java)
    }
}
