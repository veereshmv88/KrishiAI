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
import com.krishiai.app.buyer.ai.BuyingDecision
import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.buyer.ui.BuyerMainViewModel

// ─────────────────────────────────────────────
// PRODUCT DETAIL SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerProductDetailScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel,
    productId: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val product = uiState.selectedProduct ?: uiState.allProducts.find { it.productId == productId }
    val decision = uiState.buyingDecision
    val prediction = product?.let { uiState.pricePredictions[it.cropName] }

    var quantityKg by remember { mutableDoubleStateOf(1.0) }
    var showAddedToCart by remember { mutableStateOf(false) }

    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BuyerTheme.primary)
        }
        return
    }

    LaunchedEffect(productId) {
        viewModel.selectProduct(product)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.cropName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(product) }) {
                        Icon(Icons.Rounded.FavoriteBorder, null, tint = BuyerTheme.accentRed)
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Rounded.Share, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(product, quantityKg)
                            showAddedToCart = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, BuyerTheme.primary)
                    ) {
                        Icon(Icons.Rounded.AddShoppingCart, null, tint = BuyerTheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add to Cart", color = BuyerTheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            viewModel.addToCart(product, quantityKg)
                            navController.navigate("buyer_checkout")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.FlashOn, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Product Image
            item {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.cropName,
                    modifier = Modifier.fillMaxWidth().height(280.dp).background(BuyerTheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
            }

            // Main Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(product.cropName, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = BuyerTheme.textPrimary)
                                Text(product.cropCategory, fontSize = 14.sp, color = BuyerTheme.textSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%.0f", product.pricePerKg)}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = BuyerTheme.primary)
                                Text("/kg", fontSize = 13.sp, color = BuyerTheme.textSecondary)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // Tags
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip(product.quality, BuyerTheme.accent)
                            Chip("Grade ${product.grade}", Color(0xFF7B1FA2))
                            if (product.isOrganic) Chip("🌿 Organic", Color(0xFF2E7D32))
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))
                        // Details Grid
                        DetailGrid(listOf(
                            "Quantity" to "${String.format("%.0f", product.quantityKg)} kg",
                            "Harvest Date" to if (product.harvestDate > 0) {
                                val days = ((System.currentTimeMillis() - product.harvestDate) / (1000 * 60 * 60 * 24)).toInt()
                                if (days == 0) "Today" else "$days days ago"
                            } else "N/A",
                            "District" to product.district,
                            "Taluk" to product.taluk,
                            "Variety" to product.variety.ifEmpty { "Standard" },
                            "Freshness" to if (product.freshnessScore > 0) "${product.freshnessScore.toInt()}%" else "Good"
                        ))
                        if (product.description.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(product.description, fontSize = 13.sp, color = BuyerTheme.textSecondary)
                        }
                    }
                }
            }

            // Quantity Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Select Quantity", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Total: ₹${String.format("%.2f", product.pricePerKg * quantityKg)}", fontSize = 13.sp, color = BuyerTheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = BuyerTheme.primaryContainer, modifier = Modifier.size(36.dp), onClick = { if (quantityKg > 1) quantityKg-- }) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Remove, null, tint = BuyerTheme.primary) }
                            }
                            Text("${quantityKg.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.widthIn(min = 48.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Surface(shape = CircleShape, color = BuyerTheme.primary, modifier = Modifier.size(36.dp), onClick = { if (quantityKg < product.quantityKg) quantityKg++ }) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, null, tint = Color.White) }
                            }
                        }
                    }
                }
            }

            // AI Buying Decision
            if (decision != null) {
                item {
                    Spacer(Modifier.height(16.dp))
                    BuyingDecisionCard(decision)
                }
            }

            // Seller Section
            item {
                Spacer(Modifier.height(16.dp))
                SellerCard(product, navController)
            }
        }
    }
}

@Composable
private fun DetailGrid(details: List<Pair<String, String>>) {
    details.chunked(2).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { (label, value) ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, fontSize = 11.sp, color = BuyerTheme.textSecondary)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BuyerTheme.textPrimary)
                }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BuyingDecisionCard(decision: BuyingDecision) {
    val bgColor = when (decision.recommendation) {
        "BUY_NOW" -> BuyerTheme.primaryContainer
        "WAIT" -> Color(0xFFFFF8E1)
        else -> Color(0xFFE3F2FD)
    }
    val icon = when (decision.recommendation) {
        "BUY_NOW" -> Icons.Rounded.CheckCircle
        "WAIT" -> Icons.Rounded.AccessTime
        else -> Icons.Rounded.LocationOn
    }
    val iconColor = when (decision.recommendation) {
        "BUY_NOW" -> BuyerTheme.accent
        "WAIT" -> BuyerTheme.accentAmber
        else -> Color(0xFF1565C0)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AI Recommendation: ${decision.recommendation.replace("_", " ")}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyerTheme.textPrimary)
                Text(decision.reason, fontSize = 12.sp, color = BuyerTheme.textSecondary)
                if (decision.estimatedSavings > 0) {
                    Text("Potential savings: ₹${String.format("%.2f", decision.estimatedSavings)}/kg", fontSize = 12.sp, color = BuyerTheme.accent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SellerCard(product: BuyerProduct, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Seller Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = BuyerTheme.primaryContainer, modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(product.farmerName.firstOrNull()?.uppercaseChar()?.toString() ?: "F",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BuyerTheme.primary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(product.farmerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (product.isVerifiedFarmer) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Rounded.Verified, null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("📍 ${product.district}, ${product.taluk}", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { idx ->
                            Icon(
                                if (idx < product.farmerRating.toInt()) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                null, tint = BuyerTheme.accentAmber, modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("${String.format("%.1f", product.farmerRating)} • ${product.totalFarmerDeals} deals",
                            fontSize = 12.sp, color = BuyerTheme.textSecondary)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Trust", fontSize = 10.sp, color = BuyerTheme.textSecondary)
                    Text("${product.farmerTrustScore.toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BuyerTheme.accent)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* Open chat */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.5.dp, BuyerTheme.primary)
            ) {
                Icon(Icons.Rounded.Chat, null, tint = BuyerTheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Chat with Seller", color = BuyerTheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


// ─────────────────────────────────────────────
// NEARBY SELLERS SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbySellerScreen(navController: NavController, viewModel: BuyerMainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Sellers") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (uiState.nearbySellersList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.LocationOff, null, tint = BuyerTheme.textSecondary.copy(0.4f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No sellers found nearby", fontSize = 16.sp, color = BuyerTheme.textSecondary)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadNearbySellers(12.9716, 77.5946) },
                        colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary)
                    ) { Text("Find Nearby Sellers") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.nearbySellersList) { seller ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = BuyerTheme.primaryContainer, modifier = Modifier.size(52.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(seller.farmerName.firstOrNull()?.uppercaseChar()?.toString() ?: "F", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BuyerTheme.primary)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(seller.farmerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (seller.isVerified) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Rounded.Verified, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text("📍 ${String.format("%.1f", seller.distanceKm)} km • ~${seller.etaMinutes} min", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Star, null, tint = BuyerTheme.accentAmber, modifier = Modifier.size(13.dp))
                                    Text(" ${String.format("%.1f", seller.rating)} • ${seller.totalDeals} deals", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Trust", fontSize = 10.sp, color = BuyerTheme.textSecondary)
                                Text("${seller.trustScore.toInt()}%", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BuyerTheme.accent)
                            }
                        }
                    }
                }
            }
        }
    }
}
