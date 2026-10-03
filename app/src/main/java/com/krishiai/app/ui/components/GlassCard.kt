package com.krishiai.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 2.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0x20FFFFFF) else Color(0xB2FFFFFF)
    
    // Light border color overlay for glassmorphism reflections
    val borderColor = if (isDark) {
        Color(0x1EFFFFFF) // Subtle transparent white border in dark mode
    } else {
        Color(0x2B2E7D32) // Subtle green-tinted transparent border in light mode
    }

    Box(
        modifier = modifier
            .shadow(elevation, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .border(
                BorderStroke(borderWidth, borderColor),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}
