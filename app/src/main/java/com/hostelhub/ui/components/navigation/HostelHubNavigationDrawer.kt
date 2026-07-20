package com.hostelhub.ui.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.ui.navigation.Screen
import com.hostelhub.ui.theme.*

/**
 * Top-Notch 3D Modern Glassmorphic Navigation Drawer mirroring Web App (Sidebar.tsx)
 * Features 3D Cards, Gradient Icon Containers, Active Left Pill Indicator, and deep elevation.
 */
@Composable
fun HostelHubNavigationDrawer(
    drawerState: DrawerState,
    currentRoute: String,
    userRole: String = "customer",
    username: String = "System Admin",
    userEmail: String = "admin@gmail.com",
    unreadMessages: Int = 0,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onOpenFeedback: () -> Unit = {},
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(315.dp)
                    .fillMaxHeight()
                    .shadow(32.dp, RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp), spotColor = Primary.copy(alpha = 0.35f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)),
                drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    // 3D Glassmorphic Header Card (Brand Logo + Name without tagline)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Primary.copy(alpha = 0.25f)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val logoBrush = remember {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Primary.copy(alpha = 0.5f))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(logoBrush),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Hostel Hub Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Hostel Hub",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Drawer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 3D Navigation Section Tag: GENERAL NAVIGATION
                    SectionHeaderPill(title = "GENERAL NAVIGATION", icon = Icons.Default.Explore)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Navigation Items List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Dashboard
                        item {
                            DrawerNavItem3D(
                                title = "Dashboard",
                                icon = Icons.Default.Dashboard,
                                isSelected = currentRoute == Screen.Home.route,
                                onClick = { onNavigate(Screen.Home.route) }
                            )
                        }

                        // 2. Messages
                        item {
                            DrawerNavItem3D(
                                title = "Messages",
                                icon = Icons.Outlined.Chat,
                                isSelected = currentRoute == Screen.ChatList.route,
                                badgeCount = unreadMessages,
                                onClick = { onNavigate(Screen.ChatList.route) }
                            )
                        }

                        // 3. Agreements
                        item {
                            DrawerNavItem3D(
                                title = "Agreements",
                                icon = Icons.Default.Description,
                                isSelected = currentRoute == Screen.Agreements.route,
                                onClick = { onNavigate(Screen.Agreements.route) }
                            )
                        }

                        // Role/Customer specific items
                        if (userRole.equals("customer", ignoreCase = true) || userRole.equals("admin", ignoreCase = true)) {
                            item {
                                DrawerNavItem3D(
                                    title = "My Bookings",
                                    icon = Icons.Default.BookOnline,
                                    isSelected = currentRoute == Screen.BookingHistory.route,
                                    onClick = { onNavigate(Screen.BookingHistory.route) }
                                )
                            }
                            item {
                                DrawerNavItem3D(
                                    title = "My Saved Wishlists",
                                    icon = Icons.Default.FavoriteBorder,
                                    isSelected = false,
                                    onClick = { onNavigate(Screen.Home.route) }
                                )
                            }
                            item {
                                DrawerNavItem3D(
                                    title = "Fair Rent Guidance",
                                    icon = Icons.Default.Calculate,
                                    isSelected = currentRoute == Screen.FairRentEstimator.route,
                                    onClick = { onNavigate(Screen.FairRentEstimator.route) }
                                )
                            }
                        }

                        // Owner specific items
                        if (userRole.equals("owner", ignoreCase = true)) {
                            item {
                                DrawerNavItem3D(
                                    title = "Manage Bookings",
                                    icon = Icons.Default.BookOnline,
                                    isSelected = currentRoute == Screen.OwnerHostelList.route,
                                    onClick = { onNavigate(Screen.OwnerHostelList.route) }
                                )
                            }
                            item {
                                DrawerNavItem3D(
                                    title = "Price Guidance Tool",
                                    icon = Icons.Default.Calculate,
                                    isSelected = currentRoute == Screen.FairRentEstimator.route,
                                    onClick = { onNavigate(Screen.FairRentEstimator.route) }
                                )
                            }
                        }

                        // Admin & Moderation Hub
                        if (userRole.equals("admin", ignoreCase = true) || userRole.equals("owner", ignoreCase = true)) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                SectionHeaderPill(
                                    title = "PLATFORM ADMINISTRATION",
                                    icon = Icons.Default.AdminPanelSettings
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            item {
                                DrawerNavItem3D(
                                    title = "Admin Dashboard",
                                    icon = Icons.Default.AdminPanelSettings,
                                    isSelected = currentRoute == Screen.AdminDashboard.route,
                                    onClick = { onNavigate(Screen.AdminDashboard.route) }
                                )
                            }
                            item {
                                DrawerNavItem3D(
                                    title = "Fraud Detection & Security",
                                    icon = Icons.Default.Security,
                                    isSelected = currentRoute == Screen.AdminFraudDashboard.route,
                                    onClick = { onNavigate(Screen.AdminFraudDashboard.route) }
                                )
                            }
                        }

                        // My Profile
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            DrawerNavItem3D(
                                title = "My Profile",
                                icon = Icons.Default.Person,
                                isSelected = currentRoute == Screen.Profile.route,
                                onClick = { onNavigate(Screen.Profile.route) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer Section: Help, Settings, User Card, Links, Logout
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DrawerNavItem3D(
                            title = "Help & Support",
                            icon = Icons.Default.HelpOutline,
                            isSelected = false,
                            onClick = {
                                onClose()
                                onOpenFeedback()
                            }
                        )

                        DrawerNavItem3D(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            isSelected = currentRoute == Screen.Settings.route,
                            onClick = { onNavigate(Screen.Settings.route) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 3D User Profile Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Primary.copy(alpha = 0.2f)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Primary.copy(alpha = 0.18f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .shadow(6.dp, CircleShape, spotColor = Primary.copy(alpha = 0.4f))
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = username.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp
                                        )
                                    }

                                    // Online status badge dot
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = username,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    val roleBg = when (userRole.lowercase()) {
                                        "admin" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                        "owner" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                        else -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                    }
                                    val roleText = when (userRole.lowercase()) {
                                        "admin" -> Color(0xFFEF4444)
                                        "owner" -> Color(0xFF10B981)
                                        else -> Color(0xFF8B5CF6)
                                    }

                                    Surface(
                                        color = roleBg,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = userRole.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = roleText,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Privacy & Terms Links
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Privacy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { onNavigate(Screen.Profile.route) }
                            )
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Terms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { onNavigate(Screen.Profile.route) }
                            )
                        }

                        // 3D Outlined Logout Button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onClose()
                                    onLogout()
                                },
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Logout",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        content = content
    )
}

@Composable
private fun SectionHeaderPill(title: String, icon: ImageVector) {
    Surface(
        color = Primary.copy(alpha = 0.08f),
        shape = CircleShape,
        border = BorderStroke(0.5.dp, Primary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = Primary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DrawerNavItem3D(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(Primary.copy(alpha = if (isDark) 0.35f else 0.25f), Primary.copy(alpha = if (isDark) 0.12f else 0.06f))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        label = "contentColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected && !isDark) 6.dp else 0.dp,
        label = "elevation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, CircleShape, spotColor = Primary.copy(alpha = 0.3f))
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = CircleShape,
        border = if (isSelected) BorderStroke(1.dp, Primary.copy(alpha = 0.3f)) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
        ) {
            // Left Active Indicator Pill
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(4.dp)
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 3D Icon Box
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(if (isSelected && !isDark) 6.dp else 0.dp, CircleShape, spotColor = Primary.copy(alpha = 0.4f)),
                        shape = CircleShape,
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = if (isSelected) Color.White else contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = contentColor,
                        fontSize = 15.sp
                    )
                }

                if (badgeCount > 0) {
                    Surface(
                        color = Color(0xFFEF4444),
                        shape = CircleShape,
                        modifier = Modifier
                            .shadow(if (isDark) 0.dp else 4.dp, CircleShape, spotColor = Color(0xFFEF4444).copy(alpha = 0.4f))
                            .sizeIn(minWidth = 22.dp, minHeight = 22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

