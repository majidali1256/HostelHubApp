package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @GET("hostels/{id}/reviews")
    suspend fun getHostelReviews(@Path("id") hostelId: String): Response<List<Review>>

    @GET("hostels/{id}/rating")
    suspend fun getHostelRatingStats(@Path("id") hostelId: String): Response<RatingStatistics>

    @GET("reviews")
    suspend fun getReviews(
        @Query("hostelId") hostelId: String? = null,
        @Query("reviewerId") reviewerId: String? = null,
        @Query("minRating") minRating: Int? = null,
        @Query("status") status: String? = null
    ): Response<List<Review>>

    @POST("reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>

    @PATCH("reviews/{id}")
    suspend fun updateReview(
        @Path("id") reviewId: String,
        @Body request: CreateReviewRequest
    ): Response<Review>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: String): Response<MessageResponse>

    @POST("reviews/{id}/helpful")
    suspend fun toggleHelpful(@Path("id") reviewId: String): Response<HelpfulResponse>

    @POST("reviews/{id}/response")
    suspend fun postOwnerResponse(
        @Path("id") reviewId: String,
        @Body request: OwnerResponseRequest
    ): Response<Review>
}

data class CreateReviewRequest(
    val hostelId: String,
    val bookingId: String? = null,
    val rating: Int,
    val cleanliness: Int = 5,
    val accuracy: Int = 5,
    val communication: Int = 5,
    val location: Int = 5,
    val value: Int = 5,
    val title: String,
    val comment: String,
    val photos: List<String> = emptyList()
)

data class OwnerResponseRequest(
    val content: String
)

data class HelpfulResponse(
    val helpfulCount: Int = 0
)
