package com.example.feature.asma.presentation

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.R as DesignR
import com.example.feature.R as FeatureR
import com.example.designsystem.component.IhsanButton
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.asma.domain.model.AllahName
import org.koin.androidx.compose.koinViewModel

@Composable
fun AsmaScreen(
    viewModel: AsmaViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AsmaHeroHeader(onNavigateBack = onNavigateBack)
            },
            containerColor = IhsanTheme.colors.surfaceBase
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        IhsanLoadingState(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.error != null -> {
                        IhsanErrorState(
                            title = stringResource(FeatureR.string.asma_error_title),
                            message = uiState.error,
                            retryLabel = stringResource(FeatureR.string.common_retry),
                            onRetry = { viewModel.onAction(AsmaAction.OnRetry) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                AsmaSearchBar(
                                    query = uiState.searchQuery,
                                    onQueryChange = { query ->
                                        viewModel.onAction(AsmaAction.OnSearchQueryChange(query))
                                    }
                                )
                            }

                            if (uiState.searchQuery.isNotBlank()) {
                                if (uiState.searchResults.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(
                                                FeatureR.string.asma_search_results_title,
                                                uiState.searchResults.size
                                            ),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = IhsanTheme.colors.textPrimary,
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                        )
                                    }

                                    items(
                                        items = uiState.searchResults,
                                        key = { it.id }
                                    ) { item ->
                                        AsmaSearchResultCard(
                                            asma = item,
                                            onClick = {
                                                viewModel.onAction(AsmaAction.OnNameClick(item))
                                            }
                                        )
                                    }
                                } else {
                                    item {
                                        IhsanEmptyState(
                                            title = stringResource(FeatureR.string.asma_search_no_results_title),
                                            message = stringResource(
                                                FeatureR.string.asma_search_no_results_body,
                                                uiState.searchQuery
                                            ),
                                            modifier = Modifier.padding(top = 32.dp)
                                        )
                                    }
                                }
                            } else {
                                uiState.selectedName?.let { selected ->
                                    item {
                                        FeaturedAsmaCard(
                                            selectedName = selected,
                                            totalCount = uiState.asmaList.size,
                                            onPreviousClick = {
                                                viewModel.onAction(AsmaAction.OnSelectPrevious)
                                            },
                                            onNextClick = {
                                                viewModel.onAction(AsmaAction.OnSelectNext)
                                            }
                                        )
                                    }

                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = selected.name,
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 32.sp
                                                ),
                                                color = IhsanTheme.colors.brand,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = selected.explanation,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    lineHeight = 24.sp
                                                ),
                                                color = IhsanTheme.colors.textSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(24.dp))

                                            IhsanButton(
                                                onClick = { viewModel.onAction(AsmaAction.OnOpenDetails) },
                                                modifier = Modifier
                                                    .fillMaxWidth(0.8f)
                                                    .height(50.dp),
                                                containerColor = IhsanTheme.colors.brand,
                                                contentColor = IhsanTheme.colors.onBrand
                                            ) {
                                                Text(
                                                    text = stringResource(FeatureR.string.asma_reflect_cta),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.isDetailsOpen && uiState.selectedName != null) {
                    val selected = uiState.selectedName!!
                    AsmaDetailsSheet(
                        name = selected,
                        onDismiss = { viewModel.onAction(AsmaAction.OnDismissDetails) },
                        onToggleFavorite = { viewModel.onAction(AsmaAction.OnToggleFavorite(selected.id)) },
                        onCopy = {
                            val copyText = "${selected.name}\n${selected.transliteration} - ${selected.meaning}\n${selected.explanation}"
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(
                                context,
                                context.getString(FeatureR.string.asma_copy_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onShare = {
                            val shareText = "${selected.name} (${selected.transliteration})\n${selected.meaning}\n\n${selected.explanation}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    context.getString(FeatureR.string.asma_share_title)
                                )
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    context.getString(FeatureR.string.asma_share_title)
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AsmaHeroHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandColor = IhsanTheme.colors.brand
    val onBrandColor = IhsanTheme.colors.onBrand
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            IhsanTheme.colors.brandElevated,
                            IhsanTheme.colors.surfaceBase
                        )
                    } else {
                        listOf(
                            brandColor,
                            brandColor.copy(alpha = 0.90f)
                        )
                    }
                )
            )
            .statusBarsPadding()
            .padding(bottom = 20.dp)
    ) {
        Image(
            painter = painterResource(id = DesignR.drawable.ic_mosque_silhouette),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp),
            contentScale = ContentScale.FillBounds,
            alpha = if (isDark) 0.15f else 0.22f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .background(onBrandColor.copy(alpha = 0.14f))
                        .semantics { contentDescription = "رجوع" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = onBrandColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(FeatureR.string.asma_hero_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = onBrandColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(FeatureR.string.asma_hero_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        ),
                        color = onBrandColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AsmaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = IhsanTheme.colors.surfaceElevated,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(FeatureR.string.cd_asma_search),
                tint = IhsanTheme.colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(FeatureR.string.asma_search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = IhsanTheme.colors.textDisabled
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = IhsanTheme.colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "مسح البحث",
                        tint = IhsanTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedAsmaCard(
    selectedName: AllahName,
    totalCount: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = IhsanTheme.colors.surfaceWarm
        ),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "اللَّه",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = IhsanTheme.colors.goldAccent.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .background(IhsanTheme.colors.surfaceElevated.copy(alpha = 0.8f))
                        .semantics { contentDescription = "الاسم التالي" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null,
                        tint = IhsanTheme.colors.brand,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    IhsanTheme.colors.surfaceMint,
                                    IhsanTheme.colors.surfaceMuted.copy(alpha = 0.5f)
                                )
                            )
                        )
                        .border(1.dp, IhsanTheme.colors.goldAccent.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = selectedName,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.92f))
                                .togetherWith(fadeOut(animationSpec = tween(120)))
                        },
                        label = "AsmaNameAnimation"
                    ) { target ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = target.name,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 38.sp
                                ),
                                color = IhsanTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = stringResource(FeatureR.string.asma_index_format, target.id, totalCount),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = IhsanTheme.colors.goldAccent,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .background(IhsanTheme.colors.surfaceElevated.copy(alpha = 0.8f))
                        .semantics { contentDescription = "الاسم السابق" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = null,
                        tint = IhsanTheme.colors.brand,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AsmaSearchResultCard(
    asma: AllahName,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = IhsanTheme.colors.surfaceElevated
        ),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IhsanTheme.colors.surfaceMint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${asma.id}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IhsanTheme.colors.goldAccent
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asma.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IhsanTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = asma.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsmaDetailsSheet(
    name: AllahName,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = IhsanTheme.colors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(IhsanTheme.colors.surfaceWarm)
                    .border(1.dp, IhsanTheme.colors.goldAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = IhsanTheme.colors.brand
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name.transliteration,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = IhsanTheme.colors.goldAccent
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = name.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = IhsanTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = IhsanTheme.colors.surfaceWarm.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
            ) {
                Text(
                    text = name.explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp
                    ),
                    color = IhsanTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = if (name.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (name.isFavorite) IhsanTheme.colors.favorite else IhsanTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (name.isFavorite) stringResource(FeatureR.string.asma_remove_favorite) else stringResource(FeatureR.string.asma_add_favorite),
                        color = IhsanTheme.colors.textPrimary
                    )
                }

                TextButton(
                    onClick = onCopy,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = IhsanTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(FeatureR.string.asma_copy_action),
                        color = IhsanTheme.colors.textPrimary
                    )
                }

                TextButton(
                    onClick = onShare,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = IhsanTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(FeatureR.string.asma_share_action),
                        color = IhsanTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
