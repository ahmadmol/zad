package com.example.feature.sanhya.presentation

import com.example.designsystem.theme.IhsanTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.sanhya.presentation.components.QuranStoryCard
import com.example.feature.sanhya.presentation.components.StoryEmptyState

@Composable
fun SanhyaFavoritesScreen(
    viewModel: SanhyaViewModel,
    onNavigateBack: () -> Unit,
    onStoryClick: (storyId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoritesStories.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else MaterialTheme.colorScheme.surface)
    ) {
        // Top Bar
        com.example.designsystem.component.IhsanTopBar(
            title = "قصصي المفضلة", style = com.example.designsystem.component.IhsanTopBarStyle.CompactSkyline,
            onNavigateBack = onNavigateBack)

        Spacer(modifier = Modifier.height(8.dp))

        if (favorites.isEmpty()) {
            StoryEmptyState(
                title = "لا توجد قصص مفضلة بعد",
                message = "اضغط على أيقونة القلب في أي قصة لإضافتها إلى قائمة مفضلتك بسهولة."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { story ->
                    QuranStoryCard(
                        story = story,
                        onClick = { onStoryClick(story.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(story.id) }
                    )
                }
            }
        }
    }
}
