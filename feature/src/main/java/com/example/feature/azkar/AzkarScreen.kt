package com.example.feature.azkar

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarViewModel
import org.koin.androidx.compose.koinViewModel

private val HeroMinHeight = 168.dp
private val ScreenHorizontalPadding = 12.dp

@Composable
fun AzkarScreen(
    viewModel: AzkarViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onOpenSebha: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var openedCategory by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = openedCategory != null) {
        openedCategory = null
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val colors = IhsanTheme.colors
        Scaffold(containerColor = colors.surfaceBase) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = scaffoldPadding.calculateBottomPadding())
                    .background(colors.surfaceBase)
            ) {
                AzkarHero(
                    favoritesOnly = uiState.showFavoritesOnly,
                    onBack = {
                        if (openedCategory != null) openedCategory = null else onNavigateBack()
                    },
                    onToggleFavorites = {
                        viewModel.onAction(
                            AzkarAction.OnToggleShowFavorites(!uiState.showFavoritesOnly)
                        )
                    }
                )

                AzkarSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = {
                        viewModel.onAction(AzkarAction.OnSearchQueryChanged(it))
                    }
                )

                CategoryChips(
                    categories = uiState.availableCategories,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { category ->
                        openedCategory = null
                        viewModel.onAction(AzkarAction.OnCategorySelected(category))
                    }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        uiState.isLoading -> IhsanLoadingState(
                            message = stringResource(R.string.azkar_loading),
                            modifier = Modifier.fillMaxSize()
                        )

                        uiState.error != null -> IhsanErrorState(
                            title = stringResource(R.string.azkar_error_title),
                            message = stringResource(R.string.azkar_error_message),
                            modifier = Modifier.fillMaxSize()
                        )

                        uiState.azkarList.isEmpty() -> IhsanEmptyState(
                            title = stringResource(R.string.azkar_empty_title),
                            message = when {
                                uiState.searchQuery.isNotBlank() -> stringResource(
                                    R.string.azkar_search_empty_message,
                                    uiState.searchQuery
                                )
                                uiState.showFavoritesOnly -> stringResource(R.string.azkar_favorites_empty_message)
                                else -> stringResource(R.string.azkar_empty_message)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        openedCategory != null -> AzkarList(
                            azkar = uiState.azkarList,
                            category = openedCategory,
                            fontSize = uiState.fontSize,
                            onAction = viewModel::onAction
                        )

                        else -> CategoryCards(
                            azkar = uiState.azkarList,
                            onOpenCategory = { category ->
                                if (uiState.selectedCategory != category) {
                                    viewModel.onAction(AzkarAction.OnCategorySelected(category))
                                }
                                openedCategory = category
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AzkarHero(
    favoritesOnly: Boolean,
    onBack: () -> Unit,
    onToggleFavorites: () -> Unit
) {
    val colors = IhsanTheme.colors
    val isDark = colors.surfaceBase.luminance() < 0.18f
    val heroText = if (isDark) colors.textPrimary else Color(0xFF073E46)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HeroMinHeight)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_azkar_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        if (isDark) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.56f))
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.5f to Color.Transparent,
                        1f to colors.surfaceBase
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.azkar_back),
                        tint = heroText
                    )
                }
                IconButton(
                    onClick = onToggleFavorites,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (favoritesOnly) R.string.azkar_show_all else R.string.azkar_show_favorites
                        ),
                        tint = if (favoritesOnly) colors.favorite else heroText
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.azkar_redesign_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = heroText,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.azkar_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = heroText.copy(alpha = 0.86f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AzkarSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val colors = IhsanTheme.colors
    val focusManager = LocalFocusManager.current
    val searchDescription = stringResource(R.string.azkar_search_description)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(R.drawable.ic_azkar_search),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.textSecondary),
                modifier = Modifier.size(20.dp)
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.azkar_search_placeholder),
                color = colors.textSecondary
            )
        },
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceElevated,
            unfocusedContainerColor = colors.surfaceElevated,
            focusedBorderColor = colors.fieldFocusedBorder,
            unfocusedBorderColor = colors.borderSubtle,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.selectedContent
        ),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontalPadding)
            .heightIn(min = 56.dp)
            .semantics {
                contentDescription = searchDescription
            }
    )
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val options = listOf<String?>(null) + categories
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(options, key = { it ?: "all" }) { category ->
            CategoryChip(
                label = if (category == null) stringResource(R.string.azkar_category_all)
                else categoryChipLabel(category),
                icon = categoryIcon(category),
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    @DrawableRes icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors
    val container by animateColorAsState(
        targetValue = if (isSelected) colors.selectedContainer else colors.surfaceMuted,
        animationSpec = tween(160),
        label = "azkarChipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (isSelected) colors.selectedContent else colors.textSecondary,
        animationSpec = tween(160),
        label = "azkarChipContent"
    )
    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, if (isSelected) content.copy(alpha = 0.22f) else colors.borderSubtle),
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(content),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CategoryCards(
    azkar: List<Zikr>,
    onOpenCategory: (String) -> Unit
) {
    val grouped = azkar.groupBy { it.category }
    LazyColumn(
        contentPadding = PaddingValues(
            start = ScreenHorizontalPadding,
            end = ScreenHorizontalPadding,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(grouped.entries.toList(), key = { it.key }) { (category, items) ->
            AzkarCategoryCard(
                category = category,
                count = items.size,
                onClick = { onOpenCategory(category) }
            )
        }
    }
}

@Composable
private fun AzkarCategoryCard(
    category: String,
    count: Int,
    onClick: () -> Unit
) {
    val colors = IhsanTheme.colors
    val description = stringResource(R.string.azkar_category_semantics, category, count)
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            }
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(categoryImage(category)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.azkar_count_format, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_azkar_chevron_rtl),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.textSecondary),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun AzkarList(
    azkar: List<Zikr>,
    category: String?,
    fontSize: Float,
    onAction: (AzkarAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenHorizontalPadding,
            end = ScreenHorizontalPadding,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (category != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleMedium,
                        color = IhsanTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .semantics { heading() }
                    )
                    TextButton(
                        onClick = { onAction(AzkarAction.OnResetCategory(category)) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(stringResource(R.string.azkar_reset_all))
                    }
                }
            }
        }

        items(azkar, key = { it.id }) { zikr ->
            ZikrCard(zikr = zikr, fontSize = fontSize, onAction = onAction)
        }
    }
}

@Composable
private fun ZikrCard(zikr: Zikr, fontSize: Float, onAction: (AzkarAction) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val colors = IhsanTheme.colors
    val counterDescription = if (zikr.targetCount > 0) {
        stringResource(R.string.azkar_counter_description, zikr.currentCount, zikr.targetCount)
    } else {
        stringResource(R.string.azkar_free_counter_description, zikr.currentCount)
    }
    val incrementDescription = stringResource(
        if (zikr.isCompleted) R.string.azkar_completed else R.string.azkar_increment
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (zikr.isCompleted) colors.selectedContainer.copy(alpha = 0.45f)
            else colors.surfaceElevated
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (zikr.isCompleted) colors.progressActive.copy(alpha = 0.35f) else colors.borderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = zikr.title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onAction(AzkarAction.OnToggleFavorite(zikr.id)) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (zikr.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (zikr.isFavorite) R.string.azkar_remove_favorite else R.string.azkar_add_favorite
                        ),
                        tint = if (zikr.isFavorite) colors.favorite else colors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = zikr.text,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = (fontSize * 1.5f).sp,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = {
                    if (zikr.targetCount > 0) {
                        (zikr.currentCount.toFloat() / zikr.targetCount).coerceIn(0f, 1f)
                    } else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = colors.progressActive,
                trackColor = colors.progressTrack
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (zikr.targetCount > 0) {
                        stringResource(R.string.azkar_counter_format, zikr.currentCount, zikr.targetCount)
                    } else zikr.currentCount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.progressActive,
                    modifier = Modifier.semantics {
                        contentDescription = counterDescription
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAction(AzkarAction.OnReset(zikr.id)) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.azkar_reset_counter),
                            tint = colors.textSecondary
                        )
                    }
                    Button(
                        onClick = {
                            if (!zikr.isCompleted) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(AzkarAction.OnIncrement(zikr.id))
                            }
                        },
                        enabled = !zikr.isCompleted,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.selectedContainer,
                            contentColor = colors.selectedContent,
                            disabledContainerColor = colors.surfaceMuted,
                            disabledContentColor = colors.textSecondary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .semantics {
                                contentDescription = incrementDescription
                            }
                    ) {
                        Icon(
                            imageVector = if (zikr.isCompleted) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(
                                if (zikr.isCompleted) R.string.azkar_completed else R.string.azkar_increment
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@DrawableRes
private fun categoryImage(category: String): Int = when {
    category.contains("صباح") -> R.drawable.img_azkar_morning
    category.contains("مساء") -> R.drawable.img_azkar_evening
    category.contains("بعد الصلاة") -> R.drawable.img_azkar_after_prayer
    category.contains("نوم") -> R.drawable.img_azkar_sleep
    else -> R.drawable.img_azkar_various
}

@DrawableRes
private fun categoryIcon(category: String?): Int = when {
    category == null -> R.drawable.ic_azkar_all
    category.contains("صباح") -> R.drawable.ic_azkar_morning
    category.contains("مساء") -> R.drawable.ic_azkar_evening
    category.contains("بعد الصلاة") -> R.drawable.ic_azkar_after_prayer
    category.contains("نوم") -> R.drawable.ic_azkar_sleep
    else -> R.drawable.ic_azkar_various
}

@Composable
private fun categoryChipLabel(category: String): String = when {
    category.contains("صباح") -> stringResource(R.string.azkar_category_morning)
    category.contains("مساء") -> stringResource(R.string.azkar_category_evening)
    category.contains("بعد الصلاة") -> stringResource(R.string.azkar_category_after_prayer)
    category.contains("نوم") -> stringResource(R.string.azkar_category_sleep)
    category == "تسبيح" -> stringResource(R.string.azkar_category_tasbih)
    else -> category
}
