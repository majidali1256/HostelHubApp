package com.hostelhub.data.model

import com.google.gson.annotations.SerializedName

data class PredictRentRequest(
    @SerializedName("location") val location: String,
    @SerializedName("roomType") val roomType: String = "Single",
    @SerializedName("capacity") val capacity: Int = 1,
    @SerializedName("amenities") val amenities: List<String> = emptyList(),
    @SerializedName("genderPreference") val genderPreference: String = "any"
)

data class PredictRentResponse(
    @SerializedName("estimatedPrice") val estimatedPrice: Double = 35000.0,
    @SerializedName("minPrice") val minPrice: Double = 33000.0,
    @SerializedName("maxPrice") val maxPrice: Double = 37000.0,
    @SerializedName("fairnessLabel") val fairnessLabel: String = "Fair Price",
    @SerializedName("confidenceScore") val confidenceScore: Int = 92,
    @SerializedName("reasoning") val reasoning: String = "Based on market trends for this sector and selected amenities.",
    @SerializedName("marketBenchmarks") val marketBenchmarks: Map<String, Any>? = null
)

data class FairnessRange(
    @SerializedName("min") val min: Double = 0.0,
    @SerializedName("max") val max: Double = 0.0
)

data class FairnessAnalysisResponse(
    @SerializedName("hostelPrice") val hostelPrice: Double = 0.0,
    @SerializedName("predictedRange") val predictedRange: FairnessRange = FairnessRange(),
    @SerializedName("fairnessLabel") val fairnessLabel: String = "Fair Price",
    @SerializedName("reasoning") val reasoning: String = ""
)

data class MarketBenchmarksResponse(
    @SerializedName("location") val location: String = "",
    @SerializedName("averagePrice") val averagePrice: Double = 0.0,
    @SerializedName("minMarketPrice") val minMarketPrice: Double = 0.0,
    @SerializedName("maxMarketPrice") val maxMarketPrice: Double = 0.0,
    @SerializedName("priceByRoomType") val priceByRoomType: Map<String, Double> = emptyMap()
)
