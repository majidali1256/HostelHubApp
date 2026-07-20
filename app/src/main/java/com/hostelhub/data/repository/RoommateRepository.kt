package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RoommateRepository"

@Singleton
class RoommateRepository @Inject constructor(
    private val roommateApi: RoommateApi
) {
    private var demoPreferences: RoommatePreferences? = null
    private val demoMatches = mutableListOf<RoommateMatch>()

    init {
        if (BuildConfig.DEMO_MODE) {
            initDemoData()
        }
    }

    private fun initDemoData() {
        // Create some demo matches
        demoMatches.addAll(listOf(
            RoommateMatch(
                id = "match_1",
                userId = "demo_user",
                matchedUserId = "user_ali",
                user = RoommateMatchUser(
                    id = "user_ali",
                    firstName = "Ali",
                    lastName = "Hassan",
                    profilePicture = null,
                    preferences = RoommatePreferences(
                        userId = "user_ali",
                        bio = "Computer Science student at LUMS",
                        age = 22,
                        gender = "male",
                        occupation = "Student",
                        sleepSchedule = "night-owl",
                        cleanliness = 4,
                        socialLevel = 3,
                        smoking = "no",
                        drinking = "no",
                        interests = listOf("Gaming", "Cricket", "Technology"),
                        languages = listOf("Urdu", "English", "Punjabi"),
                        lookingForRoommate = true,
                        budgetRange = BudgetRange(10000.0, 20000.0)
                    )
                ),
                matchScore = 85.0,
                matchDetails = MatchDetails(
                    lifestyleScore = 80.0,
                    habitsScore = 90.0,
                    interestsScore = 85.0,
                    budgetMatch = true,
                    commonInterests = listOf("Gaming", "Cricket"),
                    compatibilityReasons = listOf("Similar sleep schedule", "Both non-smokers", "Shared interests in gaming"),
                    potentialIssues = listOf("Different cleanliness standards")
                ),
                status = MatchStatus.PENDING
            ),
            RoommateMatch(
                id = "match_2",
                userId = "demo_user",
                matchedUserId = "user_usman",
                user = RoommateMatchUser(
                    id = "user_usman",
                    firstName = "Usman",
                    lastName = "Ahmed",
                    profilePicture = null,
                    preferences = RoommatePreferences(
                        userId = "user_usman",
                        bio = "Software Engineer working in Lahore",
                        age = 25,
                        gender = "male",
                        occupation = "Software Engineer",
                        sleepSchedule = "early-bird",
                        cleanliness = 5,
                        socialLevel = 2,
                        smoking = "no",
                        drinking = "no",
                        interests = listOf("Reading", "Fitness", "Technology"),
                        languages = listOf("Urdu", "English"),
                        lookingForRoommate = true,
                        budgetRange = BudgetRange(15000.0, 30000.0)
                    )
                ),
                matchScore = 72.0,
                matchDetails = MatchDetails(
                    lifestyleScore = 65.0,
                    habitsScore = 95.0,
                    interestsScore = 60.0,
                    budgetMatch = true,
                    commonInterests = listOf("Technology"),
                    compatibilityReasons = listOf("Both non-smokers", "Clean and organized"),
                    potentialIssues = listOf("Different sleep schedules", "Different social preferences")
                ),
                status = MatchStatus.PENDING
            )
        ))
    }

    suspend fun getPreferences(): Result<RoommatePreferences?> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoPreferences)
        }

        return try {
            val response = roommateApi.getPreferences()
            if (response.isSuccessful) {
                Result.success(response.body())
            } else if (response.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(Exception("Failed to fetch preferences"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching preferences", e)
            Result.failure(Exception("Failed to fetch preferences: ${e.message}"))
        }
    }

    suspend fun savePreferences(preferences: RoommatePreferences): Result<RoommatePreferences> {
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            demoPreferences = preferences.copy(
                userId = "demo_user",
                updatedAt = DateUtils.getCurrentTimestamp()
            )
            return Result.success(demoPreferences!!)
        }

        return try {
            val response = roommateApi.savePreferences(preferences)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to save preferences"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving preferences", e)
            Result.failure(Exception("Failed to save preferences: ${e.message}"))
        }
    }

    suspend fun getMatches(): Result<List<RoommateMatch>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoMatches.sortedByDescending { it.matchScore })
        }

        return try {
            val response = roommateApi.getMatches()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch matches"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching matches", e)
            Result.failure(Exception("Failed to fetch matches: ${e.message}"))
        }
    }

    suspend fun acceptMatch(matchId: String): Result<RoommateMatch> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoMatches.indexOfFirst { it.id == matchId }
            if (index >= 0) {
                val updated = demoMatches[index].copy(status = MatchStatus.ACCEPTED)
                demoMatches[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Match not found"))
        }

        return try {
            val response = roommateApi.acceptMatch(matchId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to accept match"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectMatch(matchId: String): Result<RoommateMatch> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoMatches.indexOfFirst { it.id == matchId }
            if (index >= 0) {
                val updated = demoMatches[index].copy(status = MatchStatus.REJECTED)
                demoMatches[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Match not found"))
        }

        return try {
            val response = roommateApi.rejectMatch(matchId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to reject match"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

