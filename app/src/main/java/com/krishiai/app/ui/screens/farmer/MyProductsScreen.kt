package com.krishiai.app.ui.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    navController: NavController,
    farmerViewModel: FarmerViewModel
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("My Crops", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(com.krishiai.app.ui.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                        com.krishiai.app.ui.components.NotificationIcon(
                            onClick = { navController.navigate(com.krishiai.app.ui.navigation.Screen.NotificationCenter.route) }
                        )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = BrandGreen,
                    actionIconContentColor = Color.DarkGray,
                    navigationIconContentColor = Color.DarkGray
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.UploadCrop.route) },
                containerColor = BrandGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Crop", fontWeight = FontWeight.Bold)
            }
        },
    ) { paddingValues ->
        val myProducts by farmerViewModel.myProducts.collectAsState()
        var selectedTab by remember { mutableStateOf("All") }
        val tabs = listOf("All", "Active", "Sold", "Draft")

        val filteredProducts = when (selectedTab) {
            "Active" -> myProducts.filter { it.status == "AVAILABLE" }
            "Sold" -> myProducts.filter { it.status == "SOLD" }
            "Draft" -> myProducts.filter { it.status == "DRAFT" }
            else -> myProducts
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEach { tab ->
                    TabItem(label = tab, isSelected = selectedTab == tab, onClick = { selectedTab = tab })
                }
            }
            
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // List
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Inventory, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No crops found", style = MaterialTheme.typography.titleMedium.copy(color = Color.Gray))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap 'Add Crop' to list your first crop", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts.size) { idx ->
                        val p = filteredProducts[idx]
                        val dateText = if (p.status == "SOLD") "Sold" else "Listed"
                        MyCropItem(
                            name = p.cropName,
                            details = "${p.quantity} kg \u2022 ${p.quality}",
                            dateText = "$dateText on ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(p.harvestDate))}",
                            price = p.finalPrice.toString(),
                            status = when(p.status) {
                                "AVAILABLE" -> "Active"
                                "SOLD" -> "Sold"
                                "DRAFT" -> "Draft"
                                else -> p.status
                            },
                            syncStatus = p.syncStatus.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BrandGreen else Color.Gray
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (isSelected) {
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(BrandGreen, RoundedCornerShape(1.5.dp)))
        } else {
            Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color.Transparent))
        }
    }
}

@Composable
fun MyCropItem(name: String, details: String, dateText: String, price: String, status: String, syncStatus: String = "SYNCED") {
    val isActive = status == "Active"
    val isSyncing = syncStatus == "PENDING" || syncStatus == "UPLOADING"
    val isFailed = syncStatus == "FAILED"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Spa, contentDescription = null, tint = BrandGreen.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(2.dp))
                Text(details, style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                Spacer(modifier = Modifier.height(4.dp))
                Text(dateText, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp))
            }
            
            // Price & Status
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("₹$price", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Text("/kg", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp), modifier = Modifier.padding(bottom = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(if (isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        color = if (isActive) BrandGreen else Color(0xFFE53935),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isSyncing || isFailed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(if (isSyncing) Color(0xFFFFF3E0) else Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSyncing) "Syncing..." else "Sync Failed",
                            color = if (isSyncing) Color(0xFFE65100) else Color(0xFFD32F2F),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
