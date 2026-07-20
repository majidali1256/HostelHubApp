package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

/**
 * Fraud report and detection models
 */
data class FraudReport(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val reporterId: String = "",
    val reportedUserId: String? = null,
    val hostelId: String? = null,
    val reporter: FraudReportUserInfo? = null,
    val reportedUser: FraudReportUserInfo? = null,
    val hostel: FraudReportHostelInfo? = null,
    val type: FraudType = FraudType.OTHER,
    val description: String = "",
    val evidence: FraudReportEvidence? = null,
    val aiAnalysis: AIAnalysis? = null,
    val status: FraudReportStatus = FraudReportStatus.PENDING,
    val adminNotes: String? = null,
    val createdAt: String? = null,
    val resolvedAt: String? = null
)

data class FraudReportUserInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val profilePicture: String? = null
)

data class FraudReportHostelInfo(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val images: List<String>? = null
)

data class FraudReportEvidence(
    val images: List<String>? = null,
    val urls: List<String>? = null,
    val screenshots: List<String>? = null
)

/**
 * AI-powered fraud analysis result
 */
data class AIAnalysis(
    val imageScore: Int = 0, // 0-30
    val textScore: Int = 0, // 0-25
    val behaviorScore: Int = 0, // 0-20
    val priceScore: Int = 0, // 0-15
    val accountScore: Int = 0, // 0-10
    val totalRiskScore: Int = 0, // 0-100
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val confidence: Int = 0,
    val flags: List<String> = emptyList(),
    val reasoning: String? = null
)

enum class FraudType {
    @SerializedName("fake_listing") FAKE_LISTING,
    @SerializedName("duplicate_images") DUPLICATE_IMAGES,
    @SerializedName("suspicious_text") SUSPICIOUS_TEXT,
    @SerializedName("scam") SCAM,
    @SerializedName("impersonation") IMPERSONATION,
    @SerializedName("price_manipulation") PRICE_MANIPULATION,
    @SerializedName("other") OTHER
}

enum class FraudReportStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("investigating") INVESTIGATING,
    @SerializedName("confirmed") CONFIRMED,
    @SerializedName("dismissed") DISMISSED
}

enum class RiskLevel {
    @SerializedName("low") LOW,
    @SerializedName("medium") MEDIUM,
    @SerializedName("high") HIGH,
    @SerializedName("critical") CRITICAL
}

// Fraud type display info
object FraudTypeInfo {
    val types = listOf(
        FraudType.FAKE_LISTING to ("Fake Listing" to "The listing appears to be fraudulent or non-existent"),
        FraudType.DUPLICATE_IMAGES to ("Duplicate Images" to "Images are copied from other listings"),
        FraudType.SUSPICIOUS_TEXT to ("Suspicious Content" to "Description contains misleading information"),
        FraudType.SCAM to ("Scam" to "Attempted to defraud money or personal information"),
        FraudType.IMPERSONATION to ("Impersonation" to "Pretending to be someone else"),
        FraudType.PRICE_MANIPULATION to ("Price Manipulation" to "Price significantly different from actual"),
        FraudType.OTHER to ("Other" to "Other type of fraud or suspicious activity")
    )

    fun getDisplayName(type: FraudType): String {
        return types.find { it.first == type }?.second?.first ?: "Unknown"
    }

    fun getDescription(type: FraudType): String {
        return types.find { it.first == type }?.second?.second ?: ""
    }
}

// Risk level colors and descriptions
object RiskLevelInfo {
    fun getColor(level: RiskLevel): Long {
        return when (level) {
            RiskLevel.LOW -> 0xFF22C55E // Green
            RiskLevel.MEDIUM -> 0xFFF59E0B // Amber
            RiskLevel.HIGH -> 0xFFEF4444 // Red
            RiskLevel.CRITICAL -> 0xFF7F1D1D // Dark Red
        }
    }

    fun getDescription(level: RiskLevel): String {
        return when (level) {
            RiskLevel.LOW -> "Low risk - appears legitimate"
            RiskLevel.MEDIUM -> "Medium risk - some concerns identified"
            RiskLevel.HIGH -> "High risk - multiple red flags detected"
            RiskLevel.CRITICAL -> "Critical risk - likely fraudulent"
        }
    }
}

