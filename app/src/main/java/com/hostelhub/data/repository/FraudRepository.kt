package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FraudRepository"

@Singleton
class FraudRepository @Inject constructor(
    private val fraudApi: FraudApi
) {
    private val demoReports = mutableListOf<FraudReport>()

    suspend fun submitFraudReport(
        reportedUserId: String? = null,
        hostelId: String? = null,
        type: FraudType,
        description: String,
        evidence: FraudReportEvidence? = null
    ): Result<FraudReport> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            val newReport = FraudReport(
                id = "report_${System.currentTimeMillis()}",
                reporterId = "demo_user",
                reportedUserId = reportedUserId,
                hostelId = hostelId,
                reporter = FraudReportUserInfo(id = "demo_user", firstName = "Demo", lastName = "User"),
                reportedUser = reportedUserId?.let { FraudReportUserInfo(id = it, firstName = "Reported", lastName = "User") },
                hostel = hostelId?.let { FraudReportHostelInfo(id = it, name = "Reported Hostel", location = "Lahore") },
                type = type,
                description = description,
                evidence = evidence,
                status = FraudReportStatus.PENDING,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoReports.add(newReport)
            return Result.success(newReport)
        }

        return try {
            val request = CreateFraudReportRequest(
                reportedUserId = reportedUserId,
                hostelId = hostelId,
                type = type.name.lowercase(),
                description = description,
                evidence = evidence?.let { FraudEvidence(it.images, it.urls, it.screenshots) }
            )
            val response = fraudApi.submitFraudReport(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit report"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting report", e)
            Result.failure(Exception("Failed to submit report: ${e.message}"))
        }
    }

    suspend fun getMyFraudReports(): Result<List<FraudReport>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoReports)
        }

        return try {
            val response = fraudApi.getMyFraudReports()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch reports"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching reports", e)
            Result.failure(Exception("Failed to fetch reports: ${e.message}"))
        }
    }

    suspend fun getFraudReport(id: String): Result<FraudReport> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val report = demoReports.find { it.id == id }
            return if (report != null) {
                Result.success(report)
            } else {
                Result.failure(Exception("Report not found"))
            }
        }

        return try {
            val response = fraudApi.getFraudReport(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Report not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHostelRiskScore(hostelId: String): Result<RiskScoreResponse> {
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            // Generate demo risk analysis
            val demoAnalysis = AIAnalysis(
                imageScore = 5,
                textScore = 3,
                behaviorScore = 2,
                priceScore = 5,
                accountScore = 2,
                totalRiskScore = 17,
                riskLevel = RiskLevel.LOW,
                confidence = 85,
                flags = listOf("Verified owner", "Consistent pricing"),
                reasoning = "This listing appears legitimate with verified owner information and consistent details."
            )
            return Result.success(RiskScoreResponse(
                hostelId = hostelId,
                totalRiskScore = 17,
                riskLevel = "low",
                analysis = demoAnalysis,
                flags = listOf("Verified owner", "Consistent pricing")
            ))
        }

        return try {
            val response = fraudApi.getHostelRiskScore(hostelId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get risk score"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting risk score", e)
            Result.failure(Exception("Failed to get risk score: ${e.message}"))
        }
    }

    private fun seedDemoReportsIfNeeded() {
        if (demoReports.isEmpty()) {
            demoReports.addAll(
                listOf(
                    FraudReport(
                        id = "report_101",
                        reporterId = "student_ali",
                        hostelId = "hostel_lahore_1",
                        reporter = FraudReportUserInfo(id = "student_ali", firstName = "Ali", lastName = "Khan"),
                        hostel = FraudReportHostelInfo(id = "hostel_lahore_1", name = "Apex Boys Hostel", location = "Johar Town, Lahore"),
                        type = FraudType.DUPLICATE_IMAGES,
                        description = "The room photos match an existing hotel listing on Booking.com from Blue Area Islamabad.",
                        aiAnalysis = AIAnalysis(
                            imageScore = 25,
                            textScore = 10,
                            behaviorScore = 15,
                            priceScore = 5,
                            accountScore = 10,
                            totalRiskScore = 65,
                            riskLevel = RiskLevel.HIGH,
                            confidence = 94,
                            flags = listOf("Perceptual Hash Match (96%)", "Multiple Reports Filed"),
                            reasoning = "Perceptual image hashing detected identical photos across 3 separate accounts."
                        ),
                        status = FraudReportStatus.PENDING,
                        createdAt = "2026-07-18T14:30:00Z"
                    ),
                    FraudReport(
                        id = "report_102",
                        reporterId = "student_zara",
                        hostelId = "hostel_isb_3",
                        reporter = FraudReportUserInfo(id = "student_zara", firstName = "Zara", lastName = "Ahmed"),
                        hostel = FraudReportHostelInfo(id = "hostel_isb_3", name = "Sunrise Girls Enclave", location = "H-12, Islamabad"),
                        type = FraudType.PRICE_MANIPULATION,
                        description = "Listed rent is PKR 22,000 but the caretaker demanded PKR 35,000 cash upon arrival plus mandatory security deposit without receipt.",
                        aiAnalysis = AIAnalysis(
                            imageScore = 0,
                            textScore = 15,
                            behaviorScore = 12,
                            priceScore = 15,
                            accountScore = 5,
                            totalRiskScore = 47,
                            riskLevel = RiskLevel.MEDIUM,
                            confidence = 88,
                            flags = listOf("Off-platform Payment Request", "Price Variance"),
                            reasoning = "NLP analysis flagged keywords associated with cash extortion and hidden charges."
                        ),
                        status = FraudReportStatus.INVESTIGATING,
                        createdAt = "2026-07-17T09:15:00Z"
                    )
                )
            )
        }
    }

    suspend fun getAllFraudReports(
        status: String? = null,
        riskLevel: String? = null
    ): Result<List<FraudReport>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            seedDemoReportsIfNeeded()
            var filtered = demoReports.toList()
            if (status != null) {
                filtered = filtered.filter { it.status.name.equals(status, ignoreCase = true) }
            }
            if (riskLevel != null) {
                filtered = filtered.filter { it.aiAnalysis?.riskLevel?.name?.equals(riskLevel, ignoreCase = true) == true }
            }
            return Result.success(filtered)
        }

        return try {
            val response = fraudApi.getAllFraudReports(status = status, riskLevel = riskLevel)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.reports)
            } else {
                Result.failure(Exception("Failed to fetch all fraud reports"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all fraud reports", e)
            Result.failure(Exception("Failed to fetch all fraud reports: ${e.message}"))
        }
    }

    suspend fun updateFraudReportStatus(
        id: String,
        newStatus: FraudReportStatus,
        notes: String? = null
    ): Result<FraudReport> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            seedDemoReportsIfNeeded()
            val index = demoReports.indexOfFirst { it.id == id }
            if (index != -1) {
                val updated = demoReports[index].copy(
                    status = newStatus,
                    adminNotes = notes ?: demoReports[index].adminNotes,
                    resolvedAt = if (newStatus == FraudReportStatus.CONFIRMED || newStatus == FraudReportStatus.DISMISSED) DateUtils.getCurrentTimestamp() else demoReports[index].resolvedAt
                )
                demoReports[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Report not found in demo storage"))
        }

        return try {
            val request = UpdateFraudReportStatusRequest(
                status = newStatus.name.lowercase(),
                adminNotes = notes
            )
            val response = fraudApi.updateFraudReportStatus(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update report status"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fraud report status", e)
            Result.failure(Exception("Failed to update status: ${e.message}"))
        }
    }

    suspend fun deleteFraudReport(id: String): Result<Boolean> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            seedDemoReportsIfNeeded()
            demoReports.removeAll { it.id == id }
            return Result.success(true)
        }

        return try {
            val response = fraudApi.deleteFraudReport(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete report"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting report", e)
            Result.failure(Exception("Failed to delete report: ${e.message}"))
        }
    }

    suspend fun getFraudStats(): Result<FraudStats> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            seedDemoReportsIfNeeded()
            val pending = demoReports.count { it.status == FraudReportStatus.PENDING }
            val investigating = demoReports.count { it.status == FraudReportStatus.INVESTIGATING }
            val confirmed = demoReports.count { it.status == FraudReportStatus.CONFIRMED }
            val dismissed = demoReports.count { it.status == FraudReportStatus.DISMISSED }
            val highRisk = demoReports.count { it.aiAnalysis?.riskLevel == RiskLevel.HIGH || it.aiAnalysis?.riskLevel == RiskLevel.CRITICAL }
            return Result.success(
                FraudStats(
                    totalReports = demoReports.size,
                    pendingReports = pending,
                    investigatingReports = investigating,
                    confirmedReports = confirmed,
                    dismissedReports = dismissed,
                    highRiskHostels = highRisk
                )
            )
        }

        return try {
            val response = fraudApi.getFraudStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch fraud stats"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching fraud stats", e)
            Result.failure(Exception("Failed to fetch stats: ${e.message}"))
        }
    }

    suspend fun getFlaggedHostels(): Result<List<FraudReport>> {
        if (BuildConfig.DEMO_MODE) {
            delay(400)
            seedDemoReportsIfNeeded()
            val flagged = demoReports.filter {
                it.status == FraudReportStatus.PENDING || it.status == FraudReportStatus.INVESTIGATING
            }
            return Result.success(flagged)
        }

        return try {
            val response = fraudApi.getFlaggedHostels()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch flagged hostels"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching flagged hostels", e)
            Result.failure(Exception("Failed to fetch flagged hostels: ${e.message}"))
        }
    }
}

