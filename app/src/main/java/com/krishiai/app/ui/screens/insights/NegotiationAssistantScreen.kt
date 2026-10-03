package com.krishiai.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.domain.ai.NegotiationAction
import com.krishiai.app.domain.ai.NegotiationResult
import com.krishiai.app.ui.components.PremiumCard
import com.krishiai.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationAssistantScreen(
    navController: NavController,
    viewModel: NegotiationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    var cropName by remember { mutableStateOf("Tomato") }
    var buyerOfferPrice by remember { mutableStateOf("") }
    var predictedFairPrice by remember { mutableStateOf("") }
    var currentApmcPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Negotiation Bot", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFF3E0)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE65100)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Handshake, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("AI Deal Assistant", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100)))
                        Text("Smart counter-offers to maximize profits", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100).copy(alpha = 0.8f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { SectionHeader("Deal Details", "Enter buyer's offer to analyze") }

            item {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Name") },
                    leadingIcon = { Icon(Icons.Rounded.Eco, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            item { PriceInputField(label = "Buyer's Offer (₹/kg)", value = buyerOfferPrice, onValueChange = { buyerOfferPrice = it }) }
            item { PriceInputField(label = "AI Predicted Fair Price (₹/kg)", value = predictedFairPrice, onValueChange = { predictedFairPrice = it }) }
            item { PriceInputField(label = "Current APMC Price (₹/kg)", value = currentApmcPrice, onValueChange = { currentApmcPrice = it }) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = {
                        viewModel.analyzeOffer(
                            cropName = cropName,
                            buyerOfferPrice = buyerOfferPrice.toDoubleOrNull() ?: 0.0,
                            predictedFairPrice = predictedFairPrice.toDoubleOrNull() ?: 0.0,
                            currentApmcPrice = currentApmcPrice.toDoubleOrNull() ?: 0.0
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Generate Response", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                }
            }

            item {
                when (val state = uiState) {
                    is NegotiationState.Loading -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth().height(150.dp), contentPadding = PaddingValues(0.dp)) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Generating smart response...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                    is NegotiationState.Success -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        NegotiationResultCard(state.result)
                    }
                    is NegotiationState.Error -> {
                        PremiumCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun PriceInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Rounded.CurrencyRupee, null, tint = MaterialTheme.colorScheme.primary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun NegotiationResultCard(result: NegotiationResult) {
    val actionColor = when (result.recommendedAction) {
        NegotiationAction.ACCEPT -> Color(0xFF2E7D32)
        NegotiationAction.COUNTER -> Color(0xFFE65100)
        NegotiationAction.REJECT -> MaterialTheme.colorScheme.error
    }
    val bgColor = when (result.recommendedAction) {
        NegotiationAction.ACCEPT -> Color(0xFFE8F5E9)
        NegotiationAction.COUNTER -> Color(0xFFFFF3E0)
        NegotiationAction.REJECT -> Color(0xFFFFEBEE)
    }
    val actionIcon = when (result.recommendedAction) {
        NegotiationAction.ACCEPT -> Icons.Rounded.CheckCircle
        NegotiationAction.COUNTER -> Icons.AutoMirrored.Rounded.CompareArrows
        NegotiationAction.REJECT -> Icons.Rounded.Cancel
    }

    PremiumCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            Column(modifier = Modifier.background(bgColor).padding(20.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(actionIcon, null, tint = actionColor, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.recommendedAction.name,
                            style = MaterialTheme.typography.titleMedium.copy(color = actionColor, fontWeight = FontWeight.ExtraBold)
                        )
                    }
                    if (result.suggestedCounterOfferPrice != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Counter Offer", style = MaterialTheme.typography.labelSmall.copy(color = actionColor.copy(alpha = 0.8f)))
                            Text(
                                text = "₹${result.suggestedCounterOfferPrice}",
                                style = MaterialTheme.typography.titleMedium.copy(color = actionColor, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text("AI Reasoning", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = result.reasoning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Copy Response (English)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                ResponseBox(result.suggestedResponseEnglish)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Copy Response (Kannada)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))
                ResponseBox(result.suggestedResponseKannada)
            }
        }
    }
}

@Composable
fun ResponseBox(text: String) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
            onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text)) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape).size(36.dp)
        ) {
            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
}
