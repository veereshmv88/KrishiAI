package com.krishiai.app.ui.screens.onboarding

import com.krishiai.app.data.local.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import com.krishiai.app.ui.components.PrimaryButton
import com.krishiai.app.ui.navigation.Screen
import com.krishiai.app.utils.Constants


data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    appPreferences: AppPreferences
) {
    val pages = listOf(
        OnboardingPage(
            title = "Direct Farm-to-Buyer Marketplace",
            description = "Skip the middlemen. Connect directly with verified buyers across Karnataka and maximize your agricultural profits.",
            icon = Icons.Default.Grass,
            color = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Explainable AI Fair Pricing",
            description = "Get fair price recommendations powered by local weather, crop quality, seasonal demand, and harvest freshness.",
            icon = Icons.Default.Analytics,
            color = MaterialTheme.colorScheme.secondary
        ),
        OnboardingPage(
            title = "Secure Negotiated Selling",
            description = "See detailed pricing breakdowns, match buyer requirements, and dial farmers directly over phone to finalize deal terms.",
            icon = Icons.Default.Call,
            color = MaterialTheme.colorScheme.tertiary
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentPage < pages.size - 1) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            completeOnboarding(navController, appPreferences, coroutineScope)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sliding Graphic & Content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                } else {
                    slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                }
            },
            label = "onboarding_slider"
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large styled circle icon
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(page.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = page.color
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1.2f))

        // Page Indicator Dot bars
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, _ ->
                val width = if (index == currentPage) 24.dp else 8.dp
                val color = if (index == currentPage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        PrimaryButton(
            text = if (currentPage == pages.size - 1) "Get Started" else "Next",
            onClick = {
                if (currentPage < pages.size - 1) {
                    currentPage++
                } else {
                    completeOnboarding(navController, appPreferences, coroutineScope)
                }
            }
        )
    }
}

// Standard clickable import is utilized in layout.

private fun completeOnboarding(
    navController: NavController, 
    appPreferences: AppPreferences,
    coroutineScope: CoroutineScope
) {
    coroutineScope.launch {
        // We will implement DataStore update in AppPreferences for this
        // For now, we simulate setting it true
    }
    navController.navigate(Screen.WelcomeRoleSelection.route) {
        popUpTo(Screen.Onboarding.route) { inclusive = true }
    }
}
