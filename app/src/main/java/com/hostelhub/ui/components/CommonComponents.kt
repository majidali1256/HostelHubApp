package com.hostelhub.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hostelhub.ui.theme.*

/**
 * Animated loading shimmer effect for skeleton screens
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
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
 * Skeleton loader for hostel card
 */
@Composable
fun HostelCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                ShimmerBox(modifier = Modifier.width(200.dp).height(20.dp))
                Spacer(Modifier.height(8.dp))
                ShimmerBox(modifier = Modifier.width(150.dp).height(16.dp))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        ShimmerBox(modifier = Modifier.width(60.dp).height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Gradient button with animation
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
        targetValue = if (enabled && !isLoading) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .scale(scale),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TextOnPrimary,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold)
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
 * Trust score indicator
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
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
                targetValue = if (filled || halfFilled) 1f else 0.8f,
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
 * Empty state component
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
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel)
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
