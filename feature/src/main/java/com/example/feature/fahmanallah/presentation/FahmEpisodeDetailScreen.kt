package com.example.feature.fahmanallah.presentation

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.fahmanallah.presentation.components.FahmPracticalActionCard
import com.example.feature.fahmanallah.presentation.components.FahmRuleCard
import com.example.feature.fahmanallah.presentation.components.FahmTakeawaysSection
import com.example.feature.fahmanallah.presentation.components.FahmUnderstandSection
import com.example.feature.fahmanallah.presentation.components.FahmVideoPlayer
import java.util.Locale

@Composable
fun FahmEpisodeDetailScreen(
    episodeId: String,
    viewModel: FahmViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCompletion: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(episodeId) {
        viewModel.loadEpisodeDetail(episodeId)
    }

    if (state.isLoading || state.episode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IhsanLoadingState()
        }
        return
    }

    val ep = state.episode!!
    val formattedNumber = String.format(Locale.getDefault(), "%02d", ep.number)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = IhsanTheme.colors.textPrimary
                )
            }

            Text(
                text = "الدرس $formattedNumber",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )

            IconButton(
                onClick = { viewModel.toggleFavorite(ep.id) }
            ) {
                Icon(
                    imageVector = if (ep.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "حفظ",
                    tint = if (ep.isFavorite) primary else IhsanTheme.colors.textSecondaryMuted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 16:9 Video Player
            FahmVideoPlayer(
                videoId = ep.videoId,
                coverUrl = ep.coverUrl,
                isOnline = state.isOnline,
                isEmbeddable = ep.isEmbeddable,
                onOpenExternalYouTube = {
                    ep.videoId?.let { vId ->
                        val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/watch?v=$vId".toUri())
                        context.startActivity(intent)
                    }
                }
            )

            // Episode Title & Subtitle
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "الدرس $formattedNumber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = ep.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = IhsanTheme.colors.textPrimary
                )

                if (!ep.subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ep.subtitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }

            // Quick Actions Bar (Bookmark, Watch Later, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmark Action
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.toggleFavorite(ep.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ep.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (ep.isFavorite) "محفوظ" else "حفظ",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }

                // Watch Later Action
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.toggleWatchLater(ep.id) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WatchLater,
                            contentDescription = "لاحقًا",
                            tint = if (ep.isWatchLater) primary else IhsanTheme.colors.textSecondaryMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "لاحقًا",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }

                // Share Action
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "شاهد درس «${ep.title}» من برنامج الفهم عن الله د. عمرو خالد في تطبيق إحسان.")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الدرس"))
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مشاركة",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }

            // "قاعدة اليوم" Card (Only rendered if ruleText is present)
            if (!ep.ruleText.isNullOrBlank()) {
                FahmRuleCard(
                    ruleText = ep.ruleText,
                    ruleTitle = ep.ruleTitle ?: "قاعدة اليوم"
                )
            }

            // "ماذا أفهم عن الله؟" Summary
            if (!ep.understandText.isNullOrBlank()) {
                FahmUnderstandSection(understandText = ep.understandText)
            }

            // "ماذا أتعلم؟" Takeaways
            if (ep.takeaways.isNotEmpty()) {
                FahmTakeawaysSection(takeaways = ep.takeaways)
            }

            // "طبّقها اليوم" Practical Action
            if (!ep.practicalAction.isNullOrBlank()) {
                FahmPracticalActionCard(actionText = ep.practicalAction)
            }

            // Source Attribution Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "المصدر",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "الفهم عن الله — الجزء الأول (رمضان 2023)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textPrimary
                    )

                    Text(
                        text = "د. عمرو خالد • قناة YouTube الرسمية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, "https://www.youtube.com/@AmrKhaled".toUri())
                                context.startActivity(intent)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "زيارة القناة على YouTube",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                }
            }

            // Mark Completed CTA Button
            Button(
                onClick = {
                    viewModel.markCompleted(ep.id)
                    onNavigateToCompletion(ep.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (ep.progressState == FahmEpisodeProgressState.COMPLETED) "أتممت هذا الدرس (مكتمل)" else "أتممت هذا الدرس",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Previous / Next Episode Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.previousEpisodeId != null) {
                    OutlinedButton(
                        onClick = { onNavigateToDetail(state.previousEpisodeId!!) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "الدرس السابق", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (state.nextEpisodeId != null) {
                    OutlinedButton(
                        onClick = { onNavigateToDetail(state.nextEpisodeId!!) },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = "الدرس التالي", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
