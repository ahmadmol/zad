package com.example.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.designsystem.theme.IhsanTheme

enum class IhsanScene { WELCOME, PRAYER, QURAN, JOURNEY, AQSA }

/** Original vector artwork drawn for Ihsan. No downloaded photographs or religious text. */
@Composable
fun IhsanIllustration(scene: IhsanScene, modifier: Modifier = Modifier) {
    val accent = IhsanTheme.colors.accentContent
    val dark = IhsanTheme.isDark
    Canvas(modifier) {
        val unit = size.minDimension / 320f
        translate((size.width - 320 * unit) / 2, (size.height - 320 * unit) / 2) {
            scale(unit, unit, Offset.Zero) {
                val gold = Color(0xFFD1AE62)
                val ivory = Color(0xFFF5EDDA)
                val ink = Color(0xFF173E36)
                val halo = if (dark) Color(0xFFCCDCD2) else accent
                drawCircle(Brush.radialGradient(listOf(halo.copy(alpha = .12f), halo.copy(alpha = 0f)),
                    Offset(160f, 150f), 160f), 160f, Offset(160f, 150f))
                drawCircle(halo.copy(alpha = .12f), 118f, Offset(160f, 150f), style = Stroke(1f))
                drawCircle(halo.copy(alpha = .07f), 142f, Offset(160f, 150f), style = Stroke(1f))
                listOf(Offset(45f, 80f), Offset(280f, 130f), Offset(73f, 245f), Offset(258f, 40f)).forEachIndexed { i, p ->
                    rotate(i * 22f, p) {
                        drawPath(Path().apply { moveTo(p.x, p.y - 5); lineTo(p.x + 3, p.y); lineTo(p.x, p.y + 5); lineTo(p.x - 3, p.y); close() }, gold.copy(alpha = .7f))
                    }
                }
                drawOval(ink.copy(alpha = .10f), Offset(47f, 260f), Size(240f, 23f))
                when (scene) {
                    IhsanScene.WELCOME, IhsanScene.PRAYER, IhsanScene.AQSA -> {
                        // A stylized Dome of the Rock, distinct from the Al-Qibli prayer hall.
                        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFB4C9BB), Color(0xFF6F9B87)), 242f, 274f),
                            Offset(54f, 242f), Size(218f, 28f), CornerRadius(15f))
                        drawRect(Color(0xFFDAD4BE), Offset(82f, 182f), Size(160f, 63f))
                        drawRect(Color(0xFF527F81), Offset(82f, 179f), Size(160f, 17f))
                        drawRect(Color(0xFF98B3AD), Offset(102f, 152f), Size(120f, 28f))
                        repeat(7) { i ->
                            drawLine(gold.copy(alpha = .7f), Offset(87f + i * 23, 183f), Offset(97f + i * 23, 191f), 2f)
                            drawRoundRect(Color(0xFF365D59), Offset(89f + i * 22, 211f), Size(12f, 28f), CornerRadius(6f))
                            drawRoundRect(ivory.copy(alpha = .65f), Offset(92f + i * 22, 214f), Size(3f, 22f), CornerRadius(2f))
                        }
                        val dome = Path().apply {
                            moveTo(101f, 153f); cubicTo(104f, 119f, 128f, 94f, 160f, 90f)
                            cubicTo(194f, 95f, 219f, 120f, 222f, 153f); close()
                        }
                        drawPath(dome, Brush.linearGradient(listOf(Color(0xFFF7E1A0), gold, Color(0xFFAA7B32)), Offset(111f, 102f), Offset(220f, 160f)))
                        drawPath(Path().apply { moveTo(160f, 91f); cubicTo(146f, 106f, 138f, 128f, 136f, 153f) },
                            ivory.copy(alpha = .5f), style = Stroke(2f))
                        drawLine(gold, Offset(160f, 91f), Offset(160f, 70f), 2.5f)
                        drawArc(gold, 20f, 300f, false, Offset(157f, 59f), Size(8f, 10f), style = Stroke(2f))
                        drawRect(Color(0xFFE7D6A8), Offset(97f, 151f), Size(129f, 5f))
                        // Quiet side minaret and cypress framing the architectural silhouette.
                        drawRoundRect(ivory, Offset(43f, 151f), Size(15f, 93f), CornerRadius(3f))
                        drawRect(Color(0xFF759787), Offset(40f, 163f), Size(21f, 5f))
                        drawPath(Path().apply { moveTo(41f, 151f); quadraticTo(50f, 129f, 60f, 151f); close() }, gold)
                        drawLine(gold, Offset(50f, 131f), Offset(50f, 120f), 2f)
                        drawOval(Color(0xFF477662), Offset(257f, 169f), Size(24f, 78f))
                        drawLine(ink, Offset(269f, 228f), Offset(269f, 256f), 3f)
                        if (scene == IhsanScene.PRAYER) {
                            drawCircle(ivory, 30f, Offset(252f, 85f))
                            drawCircle(ink.copy(alpha = .14f), 26f, Offset(252f, 85f), style = Stroke(1f))
                            drawLine(ink, Offset(252f, 85f), Offset(252f, 66f), 3f, StrokeCap.Round)
                            drawLine(ink, Offset(252f, 85f), Offset(265f, 92f), 3f, StrokeCap.Round)
                        }
                    }
                    IhsanScene.QURAN -> {
                        rotate(-9f, Offset(160f, 190f)) {
                            val left = Path().apply { moveTo(60f, 120f); quadraticTo(116f, 100f, 160f, 133f); lineTo(160f, 247f); quadraticTo(110f, 222f, 60f, 240f); close() }
                            val right = Path().apply { moveTo(160f, 133f); quadraticTo(210f, 100f, 260f, 120f); lineTo(260f, 240f); quadraticTo(210f, 222f, 160f, 247f); close() }
                            drawPath(left, ink, style = Stroke(12f, join = StrokeJoin.Round))
                            drawPath(right, ink, style = Stroke(12f, join = StrokeJoin.Round))
                            drawPath(left, Brush.horizontalGradient(listOf(ivory, Color(0xFFE4DCC8))))
                            drawPath(right, Brush.horizontalGradient(listOf(Color(0xFFE8DFCB), Color(0xFFFFFBF0))))
                            repeat(6) { i ->
                                val y = 150f + i * 11
                                drawLine(gold.copy(alpha = .55f), Offset(82f, y - 5), Offset(140f, y + 4), 2f, StrokeCap.Round)
                                drawLine(gold.copy(alpha = .55f), Offset(182f, y + 4), Offset(241f, y - 5), 2f, StrokeCap.Round)
                            }
                            drawLine(gold, Offset(210f, 112f), Offset(210f, 205f), 5f)
                            drawPath(Path().apply { moveTo(205f, 204f); lineTo(215f, 204f); lineTo(215f, 218f); lineTo(210f, 213f); lineTo(205f, 218f); close() }, gold)
                        }
                        repeat(12) { i ->
                            val a = i * .33f
                            drawCircle(gold, 5f, Offset(226f + kotlin.math.cos(a) * 36f, 235f + kotlin.math.sin(a) * 28f))
                        }
                    }
                    IhsanScene.JOURNEY -> {
                        listOf(Offset(72f, 80f), Offset(58f, 143f), Offset(78f, 206f)).forEachIndexed { i, p ->
                            drawRoundRect(ink.copy(alpha = .10f), Offset(p.x + 4, p.y + 7), Size(190f, 50f), CornerRadius(16f))
                            drawRoundRect(if (dark) Color(0xFF303A36) else Color.White, p, Size(190f, 50f), CornerRadius(16f))
                            drawCircle(if (i < 2) accent else gold.copy(alpha = .3f), 14f, Offset(p.x + 160f, p.y + 25f))
                            if (i < 2) drawPath(Path().apply { moveTo(p.x + 153, p.y + 25); lineTo(p.x + 158, p.y + 30); lineTo(p.x + 167, p.y + 19) },
                                Color.White, style = Stroke(2.5f, cap = StrokeCap.Round))
                            drawRoundRect(halo.copy(alpha = .25f), Offset(p.x + 18, p.y + 15), Size(93f, 5f), CornerRadius(3f))
                            drawRoundRect(halo.copy(alpha = .10f), Offset(p.x + 18, p.y + 30), Size(65f, 4f), CornerRadius(3f))
                        }
                        drawCircle(gold, 22f, Offset(76f, 68f))
                        drawPath(Path().apply { moveTo(66f, 67f); cubicTo(66f, 57f, 76f, 58f, 76f, 64f); cubicTo(80f, 57f, 90f, 59f, 86f, 68f); lineTo(76f, 78f); close() }, ivory)
                    }
                }
            }
        }
    }
}
