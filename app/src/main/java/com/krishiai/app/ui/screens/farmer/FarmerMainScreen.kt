package com.krishiai.app.ui.screens.farmer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.krishiai.app.R
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.auth.AuthViewModel

sealed class FarmerBottomNavItem(
    val route: String,
    @androidx.annotation.StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Dashboard : FarmerBottomNavItem(Screen.Home.route, R.string.nav_home, Icons.Rounded.Dashboard)
    object Marketplace : FarmerBottomNavItem(Screen.Marketplace.route, R.string.nav_marketplace, Icons.Rounded.Storefront)
    object Sell : FarmerBottomNavItem(Screen.SellCrop.route, R.string.nav_sell, Icons.Rounded.AddCircle)
    object AITools : FarmerBottomNavItem(Screen.AITools.route, R.string.nav_ai_tools, Icons.Rounded.AutoAwesome)
    object Profile : FarmerBottomNavItem(Screen.Profile.route, R.string.nav_profile, Icons.Rounded.Person)
}

@Composable
fun FarmerMainScreen(
    rootNavController: NavController,
    authViewModel: AuthViewModel,
    farmerViewModel: FarmerViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOf(
                    FarmerBottomNavItem.Dashboard,
                    FarmerBottomNavItem.Marketplace,
                    FarmerBottomNavItem.Sell,
                    FarmerBottomNavItem.AITools,
                    FarmerBottomNavItem.Profile
                ).forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
                        label = {
                            Text(
                                text = stringResource(screen.titleRes),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                FarmerHomeScreen(
                    navController = rootNavController, // For root navigation if needed
                    authViewModel = authViewModel,
                    farmerViewModel = farmerViewModel
                )
            }
            composable(Screen.Marketplace.route) {
                FarmerMarketScreen(
                    navController = bottomNavController,
                    farmerViewModel = farmerViewModel
                )
            }
            composable(Screen.SellCrop.route) {
                UploadCropScreen(
                    navController = bottomNavController,
                    authViewModel = authViewModel,
                    farmerViewModel = farmerViewModel
                )
            }
            composable(Screen.AITools.route) {
                com.krishiai.app.ui.screens.insights.InsightsDashboardScreen(navController = rootNavController) // TEMPORARY
            }
            composable(Screen.Profile.route) {
                com.krishiai.app.ui.screens.profile.ProfileScreen(
                    navController = rootNavController,
                    authViewModel = authViewModel
                )
            }
            composable(Screen.MyProducts.route) {
                MyProductsScreen(
                    navController = bottomNavController,
                    farmerViewModel = farmerViewModel
                )
            }
        }
    }
}
