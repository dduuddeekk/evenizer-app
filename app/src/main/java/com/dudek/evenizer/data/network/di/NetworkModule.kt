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
import com.dudek.evenizer.data.network.service.OrganizerService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val BASE_URL = if (BuildConfig.BASE_URL.endsWith("/")) {
        BuildConfig.BASE_URL
    } else {
        "${BuildConfig.BASE_URL}/"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
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
    private var organizerServiceInstance: OrganizerService? = null
    
    @Volatile
    private var tokenManagerInstance: TokenManager? = null

    @Volatile
    private var authenticatedClient: OkHttpClient? = null

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

    fun getOrganizerService(context: Context): OrganizerService {
        return organizerServiceInstance ?: synchronized(this) {
            organizerServiceInstance ?: buildOrganizerService(context.applicationContext).also { organizerServiceInstance = it }
        }
    }

    private fun getBaseOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(loggingInterceptor)
    }

    private fun getAuthenticatedClient(context: Context): OkHttpClient {
        return authenticatedClient ?: synchronized(this) {
            authenticatedClient ?: run {
                val tokenManager = getTokenManager(context)
                
                // Base retrofit for "simple" auth service used in authenticator
                val authRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()
                val simpleAuthService = authRetrofit.create(AuthService::class.java)

                getBaseOkHttpClientBuilder()
                    .addInterceptor(AuthInterceptor(tokenManager))
                    .authenticator(TokenAuthenticator(tokenManager, simpleAuthService))
                    .build()
                    .also { authenticatedClient = it }
            }
        }
    }

    private fun buildApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getBaseOkHttpClientBuilder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    private fun buildAuthService(context: Context): AuthService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthService::class.java)
    }

    private fun buildUserService(context: Context): UserService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(UserService::class.java)
    }

    private fun buildEventService(context: Context): EventService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(EventService::class.java)
    }

    private fun buildOrganizerService(context: Context): OrganizerService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(context))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OrganizerService::class.java)
    }
}
