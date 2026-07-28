package com.example.feature.ehsan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.components.AuthBottomSheet
import com.example.feature.components.AuthViewModel
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.presentation.EhsanViewModel
import com.example.feature.ehsan.presentation.components.AddEhsanButton
import com.example.feature.ehsan.presentation.components.EhsanCategoryFilterOptions
import com.example.feature.ehsan.presentation.components.EhsanCityFilterOptions
import com.example.feature.ehsan.presentation.components.EhsanCommunityHero
import com.example.feature.ehsan.presentation.components.EhsanEmptyContent
import com.example.feature.ehsan.presentation.components.EhsanFilterSection
import com.example.feature.ehsan.presentation.components.EhsanListingTabs
import com.example.feature.ehsan.presentation.components.EhsanPrimaryActions
import com.example.feature.ehsan.presentation.components.EhsanSearchField
import com.example.feature.ehsan.presentation.components.EhsanTopBar
import com.example.feature.ehsan.presentation.components.LocalBoardNoticeCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun EhsanScreen(
    onNavigateBack: () -> Unit = {},
    onAddEhsanClick: (String) -> Unit = {},
    onDonationClick: (Long) -> Unit = {},
    viewModel: EhsanViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }
    var pendingActionType by remember { mutableStateOf("OFFER") }

    val cityCount = remember(uiState.donations) {
        uiState.donations.map { it.location.trim() }.filter { it.isNotEmpty() }.distinct().size
    }
    val hasActiveFilters = remember(
        uiState.selectedLocation,
        uiState.selectedCategory,
        uiState.selectedType
    ) {
        uiState.selectedLocation != "الكل" ||
            uiState.selectedCategory != "الكل" ||
            uiState.selectedType != "ALL"
    }

    val startAdd: (String) -> Unit = { type ->
        pendingActionType = type
        if (currentUser == null) showAuthSheet = true else onAddEhsanClick(type)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        // Parent MainScreen Scaffold already applies bottom-bar + navigationBars insets.
        // Keep local insets at zero so we only consume this Scaffold's topBar + FAB padding.
        Scaffold(
            topBar = { EhsanTopBar(onBack = onNavigateBack) },
            floatingActionButton = {
                AddEhsanButton(onClick = { startAdd("OFFER") })
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = IhsanTheme.colors.surfaceMuted
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = scaffoldPadding.calculateTopPadding() + 12.dp,
                    // FAB inset from this Scaffold + small breathing room (no fixed 88.dp spacer).
                    bottom = scaffoldPadding.calculateBottomPadding() + 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { LocalBoardNoticeCard() }

                item {
                    EhsanCommunityHero(
                        donorCount = uiState.donorCount,
                        completedCount = uiState.completedCount,
                        cityCount = cityCount
                    )
                }

                item {
                    EhsanPrimaryActions(
                        onDonateClick = { startAdd("OFFER") },
                        onHelpClick = { startAdd("REQUEST") }
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        EhsanSearchField(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange
                        )
                        EhsanFilterSection(
                            label = "المدن",
                            labelIcon = Icons.Default.LocationOn,
                            options = EhsanCityFilterOptions,
                            selected = uiState.selectedLocation,
                            onSelected = viewModel::onLocationChange
                        )
                        EhsanFilterSection(
                            label = "الفئات",
                            labelIcon = Icons.Default.Tag,
                            options = EhsanCategoryFilterOptions,
                            selected = uiState.selectedCategory,
                            onSelected = viewModel::onCategoryChange
                        )
                        EhsanListingTabs(
                            selectedType = uiState.selectedType,
                            onTypeSelected = viewModel::onTypeChange
                        )
                    }
                }

                when {
                    uiState.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    uiState.error != null -> {
                        item {
                            EhsanErrorContent(message = uiState.error.orEmpty())
                        }
                    }

                    uiState.filteredDonations.isEmpty() -> {
                        item {
                            EhsanEmptyContent(
                                searchQuery = uiState.searchQuery,
                                hasActiveFilters = hasActiveFilters
                            )
                        }
                    }

                    else -> {
                        items(uiState.filteredDonations, key = { it.id }) { donation ->
                            DonationListItem(
                                donation = donation,
                                onClick = { onDonationClick(donation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EhsanErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "تعذر تحميل فرص إحسان",
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

@Composable
private fun DonationListItem(donation: Donation, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = IhsanTheme.colors.quickActionSurface
            ) {
                if (donation.imageUrl != null) {
                    AsyncImage(
                        model = donation.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when (donation.category) {
                            "طعام" -> Icons.Default.Restaurant
                            "ملابس" -> Icons.Default.Checkroom
                            "أثاث" -> Icons.Default.Weekend
                            else -> Icons.Default.Category
                        },
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (donation.type == "OFFER") {
                            IhsanTheme.colors.charityOfferContainer
                        } else {
                            IhsanTheme.colors.charityRequestContainer
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (donation.type == "OFFER") "تبرع" else "طلب",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = if (donation.type == "OFFER") {
                                IhsanTheme.colors.charityOffer
                            } else {
                                IhsanTheme.colors.charityRequest
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = donation.location,
                        fontSize = 11.sp,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = donation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = donation.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        donation.donorName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }
        }
    }
}

@Preview(name = "Ehsan Empty 360", locale = "ar", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun EhsanEmptyPreview() {
    IhsanTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(color = IhsanTheme.colors.surfaceMuted) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { LocalBoardNoticeCard() }
                    item {
                        EhsanCommunityHero(donorCount = 0, completedCount = 0, cityCount = 0)
                    }
                    item {
                        EhsanPrimaryActions(onDonateClick = {}, onHelpClick = {})
                    }
                    item {
                        EhsanSearchField(query = "", onQueryChange = {})
                    }
                    item {
                        EhsanFilterSection(
                            label = "المدن",
                            labelIcon = Icons.Default.LocationOn,
                            options = EhsanCityFilterOptions,
                            selected = "الكل",
                            onSelected = {}
                        )
                    }
                    item {
                        EhsanListingTabs(selectedType = "ALL", onTypeSelected = {})
                    }
                    item {
                        EhsanEmptyContent(searchQuery = "", hasActiveFilters = false)
                    }
                }
            }
        }
    }
}

@Preview(name = "Ehsan Dark", locale = "ar", widthDp = 430, heightDp = 860, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EhsanDarkPreview() {
    EhsanEmptyPreview()
}
