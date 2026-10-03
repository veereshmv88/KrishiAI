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
fun FeedbackScreen(navController: NavController) {
    var text by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Feedback") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }}) }
    ) { p ->
        Column(modifier = Modifier.padding(p).padding(24.dp)) {
            if (submitted) {
                PremiumCard { Text("Thank you for your feedback! We will review it shortly.") }
            } else {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Describe your issue or suggestion") }, modifier = Modifier.fillMaxWidth().height(150.dp))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { submitted = true }, modifier = Modifier.fillMaxWidth()) { Text("Submit Feedback") }
            }
        }
    }
}
