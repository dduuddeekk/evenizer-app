package com.dudek.evenizer.data.network

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String
)
