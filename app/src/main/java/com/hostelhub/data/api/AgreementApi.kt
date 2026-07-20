package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Digital Agreements API endpoints
 */
interface AgreementApi {

    @GET("agreements")
    suspend fun getAgreements(): Response<List<Agreement>>

    @GET("agreements/my-agreements")
    suspend fun getMyAgreements(): Response<List<Agreement>>

    @POST("agreements")
    suspend fun createAgreement(@Body request: CreateAgreementRequest): Response<Agreement>

    @POST("agreements/generate")
    suspend fun generateAgreement(@Body request: GenerateAgreementRequest): Response<Agreement>

    @GET("agreements/{id}")
    suspend fun getAgreement(@Path("id") id: String): Response<Agreement>

    @POST("agreements/{id}/sign")
    suspend fun signAgreement(
        @Path("id") id: String,
        @Body request: SignAgreementRequest
    ): Response<Agreement>

    @POST("signatures")
    suspend fun createSignature(@Body request: CreateSignatureRequest): Response<SignatureResponse>

    @GET("agreement-templates")
    suspend fun getAgreementTemplates(): Response<List<AgreementTemplate>>

    @POST("agreements/direct")
    suspend fun createDirectAgreement(@Body request: CreateDirectAgreementRequest): Response<Agreement>

    @POST("agreements/{id}/terminate")
    suspend fun terminateAgreement(
        @Path("id") id: String,
        @Body request: TerminateAgreementRequest
    ): Response<Agreement>
}

data class GenerateAgreementRequest(
    val bookingId: String,
    val hostelId: String,
    val studentId: String,
    val ownerId: String,
    val termsAndConditions: String,
    val monthlyRent: Double
)

data class CreateAgreementRequest(
    val bookingId: String,
    val hostelId: String,
    val tenantId: String,
    val type: String = "rental",
    val title: String,
    val content: String,
    val terms: List<AgreementTerm>,
    val duration: AgreementDuration,
    val rentAmount: Double,
    val deposit: Double,
    val metadata: AgreementMetadata? = null
)

data class SignAgreementRequest(
    val signatureData: String // Base64 encoded signature image
)

data class CreateSignatureRequest(
    val signatureData: String,
    val userId: String
)

data class SignatureResponse(
    val id: String,
    val signatureUrl: String,
    val createdAt: String
)

data class AgreementTemplate(
    val id: String,
    val name: String,
    val type: String,
    val content: String,
    val terms: List<AgreementTerm>
)

data class CreateDirectAgreementRequest(
    val tenantId: String,
    val hostelId: String? = null,
    val title: String,
    val content: String,
    val terms: List<AgreementTerm>,
    val duration: AgreementDuration,
    val rentAmount: Double,
    val deposit: Double
)

data class TerminateAgreementRequest(
    val reason: String
)

