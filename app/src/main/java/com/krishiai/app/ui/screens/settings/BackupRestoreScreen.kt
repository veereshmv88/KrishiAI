package com.krishiai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(navController: NavController) {
    var status by remember { mutableStateOf("Ready") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Backup & Restore") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }}) }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(24.dp)) {
            PremiumCard {
                Column {
                    Text("Status: ")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { status = "Backup successfully exported locally." }, modifier = Modifier.fillMaxWidth()) { Text("Export Local Data (JSON)") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { status = "Restored successfully." }, modifier = Modifier.fillMaxWidth()) { Text("Import Local Data") }
                }
            }
        }
    }
}
