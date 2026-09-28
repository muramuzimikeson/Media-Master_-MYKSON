package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AudioVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 20
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val safeAmplitudes = if (amplitudes.isEmpty()) List(barCount) { 0.15f } else amplitudes

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = barWidth * 0.5f
        val startOffset = (totalWidth - (barCount * (barWidth + spacing))) / 2f

        val gradient = Brush.verticalGradient(
            colors = listOf(primaryColor, secondaryColor, tertiaryColor)
        )

        for (i in 0 until barCount) {
            val amp = if (isPlaying) {
                safeAmplitudes[i % safeAmplitudes.size]
            } else {
                0.08f
            }
            val barHeight = (amp * totalHeight).coerceIn(4f, totalHeight)
            val x = startOffset + i * (barWidth + spacing)
            val y = (totalHeight - barHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
