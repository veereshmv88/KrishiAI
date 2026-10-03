package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(
    navController: NavController,
    viewModel: CropRecommendationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var soilType by remember { mutableStateOf("Red Soil") }
    var season by remember { mutableStateOf("Kharif") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Crop Recommender", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFE3F2FD)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1565C0)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Spa, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Agronomy AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1)))
                        Text("Find the most profitable crops for your land", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0D47A1).copy(alpha = 0.8f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SectionHeader("Field Profile", "Select your soil and season") }

            item {
                Text("Soil Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Red Soil", "Black Soil", "Sandy").forEach { s ->
                        FilterChip(
                            selected = soilType == s,
                            onClick = { soilType = s },
                            label = { Text(s) }
                        )
                    }
                }
            }

            item {
                Text("Season", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Kharif", "Rabi", "Zaid").forEach { s ->
                        FilterChip(
                            selected = season == s,
                            onClick = { season = s },
                            label = { Text(s) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = { viewModel.getRecommendations(soilType, season) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Get Recommendations", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                }
            }

            when (val state = uiState) {
                is CropRecommendationState.Loading -> {
                    item {
                        PremiumCard(modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(0.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Analyzing soil compatibility...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
                is CropRecommendationState.Success -> {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    item { SectionHeader("Top Recommendations", "Ranked by profitability") }
                    items(state.crops) { crop ->
                        CropRecommendationCard(crop)
                    }
                }
                is CropRecommendationState.Error -> {
                    item {
                        PremiumCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CropRecommendationCard(crop: CropRecommendation) {
    PremiumCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Grass, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(crop.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Expected Yield: ${crop.expectedYield}", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "${crop.matchScore}% Match",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
