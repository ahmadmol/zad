package com.example.feature.sanhya.presentation

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.sanhya.presentation.components.QuranReferenceCard
import com.example.feature.sanhya.presentation.components.QuranStoryVideoPlayer
import com.example.feature.sanhya.presentation.components.StoryEpisodeCard
import com.example.feature.sanhya.presentation.components.StoryLessonCard
import com.example.feature.sanhya.presentation.components.StorySectionHeader

@Composable
fun SanhyaStoryDetailScreen(
    storyId: String,
    viewModel: SanhyaViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToQuranVerses: (storyId: String) -> Unit,
    onNavigateToEpisodes: (storyId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(storyId) {
        viewModel.loadStoryDetails(storyId)
    }

    val story = detailState.story

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else MaterialTheme.colorScheme.surface)
    ) {
        // Top Bar
        com.example.designsystem.component.IhsanTopBar(
            title = story?.title ?: "تفاصيل القصة", style = com.example.designsystem.component.IhsanTopBarStyle.Minimal,
            onNavigateBack = onNavigateBack ,
            actions = { IconButton(onClick = {
                story?.let { item ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${item.title}\n${item.episodes.firstOrNull()?.sourceUrl.orEmpty()}")
                    }
                    runCatching { context.startActivity(Intent.createChooser(intent, "مشاركة القصة")) }
                }
            }, enabled = story != null) { Icon(Icons.Default.Share, "مشاركة", tint = IhsanTheme.colors.accentContent) } })

        if (story == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (detailState.isLoading) com.example.designsystem.component.IhsanLoadingState(message = "جارٍ تحميل القصة")
                else com.example.designsystem.component.IhsanErrorState("تعذر تحميل القصة",
                    message = detailState.errorMessage ?: "القصة غير موجودة في البيانات الحالية",
                    retryLabel = "إعادة المحاولة", onRetry = { viewModel.loadStoryDetails(storyId) })

            }
            return
        }

        val activeEpisode = story.episodes.find { it.id == detailState.selectedEpisodeId }
            ?: story.episodes.firstOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Video Player Area (16:9)
            QuranStoryVideoPlayer(
                videoId = activeEpisode?.youtubeVideoId,
                coverUrl = activeEpisode?.thumbnailUrl ?: story.coverUrl.takeIf { it.isNotBlank() },
                startSeconds = 0,
                isOnline = isOnline,
                isEmbeddable = activeEpisode?.isEmbeddable == true,
                onOpenExternalYouTube = {
                    activeEpisode?.youtubeVideoId?.takeIf { it.isNotBlank() }?.let { videoId ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                        runCatching { context.startActivity(intent) }
                    }
                }
            )

            // Story Title & Badges Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = story.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = IhsanTheme.colors.accentContent
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = story.subtitle,
                    fontSize = 14.sp,
                    color = IhsanTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (story.formattedSurahLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = story.formattedSurahLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IhsanTheme.colors.accentContent
                            )
                        }
                    }

                    activeEpisode?.durationLabel?.takeIf { it.isNotBlank() }?.let { duration ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = duration,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IhsanTheme.colors.accentContent
                            )
                        }
                    }

                    if (story.totalEpisodesCount > 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${story.totalEpisodesCount} حلقات",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IhsanTheme.colors.accentContent
                            )
                        }
                    }
                }
            }

            // Action Buttons Bar: [حفظ] [مشاركة] [لاحقاً]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.toggleFavorite(story.id) }
                ) {
                    com.example.feature.learning.presentation.components.FavoriteButton(story.isFavorite, { viewModel.toggleFavorite(story.id) })

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "حفظ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Share Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "شاهد قصة \"${story.title}\" - ${story.subtitle} عبر تطبيق إحسان"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة القصة"))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = IhsanTheme.colors.accentContent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مشاركة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Watch Later Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { viewModel.toggleWatchLater(story.id) }
                ) {
                    com.example.feature.learning.presentation.components.WatchLaterButton(story.isInWatchLater, { viewModel.toggleWatchLater(story.id) })

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "لاحقًا",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // "عن القصة" Section (only rendered if non-blank)
            if (story.summary.isNotBlank()) {
                StorySectionHeader(title = "عن القصة")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = story.summary,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // "آيات القصة" Section
            if (story.quranReferences.isNotEmpty()) {
                StorySectionHeader(title = "آيات القصة")
                story.quranReferences.forEach { ref ->
                    QuranReferenceCard(
                        reference = ref,
                        onViewVersesClick = { onNavigateToQuranVerses(story.id) }
                    )
                }
            }

            // "حلقات هذه القصة" Section (if multi-episode)
            if (story.episodes.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StorySectionHeader(
                        title = "حلقات هذه القصة",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "عرض الكل",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.accentContent,
                        modifier = Modifier.clickable { onNavigateToEpisodes(story.id) }
                    )
                }

                story.episodes.take(2).forEach { episode ->
                    StoryEpisodeCard(
                        episode = episode,
                        coverUrl = episode.thumbnailUrl ?: story.coverUrl,
                        isSelected = episode.id == activeEpisode?.id,
                        onClick = { viewModel.selectEpisode(episode.id) }
                    )
                }
            }

            // "ماذا نتعلم؟" Section
            if (story.lessons.isNotEmpty()) {
                StorySectionHeader(title = "ماذا نتعلم؟")
                story.lessons.forEach { lesson ->
                    StoryLessonCard(lesson = lesson)
                }
            }

            // "المصدر" Section
            StorySectionHeader(title = "المصدر")
            com.example.feature.learning.presentation.components.RelatedContentCard(story.sourceTitle, story.sourceAuthor,
                onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse(com.example.feature.sanhya.data.datasource.SanhyaFixtureData.PROGRAM_URL))) } })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
