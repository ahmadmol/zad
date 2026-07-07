package com.example.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompass(
    bearing: Float, // Device rotation
    qiblaDirection: Float, // Direction to Kaaba from North
    modifier: Modifier = Modifier,
    isAligned: Boolean = false
) {
    val primaryColor = if (isAligned) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner subtle background circle
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .background(surfaceVariantColor.copy(alpha = 0.5f), CircleShape)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 20.dp.toPx()

            // 1. Draw small ticks around the compass
            val tickCount = 24
            for (i in 0 until tickCount) {
                val angle = i * (360f / tickCount) - bearing
                val startRadius = radius - 8.dp.toPx()
                val endRadius = radius
                
                val startX = center.x + startRadius * cos(angle * PI / 180).toFloat()
                val startY = center.y + startRadius * sin(angle * PI / 180).toFloat()
                val endX = center.x + endRadius * cos(angle * PI / 180).toFloat()
                val endY = center.y + endRadius * sin(angle * PI / 180).toFloat()

                drawLine(
                    color = outlineColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw Cardinal Points (N, E, S, W)
            val cardinalPoints = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
            cardinalPoints.forEach { (text, angleOffset) ->
                val angle = angleOffset - 90f - bearing
                val textRadius = radius + 15.dp.toPx()
                val x = center.x + textRadius * cos(angle * PI / 180).toFloat()
                val y = center.y + textRadius * sin(angle * PI / 180).toFloat()

                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        x,
                        y + 5.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.copy(alpha = 0.6f).toArgb()
                            textSize = 14.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    )
            }

            // 3. Center Dot
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = center
            )

            // 4. Qibla Arrow
            rotate(degrees = qiblaDirection - bearing, pivot = center) {
                drawLine(
                    color = primaryColor,
                    start = center,
                    end = Offset(center.x, center.y - radius + 15.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
                
                // Arrow head (small triangle)
                val headPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x, center.y - radius + 10.dp.toPx())
                    lineTo(center.x - 6.dp.toPx(), center.y - radius + 22.dp.toPx())
                    lineTo(center.x + 6.dp.toPx(), center.y - radius + 22.dp.toPx())
                    close()
                }
                drawPath(headPath, primaryColor)

                // Kaaba Icon
                drawContext.canvas.nativeCanvas.drawText(
                    "🕋",
                    center.x,
                    center.y - radius - 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        textSize = 36.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
