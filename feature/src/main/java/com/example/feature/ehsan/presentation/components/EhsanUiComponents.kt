package com.example.feature.ehsan.presentation.components

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.designsystem.component.IhsanHeroScrim
import com.example.designsystem.component.IhsanHeroTopRow
import com.example.designsystem.component.IhsanTextWithKaabaIcon
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.PrimaryTeal
import com.example.feature.R
import com.example.feature.ehsan.domain.model.Donation

/**
 * Atmospheric Header Banner for the Ehsan Screen
 * Matches the reference design with mosque silhouettes, top bar controls, and hero text.
 */
@Composable
fun EhsanHeaderBanner(
    cityName: String?,
    islamicDate: String?,
    modifier: Modifier = Modifier,
    onNotificationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    com.example.designsystem.component.IhsanSkylineHeader(
        title = "مجتمع إحسان", subtitle = stringResource(R.string.polish_community_hero),
        location = cityName, hijriDate = islamicDate, showBrand = true, modifier = modifier,
        actions = {
            IconButton(onSearchClick, Modifier.size(36.dp)) { Icon(Icons.Default.Search, "بحث", modifier = Modifier.size(20.dp)) }
            IconButton(onNotificationClick, Modifier.size(36.dp)) { Icon(Icons.Outlined.Notifications, "الإشعارات", modifier = Modifier.size(20.dp)) }
        })
}

/**
 * Segmented Tabs for switching between "عروض تبرع" (OFFER) and "طلبات مساعدة" (REQUEST)
 */
@Composable
fun EhsanSegmentedTabs(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = IhsanTheme.isDark
    val darkTeal = PrimaryTeal
    val activeContainer = if (isDark) IhsanTheme.colors.selectedContainer else darkTeal
    val activeContent = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.onBrand
    val inactiveContainer = if (isDark) IhsanTheme.colors.surfaceMuted else IhsanTheme.colors.surfaceWarm
    val inactiveContent = IhsanTheme.colors.textSecondary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(20.dp),
        color = inactiveContainer,
        border = BorderStroke(0.5.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // OFFER Tab
            val isOffer = selectedType == "OFFER"
            Surface(
                onClick = { onTypeSelected("OFFER") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                    .fillMaxHeight()
                    .semantics { role = Role.Tab; selected = selectedType == "OFFER" },
                shape = RoundedCornerShape(22.dp),
                color = if (isOffer) activeContainer else Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_ehsan_offers_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isOffer) FontWeight.Bold else FontWeight.Medium,
                        color = if (isOffer) activeContent else inactiveContent
                    )
                }
            }

            // REQUEST Tab
            val isRequest = selectedType == "REQUEST"
            Surface(
                onClick = { onTypeSelected("REQUEST") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 36.dp)
                    .fillMaxHeight()
                    .semantics { role = Role.Tab; selected = selectedType == "REQUEST" },
                shape = RoundedCornerShape(22.dp),
                color = if (isRequest) activeContainer else Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.home_ehsan_requests_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isRequest) FontWeight.Bold else FontWeight.Medium,
                        color = if (isRequest) activeContent else inactiveContent
                    )
                }
            }
        }
    }
}

/**
 * Rounded Search TextField matching reference
 */
@Composable
fun EhsanSearchField(query: String, onQueryChange: (String) -> Unit,
    cityName: String?, modifier: Modifier = Modifier, onFilterClick: (() -> Unit)? = null) {
    com.example.designsystem.component.IhsanSearchBar(modifier, query, onQueryChange,
        placeholder = "ابحث عن فرصة خيرية${cityName?.let { " في $it" }.orEmpty()}...",
        onFilterClick = onFilterClick)
}

/**
 * Sorting chips. Category filtering remains an independent state concern.
 */
@Composable
fun EhsanFilterRow(
    selectedSort: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterAllText = stringResource(R.string.polish_filter_all)
    val filterLatestText = stringResource(R.string.polish_filter_latest)
    val filterNearestText = stringResource(R.string.polish_filter_nearest)

    val isDark = IhsanTheme.isDark
    val darkTeal = PrimaryTeal
    val selectedBg = if (isDark) IhsanTheme.colors.selectedContainer else darkTeal
    val selectedFg = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.onBrand

    LazyRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            val isSelected = selectedSort == "DEFAULT"
            Surface(
                onClick = { onSortChange("DEFAULT") },
                modifier = Modifier.semantics { selected = isSelected },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) selectedBg else IhsanTheme.colors.surfaceMuted,
                border = BorderStroke(1.dp, if (isSelected) selectedBg else IhsanTheme.colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 36.dp).padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = if (isSelected) selectedFg else IhsanTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = filterAllText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) selectedFg else IhsanTheme.colors.textPrimary
                    )
                }
            }
        }

        item {
            val isSelected = selectedSort == "LATEST"
            Surface(
                onClick = { onSortChange("LATEST") },
                modifier = Modifier.semantics { selected = isSelected },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) selectedBg else IhsanTheme.colors.surfaceMuted,
                border = BorderStroke(1.dp, if (isSelected) selectedBg else IhsanTheme.colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 36.dp).padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (isSelected) selectedFg else IhsanTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = filterLatestText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) selectedFg else IhsanTheme.colors.textPrimary
                    )
                }
            }
        }


    }
}

/**
 * Detailed Donation Card item matching reference
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DonationCardItem(
    donation: Donation,
    imageModel: Any?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOffer = donation.type == "OFFER"
    val badgeColor = if (isOffer) IhsanTheme.colors.charityOffer else IhsanTheme.colors.charityRequest
    Surface(onClick, modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = IhsanTheme.colors.surfaceElevated, border = BorderStroke(.5.dp, IhsanTheme.colors.borderSubtle)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // First child is on the right in RTL and takes 30 percent of the available width.
            Surface(Modifier.weight(.30f).fillMaxHeight().heightIn(min = 136.dp),
                shape = RoundedCornerShape(12.dp), color = IhsanTheme.colors.surfaceMuted) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
                    Icon(when (donation.category) {
                        "طعام", "غذاء" -> Icons.Default.Restaurant
                        "ملابس" -> Icons.Default.Checkroom
                        "أثاث" -> Icons.Default.Weekend
                        else -> Icons.Default.Category
                    }, "صورة توضيحية للفئة", tint = IhsanTheme.colors.textSecondary, modifier = Modifier.size(28.dp))
                    if (imageModel == null) Text("صورة توضيحية", style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center, color = IhsanTheme.colors.textSecondary)
                    }
                    if (imageModel != null) AsyncImage(imageModel, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                }
            }
            Column(Modifier.weight(.70f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = RoundedCornerShape(12.dp),
                    color = if (isOffer) IhsanTheme.colors.charityOfferContainer else IhsanTheme.colors.charityRequestContainer) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(if (isOffer) Icons.Default.CardGiftcard else Icons.Default.Handshake, null, tint = badgeColor, modifier = Modifier.size(14.dp))
                        Text(stringResource(if (isOffer) R.string.polish_offer_badge else R.string.ehsan_request_label),
                            style = MaterialTheme.typography.labelSmall, color = badgeColor)
                    }
                }
                Text(donation.title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 22.sp), color = IhsanTheme.colors.textPrimary)
                if (donation.description.isNotBlank()) Text(donation.description, style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (donation.location.isNotBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(12.dp), tint = IhsanTheme.colors.textSecondary)
                        Text(donation.location, style = MaterialTheme.typography.labelSmall, color = IhsanTheme.colors.textSecondary)
                    }
                    Text(localizedPublicationDate(donation.createdAt), style = MaterialTheme.typography.labelSmall,
                        color = IhsanTheme.colors.textSecondary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp), modifier = Modifier.weight(1f), colors = com.example.designsystem.theme.ihsanOutlinedButtonColors()) {
                        Text(stringResource(R.string.polish_view_details), style = MaterialTheme.typography.labelSmall)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(14.dp))
                    }
                    if (donation.phoneNumber.isNotBlank()) OutlinedButton(shape = RoundedCornerShape(12.dp), onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(donation.phoneNumber.trim())}"))
                        runCatching { context.startActivity(dialIntent) }
                    }, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp), modifier = Modifier.weight(1f), colors = com.example.designsystem.theme.ihsanOutlinedButtonColors()) {
                        Text(stringResource(R.string.polish_contact), style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.width(3.dp))
                        Icon(Icons.AutoMirrored.Filled.Comment, null, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

/**
 * Floating Action Button for adding offer/help
 */
@Composable
fun AddEhsanFab(
    type: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(if (type == "REQUEST") R.string.polish_request_action else R.string.polish_offer_action)
    Surface(onClick = onClick, modifier = modifier.width(72.dp).heightIn(min = 72.dp),
        shape = CircleShape, color = IhsanTheme.colors.selectedContainer,
        contentColor = IhsanTheme.colors.selectedContent, shadowElevation = 2.dp) {
        Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(if (type == "REQUEST") Icons.Default.Favorite else Icons.Default.Add, null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

/** Local board notice card preserved for safety */
@Composable
fun LocalBoardNoticeCard(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(Icons.Default.Shield, null, tint = IhsanTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
        Text(stringResource(R.string.polish_local_board_body), style = MaterialTheme.typography.labelSmall,
            color = IhsanTheme.colors.textSecondary, modifier = Modifier.weight(1f))
    }
}

@Composable
fun EhsanEmptyContent(
    searchQuery: String,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    val charityEmptyText = stringResource(R.string.polish_charity_empty)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = IhsanTheme.colors.textSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = charityEmptyText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.accentContent,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddEhsanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addCharityText = stringResource(R.string.polish_add_charity)

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(addCharityText, fontWeight = FontWeight.Bold)
    }
}

val EhsanCategoryFilterOptions = listOf("الكل", "طعام", "ملابس", "أثاث", "أخرى")

@Composable
private fun localizedPublicationDate(createdAt: Long): String {
    if (createdAt <= 0L || createdAt > System.currentTimeMillis() + DateUtils.MINUTE_IN_MILLIS) {
        return stringResource(R.string.polish_publication_unavailable)
    }
    val locale = if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) {
        java.util.Locale.forLanguageTag("ar")
    } else androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    return java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
        .format(java.time.Instant.ofEpochMilli(createdAt).atZone(java.time.ZoneId.systemDefault()))
}
