package com.krishiai.app.buyer.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.krishiai.app.buyer.data.model.BuyerNotification
import com.krishiai.app.buyer.data.model.NotificationCategory
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.BuyerPremiumCard
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerNotificationCenterScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notifications = uiState.notifications
    
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Text("Mark all as read", color = BuyerPrimaryGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                        val dismissState = rememberSwipeToDismissBoxState()
                        
                        AnimatedVisibility(
                            visible = dismissState.currentValue != SwipeToDismissBoxValue.EndToStart,
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).background(BuyerDanger).padding(horizontal = 24.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White)
                                    }
                                }
                            ) {
                                NotificationCard(
                                    notification = notif,
                                    onClick = { viewModel.markNotificationAsRead(notif.id) }
                                )
                            }
                        }
                        
                        // Delete removed because ViewModel doesn't support it yet
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: BuyerNotification,
    onClick: () -> Unit
) {
    val (icon, color) = when (notification.category) {
        NotificationCategory.PRICE_ALERTS -> Icons.Rounded.TrendingDown to BuyerPremiumGold
        NotificationCategory.ORDERS -> Icons.Rounded.LocalShipping to BuyerPrimaryGreen
        NotificationCategory.AI_RECOMMENDATIONS -> Icons.Rounded.AutoAwesome to BuyerBlue
        NotificationCategory.SYSTEM -> Icons.Rounded.Info to Color.Gray
        else -> Icons.Rounded.Notifications to BuyerPrimaryGreen
    }

    BuyerPremiumCard(
        modifier = Modifier.padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(if (!notification.isRead) BuyerLightBackground else Color.Transparent),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(notification.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    if (!notification.isRead) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BuyerPrimaryGreen))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(notification.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(DateUtils.getRelativeTimeSpanString(notification.timestamp).toString(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
