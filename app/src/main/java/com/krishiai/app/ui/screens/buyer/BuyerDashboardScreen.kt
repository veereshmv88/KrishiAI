package com.krishiai.app.ui.screens.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.*
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
import com.krishiai.app.data.model.Product
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    buyerViewModel: BuyerViewModel
) {
    val selectedCategory by buyerViewModel.selectedCategory.collectAsState()
    val filteredProducts = buyerViewModel.getFilteredProducts()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Marketplace", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BrandGreen))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(com.krishiai.app.ui.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = BrandGreen,
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
                .background(Color.White)
        ) {
            // Search Bar
            val searchQuery by buyerViewModel.searchQuery.collectAsState()
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { buyerViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search crops, farmers...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // Categories
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val categories = listOf("All", "Vegetables", "Fruits", "Grains")
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) BrandGreen else Color.White)
                            .border(1.dp, if (isSelected) BrandGreen else Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                            .clickable { buyerViewModel.selectCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.DarkGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Location
            val location by buyerViewModel.selectedDistrict.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (location.isEmpty()) "Detecting Location..." else "$location, India", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
                Text("Change", style = MaterialTheme.typography.bodySmall.copy(color = BrandGreen, fontWeight = FontWeight.Bold), modifier = Modifier.clickable { 
                    buyerViewModel.loadDashboardData("Bengaluru") 
                })
            }
            
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found in this category.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { product ->
                        MarketplaceProductItem(
                            productName = product.cropName,
                            grade = product.quality,
                            weight = product.quantity,
                            price = product.finalPrice.toString(),
                            sellerName = product.farmerName,
                            distance = "${product.district}, ${product.taluk}",
                            isVerified = true,
                            onClick = { navController.navigate(Screen.ProductDetails.createRoute(product.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceProductItem(
    productName: String,
    grade: String,
    weight: String,
    price: String,
    sellerName: String,
    distance: String,
    isVerified: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                // Crop image fallback icon
                Icon(Icons.Rounded.Spa, contentDescription = null, tint = BrandGreen.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center).size(40.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFFE0E0E0))) // Seller avatar
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(sellerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Rounded.Verified, contentDescription = "Verified", tint = BrandGreen, modifier = Modifier.size(14.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(productName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                
                Text("$grade • $weight", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("₹$price", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = BrandGreen))
                        Text(" /kg", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp), modifier = Modifier.padding(bottom = 2.dp))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(distance, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Rounded.FavoriteBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
