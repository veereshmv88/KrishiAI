package com.krishiai.app.ui.screens.notifications

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.krishiai.app.core.notifications.NotificationCategory
import com.krishiai.app.core.notifications.NotificationPriority
import com.krishiai.app.data.local.entity.NotificationEntity
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    
    // Grouping logic
    val groupedNotifications = remember(notifications) {
        notifications.groupBy { notif ->
            when {
                DateUtils.isToday(notif.timestamp) -> "Today"
                DateUtils.isToday(notif.timestamp + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
                else -> "Earlier"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Center", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Mark all as read", color = BrandGreen)
                        }
                    }
                }
            )
        }
    ) { p ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.NotificationsOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("You're all caught up!", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(p).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedNotifications.forEach { (groupName, notifs) ->
                    item {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(notifs, key = { it.id }) { notif ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteNotification(notif.id)
                                    true
                                } else false
                            }
                        )
                        
                        AnimatedVisibility(
                            visible = dismissState.currentValue != SwipeToDismissBoxValue.EndToStart,
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.padding(end = 16.dp),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                NotificationItemCard(
                                    notification = notif,
                                    onClick = {
                                        viewModel.markAsRead(notif.id)
                                        notif.deepLink?.let { link ->
                                            // Real navigation logic would go here
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    val categoryEnum = try { NotificationCategory.valueOf(notification.category) } catch (e: Exception) { NotificationCategory.GENERAL }
    val priorityEnum = try { NotificationPriority.valueOf(notification.priority) } catch (e: Exception) { NotificationPriority.NORMAL }
    
    val icon = when (categoryEnum) {
        NotificationCategory.WEATHER_ALERTS -> Icons.Rounded.Cloud
        NotificationCategory.MARKETPLACE, NotificationCategory.BUY_REQUESTS, NotificationCategory.SELL_REQUESTS -> Icons.Rounded.Storefront
        NotificationCategory.AI_DISEASE_DETECTION, NotificationCategory.AI_CROP_HEALTH_REPORTS -> Icons.Rounded.EnergySavingsLeaf
        NotificationCategory.SECURITY_ALERTS -> Icons.Rounded.Security
        NotificationCategory.PAYMENTS -> Icons.Rounded.Payments
        NotificationCategory.CHAT_MESSAGES -> Icons.Rounded.Chat
        else -> Icons.Rounded.Notifications
    }

    val iconTint = when (priorityEnum) {
        NotificationPriority.CRITICAL -> Color(0xFFD32F2F)
        NotificationPriority.HIGH -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.primary
    }

    val bgColor = if (!notification.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.White

    PremiumCard(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.background(bgColor).padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = DateUtils.getRelativeTimeSpanString(notification.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (!notification.isRead) MaterialTheme.colorScheme.onSurface else Color.DarkGray
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!notification.actionPayload.isNullOrEmpty() || !notification.deepLink.isNullOrEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "VIEW DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 6.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(BrandGreen)
                )
            }
        }
    }
}
