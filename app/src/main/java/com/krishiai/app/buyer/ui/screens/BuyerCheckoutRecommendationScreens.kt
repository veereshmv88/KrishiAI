package com.krishiai.app.buyer.ui.screens

import com.krishiai.app.buyer.ui.components.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.buyer.ai.AIRecommendation
import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.buyer.ui.BuyerMainViewModel

// ─────────────────────────────────────────────
// AI RECOMMENDATIONS SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationsScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val recs = uiState.recommendations

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Recommendations") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                actions = {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.padding(end = 16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (recs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BuyerTheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("AI is analyzing market data...", fontSize = 14.sp, color = BuyerTheme.textSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8FAF8)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = BuyerTheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = BuyerTheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Personalized for You", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyerTheme.primary)
                                Text("${recs.size} crops recommended based on market data and your preferences", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                            }
                        }
                    }
                }
                items(recs) { rec ->
                    FullRecommendationCard(
                        recommendation = rec,
                        onClick = {
                            val p = uiState.allProducts.find { it.productId == rec.productId }
                            if (p != null) { viewModel.selectProduct(p); navController.navigate("buyer_product/${rec.productId}") }
                        },
                        onAddToCart = {
                            val p = uiState.allProducts.find { it.productId == rec.productId }
                            if (p != null) viewModel.addToCart(p)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullRecommendationCard(
    recommendation: AIRecommendation,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = recommendation.imageUrl,
                    contentDescription = recommendation.cropName,
                    modifier = Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)).background(BuyerTheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(recommendation.cropName, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = BuyerTheme.textPrimary)
                    Text(recommendation.farmerName, fontSize = 12.sp, color = BuyerTheme.textSecondary)
                    Text("📍 ${recommendation.district}", fontSize = 11.sp, color = BuyerTheme.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(recommendation.quality, BuyerTheme.accent)
                        Chip("Grade ${recommendation.grade}", Color(0xFF7B1FA2))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${String.format("%.0f", recommendation.pricePerKg)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BuyerTheme.primary)
                    Text("/kg", fontSize = 11.sp, color = BuyerTheme.textSecondary)
                    if (recommendation.savingsPercent > 0) {
                        Spacer(Modifier.height(2.dp))
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                            Text("↓${recommendation.savingsPercent.toInt()}%", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = BuyerTheme.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            // AI Score Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AI Score", fontSize = 11.sp, color = BuyerTheme.textSecondary)
                Spacer(Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { recommendation.aiScore / 100f },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(4.dp)),
                    color = BuyerTheme.accent,
                    trackColor = BuyerTheme.primaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text("${recommendation.aiScore.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BuyerTheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Surface(color = BuyerTheme.primaryContainer, shape = RoundedCornerShape(10.dp)) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = BuyerTheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(recommendation.aiReason, fontSize = 12.sp, color = BuyerTheme.primary, modifier = Modifier.weight(1f))
                }
            }
            if (recommendation.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recommendation.tags.forEach { tag -> Chip(tag, BuyerTheme.accentAmber) }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, BuyerTheme.primary)
                ) {
                    Icon(Icons.Rounded.AddShoppingCart, null, tint = BuyerTheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add to Cart", color = BuyerTheme.primary, fontSize = 13.sp)
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Deal", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CHECKOUT SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerCheckoutScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cart = uiState.cartSummary
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }
    var addressLine1 by remember { mutableStateOf("") }
    var addressCity by remember { mutableStateOf("") }
    var addressDistrict by remember { mutableStateOf("") }
    var addressPincode by remember { mutableStateOf("") }
    var addressName by remember { mutableStateOf("") }
    var addressPhone by remember { mutableStateOf("") }

    // Success state
    val checkoutResult = uiState.checkoutResult
    if (checkoutResult != null) {
        LaunchedEffect(checkoutResult) {
            viewModel.clearCheckoutResult()
            navController.navigate(Screen.BuyerOrders.route) {
                popUpTo(Screen.BuyerDashboard.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).background(Color(0xFFF8FAF8)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delivery Address
                item {
                    Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BuyerTheme.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = addressName, onValueChange = { addressName = it }, label = { Text("Full Name") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                                OutlinedTextField(value = addressPhone, onValueChange = { addressPhone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                            }
                            OutlinedTextField(value = addressLine1, onValueChange = { addressLine1 = it }, label = { Text("Street Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = addressCity, onValueChange = { addressCity = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                                OutlinedTextField(value = addressPincode, onValueChange = { addressPincode = it }, label = { Text("Pincode") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp))
                            }
                        }
                    }
                }

                // Payment Method
                item {
                    Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BuyerTheme.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            PaymentMethod.values().forEach { method ->
                                RadioRow(
                                    label = method.name.replace("_", " "),
                                    selected = selectedPayment == method,
                                    onClick = { selectedPayment = method }
                                )
                            }
                        }
                    }
                }

                // Order Summary
                item {
                    Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BuyerTheme.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            cart.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${item.cropName} (${item.quantityKg.toInt()} kg)", fontSize = 13.sp, color = BuyerTheme.textPrimary, modifier = Modifier.weight(1f))
                                    Text("₹${String.format("%.2f", item.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 13.sp, color = BuyerTheme.textSecondary)
                                Text("₹${String.format("%.2f", cart.subtotal)}", fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery", fontSize = 13.sp, color = BuyerTheme.textSecondary)
                                Text(if (cart.deliveryCharge == 0.0) "FREE" else "₹${String.format("%.2f", cart.deliveryCharge)}", fontSize = 13.sp, color = if (cart.deliveryCharge == 0.0) BuyerTheme.accent else BuyerTheme.textPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("GST (5%)", fontSize = 13.sp, color = BuyerTheme.textSecondary)
                                Text("₹${String.format("%.2f", cart.gst)}", fontSize = 13.sp)
                            }
                            HorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text("₹${String.format("%.2f", cart.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BuyerTheme.primary)
                            }
                        }
                    }
                }
            }

            // Place Order Button
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)), color = BuyerTheme.primary)
                        Spacer(Modifier.height(8.dp))
                    }
                    uiState.error?.let { err ->
                        Text(err, color = BuyerTheme.accentRed, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                    }
                    Button(
                        onClick = {
                            val address = DeliveryAddress(
                                name = addressName, phone = addressPhone,
                                line1 = addressLine1, city = addressCity,
                                district = addressDistrict, pincode = addressPincode
                            )
                            viewModel.placeOrder(address, selectedPayment)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && cart.items.isNotEmpty() && addressLine1.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Place Order • ₹${String.format("%.2f", cart.total)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Estimated delivery: 2-3 business days", fontSize = 11.sp, color = BuyerTheme.textSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = BuyerTheme.primary))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, color = BuyerTheme.textPrimary, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

// Helper reference to Screen object
private object Screen {
    val BuyerDashboard get() = com.krishiai.app.ui.navigation.Screen.BuyerDashboard
    val BuyerOrders get() = com.krishiai.app.ui.navigation.Screen.BuyerOrders
}
