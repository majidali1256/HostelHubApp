package com.hostelhub.ui.reviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hostelhub.ui.theme.Accent
import com.hostelhub.ui.theme.Primary

@Composable
fun WriteReviewModal(
    hostelName: String,
    onDismiss: () -> Unit,
    onSubmit: (
        rating: Int,
        cleanliness: Int,
        accuracy: Int,
        communication: Int,
        location: Int,
        value: Int,
        title: String,
        comment: String
    ) -> Unit,
    isLoading: Boolean = false
) {
    var overallRating by remember { mutableIntStateOf(5) }
    var cleanliness by remember { mutableIntStateOf(5) }
    var accuracy by remember { mutableIntStateOf(5) }
    var communication by remember { mutableIntStateOf(5) }
    var location by remember { mutableIntStateOf(5) }
    var valueForMoney by remember { mutableIntStateOf(5) }

    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var showCategories by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Review $hostelName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Your feedback helps verified students choose their home away from home.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Overall Rating
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Overall Rating",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.Center) {
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { overallRating = star },
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (star <= overallRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "$star Star",
                                            tint = Accent,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = when (overallRating) {
                                    5 -> "Exceptional - Exceeded expectations"
                                    4 -> "Great - Very satisfying stay"
                                    3 -> "Average - Met basic needs"
                                    2 -> "Below Average - Needs improvement"
                                    else -> "Poor - Unlikely to recommend"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expandable Category Breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCategories = !showCategories }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rate by Category (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showCategories) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AnimatedVisibility(visible = showCategories) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CategoryStarRow("Cleanliness", cleanliness) { cleanliness = it }
                            CategoryStarRow("Security & Accuracy", accuracy) { accuracy = it }
                            CategoryStarRow("Communication & Staff", communication) { communication = it }
                            CategoryStarRow("Location & Accessibility", location) { location = it }
                            CategoryStarRow("Value for Money", valueForMoney) { valueForMoney = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Review Headline") },
                        placeholder = { Text("e.g., Highly secure with great fiber WiFi") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Comment
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        label = { Text("Detailed Feedback") },
                        placeholder = { Text("Describe cleanliness, security checks, WiFi speed, staff behavior, and room quality...") },
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTitle = title.ifBlank {
                                when (overallRating) {
                                    5 -> "Excellent experience"
                                    4 -> "Great stay overall"
                                    3 -> "Decent hostel stay"
                                    else -> "Stay feedback"
                                }
                            }
                            onSubmit(
                                overallRating,
                                cleanliness,
                                accuracy,
                                communication,
                                location,
                                valueForMoney,
                                finalTitle,
                                comment
                            )
                        },
                        enabled = comment.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Submitting..." else "Submit Review")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryStarRow(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row {
            (1..5).forEach { star ->
                IconButton(
                    onClick = { onRatingChange(star) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$star Star",
                        tint = Accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
