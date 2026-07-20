package com.hostelhub.ui.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
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
 * Modern TripGlide Floating Pill-Shaped Bottom Navigation Bar (Icon-Only with Circular Active Background)
 */
@Composable
fun HostelHubBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        listOf(
            BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home, Screen.Home.route),
            BottomNavItem("Search", Icons.Outlined.Search, Icons.Filled.Search, Screen.Search.route),
            BottomNavItem("Bookings", Icons.Outlined.BookOnline, Icons.Filled.BookOnline, Screen.BookingHistory.route),
            BottomNavItem("Profile", Icons.Outlined.Person, Icons.Filled.Person, Screen.Profile.route)
        )
    }

    val isDark = isSystemInDarkTheme()
    val barBgColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFF111827)
    val inactiveIconColor = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.65f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 14.dp)
            .shadow(
                elevation = if (isDark) 0.dp else 24.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.35f)
            ),
        color = barBgColor,
        shape = CircleShape,
        border = if (isDark) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
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

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) (if (isDark) TextOnPrimary else Color(0xFF111827)) else inactiveIconColor,
                    label = "iconColor"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(activeBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { if (!isSelected) onNavigate(item.route) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
