package com.krishiai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.ui.navigation.Screen

import androidx.compose.ui.res.stringResource
import com.krishiai.app.R

@Composable
fun AppBottomNavigation(navController: NavController, activeRoute: String) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem(stringResource(R.string.nav_home), Screen.FarmerDashboard.route, Icons.Rounded.Home),
            BottomNavItem(stringResource(R.string.nav_marketplace), Screen.BuyerDashboard.route, Icons.Rounded.Storefront),
            BottomNavItem(stringResource(R.string.nav_sell), Screen.MyProducts.route, Icons.Rounded.Eco),
            BottomNavItem(stringResource(R.string.nav_ai_tools), Screen.AITools.route, Icons.Rounded.AutoAwesome),
            BottomNavItem("Profile", Screen.Profile.route, Icons.Rounded.Person)
        )

        items.forEach { item ->
            val isSelected = activeRoute == item.route
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    ) 
                },
                selected = isSelected,
                onClick = {
                    if (activeRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(Screen.FarmerDashboard.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
