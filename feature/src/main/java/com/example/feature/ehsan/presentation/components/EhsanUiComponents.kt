package com.example.feature.ehsan.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EhsanTopBar(
    onBack: () -> Unit,
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "إحسان",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(IhsanTheme.dimens.minTouchTarget)
                    .semantics {
                        role = Role.Button
                        contentDescription = "رجوع"
                    }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(IhsanTheme.dimens.minTouchTarget)
                    .semantics {
                        role = Role.Button
                        contentDescription = "الإشعارات"
                    }
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun LocalBoardNoticeCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = CardDefaults.cardColors(containerColor = IhsanTheme.colors.surfaceWarm),
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IhsanTheme.colors.surfaceMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "لوحة إحسان المحلية",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "القوائم محفوظة على جهازك فقط. لا يوجد تحقق رسمي من الحالات، ولا مدفوعات داخل التطبيق، ولا ضمان لتسليم التبرعات عبر المنصة. تواصل مباشرة عبر الهاتف أو واتساب بحدر.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EhsanCommunityHero(
    donorCount: Int,
    completedCount: Int,
    cityCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "مجتمع إحسان، متبرع $donorCount، حالة مكتملة $completedCount، مدينة $cityCount"
            },
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusSheet),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.ic_islamic_pattern_tile),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(110.dp)
                    .padding(8.dp),
                alpha = 0.16f
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F).copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "مجتمع إحسان",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يداً بيد لنشر الخير",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EhsanStatisticTile(
                        icon = Icons.Default.Favorite,
                        value = donorCount.toString(),
                        label = "متبرع",
                        modifier = Modifier.weight(1f)
                    )
                    EhsanStatisticTile(
                        icon = Icons.Default.CheckCircle,
                        value = completedCount.toString(),
                        label = "حالة مكتملة",
                        modifier = Modifier.weight(1f)
                    )
                    EhsanStatisticTile(
                        icon = Icons.Default.LocationOn,
                        value = cityCount.toString(),
                        label = "مدينة",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun EhsanStatisticTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EhsanPrimaryActions(
    onDonateClick: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EhsanPrimaryActionCard(
            title = "أرغب بالتبرع",
            subtitle = "عرض فائض لديك",
            icon = Icons.Default.VolunteerActivism,
            emphasized = true,
            onClick = onDonateClick,
            contentDescription = "أرغب بالتبرع، عرض فائض لديك",
            modifier = Modifier.weight(1f)
        )
        EhsanPrimaryActionCard(
            title = "أحتاج مساعدة",
            subtitle = "اطلب ما ينقصك",
            icon = Icons.Default.Handshake,
            emphasized = false,
            onClick = onHelpClick,
            contentDescription = "أحتاج مساعدة، اطلب ما ينقصك",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EhsanPrimaryActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    emphasized: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val container = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val content = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }
    Card(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 108.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = if (emphasized) null else BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasized) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
            Text(
                text = title,
                color = content,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = content.copy(alpha = 0.75f),
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = content.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun EhsanSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "بحث في فرص إحسان" },
        placeholder = {
            Text(
                text = "ابحث عن عرض، مدينة، أو محتاج...",
                fontSize = 14.sp,
                color = IhsanTheme.colors.textSecondaryMuted
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondaryMuted
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = IhsanTheme.colors.borderSubtle
        )
    )
}

@Composable
fun EhsanFilterSection(
    label: String,
    labelIcon: ImageVector,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = labelIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(options) { option ->
                EhsanFilterChip(
                    label = option,
                    selected = selected == option,
                    onClick = { onSelected(option) }
                )
            }
        }
    }
}

@Composable
fun EhsanFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(shape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else IhsanTheme.colors.borderSubtle,
                shape = shape
            )
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = if (selected) "$label، محدد" else label
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else IhsanTheme.colors.textSecondaryMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun EhsanListingTabs(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val index = when (selectedType) {
        "OFFER" -> 1
        "REQUEST" -> 2
        else -> 0
    }
    TabRow(
        selectedTabIndex = index,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[index]),
                color = MaterialTheme.colorScheme.primary,
                height = 3.dp
            )
        }
    ) {
        listOf(
            "ALL" to "الكل",
            "OFFER" to "عروض تبرع",
            "REQUEST" to "طلبات مساعدة"
        ).forEach { (type, label) ->
            val selected = selectedType == type
            Tab(
                selected = selected,
                onClick = { onTypeSelected(type) },
                text = {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else IhsanTheme.colors.textSecondaryMuted,
                        maxLines = 1
                    )
                }
            )
        }
    }
}

@Composable
fun EhsanEmptyContent(
    searchQuery: String,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    val title: String
    val subtitle: String
    val icon: ImageVector
    when {
        searchQuery.isNotBlank() -> {
            title = "لا توجد نتائج لـ \"$searchQuery\""
            subtitle = "جرّب كلمات أخرى أو أزل البحث."
            icon = Icons.Default.SearchOff
        }
        hasActiveFilters -> {
            title = "لا توجد فرص مطابقة للفلاتر"
            subtitle = "جرّب تغيير الفلاتر أو أضف فرصة جديدة."
            icon = Icons.Default.VolunteerActivism
        }
        else -> {
            title = "لا توجد فرص إحسان متاحة حالياً"
            subtitle = "جرّب تغيير الفلاتر أو أضف فرصة جديدة."
            icon = Icons.Default.VolunteerActivism
        }
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp)
            .semantics { contentDescription = "$title. $subtitle" },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(IhsanTheme.colors.quickActionSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondaryMuted,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = IhsanTheme.colors.textSecondaryMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddEhsanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = IhsanTheme.dimens.minTouchTarget)
            .semantics {
                role = Role.Button
                contentDescription = "إضافة إحسان"
            },
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text("إضافة إحسان", fontWeight = FontWeight.Bold)
    }
}

/** Existing city filter options shown by the current screen (unchanged set). */
val EhsanCityFilterOptions = listOf("الكل", "حلب", "دمشق", "حمص", "حماة")

/** Existing category filter options shown by the current screen (unchanged set). */
val EhsanCategoryFilterOptions = listOf("الكل", "طعام", "ملابس", "أثاث", "أخرى")
