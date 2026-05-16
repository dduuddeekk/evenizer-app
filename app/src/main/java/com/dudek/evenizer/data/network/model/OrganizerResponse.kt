package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OrganizerListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: OrganizerListData? = null
)

@Serializable
data class OrganizerListData(
    val data: List<OrganizerData>,
    val meta: Meta
)

@Serializable
data class OrganizerResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: OrganizerData? = null
)
