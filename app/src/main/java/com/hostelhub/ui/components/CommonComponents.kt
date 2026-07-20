package com.hostelhub.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.hostelhub.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hostelhub.ui.theme.*

/**
 * Animated loading shimmer effect for skeleton screens (~24px generous curves)
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, 0f)
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * Skeleton loader for hostel card (~24px TripGlide shape language)
 */
@Composable
fun HostelCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            Column(modifier = Modifier.padding(18.dp)) {
                ShimmerBox(modifier = Modifier.width(220.dp).height(22.dp))
                Spacer(Modifier.height(8.dp))
                ShimmerBox(modifier = Modifier.width(160.dp).height(16.dp))
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        ShimmerBox(modifier = Modifier.width(70.dp).height(28.dp), shape = CircleShape)
                    }
                }
            }
        }
    }
}

/**
 * Gradient/Pill button with subtle scale animation on tap (Framer MCP 150-300ms feel)
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 1f else 0.96f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        enabled = enabled && !isLoading,
        shape = CircleShape, // Fully pill-shaped as per TripGlide rules
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = TextOnPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TextOnPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Verified badge with animation
 */
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "verified")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Surface(
        shape = CircleShape,
        color = Success,
        modifier = modifier.scale(scale)
    ) {
        Icon(
            Icons.Default.Verified,
            contentDescription = "Verified",
            modifier = Modifier.size(size).padding(2.dp),
            tint = Color.White
        )
    }
}

/**
 * Trust score indicator (pill shape)
 */
@Composable
fun TrustScoreIndicator(
    score: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        score >= 80 -> Success
        score >= 50 -> Warning
        else -> Error
    }
    
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1000),
        label = "score"
    )
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Shield,
            null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                "Trust Score",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            Text(
                "$animatedScore%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = animatedScore / 100f,
            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Animated rating stars
 */
@Composable
fun AnimatedRatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp
) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            val starValue = index + 1
            val filled = rating >= starValue
            val halfFilled = rating > index && rating < starValue
            
            val animatedScale by animateFloatAsState(
                targetValue = if (filled || halfFilled) 1f else 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "star$index"
            )
            
            Icon(
                imageVector = when {
                    filled -> Icons.Default.Star
                    halfFilled -> Icons.Default.StarHalf
                    else -> Icons.Default.StarBorder
                },
                contentDescription = null,
                modifier = Modifier.size(starSize).scale(animatedScale),
                tint = if (filled || halfFilled) Accent else MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Pulsing notification dot
 */
@Composable
fun NotificationDot(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Surface(
        shape = CircleShape,
        color = Error,
        modifier = modifier.scale(scale)
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Empty state component with pill button
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(actionLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Price tag with currency
 */
@Composable
fun PriceTag(
    price: Int,
    period: String = "/mo",
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            "Rs ",
            style = if (large) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = Primary,
            fontWeight = FontWeight.Medium
        )
        Text(
            "$price",
            style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            period,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
    }
}

// ============================================================================
// TRIPGLIDE DESIGN SYSTEM FOUNDATIONAL REUSABLE COMPONENTS
// ============================================================================

/**
 * Icon buttons sitting on top of images (back, favorite, filter) with circular semi-transparent backing
 */
@Composable
fun TripGlideOverlayIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    isFavorite: Boolean = false,
    contentDescription: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favScale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF111827).copy(alpha = 0.45f)) // Keeps icon readable over any photo
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isFavorite) Color(0xFFEF4444) else tint,
            modifier = Modifier.size(20.dp).scale(scale)
        )
    }
}

/**
 * Star ratings shown in a small bordered pill; review counts as underlined text
 */
@Composable
fun TripGlideRatingPill(
    rating: Double,
    reviewCount: Int,
    modifier: Modifier = Modifier,
    onReviewClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (reviewCount >= 0) {
            Text(
                text = "$reviewCount reviews",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = if (onReviewClick != null) Modifier.clickable { onReviewClick() } else Modifier
            )
        }
    }
}

/**
 * Underlined text for lightweight links like "Read more," "See all"
 */
@Composable
fun TripGlideUnderlinedLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Medium
        ),
        color = color,
        modifier = modifier.clickable { onClick() }
    )
}

/**
 * Horizontally scrollable pill row for categories/chips
 */
@Composable
fun TripGlideCategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                label = "chipBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextOnPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                label = "chipText"
            )
            
            Surface(
                shape = CircleShape,
                color = bgColor,
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                modifier = Modifier
                    .height(40.dp)
                    .clickable { onCategorySelected(category) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Sticky bottom action bar (full-width, solid primary-color pill button)
 */
@Composable
fun TripGlideStickyBottomBar(
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    priceOrTitleInfo: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isAppInDarkTheme()) 0.dp else 16.dp,
        border = if (isAppInDarkTheme()) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (priceOrTitleInfo != null) {
                Box(modifier = Modifier.weight(1f)) {
                    priceOrTitleInfo()
                }
                Spacer(Modifier.width(16.dp))
            }
            
            GradientButton(
                text = buttonText,
                onClick = onButtonClick,
                modifier = if (priceOrTitleInfo != null) Modifier.weight(1.3f) else Modifier.fillMaxWidth(),
                isLoading = isLoading,
                enabled = enabled
            )
        }
    }
}

/**
 * Bottom-sheet drag-handle indicator (~40x4px rounded pill)
 */
@Composable
fun TripGlideDragHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(4.5.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )
    }
}

/**
 * Nested CTA inside image card (e.g., dark pill footer bar with "See more" and arrow living inside image card)
 */
@Composable
fun TripGlideCardFooterAction(
    text: String = "See more",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ctaScale"
    )
    val isDark = isAppInDarkTheme()
    val bgColor = if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF1E40AF)
    
    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { onClick() }
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = text,
                        tint = Color(0xFF1E40AF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

