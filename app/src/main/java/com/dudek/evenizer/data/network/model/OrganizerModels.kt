package com.dudek.evenizer.data.network.model

import kotlinx.serialization.SerialName
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
    val reviews: Double = 0.0,
    val roles: List<RoleData>? = emptyList(),
    val organizerMembers: List<kotlinx.serialization.json.JsonElement>? = emptyList(),
    val organizerLocations: List<kotlinx.serialization.json.JsonElement>? = emptyList(),
    @SerialName("_count")
    val count: OrganizerCount? = null
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

@Serializable
data class CreateRoleRequest(
    val name: String,
    val description: String
)

@Serializable
data class RoleData(
    val uuid: String,
    val name: String,
    val organizerUuid: String? = null,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class RoleResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: RoleData? = null
)

@Serializable
data class RoleListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<RoleData> = emptyList()
)

@Serializable
data class CreateMemberRequest(
    val userUuid: String,
    val roleUuid: String
)

@Serializable
data class MemberResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: MemberData? = null
)

@Serializable
data class MemberData(
    val uuid: String,
    val status: String,
    val reason: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val user: UserData? = null,
    val role: RoleData? = null
)
