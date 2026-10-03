package com.krishiai.app.ui.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen

import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.screens.weather.WeatherUiState
import com.krishiai.app.ui.screens.weather.WeatherLocationPicker
import kotlin.math.roundToInt
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    farmerViewModel: FarmerViewModel
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("KrishiAI", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(com.krishiai.app.ui.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        com.krishiai.app.ui.components.NotificationIcon(
                            onClick = { navController.navigate(Screen.NotificationCenter.route) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = BrandGreen,
                    actionIconContentColor = Color.DarkGray,
                    navigationIconContentColor = Color.DarkGray
                )
            )
        },
    ) { paddingValues ->
        val currentUser by authViewModel.currentUser.collectAsState()
        var showLocationPicker by remember { mutableStateOf(false) }
        
        if (showLocationPicker) {
            WeatherLocationPicker(
                onLocationSelected = { location ->
                    farmerViewModel.setWeatherForLocation(location)
                    showLocationPicker = false
                },
                onDismissRequest = { showLocationPicker = false }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Greeting Section
            item {
                val userName = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Farmer"
                val location = currentUser?.district?.takeIf { it.isNotBlank() }?.let { "$it, Karnataka" } ?: "Location Not Set"
                
                Column {
                    Text("Hello, $userName 👋", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                    Text("Good Morning, Farmer!", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showLocationPicker = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(location, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }
                }
            }
            
            // Weather Card
            item {
                val weatherUiState by farmerViewModel.weatherUiState.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    when (val state = weatherUiState) {
                        is WeatherUiState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = Color(0xFF1976D2))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Getting weather...", color = Color(0xFF1565C0))
                            }
                        }
                        is WeatherUiState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(state.message, color = Color(0xFF1565C0), style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (state.canRetry) {
                                        Button(
                                            onClick = { farmerViewModel.loadWeatherForDashboard(forceRefresh = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                        is WeatherUiState.Success -> {
                            val weather = state.weatherData
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Cloud, contentDescription = "Weather Icon", tint = Color(0xFF64B5F6), modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("${weather.currentTemperature.roundToInt()}°C", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                                            Text(weather.condition, style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(weather.locationName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)))
                                        Text("${weather.todayHigh.roundToInt()}° / ${weather.todayLow.roundToInt()}°", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    WeatherDetailColumn("Humidity", "${weather.humidity}%")
                                    WeatherDetailColumn("Wind", "${weather.windSpeed} km/h")
                                    WeatherDetailColumn("Rain Chance", "${weather.rainChance}%") 
                                }
                                
                                if (state.isCached) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val minsAgo = (System.currentTimeMillis() - weather.lastUpdated) / (1000 * 60)
                                    Text("Last updated: $minsAgo minutes ago (Offline)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                        is WeatherUiState.NoData -> {
                            // Do nothing or show placeholder
                        }
                    }
                }
            }

            // Market Overview
            item {
                SectionTitleRow(title = "Market Overview", actionText = "View All")
            }

            item {
                val apmcPrices by farmerViewModel.basePrices.collectAsState()
                
                if (apmcPrices.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(apmcPrices.take(5)) { price ->
                            MarketOverviewCard(
                                name = price.cropName, 
                                price = "₹${price.modalPrice} /kg", 
                                percentage = "0.0%", 
                                isUp = true
                            )
                        }
                    }
                } else {
                    Text("Live market data unavailable for this region.", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
            }

            // Your Farm Overview
            item {
                SectionTitleRow(title = "Your Farm Overview", actionText = "This Month")
            }

            item {
                val myProducts by farmerViewModel.myProducts.collectAsState()
                val revenueStats by farmerViewModel.revenueStats.collectAsState()
                
                val totalCrops = myProducts.size
                val activeListings = myProducts.count { it.status == "AVAILABLE" }
                val cropsSold = revenueStats.totalDeals
                val totalRevenue = "₹%,d".format(revenueStats.totalRevenue.toInt())

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FarmOverviewCard(modifier = Modifier.weight(1f), title = "Total Crops", value = totalCrops.toString(), icon = Icons.Rounded.Eco, iconTint = BrandGreen)
                        FarmOverviewCard(modifier = Modifier.weight(1f), title = "Active Listings", value = activeListings.toString(), icon = Icons.AutoMirrored.Rounded.ListAlt, iconTint = BrandGreen)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FarmOverviewCard(modifier = Modifier.weight(1f), title = "Crops Sold", value = cropsSold.toString(), icon = Icons.Rounded.DoneAll, iconTint = Color(0xFF1976D2))
                        FarmOverviewCard(modifier = Modifier.weight(1f), title = "Revenue", value = totalRevenue, icon = Icons.Rounded.Payments, iconTint = Color(0xFFF57C00))
                    }
                }
            }

            // AI Recommendation
            item {
                val marketIntell by farmerViewModel.marketIntelligence.collectAsState()
                val topRec = marketIntell.firstOrNull()

                Text("AI Recommendation", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (topRec != null) {
                                Text("Good to sell ${topRec.cropName} today!", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                                Text(topRec.insight, style = MaterialTheme.typography.bodySmall.copy(color = BrandGreen.copy(alpha = 0.8f)), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            } else {
                                Text("No recommendation available yet.", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                                Text("Add crops and fetch market data to see AI recommendations.", style = MaterialTheme.typography.bodySmall.copy(color = BrandGreen.copy(alpha = 0.8f)))
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("View Details", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                            }
                        }
                        Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(48.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun WeatherDetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun SectionTitleRow(title: String, actionText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(actionText, style = MaterialTheme.typography.labelSmall.copy(color = BrandGreen, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun MarketOverviewCard(name: String, price: String, percentage: String, isUp: Boolean) {
    Card(
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text(price, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isUp) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = if (isUp) BrandGreen else Color.Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(percentage, style = MaterialTheme.typography.labelSmall.copy(color = if (isUp) BrandGreen else Color.Red))
            }
        }
    }
}

@Composable
fun FarmOverviewCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}
