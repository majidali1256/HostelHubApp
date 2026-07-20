package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ReviewRepository"

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewApi: ReviewApi
) {
    private val mockReviews = mutableListOf(
        Review(
            id = "rev_1",
            hostelId = "demo_1",
            userId = "user_ali",
            username = "Ali Khan",
            rating = 5,
            cleanliness = 5,
            accuracy = 5,
            communication = 5,
            location = 5,
            value = 5,
            title = "Outstanding security and high speed WiFi!",
            comment = "I stayed here for 2 semesters. The biometric access control is excellent and the high speed fiber WiFi never went down. Highly recommended for engineering students.",
            photos = emptyList(),
            helpfulCount = 12,
            helpfulVotes = listOf("user_2", "user_3"),
            response = OwnerReply(
                content = "Thank you so much Ali! We are glad you enjoyed the biometric security and high-speed fiber.",
                responderId = ReviewerInfo(firstName = "Management", lastName = "Team")
            ),
            createdAt = "2 days ago"
        ),
        Review(
            id = "rev_2",
            hostelId = "demo_1",
            userId = "user_bilal",
            username = "Bilal Ahmed",
            rating = 4,
            cleanliness = 4,
            accuracy = 5,
            communication = 4,
            location = 5,
            value = 4,
            title = "Clean rooms and very close to NUST campus",
            comment = "The location is unbeatable if you study at H-12. Food is good too though laundry can get busy on weekends.",
            photos = emptyList(),
            helpfulCount = 5,
            helpfulVotes = emptyList(),
            response = null,
            createdAt = "1 week ago"
        )
    )

    suspend fun getHostelReviews(hostelId: String): Result<List<Review>> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(mockReviews.filter { it.hostelId == hostelId || hostelId.startsWith("demo") })
        }
        return try {
            val response = reviewApi.getHostelReviews(hostelId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = extractErrorMessage(response.errorBody(), "Failed to fetch reviews")
                Log.e(TAG, err)
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reviews", e)
            Result.failure(e)
        }
    }

    suspend fun getHostelRatingStats(hostelId: String): Result<RatingStatistics> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            val filtered = mockReviews.filter { it.hostelId == hostelId || hostelId.startsWith("demo") }
            val count = filtered.size
            if (count == 0) return Result.success(RatingStatistics())
            val avg = filtered.map { it.rating.toFloat() }.average().toFloat()
            val clean = filtered.map { if (it.cleanliness > 0) it.cleanliness.toFloat() else it.rating.toFloat() }.average().toFloat()
            val acc = filtered.map { if (it.accuracy > 0) it.accuracy.toFloat() else it.rating.toFloat() }.average().toFloat()
            val comm = filtered.map { if (it.communication > 0) it.communication.toFloat() else it.rating.toFloat() }.average().toFloat()
            val loc = filtered.map { if (it.location > 0) it.location.toFloat() else it.rating.toFloat() }.average().toFloat()
            val valForMon = filtered.map { if (it.value > 0) it.value.toFloat() else it.rating.toFloat() }.average().toFloat()
            
            val breakdown = mutableMapOf<String, Int>()
            (1..5).forEach { star ->
                breakdown[star.toString()] = filtered.count { it.rating == star }
            }
            return Result.success(
                RatingStatistics(
                    avgRating = avg,
                    avgCleanliness = clean,
                    avgAccuracy = acc,
                    avgCommunication = comm,
                    avgLocation = loc,
                    avgValue = valForMon,
                    totalReviews = count,
                    breakdown = breakdown
                )
            )
        }
        return try {
            val response = reviewApi.getHostelRatingStats(hostelId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load rating statistics"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stats", e)
            Result.failure(e)
        }
    }

    suspend fun createReview(request: CreateReviewRequest): Result<Review> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val newRev = Review(
                id = "rev_${System.currentTimeMillis()}",
                hostelId = request.hostelId,
                bookingId = request.bookingId,
                userId = "current_user",
                username = "You",
                rating = request.rating,
                cleanliness = request.cleanliness,
                accuracy = request.accuracy,
                communication = request.communication,
                location = request.location,
                value = request.value,
                title = request.title,
                comment = request.comment,
                photos = request.photos,
                helpfulCount = 0,
                createdAt = "Just now"
            )
            mockReviews.add(0, newRev)
            return Result.success(newRev)
        }
        return try {
            val response = reviewApi.createReview(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = extractErrorMessage(response.errorBody(), "Failed to create review")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReview(reviewId: String): Result<Boolean> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            mockReviews.removeAll { it.id == reviewId }
            return Result.success(true)
        }
        return try {
            val response = reviewApi.deleteReview(reviewId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete review"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleHelpful(reviewId: String): Result<Int> {
        if (BuildConfig.DEMO_MODE) {
            delay(200)
            val idx = mockReviews.indexOfFirst { it.id == reviewId }
            if (idx != -1) {
                val rev = mockReviews[idx]
                val newCount = rev.helpfulCount + 1
                mockReviews[idx] = rev.copy(helpfulCount = newCount)
                return Result.success(newCount)
            }
            return Result.success(1)
        }
        return try {
            val response = reviewApi.toggleHelpful(reviewId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.helpfulCount)
            } else {
                Result.failure(Exception("Failed to record vote"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postOwnerResponse(reviewId: String, content: String): Result<Review> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            val idx = mockReviews.indexOfFirst { it.id == reviewId }
            if (idx != -1) {
                val rev = mockReviews[idx]
                val reply = OwnerReply(
                    content = content,
                    responderId = ReviewerInfo(firstName = "Hostel", lastName = "Owner"),
                    respondedAt = "Just now"
                )
                val updated = rev.copy(response = reply)
                mockReviews[idx] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Review not found in demo data"))
        }
        return try {
            val response = reviewApi.postOwnerResponse(reviewId, OwnerResponseRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val err = extractErrorMessage(response.errorBody(), "Failed to submit response")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: okhttp3.ResponseBody?, defaultMsg: String): String {
        return try {
            val errorStr = errorBody?.string() ?: return defaultMsg
            if (errorStr.contains("\"error\":")) {
                org.json.JSONObject(errorStr).optString("error", defaultMsg)
            } else if (errorStr.contains("\"message\":")) {
                org.json.JSONObject(errorStr).optString("message", defaultMsg)
            } else {
                errorStr.ifBlank { defaultMsg }
            }
        } catch (e: Exception) {
            defaultMsg
        }
    }
}
