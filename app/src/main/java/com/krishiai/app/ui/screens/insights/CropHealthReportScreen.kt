package com.krishiai.app.ui.screens.insights

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.disease.ai.RecommendedVideo
import com.krishiai.app.disease.ai.DiseaseKnowledge
import com.krishiai.app.utils.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropHealthReportScreen(
    navController: NavController,
    diseaseName: String,
    confidenceScore: Float,
    viewModel: CropHealthReportViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    var isGeneratingPdf by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(diseaseName, confidenceScore) {
        viewModel.loadReport(diseaseName, confidenceScore)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Crop Health Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is CropHealthReportState.Success) {
                        val successState = uiState as CropHealthReportState.Success
                        IconButton(onClick = {
                            isGeneratingPdf = true
                            val it = successState.diseaseKnowledge
                            val file = PdfReportGenerator.generateDiseaseReport(
                                context = context,
                                cropName = it.cropName,
                                diseaseName = it.diseaseName,
                                scientificName = it.scientificName,
                                confidenceScore = successState.confidenceScore,
                                severityLevel = it.severityLevel,
                                healthScore = successState.healthScore,
                                treatment = it.organicTreatment + "\n" + it.chemicalTreatment,
                                organicTreatments = it.organicTreatment,
                                chemicalTreatments = it.chemicalTreatment,
                                recommendedFertilizers = it.recommendedFertilizer
                            )
                            isGeneratingPdf = false
                            // In real app, launch intent to view/share PDF
                        }) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Rounded.PictureAsPdf, contentDescription = "Export PDF")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CropHealthReportState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CropHealthReportState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = Color.Red)
                }
            }
            is CropHealthReportState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HealthScoreCard(
                            cropName = state.diseaseKnowledge.cropName,
                            diseaseName = state.diseaseKnowledge.diseaseName,
                            healthScore = state.healthScore,
                            confidence = state.confidenceScore,
                            severity = state.diseaseKnowledge.severityLevel
                        )
                    }

                    item {
                        DiseaseInfoCard(
                            scientificName = state.diseaseKnowledge.scientificName,
                            description = state.diseaseKnowledge.description,
                            symptoms = state.diseaseKnowledge.symptoms,
                            causes = state.diseaseKnowledge.causes
                        )
                    }

                    item {
                        ExpertSolutionCard(
                            organic = state.diseaseKnowledge.organicTreatment,
                            chemical = state.diseaseKnowledge.chemicalTreatment,
                            fertilizers = state.diseaseKnowledge.recommendedFertilizer,
                            pesticides = state.diseaseKnowledge.recommendedPesticide
                        )
                    }

                    item {
                        AIInsightsCard()
                    }

                    item {
                        WeatherImpactCard()
                    }
                    
                    item {
                        RecoveryTimelineCard()
                    }

                    if (state.recommendedVideos.isNotEmpty()) {
                        item {
                            Text(
                                "Learn From Experts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(state.recommendedVideos) { video ->
                            VideoRecommendationCard(video = video)
                        }
                    }

                    item {
                        VoiceAssistantCard()
                    }
                    
                    item {
                        ActionButtonsCard()
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HealthScoreCard(
    cropName: String,
    diseaseName: String,
    healthScore: Int,
    confidence: Float,
    severity: String
) {
    val color = if (healthScore > 80) Color(0xFF4CAF50) else if (healthScore > 50) Color(0xFFFF9800) else Color(0xFFF44336)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(4.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$healthScore", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(diseaseName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(cropName, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Confidence: ${(confidence * 100).toInt()}%")
                Text("Severity: $severity", color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DiseaseInfoCard(scientificName: String, description: String, symptoms: String, causes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Disease Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Scientific Name: $scientificName", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Symptoms:", fontWeight = FontWeight.Bold)
            Text(symptoms, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Causes:", fontWeight = FontWeight.Bold)
            Text(causes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ExpertSolutionCard(
    organic: String,
    chemical: String,
    fertilizers: String,
    pesticides: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Science, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Expert Solutions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (organic.isNotBlank()) {
                Text("Organic Treatments", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text(organic, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (chemical.isNotBlank()) {
                Text("Chemical Treatments", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Text(chemical, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (fertilizers.isNotBlank()) {
                Text("Recommended Fertilizers (Urea, DAP, NPK, etc.)", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                Text(fertilizers, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (pesticides.isNotBlank()) {
                Text("Recommended Pesticides", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                Text(pesticides, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun VideoRecommendationCard(video: RecommendedVideo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle YouTube click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Note: Thumbnail would use Coil in real app
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayCircleOutline, contentDescription = "Play", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(video.title, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(video.channelName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Badge(containerColor = Color.Red, contentColor = Color.White) {
                    Text(if (video.language == "kn") "Kannada" else "English")
                }
            }
        }
    }
}

@Composable
fun AIInsightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TipsAndUpdates, contentDescription = null, tint = Color(0xFFE65100))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Insights - Immediate Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("1. Isolate infected plants immediately.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("2. Avoid overhead irrigation to reduce spread.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("3. Apply recommended organic fungicides today.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeatherImpactCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Thermostat, contentDescription = null, tint = Color(0xFF1565C0))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Weather Impact on Disease", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Temp", style = MaterialTheme.typography.labelSmall)
                    Text("32°C", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Humidity", style = MaterialTheme.typography.labelSmall)
                    Text("75%", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Spread Risk", style = MaterialTheme.typography.labelSmall)
                    Text("High", fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Recommendation: High humidity favors disease spread. Avoid evening watering.", style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

@Composable
fun RecoveryTimelineCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recovery Prediction Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TimelineItem("Day 1", "Apply Initial Treatment")
            TimelineItem("Day 3", "Monitor for halting of spread")
            TimelineItem("Day 7", "Apply secondary foliar spray")
            TimelineItem("Day 14", "Expected 80% Recovery")
        }
    }
}

@Composable
fun TimelineItem(day: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text("$day: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Text(action, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VoiceAssistantCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color(0xFF6A1B9A))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voice Assistant Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))) {
                    Text("Explain Disease", fontSize = 12.sp)
                }
                Button(onClick = { }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))) {
                    Text("Read Treatment", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) {
                    Text("Translate to Kannada", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { }, modifier = Modifier.weight(1f)) {
                    Text("Ask Follow-up", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ActionButtonsCard() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Rounded.Scanner, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Again")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Share Report")
            }
            OutlinedButton(onClick = { }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Save to History")
            }
        }
        TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.SupportAgent, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact Agricultural Expert")
        }
    }
}
