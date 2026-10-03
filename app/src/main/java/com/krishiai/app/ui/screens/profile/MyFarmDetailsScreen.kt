package com.krishiai.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.ui.screens.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFarmDetailsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var farmSize by remember { mutableStateOf("5 Acres") }
    var soilType by remember { mutableStateOf("Red Soil") }
    var primaryCrops by remember { mutableStateOf("Tomato, Onion, Maize") }
    var irrigationType by remember { mutableStateOf("Drip Irrigation") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Farm Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = farmSize,
                onValueChange = { farmSize = it },
                label = { Text("Farm Size") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = soilType,
                onValueChange = { soilType = it },
                label = { Text("Soil Type") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = primaryCrops,
                onValueChange = { primaryCrops = it },
                label = { Text("Primary Crops") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = irrigationType,
                onValueChange = { irrigationType = it },
                label = { Text("Irrigation Method") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { 
                    // Save logic
                    navController.navigateUp() 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Save Farm Details")
            }
        }
    }
}
