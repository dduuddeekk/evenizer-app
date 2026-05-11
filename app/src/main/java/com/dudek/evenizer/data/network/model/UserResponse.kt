package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: UserData? = null,
    val error: String? = null
)
