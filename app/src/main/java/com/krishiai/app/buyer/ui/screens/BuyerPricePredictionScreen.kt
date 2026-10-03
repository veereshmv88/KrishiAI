package com.krishiai.app.buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.*
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerPricePredictionScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val crops = uiState.allProducts.map { it.cropName }.distinct().take(10)
    var selectedCrop by remember { mutableStateOf(crops.firstOrNull() ?: "") }
    val prediction = uiState.pricePredictions[selectedCrop]

    LaunchedEffect(selectedCrop) {
        if (selectedCrop.isNotEmpty()) viewModel.loadPricePrediction(selectedCrop)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Price Prediction", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crop Selector
            item {
                Text("Select Commodity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(crops) { crop ->
                        FilterChip(
                            selected = selectedCrop == crop,
                            onClick = { selectedCrop = crop },
                            label = { Text(crop) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BuyerPrimaryGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (prediction != null) {
                // Prediction Summary
                item {
                    BuyerPremiumCard {
                        Text(selectedCrop, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = BuyerPrimaryGreen)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            PredictionStat("Current", "₹${String.format("%.0f", prediction.currentPrice)}", Color.Black)
                            PredictionStat("Tomorrow", "₹${String.format("%.0f", prediction.tomorrowPrice)}", if (prediction.tomorrowPrice > prediction.currentPrice) BuyerDanger else BuyerPrimaryGreen)
                            PredictionStat("7 Days", "₹${String.format("%.0f", prediction.week7Price)}", if (prediction.week7Price > prediction.currentPrice) BuyerDanger else BuyerPrimaryGreen)
                            PredictionStat("30 Days", "₹${String.format("%.0f", prediction.month30Price)}", Color.Gray)
                        }
                    }
                }

                // AI Advice
                item {
                    val adviceColor = when {
                        prediction.trend == "DOWN" -> BuyerSecondaryGreen
                        prediction.trend == "UP" -> BuyerWarning.copy(alpha = 0.8f)
                        else -> BuyerBlue.copy(alpha = 0.8f)
                    }
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = adviceColor),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("AI Advice", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(prediction.advice, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("Confidence: ${(prediction.confidenceScore * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Interactive Chart
                item {
                    BuyerPremiumCard {
                        Text("7-Day Forecast", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp))
                        
                        // Use the premium Canvas LineChart from Analytics Screen
                        LineChart(
                            data = prediction.predictedPrices.take(7),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEachIndexed { idx, day ->
                                Text(day, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BuyerPrimaryGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun PredictionStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
