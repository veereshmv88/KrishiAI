package com.krishiai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.krishiai.app.ui.screens.notifications.NotificationViewModel

@Composable
fun NotificationIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val unreadCount by viewModel.unreadCount.collectAsState()

    // Bell ringing animation triggered when count increases
    var previousCount by remember { mutableStateOf(unreadCount) }
    var triggerAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(unreadCount) {
        if (unreadCount > previousCount) {
            triggerAnimation = true
        }
        previousCount = unreadCount
    }

    val rotation by animateFloatAsState(
        targetValue = if (triggerAnimation) 15f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { triggerAnimation = false }
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .offset(x = (-4).dp, y = 4.dp)
                            .scale(if (triggerAnimation) 1.2f else 1f) // Scale animation
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (unreadCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    // Apply bell ringing rotation if new notification arrived
                    .scale(if (triggerAnimation) 1.1f else 1f)
            )
        }
    }
}
