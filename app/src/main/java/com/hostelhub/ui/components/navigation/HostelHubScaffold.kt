package com.hostelhub.ui.components.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.ui.theme.Primary
import kotlinx.coroutines.launch

/**
 * Unified Master Scaffold that integrates the Top-Notch 3D Glassmorphic Top Bar,
 * Three-Line Hamburger Slide-Out Drawer (with full Web App section parity), and Floating Bottom Bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelHubScaffold(
    title: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = { onNavigate(com.hostelhub.ui.navigation.Screen.Notifications.route) },
    onChatClick: () -> Unit = { onNavigate(com.hostelhub.ui.navigation.Screen.ChatList.route) },
    onLogout: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    unreadNotifications: Int = 0,
    unreadMessages: Int = 0,
    showBottomBar: Boolean = true,
    floatingActionButton: @Composable () -> Unit = {},
    userRole: String = "customer",
    username: String = "System Admin",
    userEmail: String = "admin@gmail.com",
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    HostelHubNavigationDrawer(
        drawerState = drawerState,
        currentRoute = currentRoute,
        userRole = userRole,
        username = username,
        userEmail = userEmail,
        unreadMessages = unreadMessages,
        onNavigate = { route ->
            scope.launch {
                if (drawerState.isOpen) drawerState.close()
            }
            onNavigate(route)
        },
        onClose = {
            scope.launch {
                if (drawerState.isOpen) drawerState.close()
            }
        },
        onLogout = onLogout,
        onOpenFeedback = onOpenFeedback
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                HostelHubTopBar(
                    title = title,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onNotificationsClick = onNotificationsClick,
                    onChatClick = onChatClick,
                    unreadNotifications = unreadNotifications,
                    unreadMessages = unreadMessages
                )
            },
            floatingActionButton = floatingActionButton,
            bottomBar = {
                if (showBottomBar) {
                    HostelHubBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route -> onNavigate(route) }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (title.isNotBlank() && !title.equals("Dashboard", ignoreCase = true)) {
                    Text(
                        text = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    content(PaddingValues(0.dp))
                }
            }
        }
    }
}
