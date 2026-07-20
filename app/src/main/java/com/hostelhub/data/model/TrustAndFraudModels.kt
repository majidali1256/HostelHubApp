package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Trust Score detail model returned by backend /api/users/:id/trust-score
 */
data class TrustScoreDetail(
    @SerializedName(value = "userId", alternate = ["_id", "id"])
    val userId: String = "",
    val score: Int = 50,
    val level: String = "member",
    val factors: TrustScoreFactors? = null,
    val badges: List<TrustBadge> = emptyList(),
    val lastCalculated: String? = null
) {
    fun getFormattedScore(): String = "$score/100"

    fun getLevelTitle(): String = when (level.lowercase()) {
        "verified_owner", "verified" -> "Verified Partner"
        "trusted_owner", "trusted" -> "Super Host"
        "high_trust" -> "High Trust Member"
        "low_trust" -> "Needs Verification"
        else -> "Standard Member"
    }
}

data class TrustScoreFactors(
    val verifiedEmail: Boolean = false,
    val verifiedPhone: Boolean = false,
    val verifiedId: Boolean = false,
    val documentStatus: String = "none",
    val reportsCount: Int = 0,
    val completedBookings: Int = 0,
    val cancellationRate: Int = 0,
    val positiveReviews: Int = 0
)

data class TrustBadge(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val description: String = ""
)

data class UserBadgesResponse(
    val badges: List<TrustBadge> = emptyList()
)

/**
 * Admin statistics for fraud detection dashboard
 */
data class FraudStats(
    val totalReports: Int = 0,
    val pendingReports: Int = 0,
    val investigatingReports: Int = 0,
    val confirmedReports: Int = 0,
    val dismissedReports: Int = 0,
    val highRiskHostels: Int = 0
)

data class UpdateFraudReportStatusRequest(
    val status: String, // "investigating", "confirmed", "dismissed"
    val adminNotes: String? = null
)

data class FraudReportsListResponse(
    val reports: List<FraudReport> = emptyList(),
    val total: Int = 0
)
