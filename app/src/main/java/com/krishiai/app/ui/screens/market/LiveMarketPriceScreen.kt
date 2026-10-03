package com.krishiai.app.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.krishiai.app.data.local.entity.MarketPriceEntity
import com.krishiai.app.domain.ai.MarketRecommendation
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMarketPriceScreen(
    navController: NavController,
    viewModel: LiveMarketPriceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Market Prices", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchLocationAndPrices() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is MarketUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MarketUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchLocationAndPrices() }) {
                            Text("Retry")
                        }
                    }
                }
                is MarketUiState.Empty -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var selectedDistrict by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(null) }
                        
                        com.krishiai.app.ui.components.SmartLocationPicker(
                            label = "Search Market by District",
                            selectedLocation = selectedDistrict,
                            onLocationSelected = { node ->
                                selectedDistrict = node
                                viewModel.fetchPricesForDistrict(node.name)
                            },
                            filterType = com.krishiai.app.core.location.LocationType.DISTRICT,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Icon(Icons.Rounded.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No live market data available for this region.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchLocationAndPrices() }) {
                            Text("Refresh")
                        }
                    }
                }
                is MarketUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location Header
                        item {
                            var selectedDistrict by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(null) }
                            
                            // Initialize selectedDistrict when state location is available
                            LaunchedEffect(state.location?.district) {
                                if (selectedDistrict == null && !state.location?.district.isNullOrEmpty()) {
                                    selectedDistrict = com.krishiai.app.core.location.LocationNode("tmp", state.location!!.district, com.krishiai.app.core.location.LocationType.DISTRICT)
                                }
                            }
                            
                            com.krishiai.app.ui.components.SmartLocationPicker(
                                label = "Search Market by District",
                                selectedLocation = selectedDistrict,
                                onLocationSelected = { node ->
                                    selectedDistrict = node
                                    viewModel.fetchPricesForDistrict(node.name)
                                },
                                filterType = com.krishiai.app.core.location.LocationType.DISTRICT,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            )
                        }

                        // AI Recommendation
                        state.recommendation?.let { rec ->
                            item {
                                AIRecommendationCard(rec)
                            }
                        }

                        // Price List
                        if (state.prices.isEmpty()) {
                            item {
                                Text("No market prices found for your region.")
                            }
                        } else {
                            items(state.prices) { price ->
                                MarketPriceCard(price)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIRecommendationCard(recommendation: MarketRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = BrandGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Insight: ${recommendation.action}", fontWeight = FontWeight.Bold, color = BrandGreen)
                Spacer(modifier = Modifier.weight(1f))
                Text("${recommendation.confidence}% Confidence", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(recommendation.reasoning, style = MaterialTheme.typography.bodyMedium)
            
            recommendation.estimatedAdditionalProfit?.let { profit ->
                Spacer(modifier = Modifier.height(8.dp))
                Text("Est. Extra Profit: ₹${String.format("%.2f", profit)}/Quintal", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            }
        }
    }
}

@Composable
fun MarketPriceCard(price: MarketPriceEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(price.commodity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("₹${price.modalPrice}/Q", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = BrandGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Storefront, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${price.market}, ${price.district}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                PriceMetric("Min Price", "₹${price.minPrice}")
                PriceMetric("Max Price", "₹${price.maxPrice}")
                PriceMetric("Variety", price.variety)
            }
        }
    }
}

@Composable
fun PriceMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
