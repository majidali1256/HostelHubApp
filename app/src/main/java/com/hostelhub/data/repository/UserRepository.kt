package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.UserApi
import com.hostelhub.data.model.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    private var demoUser: User? = null

    suspend fun getUserById(id: String): Result<User> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            if (demoUser == null) {
                demoUser = User(
                    id = "demo_user",
                    username = "demouser",
                    email = "demo@example.com",
                    role = UserRole.CUSTOMER,
                    firstName = "Demo",
                    lastName = "User",
                    contactNumber = "+92 300 1234567",
                    profilePicture = null,
                    trustScore = 75,
                    isVerified = false,
                    emailVerified = true,
                    verificationStatus = "unverified",
                    bankDetails = BankDetails(
                        bankName = "HBL",
                        accountTitle = "Demo User",
                        accountNumber = "1234567890",
                        iban = "PK00HABB0001234567890123",
                        jazzCashNumber = "+92 300 1234567",
                        easyPaisaNumber = null,
                        verified = false
                    )
                )
            }
            return Result.success(demoUser!!)
        }

        return try {
            val response = userApi.getUserById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user", e)
            Result.failure(Exception("Failed to fetch user: ${e.message}"))
        }
    }

    suspend fun updateUser(id: String, updates: Map<String, Any>): Result<User> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            demoUser = demoUser?.copy(
                firstName = updates["firstName"] as? String ?: demoUser?.firstName,
                lastName = updates["lastName"] as? String ?: demoUser?.lastName,
                contactNumber = updates["contactNumber"] as? String ?: demoUser?.contactNumber
            )
            return Result.success(demoUser!!)
        }

        return try {
            val response = userApi.updateUser(id, updates)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update user"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user", e)
            Result.failure(Exception("Failed to update user: ${e.message}"))
        }
    }

    suspend fun updateProfilePicture(userId: String, imageFile: File): Result<User> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            demoUser = demoUser?.copy(profilePicture = "demo_profile_${System.currentTimeMillis()}.jpg")
            return Result.success(demoUser!!)
        }

        return try {
            val imagePart = MultipartBody.Part.createFormData(
                "profilePicture",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val response = userApi.updateProfilePicture(userId, imagePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile picture"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile picture", e)
            Result.failure(Exception("Failed to update profile picture: ${e.message}"))
        }
    }

    suspend fun getBankDetails(ownerId: String): Result<BankDetails> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            return Result.success(demoUser?.bankDetails ?: BankDetails(
                bankName = "HBL",
                accountTitle = "Demo Owner",
                accountNumber = "1234567890",
                iban = "PK00HABB0001234567890123",
                jazzCashNumber = "+92 300 1234567",
                easyPaisaNumber = "+92 321 1234567",
                verified = true
            ))
        }

        return try {
            val response = userApi.getBankDetails(ownerId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toBankDetails())
            } else {
                Result.failure(Exception("Failed to fetch bank details"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bank details", e)
            Result.failure(Exception("Failed to fetch bank details: ${e.message}"))
        }
    }

    suspend fun getMyBankDetails(): Result<BankDetails> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            return Result.success(demoUser?.bankDetails ?: BankDetails())
        }

        return try {
            val response = userApi.getMyBankDetails()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toBankDetails())
            } else {
                Result.failure(Exception("Failed to fetch bank details"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching my bank details", e)
            Result.failure(Exception("Failed to fetch my bank details: ${e.message}"))
        }
    }

    suspend fun updateBankDetails(bankDetails: BankDetails): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            demoUser = demoUser?.copy(bankDetails = bankDetails)
            return Result.success("Bank details updated successfully")
        }

        return try {
            val response = userApi.updateBankDetails(bankDetails)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Bank details updated")
            } else {
                Result.failure(Exception("Failed to update bank details"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating bank details", e)
            Result.failure(Exception("Failed to update bank details: ${e.message}"))
        }
    }

    suspend fun uploadVerificationDocument(documentFile: File, documentType: String): Result<String> {
        if (BuildConfig.DEMO_MODE) {
            delay(1500)
            demoUser = demoUser?.copy(verificationStatus = "pending")
            return Result.success("Document uploaded. Verification pending.")
        }

        return try {
            val documentPart = MultipartBody.Part.createFormData(
                "document",
                documentFile.name,
                documentFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val typePart = documentType.toRequestBody("text/plain".toMediaTypeOrNull())
            val namePart = documentFile.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = userApi.uploadVerificationDocument(documentPart, typePart, namePart)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Document uploaded")
            } else {
                Result.failure(Exception("Failed to upload document"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading document", e)
            Result.failure(Exception("Failed to upload document: ${e.message}"))
        }
    }

    suspend fun getVerificationStatus(): Result<VerificationStatusResponse> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(VerificationStatusResponse(
                status = demoUser?.verificationStatus ?: "unverified",
                trustScore = demoUser?.trustScore ?: 50,
                documents = emptyList()
            ))
        }

        return try {
            val response = userApi.getVerificationStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get verification status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRole(userId: String, role: String): Result<User> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val userRole = when (role.lowercase()) {
                "owner" -> UserRole.OWNER
                "customer" -> UserRole.CUSTOMER
                "admin" -> UserRole.ADMIN
                else -> UserRole.PENDING
            }
            demoUser = demoUser?.copy(role = userRole)
            return Result.success(demoUser!!)
        }

        return try {
            val response = userApi.updateUser(userId, mapOf("role" to role))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update role"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrustScore(userId: String): Result<TrustScoreDetail> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            val badges = listOf(
                TrustBadge(id = "verified_owner", name = "Verified Owner", icon = "check-circle", description = "Identity verified by Hostel Hub moderation"),
                TrustBadge(id = "id_check", name = "ID Document Check", icon = "shield", description = "CNIC/Passport successfully validated"),
                TrustBadge(id = "low_cancellations", name = "Low Cancellation Rate", icon = "thumb-up", description = "Reliable booking fulfillment (98% accuracy)")
            )
            return Result.success(
                TrustScoreDetail(
                    userId = userId,
                    score = 92,
                    level = "verified_owner",
                    factors = TrustScoreFactors(
                        verifiedEmail = true,
                        verifiedPhone = true,
                        verifiedId = true,
                        documentStatus = "verified",
                        reportsCount = 0,
                        completedBookings = 18,
                        cancellationRate = 2,
                        positiveReviews = 42
                    ),
                    badges = badges,
                    lastCalculated = "2026-07-19T10:00:00Z"
                )
            )
        }

        return try {
            val response = userApi.getTrustScore(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch trust score"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trust score", e)
            Result.failure(Exception("Failed to fetch trust score: ${e.message}"))
        }
    }

    suspend fun getUserBadges(userId: String): Result<List<TrustBadge>> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(listOf(
                TrustBadge(id = "verified_owner", name = "Verified Owner", icon = "check-circle", description = "Identity verified by Hostel Hub moderation"),
                TrustBadge(id = "id_check", name = "ID Document Check", icon = "shield", description = "CNIC/Passport successfully validated"),
                TrustBadge(id = "low_cancellations", name = "Low Cancellation Rate", icon = "thumb-up", description = "Reliable booking fulfillment")
            ))
        }

        return try {
            val response = userApi.getUserBadges(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.badges)
            } else {
                Result.failure(Exception("Failed to fetch badges"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching badges", e)
            Result.failure(Exception("Failed to fetch badges: ${e.message}"))
        }
    }
}

data class VerificationStatusResponse(
    val status: String = "unverified",
    val trustScore: Int? = 50,
    val document: String? = null,
    val rejectionReason: String? = null,
    val verificationDate: String? = null,
    val documents: List<VerificationDocument>? = emptyList()
)

data class VerificationDocument(
    val id: String = "",
    val type: String = "",
    val status: String = "",
    val uploadedAt: String = ""
)

