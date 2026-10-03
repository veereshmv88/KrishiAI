package com.krishiai.app.ui.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.krishiai.app.ui.theme.BrandGreen

import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// Crops will now be loaded dynamically from CommoditySearchEngine
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadCropScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    farmerViewModel: FarmerViewModel
) {
    var currentStep by remember { mutableStateOf(1) }
    
    // Form State
    var selectedCropNode by remember { mutableStateOf<com.krishiai.app.core.commodity.CommodityNode?>(null) }
    var selectedCrop by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("25/05/2025") }
    var price by remember { mutableStateOf("") }
    
    var districtNode by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(null) }
    var talukNode by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(null) }
    
    // Connect Location
    val locationResult by farmerViewModel.locationResult.collectAsState()
    val locationText = if (districtNode != null && talukNode != null) {
        "${districtNode!!.name}, ${talukNode!!.name}"
    } else if (locationResult != null) {
        "${locationResult!!.district}, ${locationResult!!.taluk}"
    } else "Fetching Location..."
    
    LaunchedEffect(Unit) {
        farmerViewModel.fetchCurrentLocation()
    }
    
    LaunchedEffect(locationResult) {
        if (locationResult != null && districtNode == null) {
            districtNode = com.krishiai.app.core.location.LocationNode("tmp", locationResult!!.district, com.krishiai.app.core.location.LocationType.DISTRICT)
            talukNode = com.krishiai.app.core.location.LocationNode("tmp2", locationResult!!.taluk, com.krishiai.app.core.location.LocationType.TALUK)
        }
    }
    
    // Connect AI Price
    val aiPrice by farmerViewModel.calculatedAiPrice.collectAsState()
    LaunchedEffect(selectedCrop, quantity) {
        if (selectedCrop.isNotEmpty() && quantity.isNotEmpty()) {
            farmerViewModel.calculateRecommendedPrice(
                cropName = selectedCrop,
                district = locationResult?.district ?: "Bagalkot",
                quality = "A",
                quantityKg = quantity.toDoubleOrNull() ?: 100.0,
                harvestDate = System.currentTimeMillis()
            )
        }
    }
    
    LaunchedEffect(aiPrice) {
        if (aiPrice != null && price.isEmpty()) {
            price = aiPrice!!.finalPrice.toString()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (currentStep == 1) "Sell Crop - Details" 
                                   else if (currentStep == 2) "Sell Crop - Images" 
                                   else "Sell Crop - Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentStep > 1) currentStep-- else navController.popBackStack() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            Column {
                Box(modifier = Modifier.background(Color.White).padding(16.dp)) {
                    val context = LocalContext.current
                    val currentUser by authViewModel.currentUser.collectAsState()
                    Button(
                        onClick = { 
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null || currentUser == null) {
                                    Toast.makeText(context, "Please sign in to continue.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                
                                // Add to Sell logic
                                farmerViewModel.uploadCropListing(
                                    farmerId = user.uid,
                                    farmerName = currentUser?.fullName ?: "Unknown Farmer",
                                    farmerPhone = currentUser?.mobileNumber ?: "",
                                    cropCategory = selectedCropNode?.category ?: "Unknown",
                                    cropName = selectedCrop,
                                    quantity = quantity,
                                    quality = "A Grade",
                                    harvestDate = System.currentTimeMillis(),
                                    description = "Freshly harvested $selectedCrop",
                                    district = locationResult?.district ?: "Unknown",
                                    taluk = locationResult?.taluk ?: "Unknown",
                                    imageBytes = null,
                                    customPrice = price.toDoubleOrNull() ?: 0.0
                                )
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                    ) {
                        Text(if (currentStep == 3) "Add to Sell" else "Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepItem(number = "1", label = "Crop Details", isActive = currentStep >= 1)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep >= 2) BrandGreen else Color(0xFFE0E0E0), thickness = 2.dp)
                StepItem(number = "2", label = "Images", isActive = currentStep >= 2)
                HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (currentStep >= 3) BrandGreen else Color(0xFFE0E0E0), thickness = 2.dp)
                StepItem(number = "3", label = "Preview", isActive = currentStep == 3)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (currentStep == 1) {
                // Form Fields
                Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    // Smart Commodity Picker
                    com.krishiai.app.ui.components.SmartCommodityPicker(
                        label = "Crop Name",
                        selectedCommodity = selectedCropNode,
                        onCommoditySelected = { 
                            selectedCropNode = it
                            selectedCrop = it.name
                            price = "" // reset price to trigger recalc
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        trailingIcon = { Text("kg", modifier = Modifier.padding(end = 16.dp), color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        )
                    )
                    
                    // Quality Grade removed.
                    
                    OutlinedTextField(
                        value = harvestDate,
                        onValueChange = { harvestDate = it },
                        label = { Text("Harvest Date") },
                        trailingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        )
                    )
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹/kg)") },
                        trailingIcon = { 
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(BrandGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .border(1.dp, BrandGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (aiPrice != null) "AI Suggested" else "Calculating...", color = BrandGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen
                        )
                    )
                    
                    com.krishiai.app.ui.components.SmartLocationPicker(
                        label = "District",
                        selectedLocation = districtNode,
                        onLocationSelected = { 
                            districtNode = it
                            talukNode = null 
                        },
                        filterType = com.krishiai.app.core.location.LocationType.DISTRICT,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    com.krishiai.app.ui.components.SmartLocationPicker(
                        label = "Taluk",
                        selectedLocation = talukNode,
                        onLocationSelected = { talukNode = it },
                        filterType = com.krishiai.app.core.location.LocationType.TALUK,
                        parentId = districtNode?.id,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (currentStep == 2) {
                // Step 2: Images
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                            .clickable { /* Select Image */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AddAPhoto, contentDescription = "Add Photo", modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to upload crop photos", color = Color.Gray)
                        }
                    }
                }
            } else if (currentStep == 3) {
                // Step 3: Preview
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Preview Your Listing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            PreviewRow("Crop Name", selectedCrop)
                            PreviewRow("Quantity", "$quantity kg")
                            PreviewRow("Price", "₹$price / kg")
                            PreviewRow("Harvest Date", harvestDate)
                            PreviewRow("Location", locationText)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StepItem(number: String, label: String, isActive: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isActive) BrandGreen else Color.White)
                .border(1.dp, if (isActive) BrandGreen else Color.Gray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = if (isActive) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) BrandGreen else Color.Gray,
                fontSize = 11.sp
            )
        )
    }
}
