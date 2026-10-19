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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanButton
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R as FeatureR
import com.example.feature.asma.domain.model.AllahName
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun AsmaScreen(
    viewModel: AsmaViewModel = koinViewModel(),
    initialNameId: Int? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var initialTargetHandled by remember(initialNameId) { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(initialNameId, uiState.asmaList) {
        if (!initialTargetHandled) uiState.asmaList.firstOrNull { it.id == initialNameId }?.let { name ->
            initialTargetHandled = true
            viewModel.onAction(AsmaAction.OnNameClick(name)); viewModel.onAction(AsmaAction.OnOpenDetails)
        }
    }
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AsmaHeroHeader(
                    onNavigateBack = onNavigateBack
                )
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                            },
                                            onReflectClick = {
                                                viewModel.onAction(AsmaAction.OnOpenDetails)
                                            }
                                        )
                                    }
                                }
                                items(uiState.visibleAsmaList, key = { "name-${it.id}" }) { name ->
                                    AsmaSearchResultCard(name, onClick = { viewModel.onAction(AsmaAction.OnNameClick(name)) })
                                }
                            }
                        }
                    }
                }

                if (uiState.isDetailsOpen && uiState.selectedName != null) {
                    val selected = uiState.selectedName!!
                    AsmaDetailsDialog(
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
    com.example.designsystem.component.IhsanTopBar(
        title = stringResource(FeatureR.string.asma_hero_title), modifier = modifier,
        style = com.example.designsystem.component.IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background, onNavigateBack = onNavigateBack)
}

@Composable
private fun AsmaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    com.example.designsystem.component.IhsanSearchBar(query = query, onQueryChange = onQueryChange,
        placeholder = stringResource(FeatureR.string.asma_search_placeholder), modifier = modifier)
}

@Composable
private fun FeaturedAsmaCard(
    selectedName: AllahName,
    totalCount: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onReflectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val font = FontFamily(Font(FeatureR.font.amiri_quran))
    Card(modifier.fillMaxWidth().heightIn(min = 380.dp), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceWarm),
        border = BorderStroke(.5.dp, IhsanTheme.colors.borderSubtle)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("اللَّهُ", fontFamily = font, fontSize = 30.sp, lineHeight = 46.sp, color = if (IhsanTheme.isDark) IhsanTheme.colors.goldAccent else IhsanTheme.colors.textPrimary)
            Row(Modifier.fillMaxWidth().heightIn(min = 172.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onNextClick, Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, "الاسم التالي", tint = IhsanTheme.colors.accentContent)
                }
                Column(Modifier.weight(1f).background(IhsanTheme.colors.surfaceElevated.copy(alpha = .6f), CircleShape)
                    .padding(vertical = 22.dp).clickable(onClick = onReflectClick), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedName.name, fontFamily = font, fontSize = 54.sp, lineHeight = 92.sp,
                        color = IhsanTheme.colors.textPrimary, textAlign = TextAlign.Center)
                    Text("${selectedName.id} من $totalCount", fontSize = 11.sp, color = IhsanTheme.colors.textSecondary)
                }
                IconButton(onPreviousClick, Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, "الاسم السابق", tint = IhsanTheme.colors.accentContent)
                }
            }
            Text(selectedName.name, fontSize = 21.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold,
                color = IhsanTheme.colors.textPrimary, modifier = Modifier.padding(top = 10.dp))
            Text(selectedName.explanation, fontSize = 13.sp, lineHeight = 20.sp,
                color = IhsanTheme.colors.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp, bottom = 14.dp))
            Button(onReflectClick, shape = RoundedCornerShape(18.dp), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IhsanTheme.colors.selectedContainer, contentColor = IhsanTheme.colors.selectedContent)) {
                Text("تأمل في الاسم", fontSize = 14.sp)
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp).border(1.dp, IhsanTheme.colors.goldAccent.copy(alpha = .4f), CircleShape)
                    .clip(CircleShape)
                    .background(IhsanTheme.colors.surfaceMint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = asma.id.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
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
                    text = asma.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AsmaDetailsDialog(
    name: AllahName,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = IhsanTheme.colors.surfaceElevated,
            border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
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
                        color = if (IhsanTheme.isDark) IhsanTheme.colors.goldAccent else IhsanTheme.colors.brand
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
                        text = "شرح تحريري يحتاج توثيق المصدر والمراجعة:\n${name.explanation}",
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
                    , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
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
                    , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
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
                    , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
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
}
