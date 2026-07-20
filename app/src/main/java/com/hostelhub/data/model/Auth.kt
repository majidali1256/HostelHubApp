package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Authentication response from login/signup
 */
data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String
)

/**
 * Login request payload
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Signup request payload
 */
data class SignupRequest(
    val email: String,
    val password: String,
    val username: String,
    val role: String = "customer",
    val firstName: String? = null,
    val lastName: String? = null,
    val contactNumber: String? = null
)

/**
 * Forgot password request
 */
data class ForgotPasswordRequest(
    val email: String
)

/**
 * Reset password request
 */
data class ResetPasswordRequest(
    val password: String
)

/**
 * Generic message response
 */
data class MessageResponse(
    val message: String
)

/**
 * Generic error response
 */
data class ErrorResponse(
    val error: String
)

data class VerifyResetCodeRequest(
    val email: String,
    val code: String
)

data class VerifyResetCodeResponse(
    val valid: Boolean,
    val message: String? = null
)

data class ResetPasswordWithCodeRequest(
    val email: String,
    val code: String,
    val password: String
)

data class SocialLoginRequest(
    val provider: String,
    val providerId: String,
    val email: String,
    val name: String,
    val picture: String? = null
)
