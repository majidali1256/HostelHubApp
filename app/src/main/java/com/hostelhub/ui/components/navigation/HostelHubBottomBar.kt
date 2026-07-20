package com.hostelhub.ui.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.hostelhub.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.ui.navigation.Screen
import com.hostelhub.ui.theme.Primary
import com.hostelhub.ui.theme.TextOnPrimary

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val route: String
)

/**
 * Modern Floating Pill-Shaped Bottom Navigation Bar matching Blue & White Theme (Black replaced by Royal Blue/White)
 * Features 5 distinct options with crisp circular active background and readable labels.
 */
@Composable
fun HostelHubBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        listOf(
            BottomNavItem("Explore", Icons.Outlined.Explore, Icons.Filled.Explore, Screen.Home.route),
            BottomNavItem("My Bookings", Icons.Outlined.BookOnline, Icons.Filled.BookOnline, Screen.BookingHistory.route),
            BottomNavItem("Wishlist", Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite, Screen.Wishlist.route),
            BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person, Screen.Profile.route),
            BottomNavItem("Settings", Icons.Outlined.Settings, Icons.Filled.Settings, Screen.Settings.route)
        )
    }

    val isDark = isAppInDarkTheme()
    // Replaced black (#212529 / #111827) with Royal Blue (Primary / #1E40AF) for light mode bar background
    val barBgColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFF1E40AF)
    val inactiveIconColor = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.70f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = if (isDark) 0.dp else 18.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Primary.copy(alpha = 0.45f)
            ),
        color = barBgColor,
        shape = RoundedCornerShape(28.dp),
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.06f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "itemScale"
                )

                val activeBgColor by animateColorAsState(
                    targetValue = if (isSelected) (if (isDark) Primary else Color.White) else Color.Transparent,
                    label = "activeBg"
                )

                // Replaced black with Royal Blue inside the white active circle
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) (if (isDark) TextOnPrimary else Color(0xFF1E40AF)) else inactiveIconColor,
                    label = "iconColor"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) (if (isDark) Primary else Color.White) else inactiveIconColor,
                    label = "textColor"
                )

                Column(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { if (!isSelected) onNavigate(item.route) }
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(activeBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

