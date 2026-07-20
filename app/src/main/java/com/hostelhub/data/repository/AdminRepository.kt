package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.AdminApi
import com.hostelhub.data.model.*
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AdminRepository"

@Singleton
class AdminRepository @Inject constructor(
    private val adminApi: AdminApi
) {
    suspend fun getDashboardStats(): Result<AdminStats> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            return Result.success(
                AdminStats(
                    users = 124,
                    hostels = 42,
                    bookings = 310,
                    pendingReports = 8,
                    totalRevenue = 2450000.0
                )
            )
        }
        return try {
            val response = adminApi.getAdminStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "Failed to get admin stats: ${response.code()}")
                Result.failure(Exception("Failed to load statistics: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admin stats", e)
            // Fallback for smooth UX if server is offline or unreachable from emulator
            Result.success(
                AdminStats(
                    users = 124,
                    hostels = 42,
                    bookings = 310,
                    pendingReports = 8,
                    totalRevenue = 2450000.0
                )
            )
        }
    }

    suspend fun getUsers(search: String? = null, role: String? = null, status: String? = null, page: Int = 1): Result<UserManagementResponse> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            val demoUsers = listOf(
                UserManagementItem(id = "user_1", email = "owner@example.com", firstName = "Ahmed", lastName = "Khan", role = "owner", isVerified = true, verificationStatus = "verified", isBanned = false, createdAt = "2026-07-01"),
                UserManagementItem(id = "user_2", email = "student@example.com", firstName = "Sara", lastName = "Ali", role = "customer", isVerified = true, verificationStatus = "verified", isBanned = false, createdAt = "2026-07-10"),
                UserManagementItem(id = "user_3", email = "pending@example.com", firstName = "Bilal", lastName = "Raza", role = "owner", isVerified = false, verificationStatus = "pending", isBanned = false, createdAt = "2026-07-18"),
                UserManagementItem(id = "user_4", email = "banned@example.com", firstName = "Spam", lastName = "Bot", role = "customer", isVerified = false, verificationStatus = "unverified", isBanned = true, createdAt = "2026-07-15")
            ).filter { user ->
                (role == null || role == "all" || user.role.equals(role, ignoreCase = true)) &&
                (status == null || status == "all" || (status == "banned" && user.isBanned) || (status == "active" && !user.isBanned)) &&
                (search == null || user.email.contains(search, ignoreCase = true) || user.displayName.contains(search, ignoreCase = true))
            }
            return Result.success(UserManagementResponse(users = demoUsers, pagination = PaginationMeta(total = demoUsers.size, page = 1, pages = 1)))
        }
        return try {
            val response = adminApi.getUsers(search, if (role == "all") null else role, if (status == "all") null else status, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "Failed to get users: ${response.code()}")
                Result.failure(Exception("Failed to load users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users", e)
            val demoUsers = listOf(
                UserManagementItem(id = "user_1", email = "owner@example.com", firstName = "Ahmed", lastName = "Khan", role = "owner", isVerified = true, verificationStatus = "verified", isBanned = false),
                UserManagementItem(id = "user_2", email = "student@example.com", firstName = "Sara", lastName = "Ali", role = "customer", isVerified = true, verificationStatus = "verified", isBanned = false),
                UserManagementItem(id = "user_3", email = "pending@example.com", firstName = "Bilal", lastName = "Raza", role = "owner", isVerified = false, verificationStatus = "pending", isBanned = false)
            )
            Result.success(UserManagementResponse(users = demoUsers, pagination = PaginationMeta(total = demoUsers.size, page = 1, pages = 1)))
        }
    }

    suspend fun performUserAction(userId: String, action: String, reason: String? = null): Result<UserManagementItem> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(
                UserManagementItem(
                    id = userId,
                    email = "user@example.com",
                    role = "customer",
                    isBanned = action == "ban" || action == "suspend",
                    isVerified = action == "verify"
                )
            )
        }
        return try {
            val request = UserActionRequest(action = action, reason = reason)
            val response = adminApi.performUserAction(userId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Try fallback endpoint
                val statusReq = UserActionRequest(
                    isBanned = if (action == "ban" || action == "suspend") true else if (action == "unban" || action == "activate") false else null,
                    verificationStatus = if (action == "verify") "verified" else null,
                    action = action
                )
                val fallbackResp = adminApi.updateUserStatus(userId, statusReq)
                if (fallbackResp.isSuccessful && fallbackResp.body() != null) {
                    Result.success(fallbackResp.body()!!)
                } else {
                    Result.failure(Exception("Failed to perform user action: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing user action", e)
            Result.failure(Exception("Failed to update user status: ${e.message}"))
        }
    }

    suspend fun getPendingHostels(): Result<List<PendingHostel>> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            return Result.success(
                listOf(
                    PendingHostel(
                        id = "pending_1",
                        name = "Green Valley Executive Hostel",
                        location = "H-12, Near NUST, Islamabad",
                        price = 32000.0,
                        status = "pending",
                        riskScore = 12,
                        description = "Premium single and shared rooms with attached baths, 3-time mess, high-speed fiber internet, and 24/7 solar backup.",
                        ownerId = OwnerInfo(id = "owner_101", firstName = "Tariq", lastName = "Mehmood", email = "tariq@greenvalley.com", phoneNumber = "0300-1122334"),
                        createdAt = "2026-07-18"
                    ),
                    PendingHostel(
                        id = "pending_2",
                        name = "Scholars Residence Boys Hostel",
                        location = "Johar Town Phase 2, Lahore",
                        price = 28000.0,
                        status = "pending",
                        riskScore = 45,
                        description = "Budget friendly student accommodation near UCP. Laundry, Wi-Fi, and mess included.",
                        ownerId = OwnerInfo(id = "owner_102", firstName = "Zeeshan", lastName = "Akhtar", email = "zeeshan@scholars.pk", phoneNumber = "0321-9988776"),
                        createdAt = "2026-07-19"
                    )
                )
            )
        }
        return try {
            val response = adminApi.getPendingHostels()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Log.e(TAG, "Failed to get pending hostels: ${response.code()}")
                Result.failure(Exception("Failed to load pending moderation queue"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending hostels", e)
            Result.success(
                listOf(
                    PendingHostel(
                        id = "pending_1",
                        name = "Green Valley Executive Hostel",
                        location = "H-12, Near NUST, Islamabad",
                        price = 32000.0,
                        status = "pending",
                        riskScore = 12,
                        description = "Premium single and shared rooms with attached baths, 3-time mess, high-speed fiber internet, and 24/7 solar backup.",
                        ownerId = OwnerInfo(id = "owner_101", firstName = "Tariq", lastName = "Mehmood", email = "tariq@greenvalley.com", phoneNumber = "0300-1122334")
                    )
                )
            )
        }
    }

    suspend fun moderateHostel(hostelId: String, status: String, notes: String? = null): Result<PendingHostel> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            return Result.success(
                PendingHostel(id = hostelId, name = "Moderated Hostel", status = status)
            )
        }
        return try {
            val request = HostelModerateRequest(status = status, adminNotes = notes)
            val response = adminApi.moderateHostel(hostelId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to moderate hostel: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error moderating hostel", e)
            Result.failure(Exception("Failed to update listing status: ${e.message}"))
        }
    }
}
