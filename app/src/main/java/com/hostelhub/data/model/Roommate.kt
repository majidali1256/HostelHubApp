package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Roommate matching preferences and models
 */
data class RoommatePreferences(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String? = null,
    val userId: String = "",
    val bio: String? = null,
    val age: Int? = null,
    val gender: String? = null, // "male", "female", "non-binary", "prefer-not-to-say"
    val occupation: String? = null,

    // Lifestyle
    val sleepSchedule: String? = null, // "early-bird", "night-owl", "flexible"
    val cleanliness: Int? = null, // 1-5
    val socialLevel: Int? = null, // 1-5

    // Habits
    val smoking: String? = null, // "yes", "no", "occasionally"
    val drinking: String? = null, // "yes", "no", "occasionally"
    val pets: String? = null, // "yes", "no", "allergic"

    // Preferences
    val preferredGender: List<String>? = null,
    val preferredAgeRange: AgeRange? = null,
    val dealBreakers: List<String>? = null,
    val interests: List<String>? = null,
    val languages: List<String>? = null,

    // Availability
    val lookingForRoommate: Boolean = false,
    val moveInDate: String? = null,
    val budgetRange: BudgetRange? = null,

    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AgeRange(
    val min: Int = 18,
    val max: Int = 50
)

data class BudgetRange(
    val min: Double = 5000.0,
    val max: Double = 50000.0
)

/**
 * Roommate match result from AI matching
 */
data class RoommateMatch(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val userId: String = "",
    val matchedUserId: String = "",
    val user: RoommateMatchUser? = null,
    val matchScore: Double = 0.0, // 0-100
    val matchDetails: MatchDetails? = null,
    val status: MatchStatus = MatchStatus.PENDING,
    val createdAt: String? = null
)

data class RoommateMatchUser(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val email: String? = null,
    val preferences: RoommatePreferences? = null
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            else -> "User"
        }
}

data class MatchDetails(
    val lifestyleScore: Double = 0.0,
    val habitsScore: Double = 0.0,
    val interestsScore: Double = 0.0,
    val budgetMatch: Boolean = false,
    val commonInterests: List<String>? = null,
    val compatibilityReasons: List<String>? = null,
    val potentialIssues: List<String>? = null
)

enum class MatchStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("accepted") ACCEPTED,
    @SerializedName("rejected") REJECTED,
    @SerializedName("mutual") MUTUAL // Both users accepted
}

// Sleep schedule options
object SleepSchedule {
    const val EARLY_BIRD = "early-bird"
    const val NIGHT_OWL = "night-owl"
    const val FLEXIBLE = "flexible"

    val options = listOf(
        EARLY_BIRD to "Early Bird (Sleep early, wake early)",
        NIGHT_OWL to "Night Owl (Sleep late, wake late)",
        FLEXIBLE to "Flexible (Adaptable schedule)"
    )
}

// Habit options
object HabitOptions {
    const val YES = "yes"
    const val NO = "no"
    const val OCCASIONALLY = "occasionally"

    val options = listOf(YES to "Yes", NO to "No", OCCASIONALLY to "Occasionally")
}

// Common interests for roommate matching
object CommonInterests {
    val list = listOf(
        "Sports", "Gaming", "Reading", "Movies", "Music", "Cooking",
        "Travel", "Photography", "Fitness", "Technology", "Art",
        "Cricket", "Football", "Hiking", "Yoga", "Meditation"
    )
}

// Languages commonly spoken in Pakistan
object PakistaniLanguages {
    val list = listOf(
        "Urdu", "English", "Punjabi", "Sindhi", "Pashto",
        "Balochi", "Saraiki", "Hindko", "Brahui"
    )
}

