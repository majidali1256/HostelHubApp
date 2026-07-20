package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Authentication API endpoints
 */
interface AuthApi {
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>
    
    @POST("auth/reset-password/{token}")
    suspend fun resetPassword(
        @Path("token") token: String,
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>
    
    @GET("auth/verify-email/{token}")
    suspend fun verifyEmail(@Path("token") token: String): Response<MessageResponse>
    
    @POST("auth/verify-reset-code")
    suspend fun verifyResetCode(@Body request: VerifyResetCodeRequest): Response<VerifyResetCodeResponse>
    
    @POST("auth/reset-password")
    suspend fun resetPasswordWithCode(@Body request: ResetPasswordWithCodeRequest): Response<MessageResponse>
    
    @POST("auth/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<AuthResponse>
}
