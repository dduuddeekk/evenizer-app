package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ReviewListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReviewListContainer? = null
)

@Serializable
data class ReviewListContainer(
    val data: List<ReviewData>,
    val meta: Meta
)

@Serializable
data class ReviewDetailResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ReviewData? = null
)

@Serializable
data class CommonReviewResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: kotlinx.serialization.json.JsonElement? = null
)
