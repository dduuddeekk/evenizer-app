package com.dudek.evenizer.data.network

import retrofit2.http.GET

interface ApiService {
    @GET("health")
    suspend fun checkHealth(): HealthResponse
}
