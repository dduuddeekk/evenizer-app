package com.dudek.evenizer.data.network.service

import com.dudek.evenizer.data.network.model.CommonReviewResponse
import com.dudek.evenizer.data.network.model.ReviewDetailResponse
import com.dudek.evenizer.data.network.model.ReviewListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ReviewService {

    @GET("review/organizer/{organizerUuid}")
    suspend fun getOrganizerReviews(
        @Path("organizerUuid") organizerUuid: String,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10,
        @Query("sortBy") sortBy: String? = "createdAt",
        @Query("sortOrder") sortOrder: String? = "desc"
    ): Response<ReviewListResponse>

    @Multipart
    @POST("review/organizer/{organizerUuid}")
    suspend fun createOrganizerReview(
        @Path("organizerUuid") organizerUuid: String,
        @Part("rating") rating: RequestBody,
        @Part("comment") comment: RequestBody?,
        @Part medias: List<MultipartBody.Part>?
    ): Response<ReviewDetailResponse>

    @Multipart
    @PATCH("review/{uuid}")
    suspend fun updateReview(
        @Path("uuid") uuid: String,
        @Part("rating") rating: RequestBody?,
        @Part("comment") comment: RequestBody?,
        @Part medias: List<MultipartBody.Part>?
    ): Response<ReviewDetailResponse>

    @DELETE("review/{uuid}")
    suspend fun deleteReview(
        @Path("uuid") uuid: String
    ): Response<CommonReviewResponse>
}
