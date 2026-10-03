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
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var name by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var district by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(
        if (!currentUser?.district.isNullOrEmpty()) 
            com.krishiai.app.core.location.LocationNode("tmp", currentUser!!.district, com.krishiai.app.core.location.LocationType.DISTRICT) 
        else null
    ) }
    var taluk by remember { mutableStateOf<com.krishiai.app.core.location.LocationNode?>(
        if (!currentUser?.taluk.isNullOrEmpty()) 
            com.krishiai.app.core.location.LocationNode("tmp2", currentUser!!.taluk, com.krishiai.app.core.location.LocationType.TALUK) 
        else null
    ) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            com.krishiai.app.ui.components.SmartLocationPicker(
                label = "District",
                selectedLocation = district,
                onLocationSelected = { 
                    district = it
                    // Reset taluk when district changes
                    taluk = null
                },
                filterType = com.krishiai.app.core.location.LocationType.DISTRICT,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            com.krishiai.app.ui.components.SmartLocationPicker(
                label = "Taluk",
                selectedLocation = taluk,
                onLocationSelected = { taluk = it },
                filterType = com.krishiai.app.core.location.LocationType.TALUK,
                parentId = district?.id,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { 
                    // Save logic here (requires extending AuthViewModel/Repository)
                    navController.navigateUp() 
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Save Changes")
            }
        }
    }
}
