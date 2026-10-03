package com.krishiai.app.buyer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.krishiai.app.buyer.data.model.BuyerProduct
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerExploreScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Spices", "Organic")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Premium Search Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BuyerPrimaryGreen)
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Text("Explore Marketplace", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { q ->
                            query = q
                            if (q.length >= 2) viewModel.search(q)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        placeholder = { Text("Search AI, Images, Voice...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = BuyerPrimaryGreen) },
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { /* OCR */ }) { Icon(Icons.Rounded.CameraAlt, null, tint = BuyerPrimaryGreen) }
                                IconButton(onClick = { /* Voice */ }) { Icon(Icons.Rounded.Mic, null, tint = BuyerPrimaryGreen) }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            // Mocking the filter behavior
                        },
                        label = { Text(cat, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BuyerPrimaryGreen,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Results List
            val products = if (query.isEmpty()) uiState.allProducts else uiState.searchResults
            if (products.isEmpty() && query.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results for \"$query\"", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        PremiumProductCard(
                            product = product,
                            onClick = { navController.navigate("buyer_product/${product.productId}") },
                            onWishlist = { /* TODO */ },
                            onQuickBuy = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumProductCard(
    product: BuyerProduct,
    onClick: () -> Unit,
    onWishlist: () -> Unit,
    onQuickBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Image
            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
            ) {
                AsyncImage(model = product.imageUrls.firstOrNull() ?: product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Surface(
                    color = BuyerPremiumGold,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text("4.5 ★", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(product.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(product.farmerName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = BuyerAccentGreen, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("${String.format("%.1f", product.distanceKm)} km away", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("₹${product.pricePerKg}", color = BuyerPrimaryGreen, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("/kg", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 2.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("₹${product.pricePerKg + 20}", color = Color.LightGray, style = MaterialTheme.typography.labelSmall, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
            
            // Actions
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height(100.dp)) {
                IconButton(onClick = onWishlist, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.FavoriteBorder, null, tint = BuyerDanger)
                }
                IconButton(onClick = onQuickBuy, modifier = Modifier.size(40.dp).clip(CircleShape).background(BuyerPrimaryGreen)) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White)
                }
            }
        }
    }
}
