package com.krishiai.app.disease.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HealthScoreCard(score: Int) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedScore by animateFloatAsState(
        targetValue = if (animationPlayed) score.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1500)
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI Health Score",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(120.dp),
                    color = when {
                        animatedScore >= 80 -> MaterialTheme.colorScheme.primary
                        animatedScore >= 50 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    },
                    strokeWidth = 12.dp
                )
                Text(
                    text = "${animatedScore.toInt()}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
