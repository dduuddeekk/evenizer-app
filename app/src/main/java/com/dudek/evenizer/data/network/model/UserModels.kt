package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val uuid: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val username: String? = null,
    val isEmailVerified: Boolean = false,
    val isVerified: Boolean = false,
    val status: String? = null,
    val role: String? = null,
    val profile: String? = null,
    val birthdate: String? = null,
    val bio: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val deletedAt: String? = null
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)
