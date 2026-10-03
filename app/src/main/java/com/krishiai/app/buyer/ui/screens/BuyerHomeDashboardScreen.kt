package com.krishiai.app.buyer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.buyer.ai.AIRecommendation
import com.krishiai.app.buyer.ai.MarketInsight
import com.krishiai.app.buyer.data.model.BuyerProduct
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.BuyerGradientButton
import com.krishiai.app.buyer.ui.components.BuyerPremiumCard
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerHomeDashboardScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav
        ) {
            // ── 1. Premium Welcome Header ──
            item {
                BuyerWelcomeHeader(
                    userName = "Veeresh", 
                    profileUrl = "", 
                    location = "Hubballi, Karnataka",
                    weather = "28°C, Clear",
                    notificationCount = 3,
                    onNotificationClick = { navController.navigate("buyer_notifications") },
                    onProfileClick = { navController.navigate("buyer_profile") }
                )
            }

            // ── 2. Dashboard Overview (4 Quick Cards) ──
            item {
                Spacer(Modifier.height(16.dp))
                DashboardOverviewSection(
                    totalOrders = uiState.orders.size,
                    wishlistCount = uiState.wishlistCount,
                    savings = 1250, // Mock savings
                    onOrdersClick = { navController.navigate("buyer_orders") },
                    onWishlistClick = { navController.navigate("buyer_wishlist") }
                )
            }

            // ── 3. AI Best Deal Today (Hero Banner) ──
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("🔥 AI Best Deal Today", "See All") {
                    navController.navigate("buyer_recommendations")
                }
                if (uiState.bestDeal != null) {
                    AIBestDealHeroBanner(deal = uiState.bestDeal!!) {
                        navController.navigate("buyer_product/${uiState.bestDeal!!.productId}")
                    }
                } else {
                    LoadingSkeleton(Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp))
                }
            }

            // ── 4. AI Insights ──
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("🧠 AI Tools", "Explore") {
                    navController.navigate("buyer_ai_tools")
                }
                AIInsightsCard()
            }

            // ── 5. Live Market Prices (AGMARKNET) ──
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("📈 Live Market Prices", "View Map") {
                    navController.navigate("buyer_market_intelligence")
                }
                LiveMarketTicker(insights = uiState.marketInsights)
            }

            // ── 6. Recommended For You ──
            item {
                Spacer(Modifier.height(24.dp))
                SectionHeader("✨ Recommended For You", null) {}
                RecommendedProductsRow(
                    recommendations = uiState.recommendations,
                    onProductClick = { navController.navigate("buyer_product/${it}") }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// COMPONENTS
// ─────────────────────────────────────────────────────────────────

@Composable
fun BuyerWelcomeHeader(
    userName: String,
    profileUrl: String,
    location: String,
    weather: String,
    notificationCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(BuyerPrimaryGreen, BuyerAccentGreen)))
            .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileUrl.isNotEmpty()) {
                        AsyncImage(model = profileUrl, contentDescription = "Profile", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = BuyerPrimaryGreen, modifier = Modifier.size(32.dp))
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Greeting & Location
                Column {
                    Text("Good Morning,", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                    Text(userName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(location, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(weather, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Notification Badge
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Badge(containerColor = BuyerPremiumOrange) { Text("$notificationCount", color = Color.White) }
                    }
                }
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardOverviewSection(
    totalOrders: Int,
    wishlistCount: Int,
    savings: Int,
    onOrdersClick: () -> Unit,
    onWishlistClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OverviewCard(
            title = "Orders",
            value = totalOrders.toString(),
            icon = Icons.AutoMirrored.Rounded.List,
            color = BuyerBlue,
            modifier = Modifier.weight(1f),
            onClick = onOrdersClick
        )
        OverviewCard(
            title = "Wishlist",
            value = wishlistCount.toString(),
            icon = Icons.Rounded.Favorite,
            color = BuyerDanger,
            modifier = Modifier.weight(1f),
            onClick = onWishlistClick
        )
        OverviewCard(
            title = "Savings",
            value = "₹$savings",
            icon = Icons.Rounded.Savings,
            color = BuyerPremiumGold,
            modifier = Modifier.weight(1f)
        ) {}
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AIBestDealHeroBanner(deal: AIRecommendation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            // Background Image
            AsyncImage(
                model = deal.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    ))
            )
            
            // Badges
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(color = BuyerPremiumGold, shape = RoundedCornerShape(12.dp)) {
                    Text("Top Deal", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
                Surface(color = BuyerAIGreen.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI Score: ${deal.aiScore.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Text(deal.cropName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("By ${deal.farmerName}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.Star, null, tint = BuyerPremiumGold, modifier = Modifier.size(14.dp))
                    Text(" 4.5 • ${String.format("%.1f", deal.distanceKm)} km", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("₹${deal.pricePerKg}", color = BuyerPremiumGold, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("/kg", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("₹${deal.pricePerKg + 15}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun AIInsightsCard() {
    BuyerPremiumCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(BuyerAIBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = BuyerAIBlue)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Buying Tip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Onion prices are expected to drop by 5% next week. Wait before bulk buying.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun LiveMarketTicker(insights: List<MarketInsight>) {
    if (insights.isEmpty()) {
        LoadingSkeleton(Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 16.dp))
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(insights) { insight ->
            Card(
                modifier = Modifier.width(160.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(insight.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Local APMC", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${insight.weeklyTrend.lastOrNull() ?: 1200.0}/q", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = BuyerPrimaryGreen)
                        Spacer(Modifier.weight(1f))
                        val isUp = insight.priceMovement == "UP"
                        Icon(
                            if (isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isUp) BuyerDanger else BuyerAIGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedProductsRow(recommendations: List<AIRecommendation>, onProductClick: (String) -> Unit) {
    if (recommendations.isEmpty()) {
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) { LoadingSkeleton(Modifier.width(160.dp).height(240.dp)) }
        }
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(recommendations) { rec ->
            Card(
                modifier = Modifier.width(160.dp).clickable { onProductClick(rec.productId) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                        AsyncImage(model = rec.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Surface(
                            color = BuyerAIGreen,
                            shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text("${rec.aiScore.toInt()} AI", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(rec.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(rec.farmerName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${rec.pricePerKg}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = BuyerPrimaryGreen)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Rounded.Star, null, tint = BuyerPremiumGold, modifier = Modifier.size(12.dp))
                            Text("4.5", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String?, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (actionText != null) {
            Text(actionText, color = BuyerPrimaryGreen, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onActionClick() })
        }
    }
}

@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Gray.copy(alpha = alpha))
    )
}
