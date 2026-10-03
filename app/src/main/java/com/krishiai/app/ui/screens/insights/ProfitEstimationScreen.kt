package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.krishiai.app.domain.ai.ProfitEstimationResult
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitEstimationScreen(
    navController: NavController,
    viewModel: ProfitEstimationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var expectedRevenue by remember { mutableStateOf("") }
    var transportationCost by remember { mutableStateOf("") }
    var labourCost by remember { mutableStateOf("") }
    var fertilizerCost by remember { mutableStateOf("") }
    var miscellaneousCost by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit Estimator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E88E5)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Analytics, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Financial AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)))
                        Text("Calculate expected ROI & margins", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0).copy(alpha = 0.8f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SectionHeader("Input Costs", "Enter expected expenses and revenue") }
            item { CostInputField(label = "Expected Revenue (₹)", value = expectedRevenue, onValueChange = { expectedRevenue = it }, icon = Icons.Rounded.AccountBalanceWallet) }
            item { CostInputField(label = "Transportation Cost (₹)", value = transportationCost, onValueChange = { transportationCost = it }, icon = Icons.Rounded.LocalShipping) }
            item { CostInputField(label = "Labour Cost (₹)", value = labourCost, onValueChange = { labourCost = it }, icon = Icons.Rounded.Engineering) }
            item { CostInputField(label = "Fertilizer & Seeds Cost (₹)", value = fertilizerCost, onValueChange = { fertilizerCost = it }, icon = Icons.Rounded.Grass) }
            item { CostInputField(label = "Other Expenses (₹)", value = miscellaneousCost, onValueChange = { miscellaneousCost = it }, icon = Icons.Rounded.MoreHoriz) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = {
                        viewModel.estimateProfit(
                            expectedRevenue = expectedRevenue.toDoubleOrNull() ?: 0.0,
                            transportationCost = transportationCost.toDoubleOrNull() ?: 0.0,
                            labourCost = labourCost.toDoubleOrNull() ?: 0.0,
                            fertilizerCost = fertilizerCost.toDoubleOrNull() ?: 0.0,
                            miscellaneousCost = miscellaneousCost.toDoubleOrNull() ?: 0.0
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Calculate Profit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.Calculate, contentDescription = null)
                }
            }

            item {
                when (val state = uiState) {
                    is ProfitEstimationState.Loading -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(0.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Crunching numbers...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    is ProfitEstimationState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfitEstimationResultCard(state.result)
                    }
                    is ProfitEstimationState.Error -> {
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
fun CostInputField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
fun ProfitEstimationResultCard(result: ProfitEstimationResult) {
    val isProfitable = result.expectedProfit > 0
    val containerColor = if (isProfitable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (isProfitable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            Column(modifier = Modifier.background(containerColor).padding(20.dp).fillMaxWidth()) {
                Text("Expected Profit", style = MaterialTheme.typography.titleMedium.copy(color = contentColor.copy(alpha = 0.8f)))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "₹${"%.2f".format(result.expectedProfit)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = contentColor)
                    )
                    if (isProfitable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${"%.1f".format(result.profitMarginPercentage)}% Margin", style = MaterialTheme.typography.labelSmall.copy(color = contentColor, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text("Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                
                BreakdownRow("Total Investment", "₹${"%.2f".format(result.totalInvestment)}", true)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfitable) Icons.Rounded.Lightbulb else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (isProfitable) Color(0xFFFBC02D) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = result.recommendation, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}
