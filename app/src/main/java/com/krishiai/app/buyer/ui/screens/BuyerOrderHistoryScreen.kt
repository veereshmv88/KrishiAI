package com.krishiai.app.buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.buyer.data.model.BuyerOrder
import com.krishiai.app.buyer.data.model.OrderStatus
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.BuyerPremiumCard
import com.krishiai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrderHistoryScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders = uiState.orders

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Delivered", "Cancelled")

    val filteredOrders = when (selectedTabIndex) {
        0 -> orders.filter { it.status in listOf(OrderStatus.PROCESSING, OrderStatus.ACCEPTED, OrderStatus.PACKED, OrderStatus.SHIPPED) }
        1 -> orders.filter { it.status == OrderStatus.DELIVERED }
        else -> orders.filter { it.status == OrderStatus.CANCELLED }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Orders", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = BuyerPrimaryGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 3.dp,
                            color = BuyerPrimaryGreen
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) BuyerPrimaryGreen else Color.Gray
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ShoppingBag, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No ${tabs[selectedTabIndex].lowercase()} orders found.", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredOrders) { order ->
                    PremiumOrderCard(order = order, onClick = { /* TODO: Order Details */ })
                }
            }
        }
    }
}

@Composable
fun PremiumOrderCard(order: BuyerOrder, onClick: () -> Unit) {
    BuyerPremiumCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Order #${order.orderId.takeLast(6).uppercase()}", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            OrderStatusBadge(status = order.status)
        }
        
        Spacer(Modifier.height(12.dp))
        Divider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                AsyncImage(model = order.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(order.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("By ${order.farmerName}", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("${order.quantityKg} kg • ₹${order.pricePerKg}/kg", style = MaterialTheme.typography.labelSmall, color = BuyerPrimaryGreen, fontWeight = FontWeight.Bold)
            }
            Text("₹${order.totalAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
        
        if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().background(BuyerLightBackground, RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocalShipping, null, tint = BuyerPrimaryGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Estimated Delivery", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(order.estimatedDelivery)), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, null, tint = Color.Gray)
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val (color, text) = when (status) {
        OrderStatus.PROCESSING -> BuyerWarning to "Processing"
        OrderStatus.ACCEPTED -> BuyerBlue to "Accepted"
        OrderStatus.PACKED -> BuyerBlue to "Packed"
        OrderStatus.SHIPPED -> BuyerBlue to "Shipped"
        OrderStatus.DELIVERED -> BuyerPrimaryGreen to "Delivered"
        OrderStatus.CANCELLED -> BuyerDanger to "Cancelled"
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
