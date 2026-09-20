package com.example.feature.nabiihsan.presentation

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.nabiihsan.presentation.components.NabiEpisodeCard
import com.example.feature.nabiihsan.presentation.components.NabiLessonItem
import com.example.feature.nabiihsan.presentation.components.NabiSectionHeader
import com.example.feature.nabiihsan.presentation.components.NabiSourceCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanRecipeDetailScreen(
    recipeId: String,
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onEpisodeClick: (String) -> Unit = {}
) {
    val uiState by viewModel.recipeDetailState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(recipeId) {
        viewModel.loadRecipeDetail(recipeId)
    }

    val recipe = uiState.recipe

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
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
                text = "تفاصيل الوصفة",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = {
                    if (recipe != null) {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${recipe.title}\n${recipe.meaning}\nوصفة نبوية - نبي الإحسان")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة الوصفة"))
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
        } else if (recipe == null || uiState.errorMessage != null) {
            IhsanErrorState(
                title = "حدث خطأ",
                message = uiState.errorMessage ?: "تعذر تحميل تفاصيل الوصفة",
                retryLabel = "إعادة المحاولة",
                onRetry = { viewModel.loadRecipeDetail(recipeId) },
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
                // Recipe Title Banner
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = recipe.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recipe.meaning,
                        fontSize = 13.sp,
                        color = IhsanTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

                // Quote Highlight Box (Mockup 7)
                if (!recipe.quranOrHadithQuote.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = recipe.quranOrHadithQuote,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                            if (!recipe.quoteSource.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = recipe.quoteSource,
                                    fontSize = 12.sp,
                                    color = IhsanTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Meaning Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    NabiSectionHeader(title = "معنى الوصفة")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recipe.meaning,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )
                }

                // From Seerah Section
                if (recipe.seerahContext.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "من السيرة")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = recipe.seerahContext,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 19.sp
                        )
                    }
                }

                // What We Learn Section
                if (recipe.whatWeLearn.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "ماذا نتعلم؟")
                        Spacer(modifier = Modifier.height(6.dp))
                        recipe.whatWeLearn.forEach { lesson ->
                            NabiLessonItem(text = lesson)
                        }
                    }
                }

                // Practical Application Section
                if (recipe.practicalReflection.isNotBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "تطبيق عملي")
                        Spacer(modifier = Modifier.height(6.dp))
                        NabiLessonItem(text = recipe.practicalReflection)
                    }
                }

                // Related Episodes Section
                if (uiState.relatedEpisodes.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        NabiSectionHeader(title = "الحلقات المرتبطة")
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.relatedEpisodes.forEach { ep ->
                            NabiEpisodeCard(
                                episode = ep,
                                onEpisodeClick = onEpisodeClick,
                                onFavoriteToggle = { viewModel.toggleEpisodeFavorite(it) }
                            )
                        }
                    }
                }

                // Source Attribution Card
                NabiSourceCard(
                    sourceTitle = recipe.source,
                    authorName = "محتوى إحسان التحريري"
                )

                // Save Recipe Button
                Button(
                    onClick = { viewModel.toggleRecipeFavorite(recipe.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recipe.isFavorite) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (recipe.isFavorite) MaterialTheme.colorScheme.onSurface else Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (recipe.isFavorite) "الوصفة محفوظة" else "احفظ الوصفة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
