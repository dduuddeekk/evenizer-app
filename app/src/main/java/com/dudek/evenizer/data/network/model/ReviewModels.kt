package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewData(
    val uuid: String,
    val userUuid: String? = null,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null,
    val medias: List<ReviewMedia>? = emptyList(),
    val organizerReviews: List<OrganizerReviewRelation>? = emptyList()
)

@Serializable
data class ReviewMedia(
    val uuid: String,
    val url: String,
    val type: String, // "IMAGE" or "VIDEO"
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class OrganizerReviewRelation(
    val uuid: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)
