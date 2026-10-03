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
fun BuyerWishlistScreen(
    navController: NavController,
    viewModel: BuyerMainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // In a real app we'd fetch this from ViewModel wishlist IDs.
    // For now we mock it with a subset of products.
    val wishlistProducts = uiState.allProducts.take(3) 

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BuyerPrimaryGreen)
                    .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("My Wishlist", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Surface(color = BuyerDanger, shape = RoundedCornerShape(12.dp)) {
                        Text("${wishlistProducts.size} Items", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (wishlistProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.FavoriteBorder, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Your wishlist is empty", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wishlistProducts) { product ->
                        WishlistProductCard(
                            product = product,
                            isPriceDrop = product.pricePerKg < 50, // mock condition
                            isNearby = product.distanceKm < 5.0, // mock condition
                            onClick = { navController.navigate("buyer_product/${product.productId}") },
                            onRemove = { /* TODO */ },
                            onBuyNow = { /* TODO */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistProductCard(
    product: BuyerProduct,
    isPriceDrop: Boolean,
    isNearby: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onBuyNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(modifier = Modifier.padding(12.dp)) {
                // Image
                Box(
                    modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(model = product.imageUrls.firstOrNull() ?: product.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    if (isPriceDrop) {
                        Surface(
                            color = BuyerDanger,
                            shape = RoundedCornerShape(bottomEnd = 12.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text("Price Drop", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(product.cropName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.Close, null, tint = Color.Gray)
                        }
                    }
                    Text(product.farmerName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = if (isNearby) BuyerAccentGreen else Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("${String.format("%.1f", product.distanceKm)} km away", style = MaterialTheme.typography.labelSmall, color = if (isNearby) BuyerPrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isNearby) FontWeight.Bold else FontWeight.Normal)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text("₹${product.pricePerKg}/kg", color = BuyerPrimaryGreen, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            // Actions
            Row(modifier = Modifier.fillMaxWidth().background(BuyerLightBackground).padding(12.dp)) {
                OutlinedButton(
                    onClick = { /* Add to Cart */ },
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BuyerPrimaryGreen)
                ) {
                    Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Cart")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onBuyNow,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BuyerPrimaryGreen)
                ) {
                    Text("Buy Now")
                }
            }
        }
    }
}
