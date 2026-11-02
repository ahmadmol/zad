package com.example.feature.ehsan

import com.example.feature.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.layout
import androidx.compose.material3.FabPosition
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.components.AuthBottomSheet
import com.example.feature.components.AuthViewModel
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.presentation.EhsanViewModel
import com.example.feature.ehsan.presentation.components.AddEhsanFab
import com.example.feature.ehsan.presentation.components.DonationCardItem
import com.example.feature.ehsan.presentation.components.EhsanEmptyContent
import com.example.feature.ehsan.presentation.components.EhsanFilterRow
import com.example.feature.ehsan.presentation.components.EhsanHeaderBanner
import com.example.feature.ehsan.presentation.components.EhsanSearchField
import com.example.feature.ehsan.presentation.components.EhsanSegmentedTabs
import com.example.feature.ehsan.presentation.components.LocalBoardNoticeCard
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Ehsan Screen Redesign matching reference design
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EhsanScreen(
    onNavigateBack: () -> Unit = {},
    onAddEhsanClick: (String) -> Unit = {},
    onDonationClick: (Long) -> Unit = {},
    showNavigationIcon: Boolean = false,
    viewModel: EhsanViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val imageStore: EhsanImageStore = koinInject()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showDemoReset by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf("OFFER") }

    val hasActiveFilters = remember(
        uiState.selectedLocation,
        uiState.selectedCategory,
        uiState.selectedSort,
        uiState.selectedType, uiState.selectedStatus, uiState.selectedOrigin
    ) {
            uiState.selectedLocation != "الكل" ||
            uiState.selectedCategory != "الكل" ||
            uiState.selectedSort != "DEFAULT" ||
            uiState.selectedType != "OFFER" || uiState.selectedStatus != "ALL" || uiState.selectedOrigin != "ALL"
    }

    val startAdd: (String) -> Unit = { type ->
        pendingActionType = type
        if (currentUser == null) showAuthSheet = true else onAddEhsanClick(type)
    }

    if (showDemoReset) androidx.compose.material3.AlertDialog(
        onDismissRequest = { showDemoReset = false }, title = { Text("إعادة أمثلة DEMO؟") },
        text = { Text("يعاد إنشاء الأمثلة الخيالية فقط. تبقى سجلاتك وصورك وملفك والقرآن والأذكار والإعدادات محفوظة.") },
        confirmButton = { androidx.compose.material3.TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { showDemoReset = false; viewModel.resetDemoOnly() }) { Text("إعادة أمثلة DEMO") } },
        dismissButton = { androidx.compose.material3.TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { showDemoReset = false }) { Text("إلغاء") } })

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            floatingActionButtonPosition = FabPosition.Start,
            floatingActionButton = {
                AddEhsanFab(
                    type = uiState.selectedType,
                    onClick = { startAdd(uiState.selectedType) }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background
        ) { scaffoldPadding ->
            if (showAuthSheet) {
                AuthBottomSheet(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = {
                        showAuthSheet = false
                        onAddEhsanClick(pendingActionType)
                    }
                )
            }

            if (showAdvancedFilters) com.example.feature.ehsan.presentation.EhsanAdvancedFilters(
                uiState, onDismiss = { showAdvancedFilters = false },
                onApply = { city, category, sort ->
                    viewModel.onLocationChange(city); viewModel.onCategoryChange(category); viewModel.onSortChange(sort)
                    showAdvancedFilters = false
                })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = scaffoldPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 0: Atmospheric Mosque Silhouette Banner
                item {
                    EhsanHeaderBanner(
                        cityName = uiState.locationName,
                        islamicDate = com.example.feature.core.util.currentHijriDateText(),
                        onSearchClick = { showAdvancedFilters = true }
                    )
                }

                // Item 1: Segmented Tabs (عروض تبرع / طلبات مساعدة)
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = IhsanTheme.dimens.screenHorizontal)
                            .layout { measurable, constraints ->
                                val child = measurable.measure(constraints)
                                val overlap = 12.dp.roundToPx()
                                layout(child.width, (child.height - overlap).coerceAtLeast(0)) {
                                    child.placeRelative(0, -overlap)
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EhsanSegmentedTabs(
                            selectedType = uiState.selectedType,
                            onTypeSelected = viewModel::onTypeChange
                        )

                        // Item 2: Search Bar
                        EhsanSearchField(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            cityName = uiState.locationName,
                            onFilterClick = { showAdvancedFilters = true }
                        )

                        // Item 3: Sorting controls
                        EhsanFilterRow(
                            selectedSort = uiState.selectedSort,
                            onSortChange = viewModel::onSortChange
                        )

                        Text("${uiState.filteredDonations.size} نتيجة", style = MaterialTheme.typography.labelSmall,
                            color = IhsanTheme.colors.textSecondary)
                        LocalBoardNoticeCard()
                        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ALL" to "كل الحالات", "AVAILABLE" to "متاح", "PENDING" to "بانتظار محلي", "COMPLETED" to "منتهٍ محليًا").forEach { (value, label) ->
                                androidx.compose.material3.FilterChip(selected = uiState.selectedStatus == value, onClick = { viewModel.onStatusChange(value) }, label = { Text(label) })
                            }
                        }
                        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("ALL" to "كل المصادر", "USER" to "محلي", "LEGACY" to "قديم بلا ملكية", "DEMO" to "أمثلة DEMO").forEach { (value, label) ->
                                androidx.compose.material3.FilterChip(selected = uiState.selectedOrigin == value, onClick = { viewModel.onOriginChange(value) }, label = { Text(label) })
                            }
                        }
                        androidx.compose.material3.TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { showDemoReset = true }) { Text("إعادة أمثلة DEMO فقط") }
                    }
                }

                // Listing Content Section
                when {
                    uiState.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = IhsanTheme.colors.accentContent)
                            }
                        }
                    }

                    uiState.error != null -> {
                        item {
                            Column { EhsanErrorContent(message = uiState.error.orEmpty()); Button(onClick = viewModel::retry, modifier = Modifier.padding(16.dp)) { Text("إعادة المحاولة") } }
                        }
                    }

                    uiState.filteredDonations.isEmpty() -> {
                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                EhsanEmptyContent(
                                    searchQuery = uiState.searchQuery,
                                    hasActiveFilters = hasActiveFilters
                                )
                            }
                        }
                    }

                    else -> {
                        items(uiState.filteredDonations, key = { it.id }) { donation ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                DonationCardItem(
                                    donation = donation,
                                    imageModel = imageStore.resolve(donation.imageUrl),
                                    onClick = { onDonationClick(donation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EhsanErrorContent(message: String) {
    val charityLoadErrorText = stringResource(R.string.polish_charity_load_error)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = charityLoadErrorText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = IhsanTheme.colors.textSecondaryMuted
            )
        }
    }
}
