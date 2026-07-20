package com.hostelhub.data.api

import com.hostelhub.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Fraud Detection & Reporting API endpoints
 */
interface FraudApi {

    @POST("fraud/report")
    suspend fun submitFraudReport(@Body request: CreateFraudReportRequest): Response<FraudReport>

    @GET("fraud/reports")
    suspend fun getMyFraudReports(): Response<List<FraudReport>>

    @GET("fraud/reports/{id}")
    suspend fun getFraudReport(@Path("id") id: String): Response<FraudReport>

    @POST("fraud/calculate-risk/{id}")
    suspend fun getHostelRiskScore(@Path("id") hostelId: String): Response<RiskScoreResponse>

    @Multipart
    @POST("fraud/report-evidence")
    suspend fun submitFraudReportWithEvidence(
        @Part("reportedUserId") reportedUserId: okhttp3.RequestBody?,
        @Part("hostelId") hostelId: okhttp3.RequestBody?,
        @Part("type") type: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody,
        @Part evidence: List<MultipartBody.Part>
    ): Response<FraudReport>

    @GET("fraud/reports")
    suspend fun getAllFraudReports(
        @Query("status") status: String? = null,
        @Query("riskLevel") riskLevel: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("skip") skip: Int = 0
    ): Response<FraudReportsListResponse>

    @PATCH("fraud/reports/{id}")
    suspend fun updateFraudReportStatus(
        @Path("id") id: String,
        @Body request: UpdateFraudReportStatusRequest
    ): Response<FraudReport>

    @DELETE("fraud/reports/{id}")
    suspend fun deleteFraudReport(@Path("id") id: String): Response<MessageResponse>

    @GET("fraud/stats")
    suspend fun getFraudStats(): Response<FraudStats>

    @GET("fraud/flagged-hostels")
    suspend fun getFlaggedHostels(): Response<List<FraudReport>>
}

data class CreateFraudReportRequest(
    val reportedUserId: String? = null,
    val hostelId: String? = null,
    val type: String, // "fake_listing", "duplicate_images", "suspicious_text", "scam", "impersonation", "other"
    val description: String,
    val evidence: FraudEvidence? = null
)

data class FraudEvidence(
    val images: List<String>? = null,
    val urls: List<String>? = null,
    val screenshots: List<String>? = null
)

data class RiskScoreResponse(
    val hostelId: String,
    val totalRiskScore: Int,
    val riskLevel: String,
    val analysis: AIAnalysis?,
    val flags: List<String>
)

