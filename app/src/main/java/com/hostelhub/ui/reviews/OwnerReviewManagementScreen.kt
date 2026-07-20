package com.hostelhub.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.data.model.Review
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerReviewManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Needs Reply, 2: Replied
    var replyingToReview by remember { mutableStateOf<Review?>(null) }
    var replyText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Load reviews for owner properties (demo loads all or property reviews)
        viewModel.loadHostelReviews("demo_1")
    }

    val reviews = uiState.reviews
    val filteredReviews = remember(reviews, selectedTab) {
        when (selectedTab) {
            1 -> reviews.filter { it.response == null || it.response.content.isBlank() }
            2 -> reviews.filter { it.response != null && it.response.content.isNotBlank() }
            else -> reviews
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hostel Review Management", fontWeight = FontWeight.Bold)
                        Text("Monitor student feedback and post official replies", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${reviews.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val pending = reviews.count { it.response == null || it.response.content.isBlank() }
                        Text("Needs Reply ($pending)")
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        val replied = reviews.count { it.response != null && it.response.content.isNotBlank() }
                        Text("Answered ($replied)")
                    }
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (filteredReviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            text = "No reviews in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredReviews, key = { it.id }) { review ->
                        OwnerReviewCard(
                            review = review,
                            onReplyClick = {
                                replyingToReview = review
                                replyText = review.response?.content ?: ""
                            }
                        )
                    }
                }
            }
        }
    }

    // Reply Modal
    if (replyingToReview != null) {
        val rev = replyingToReview!!
        AlertDialog(
            onDismissRequest = { if (!uiState.isSubmitting) replyingToReview = null },
            title = { Text("Official Host Reply to ${rev.displayUsername}") },
            text = {
                Column {
                    Text(
                        text = "\"${rev.comment}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Host Response") },
                        placeholder = { Text("Thank the student or clarify our policies and improvements...") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Quick Suggestions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    SuggestionChip(
                        onClick = { replyText = "Thank you for staying with us! We appreciate your wonderful feedback and look forward to welcoming you back next semester." },
                        label = { Text("Appreciation reply", style = MaterialTheme.typography.bodySmall) }
                    )
                    SuggestionChip(
                        onClick = { replyText = "Thank you for your valuable feedback. We have immediately notified our maintenance and security staff to ensure these issues are resolved." },
                        label = { Text("Action taken reply", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitOwnerReply(rev.id, replyText) {
                            replyingToReview = null
                            replyText = ""
                        }
                    },
                    enabled = replyText.isNotBlank() && !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextOnPrimary)
                    } else {
                        Text("Post Official Reply")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingToReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OwnerReviewCard(
    review: Review,
    onReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = review.displayUsername,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = review.createdAt ?: "Recent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (!review.title.isNullOrBlank()) {
                Text(
                    text = review.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )

            if (review.response != null && review.response.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Reply, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Your Official Reply",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = review.response.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onReplyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (review.response != null) MaterialTheme.colorScheme.secondaryContainer else Primary,
                        contentColor = if (review.response != null) MaterialTheme.colorScheme.onSecondaryContainer else TextOnPrimary
                    )
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (review.response != null) "Edit Reply" else "Answer Review")
                }
            }
        }
    }
}
