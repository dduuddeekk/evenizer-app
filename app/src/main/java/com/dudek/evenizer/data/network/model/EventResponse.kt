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
