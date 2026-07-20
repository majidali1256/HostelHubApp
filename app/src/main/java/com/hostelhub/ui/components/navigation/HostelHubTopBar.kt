package com.hostelhub.ui.components.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.ui.components.NotificationBadgeWidget
import com.hostelhub.ui.theme.Primary

/**
 * Top-notch 3D Glassmorphic Top Bar with Three-Line Hamburger Drawer Trigger and Brand Logo on top.
 * Fully responsive across all screen sizes with zero text overlapping, aligned with webapp Header.tsx & ui-ux-pro-max.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelHubTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    unreadNotifications: Int = 0,
    unreadMessages: Int = 0,
    modifier: Modifier = Modifier
) {
    var menuPressed by remember { mutableStateOf(false) }
    val menuScale by animateFloatAsState(
        targetValue = if (menuPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "menuScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                spotColor = Primary.copy(alpha = 0.2f)
            ),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left: Three-Line Menu Button + 3D Glowing Brand Logo (Zero Text Overlap & No Tagline)
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(menuScale)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                menuPressed = true
                                onMenuClick()
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Show Navigation Sections",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                LaunchedEffect(menuPressed) {
                    if (menuPressed) {
                        kotlinx.coroutines.delay(120)
                        menuPressed = false
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 3D Brand Logo & Name (Clean, no tagline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val logoBrush = remember {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(10.dp),
                                spotColor = Primary.copy(alpha = 0.4f)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(logoBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Hostel Hub Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Hostel Hub",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Right: Notifications & Messages Shortcuts
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chat Button with Badge
                Box {
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "Messages",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (unreadMessages > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadMessages > 99) "99+" else unreadMessages.toString(),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notification Bell
                NotificationBadgeWidget(
                    unreadCount = unreadNotifications,
                    onClick = onNotificationsClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), CircleShape)
                )
            }
        }
    }
}

