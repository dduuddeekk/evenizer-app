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

@Serializable
data class UserListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: UserListData? = null,
    val error: String? = null
)

@Serializable
data class UserListData(
    val data: List<UserData>,
    val meta: Meta
)
