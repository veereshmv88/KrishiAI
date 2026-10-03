package com.krishiai.app.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // App Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "KrishiAI",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Version 2.0.0",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                )
            }

            // Build Information
            AboutSection(icon = Icons.Rounded.Info, title = "Build Information") {
                InfoRow("Build", "v2.0.0-beta")
                InfoRow("Environment", "Development")
                InfoRow("Build Type", "Beta / Development Preview")
                InfoRow("Release Date", "July 2026")
            }

            // Development Details
            AboutSection(icon = Icons.Rounded.Architecture, title = "Development Details") {
                InfoRow("Architecture", "Clean Architecture (MVVM)")
                InfoRow("Database", "Room + Firebase Firestore")
                InfoRow("AI Engine", "TensorFlow Lite")
                InfoRow("Offline Support", "Available")
                InfoRow("Cloud Sync", "Firebase Cloud Sync")
            }

            // About ML Models
            AboutSection(icon = Icons.Rounded.Psychology, title = "AI Models & Technology Documentation") {
                Text(
                    text = "KrishiAI is powered by advanced on-device and edge Machine Learning models designed specifically for Indian agriculture.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Price Prediction", "Random Forest Regressor (Ensemble learning simulated via robust heuristics for offline capabilities)")
                InfoRow("Disease Detection", "MobileNetV1 Quantized TFLite (224x224 input, converted for Edge TPUs/Mobile)")
                InfoRow("Quality Grading", "MobileNet Classification (ImageNet fallback heuristics for grading visual consistency)")
                InfoRow("Profit Estimation", "Rule-based Regression (calculates labor, transport, APMC live rates)")
                InfoRow("Negotiation Bot", "Dynamic thresholding AI Decision Engine")
                InfoRow("OCR Scanner", "Google ML Kit Vision (On-device text recognition)")
                InfoRow("Voice Search", "Android SpeechRecognizer (Natural language parsing)")
                InfoRow("Inference Pipeline", "100% On-device, offline supported using TensorFlow Lite Interpreter")
                InfoRow("Model Sources", "TensorFlow Hub, Google ML Kit, Android Core APIs")
            }

            // Developer Info
            AboutSection(icon = Icons.Rounded.Person, title = "Developed By") {
                Text(
                    text = "Veeresh M V",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Software & Machine Learning Developer",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // Technical Support
            AboutSection(icon = Icons.Rounded.SupportAgent, title = "Technical Support") {
                Text(
                    text = "veereshmvmath009@gmail.com",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+91 9380022929",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            // Copyright
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "© 2026 KrishiAI. All Rights Reserved.",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AboutSection(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        SectionHeader(title, "")
        Spacer(modifier = Modifier.height(12.dp))
        PremiumCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant), modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}
