package com.hostelhub.data.repository

import android.util.Log
import com.hostelhub.BuildConfig
import com.hostelhub.data.api.*
import com.hostelhub.data.model.*
import com.hostelhub.utils.DateUtils
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppointmentRepository"

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentApi: AppointmentApi
) {
    private val demoAppointments = mutableListOf<Appointment>()


    suspend fun getAppointments(status: String? = null, upcoming: Boolean? = null): Result<List<Appointment>> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            var filtered = demoAppointments.toList()
            if (status != null) {
                filtered = filtered.filter { it.status.name.equals(status, ignoreCase = true) }
            }
            return Result.success(filtered)
        }

        return try {
            val response = appointmentApi.getAppointments(status, upcoming)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch appointments"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching appointments", e)
            Result.failure(Exception("Failed to fetch appointments: ${e.message}"))
        }
    }

    suspend fun getAppointment(id: String): Result<Appointment> {
        if (BuildConfig.DEMO_MODE) {
            delay(300)
            val appointment = demoAppointments.find { it.id == id }
            return if (appointment != null) {
                Result.success(appointment)
            } else {
                Result.failure(Exception("Appointment not found"))
            }
        }

        return try {
            val response = appointmentApi.getAppointment(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Appointment not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        hostelId: String,
        ownerId: String,
        scheduledTime: String,
        duration: Int = 60,
        type: String = "viewing",
        notes: String? = null,
        location: String? = null
    ): Result<Appointment> {
        if (BuildConfig.DEMO_MODE) {
            delay(800)
            val newAppointment = Appointment(
                id = "demo_appt_${System.currentTimeMillis()}",
                hostelId = hostelId,
                customerId = "demo_user",
                ownerId = ownerId,
                hostel = AppointmentHostelInfo(id = hostelId, name = "Demo Hostel", location = "Lahore"),
                customer = AppointmentUserInfo(id = "demo_user", firstName = "Demo", lastName = "User"),
                owner = AppointmentUserInfo(id = ownerId, firstName = "Hostel", lastName = "Owner"),
                scheduledTime = scheduledTime,
                duration = duration,
                type = AppointmentType.valueOf(type.uppercase()),
                status = AppointmentStatus.PENDING,
                notes = notes,
                location = location,
                createdAt = DateUtils.getCurrentTimestamp()
            )
            demoAppointments.add(newAppointment)
            return Result.success(newAppointment)
        }

        return try {
            val request = CreateAppointmentRequest(hostelId, ownerId, scheduledTime, duration, type, notes, location)
            val response = appointmentApi.createAppointment(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create appointment"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating appointment", e)
            Result.failure(Exception("Failed to create appointment: ${e.message}"))
        }
    }

    suspend fun confirmAppointment(id: String): Result<Appointment> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoAppointments.indexOfFirst { it.id == id }
            if (index >= 0) {
                val updated = demoAppointments[index].copy(
                    status = AppointmentStatus.CONFIRMED,
                    confirmedAt = DateUtils.getCurrentTimestamp()
                )
                demoAppointments[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Appointment not found"))
        }

        return try {
            val response = appointmentApi.updateAppointment(id, UpdateAppointmentRequest(status = "confirmed"))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to confirm appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelAppointment(id: String, reason: String? = null): Result<Appointment> {
        if (BuildConfig.DEMO_MODE) {
            delay(500)
            val index = demoAppointments.indexOfFirst { it.id == id }
            if (index >= 0) {
                val updated = demoAppointments[index].copy(
                    status = AppointmentStatus.CANCELLED,
                    cancelledAt = DateUtils.getCurrentTimestamp(),
                    cancelReason = reason
                )
                demoAppointments[index] = updated
                return Result.success(updated)
            }
            return Result.failure(Exception("Appointment not found"))
        }

        return try {
            val response = appointmentApi.updateAppointment(id, UpdateAppointmentRequest(status = "cancelled", notes = reason))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to cancel appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

