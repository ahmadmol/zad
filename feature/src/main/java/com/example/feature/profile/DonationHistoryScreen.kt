package com.example.feature.profile

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.ehsan.domain.model.DonationStatus
import com.example.feature.profile.presentation.ProfileViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

enum class HistoryFilterTab(val label: String) {
    ALL("الكل"),
    OFFERS("عروض التبرع"),
    REQUESTS("طلبات المساعدة")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationHistoryScreen(
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imageStore: EhsanImageStore = koinInject()
    var selectedTab by remember { mutableStateOf(HistoryFilterTab.ALL) }

    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark
    val darkTealColor = if (isDark) colors.textPrimary else Color(0xFF003B46)

    val filteredDonations = remember(uiState.myDonations, selectedTab) {
        when (selectedTab) {
            HistoryFilterTab.ALL -> uiState.myDonations
            HistoryFilterTab.OFFERS -> uiState.myDonations.filter { it.type == "OFFER" }
            HistoryFilterTab.REQUESTS -> uiState.myDonations.filter { it.type == "REQUEST" }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_home_hero_background),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier.matchParentSize()
                    )

                    if (isDark) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.55f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "رجوع"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = darkTealColor
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "سجل المساهمات",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkTealColor
                            )
                        }
                    }
                }
            },
            containerColor = colors.surfaceBase
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Filter Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilterTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else colors.surfaceElevated,
                            border = if (isSelected) null else BorderStroke(1.dp, colors.borderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        IhsanLoadingState(message = stringResource(R.string.common_loading))
                    }
                } else if (filteredDonations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IhsanEmptyState(
                            title = "لا توجد مساهمات بعد",
                            message = when (selectedTab) {
                                HistoryFilterTab.ALL -> "تظهر هنا عروض التبرع وطلبات المساعدة التي قمت بإضافتها"
                                HistoryFilterTab.OFFERS -> "لم تقم بإضافة أي عروض تبرع بعد"
                                HistoryFilterTab.REQUESTS -> "لم تقم بإضافة أي طلبات مساعدة بعد"
                            },
                            icon = Icons.Default.FavoriteBorder
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredDonations, key = { it.id }) { donation ->
                            val statusTyped = DonationStatus.fromStorage(donation.status)
                            val isOffer = donation.type == "OFFER"

                            Surface(
                                onClick = { onItemClick(donation.id) },
                                shape = RoundedCornerShape(18.dp),
                                color = colors.surfaceElevated,
                                border = BorderStroke(1.dp, colors.borderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Item Thumbnail
                                    Surface(
                                        modifier = Modifier.size(76.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isOffer) colors.charityOfferContainer else colors.charityRequestContainer
                                    ) {
                                        val imageModel = imageStore.resolve(donation.imageUrl)
                                        if (imageModel != null) {
                                            AsyncImage(
                                                model = imageModel,
                                                contentDescription = donation.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (donation.category) {
                                                        "طعام" -> Icons.Default.Restaurant
                                                        "ملابس" -> Icons.Default.Checkroom
                                                        "أثاث" -> Icons.Default.Weekend
                                                        else -> if (isOffer) Icons.Default.VolunteerActivism else Icons.Default.Handshake
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(36.dp),
                                                    tint = if (isOffer) colors.charityOffer.copy(alpha = 0.5f) else colors.charityRequest.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Content Info
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Status chip
                                        val (statusBg, statusFg, statusText) = when (statusTyped) {
                                            DonationStatus.ACTIVE -> Triple(colors.charityOfferContainer, colors.charityOffer, "نشط")
                                            DonationStatus.COORDINATING -> Triple(colors.surfaceMint, MaterialTheme.colorScheme.primary, "جارٍ التنسيق")
                                            DonationStatus.FULFILLED -> Triple(colors.charityOfferContainer, colors.success, "مكتمل")
                                            DonationStatus.EXPIRED -> Triple(colors.surfaceMuted, colors.textSecondary, "منتهي")
                                            DonationStatus.CANCELLED -> Triple(colors.charityRequestContainer, colors.charityRequest, "ملغى")
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = statusBg,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(statusFg)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = statusText,
                                                        color = statusFg,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = if (isOffer) "عرض تبرع" else "طلب مساعدة",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = donation.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        if (donation.createdAt > 0) {
                                            Text(
                                                text = DateUtils.getRelativeTimeSpanString(
                                                    donation.createdAt,
                                                    System.currentTimeMillis(),
                                                    DateUtils.DAY_IN_MILLIS
                                                ).toString(),
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

