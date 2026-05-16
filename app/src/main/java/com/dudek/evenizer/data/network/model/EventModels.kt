package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val title: String,
    val start: String,
    val end: String,
    val status: String,
    val isPublic: Boolean,
    val description: String,
    val categories: List<String>,
    val locations: List<EventLocationRequest>
)

@Serializable
data class EventLocationRequest(
    val type: String,
    val location: String
)

@Serializable
data class EventData(
    val uuid: String,
    val title: String,
    val start: String,
    val end: String,
    val status: String,
    val isPublic: Boolean,
    val userUuid: String? = null,
    val banner: String? = null,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val categories: List<EventCategory>? = null,
    val eventLocations: List<EventLocationData>? = null,
    val ticketTiers: List<TicketTier>? = emptyList(),
    val eventOrganizers: List<EventOrganizer>? = emptyList(),
    val _count: Count? = null,
    val isFavorited: Boolean = false
)

@Serializable
data class EventOrganizer(
    val uuid: String,
    val eventUuid: String,
    val organizerUuid: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class TicketTier(
    val uuid: String? = null
)

@Serializable
data class Count(
    val favouritedBy: Int = 0,
    val rundowns: Int = 0
)

@Serializable
data class EventCategory(
    val uuid: String,
    val eventUuid: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val categoryDetails: List<CategoryDetail>? = null
)

@Serializable
data class CategoryDetail(
    val uuid: String,
    val name: String,
    val categoryUuid: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class EventLocationData(
    val uuid: String,
    val type: String,
    val location: String,
    val eventUuid: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)
