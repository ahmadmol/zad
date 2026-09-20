package com.example.feature.nabiihsan.presentation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.nabiihsan.presentation.components.NabiIhsanVideoPlayer
import com.example.feature.nabiihsan.presentation.components.NabiLessonItem
import com.example.feature.nabiihsan.presentation.components.NabiRecipeCard
import com.example.feature.nabiihsan.presentation.components.NabiSectionHeader
import com.example.feature.nabiihsan.presentation.components.NabiSourceCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanEpisodeDetailScreen(
    episodeId: String,
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onOpenEpisode: (String) -> Unit = {}
) {
    val uiState by viewModel.episodeDetailState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isAboutExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(episodeId) {
        viewModel.loadEpisodeDetail(episodeId)
    }

    val episode = uiState.episode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = episode?.title ?: "تفاصيل الحلقة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = {
                    if (episode != null) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${episode.title}\n${episode.subtitle}\nسلسلة نبي الإحسان")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الحلقة"))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "مشاركة",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (uiState.isLoading) {
            IhsanLoadingState(modifier = Modifier.fillMaxSize())
        } else if (episode == null || uiState.errorMessage != null) {
            IhsanErrorState(
                title = "حدث خطأ",
                message = uiState.errorMessage ?: "تعذر تحميل تفاصيل الحلقة",
                retryLabel = "إعادة المحاولة",
                onRetry = { viewModel.loadEpisodeDetail(episodeId) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Video Player Container (16:9)
                NabiIhsanVideoPlayer(
                    videoId = episode.youtubeVideoId,
                    coverUrl = episode.coverUrl,
                    isOnline = isOnline,
                    isEmbeddable = episode.isEmbeddable,
                    onOpenExternalYouTube = {
                        episode.youtubeVideoId?.takeIf { it.isNotBlank() }?.let { videoId ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )

                // Title & Subtitle Block
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (episode.episodeNumber != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "الحلقة ${episode.episodeNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = episode.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = episode.subtitle,
                        fontSize = 13.sp,
                        color = IhsanTheme.colors.textSecondary
                    )
                }

                // Action Row (حفظ / مشاركة / لاحقاً)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ActionButtonItem(
                        icon = if (episode.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "حفظ",
                        isHighlight = episode.isFavorite,
                        onClick = { viewModel.toggleEpisodeFavorite(episode.id) }
                    )
                    ActionButtonItem(
                        icon = Icons.Default.Share,
                        label = "مشاركة",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${episode.title}\n${episode.subtitle}\nنبي الإحسان")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة"))
                        }
                    )
                    ActionButtonItem(
                        icon = if (episode.isWatchLater) Icons.Default.WatchLater else Icons.Outlined.WatchLater,
                        label = "لاحقاً",
                        isHighlight = episode.isWatchLater,
                        onClick = { viewModel.toggleEpisodeWatchLater(episode.id) }
                    )
                }

                // About Episode
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "عن الحلقة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = episode.aboutText,
                            fontSize = 13.sp,
                            color = IhsanTheme.colors.textSecondary,
                            maxLines = if (isAboutExpanded) Int.MAX_VALUE else 3,
                            lineHeight = 18.sp
                        )
                        if (episode.aboutText.length > 100) {
                            TextButton(
                                onClick = { isAboutExpanded = !isAboutExpanded },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = if (isAboutExpanded) "عرض أقل" else "عرض المزيد",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // From Seerah Section
                if (episode.seerahContext.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "من السيرة")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = episode.seerahContext,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp
                        )
                    }
                }

                // What We Learn Section
                if (episode.lessons.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "ماذا نتعلم؟")
                        Spacer(modifier = Modifier.height(6.dp))
                        episode.lessons.forEach { lesson ->
                            NabiLessonItem(text = lesson)
                        }
                    }
                }

                // Linked Prophetic Recipe Card
                if (uiState.linkedRecipes.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "وصفة الإحسان في هذه الحلقة")
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.linkedRecipes.forEach { recipe ->
                            NabiRecipeCard(
                                recipe = recipe,
                                onRecipeClick = onRecipeClick
                            )
                        }
                    }
                }

                // Practical Reflection
                if (episode.practicalReflection.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "كيف أطبقها في حياتي؟")
                        Spacer(modifier = Modifier.height(6.dp))
                        NabiLessonItem(text = episode.practicalReflection)
                    }
                }

                // Source Attribution Card
                NabiSourceCard(
                    sourceTitle = episode.source,
                    authorName = "محتوى إحسان التحريري"
                )

                // Navigation Row: Previous / Next Episode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (uiState.previousEpisodeId != null) {
                        Button(
                            onClick = { onOpenEpisode(uiState.previousEpisodeId!!) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الحلقة السابقة", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (uiState.nextEpisodeId != null) {
                        Button(
                            onClick = { onOpenEpisode(uiState.nextEpisodeId!!) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("الحلقة التالية", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ActionButtonItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isHighlight: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isHighlight) MaterialTheme.colorScheme.primary else IhsanTheme.colors.textSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else IhsanTheme.colors.textSecondary,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
    }
}
