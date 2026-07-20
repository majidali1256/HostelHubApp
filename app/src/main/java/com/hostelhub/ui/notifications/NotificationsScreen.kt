package com.hostelhub.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hostelhub.data.model.Notification
import com.hostelhub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (uiState.unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Badge(containerColor = Primary) {
                                Text(
                                    text = uiState.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.markAllAsRead() },
                        enabled = uiState.unreadCount > 0
                    ) {
                        Text("Mark all read")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilter.values().forEach { filter ->
                    val selected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectFilter(filter) },
                        label = { Text(filter.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary.copy(alpha = 0.15f),
                            selectedLabelColor = Primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            if (uiState.isLoading && uiState.notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.error != null && uiState.notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error loading notifications",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNotifications() }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (uiState.filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (uiState.selectedFilter == NotificationFilter.ALL)
                                "No notifications yet"
                            else
                                "No ${uiState.selectedFilter.displayName.lowercase()} notifications",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "We'll let you know when updates arrive",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.filteredNotifications,
                        key = { it.id }
                    ) { notification ->
                        val dismissState = rememberDismissState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == DismissValue.DismissedToStart) {
                                    viewModel.deleteNotification(notification.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                val color = when (dismissState.dismissDirection) {
                                    DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            dismissContent = {
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        
                                        // Handle navigation
                                        if (notification.type == "booking" || notification.data?.bookingId != null) {
                                            val bookingId = notification.data?.bookingId ?: ""
                                            onNavigateToBooking(bookingId)
                                        } else if (notification.type == "message" || notification.data?.conversationId != null) {
                                            val conversationId = notification.data?.conversationId ?: ""
                                            if (conversationId.isNotEmpty()) {
                                                onNavigateToChat(conversationId)
                                            }
                                        }
                                    },
                                    onDelete = { viewModel.deleteNotification(notification.id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = when (notification.type) {
        "booking" -> Icons.Outlined.BookOnline
        "message" -> Icons.Outlined.Chat
        "payment" -> Icons.Outlined.Payment
        "review" -> Icons.Outlined.Star
        "appointment" -> Icons.Outlined.Event
        "system" -> Icons.Outlined.AdminPanelSettings
        else -> Icons.Outlined.Notifications
    }
    
    val iconColor = when (notification.type) {
        "booking" -> Success
        "message" -> Secondary
        "payment" -> Primary
        "review" -> Accent
        "appointment" -> Primary
        "system" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) 
                MaterialTheme.colorScheme.surface 
            else 
                Primary.copy(alpha = 0.07f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.read) 1.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Surface
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.read) FontWeight.SemiBold else FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (!notification.read) {
                        Surface(
                            shape = CircleShape,
                            color = Primary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (notification.read) 0.7f else 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.createdAt ?: "Just now",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete notification",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
