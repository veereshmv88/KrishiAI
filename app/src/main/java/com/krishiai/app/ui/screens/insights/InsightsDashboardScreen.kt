package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.R
import com.krishiai.app.ui.components.AppBottomNavigation
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.theme.BrandGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsDashboardScreen(
    navController: NavController
) {
    Scaffold(
        containerColor = Color(0xFFF9F9F9), // Light gray background
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "AI Tools",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = "Sparkles",
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "Smart AI for Smarter Farming",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    com.krishiai.app.ui.components.NotificationIcon(
                        onClick = { navController.navigate(Screen.NotificationCenter.route) },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F9F9),
                    titleContentColor = BrandGreen,
                    actionIconContentColor = Color.DarkGray,
                    navigationIconContentColor = Color.DarkGray
                )
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeroBanner()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AI Tools",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    )
                    Surface(
                        color = BrandGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "How AI Helps?",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = BrandGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Info",
                                tint = BrandGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                val aiTools = listOf(
                    AiToolItem(
                        "AI Voice Assistant",
                        "Talk to AI assistant for instant farming advice.",
                        Icons.Rounded.Mic,
                        Screen.VoiceAssistant.route
                    ),
                    AiToolItem(
                        "Live Market Prices",
                        "View real-time AGMARKNET crop prices across India.",
                        Icons.Rounded.Storefront,
                        Screen.LiveMarketPrice.route
                    ),
                    AiToolItem(
                        "AI Price Prediction",
                        "Predict the best selling price for your crops using ML models.",
                        Icons.Rounded.TrendingUp,
                        Screen.PricePrediction.route 
                    ),
                    AiToolItem(
                        "Disease Detection (Scanner)",
                        "Detect crop diseases instantly using camera and AI.",
                        Icons.Rounded.ImageSearch,
                        Screen.CropScanner.route
                    ),
                    AiToolItem(
                        "Profit Estimator",
                        "Calculate expected profit and optimize your farming.",
                        Icons.Rounded.Calculate,
                        Screen.ProfitEstimation.route
                    ),
                    AiToolItem(
                        "Negotiation Assistant",
                        "Get AI suggestions and smart replies for better deals.",
                        Icons.Rounded.Handshake,
                        Screen.NegotiationAssistant.route
                    ),
                    AiToolItem(
                        "Market Intelligence",
                        "Get insights on market trends, demand and price movement.",
                        Icons.Rounded.BarChart,
                        Screen.MarketIntelligence.route 
                    ),
                    AiToolItem(
                        "Crop Recommender",
                        "Get AI recommended crops based on your soil and location.",
                        Icons.Rounded.EnergySavingsLeaf,
                        Screen.CropRecommendation.route 
                    ),
                    AiToolItem(
                        "AI OCR Scanner",
                        "Scan fertilizer bags, pesticide labels, and receipts using ML Kit.",
                        Icons.Rounded.DocumentScanner,
                        Screen.OcrScanner.route 
                    ),
                    AiToolItem(
                        "OCR History",
                        "View previously scanned documents and extracted insights.",
                        Icons.Rounded.History,
                        Screen.OcrHistory.route
                    )
                )

                // Need a fixed height grid for LazyColumn interoperability
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in aiTools.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AiToolCard(aiTools[i]) {
                                    navController.navigate(aiTools[i].route)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (i + 1 < aiTools.size) {
                                    AiToolCard(aiTools[i + 1]) {
                                        navController.navigate(aiTools[i + 1].route)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                TechStackSection()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Assume there's a background decorative image. Since we don't have the exact image,
        // we'll rely on the gradient and compose standard UI elements to simulate the hero.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Harness the power of AI\nto grow better, earn more\nand farm smarter.",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E3924)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Scroll to tools */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Explore AI Tools \u2193", color = Color.White)
                }
            }
            Box(
                modifier = Modifier.weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for AI chip graphic. Using an icon for now.
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Memory,
                        contentDescription = "AI Chip",
                        tint = BrandGreen,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        "AI",
                        color = BrandGreen,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

data class AiToolItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun AiToolCard(item: AiToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BrandGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E3924)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, lineHeight = 16.sp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowForward,
                        contentDescription = "Go",
                        tint = BrandGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TechStackSection() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Memory, contentDescription = null, tint = BrandGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI Models & Technology",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                )
            }
            Surface(
                color = BrandGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "View Details",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = BrandGreen,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 6 tech stack items
        val techItems = listOf(
            Triple("Price Prediction", "Random Forest\nRegressor", Icons.Rounded.Share),
            Triple("Disease Detection", "TensorFlow Lite\n(EfficientNet)", Icons.Rounded.ModelTraining),
            Triple("OCR Scanner", "Google ML Kit\nOCR", Icons.Rounded.DocumentScanner),
            Triple("Voice Search", "Android Speech\nRecognizer", Icons.Rounded.Mic),
            Triple("Weather Intelligence", "OpenWeather API", Icons.Rounded.Cloud),
            Triple("Data Analytics", "Python • Pandas\n• NumPy", Icons.Rounded.Analytics),
            Triple("Market Analytics", "Scikit-learn\n& AI Models", Icons.Rounded.DataUsage),
            Triple("Cloud & Backend", "Firebase\n& Firestore", Icons.Rounded.CloudSync)
        )

        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in techItems.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TechItem(
                        modifier = Modifier.weight(1f),
                        title = techItems[i].first,
                        desc = techItems[i].second,
                        icon = techItems[i].third
                    )
                    if (i + 1 < techItems.size) {
                        TechItem(
                            modifier = Modifier.weight(1f),
                            title = techItems[i + 1].first,
                            desc = techItems[i + 1].second,
                            icon = techItems[i + 1].third
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Your data is safe and secure with enterprise-grade security.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                )
                Text(
                    "We use advanced AI responsibly to support your farming decisions.",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF15803D))
                )
            }
        }
    }
}

@Composable
fun TechItem(modifier: Modifier = Modifier, title: String, desc: String, icon: ImageVector) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.DarkGray))
            Text(desc, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp, lineHeight = 12.sp))
        }
    }
}
