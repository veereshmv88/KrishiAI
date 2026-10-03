package com.krishiai.app.buyer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.*
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerAnalyticsScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val orders = uiState.orders

    val completed = orders.filter { it.status == com.krishiai.app.buyer.data.model.OrderStatus.DELIVERED }
    val totalSpending = completed.sumOf { it.totalAmount }
    val totalSavings = totalSpending * 0.15 // mock 15% savings

    val monthlySpendingTrend = List(6) { if (completed.isEmpty()) 0.0 else totalSpending / 6 + (Math.random() * 500) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Buyer Analytics", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { p ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(p).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalyticsStatCard(
                        title = "Total Spent",
                        value = "₹${totalSpending.toInt()}",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        color = BuyerPrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsStatCard(
                        title = "Total Savings",
                        value = "₹${totalSavings.toInt()}",
                        icon = Icons.Rounded.Savings,
                        color = BuyerPremiumGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                BuyerPremiumCard {
                    Text("Spending Trend (6 Months)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    LineChart(data = monthlySpendingTrend, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }

            item {
                BuyerPremiumCard {
                    Text("Top Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    CategoryBar(name = "Vegetables", percentage = 0.6f, color = BuyerPrimaryGreen)
                    Spacer(Modifier.height(8.dp))
                    CategoryBar(name = "Fruits", percentage = 0.3f, color = BuyerPremiumGold)
                    Spacer(Modifier.height(8.dp))
                    CategoryBar(name = "Grains", percentage = 0.1f, color = BuyerBlue)
                }
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun LineChart(data: List<Double>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxVal = data.maxOrNull()?.toFloat() ?: 1f
    val minVal = data.minOrNull()?.toFloat() ?: 0f
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)
        
        val path = Path()
        data.forEachIndexed { index, value ->
            val normalizedY = if (maxVal == minVal) height / 2 else height - ((value.toFloat() - minVal) / (maxVal - minVal) * height)
            val x = index * stepX
            if (index == 0) {
                path.moveTo(x, normalizedY)
            } else {
                path.lineTo(x, normalizedY)
            }
        }
        
        drawPath(
            path = path,
            color = BuyerPrimaryGreen,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        
        data.forEachIndexed { index, value ->
            val normalizedY = if (maxVal == minVal) height / 2 else height - ((value.toFloat() - minVal) / (maxVal - minVal) * height)
            val x = index * stepX
            drawCircle(
                color = BuyerPremiumGold,
                radius = 6.dp.toPx(),
                center = Offset(x, normalizedY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(x, normalizedY)
            )
        }
    }
}

@Composable
fun CategoryBar(name: String, percentage: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(name, modifier = Modifier.weight(0.3f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.weight(0.6f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(percentage).background(color)
            )
        }
        Text("${(percentage * 100).toInt()}%", modifier = Modifier.weight(0.1f).padding(start = 8.dp), style = MaterialTheme.typography.labelSmall)
    }
}
