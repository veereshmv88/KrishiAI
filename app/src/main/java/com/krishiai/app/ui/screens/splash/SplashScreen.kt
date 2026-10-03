package com.krishiai.app.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krishiai.app.R
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.ui.theme.BrandGreen

@Composable
fun SplashScreen(
    navController: NavController,
    onNavigateToAuth: () -> Unit,
    onNavigateToFarmer: () -> Unit,
    onNavigateToBuyer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Section (White)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.EnergySavingsLeaf,
                contentDescription = "Logo",
                tint = BrandGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "KrishiAI",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandGreen,
                    letterSpacing = (-1).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI-Powered Agriculture\nfor a Better Tomorrow",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }

        // Bottom Section (Image & Actions)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
        ) {
            // Background curved shape
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.15f)
                            quadraticTo(
                                size.width * 0.5f, -size.height * 0.1f,
                                size.width, size.height * 0.2f
                            )
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        clipPath(path) {
                            this@drawWithContent.drawContent()
                        }
                    }
            ) {
                // Farmer image background removed due to missing asset
                // Dark green gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0A3617).copy(alpha = 0.8f),
                                    Color(0xFF062B10)
                                ),
                                startY = 0f
                            )
                        )
                )

                // Content inside the green area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Smart Farming. Better Future.",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { navController.navigate(Screen.WelcomeRoleSelection.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onNavigateToAuth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BrandGreen
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Login / Signup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
