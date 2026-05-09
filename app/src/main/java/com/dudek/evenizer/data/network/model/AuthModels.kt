package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
    val device: String = "mobile"
)

@Serializable
data class LoginData(
    val user: UserData,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class RefreshData(
    val accessToken: String
)
