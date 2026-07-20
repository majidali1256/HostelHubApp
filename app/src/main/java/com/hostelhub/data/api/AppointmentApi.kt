package com.hostelhub.data.api

import com.hostelhub.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Appointments API endpoints
 */
interface AppointmentApi {

    @POST("appointments")
    suspend fun createAppointment(@Body request: CreateAppointmentRequest): Response<Appointment>

    @GET("appointments")
    suspend fun getAppointments(
        @Query("status") status: String? = null,
        @Query("upcoming") upcoming: Boolean? = null
    ): Response<List<Appointment>>

    @GET("appointments/{id}")
    suspend fun getAppointment(@Path("id") id: String): Response<Appointment>

    @PATCH("appointments/{id}")
    suspend fun updateAppointment(
        @Path("id") id: String,
        @Body request: UpdateAppointmentRequest
    ): Response<Appointment>

    @PATCH("appointments/{id}/confirm")
    suspend fun confirmAppointment(@Path("id") id: String): Response<Appointment>

    @PATCH("appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") id: String,
        @Body request: CancelAppointmentRequest? = null
    ): Response<Appointment>
}

data class CreateAppointmentRequest(
    val hostelId: String,
    val ownerId: String,
    val scheduledTime: String,
    val duration: Int? = 60,
    val type: String = "viewing",
    val notes: String? = null,
    val location: String? = null
)

data class UpdateAppointmentRequest(
    val status: String? = null,
    val scheduledTime: String? = null,
    val notes: String? = null
)

data class CancelAppointmentRequest(
    val reason: String? = null
)

