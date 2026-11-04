package com.example.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.example.designsystem.theme.IhsanTheme
import kotlin.math.cos
import kotlin.math.sin

enum class IhsanScene {
    WELCOME,
    PRAYER,
    QURAN,
    JOURNEY,
    AQSA,
    MADINAH_NABAWI,
    MAKKAH_KAABA
}

/**
 * Original architectural and spiritual vector artwork drawn for Ihsan.
 * Directly inspired by the Three Holy Sanctuaries:
 * 1. Al-Aqsa Mosque & Dome of the Rock (Jerusalem)
 * 2. The Prophet's Mosque (Medina)
 * 3. The Holy Kaaba & Grand Mosque (Mecca)
 */
@Composable
fun IhsanIllustration(scene: IhsanScene, modifier: Modifier = Modifier) {
    val accent = IhsanTheme.colors.accentContent
    val dark = IhsanTheme.isDark
    Canvas(modifier) {
        val unit = size.minDimension / 320f
        translate((size.width - 320 * unit) / 2, (size.height - 320 * unit) / 2) {
            scale(unit, unit, Offset.Zero) {
                val gold = Color(0xFFD1AE62)
                val goldLight = Color(0xFFF7E1A0)
                val goldDark = Color(0xFFAA7B32)
                val ivory = Color(0xFFF5EDDA)
                val ink = Color(0xFF173E36)
                val halo = if (dark) Color(0xFFCCDCD2) else accent

                // Common spiritual ambient background glow
                drawCircle(
                    Brush.radialGradient(
                        listOf(halo.copy(alpha = .14f), halo.copy(alpha = 0f)),
                        Offset(160f, 150f), 160f
                    ),
                    160f, Offset(160f, 150f)
                )
                drawCircle(halo.copy(alpha = .10f), 125f, Offset(160f, 150f), style = Stroke(1f))
                drawCircle(halo.copy(alpha = .06f), 146f, Offset(160f, 150f), style = Stroke(1f))

                // Corner decorative stars
                listOf(Offset(45f, 65f), Offset(275f, 65f), Offset(45f, 255f), Offset(275f, 255f)).forEachIndexed { i, p ->
                    rotate(i * 22f, p) {
                        drawPath(
                            Path().apply {
                                moveTo(p.x, p.y - 5)
                                lineTo(p.x + 3, p.y)
                                lineTo(p.x, p.y + 5)
                                lineTo(p.x - 3, p.y)
                                close()
                            },
                            gold.copy(alpha = .6f)
                        )
                    }
                }

                when (scene) {
                    // ==========================================
                    // 1. AL-AQSA MOSQUE & DOME OF THE ROCK (JERUSALEM)
                    // Inspired directly by Image 1
                    // ==========================================
                    IhsanScene.WELCOME, IhsanScene.PRAYER, IhsanScene.AQSA -> {
                        // Sunrise sky glow
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color(0x33FFB74D), Color(0x00FFB74D)),
                                Offset(160f, 130f), 110f
                            ),
                            110f, Offset(160f, 130f)
                        )

                        // 1. Framing Ornate Jerusalem Stone Arch
                        val archPath = Path().apply {
                            moveTo(34f, 260f)
                            lineTo(34f, 130f)
                            cubicTo(34f, 60f, 95f, 26f, 160f, 24f)
                            cubicTo(225f, 26f, 286f, 60f, 286f, 130f)
                            lineTo(286f, 260f)
                        }
                        drawPath(archPath, gold.copy(alpha = .35f), style = Stroke(4f))
                        drawPath(archPath, Color(0xFFC7A35C).copy(alpha = .7f), style = Stroke(1.5f))

                        // Arch Spandrels Arabesque Accent
                        drawPath(
                            Path().apply {
                                moveTo(34f, 130f); lineTo(34f, 26f); lineTo(130f, 26f)
                                cubicTo(80f, 32f, 40f, 75f, 34f, 130f); close()
                            },
                            Color(0x221B4D3E)
                        )
                        drawPath(
                            Path().apply {
                                moveTo(286f, 130f); lineTo(286f, 26f); lineTo(190f, 26f)
                                cubicTo(240f, 32f, 280f, 75f, 286f, 130f); close()
                            },
                            Color(0x221B4D3E)
                        )

                        // 2. Hanging Golden Heritage Lantern (الفانوس الذهبي المعلق)
                        drawLine(gold, Offset(160f, 24f), Offset(160f, 52f), strokeWidth = 1.5f)
                        drawCircle(Brush.radialGradient(listOf(Color(0x66FFD54F), Color(0x00FFD54F)), Offset(160f, 64f), 24f), 24f, Offset(160f, 64f))
                        drawPath(
                            Path().apply {
                                moveTo(156f, 52f); lineTo(164f, 52f); lineTo(166f, 62f)
                                lineTo(160f, 74f); lineTo(154f, 62f); close()
                            },
                            Brush.verticalGradient(listOf(goldLight, gold, goldDark), 52f, 74f)
                        )
                        drawCircle(Color(0xFFFFF9C4), 3.5f, Offset(160f, 62f))

                        // 3. Sacred Olive Branch along the left arch (غصن الزيتون الأخضر)
                        val oliveStem = Path().apply {
                            moveTo(38f, 135f)
                            cubicTo(40f, 95f, 65f, 58f, 105f, 38f)
                        }
                        drawPath(oliveStem, Color(0xFF4A6B53), style = Stroke(1.8f, cap = StrokeCap.Round))
                        listOf(
                            Offset(44f, 115f) to 25f,
                            Offset(52f, 92f) to 40f,
                            Offset(66f, 72f) to 55f,
                            Offset(84f, 54f) to 65f,
                            Offset(102f, 42f) to 75f
                        ).forEach { (pt, angle) ->
                            rotate(angle, pt) {
                                drawOval(Color(0xFF386641), pt - Offset(3f, 7f), Size(6f, 14f))
                                drawOval(Color(0xFF6A994E), pt - Offset(1.5f, 5f), Size(3f, 10f))
                            }
                        }

                        // 4. Dome of the Rock Base & Drums
                        // Courtyard base platform
                        drawRoundRect(
                            Brush.verticalGradient(listOf(Color(0xFFE8E0D0), Color(0xFFC7BBA2)), 238f, 264f),
                            Offset(50f, 238f), Size(220f, 26f), CornerRadius(10f)
                        )
                        // Lower Jerusalem white marble facade
                        drawRect(Color(0xFFEDE6D8), Offset(76f, 192f), Size(168f, 48f))
                        // Upper Persian turquoise/blue mosaic band
                        drawRect(
                            Brush.horizontalGradient(listOf(Color(0xFF0F4C5C), Color(0xFF1B6B78), Color(0xFF0F4C5C))),
                            Offset(76f, 172f), Size(168f, 22f)
                        )
                        // Turquoise tholobate / cylinder drum
                        drawRect(Color(0xFF135A52), Offset(100f, 142f), Size(120f, 31f))

                        // 5 Arched Windows on Drum with golden light
                        repeat(5) { i ->
                            val wx = 108f + i * 22f
                            drawRoundRect(Color(0xFFFFF176), Offset(wx, 147f), Size(12f, 22f), CornerRadius(6f))
                            drawRoundRect(Color(0xFF0E3832), Offset(wx + 2f, 149f), Size(8f, 18f), CornerRadius(4f))
                            drawRoundRect(Color(0xFFFFF9C4), Offset(wx + 3.5f, 151f), Size(5f, 14f), CornerRadius(2.5f))
                        }

                        // Octagonal angled sides
                        drawPath(
                            Path().apply {
                                moveTo(56f, 240f); lineTo(76f, 172f); lineTo(76f, 240f); close()
                            },
                            Color(0xFF0B3A33)
                        )
                        drawPath(
                            Path().apply {
                                moveTo(264f, 240f); lineTo(244f, 172f); lineTo(244f, 240f); close()
                            },
                            Color(0xFF09312B)
                        )

                        // 5. The Golden Dome (القبة الذهبية المشرفة)
                        val dome = Path().apply {
                            moveTo(98f, 144f)
                            cubicTo(101f, 108f, 126f, 82f, 160f, 78f)
                            cubicTo(194f, 82f, 219f, 108f, 222f, 144f)
                            close()
                        }
                        drawPath(
                            dome,
                            Brush.linearGradient(
                                listOf(Color(0xFFFFF9C4), goldLight, gold, goldDark),
                                Offset(110f, 82f), Offset(220f, 144f)
                            )
                        )

                        // Meridian Rib Lines
                        drawPath(Path().apply { moveTo(160f, 78f); lineTo(160f, 144f) }, Color(0x66FFFFFF), style = Stroke(1.5f))
                        drawPath(Path().apply { moveTo(160f, 78f); cubicTo(144f, 96f, 134f, 118f, 130f, 144f) }, Color(0x44FFFFFF), style = Stroke(1.2f))
                        drawPath(Path().apply { moveTo(160f, 78f); cubicTo(176f, 96f, 186f, 118f, 190f, 144f) }, Color(0x334E3400), style = Stroke(1.2f))

                        // Crescent & Spire atop the dome
                        drawLine(goldLight, Offset(160f, 78f), Offset(160f, 58f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                        drawArc(goldLight, 45f, 270f, false, Offset(156.5f, 48f), Size(7f, 9f), style = Stroke(1.8f))

                        // Central Entrance Portal
                        drawRoundRect(Color(0xFFD4AF37), Offset(148f, 204f), Size(24f, 36f), CornerRadius(12f))
                        drawRoundRect(Color(0xFF1A2621), Offset(151f, 207f), Size(18f, 33f), CornerRadius(9f))
                        drawRoundRect(goldLight, Offset(157f, 218f), Size(6f, 22f), CornerRadius(3f))

                        // 6. Inlaid 8-Pointed Geometric Marble Floor Star (النجمة الثمانية الإسلامية)
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 24f, color = Color(0xFFC7A35C))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 18f, color = Color(0xFF1B4D3E))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 10f, color = Color(0xFFFFF8E7))

                        if (scene == IhsanScene.PRAYER) {
                            drawCircle(ivory, 26f, Offset(248f, 75f))
                            drawCircle(ink.copy(alpha = .14f), 22f, Offset(248f, 75f), style = Stroke(1f))
                            drawLine(ink, Offset(248f, 75f), Offset(248f, 58f), 2.5f, StrokeCap.Round)
                            drawLine(ink, Offset(248f, 75f), Offset(259f, 81f), 2.5f, StrokeCap.Round)
                        }
                    }

                    // ==========================================
                    // 2. THE PROPHET'S MOSQUE (MEDINA)
                    // Inspired directly by Image 2
                    // ==========================================
                    IhsanScene.MADINAH_NABAWI -> {
                        // Sunset pastel rosy glow behind minarets and dome
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color(0x38FFA726), Color(0x22F48FB1), Color(0x00000000)),
                                Offset(160f, 130f), 130f
                            ),
                            130f, Offset(160f, 130f)
                        )

                        // 1. Striped Marble Arch Framing
                        val archP = Path().apply {
                            moveTo(34f, 260f)
                            lineTo(34f, 120f)
                            cubicTo(34f, 55f, 95f, 26f, 160f, 24f)
                            cubicTo(225f, 26f, 286f, 55f, 286f, 120f)
                            lineTo(286f, 260f)
                        }
                        drawPath(archP, Color(0xFFD7CCC8), style = Stroke(5f))
                        drawPath(archP, Color(0xFF8D6E63), style = Stroke(2f))

                        // Hanging golden lantern from apex
                        drawLine(gold, Offset(160f, 24f), Offset(160f, 52f), strokeWidth = 1.5f)
                        drawCircle(Brush.radialGradient(listOf(Color(0x66FFD54F), Color(0x00FFD54F)), Offset(160f, 62f), 22f), 22f, Offset(160f, 62f))
                        drawPath(
                            Path().apply {
                                moveTo(156f, 52f); lineTo(164f, 52f); lineTo(166f, 62f); lineTo(160f, 72f); lineTo(154f, 62f); close()
                            },
                            Brush.verticalGradient(listOf(goldLight, gold, goldDark), 52f, 72f)
                        )

                        // 2. Grand Illuminated Minaret of the Prophet's Mosque (المئذنة الشريفة)
                        val minaretX = 242f
                        drawRect(Color(0xFFFAF7F0), Offset(minaretX, 70f), Size(18f, 170f))
                        // Green illuminated balconies
                        drawRect(Color(0xFF2E7D32), Offset(minaretX - 3f, 85f), Size(24f, 8f))
                        drawCircle(Color(0xFF81C784), 4f, Offset(minaretX + 9f, 89f))
                        drawRect(Color(0xFF2E7D32), Offset(minaretX - 2f, 130f), Size(22f, 7f))
                        // Minaret Spire & Crescent
                        drawPath(
                            Path().apply {
                                moveTo(minaretX, 70f); lineTo(minaretX + 9f, 40f); lineTo(minaretX + 18f, 70f); close()
                            },
                            goldLight
                        )
                        drawLine(goldLight, Offset(minaretX + 9f, 40f), Offset(minaretX + 9f, 30f), strokeWidth = 1.8f)
                        drawArc(goldLight, 45f, 270f, false, Offset(minaretX + 6.5f, 23f), Size(5f, 7f), style = Stroke(1.5f))

                        // 3. Smaller Left Minaret
                        val minaretLX = 60f
                        drawRect(Color(0xFFFAF7F0), Offset(minaretLX, 90f), Size(14f, 150f))
                        drawRect(Color(0xFF2E7D32), Offset(minaretLX - 2f, 105f), Size(18f, 6f))
                        drawPath(
                            Path().apply {
                                moveTo(minaretLX, 90f); lineTo(minaretLX + 7f, 65f); lineTo(minaretLX + 14f, 90f); close()
                            },
                            goldLight
                        )

                        // 4. Courtyard Giant Folding Umbrella Canopies (مظلات الحرم النبوي)
                        listOf(Offset(92f, 195f), Offset(224f, 195f)).forEach { u ->
                            // Umbrella stem
                            drawLine(Color(0xFFC7A35C), Offset(u.x, u.y), Offset(u.x, u.y - 35f), strokeWidth = 3f)
                            // Inverted conical folding canopy
                            val canopy = Path().apply {
                                moveTo(u.x, u.y - 12f)
                                lineTo(u.x - 30f, u.y - 42f)
                                lineTo(u.x + 30f, u.y - 42f)
                                close()
                            }
                            drawPath(canopy, Brush.verticalGradient(listOf(Color(0xFFFFFDF7), Color(0xFFEADBCE)), u.y - 42f, u.y - 12f))
                            drawPath(canopy, Color(0xFFC7A35C).copy(alpha = .6f), style = Stroke(1.2f))
                            // Small golden lantern on umbrella pole
                            drawCircle(Color(0xFFFFD54F), 3.5f, Offset(u.x, u.y - 14f))
                        }

                        // 5. White Colonnade Wall of the Prophet's Mosque
                        drawRect(Color(0xFFF7F2E7), Offset(55f, 190f), Size(210f, 50f))
                        repeat(8) { i ->
                            val ax = 68f + i * 23f
                            drawRoundRect(Color(0xFFC7A35C), Offset(ax, 204f), Size(14f, 32f), CornerRadius(7f))
                            drawRoundRect(Color(0xFF2E3D35), Offset(ax + 2f, 206f), Size(10f, 30f), CornerRadius(5f))
                            drawRoundRect(goldLight, Offset(ax + 4f, 214f), Size(6f, 20f), CornerRadius(3f))
                        }

                        // 6. Silver Dome
                        val silverDome = Path().apply {
                            moveTo(98f, 190f)
                            cubicTo(100f, 160f, 115f, 145f, 130f, 145f)
                            cubicTo(145f, 145f, 160f, 160f, 162f, 190f)
                            close()
                        }
                        drawPath(silverDome, Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFCFD8DC), Color(0xFF90A4AE)), Offset(100f, 145f), Offset(160f, 190f)))
                        drawLine(gold, Offset(130f, 145f), Offset(130f, 135f), strokeWidth = 1.5f)

                        // 7. The Majestic Green Dome (القبة الخضراء الشريفة)
                        val greenDome = Path().apply {
                            moveTo(132f, 190f)
                            cubicTo(134f, 142f, 155f, 116f, 178f, 112f)
                            cubicTo(201f, 116f, 222f, 142f, 224f, 190f)
                            close()
                        }
                        drawPath(
                            greenDome,
                            Brush.linearGradient(
                                listOf(Color(0xFF43A047), Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF0F3D17)),
                                Offset(135f, 112f), Offset(224f, 190f)
                            )
                        )
                        // Meridian ribs on Green Dome
                        drawPath(Path().apply { moveTo(178f, 112f); lineTo(178f, 190f) }, Color(0x44FFFFFF), style = Stroke(1.5f))
                        drawPath(Path().apply { moveTo(178f, 112f); cubicTo(165f, 128f, 158f, 155f, 154f, 190f) }, Color(0x33FFFFFF), style = Stroke(1.2f))
                        drawPath(Path().apply { moveTo(178f, 112f); cubicTo(191f, 128f, 198f, 155f, 202f, 190f) }, Color(0x44002607), style = Stroke(1.2f))

                        // Golden Crescent & Spire atop Green Dome
                        drawLine(goldLight, Offset(178f, 112f), Offset(178f, 92f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                        drawArc(goldLight, 45f, 270f, false, Offset(174.5f, 82f), Size(7f, 9f), style = Stroke(1.8f))

                        // 8. Marble Courtyard & 8-Pointed Geometric Floor Star
                        drawRect(Brush.verticalGradient(listOf(Color(0xFFFAF7F0), Color(0xFFE4DDD0)), 240f, 265f), Offset(30f, 240f), Size(260f, 25f))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 24f, color = Color(0xFFC7A35C))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 18f, color = Color(0xFF2E7D32))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 10f, color = Color(0xFFFFF9E6))
                    }

                    // ==========================================
                    // 3. THE HOLY KAABA & MAKKAH (MECCA)
                    // Inspired directly by Image 3
                    // ==========================================
                    IhsanScene.MAKKAH_KAABA -> {
                        // Spiritual dawn golden sky radiance
                        drawCircle(
                            Brush.radialGradient(
                                listOf(Color(0x44FFE082), Color(0x22FFB74D), Color(0x00000000)),
                                Offset(160f, 110f), 130f
                            ),
                            130f, Offset(110f, 110f)
                        )

                        // 1. Ornate White-and-Gold Marble Arch Framing
                        val kaabaArch = Path().apply {
                            moveTo(34f, 260f)
                            lineTo(34f, 120f)
                            cubicTo(34f, 50f, 95f, 24f, 160f, 22f)
                            cubicTo(225f, 24f, 286f, 50f, 286f, 120f)
                            lineTo(286f, 260f)
                        }
                        drawPath(kaabaArch, Color(0xFFE0E0E0), style = Stroke(6f))
                        drawPath(kaabaArch, Color(0xFFC7A35C), style = Stroke(2f))

                        // Dual Hanging Golden Lanterns from arch spandrels
                        listOf(80f, 240f).forEach { lx ->
                            drawLine(gold, Offset(lx, 42f), Offset(lx, 74f), strokeWidth = 1.2f)
                            drawCircle(Brush.radialGradient(listOf(Color(0x66FFD54F), Color(0x00FFD54F)), Offset(lx, 82f), 18f), 18f, Offset(lx, 82f))
                            drawPath(
                                Path().apply {
                                    moveTo(lx - 5f, 74f); lineTo(lx + 5f, 74f); lineTo(lx + 6f, 84f); lineTo(lx, 94f); lineTo(lx - 6f, 84f); close()
                                },
                                Brush.verticalGradient(listOf(goldLight, gold, goldDark), 74f, 94f)
                            )
                        }

                        // 2. Makkah Royal Clock Tower Silhouette (برج الساعة المشرفة) in background
                        val clockX = 160f
                        drawRect(Color(0xFFD4C8B4), Offset(clockX - 16f, 38f), Size(32f, 90f))
                        // Clock spire and golden crescent
                        drawPath(
                            Path().apply {
                                moveTo(clockX - 8f, 38f); lineTo(clockX, 14f); lineTo(clockX + 8f, 38f); close()
                            },
                            Color(0xFFB5A68F)
                        )
                        drawLine(goldLight, Offset(clockX, 14f), Offset(clockX, 8f), strokeWidth = 1.5f)
                        drawArc(goldLight, 45f, 270f, false, Offset(clockX - 3.5f, 3f), Size(7f, 8f), style = Stroke(1.5f))
                        // Illuminated Clock Face
                        drawCircle(Color(0xFFFFF9C4), 8f, Offset(clockX, 52f))
                        drawCircle(Color(0xFF1B3D2F), 6f, Offset(clockX, 52f), style = Stroke(1.2f))

                        // Grand Mosque Minarets in middle ground
                        listOf(86f, 234f).forEach { mx ->
                            drawRect(Color(0xFFF0EBE1), Offset(mx - 6f, 65f), Size(12f, 130f))
                            drawPath(
                                Path().apply {
                                    moveTo(mx - 6f, 65f); lineTo(mx, 46f); lineTo(mx + 6f, 65f); close()
                                },
                                goldLight
                            )
                        }

                        // Grand Mosque Colonnade Arches
                        drawRect(Color(0xFFFAF7F0), Offset(55f, 160f), Size(210f, 40f))
                        repeat(10) { i ->
                            val ax = 62f + i * 20f
                            drawRoundRect(Color(0xFFC7A35C), Offset(ax, 172f), Size(12f, 24f), CornerRadius(6f))
                            drawRoundRect(Color(0xFF263238), Offset(ax + 1.5f, 174f), Size(9f, 22f), CornerRadius(4.5f))
                        }

                        // 3. THE HOLY KAABA (الكعبة المشرفة)
                        val kLeft = 106f
                        val kTop = 132f
                        val kWidth = 108f
                        val kHeight = 105f

                        // Main Kaaba Block with rich black Kiswah
                        drawRect(
                            Brush.linearGradient(
                                listOf(Color(0xFF2C2F33), Color(0xFF1A1C1E), Color(0xFF0F1012)),
                                Offset(kLeft, kTop), Offset(kLeft + kWidth, kTop + kHeight)
                            ),
                            Offset(kLeft, kTop), Size(kWidth, kHeight)
                        )

                        // 4. Golden Embroidered Quranic Kiswah Belt (حزام الكعبة المذهب)
                        val beltTop = kTop + 24f
                        val beltHeight = 16f
                        drawRect(
                            Brush.horizontalGradient(
                                listOf(gold, goldLight, gold, goldDark, goldLight, gold)
                            ),
                            Offset(kLeft, beltTop), Size(kWidth, beltHeight)
                        )
                        // Intricate Calligraphic Texture Lines on Belt
                        repeat(6) { i ->
                            val bx = kLeft + 6f + i * 16f
                            drawRoundRect(Color(0xFF8A6518), Offset(bx, beltTop + 2.5f), Size(12f, beltHeight - 5f), CornerRadius(2f))
                            drawRoundRect(Color(0xFFFFF9C4), Offset(bx + 1.5f, beltTop + 4f), Size(9f, beltHeight - 8f), CornerRadius(1.5f))
                        }

                        // 5. Bab Al-Kaaba (باب الكعبة المشرفة المذهب)
                        val doorLeft = kLeft + 54f
                        val doorTop = kTop + 45f
                        val doorWidth = 32f
                        val doorHeight = 58f
                        drawRoundRect(
                            Brush.verticalGradient(listOf(Color(0xFFFFF59D), gold, goldDark), doorTop, doorTop + doorHeight),
                            Offset(doorLeft, doorTop), Size(doorWidth, doorHeight), CornerRadius(5f)
                        )
                        // Bab Al-Kaaba Ornamental Border & Embroidered Curtain
                        drawRoundRect(Color(0xFF7A5600), Offset(doorLeft + 2.5f, doorTop + 2.5f), Size(doorWidth - 5f, doorHeight - 5f), CornerRadius(4f), style = Stroke(1.5f))
                        drawRoundRect(Color(0xFFFFFDE7), Offset(doorLeft + 6f, doorTop + 6f), Size(doorWidth - 12f, doorHeight - 12f), CornerRadius(2f))
                        // Door divider
                        drawLine(Color(0xFF7A5600), Offset(doorLeft + doorWidth / 2f, doorTop + 6f), Offset(doorLeft + doorWidth / 2f, doorTop + doorHeight - 6f), strokeWidth = 1.2f)

                        // Kaaba Marble Base (الشاذروان الرخامي)
                        drawRect(Color(0xFFE8E0D2), Offset(kLeft - 4f, kTop + kHeight), Size(kWidth + 8f, 6f))

                        // 6. Mataf Marble Floor & 8-Pointed Geometric Floor Star
                        drawRect(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEDE6DA)), 240f, 265f), Offset(30f, 240f), Size(260f, 25f))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 24f, color = Color(0xFFC7A35C))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 18f, color = Color(0xFF0F3025))
                        drawEightPointedStar(this, Offset(160f, 280f), radius = 10f, color = Color(0xFFFFF8E7))
                    }

                    // Existing scenes preserved
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
                            drawCircle(gold, 5f, Offset(226f + cos(a) * 36f, 235f + sin(a) * 28f))
                        }
                    }
                    IhsanScene.JOURNEY -> {
                        listOf(Offset(72f, 80f), Offset(58f, 143f), Offset(78f, 206f)).forEachIndexed { i, p ->
                            drawRoundRect(ink.copy(alpha = .10f), Offset(p.x + 4, p.y + 7), Size(190f, 50f), CornerRadius(16f))
                            drawRoundRect(if (dark) Color(0xFF303A36) else Color.White, p, Size(190f, 50f), CornerRadius(16f))
                            drawCircle(if (i < 2) accent else gold.copy(alpha = .3f), 14f, Offset(p.x + 160f, p.y + 25f))
                            if (i < 2) drawPath(
                                Path().apply { moveTo(p.x + 153, p.y + 25); lineTo(p.x + 158, p.y + 30); lineTo(p.x + 167, p.y + 19) },
                                Color.White, style = Stroke(2.5f, cap = StrokeCap.Round)
                            )
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

/**
 * Draws an authentic 8-pointed Islamic geometric star medallion (النجمة الثمانية الإسلامية),
 * created by intersecting two concentric squares rotated at 45 degrees.
 */
private fun drawEightPointedStar(
    drawScope: DrawScope,
    center: Offset,
    radius: Float,
    color: Color
) {
    val halfSide = radius * 0.7071f
    val rect = Rect(center.x - halfSide, center.y - halfSide, center.x + halfSide, center.y + halfSide)

    val square1 = Path().apply {
        addRect(rect)
    }
    drawScope.drawPath(square1, color)

    drawScope.rotate(45f, center) {
        val square2 = Path().apply {
            addRect(rect)
        }
        drawScope.drawPath(square2, color)
    }
}
