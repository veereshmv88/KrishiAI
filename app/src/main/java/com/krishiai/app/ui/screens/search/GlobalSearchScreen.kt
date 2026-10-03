package com.krishiai.app.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    val recentSearches = listOf("Tomatoes", "Fertilizers", "Weather Forecast", "Pesticides")
    val suggestions = listOf("Market prices in your area", "AI Disease Detection", "Best time to sell")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search crops, tools, prices...") },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                }
            )
        }
    ) { p ->
        LazyColumn(modifier = Modifier.padding(p).padding(16.dp)) {
            if (query.isEmpty()) {
                item {
                    Text("Recent Searches", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                }
                items(recentSearches) { search ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { query = search }.padding(vertical = 12.dp)) {
                        Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(16.dp))
                        Text(search, style = MaterialTheme.typography.bodyLarge)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
                
                item { Spacer(Modifier.height(24.dp)) }
                
                item {
                    Text("Suggestions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(8.dp))
                }
                items(suggestions) { suggestion ->
                    PremiumCard(modifier = Modifier.padding(vertical = 4.dp).clickable { query = suggestion }) {
                        Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                item {
                    PremiumCard {
                        Text("Search Results for \"$query\"", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Searching real-time data is currently disabled in offline mode. Please connect to the internet or try a cached item.")
                    }
                }
            }
        }
    }
}
