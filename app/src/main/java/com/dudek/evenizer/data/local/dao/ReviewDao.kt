package com.dudek.evenizer.data.local.dao

import androidx.room.*
import com.dudek.evenizer.data.local.entity.ReviewEntity
import com.dudek.evenizer.data.local.entity.ReviewMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedias(medias: List<ReviewMediaEntity>)

    @Transaction
    @Query("SELECT * FROM reviews WHERE organizerUuid = :organizerUuid ORDER BY createdAt DESC")
    fun getReviewsForOrganizer(organizerUuid: String): Flow<List<ReviewWithMedias>>

    @Query("DELETE FROM reviews WHERE organizerUuid = :organizerUuid")
    suspend fun clearReviewsForOrganizer(organizerUuid: String)

    @Delete
    suspend fun deleteReview(review: ReviewEntity)
}

data class ReviewWithMedias(
    @Embedded val review: ReviewEntity,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "reviewUuid"
    )
    val medias: List<ReviewMediaEntity>
)
