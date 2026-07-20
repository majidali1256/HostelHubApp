package com.hostelhub.data.api

import com.hostelhub.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Booking API endpoints
 */
interface BookingApi {

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<Booking>

    @GET("bookings")
    suspend fun getAllBookings(): Response<List<Booking>>

    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): Response<List<Booking>>

    @GET("bookings/my-hostel-bookings")
    suspend fun getMyHostelBookings(): Response<List<Booking>>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: String): Response<Booking>

    @POST("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") id: String): Response<Booking>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") id: String,
        @Body request: CancelBookingRequest
    ): Response<Booking>

    @POST("bookings/check-availability")
    suspend fun checkAvailability(@Body request: CheckAvailabilityRequest): Response<AvailabilityResponse>

    @GET("hostels/{id}/available-dates")
    suspend fun getAvailableDates(
        @Path("id") hostelId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<AvailableDatesResponse>

    @Multipart
    @POST("bookings/{id}/payment-receipt")
    suspend fun uploadPaymentReceipt(
        @Path("id") bookingId: String,
        @Part receipt: MultipartBody.Part,
        @Part("transactionId") transactionId: okhttp3.RequestBody,
        @Part("paymentMethod") paymentMethod: okhttp3.RequestBody
    ): Response<Booking>

    @PUT("bookings/{id}/verify-payment")
    suspend fun verifyPayment(
        @Path("id") bookingId: String,
        @Body request: VerifyPaymentRequest
    ): Response<Booking>

    @PUT("bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") bookingId: String,
        @Body request: UpdateBookingStatusRequest
    ): Response<Booking>

    @POST("bookings/{id}/pay-online")
    suspend fun payOnline(
        @Path("id") bookingId: String,
        @Body request: PayOnlineRequest
    ): Response<OnlinePaymentResponse>
}

data class CreateBookingRequest(
    val hostelId: String,
    val checkIn: String,
    val checkOut: String,
    val numberOfGuests: Int,
    val specialRequests: String? = null
)

data class CancelBookingRequest(
    val reason: String? = null
)

data class CheckAvailabilityRequest(
    val hostelId: String,
    val checkIn: String,
    val checkOut: String
)

data class AvailabilityResponse(
    val available: Boolean,
    val reason: String? = null
)

data class AvailableDatesResponse(
    val availableDates: List<String>,
    val blockedDates: List<String>
)

data class VerifyPaymentRequest(
    @com.google.gson.annotations.SerializedName("verified") val verified: Boolean,
    @com.google.gson.annotations.SerializedName("approved") val approved: Boolean = verified,
    @com.google.gson.annotations.SerializedName("rejectionReason") val rejectionReason: String? = null
)

data class UpdateBookingStatusRequest(
    val status: String,
    val reason: String? = null
)

data class PayOnlineRequest(
    val paymentMethod: String,
    val amount: Double
)

data class OnlinePaymentResponse(
    val success: Boolean,
    val message: String,
    val paymentUrl: String? = null,
    val transactionId: String? = null
)

