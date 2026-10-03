package com.krishiai.app.buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.BuyerPremiumCard
import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProfileScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val profileMenuItems = listOf(
        Triple("My Profile", Icons.Rounded.Person, "buyer_edit_profile"),
        Triple("Address Book", Icons.Rounded.LocationOn, "buyer_addresses"),
        Triple("My Orders", Icons.Rounded.ShoppingBag, "buyer_orders"),
        Triple("Wishlist", Icons.Rounded.Favorite, "buyer_wishlist"),
        Triple("Price Alerts", Icons.Rounded.Notifications, "buyer_price_alerts"),
        Triple("Payment Methods", Icons.Rounded.Payment, "buyer_payments"),
        Triple("Language", Icons.Rounded.Language, "settings_language"),
        Triple("Settings", Icons.Rounded.Settings, "settings"),
        Triple("Help & Support", Icons.Rounded.Help, "settings_help"),
        Triple("Logout", Icons.AutoMirrored.Rounded.Logout, null)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("My Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerPrimaryGreen, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // Profile Header
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(BuyerPrimaryGreen, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(90.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(50.dp))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Enterprise Buyer", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(viewModel.currentUserId.take(10), color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            ProfileStat("Orders", "${uiState.orders.size}")
                            ProfileStat("Wishlist", "${uiState.wishlistCount}")
                            ProfileStat("In Cart", "${uiState.cartCount}")
                        }
                    }
                }
            }

            // Menu Items
            item { Spacer(Modifier.height(24.dp)) }
            items(profileMenuItems) { (label, icon, route) ->
                BuyerPremiumCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    onClick = {
                        if (route == null) authViewModel.logout()
                        else navController.navigate(route)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = BuyerLightBackground, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(icon, null, tint = if (label == "Logout") BuyerDanger else BuyerPrimaryGreen, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(label, style = MaterialTheme.typography.titleMedium, color = if (label == "Logout") BuyerDanger else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color.Gray)
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color.White.copy(0.8f), style = MaterialTheme.typography.labelMedium)
    }
}
