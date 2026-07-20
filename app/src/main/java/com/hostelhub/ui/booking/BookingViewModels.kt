package com.hostelhub.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.model.Booking
import com.hostelhub.data.model.Hostel
import com.hostelhub.data.repository.BookingRepository
import com.hostelhub.data.repository.HostelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun loadHostel(hostelId: String) {
        viewModelScope.launch {
            hostelRepository.getHostelById(hostelId)
                .onSuccess { hostel ->
                    _uiState.value = _uiState.value.copy(hostel = hostel, hostelId = hostelId)
                }
        }
    }
    
    fun updateCheckInDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val date = calendar.time
        _uiState.value = _uiState.value.copy(
            checkInDate = date,
            checkInDateString = dateFormat.format(date),
            error = null
        )
    }
    
    fun updateCheckOutDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        val date = calendar.time
        _uiState.value = _uiState.value.copy(
            checkOutDate = date,
            checkOutDateString = dateFormat.format(date),
            error = null
        )
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun createBooking(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        if (state.checkInDate == null || state.checkOutDate == null) {
            _uiState.value = state.copy(error = "Please select both dates")
            return
        }
        
        if (state.checkOutDate.before(state.checkInDate) || state.checkOutDate == state.checkInDate) {
            _uiState.value = state.copy(error = "Check-out must be after check-in")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            bookingRepository.createBooking(
                hostelId = state.hostelId,
                checkIn = state.checkInDateString,
                checkOut = state.checkOutDateString,
                numberOfGuests = 1,
                specialRequests = state.notes.takeIf { it.isNotBlank() }
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Booking failed"
                    )
                }
        }
    }
}

data class BookingUiState(
    val hostelId: String = "",
    val hostel: Hostel? = null,
    val checkInDate: Date? = null,
    val checkOutDate: Date? = null,
    val checkInDateString: String = "",
    val checkOutDateString: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookingHistoryViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingHistoryUiState())
    val uiState: StateFlow<BookingHistoryUiState> = _uiState.asStateFlow()
    
    fun loadBookings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            bookingRepository.getMyBookings()
                .onSuccess { bookings ->
                    _uiState.value = _uiState.value.copy(isLoading = false, bookings = bookings)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
        }
    }
}

data class BookingHistoryUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
