package com.krishiai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPreferencesScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val syncEnabled by viewModel.offlineSyncEnabled.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("AI Preferences") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }}) }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(24.dp)) {
            PremiumCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Offline AI Inference", fontWeight = FontWeight.Bold)
                        Text("Download TFLite models for disease detection and yield estimation over Wi-Fi for offline use.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = syncEnabled, onCheckedChange = { viewModel.setOfflineSync(it) })
                }
            }
        }
    }
}
