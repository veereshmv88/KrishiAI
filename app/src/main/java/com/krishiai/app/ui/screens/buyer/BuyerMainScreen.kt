package com.krishiai.app.ui.screens.buyer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
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
import com.krishiai.app.buyer.ui.BuyerMainViewModel
import com.krishiai.app.buyer.ui.screens.BuyerCartScreen
import com.krishiai.app.buyer.ui.screens.BuyerCheckoutScreen
import com.krishiai.app.buyer.ui.screens.BuyerExploreScreen
import com.krishiai.app.buyer.ui.screens.BuyerHomeDashboardScreen
import com.krishiai.app.buyer.ui.screens.BuyerOrderHistoryScreen
import com.krishiai.app.buyer.ui.screens.BuyerProductDetailScreen
import com.krishiai.app.buyer.ui.screens.BuyerProfileScreen
import com.krishiai.app.buyer.ui.screens.BuyerWishlistScreen
import com.krishiai.app.buyer.ui.screens.NearbySellerScreen
import com.krishiai.app.buyer.ui.screens.BuyerPricePredictionScreen
import com.krishiai.app.buyer.ui.screens.AIRecommendationsScreen
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.auth.AuthViewModel

sealed class BuyerBottomNavItem(
    val route: String,
    @androidx.annotation.StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Home : BuyerBottomNavItem(Screen.BuyerDashboard.route, R.string.nav_home, Icons.Rounded.Dashboard)
    object Explore : BuyerBottomNavItem(Screen.BuyerSearch.route, R.string.nav_explore, Icons.Rounded.Search)
    object Orders : BuyerBottomNavItem(Screen.BuyerOrders.route, R.string.nav_orders, Icons.AutoMirrored.Rounded.List)
    object Wishlist : BuyerBottomNavItem(Screen.BuyerWishlist.route, R.string.nav_wishlist, Icons.Rounded.ShoppingCart)
    object Profile : BuyerBottomNavItem(Screen.BuyerProfile.route, R.string.nav_profile, Icons.Rounded.Person)
}

@Composable
fun BuyerMainScreen(
    rootNavController: NavController,
    authViewModel: AuthViewModel,
    buyerViewModel: BuyerViewModel
) {
    val bottomNavController = rememberNavController()
    val buyerMainViewModel: BuyerMainViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                listOf(
                    BuyerBottomNavItem.Home,
                    BuyerBottomNavItem.Explore,
                    BuyerBottomNavItem.Orders,
                    BuyerBottomNavItem.Wishlist,
                    BuyerBottomNavItem.Profile
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
            startDestination = Screen.BuyerDashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.BuyerDashboard.route) {
                BuyerHomeDashboardScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerSearch.route) {
                BuyerExploreScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerCart.route) {
                BuyerCartScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerWishlist.route) {
                BuyerWishlistScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerOrders.route) {
                BuyerOrderHistoryScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerProfile.route) {
                BuyerProfileScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel,
                    authViewModel = authViewModel
                )
            }
            composable("buyer_product/{productId}") { backStack ->
                val productId = backStack.arguments?.getString("productId") ?: ""
                BuyerProductDetailScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel,
                    productId = productId
                )
            }
            composable(Screen.BuyerPricePrediction.route) {
                BuyerPricePredictionScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerNearbySellers.route) {
                NearbySellerScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerRecommendations.route) {
                AIRecommendationsScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable(Screen.BuyerCheckout.route) {
                BuyerCheckoutScreen(
                    navController = bottomNavController,
                    viewModel = buyerMainViewModel
                )
            }
            composable("buyer_ai_tools") {
                com.krishiai.app.buyer.ui.screens.BuyerAIToolsScreen(
                    navController = rootNavController, // Use root for VoiceAssistant navigation
                    viewModel = buyerMainViewModel
                )
            }
        }
    }
}
