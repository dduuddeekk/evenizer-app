package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class OrganizerData(
    val uuid: String,
    val name: String,
    val status: String,
    val isVerified: Boolean,
    val isPublic: Boolean,
    val description: String? = null,
    val logo: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val userUuid: String? = null,
    val followCount: Int = 0,
    val isFollow: Boolean = false,
    val _count: OrganizerCount? = null
)

@Serializable
data class OrganizerCount(
    val followers: Int = 0,
    val eventOrganizers: Int = 0
)

@Serializable
data class CreateOrganizerRequest(
    val name: String,
    val description: String,
    val isPublic: Boolean = true
)
