package com.krishiai.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object WelcomeRoleSelection : Screen("welcome_role_selection")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    
    // Main Bottom Navigation
    object Main : Screen("main")
    object FarmerMain : Screen("farmer_main")
    object BuyerMain : Screen("buyer_main")
    object Home : Screen("home") // Dashboard
    object Marketplace : Screen("marketplace")
    object SellCrop : Screen("sell_crop")

    object LiveMarketPrice : Screen("live_market_price")
    object AITools : Screen("ai_tools")
    
    // AI Feature Screens
    object DiseaseDetection : Screen("disease_detection")
    object CropScanner : Screen("crop_scanner")
    object OcrScanner : Screen("ocr_scanner")
    object OcrHistory : Screen("ocr_history")
    object CropHealthReport : Screen("crop_health_report/{diseaseName}/{confidence}") {
        fun createRoute(diseaseName: String, confidence: Float) = "crop_health_report/${diseaseName.replace("/", "_")}/$confidence"
    }
    object ProfitEstimation : Screen("profit_estimation")
    object NegotiationAssistant : Screen("negotiation_assistant")
    object PricePrediction : Screen("price_prediction")
    object MarketIntelligence : Screen("market_intelligence")
    object CropRecommendation : Screen("crop_recommendation")
    object VoiceAssistant : Screen("voice_assistant")
    
    // Farmer screens
    object FarmerDashboard : Screen("farmer_dashboard")
    object UploadCrop : Screen("upload_crop")
    object MyProducts : Screen("my_products")
    
    // Buyer screens
    object BuyerDashboard : Screen("buyer_dashboard")
    object BuyerSearch : Screen("buyer_search")
    object BuyerCart : Screen("buyer_cart")
    object BuyerOrders : Screen("buyer_orders")
    object BuyerProfile : Screen("buyer_profile")
    object BuyerProductDetail : Screen("buyer_product/{productId}") {
        fun createRoute(productId: String) = "buyer_product/$productId"
    }
    object BuyerPricePrediction : Screen("buyer_prediction")
    object BuyerNearbySellers : Screen("buyer_nearby")
    object BuyerRecommendations : Screen("buyer_recommendations")
    object BuyerMarketIntelligence : Screen("buyer_market_intelligence")
    object BuyerCompare : Screen("buyer_compare")
    object BuyerWishlist : Screen("buyer_wishlist")
    object BuyerCheckout : Screen("buyer_checkout")
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    
    // Shared screens
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object PersonalInfo : Screen("personal_info")
    object MyFarmDetails : Screen("my_farm_details")
    object Settings : Screen("settings")
    object NotificationsSettings : Screen("settings_notifications")
    object PrivacySettings : Screen("settings_privacy")
    object AIPreferences : Screen("settings_ai")
    object BackupRestore : Screen("settings_backup")
    object HelpCenter : Screen("settings_help")
    object Feedback : Screen("settings_feedback")
    object LanguageSettings : Screen("settings_language")
    object AccountManagement : Screen("settings_account")
    
    // Notification Center & Search
    object NotificationCenter : Screen("notification_center")
    object GlobalSearch : Screen("global_search")
    
    // Cart
    object Cart : Screen("cart")
    
    object About : Screen("about")
}
