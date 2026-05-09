package com.dudek.evenizer.data.network.response

import com.dudek.evenizer.data.network.model.LoginData
import com.dudek.evenizer.data.network.model.RefreshData
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: LoginData? = null,
    val error: String? = null
)

@Serializable
data class RefreshResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: RefreshData? = null,
    val error: String? = null
)

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: String? = null,
    val error: String? = null
)
