package com.hostelhub.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.data.api.CreateReviewRequest
import com.hostelhub.data.local.TokenManager
import com.hostelhub.data.model.RatingStatistics
import com.hostelhub.data.model.Review
import com.hostelhub.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewsUiState(
    val reviews: List<Review> = emptyList(),
    val filteredReviews: List<Review> = emptyList(),
    val ratingStats: RatingStatistics = RatingStatistics(),
    val selectedFilter: String = "All",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    val currentUserRole: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(tokenManager.userId, tokenManager.userRole) { id, role ->
                id to role
            }.collect { (id, role) ->
                _uiState.update { it.copy(currentUserId = id, currentUserRole = role) }
            }
        }
    }

    fun loadHostelReviews(hostelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val statsResult = reviewRepository.getHostelRatingStats(hostelId)
            val reviewsResult = reviewRepository.getHostelReviews(hostelId)

            if (reviewsResult.isSuccess) {
                val list = reviewsResult.getOrDefault(emptyList())
                val stats = statsResult.getOrDefault(RatingStatistics())
                _uiState.update {
                    it.copy(
                        reviews = list,
                        ratingStats = stats,
                        isLoading = false
                    )
                }
                applyFilter(_uiState.value.selectedFilter)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = reviewsResult.exceptionOrNull()?.message ?: "Failed to load reviews"
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        val all = _uiState.value.reviews
        val filtered = when (filter) {
            "5 Stars" -> all.filter { it.rating == 5 }
            "4 Stars" -> all.filter { it.rating == 4 }
            "With Response" -> all.filter { it.response != null && it.response.content.isNotBlank() }
            "My Reviews" -> all.filter { it.actualUserId == _uiState.value.currentUserId || it.userId == _uiState.value.currentUserId }
            else -> all
        }
        _uiState.update { it.copy(filteredReviews = filtered) }
    }

    fun submitReview(
        hostelId: String,
        bookingId: String?,
        rating: Int,
        cleanliness: Int,
        accuracy: Int,
        communication: Int,
        location: Int,
        value: Int,
        title: String,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val req = CreateReviewRequest(
                hostelId = hostelId,
                bookingId = bookingId,
                rating = rating,
                cleanliness = cleanliness,
                accuracy = accuracy,
                communication = communication,
                location = location,
                value = value,
                title = title,
                comment = comment
            )
            val res = reviewRepository.createReview(req)
            _uiState.update { it.copy(isSubmitting = false) }
            if (res.isSuccess) {
                val newRev = res.getOrThrow()
                val updatedList = listOf(newRev) + _uiState.value.reviews
                _uiState.update { it.copy(reviews = updatedList) }
                applyFilter(_uiState.value.selectedFilter)
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = res.exceptionOrNull()?.message ?: "Failed to submit review") }
            }
        }
    }

    fun toggleHelpful(reviewId: String) {
        viewModelScope.launch {
            val res = reviewRepository.toggleHelpful(reviewId)
            if (res.isSuccess) {
                val newCount = res.getOrThrow()
                val updatedList = _uiState.value.reviews.map { rev ->
                    if (rev.id == reviewId) rev.copy(helpfulCount = newCount) else rev
                }
                _uiState.update { it.copy(reviews = updatedList) }
                applyFilter(_uiState.value.selectedFilter)
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            val res = reviewRepository.deleteReview(reviewId)
            if (res.isSuccess) {
                val updatedList = _uiState.value.reviews.filter { it.id != reviewId }
                _uiState.update { it.copy(reviews = updatedList) }
                applyFilter(_uiState.value.selectedFilter)
            } else {
                _uiState.update { it.copy(errorMessage = res.exceptionOrNull()?.message ?: "Failed to delete review") }
            }
        }
    }

    fun submitOwnerReply(reviewId: String, content: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val res = reviewRepository.postOwnerResponse(reviewId, content)
            _uiState.update { it.copy(isSubmitting = false) }
            if (res.isSuccess) {
                val updatedRev = res.getOrThrow()
                val updatedList = _uiState.value.reviews.map { rev ->
                    if (rev.id == reviewId) updatedRev else rev
                }
                _uiState.update { it.copy(reviews = updatedList) }
                applyFilter(_uiState.value.selectedFilter)
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = res.exceptionOrNull()?.message ?: "Failed to submit reply") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
