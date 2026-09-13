package com.example.designsystem.component

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompass(
    bearing: Float, // Device rotation from North
    qiblaDirection: Float, // Qibla angle from North
    modifier: Modifier = Modifier,
    isAligned: Boolean = false
) {
    val activeTeal = if (isAligned) Color(0xFF10B981) else Color(0xFF0F766E)
    val alignGlow = if (isAligned) Color(0x3310B981) else Color(0x1A0F766E)

    Box(
        modifier = modifier.size(290.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(alignGlow)
                .border(1.dp, activeTeal.copy(alpha = 0.25f), CircleShape)
        )

        // White Dial Body
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .shadow(12.dp, CircleShape, spotColor = Color(0x20000000))
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFE2E8F0), CircleShape)
        )

        Canvas(modifier = Modifier.fillMaxSize(0.92f)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)
            val radius = size.minDimension / 2f - 12.dp.toPx()

            // 1. Draw Dial Tick Marks and Cardinal Points (Rotated by -bearing)
            rotate(degrees = -bearing, pivot = center) {
                // Background subtle rosette pattern
                drawCircle(
                    color = Color(0xFFF1F5F9),
                    radius = radius * 0.55f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFF1F5F9),
                    radius = radius * 0.38f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Ticks
                val totalTicks = 60
                for (i in 0 until totalTicks) {
                    val angleDeg = i * (360f / totalTicks)
                    val isMajor = i % 5 == 0
                    val isCardinal = i % 15 == 0

                    val tickLength = if (isCardinal) 12.dp.toPx() else if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                    val tickWidth = if (isCardinal) 2.dp.toPx() else if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                    val tickColor = if (isCardinal) Color(0xFF334155) else if (isMajor) Color(0xFF64748B) else Color(0xFFCBD5E1)

                    val startR = radius - tickLength
                    val endR = radius

                    val rad = Math.toRadians(angleDeg.toDouble() - 90.0)
                    val x1 = cx + startR * cos(rad).toFloat()
                    val y1 = cy + startR * sin(rad).toFloat()
                    val x2 = cx + endR * cos(rad).toFloat()
                    val y2 = cy + endR * sin(rad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Cardinal Directions in Arabic: N (ش), E (ع), S (ج), W (غ)
                val cardinals = listOf(
                    "ش" to 0f,   // North
                    "ع" to 90f,  // East
                    "ج" to 180f, // South
                    "غ" to 270f  // West
                )

                val paint = Paint().apply {
                    color = Color(0xFF0F172A).toArgb()
                    textSize = 15.sp.toPx()
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }

                val textR = radius - 24.dp.toPx()
                cardinals.forEach { (label, angleDeg) ->
                    val rad = Math.toRadians(angleDeg.toDouble() - 90.0)
                    val tx = cx + textR * cos(rad).toFloat()
                    val ty = cy + textR * sin(rad).toFloat() + 5.dp.toPx()

                    drawContext.canvas.nativeCanvas.drawText(label, tx, ty, paint)
                }
            }

            // 2. Center 3D Isometric Kaaba Graphic
            val kSize = 18.dp.toPx()
            // Top face (Diamond)
            val topPath = Path().apply {
                moveTo(cx, cy - kSize * 0.8f)
                lineTo(cx + kSize * 0.7f, cy - kSize * 0.4f)
                lineTo(cx, cy)
                lineTo(cx - kSize * 0.7f, cy - kSize * 0.4f)
                close()
            }
            drawPath(topPath, Color(0xFF2B2D31))

            // Left face
            val leftPath = Path().apply {
                moveTo(cx - kSize * 0.7f, cy - kSize * 0.4f)
                lineTo(cx, cy)
                lineTo(cx, cy + kSize * 0.8f)
                lineTo(cx - kSize * 0.7f, cy + kSize * 0.4f)
                close()
            }
            drawPath(leftPath, Color(0xFF181A1D))

            // Right face
            val rightPath = Path().apply {
                moveTo(cx, cy)
                lineTo(cx + kSize * 0.7f, cy - kSize * 0.4f)
                lineTo(cx + kSize * 0.7f, cy + kSize * 0.4f)
                lineTo(cx, cy + kSize * 0.8f)
                close()
            }
            drawPath(rightPath, Color(0xFF0F1012))

            // Gold Kiswa Band across front faces
            val goldColor = Color(0xFFD4AF37)
            val bandLeft = Path().apply {
                moveTo(cx - kSize * 0.7f, cy - kSize * 0.2f)
                lineTo(cx, cy + kSize * 0.2f)
                lineTo(cx, cy + kSize * 0.32f)
                lineTo(cx - kSize * 0.7f, cy - kSize * 0.08f)
                close()
            }
            drawPath(bandLeft, goldColor)

            val bandRight = Path().apply {
                moveTo(cx, cy + kSize * 0.2f)
                lineTo(cx + kSize * 0.7f, cy - kSize * 0.2f)
                lineTo(cx + kSize * 0.7f, cy - kSize * 0.08f)
                lineTo(cx, cy + kSize * 0.32f)
                close()
            }
            drawPath(bandRight, goldColor)

            // Gold Door on right face
            val doorPath = Path().apply {
                moveTo(cx + kSize * 0.25f, cy + kSize * 0.22f)
                lineTo(cx + kSize * 0.45f, cy + kSize * 0.11f)
                lineTo(cx + kSize * 0.45f, cy + kSize * 0.45f)
                lineTo(cx + kSize * 0.25f, cy + kSize * 0.56f)
                close()
            }
            drawPath(doorPath, goldColor)

            // 3. Qibla Arrow / Needle pointing towards (qiblaDirection - bearing)
            val needleAngle = qiblaDirection - bearing
            rotate(degrees = needleAngle, pivot = center) {
                val arrowTipR = radius - 8.dp.toPx()
                val arrowBaseR = 28.dp.toPx()

                val arrowPath = Path().apply {
                    moveTo(cx, cy - arrowTipR)
                    lineTo(cx - 10.dp.toPx(), cy - arrowBaseR)
                    lineTo(cx, cy - arrowBaseR + 4.dp.toPx())
                    lineTo(cx + 10.dp.toPx(), cy - arrowBaseR)
                    close()
                }

                // Shadow under needle
                drawPath(
                    path = arrowPath,
                    color = Color.Black.copy(alpha = 0.2f)
                )

                // Needle main color
                drawPath(
                    path = arrowPath,
                    color = activeTeal
                )
            }
        }
    }
}
