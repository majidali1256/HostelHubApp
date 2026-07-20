package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Admin & Moderation API endpoints (Module 2)
 */
interface AdminApi {
    @GET("admin/stats")
    suspend fun getAdminStats(): Response<AdminStats>

    @GET("admin/users")
    suspend fun getUsers(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1
    ): Response<UserManagementResponse>

    @POST("admin/users/{id}/action")
    suspend fun performUserAction(
        @Path("id") id: String,
        @Body request: UserActionRequest
    ): Response<UserManagementItem>

    @PUT("admin/users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") id: String,
        @Body request: UserActionRequest
    ): Response<UserManagementItem>

    @GET("hostels/admin/pending")
    suspend fun getPendingHostels(): Response<List<PendingHostel>>

    @PUT("hostels/admin/moderate/{id}")
    suspend fun moderateHostel(
        @Path("id") id: String,
        @Body request: HostelModerateRequest
    ): Response<PendingHostel>
}
