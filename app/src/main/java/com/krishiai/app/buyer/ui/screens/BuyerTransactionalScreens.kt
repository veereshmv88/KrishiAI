package com.krishiai.app.buyer.ui.screens

import com.krishiai.app.buyer.ui.components.*

import androidx.compose.animation.*
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
import com.krishiai.app.buyer.data.model.*
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.ui.theme.*

object BuyerTheme {
    val primary = BuyerPrimaryGreen
    val primaryLight = BuyerSecondaryGreen
    val primaryContainer = LightPrimaryContainer
    val accent = BuyerAccentGreen
    val accentAmber = BuyerWarning
    val accentRed = BuyerDanger
    val surface = BuyerLightBackground
    val cardBg = Color.White
    val textPrimary = Color(0xFF1B2036)
    val textSecondary = Color(0xFF546E7A)
    val gradientGreen = listOf(BuyerPrimaryGreen, BuyerSecondaryGreen)
    val gradientAmber = listOf(BuyerWarning, BuyerPremiumGold)
}

// ─────────────────────────────────────────────
// SEARCH SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerSearchScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Spices", "Oilseeds")
    var selectedCategory by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAF8))) {
        // Search Header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(BuyerTheme.primary)
                .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                    Text("Search Crops & Sellers", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                // Search Field
                TextField(
                    value = query,
                    onValueChange = { q ->
                        query = q
                        if (q.length >= 2) viewModel.search(q)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search tomato, onion, potato...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = {
                        Row {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = ""; viewModel.search("") }) {
                                    Icon(Icons.Rounded.Clear, null)
                                }
                            }
                            IconButton(onClick = { /* Voice */ }) {
                                Icon(Icons.Rounded.Mic, null, tint = BuyerTheme.primary)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        // Category Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = {
                        selectedCategory = cat
                        val filter = if (cat == "All") BuyerSearchFilters() else BuyerSearchFilters(category = cat)
                        viewModel.updateFilters(filter)
                        if (query.isNotEmpty()) viewModel.search(query, filter)
                    },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BuyerTheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Filter Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (query.isEmpty()) "${uiState.allProducts.size} crops available" else "${uiState.searchResults.size} results for \"$query\"",
                fontSize = 13.sp, color = BuyerTheme.textSecondary
            )
            OutlinedButton(
                onClick = { showFilters = !showFilters },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BuyerTheme.primary)
            ) {
                Icon(Icons.Rounded.FilterList, null, tint = BuyerTheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filter", fontSize = 13.sp, color = BuyerTheme.primary)
            }
        }

        // Results
        val products = if (query.isEmpty()) uiState.allProducts else uiState.searchResults
        if (products.isEmpty() && query.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.SearchOff, null, tint = BuyerTheme.textSecondary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No results for \"$query\"", fontSize = 16.sp, color = BuyerTheme.textSecondary)
                    Text("Try a different search term", fontSize = 13.sp, color = BuyerTheme.textSecondary.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(products) { product ->
                    SearchProductCard(
                        product = product,
                        onClick = {
                            viewModel.selectProduct(product)
                            navController.navigate("buyer_product/${product.productId}")
                        },
                        onAddToCart = { viewModel.addToCart(product) },
                        onWishlist = { viewModel.toggleWishlist(product) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchProductCard(
    product: BuyerProduct,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onWishlist: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.cropName,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(BuyerTheme.primaryContainer),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(product.cropName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BuyerTheme.textPrimary)
                    IconButton(onClick = onWishlist, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.FavoriteBorder, null, tint = BuyerTheme.accentRed, modifier = Modifier.size(18.dp))
                    }
                }
                Text(product.farmerName, fontSize = 12.sp, color = BuyerTheme.textSecondary)
                Text("📍 ${product.district}, ${product.taluk}", fontSize = 11.sp, color = BuyerTheme.textSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip(product.quality, BuyerTheme.accent)
                    Chip("Grade ${product.grade}", Color(0xFF7B1FA2))
                    if (product.isOrganic) Chip("🌿 Organic", Color(0xFF2E7D32))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("₹${String.format("%.0f", product.pricePerKg)}/kg",
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BuyerTheme.primary)
                        Text("${String.format("%.0f", product.quantityKg)} kg available", fontSize = 11.sp, color = BuyerTheme.textSecondary)
                    }
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// CART SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerCartScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val cart = uiState.cartSummary
    var couponCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cart (${cart.itemCount})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        if (cart.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = BuyerTheme.textSecondary.copy(0.4f), modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Your cart is empty", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = BuyerTheme.textPrimary)
                    Text("Add crops from the marketplace", fontSize = 14.sp, color = BuyerTheme.textSecondary)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { navController.navigate("buyer_search") }, colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary)) {
                        Text("Browse Crops")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cart.items) { item ->
                        CartItemCard(
                            item = item,
                            onIncrease = { viewModel.updateCartQuantity(item.cartItemId, item.quantityKg + 1) },
                            onDecrease = {
                                if (item.quantityKg > 1) viewModel.updateCartQuantity(item.cartItemId, item.quantityKg - 1)
                                else viewModel.removeFromCart(item.cartItemId)
                            },
                            onRemove = { viewModel.removeFromCart(item.cartItemId) }
                        )
                    }
                    item {
                        // Coupon
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocalOffer, null, tint = BuyerTheme.accentAmber, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                TextField(
                                    value = couponCode,
                                    onValueChange = { couponCode = it },
                                    placeholder = { Text("Enter coupon code") },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                    singleLine = true
                                )
                                TextButton(onClick = { /* Apply coupon */ }) { Text("Apply", color = BuyerTheme.primary) }
                            }
                        }
                    }
                    item {
                        // Price Breakdown
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Price Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                PriceRow("Subtotal (${cart.itemCount} items)", "₹${String.format("%.2f", cart.subtotal)}")
                                PriceRow("Delivery Charges", if (cart.deliveryCharge == 0.0) "FREE" else "₹${String.format("%.2f", cart.deliveryCharge)}")
                                PriceRow("GST (5%)", "₹${String.format("%.2f", cart.gst)}")
                                if (cart.discount > 0) PriceRow("Discount", "-₹${String.format("%.2f", cart.discount)}", BuyerTheme.accent)
                                HorizontalDivider()
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Amount", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text("₹${String.format("%.2f", cart.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BuyerTheme.primary)
                                }
                            }
                        }
                    }
                }
                // Checkout Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { navController.navigate("buyer_checkout") },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BuyerTheme.primary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(Icons.Rounded.Payment, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Proceed to Checkout • ₹${String.format("%.2f", cart.total)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit, onRemove: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.cropName,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)).background(BuyerTheme.primaryContainer),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.cropName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(item.farmerName, fontSize = 12.sp, color = BuyerTheme.textSecondary)
                Text("₹${String.format("%.0f", item.pricePerKg)}/kg", fontSize = 13.sp, color = BuyerTheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Rounded.Close, null, tint = BuyerTheme.accentRed, modifier = Modifier.size(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = BuyerTheme.primaryContainer, modifier = Modifier.size(28.dp), onClick = onDecrease) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Remove, null, tint = BuyerTheme.primary, modifier = Modifier.size(16.dp)) }
                    }
                    Text("  ${item.quantityKg.toInt()} kg  ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Surface(shape = CircleShape, color = BuyerTheme.primary, modifier = Modifier.size(28.dp), onClick = onIncrease) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
                Text("₹${String.format("%.2f", item.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BuyerTheme.textPrimary)
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = BuyerTheme.textPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = BuyerTheme.textSecondary)
        Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
// ORDERS SCREEN
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerOrdersScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("All", "Processing", "Accepted", "Packed", "Shipped", "Delivered", "Cancelled")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BuyerTheme.primary, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedOrderTab,
                containerColor = Color.White,
                contentColor = BuyerTheme.primary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.selectedOrderTab == index,
                        onClick = { viewModel.setOrderTab(index) },
                        text = { Text(tab, fontSize = 13.sp) }
                    )
                }
            }

            val filtered = if (uiState.selectedOrderTab == 0) uiState.orders
            else uiState.orders.filter { it.status.name == tabs[uiState.selectedOrderTab].uppercase() }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.ShoppingBag, null, tint = BuyerTheme.textSecondary.copy(0.4f), modifier = Modifier.size(80.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No orders yet", fontSize = 16.sp, color = BuyerTheme.textSecondary)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered) { order ->
                        OrderCard(order = order, onClick = { /* Open order detail */ })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderCard(order: BuyerOrder, onClick: () -> Unit) {
    val statusColor = when (order.status) {
        OrderStatus.DELIVERED -> BuyerTheme.accent
        OrderStatus.CANCELLED -> BuyerTheme.accentRed
        OrderStatus.SHIPPED -> Color(0xFF1565C0)
        else -> BuyerTheme.accentAmber
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BuyerTheme.textPrimary)
                    Spacer(Modifier.width(8.dp))
                    if (order.syncState == SyncState.PENDING) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = "Sync Pending", tint = BuyerTheme.textSecondary, modifier = Modifier.size(16.dp))
                    } else if (order.syncState == SyncState.FAILED) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = "Sync Failed", tint = BuyerTheme.accentRed, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Rounded.CloudDone, contentDescription = "Synced", tint = BuyerTheme.accent, modifier = Modifier.size(16.dp))
                    }
                }
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(order.status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = order.imageUrl,
                    contentDescription = order.cropName,
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(BuyerTheme.primaryContainer),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.cropName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("From: ${order.farmerName}", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                    Text("${String.format("%.1f", order.quantityKg)} kg • ₹${String.format("%.0f", order.pricePerKg)}/kg", fontSize = 12.sp, color = BuyerTheme.textSecondary)
                }
                Text("₹${String.format("%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BuyerTheme.primary)
            }
            // Tracking timeline (simplified)
            if (order.status == OrderStatus.SHIPPED || order.status == OrderStatus.DELIVERED) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                OrderTimeline(order.status)
            }
        }
    }
}

@Composable
private fun OrderTimeline(status: OrderStatus) {
    val steps = listOf("Ordered", "Accepted", "Packed", "Shipped", "Delivered")
    val currentStep = when (status) {
        OrderStatus.PROCESSING -> 0; OrderStatus.ACCEPTED -> 1; OrderStatus.PACKED -> 2
        OrderStatus.SHIPPED -> 3; OrderStatus.DELIVERED -> 4; else -> -1
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        steps.forEachIndexed { idx, step ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(20.dp)
                        .background(if (idx <= currentStep) BuyerTheme.accent else Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (idx <= currentStep) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
                Spacer(Modifier.height(3.dp))
                Text(step, fontSize = 8.sp, color = if (idx <= currentStep) BuyerTheme.accent else BuyerTheme.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
