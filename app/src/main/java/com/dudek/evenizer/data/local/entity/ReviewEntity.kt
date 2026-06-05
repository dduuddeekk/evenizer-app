package com.dudek.evenizer.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dudek.evenizer.data.network.model.ReviewData
import com.dudek.evenizer.data.network.model.ReviewMedia

@Entity(
    tableName = "reviews",
    indices = [Index(value = ["organizerUuid"])]
)
data class ReviewEntity(
    @PrimaryKey val uuid: String,
    val organizerUuid: String,
    val userUuid: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "review_medias",
    foreignKeys = [
        ForeignKey(
            entity = ReviewEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["reviewUuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["reviewUuid"])]
)
data class ReviewMediaEntity(
    @PrimaryKey val uuid: String,
    val reviewUuid: String,
    val url: String,
    val type: String,
    val createdAt: String,
    val updatedAt: String
)

fun ReviewData.toEntity(organizerUuid: String): ReviewEntity {
    return ReviewEntity(
        uuid = uuid,
        organizerUuid = organizerUuid,
        userUuid = userUuid,
        rating = rating,
        comment = comment,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ReviewMedia.toEntity(reviewUuid: String): ReviewMediaEntity {
    return ReviewMediaEntity(
        uuid = uuid,
        reviewUuid = reviewUuid,
        url = url,
        type = type,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
