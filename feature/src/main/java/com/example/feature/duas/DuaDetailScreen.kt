package com.example.feature.duas

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.duas.domain.model.Dua

@Composable
fun DuaDetailScreen(
    dua: Dua?,
    onBack: () -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = IhsanTheme.colors.surfaceBase,
        ) { padding ->
            if (dua == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = IhsanTheme.colors.accentContent)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    // 1. Top Mosque Hero Image Header
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_mosque_sunrise_landscape),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        contentScale = ContentScale.Crop,
                    )

                    // 2. Main Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Spacer matching header height minus overlap
                        Spacer(modifier = Modifier.height(148.dp))

                        // Overlapping Curved Container
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(
                                topStart = 44.dp,
                                topEnd = 44.dp,
                                bottomStart = 28.dp,
                                bottomEnd = 28.dp,
                            ),
                            color = IhsanTheme.colors.surfaceElevated,
                            border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
                            shadowElevation = 2.dp,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Dua Title
                                Text(
                                    text = dua.title,
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IhsanTheme.colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dua Main Verse / Text
                                Text(
                                    text = dua.text,
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IhsanTheme.colors.accentContent,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 40.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Category / Source Pill Badge
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = IhsanTheme.colors.surfaceMuted,
                                    border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = IhsanTheme.colors.accentContent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dua.category.ifBlank { "من السنة النبوية" },
                                            color = IhsanTheme.colors.accentContent,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 3 Interactive Action Cards (Listen, Copy, Likes)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Action 1: Listen (استماع)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated),
                                        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = false) {}
                                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(IhsanTheme.colors.brand),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                      contentDescription = "الصوت غير متاح؛ لم يعتمد تسجيل لهذا الدعاء",
                                                    tint = IhsanTheme.colors.onBrand,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "الصوت غير متاح",
                                                color = IhsanTheme.colors.accentContent,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Action 2: Copy (نسخ)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated),
                                        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    clipboardManager.setText(AnnotatedString(dua.text))
                                                    Toast.makeText(context, "تم نسخ الدعاء", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(IhsanTheme.colors.surfaceMuted),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "نسخ",
                                                    tint = IhsanTheme.colors.accentContent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "نسخ",
                                                color = IhsanTheme.colors.accentContent,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Action 3: Favorite / Likes Count
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceElevated),
                                        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onToggleFavorite(dua.id, dua.isFavorite)
                                                }
                                                .padding(vertical = 10.dp, horizontal = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(IhsanTheme.colors.surfaceMuted),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (dua.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                                    contentDescription = "حفظ الدعاء في المفضلة",
                                                    tint = IhsanTheme.colors.favorite,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (dua.isFavorite) "محفوظ" else "حفظ",
                                                color = IhsanTheme.colors.favorite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Lower Content Sections ("شرح الدعاء" & "متى يقال؟")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            // Section 1: "شرح الدعاء"
                            Text(
                                text = "شرح الدعاء",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = getDuaExplanation(dua),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = IhsanTheme.colors.textSecondary,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Start
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Section 2: "متى يقال؟"
                            Text(
                                text = "متى يقال؟",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = getDuaWhenToSay(dua),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = IhsanTheme.colors.textSecondary,
                                    lineHeight = 24.sp,
                                    textAlign = TextAlign.Start
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // 4. Overlay Top Floating Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button (Top Left)
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = IhsanTheme.colors.onBrand.copy(alpha = 0.92f),
                            shadowElevation = 3.dp
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = IhsanTheme.colors.accentContent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Right Action Buttons (Share & Bookmark)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = IhsanTheme.colors.onBrand.copy(alpha = 0.92f),
                                shadowElevation = 3.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\n${dua.text}\n\nالمصدر: ${dua.source}")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "مشاركة",
                                        tint = IhsanTheme.colors.accentContent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = IhsanTheme.colors.onBrand.copy(alpha = 0.92f),
                                shadowElevation = 3.dp
                            ) {
                                IconButton(
                                    onClick = {
                                        onToggleFavorite(dua.id, dua.isFavorite)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (dua.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "المفضلة",
                                        tint = IhsanTheme.colors.accentContent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getDuaExplanation(dua: Dua): String {
    if (dua.explanation.isNotBlank()) return dua.explanation
    return when {
        dua.title.contains("قضاء الحاجة") || dua.text.contains("سهلاً") ->
            "هذا دعاء عظيم يتضمن التوكل على الله وطلب التيسير في الأمور كافة، وهو من الأدعية المأثورة عن النبي ﷺ."
        dua.title.contains("استخارة") || dua.text.contains("أستخيرك") ->
            "دعاء يسن للعبد أن يدعو به إذا همّ بأمر من الأمور المباحة، ليفوض أمره إلى الله تعالى ويطلب منه التوفيق والاختيار الأفضل."
        dua.title.contains("الهم") || dua.text.contains("الهم") ->
            "من أعظم الأدعية النبوية في تفريج الكرب وإزالة الهم والغم والجلب للطمأنينة وانشراح الصدر."
        dua.title.contains("سفر") || dua.text.contains("سخر") ->
            "دعاء يتضمن طلب السلامة من الله والاستعاذة به من وعثاء السفر وكآبة المنظر وسوء المنقلب."
        dua.title.contains("رزق") || dua.text.contains("رزقاً") ->
            "دعاء مبارك يطلب فيه المسلم من الله تعالى العلم النافع والرزق الطيب والعمل الصالح المتقبل."
        else ->
            "هذا دعاء مبارك يتضمن التوسل إلى الله تعالى والتضرع إليه برحمته وفضله الواسع، وهو من الأدعية المأثورة في السنة النبوية الشريفة."
    }
}

private fun getDuaWhenToSay(dua: Dua): String {
    if (dua.whenToSay.isNotBlank()) return dua.whenToSay
    return when {
        dua.title.contains("قضاء الحاجة") || dua.text.contains("سهلاً") ->
            "يقال عند الحاجة إلى تيسير أمر مهم في الدنيا سواء في العمل أو الدراسة أو أي أمر آخر."
        dua.title.contains("استخارة") || dua.text.contains("أستخيرك") ->
            "يقال بعد صلاة ركعتين من غير الفريضة عندما يحتار المسلم في الاختيار بين أمرين مباحين."
        dua.title.contains("الهم") || dua.text.contains("الهم") ->
            "يقال عند الشعور بالضيق أو الحزن أو تراكم الديون والمتاعب النفسية."
        dua.title.contains("سفر") || dua.text.contains("سخر") ->
            "يقال عند استواء المسافر على دابته أو وسيلة سفره قبل الانطلاق."
        dua.title.contains("رزق") || dua.text.contains("رزقاً") ->
            "يقال بعد صلاة الفجر وفي أوقات إجابة الدعاء لطلب البركة والرزق الحلال."
        else ->
            "يقال في أوقات الاستجابة وعند الحاجة والتضرع إلى الله تعالى في السراء والضراء."
    }
}
