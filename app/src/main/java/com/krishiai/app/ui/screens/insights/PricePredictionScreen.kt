package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.domain.ai.PricePredictionResult
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricePredictionScreen(
    navController: NavController,
    viewModel: PricePredictionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var cropName by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var quality by remember { mutableStateOf("Average") }
    var quantityKg by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }
    var historicalApmcPrice by remember { mutableStateOf("") }
    var weatherCondition by remember { mutableStateOf("Clear") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Price Prediction", fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF3E5F5)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF8E24AA)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.TrendingUp, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Market AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4A148C)))
                        Text("Predict selling price based on real-time data", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A148C).copy(alpha = 0.8f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item { SectionHeader("Crop Details", "Enter the crop specifications") }
            item { ParameterInputField("Crop Name (e.g. Tomato)", cropName, { cropName = it }, Icons.Rounded.Agriculture, false) }
            item { ParameterInputField("District / Location", district, { district = it }, Icons.Rounded.LocationOn, false) }
            item { ParameterInputField("Quantity (Kg)", quantityKg, { quantityKg = it }, Icons.Rounded.Scale, true) }
            item { ParameterInputField("Base APMC Price (optional)", historicalApmcPrice, { historicalApmcPrice = it }, Icons.Rounded.CurrencyRupee, true) }
            
            item {
                Text("Quality Grade", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Premium", "Good", "Average").forEach { q ->
                        FilterChip(
                            selected = quality == q,
                            onClick = { quality = q },
                            label = { Text(q) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = {
                        viewModel.predictPrice(
                            cropName = cropName.ifEmpty { "Tomato" },
                            district = district.ifEmpty { "Bangalore Urban" },
                            quality = quality,
                            quantityKg = quantityKg.toDoubleOrNull() ?: 100.0,
                            harvestDate = System.currentTimeMillis(),
                            historicalApmcPrice = historicalApmcPrice.toDoubleOrNull() ?: 0.0,
                            weatherCondition = weatherCondition
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Predict Price", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                }
            }

            item {
                when (val state = uiState) {
                    is PricePredictionState.Loading -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(0.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Analyzing market data...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    is PricePredictionState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        PredictionResultCard(state.result)
                    }
                    is PricePredictionState.Error -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ParameterInputField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, isNumber: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun PredictionResultCard(result: PricePredictionResult) {
    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            Column(modifier = Modifier.background(Color(0xFFF3E5F5)).padding(20.dp).fillMaxWidth()) {
                Text("AI Suggested Price", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF6A1B9A)))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "₹${"%.2f".format(result.suggestedPricePerKg)} / kg",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A148C))
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(Color.White, shape = RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Rounded.Psychology, contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${(result.confidenceScore * 100).toInt()}% Match", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4A148C), fontWeight = FontWeight.Bold))
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text("Analysis", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(text = result.explanation, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
            }
        }
    }
}
