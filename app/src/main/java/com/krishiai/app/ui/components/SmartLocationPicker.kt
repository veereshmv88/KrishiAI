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
import com.krishiai.app.core.location.LocationNode
import com.krishiai.app.core.location.LocationSearchEngine
import com.krishiai.app.core.location.LocationSearchResult
import com.krishiai.app.core.location.LocationType
import com.krishiai.app.core.location.LocationHelper
import com.krishiai.app.data.local.dao.LocationDao
import com.krishiai.app.data.local.entity.LocationHistoryEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun SmartLocationPicker(
    label: String = "Select Location",
    selectedLocation: LocationNode? = null,
    onLocationSelected: (LocationNode) -> Unit,
    filterType: LocationType? = null,
    parentId: String? = null,
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    SmartLocationPicker(
        label = label,
        selectedLocation = selectedLocation,
        onLocationSelected = onLocationSelected,
        searchEngine = locationViewModel.searchEngine,
        locationDao = locationViewModel.locationDao,
        locationHelper = locationViewModel.locationHelper,
        filterType = filterType,
        parentId = parentId,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLocationPicker(
    label: String = "Select Location",
    selectedLocation: LocationNode?,
    onLocationSelected: (LocationNode) -> Unit,
    searchEngine: LocationSearchEngine,
    locationDao: LocationDao,
    locationHelper: LocationHelper? = null,
    filterType: LocationType? = null,
    parentId: String? = null,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf(selectedLocation?.name ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<LocationSearchResult>>(emptyList()) }
    var recentLocations by remember { mutableStateOf<List<LocationHistoryEntity>>(emptyList()) }
    var favoriteLocations by remember { mutableStateOf<List<LocationHistoryEntity>>(emptyList()) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        searchEngine.initialize()
        launch {
            locationDao.getRecentLocations().collectLatest { recentLocations = it }
        }
        launch {
            locationDao.getFavoriteLocations().collectLatest { favoriteLocations = it }
        }
    }

    LaunchedEffect(query, filterType, parentId) {
        if (query.isNotBlank() && isDropdownExpanded) {
            searchResults = searchEngine.searchLocations(query, filterType, parentId)
        } else {
            searchResults = emptyList()
        }
    }

    // Update query if selectedLocation changes externally
    LaunchedEffect(selectedLocation) {
        if (selectedLocation != null && query != selectedLocation.name) {
            query = selectedLocation.name
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
                                searchResults = searchEngine.searchLocations("", filterType, parentId)
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
                    .heightIn(max = 350.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                var isFetchingLocation by remember { mutableStateOf(false) }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (query.isEmpty() && locationHelper != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isFetchingLocation = true
                                        coroutineScope.launch {
                                            val locDetails = locationHelper.getCurrentLocation()
                                            if (locDetails != null) {
                                                val districtStr = locDetails.district
                                                val node = searchEngine.searchLocations(districtStr, filterType).firstOrNull()?.node
                                                if (node != null) {
                                                    query = node.name
                                                    isDropdownExpanded = false
                                                    onLocationSelected(node)
                                                }
                                            }
                                            isFetchingLocation = false
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isFetchingLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Rounded.MyLocation, contentDescription = "Use Current Location", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Use Current Location", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                            }
                            HorizontalDivider()
                        }
                    }

                    if (query.isEmpty() && favoriteLocations.isNotEmpty()) {
                        item {
                            Text("Favorites", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                        }
                        items(favoriteLocations) { fav ->
                            LocationHistoryItem(fav) {
                                coroutineScope.launch {
                                    val node = searchEngine.searchLocations(fav.name).firstOrNull()?.node
                                    if (node != null) {
                                        query = node.name
                                        isDropdownExpanded = false
                                        onLocationSelected(node)
                                    }
                                }
                            }
                        }
                    }

                    if (query.isEmpty() && recentLocations.isNotEmpty()) {
                        item {
                            Text("Recent Searches", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                        }
                        items(recentLocations) { recent ->
                            LocationHistoryItem(recent) {
                                coroutineScope.launch {
                                    val node = searchEngine.searchLocations(recent.name).firstOrNull()?.node
                                    if (node != null) {
                                        query = node.name
                                        isDropdownExpanded = false
                                        onLocationSelected(node)
                                    }
                                }
                            }
                        }
                    }

                    if (searchResults.isNotEmpty()) {
                        if (query.isNotEmpty()) {
                            item {
                                Text("Suggestions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                            }
                        }
                        items(searchResults) { result ->
                            LocationSuggestionItem(result) {
                                query = result.node.name
                                isDropdownExpanded = false
                                onLocationSelected(result.node)
                                
                                // Save to history
                                coroutineScope.launch {
                                    locationDao.insertOrUpdate(
                                        LocationHistoryEntity(
                                            id = result.node.id,
                                            name = result.node.name,
                                            type = result.node.type.name,
                                            parentId = result.node.parentId,
                                            breadcrumb = result.breadcrumb,
                                            isFavorite = favoriteLocations.any { it.id == result.node.id },
                                            lastUsedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        }
                    } else if (query.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No locations found", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationSuggestionItem(result: LocationSearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when(result.node.type) {
                LocationType.DISTRICT -> Icons.Rounded.Map
                LocationType.TALUK -> Icons.Rounded.LocationCity
                else -> Icons.Rounded.Place
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(result.node.name, style = MaterialTheme.typography.bodyLarge)
            Text(result.breadcrumb, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun LocationHistoryItem(history: LocationHistoryEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (history.isFavorite) Icons.Rounded.Star else Icons.Rounded.History,
            contentDescription = null,
            tint = if (history.isFavorite) Color(0xFFFFC107) else Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(history.name, style = MaterialTheme.typography.bodyLarge)
            Text(history.breadcrumb, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
