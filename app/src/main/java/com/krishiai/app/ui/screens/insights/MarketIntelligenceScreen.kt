package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketIntelligenceScreen(
    navController: NavController,
    viewModel: MarketIntelligenceViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Intelligence", fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFE8F5E9)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2E7D32)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.DataUsage, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Live Trends", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)))
                        Text("APMC market activity across Karnataka", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1B5E20).copy(alpha = 0.8f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SectionHeader("Top Movers Today", "Crops with highest price changes") }

            item {
                MarketMoverCard("Tomato", "₹32/kg", "+14.5%", true)
            }
            item {
                MarketMoverCard("Onion", "₹45/kg", "+8.2%", true)
            }
            item {
                MarketMoverCard("Potato", "₹18/kg", "-5.0%", false)
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            
            item { SectionHeader("Regional Demand Heatmap", "High demand zones") }

            item {
                PremiumCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DemandRow("Bengaluru Urban", "Very High", Color(0xFFD32F2F))
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        DemandRow("Mysuru", "High", Color(0xFFF57C00))
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        DemandRow("Hubballi", "Moderate", Color(0xFFFBC02D))
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        DemandRow("Belagavi", "Stable", Color(0xFF388E3C))
                    }
                }
            }
        }
    }
}

@Composable
fun MarketMoverCard(crop: String, price: String, change: String, isUp: Boolean) {
    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Grass, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(crop, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(price, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = if (isUp) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = change,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = if (isUp) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

@Composable
fun DemandRow(district: String, level: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(district, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
            Text(level, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold))
        }
    }
}
