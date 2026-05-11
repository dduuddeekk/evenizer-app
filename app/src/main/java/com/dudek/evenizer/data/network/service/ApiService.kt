package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.HealthResponse
import retrofit2.http.GET

interface ApiService {
    @GET("health")
    suspend fun checkHealth(): HealthResponse
}
