package com.krishiai.app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.screens.about.AboutScreen
import com.krishiai.app.ui.screens.auth.AuthViewModel
import com.krishiai.app.ui.screens.auth.ForgotPasswordScreen
import com.krishiai.app.ui.screens.auth.LoginScreen
import com.krishiai.app.ui.screens.auth.SignupScreen
import com.krishiai.app.ui.screens.buyer.BuyerDashboardScreen
import com.krishiai.app.ui.screens.buyer.BuyerViewModel
import com.krishiai.app.ui.screens.buyer.ProductDetailsScreen

import com.krishiai.app.ui.screens.farmer.FarmerViewModel
import com.krishiai.app.ui.screens.farmer.MyProductsScreen
import com.krishiai.app.ui.screens.farmer.UploadCropScreen
import com.krishiai.app.ui.screens.onboarding.OnboardingScreen
import com.krishiai.app.ui.screens.profile.ProfileScreen
import com.krishiai.app.ui.screens.settings.SettingsScreen
import com.krishiai.app.ui.screens.splash.SplashScreen
import com.krishiai.app.ui.screens.splash.SplashViewModel
import com.krishiai.app.ui.screens.welcome.WelcomeRoleSelectionScreen
import com.krishiai.app.ui.theme.KrishiAITheme
import com.krishiai.app.ui.screens.insights.DiseaseDetectionViewModel
import com.krishiai.app.ui.screens.insights.ProfitEstimationViewModel
import com.krishiai.app.ui.screens.insights.NegotiationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: com.krishiai.app.data.local.AppPreferences

    @Inject
    lateinit var languageManager: com.krishiai.app.core.language.LanguageManager

    @Inject
    lateinit var roleManager: com.krishiai.app.core.auth.RoleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentLanguage by languageManager.currentLanguage.collectAsState()
            
            // This forces recomposition when language changes
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val context = androidx.compose.ui.platform.LocalContext.current
            
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides context
            ) {
                KrishiAITheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        KrishiAppNavigation(
                            appPreferences = appPreferences,
                            roleManager = roleManager
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KrishiAppNavigation(
    appPreferences: com.krishiai.app.data.local.AppPreferences,
    roleManager: com.krishiai.app.core.auth.RoleManager
) {
    val navController = rememberNavController()
    
    // Instantiate shared AuthViewModel scoped to the navigation graph so login/signup share user info
    val authViewModel: AuthViewModel = hiltViewModel()
    val farmerViewModel: FarmerViewModel = hiltViewModel()
    val buyerViewModel: BuyerViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash routing
        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                onNavigateToAuth = { navController.navigate(Screen.Login.route) },
                onNavigateToFarmer = { navController.navigate(Screen.FarmerMain.route) },
                onNavigateToBuyer = { navController.navigate(Screen.BuyerMain.route) }
            )
        }

        // Onboarding slides
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController, appPreferences = appPreferences)
        }

        // Welcome Role selection
        composable(Screen.WelcomeRoleSelection.route) {
            WelcomeRoleSelectionScreen(navController = navController, authViewModel = authViewModel)
        }

        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }

        // Main entry point for logged-in users
        composable(Screen.FarmerMain.route) {
            com.krishiai.app.ui.screens.farmer.FarmerMainScreen(
                rootNavController = navController,
                authViewModel = authViewModel,
                farmerViewModel = farmerViewModel
            )
        }

        composable(Screen.BuyerMain.route) {
            com.krishiai.app.ui.screens.buyer.BuyerMainScreen(
                rootNavController = navController,
                authViewModel = authViewModel,
                buyerViewModel = buyerViewModel
            )
        }



        // Shared views
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.EditProfile.route) {
            com.krishiai.app.ui.screens.profile.EditProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.PersonalInfo.route) {
            com.krishiai.app.ui.screens.profile.PersonalInfoScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.MyFarmDetails.route) {
            com.krishiai.app.ui.screens.profile.MyFarmDetailsScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.NotificationsSettings.route) {
            com.krishiai.app.ui.screens.settings.NotificationsSettingsScreen(navController = navController)
        }
        composable(Screen.PrivacySettings.route) {
            com.krishiai.app.ui.screens.settings.PrivacySettingsScreen(navController = navController)
        }
        composable(Screen.AIPreferences.route) {
            com.krishiai.app.ui.screens.settings.AIPreferencesScreen(navController = navController)
        }
        composable(Screen.BackupRestore.route) {
            com.krishiai.app.ui.screens.settings.BackupRestoreScreen(navController = navController)
        }
        composable(Screen.HelpCenter.route) {
            com.krishiai.app.ui.screens.settings.HelpCenterScreen(navController = navController)
        }
        composable(Screen.Feedback.route) {
            com.krishiai.app.ui.screens.settings.FeedbackScreen(navController = navController)
        }
        composable(Screen.NotificationCenter.route) {
            com.krishiai.app.ui.screens.notifications.NotificationCenterScreen(navController = navController)
        }
        composable(Screen.GlobalSearch.route) {
            com.krishiai.app.ui.screens.search.GlobalSearchScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        
        composable(Screen.LiveMarketPrice.route) {
            com.krishiai.app.ui.screens.market.LiveMarketPriceScreen(navController = navController)
        }
        
        // AI Screens
        composable(Screen.AITools.route) {
            com.krishiai.app.ui.screens.insights.InsightsDashboardScreen(navController = navController)
        }
        composable(Screen.DiseaseDetection.route) {
            val diseaseDetectionViewModel: DiseaseDetectionViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.DiseaseDetectionScreen(
                navController = navController,
                viewModel = diseaseDetectionViewModel
            )
        }
        composable(Screen.CropScanner.route) {
            val cropScannerViewModel: com.krishiai.app.ui.screens.insights.CropScannerViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.CropScannerScreen(
                navController = navController,
                viewModel = cropScannerViewModel
            )
        }
        composable(Screen.OcrScanner.route) {
            val ocrScannerViewModel: com.krishiai.app.ui.screens.insights.OcrScannerViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.OcrScannerScreen(
                navController = navController,
                viewModel = ocrScannerViewModel
            )
        }
        composable(Screen.OcrHistory.route) {
            val ocrHistoryViewModel: com.krishiai.app.ui.screens.insights.OcrHistoryViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.OcrHistoryScreen(
                navController = navController,
                viewModel = ocrHistoryViewModel
            )
        }
        composable(
            route = Screen.CropHealthReport.route,
            arguments = listOf(
                navArgument("diseaseName") { type = NavType.StringType },
                navArgument("confidence") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val diseaseName = backStackEntry.arguments?.getString("diseaseName")?.replace("_", "/") ?: ""
            val confidence = backStackEntry.arguments?.getFloat("confidence") ?: 0f
            val reportViewModel: com.krishiai.app.ui.screens.insights.CropHealthReportViewModel = hiltViewModel()
            
            com.krishiai.app.ui.screens.insights.CropHealthReportScreen(
                navController = navController,
                diseaseName = diseaseName,
                confidenceScore = confidence,
                viewModel = reportViewModel
            )
        }
        composable(Screen.ProfitEstimation.route) {
            val profitEstimationViewModel: ProfitEstimationViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.ProfitEstimationScreen(
                navController = navController,
                viewModel = profitEstimationViewModel
            )
        }
        composable(Screen.NegotiationAssistant.route) {
            val negotiationViewModel: NegotiationViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.NegotiationAssistantScreen(
                navController = navController,
                viewModel = negotiationViewModel
            )
        }
        composable(Screen.PricePrediction.route) {
            val pricePredictionViewModel: com.krishiai.app.ui.screens.insights.PricePredictionViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.PricePredictionScreen(
                navController = navController,
                viewModel = pricePredictionViewModel
            )
        }
        composable(Screen.MarketIntelligence.route) {
            val marketViewModel: com.krishiai.app.ui.screens.insights.MarketIntelligenceViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.MarketIntelligenceScreen(
                navController = navController,
                viewModel = marketViewModel
            )
        }
        composable(Screen.CropRecommendation.route) {
            val cropViewModel: com.krishiai.app.ui.screens.insights.CropRecommendationViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.CropRecommendationScreen(
                navController = navController,
                viewModel = cropViewModel
            )
        }
        composable(Screen.VoiceAssistant.route) {
            val voiceAssistantViewModel: com.krishiai.app.ui.screens.insights.VoiceAssistantViewModel = hiltViewModel()
            com.krishiai.app.ui.screens.insights.VoiceAssistantScreen(
                navController = navController,
                viewModel = voiceAssistantViewModel
            )
        }
    }
}
