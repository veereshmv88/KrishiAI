package com.krishiai.app.ui.screens.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavController,
    productId: String
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("Crop Details", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { /* Favorite */ }) {
                        Icon(Icons.Rounded.Favorite, contentDescription = "Favorite", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    actionIconContentColor = Color.DarkGray,
                    navigationIconContentColor = Color.DarkGray
                )
            )
        },
        bottomBar = {
            AppBottomNavigation(navController = navController, activeRoute = Screen.BuyerDashboard.route)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFFF5F5F5))
                ) {
                    // Placeholder for actual image
                    Icon(
                        Icons.Rounded.Spa,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(80.dp),
                        tint = BrandGreen.copy(alpha = 0.3f)
                    )
                    
                    // Image Counter Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("1/5", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Seller Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFE0E0E0))) // Avatar
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ramesh H.", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Farmer", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("4.8", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Text(" (32 reviews)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Title & Price Section
                    Text("Tomato", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A Grade • 100 kg", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BrandGreen),
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹22.5", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                            Text(" /kg", style = MaterialTheme.typography.titleMedium.copy(color = BrandGreen), modifier = Modifier.padding(bottom = 2.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("2.1 km away", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailColumn("Harvest Date", "25 May 2025")
                            HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                            DetailColumn("Quality", "A Grade")
                            HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp), color = Color(0xFFE0E0E0))
                            DetailColumn("Location", "Bagalkot, KA")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text("Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Fresh and high quality tomatoes directly from farm. Grown with organic methods.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray, lineHeight = 22.sp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // AI Price Prediction
                    Text("AI Price Prediction", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Recommended Price", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("₹22.5 /kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                            }
                            Column {
                                Text("Confidence", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("92%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                            }
                            Text("High Demand", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE53935)))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Fixed Action Buttons at the bottom, just above the nav bar
            Box(modifier = Modifier.background(Color.White)) {
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Chat */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { /* Contact */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contact", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray))
    }
}
