package com.dudek.evenizer.data.network.response

import com.dudek.evenizer.data.network.model.UserData
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: UserData? = null,
    val error: String? = null
)
