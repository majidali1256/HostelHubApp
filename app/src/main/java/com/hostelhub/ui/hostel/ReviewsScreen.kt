package com.hostelhub.ui.hostel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.ui.reviews.*
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    hostelId: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddReviewModal by remember { mutableStateOf(false) }
    var replyingToReviewId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(hostelId) {
        viewModel.loadHostelReviews(hostelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verified Student Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (uiState.currentUserRole != "owner") {
                FloatingActionButton(
                    onClick = { showAddReviewModal = true },
                    containerColor = Primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = "Add Review", tint = TextOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Write Review", color = TextOnPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        val stats = uiState.ratingStats
        val reviews = uiState.filteredReviews

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Stats Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%.1f", if (stats.avgRating > 0f) stats.avgRating else 5.0f),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary
                                )
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < stats.avgRating.toInt().coerceAtLeast(4)) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${stats.totalReviews.takeIf { it > 0 } ?: uiState.reviews.size} total reviews",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Breakdown bars
                            Column(modifier = Modifier.weight(1f)) {
                                val clean = if (stats.avgCleanliness > 0f) stats.avgCleanliness else 4.9f
                                val sec = if (stats.avgAccuracy > 0f) stats.avgAccuracy else 4.8f
                                val comm = if (stats.avgCommunication > 0f) stats.avgCommunication else 4.7f
                                val valMon = if (stats.avgValue > 0f) stats.avgValue else 4.8f

                                CategoryBar("Cleanliness", clean)
                                Spacer(modifier = Modifier.height(4.dp))
                                CategoryBar("Security", sec)
                                Spacer(modifier = Modifier.height(4.dp))
                                CategoryBar("Staff", comm)
                                Spacer(modifier = Modifier.height(4.dp))
                                CategoryBar("Value", valMon)
                            }
                        }
                    }
                }

                // Filter Chips Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "5 Stars", "4 Stars", "With Response", "My Reviews")
                        filters.forEach { filter ->
                            val selected = uiState.selectedFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setFilter(filter) },
                                label = { Text(filter) },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = TextOnPrimary
                                )
                            )
                        }
                    }
                }

                // Reviews list or empty state
                if (reviews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = Accent,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (uiState.selectedFilter == "All") "No verified student reviews yet" else "No reviews match '${uiState.selectedFilter}'",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your voice matters! Share your stay experience.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(reviews, key = { it.id }) { review ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            ReviewCardView(
                                review = review,
                                onHelpfulClick = { viewModel.toggleHelpful(review.id) },
                                onDeleteClick = if (review.actualUserId == uiState.currentUserId || review.userId == uiState.currentUserId) {
                                    { viewModel.deleteReview(review.id) }
                                } else null,
                                onReplyClick = if (uiState.currentUserRole == "owner") {
                                    { replyingToReviewId = review.id }
                                } else null,
                                isPreview = false
                            )
                        }
                    }
                }
            }
        }
    }

    // Write Review Modal
    if (showAddReviewModal) {
        WriteReviewModal(
            hostelName = "Hostel Hub Property",
            onDismiss = { showAddReviewModal = false },
            onSubmit = { rating, cleanliness, accuracy, communication, location, value, title, comment ->
                viewModel.submitReview(
                    hostelId = hostelId,
                    bookingId = null,
                    rating = rating,
                    cleanliness = cleanliness,
                    accuracy = accuracy,
                    communication = communication,
                    location = location,
                    value = value,
                    title = title,
                    comment = comment
                ) {
                    showAddReviewModal = false
                }
            },
            isLoading = uiState.isSubmitting
        )
    }

    // Owner Reply Dialog
    if (replyingToReviewId != null) {
        AlertDialog(
            onDismissRequest = { replyingToReviewId = null },
            title = { Text("Post Official Host Reply") },
            text = {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Your response") },
                    placeholder = { Text("Thank the student or address specific feedback...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val revId = replyingToReviewId!!
                        viewModel.submitOwnerReply(revId, replyText) {
                            replyText = ""
                            replyingToReviewId = null
                        }
                    },
                    enabled = replyText.isNotBlank() && !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextOnPrimary)
                    } else {
                        Text("Post Reply")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingToReviewId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
