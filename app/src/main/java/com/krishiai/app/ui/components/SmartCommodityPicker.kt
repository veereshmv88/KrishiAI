package com.krishiai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.krishiai.app.core.commodity.CommodityNode
import com.krishiai.app.core.commodity.CommoditySearchEngine
import com.krishiai.app.core.commodity.CommoditySearchResult
import kotlinx.coroutines.launch

@Composable
fun SmartCommodityPicker(
    label: String = "Select Crop",
    selectedCommodity: CommodityNode? = null,
    onCommoditySelected: (CommodityNode) -> Unit,
    modifier: Modifier = Modifier,
    commodityViewModel: CommodityViewModel = hiltViewModel()
) {
    SmartCommodityPicker(
        label = label,
        selectedCommodity = selectedCommodity,
        onCommoditySelected = onCommoditySelected,
        searchEngine = commodityViewModel.searchEngine,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCommodityPicker(
    label: String = "Select Crop",
    selectedCommodity: CommodityNode?,
    onCommoditySelected: (CommodityNode) -> Unit,
    searchEngine: CommoditySearchEngine,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(selectedCommodity?.name ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<CommoditySearchResult>>(emptyList()) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        searchEngine.initialize()
    }

    LaunchedEffect(query) {
        if (query.isNotBlank() && isDropdownExpanded) {
            searchResults = searchEngine.searchCommodities(query)
        } else {
            searchResults = emptyList()
        }
    }

    // Update query if selectedCommodity changes externally
    LaunchedEffect(selectedCommodity) {
        if (selectedCommodity != null && query != selectedCommodity.name) {
            query = selectedCommodity.name
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                isDropdownExpanded = true
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        isDropdownExpanded = true
                        if (query.isEmpty()) {
                            coroutineScope.launch {
                                searchResults = searchEngine.searchCommodities("")
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { 
                        query = ""
                        isDropdownExpanded = true
                    }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        AnimatedVisibility(
            visible = isDropdownExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .heightIn(max = 300.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (searchResults.isNotEmpty()) {
                        if (query.isNotEmpty()) {
                            item {
                                Text("Suggestions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                            }
                        }
                        items(searchResults) { result ->
                            CommoditySuggestionItem(result) {
                                query = result.node.name
                                isDropdownExpanded = false
                                onCommoditySelected(result.node)
                            }
                        }
                    } else if (query.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No crops found", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommoditySuggestionItem(result: CommoditySearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Eco,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(result.node.name, style = MaterialTheme.typography.bodyLarge)
            Text(result.node.category, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
