package com.dudek.evenizer.data.repository

import android.content.Context
import android.net.Uri
import com.dudek.evenizer.data.local.dao.ReviewDao
import com.dudek.evenizer.data.local.dao.ReviewWithMedias
import com.dudek.evenizer.data.local.entity.toEntity
import com.dudek.evenizer.data.network.model.ReviewData
import com.dudek.evenizer.data.network.service.ReviewService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ReviewRepository(
    private val reviewService: ReviewService,
    private val reviewDao: ReviewDao
) {
    fun getReviews(organizerUuid: String): Flow<List<ReviewWithMedias>> {
        return reviewDao.getReviewsForOrganizer(organizerUuid)
    }

    suspend fun refreshReviews(organizerUuid: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = reviewService.getOrganizerReviews(organizerUuid)
                if (response.isSuccessful) {
                    val reviews = response.body()?.data?.data ?: emptyList()
                    reviewDao.clearReviewsForOrganizer(organizerUuid)
                    reviewDao.insertReviews(reviews.map { it.toEntity(organizerUuid) })
                    reviewDao.insertMedias(reviews.flatMap { review ->
                        review.medias?.map { it.toEntity(review.uuid) } ?: emptyList()
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun createReview(
        context: Context,
        organizerUuid: String,
        rating: Int,
        comment: String?,
        mediaUris: List<Uri>
    ): Result<ReviewData?> {
        return withContext(Dispatchers.IO) {
            try {
                val ratingBody = rating.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val commentBody = comment?.toRequestBody("text/plain".toMediaTypeOrNull())
                val mediaParts = mediaUris.mapNotNull { uri ->
                    val file = uriToFile(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("medias", it.name, requestFile)
                    }
                }

                val response = reviewService.createOrganizerReview(organizerUuid, ratingBody, commentBody, mediaParts)
                if (response.isSuccessful) {
                    val review = response.body()?.data
                    review?.let {
                        reviewDao.insertReviews(listOf(it.toEntity(organizerUuid)))
                        reviewDao.insertMedias(it.medias?.map { m -> m.toEntity(it.uuid) } ?: emptyList())
                    }
                    Result.success(review)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateReview(
        context: Context,
        reviewUuid: String,
        organizerUuid: String,
        rating: Int?,
        comment: String?,
        mediaUris: List<Uri>
    ): Result<ReviewData?> {
        return withContext(Dispatchers.IO) {
            try {
                val ratingBody = rating?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val commentBody = comment?.toRequestBody("text/plain".toMediaTypeOrNull())
                val mediaParts = mediaUris.mapNotNull { uri ->
                    val file = uriToFile(context, uri)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("medias", it.name, requestFile)
                    }
                }

                val response = reviewService.updateReview(reviewUuid, ratingBody, commentBody, mediaParts)
                if (response.isSuccessful) {
                    val review = response.body()?.data
                    review?.let {
                        reviewDao.insertReviews(listOf(it.toEntity(organizerUuid)))
                        reviewDao.insertMedias(it.medias?.map { m -> m.toEntity(it.uuid) } ?: emptyList())
                    }
                    Result.success(review)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteReview(reviewUuid: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = reviewService.deleteReview(reviewUuid)
                if (response.isSuccessful) {
                    // Logic to delete from local DB would go here if needed
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_review_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return if (file.exists()) file else null
    }
}
