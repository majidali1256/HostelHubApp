package com.hostelhub.data.api

import com.hostelhub.data.model.*
import com.hostelhub.data.repository.VerificationStatusResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * User API endpoints
 */
interface UserApi {
    
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<User>
    
    @POST("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: Map<String, Any>
    ): Response<User>

    @Multipart
    @POST("users/{id}/profile-picture")
    suspend fun updateProfilePicture(
        @Path("id") id: String,
        @Part profilePicture: MultipartBody.Part
    ): Response<User>

    @GET("users/bank-details/{ownerId}")
    suspend fun getBankDetails(@Path("ownerId") ownerId: String): Response<BankDetailsResponse>

    @GET("users/bank-details")
    suspend fun getMyBankDetails(): Response<BankDetailsResponse>

    @PUT("users/bank-details")
    suspend fun updateBankDetails(@Body bankDetails: BankDetails): Response<MessageResponse>

    @Multipart
    @POST("verification/upload")
    suspend fun uploadVerificationDocument(
        @Part document: MultipartBody.Part,
        @Part("documentType") documentType: okhttp3.RequestBody,
        @Part("documentName") documentName: okhttp3.RequestBody
    ): Response<MessageResponse>

    @GET("verification/status")
    suspend fun getVerificationStatus(): Response<VerificationStatusResponse>

    @GET("users/{id}/trust-score")
    suspend fun getTrustScore(@Path("id") id: String): Response<TrustScoreDetail>

    @GET("users/{id}/badges")
    suspend fun getUserBadges(@Path("id") id: String): Response<UserBadgesResponse>
}

data class BankDetailsResponse(
    val bankDetails: BankDetails? = null,
    val bankName: String? = null,
    val accountTitle: String? = null,
    val accountNumber: String? = null,
    val iban: String? = null,
    val jazzCashNumber: String? = null,
    val easyPaisaNumber: String? = null,
    val verified: Boolean = false
) {
    fun toBankDetails(): BankDetails = bankDetails ?: BankDetails(
        bankName = bankName,
        accountTitle = accountTitle,
        accountNumber = accountNumber,
        iban = iban,
        jazzCashNumber = jazzCashNumber,
        easyPaisaNumber = easyPaisaNumber,
        verified = verified
    )
}
