package com.krishiai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Help & FAQ") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }}) }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(24.dp)) {
            PremiumCard {
                Column {
                    Text("Frequently Asked Questions", fontWeight = FontWeight.Bold)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Q: How do I sell my crop?\nA: Navigate to 'Sell' in the bottom navigation and fill out the form.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Q: How does the AI price estimation work?\nA: It uses real-time APMC mandi data and historical trends to provide a fair market price.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
