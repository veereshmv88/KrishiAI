package com.krishiai.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.krishiai.app.ui.components.AIGradientBadge
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.screens.farmer.FarmerViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    farmerViewModel: FarmerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val weatherUiState by farmerViewModel.weatherUiState.collectAsStateWithLifecycle()
    val location by farmerViewModel.locationResult.collectAsStateWithLifecycle()
    val intelligence by farmerViewModel.marketIntelligence.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            farmerViewModel.fetchCurrentLocation()
            farmerViewModel.loadDashboardData(user.uid, user.district)
            farmerViewModel.loadWeatherForDashboard(forceRefresh = false)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { AppBottomNavigation(navController = navController, activeRoute = Screen.Home.route) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            // Header
            item {
                DashboardTopHeader(
                    navController = navController,
                    userName = currentUser?.fullName ?: "Loading..."
                )
            }
            
            // Location & Weather Pill
            item {
                val weatherDesc = (weatherUiState as? com.krishiai.app.ui.screens.weather.WeatherUiState.Success)?.weatherData?.condition ?: "Fetching Weather..."
                val temp = (weatherUiState as? com.krishiai.app.ui.screens.weather.WeatherUiState.Success)?.weatherData?.currentTemperature?.toInt()?.toString()?.plus("°C") ?: "--°C"
                
                LocationWeatherPill(
                    locationName = location?.district ?: currentUser?.district ?: "Loading Location...",
                    weatherDesc = weatherDesc,
                    temp = temp
                )
            }

            // Quick Actions
            item {
                QuickActionsRow(navController)
            }

            // Revenue & Analytics
            item {
                AnalyticsSection(revenueStats = farmerViewModel.revenueStats.collectAsStateWithLifecycle().value)
            }

            // Market Intelligence (Live APMC Prices, Trending Crops)
            item {
                MarketIntelligenceSection(intelligence = intelligence)
            }

            // AI Tools
            item {
                AIToolsSection(navController)
            }

            // AI Models & Technology (The Showcase)
            item {
                AIModelsTechnologySection()
            }
            
            // Recent Activity
            item {
                RecentActivitySection()
            }
        }
    }
}

@Composable
fun DashboardTopHeader(navController: NavController, userName: String = "Ramesh Patil") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Good Morning,",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = { navController.navigate(Screen.GlobalSearch.route) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            com.krishiai.app.ui.components.NotificationIcon(
                onClick = { navController.navigate(Screen.NotificationCenter.route) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(40.dp)
            )
        }
    }
}

@Composable
fun LocationWeatherPill(locationName: String = "Belagavi, Karnataka", weatherDesc: String = "Partly Cloudy", temp: String = "24°C") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = locationName,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        
        // Weather
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WbCloudy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$temp $weatherDesc",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            )
        }
    }
}

@Composable
fun QuickActionsRow(navController: NavController) {
    val actions = listOf(
        Triple("Sell Crop", Icons.Rounded.AddCircle, Screen.SellCrop.route),
        Triple("Market", Icons.Rounded.Storefront, Screen.Marketplace.route),
        Triple("AI Tools", Icons.Rounded.AutoAwesome, Screen.AITools.route),
        Triple("My Crops", Icons.Rounded.Inventory, Screen.MyProducts.route),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(actions) { action ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { navController.navigate(action.third) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = action.second,
                        contentDescription = action.first,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = action.first,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun AnalyticsSection(revenueStats: com.krishiai.app.data.model.RevenueStats = com.krishiai.app.data.model.RevenueStats()) {
    Column {
        SectionHeader(title = "Revenue Analytics", subtitle = "Your overall performance")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PremiumCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Total Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (revenueStats.totalRevenue > 0) "₹${"%,.0f".format(revenueStats.totalRevenue)}" else "₹0",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            PremiumCard(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Total Deals", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${revenueStats.totalDeals} Deals",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketIntelligenceSection(intelligence: List<com.krishiai.app.data.model.MarketIntelligence>) {
    Column {
        SectionHeader(title = "Market Intelligence", subtitle = "Live APMC Prices & Trends", actionText = "See All", onActionClick = {})
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (intelligence.isEmpty()) {
                item { Text("No market data available yet", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(intelligence) { item ->
                    val isPositive = item.expectedMovement == com.krishiai.app.data.model.PriceMovement.UP || item.expectedMovement == com.krishiai.app.data.model.PriceMovement.STABLE
                    val changeStr = if (item.expectedMovement == com.krishiai.app.data.model.PriceMovement.STABLE) "0%" else "${if(isPositive) "+" else "-"}${item.expectedMovementPercent}%"
                    val demandStr = item.demandLevel.name.replace("_", " ").lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + " Demand"
                    MarketTrendCard(
                        name = item.cropName,
                        price = "₹${item.currentPrice.toInt()}/kg",
                        change = changeStr,
                        isPositive = isPositive,
                        demand = demandStr
                    )
                }
            }
        }
    }
}

@Composable
fun MarketTrendCard(name: String, price: String, change: String, isPositive: Boolean, demand: String) {
    PremiumCard(
        modifier = Modifier.width(140.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = price, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(8.dp))
            
            val changeColor = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(changeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall.copy(color = changeColor, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = demand, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun AIToolsSection(navController: NavController) {
    Column {
        SectionHeader(title = "AI Tools", subtitle = "Powered by KrishiAI Core")
        
        val tools = listOf(
            Triple("Price Prediction", Icons.Rounded.Timeline, "AI predicts the best price based on trends."),
            Triple("Disease Detection", Icons.Rounded.CameraAlt, "Scan crops to detect diseases instantly."),
            Triple("Profit Estimation", Icons.Rounded.Calculate, "Estimate margins and cost of farming."),
            Triple("Negotiation Assistant", Icons.Rounded.Handshake, "AI advises you when dealing with buyers.")
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tools) { tool ->
                PremiumCard(
                    modifier = Modifier.width(220.dp),
                    onClick = { navController.navigate(Screen.AITools.route) }
                ) {
                    AIGradientBadge(text = "KrishiAI Engine", icon = Icons.Rounded.AutoAwesome)
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(imageVector = tool.second, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = tool.first, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = tool.third, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }
    }
}

@Composable
fun AIModelsTechnologySection() {
    Column {
        SectionHeader(title = "AI Models & Technology", subtitle = "The powerful engine behind KrishiAI")
        
        val technologies = listOf(
            Triple("Random Forest", "Scikit-Learn Regression", "92% Acc"),
            Triple("TensorFlow Lite", "On-Device Inference", "95% Acc"),
            Triple("Google ML Kit", "OCR & Vision", "Fast"),
            Triple("Android Speech", "Voice Recognition", "Native"),
            Triple("Python", "Pandas & NumPy", "Backend"),
            Triple("Jetpack Compose", "Material 3", "UI"),
            Triple("Room & Firebase", "Offline-First Data", "Sync")
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(technologies) { tech ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .width(160.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Rounded.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = tech.third,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = tech.first, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = tech.second, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column {
        SectionHeader(title = "Recent Activity")
        
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityRow("AI Recommendation", "High demand for Onion in APMC.", "2 hrs ago", Icons.Rounded.AutoAwesome, MaterialTheme.colorScheme.primary)
            ActivityRow("Listing Published", "100kg Tomato successfully listed.", "5 hrs ago", Icons.Rounded.CheckCircle, MaterialTheme.colorScheme.secondary)
            ActivityRow("New Transaction", "Payment of ₹4,500 received.", "1 day ago", Icons.Rounded.Payments, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun ActivityRow(title: String, desc: String, time: String, icon: ImageVector, iconColor: Color) {
    PremiumCard(contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
            Text(text = time, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
