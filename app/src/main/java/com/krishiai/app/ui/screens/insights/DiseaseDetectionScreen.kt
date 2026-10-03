package com.krishiai.app.ui.screens.insights

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.krishiai.app.domain.ai.DiseaseDetectionResult
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetectionScreen(
    navController: NavController,
    viewModel: DiseaseDetectionViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val tempFile = File(context.cacheDir, "crop_image_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            viewModel.detectDisease(tempFile)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disease Scanner", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Scanner Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.CenterFocusStrong, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("TensorFlow Lite AI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer))
                        Text("Instant on-device crop analysis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            // Upload Area
            item {
                PremiumCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tap to upload leaf image", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Works offline with ML Kit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Results Section
            item {
                when (val state = uiState) {
                    is DiseaseDetectionState.Idle -> {
                        RecentChecksSection()
                    }
                    is DiseaseDetectionState.Loading -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(0.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Analyzing image with AI...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    is DiseaseDetectionState.Success -> {
                        DetectionResultCard(state.result)
                    }
                    is DiseaseDetectionState.Error -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionResultCard(result: DiseaseDetectionResult) {
    val isHealthy = result.isHealthy
    val containerColor = if (isHealthy) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (isHealthy) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(modifier = Modifier.background(containerColor).padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = if (isHealthy) Icons.Rounded.CheckCircle else Icons.Rounded.Warning, contentDescription = null, tint = contentColor, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = result.diseaseName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = contentColor))
                    Text(text = "AI Confidence: ${(result.confidenceScore * 100).toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.8f)))
                }
                if (!isHealthy) {
                    Surface(
                        color = when (result.severityLevel) {
                            "SEVERE" -> Color(0xFFFFCDD2)
                            "MODERATE" -> Color(0xFFFFF9C4)
                            else -> Color(0xFFE3F2FD)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = result.severityLevel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (result.severityLevel) {
                                    "SEVERE" -> Color(0xFFC62828)
                                    "MODERATE" -> Color(0xFFF57F17)
                                    else -> Color(0xFF1565C0)
                                }
                            )
                        )
                    }
                }
            }

            if (!isHealthy) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (result.symptoms.isNotEmpty()) {
                        Text("Detected Symptoms", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        result.symptoms.forEach { 
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                Icon(Icons.Rounded.FiberManualRecord, null, modifier = Modifier.size(10.dp).padding(top = 4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (result.suggestedTreatments.isNotEmpty()) {
                        Text("AI Suggested Treatments", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        result.suggestedTreatments.forEach { 
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (result.preventiveMeasures.isNotEmpty()) {
                        Text("Preventive Measures", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        result.preventiveMeasures.forEach { 
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                Icon(Icons.Rounded.Shield, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = Color(0xFF1976D2))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentChecksSection() {
    Column {
        SectionHeader("Recent Scans", "Your previous AI health checks")
        Spacer(modifier = Modifier.height(16.dp))
        RecentCheckItem("Tomato Leaf", "Early Blight", "2 days ago", false)
        Spacer(modifier = Modifier.height(12.dp))
        RecentCheckItem("Chilli Leaf", "Healthy", "4 days ago", true)
    }
}

@Composable
fun RecentCheckItem(crop: String, status: String, time: String, isHealthy: Boolean) {
    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(crop, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(status, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = if (isHealthy) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error))
                }
            }
            Text(time, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
