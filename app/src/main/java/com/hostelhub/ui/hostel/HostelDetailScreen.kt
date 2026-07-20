package com.hostelhub.ui.hostel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hostelhub.data.model.Review
import com.hostelhub.ui.reviews.*
import com.hostelhub.ui.fairrent.*
import com.hostelhub.ui.trust.TrustScoreBadgeWidget
import com.hostelhub.ui.fraud.ReportFraudModal
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HostelDetailScreen(
    hostelId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: HostelDetailViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    var showWriteReviewModal by remember { mutableStateOf(false) }
    var showReportFraudModal by remember { mutableStateOf(false) }
    
    LaunchedEffect(hostelId) {
        viewModel.loadHostel(hostelId)
        reviewViewModel.loadHostelReviews(hostelId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                    IconButton(onClick = { /* Favorite */ }) {
                        Icon(Icons.Outlined.FavoriteBorder, "Favorite")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            uiState.hostel?.let { hostel ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Rs ${hostel.price.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                "per month",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Button(
                            onClick = onNavigateToBooking,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BookOnline, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Book Now", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, null, tint = Error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(uiState.error ?: "Error loading hostel")
                    TextButton(onClick = { viewModel.loadHostel(hostelId) }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            uiState.hostel?.let { hostel ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 16.dp)
                ) {
                    // Image Carousel
                    item {
                        Box(modifier = Modifier.height(300.dp)) {
                            val pagerState = rememberPagerState(pageCount = { hostel.resolvedImages.size.coerceAtLeast(1) })
                            
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(hostel.resolvedImages.getOrNull(page) ?: "")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            // Page Indicator
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(hostel.resolvedImages.size.coerceAtLeast(1)) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == pagerState.currentPage) Color.White
                                                else Color.White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                            
                            // Badges
                            if (hostel.verified) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 60.dp, start = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Success
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, null, Modifier.size(16.dp), tint = Color.White)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Verified", color = Color.White, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Title & Location
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                hostel.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp), tint = Primary)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    hostel.location,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(Icons.Default.Star, String.format("%.1f", hostel.rating), "Rating")
                                StatItem(Icons.Default.RateReview, "${hostel.reviews?.size ?: 0}", "Reviews")
                                StatItem(Icons.Default.Category, hostel.category, "Type")
                            }
                        }
                    }

                    // Module 10: Price Fairness Analysis & Market Benchmarks Card
                    uiState.fairnessAnalysis?.let { analysis ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, null, tint = Primary, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("AI Price Guidance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        }
                                        FairnessBadgeWidget(label = analysis.fairnessLabel)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Market Benchmark Band: PKR ${analysis.predictedRange.min.toInt() / 1000}k – ${analysis.predictedRange.max.toInt() / 1000}k / mo",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                    if (analysis.reasoning.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            analysis.reasoning,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Divider
                    item { Divider(modifier = Modifier.padding(horizontal = 16.dp)) }

                    // Description
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "About",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                hostel.description ?: "No description available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    // Amenities
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Amenities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(hostel.amenities) { amenity ->
                                    AmenityItem(amenity)
                                }
                            }
                        }
                    }

                    // Rooms & Beds Inventory (Module 3)
                    if (!hostel.rooms.isNullOrEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Available Rooms & Beds",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    hostel.rooms.forEach { room ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Outlined.MeetingRoom, null, tint = Primary, modifier = Modifier.size(20.dp))
                                                        Spacer(Modifier.width(8.dp))
                                                        Text(room.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                    }
                                                    Surface(
                                                        color = Primary.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            "${room.type} • Rs ${room.price.toInt()}/mo",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Primary,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(8.dp))
                                                Text("Beds Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                                Spacer(Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    room.beds.forEach { bed ->
                                                        Surface(
                                                            color = if (bed.isOccupied) Error.copy(alpha = 0.15f) else Success.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    Icons.Outlined.Bed,
                                                                    null,
                                                                    tint = if (bed.isOccupied) Error else Success,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Spacer(Modifier.width(4.dp))
                                                                Text(
                                                                    "${bed.bedNumber} (${if (bed.isOccupied) "Booked" else "Available"})",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = if (bed.isOccupied) Error else Success
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Owner Info
                    uiState.owner?.let { owner ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = owner.profilePicture ?: "",
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            owner.username,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "Property Owner",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        TrustScoreBadgeWidget(
                                            score = owner.trustScore ?: 92,
                                            isCompact = true
                                        )
                                    }
                                    IconButton(onClick = { onNavigateToChat(owner.id) }) {
                                        Icon(Icons.Default.Chat, "Chat", tint = Primary)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Reviews Section Widget
                    item {
                        val displayReviews = if (reviewUiState.reviews.isNotEmpty()) reviewUiState.reviews else (hostel.reviews ?: emptyList())
                        ReviewsSectionWidget(
                            reviews = displayReviews,
                            ratingStats = reviewUiState.ratingStats,
                            onSeeAllClick = onNavigateToReviews,
                            onWriteReviewClick = { showWriteReviewModal = true },
                            onHelpfulClick = { reviewViewModel.toggleHelpful(it) }
                        )
                    }

                    // Report Fraud & Security Issue Banner
                    item {
                        Card(
                            onClick = { showReportFraudModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Report Suspicious Listing",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Error
                                    )
                                    Text(
                                        text = "Flag misleading photos, hidden fees, or off-platform scam attempts.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWriteReviewModal && uiState.hostel != null) {
        WriteReviewModal(
            hostelName = uiState.hostel!!.name,
            onDismiss = { showWriteReviewModal = false },
            onSubmit = { rating, cleanliness, accuracy, communication, location, value, title, comment ->
                reviewViewModel.submitReview(
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
                    showWriteReviewModal = false
                }
            },
            isLoading = reviewUiState.isSubmitting
        )
    }

    if (showReportFraudModal && uiState.hostel != null) {
        ReportFraudModal(
            hostelId = hostelId,
            hostelName = uiState.hostel!!.name,
            onDismiss = { showReportFraudModal = false },
            onReportSubmitted = { showReportFraudModal = false }
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun AmenityItem(amenity: String) {
    val icon = when (amenity.lowercase()) {
        "wifi" -> Icons.Default.Wifi
        "ac" -> Icons.Default.AcUnit
        "parking" -> Icons.Default.LocalParking
        "laundry" -> Icons.Default.LocalLaundryService
        "kitchen" -> Icons.Default.Kitchen
        "security" -> Icons.Default.Security
        "gym" -> Icons.Default.FitnessCenter
        "tv" -> Icons.Default.Tv
        "geyser" -> Icons.Default.HotTub
        "meals" -> Icons.Default.Restaurant
        else -> Icons.Default.CheckCircle
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Primary.copy(alpha = 0.1f)
        ) {
            Icon(icon, amenity, modifier = Modifier.padding(12.dp), tint = Primary)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            amenity,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

