package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: EventData? = null
)

@Serializable
data class EventListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: EventListContainer? = null
)

@Serializable
data class EventListContainer(
    val data: List<EventData>,
    val meta: Meta
)

@Serializable
data class Meta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

@Serializable
data class InviteOrganizerRequest(
    val organizerUuid: String,
    val roleUuids: List<String>
)

@Serializable
data class EventOrganizerData(
    val uuid: String,
    val status: String,
    val rejectReason: String? = null,
    val eventUuid: String? = null,
    val organizerUuid: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val organizer: OrganizerData? = null,
    val eventOrganizerDetails: List<EventOrganizerDetail>? = emptyList()
)

@Serializable
data class EventOrganizerDetail(
    val uuid: String,
    val eventOrganizerUuid: String? = null,
    val roleUuid: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val role: RoleData? = null
)

@Serializable
data class EventOrganizerResponse(
    val statusCode: Int? = null,
    val success: Boolean? = null,
    val message: String,
    val data: EventOrganizerData? = null
)

@Serializable
data class EventOrganizerListResponse(
    val statusCode: Int? = null,
    val success: Boolean? = null,
    val message: String,
    val data: List<EventOrganizerData> = emptyList()
)

@Serializable
data class FavouriteResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: FavouriteData? = null
)

@Serializable
data class FavouriteData(
    val message: String
)

@Serializable
data class DeleteEventResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: DeleteEventData? = null
)

@Serializable
data class DeleteEventData(
    val message: String
)
