package com.krishiai.app.buyer.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.components.BuyerPremiumCard
import com.krishiai.app.ui.theme.BuyerPrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerAIToolsScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buyer AI Tools", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BuyerPrimaryGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                BuyerPremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("buyer_recommendations") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BuyerPrimaryGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.LocalOffer, null, tint = BuyerPrimaryGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Best Deal Finder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("AI-powered recommendations based on market prices.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                BuyerPremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("buyer_prediction") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BuyerPrimaryGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.QueryStats, null, tint = BuyerPrimaryGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Price Prediction", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Forecast future crop prices to plan your bulk purchases.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                BuyerPremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("voice_assistant") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(BuyerPrimaryGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Mic, null, tint = BuyerPrimaryGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Voice Assistant", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Ask questions in English or Kannada about market trends.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
