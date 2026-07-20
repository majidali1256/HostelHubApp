package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.AuthApi
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    val isLoggedIn: Flow<Boolean> = tokenManager.isLoggedIn
    val currentUserId: Flow<String?> = tokenManager.userId
    val userRole: Flow<String?> = tokenManager.userRole
    
    suspend fun login(email: String, password: String): Result<User> {
        Log.d(TAG, "Attempting login for email: $email")

        // Demo mode - bypass server and use mock data
        if (BuildConfig.DEMO_MODE) {
            return demoLogin(email, password)
        }

        return try {
            val response = authApi.login(LoginRequest(email, password))
            Log.d(TAG, "Login response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                Log.d(TAG, "Login successful for user: ${authResponse.user.id}")
                tokenManager.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.user.id,
                    role = authResponse.user.role.name.lowercase()
                )
                Result.success(authResponse.user)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody(), "Login failed with code: ${response.code()}")
                Log.e(TAG, "Login failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Connection error", e)
            Result.failure(Exception("Cannot connect to server. Please check if the server is running."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            Result.failure(Exception("Connection timed out. Please try again."))
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Unknown host error", e)
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(Exception("Login failed: ${e.message}"))
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
    
    private suspend fun demoLogin(email: String, password: String): Result<User> {
        Log.d(TAG, "Demo mode: Simulating login")
        delay(1000) // Simulate network delay

        // Simple validation for demo
        if (email.isBlank() || password.length < 4) {
            return Result.failure(Exception("Invalid email or password"))
        }

        // Create demo user based on email
        val isOwner = email.contains("owner", ignoreCase = true)
        val demoUser = User(
            id = "demo_user_${System.currentTimeMillis()}",
            username = email.substringBefore("@"),
            email = email,
            role = if (isOwner) UserRole.OWNER else UserRole.CUSTOMER,
            firstName = "Demo",
            lastName = "User",
            contactNumber = "+92 300 1234567",
            profilePicture = null,
            trustScore = 85,
            isVerified = true,
            emailVerified = true,
            verificationStatus = "verified"
        )

        // Save demo tokens
        tokenManager.saveTokens(
            accessToken = "demo_access_token_${System.currentTimeMillis()}",
            refreshToken = "demo_refresh_token_${System.currentTimeMillis()}",
            userId = demoUser.id
        )

        Log.d(TAG, "Demo login successful for: ${demoUser.email}")
        return Result.success(demoUser)
    }

    suspend fun signup(
        email: String,
        password: String,
        username: String,
        role: String = "customer",
        firstName: String? = null,
        lastName: String? = null,
        contactNumber: String? = null
    ): Result<User> {
        Log.d(TAG, "Attempting signup for email: $email")

        // Demo mode - bypass server and use mock data
        if (BuildConfig.DEMO_MODE) {
            return demoSignup(email, password, username, role, firstName, lastName, contactNumber)
        }

        return try {
            val request = SignupRequest(
                email = email,
                password = password,
                username = username,
                role = role,
                firstName = firstName,
                lastName = lastName,
                contactNumber = contactNumber
            )
            val response = authApi.signup(request)
            Log.d(TAG, "Signup response code: ${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                Log.d(TAG, "Signup successful for user: ${authResponse.user.id}")
                tokenManager.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.user.id,
                    role = authResponse.user.role.name.lowercase()
                )
                Result.success(authResponse.user)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody(), "Signup failed with code: ${response.code()}")
                Log.e(TAG, "Signup failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Connection error", e)
            Result.failure(Exception("Cannot connect to server. Please check if the server is running."))
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            Result.failure(Exception("Connection timed out. Please try again."))
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Unknown host error", e)
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Log.e(TAG, "Signup error", e)
            Result.failure(Exception("Signup failed: ${e.message}"))
        }
    }
    
    private suspend fun demoSignup(
        email: String,
        password: String,
        username: String,
        role: String,
        firstName: String?,
        lastName: String?,
        contactNumber: String?
    ): Result<User> {
        Log.d(TAG, "Demo mode: Simulating signup")
        delay(1500) // Simulate network delay

        // Simple validation for demo
        if (email.isBlank() || password.length < 6 || username.isBlank()) {
            return Result.failure(Exception("Please fill in all required fields. Password must be at least 6 characters."))
        }

        val userRole = when (role.lowercase()) {
            "owner" -> UserRole.OWNER
            "admin" -> UserRole.ADMIN
            else -> UserRole.CUSTOMER
        }

        val demoUser = User(
            id = "demo_user_${System.currentTimeMillis()}",
            username = username,
            email = email,
            role = userRole,
            firstName = firstName ?: username,
            lastName = lastName,
            contactNumber = contactNumber,
            profilePicture = null,
            trustScore = 50,
            isVerified = false,
            emailVerified = false,
            verificationStatus = "unverified"
        )

        // Save demo tokens
        tokenManager.saveTokens(
            accessToken = "demo_access_token_${System.currentTimeMillis()}",
            refreshToken = "demo_refresh_token_${System.currentTimeMillis()}",
            userId = demoUser.id,
            role = demoUser.role.name.lowercase()
        )

        Log.d(TAG, "Demo signup successful for: ${demoUser.email}")
        return Result.success(demoUser)
    }

    suspend fun forgotPassword(email: String): Result<String> {
        // Demo mode
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            return Result.success("Password reset link has been sent to $email (Demo Mode)")
        }

        return try {
            val response = authApi.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Reset link sent")
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody(), "Failed to send reset email")))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send reset email: ${e.message}"))
        }
    }
    
    suspend fun resetPassword(token: String, newPassword: String): Result<String> {
        // Demo mode
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            return Result.success("Password has been reset successfully (Demo Mode)")
        }

        return try {
            val response = authApi.resetPassword(token, ResetPasswordRequest(newPassword))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password reset successful")
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody(), "Password reset failed")))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Password reset failed: ${e.message}"))
        }
    }
    
    suspend fun verifyResetCode(email: String, code: String): Result<Boolean> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return if (code == "123456" || code.length == 6) {
                Result.success(true)
            } else {
                Result.failure(Exception("Invalid reset code"))
            }
        }

        return try {
            val response = authApi.verifyResetCode(VerifyResetCodeRequest(email, code))
            if (response.isSuccessful && response.body()?.valid == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody(), "Invalid or expired code")))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Code verification failed: ${e.message}"))
        }
    }

    suspend fun resetPasswordWithCode(email: String, code: String, newPassword: String): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            return Result.success("Password has been reset successfully (Demo Mode)")
        }

        return try {
            val response = authApi.resetPasswordWithCode(ResetPasswordWithCodeRequest(email, code, newPassword))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password reset successfully")
            } else {
                Result.failure(Exception(extractErrorMessage(response.errorBody(), "Password reset failed")))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Password reset failed: ${e.message}"))
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }
    
    suspend fun getAccessToken(): String? = tokenManager.accessToken.first()

    suspend fun socialLogin(
        provider: String,
        providerId: String,
        email: String,
        name: String,
        picture: String? = null
    ): Result<User> {
        Log.d(TAG, "Attempting social login via $provider for email: $email")
        if (BuildConfig.DEMO_MODE) {
            return demoLogin(email, "social_token_$providerId")
        }
        return try {
            val response = authApi.socialLogin(SocialLoginRequest(provider, providerId, email, name, picture))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    userId = authResponse.user.id,
                    role = authResponse.user.role.name.lowercase()
                )
                Result.success(authResponse.user)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody(), "Social login failed with code: ${response.code()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Social login error", e)
            Result.failure(Exception("Social login failed: ${e.message}"))
        }
    }

    suspend fun saveOAuthTokens(accessToken: String, refreshToken: String, userId: String, role: String? = null) {
        tokenManager.saveTokens(accessToken, refreshToken, userId, role)
    }
}
