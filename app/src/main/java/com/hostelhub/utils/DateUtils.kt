package com.hostelhub.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date/time handling
 * Uses SimpleDateFormat for API 24 compatibility (instead of java.time which requires API 26)
 */
object DateUtils {

    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    private val dateOnlyFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val displayFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    private val displayTimeFormat: SimpleDateFormat
        get() = SimpleDateFormat("hh:mm a", Locale.US)

    private val displayDateTimeFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)

    /**
     * Get current timestamp in ISO 8601 format
     */
    fun getCurrentTimestamp(): String {
        return isoFormat.format(Date())
    }

    /**
     * Get current date in yyyy-MM-dd format
     */
    fun getCurrentDate(): String {
        return dateOnlyFormat.format(Date())
    }

    /**
     * Format ISO date string to display format (e.g., "Dec 16, 2025")
     */
    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Format ISO date string to display time format (e.g., "02:30 PM")
     */
    fun formatTime(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayTimeFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Format ISO date string to full display format (e.g., "Dec 16, 2025 at 02:30 PM")
     */
    fun formatDateTime(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            displayDateTimeFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday", "Dec 15")
     */
    fun getRelativeTime(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = isoFormat.parse(isoDate) ?: return isoDate
            val now = Date()
            val diffMs = now.time - date.time
            val diffMinutes = diffMs / (1000 * 60)
            val diffHours = diffMs / (1000 * 60 * 60)
            val diffDays = diffMs / (1000 * 60 * 60 * 24)

            when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                diffHours < 24 -> "${diffHours}h ago"
                diffDays < 2 -> "Yesterday"
                diffDays < 7 -> "${diffDays}d ago"
                else -> displayFormat.format(date)
            }
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Calculate duration between two dates in months
     */
    fun calculateDurationMonths(startDate: String?, endDate: String?): Int {
        if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) return 0
        return try {
            val start = dateOnlyFormat.parse(startDate) ?: return 0
            val end = dateOnlyFormat.parse(endDate) ?: return 0
            val diffMs = end.time - start.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            (diffDays / 30).toInt().coerceAtLeast(1)
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Parse date string to Date object
     */
    fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null
        return try {
            isoFormat.parse(dateString)
        } catch (e: Exception) {
            try {
                dateOnlyFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }
}

