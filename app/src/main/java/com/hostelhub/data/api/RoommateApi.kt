package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Roommate Matching API endpoints
 */
interface RoommateApi {

    @GET("roommate/preferences")
    suspend fun getPreferences(): Response<RoommatePreferences>

    @POST("roommate/preferences")
    suspend fun savePreferences(@Body preferences: RoommatePreferences): Response<RoommatePreferences>

    @GET("roommate/matches")
    suspend fun getMatches(): Response<List<RoommateMatch>>

    @PATCH("roommate/matches/{id}")
    suspend fun updateMatchStatus(
        @Path("id") matchId: String,
        @Body request: UpdateMatchStatusRequest
    ): Response<RoommateMatch>

    @POST("roommate/matches/{id}/accept")
    suspend fun acceptMatch(@Path("id") matchId: String): Response<RoommateMatch>

    @POST("roommate/matches/{id}/reject")
    suspend fun rejectMatch(@Path("id") matchId: String): Response<RoommateMatch>
}

data class UpdateMatchStatusRequest(
    val action: String // "accept" or "decline"
)

