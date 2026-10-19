package com.example.feature.profile.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.designsystem.component.IhsanHeroScrim
import com.example.designsystem.component.IhsanHeroTopRow
import com.example.designsystem.component.IhsanTextWithKaabaIcon
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

// =================================================================================================
// 1. Profile Hero Header with Location, Hijri Date, Brand Mark, Search & Notification icons
//    and a smooth curved bottom transition into the body surface.
// =================================================================================================

@Composable
fun ProfileHeroHeader(
    location: String,
    hijriDate: String?,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val foreground = IhsanTheme.colors.onBrand
    Box(modifier.fillMaxWidth()) {
        Image(painterResource(R.drawable.ihsan_home_hero_background), null,
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter, modifier = Modifier.matchParentSize())
        IhsanHeroScrim(Modifier.matchParentSize())
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp).padding(top = 2.dp, bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IhsanHeroTopRow(location, hijriDate.orEmpty(), onSearchClick, onNotificationClick)
        }
        val surfaceColor = IhsanTheme.colors.surfaceBase
        Canvas(Modifier.fillMaxWidth().height(32.dp).align(Alignment.BottomCenter)) {
            val path = Path().apply {
                moveTo(0f, size.height)
                quadraticTo(size.width / 2, -size.height * 0.35f, size.width, size.height)
                lineTo(size.width, size.height); lineTo(0f, size.height); close()
            }
            drawPath(path, surfaceColor)
        }
    }
}

// =================================================================================================
// 2. Profile Avatar Overlay — Centered over the curved transition
// =================================================================================================

@Composable
fun ProfileAvatarOverlay(
    userName: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarModel: Any? = null
) {
    val avatarUserDescriptionText = stringResource(R.string.polish_avatar_user_description , userName)
    val avatarDescriptionText = stringResource(R.string.polish_avatar_description)
    val editProfileActionText = stringResource(R.string.polish_edit_profile_action)

    val isDark = IhsanTheme.isDark
    val initial = profileInitialFromName(userName)
    var avatarFailed by remember(avatarModel) { mutableStateOf(false) }
    val iconColor = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.brand
    val badgeBg = if (isDark) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.brand
    val badgeIconTint = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.onBrand

    Box(
        modifier = modifier
            .size(108.dp)
            .semantics { contentDescription = avatarUserDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        // Outer avatar circle with cream/white border and shadow
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(elevation = 1.dp, shape = CircleShape),
            shape = CircleShape,
            color = IhsanTheme.colors.surfaceElevated,
            border = BorderStroke(3.5.dp, IhsanTheme.colors.surfaceBase)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(IhsanTheme.colors.surfaceMint)
            ) {
                if (avatarModel != null && !avatarFailed) {
                    AsyncImage(
                        model = avatarModel,
                        onError = { avatarFailed = true },
                        placeholder = painterResource(R.drawable.polish_avatar_placeholder),
                        error = painterResource(R.drawable.polish_avatar_placeholder),
                        contentDescription = avatarDescriptionText,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else if (userName.isNotBlank() && userName != "زائر") {
                    Text(
                        text = initial,
                        color = iconColor,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        IconButton(onClick = onEditClick, modifier = Modifier.align(Alignment.BottomEnd).size(48.dp)) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(badgeBg)
                .border(1.dp, IhsanTheme.colors.surfaceBase, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CameraAlt, editProfileActionText, tint = badgeIconTint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// =================================================================================================
// 3. Profile Title Header — Centered below avatar
// =================================================================================================

@Composable
fun ProfileTitleHeader(
    modifier: Modifier = Modifier
) {
    val profileTitleText = stringResource(R.string.polish_profile_title)
    val profileSubtitleText = stringResource(R.string.polish_profile_subtitle)

    val isDark = IhsanTheme.isDark
    val titleColor = if (isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.brand
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = profileTitleText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = profileSubtitleText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = IhsanTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// =================================================================================================
// 4. Grouped Section Card Container
// =================================================================================================

@Composable
fun ProfileGroupedSection(
    headerTitle: String,
    headerIcon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = IhsanTheme.isDark
    val titleColor = if (isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.brand
    val iconColor = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.brand

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusLarge),
        color = IhsanTheme.colors.surfaceElevated,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 2.dp)) {
            // Section Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IhsanTheme.colors.surfaceMint)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IhsanTheme.colors.surfaceMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = headerTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = IhsanTheme.colors.divider
            )

            content()
        }
    }
}

// =================================================================================================
// 5. Profile Setting Row — Consistent layout matching reference image
// =================================================================================================

@Composable
fun ProfileSettingRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    customIconComposable: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val settingDescriptionText = stringResource(R.string.polish_setting_description , title, subtitle)

    val isDark = IhsanTheme.isDark
    val titleColor = if (isDark) IhsanTheme.colors.textPrimary else IhsanTheme.colors.brand
    val iconColor = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.brand

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = settingDescriptionText
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Tile Container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(IhsanTheme.colors.surfaceMint),
            contentAlignment = Alignment.Center
        ) {
            if (customIconComposable != null) {
                customIconComposable()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title and Subtitle Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = IhsanTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing Content or Navigation Chevron
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// =================================================================================================
// 6. Font Size 'Aa' Icon Composable for "حجم الخط" Row
// =================================================================================================

@Composable
fun FontIconAa(
    modifier: Modifier = Modifier
) {
    val isDark = IhsanTheme.isDark
    val iconColor = if (isDark) IhsanTheme.colors.selectedContent else IhsanTheme.colors.brand
    Text(
        text = "Aa",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = iconColor,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

// =================================================================================================
// Backward Compatibility / Existing Components Bridge
// =================================================================================================

@Composable
fun ProfileTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = IhsanTheme.colors.surfaceBase
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .defaultMinSize(minHeight = IhsanTheme.dimens.minTouchTarget),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = IhsanTheme.colors.textPrimary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IhsanTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userPhone: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileAvatarOverlay(
        userName = userName,
        onEditClick = onEditClick,
        modifier = modifier
    )
}

@Composable
fun ProfileAvatar(
    initial: String,
    contentDescription: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileAvatarOverlay(
        userName = initial,
        onEditClick = onEditClick,
        modifier = modifier
    )
}

@Composable
fun ProfileImpactCard(
    donationsCount: Int,
    requestsCount: Int,
    levelLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusLarge),
        color = IhsanTheme.colors.surfaceElevated,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$donationsCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = stringResource(R.string.polish_donations_count_label), fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$requestsCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = stringResource(R.string.polish_requests_count_label), fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
fun ProfileSettingsCard(
    onDonationHistory: () -> Unit,
    onReminders: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProfileGroupedSection(
        headerTitle = stringResource(R.string.polish_app_settings_section),
        headerIcon = Icons.Default.Settings,
        modifier = modifier
    ) {
        ProfileSettingRow(
            title = stringResource(R.string.polish_donation_history),
            subtitle = stringResource(R.string.polish_donation_history_subtitle),
            icon = Icons.Default.History,
            onClick = onDonationHistory
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = stringResource(R.string.polish_notification_settings),
            subtitle = stringResource(R.string.polish_notification_settings_subtitle),
            icon = Icons.Default.Notifications,
            onClick = onReminders
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = stringResource(R.string.polish_general_settings),
            subtitle = stringResource(R.string.polish_general_settings_subtitle),
            icon = Icons.Default.Settings,
            onClick = onSettings
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = stringResource(R.string.polish_privacy_security),
            subtitle = stringResource(R.string.polish_privacy_security_subtitle),
            icon = Icons.Default.Shield,
            onClick = onPrivacy
        )
    }
}

@Composable
fun ProfileSupportCard(
    onEmailClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onHelp: () -> Unit,
    onLogout: () -> Unit,
    emailLabel: String,
    whatsappLabel: String,
    modifier: Modifier = Modifier
) {
    ProfileGroupedSection(
        headerTitle = stringResource(R.string.polish_support_section),
        headerIcon = Icons.AutoMirrored.Filled.Chat,
        modifier = modifier
    ) {
        ProfileSettingRow(
            title = stringResource(R.string.polish_email),
            subtitle = emailLabel,
            icon = Icons.Filled.Email,
            onClick = onEmailClick
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = stringResource(R.string.polish_logout),
            subtitle = stringResource(R.string.polish_local_logout_subtitle),
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogout
        )
    }
}

@Composable
fun ProfileVersionText(
    versionLabel: String,
    modifier: Modifier = Modifier
) {
    val versionFormatText = stringResource(R.string.polish_version_format , versionLabel)

    Text(
        text = versionFormatText,
        style = MaterialTheme.typography.labelSmall,
        color = IhsanTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

private fun profileInitialFromName(userName: String): String {
    val trimmed = userName.trim()
    val letter = trimmed.firstOrNull { it.isLetter() }
    return letter?.uppercaseChar()?.toString() ?: "؟"
}
