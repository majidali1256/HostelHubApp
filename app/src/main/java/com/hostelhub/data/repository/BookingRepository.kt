package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.hostelhub.utils.DateUtils
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "BookingRepository"

@Singleton
class BookingRepository @Inject constructor(
    private val bookingApi: BookingApi
) {

    // Demo bookings for testing
    private val demoBookings = mutableListOf<Booking>()

    suspend fun getMyBookings(): Result<List<Booking>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoBookings.filter { it.customerId?.id == "demo_user" })
        }

        return try {
            val response = bookingApi.getMyBookings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch bookings"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bookings", e)
            Result.failure(Exception("Failed to fetch bookings: ${e.message}"))
        }
    }

    suspend fun getMyHostelBookings(): Result<List<Booking>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(demoBookings.filter { it.hostelId?.id?.startsWith("demo") == true })
        }

        return try {
            val response = bookingApi.getMyHostelBookings()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch hostel bookings"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hostel bookings", e)
            Result.failure(Exception("Failed to fetch hostel bookings: ${e.message}"))
        }
    }
    
    suspend fun getBookingById(id: String): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val booking = demoBookings.find { it.id == id }
            return if (booking != null) {
                Result.success(booking)
            } else {
                Result.failure(Exception("Booking not found"))
            }
        }

        return try {
            val response = bookingApi.getBookingById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Booking not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createBooking(
        hostelId: String,
        checkIn: String,
        checkOut: String,
        numberOfGuests: Int,
        specialRequests: String? = null
    ): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            val demoBooking = Booking(
                id = "demo_booking_${System.currentTimeMillis()}",
                hostelId = BookingHostelInfo(
                    id = hostelId,
                    name = "Demo Hostel",
                    location = "Lahore, Pakistan",
                    price = 15000.0,
                    images = listOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800")
                ),
                customerId = BookingUserInfo(
                    id = "demo_user",
                    firstName = "Demo",
                    lastName = "User",
                    email = "demo@example.com"
                ),
                checkIn = checkIn,
                checkOut = checkOut,
                status = BookingStatus.PENDING,
                totalPrice = 45000.0,
                numberOfGuests = numberOfGuests,
                specialRequests = specialRequests,
                paymentStatus = PaymentStatus.PENDING,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoBookings.add(demoBooking)
            return Result.success(demoBooking)
        }

        return try {
            val request = CreateBookingRequest(hostelId, checkIn, checkOut, numberOfGuests, specialRequests)
            val response = bookingApi.createBooking(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val error = response.errorBody()?.string() ?: "Failed to create booking"
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating booking", e)
            Result.failure(Exception("Failed to create booking: ${e.message}"))
        }
    }
    
    suspend fun confirmBooking(id: String): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoBookings.indexOfFirst { it.id == id }
            if (index >= 0) {
                val updated = demoBookings[index].copy(
                    status = BookingStatus.CONFIRMED,
                    confirmedAt = DateUtils.getCurrentTimestamp()
                )
                demoBookings[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Booking not found"))
        }

        return try {
            val response = bookingApi.confirmBooking(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to confirm booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelBooking(id: String, reason: String? = null): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoBookings.indexOfFirst { it.id == id }
            if (index >= 0) {
                val updated = demoBookings[index].copy(
                    status = BookingStatus.CANCELLED,
                    cancelledAt = DateUtils.getCurrentTimestamp(),
                    cancelReason = reason
                )
                demoBookings[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Booking not found"))
        }

        return try {
            val response = bookingApi.cancelBooking(id, CancelBookingRequest(reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to cancel booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun checkAvailability(hostelId: String, checkIn: String, checkOut: String): Result<AvailabilityResponse> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            return Result.success(AvailabilityResponse(available = true))
        }

        return try {
            val response = bookingApi.checkAvailability(CheckAvailabilityRequest(hostelId, checkIn, checkOut))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check availability"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPaymentReceipt(
        bookingId: String,
        receiptFile: File,
        transactionId: String,
        paymentMethod: String
    ): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            val index = demoBookings.indexOfFirst { it.id == bookingId }
            if (index >= 0) {
                val updated = demoBookings[index].copy(
                    paymentStatus = PaymentStatus.SUBMITTED,
                    paymentMethod = paymentMethod,
                    transactionId = transactionId,
                    paymentReceipt = PaymentReceipt(
                        image = "demo_receipt.jpg",
                        uploadedAt = DateUtils.getCurrentTimestamp()
                    )
                )
                demoBookings[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Booking not found"))
        }

        return try {
            val receiptPart = MultipartBody.Part.createFormData(
                "receipt",
                receiptFile.name,
                receiptFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            val transactionIdBody = transactionId.toRequestBody("text/plain".toMediaTypeOrNull())
            val paymentMethodBody = paymentMethod.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = bookingApi.uploadPaymentReceipt(
                bookingId, receiptPart, transactionIdBody, paymentMethodBody
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to upload receipt"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading receipt", e)
            Result.failure(Exception("Failed to upload receipt: ${e.message}"))
        }
    }
    
    suspend fun verifyPayment(bookingId: String, verified: Boolean, rejectionReason: String? = null): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoBookings.indexOfFirst { it.id == bookingId }
            if (index >= 0) {
                val updated = demoBookings[index].copy(
                    paymentStatus = if (verified) PaymentStatus.VERIFIED else PaymentStatus.REJECTED,
                    paymentReceipt = demoBookings[index].paymentReceipt?.copy(
                        verified = verified,
                        verifiedAt = DateUtils.getCurrentTimestamp(),
                        rejectionReason = rejectionReason
                    )
                )
                demoBookings[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Booking not found"))
        }

        return try {
            val response = bookingApi.verifyPayment(
                bookingId, 
                VerifyPaymentRequest(
                    verified = verified,
                    approved = verified,
                    rejectionReason = rejectionReason
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to verify payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String, reason: String? = null): Result<Booking> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoBookings.indexOfFirst { it.id == bookingId }
            if (index >= 0) {
                val newStatus = when (status.lowercase()) {
                    "confirmed" -> BookingStatus.CONFIRMED
                    "cancelled" -> BookingStatus.CANCELLED
                    "completed" -> BookingStatus.COMPLETED
                    "rejected" -> BookingStatus.REJECTED
                    else -> BookingStatus.PENDING
                }
                val updated = demoBookings[index].copy(status = newStatus, cancelReason = reason)
                demoBookings[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Booking not found"))
        }

        return try {
            val response = bookingApi.updateBookingStatus(bookingId, UpdateBookingStatusRequest(status, reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update booking status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun payOnline(bookingId: String, paymentMethod: String, amount: Double): Result<OnlinePaymentResponse> {
        if (BuildConfig.DEMO_MODE) {
            delay(1000)
            return Result.success(OnlinePaymentResponse(success = true, message = "Payment successful (Demo Mode)", transactionId = "TXN_${System.currentTimeMillis()}"))
        }

        return try {
            val response = bookingApi.payOnline(bookingId, PayOnlineRequest(paymentMethod, amount))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to initiate online payment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
