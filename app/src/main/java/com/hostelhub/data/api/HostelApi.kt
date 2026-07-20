package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Hostel API endpoints
 */
interface HostelApi {
    
    @GET("hostels")
    suspend fun getHostels(): Response<HostelListResponse>
    
    @GET("hostels/{id}")
    suspend fun getHostelById(@Path("id") id: String): Response<Hostel>
    
    @GET("hostels/search")
    suspend fun searchHostels(
        @Query("search") search: String? = null,
        @Query("minPrice") minPrice: Int? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("amenities") amenities: String? = null,
        @Query("roomCategories") roomCategories: String? = null,
        @Query("genderPreference") genderPreference: String? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("verifiedOnly") verifiedOnly: Boolean? = null,
        @Query("location") location: String? = null,
        @Query("sortBy") sortBy: String? = null
    ): Response<List<Hostel>>
    
    @GET("hostels/nearby")
    suspend fun getNearbyHostels(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radius: Int = 5000
    ): Response<List<Hostel>>
    
    @GET("hostels/recommendations")
    suspend fun getRecommendations(): Response<List<Hostel>>
    
    @POST("hostels/{id}/reviews")
    suspend fun addReview(
        @Path("id") hostelId: String,
        @Body review: ReviewRequest
    ): Response<Hostel>
    
    @DELETE("hostels/{id}/reviews")
    suspend fun deleteReview(@Path("id") hostelId: String): Response<MessageResponse>
    
    @POST("search/ai-query")
    suspend fun aiSearch(@Body query: AISearchRequest): Response<AISearchResponse>

    @POST("ai/smart-search")
    suspend fun aiSmartSearch(@Body query: AISearchRequest): Response<AISearchResponse>
    
    // Module 10: Price Guidance & Fair Rent Estimator
    @POST("hostels/predict-rent")
    suspend fun predictRent(@Body request: PredictRentRequest): Response<PredictRentResponse>

    @GET("hostels/{id}/fairness-analysis")
    suspend fun getFairnessAnalysis(@Path("id") id: String): Response<FairnessAnalysisResponse>

    @GET("market/benchmarks")
    suspend fun getMarketBenchmarks(@Query("location") location: String): Response<MarketBenchmarksResponse>
    
    // Owner endpoints (Module 3)
    @GET("hostels/owner/my-hostels")
    suspend fun getOwnerHostels(): Response<HostelListResponse>
    
    @POST("hostels")
    suspend fun createHostel(@Body request: CreateHostelRequest): Response<Hostel>
    
    @PUT("hostels/{id}")
    suspend fun updateHostel(@Path("id") id: String, @Body request: CreateHostelRequest): Response<Hostel>
    
    @DELETE("hostels/{id}")
    suspend fun deleteHostel(@Path("id") id: String): Response<MessageResponse>
    
    @PATCH("hostels/{id}/status")
    suspend fun updateHostelStatus(@Path("id") id: String, @Body request: StatusRequest): Response<Hostel>
}

/**
 * Wrapper for paginated hostel list responses from the backend.
 * Backend returns: {"hostels": [...], "pagination": {...}}
 */
data class HostelListResponse(
    val hostels: List<Hostel> = emptyList(),
    val pagination: PaginationInfo? = null
)

data class PaginationInfo(
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val hasMore: Boolean = false
)

data class CreateHostelRequest(
    val name: String,
    val location: String,
    val price: Double,
    val capacity: Int? = null,
    val description: String? = null,
    val amenities: List<String> = emptyList(),
    val category: String = "Shared Room",
    val genderPreference: String = "any",
    val images: List<String> = emptyList(),
    val propertyRules: List<String>? = null,
    val rooms: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class StatusRequest(
    val status: String
)

data class ReviewRequest(
    val rating: Int,
    val comment: String
)

data class AISearchRequest(
    val query: String,
    val userPreferences: Map<String, Any>? = null
)

data class AISearchSuggestion(
    val hostel: Hostel,
    val matchReason: String? = null
)

data class AISearchResponse(
    val recommendation: String? = null,
    val interpretation: String? = null,
    val hostels: List<Hostel> = emptyList(),
    val suggestions: List<AISearchSuggestion> = emptyList()
)

