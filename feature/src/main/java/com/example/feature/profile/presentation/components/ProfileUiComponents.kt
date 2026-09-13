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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

// =================================================================================================
// 1. Profile Hero Header with Location, Hijri Date, Brand Mark, Search & Notification icons
//    and a smooth curved bottom transition into the body surface.
// =================================================================================================

@Composable
fun ProfileHeroHeader(
    location: String,
    hijriDate: String,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkTealColor = Color(0xFF003B46)
    val darkTealSubtext = Color(0xFF1B535D)
    val surfaceColor = IhsanTheme.colors.surfaceBase

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        // Hero background image
        Image(
            painter = painterResource(id = R.drawable.ihsan_home_hero_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )

        // Header Top Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // RTL Right: Location & Hijri Date
                Column(horizontalAlignment = Alignment.Start) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_location),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location.ifBlank { "حلب" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkTealColor
                        )
                    }
                    Text(
                        text = hijriDate.ifBlank { "12 ربيع الأول 1448" },
                        fontSize = 10.5.sp,
                        color = darkTealSubtext,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 19.dp)
                    )
                }

                // Center: Brand Logo & Title & Subtitle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_brand_mark),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = stringResource(R.string.home_hero_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkTealColor,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = stringResource(R.string.home_hero_subtitle),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = darkTealSubtext.copy(alpha = 0.85f)
                    )
                }

                // RTL Left: Search & Notification Buttons (Touch Target >= 48dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onSearchClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = "بحث"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_search),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onNotificationClick)
                            .semantics {
                                role = Role.Button
                                contentDescription = "التنبيهات"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_icon_notification),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Curved Bottom Transition Cut
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, h)
                quadraticTo(
                    w / 2f, -h * 0.35f,
                    w, h
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = path, color = surfaceColor)
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
    modifier: Modifier = Modifier
) {
    val initial = profileInitialFromName(userName)
    val darkTealColor = Color(0xFF003B46)

    Box(
        modifier = modifier
            .size(108.dp)
            .semantics { contentDescription = "صورة الملف الشخصي، $userName" },
        contentAlignment = Alignment.Center
    ) {
        // Outer avatar circle with cream/white border and shadow
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(elevation = 3.dp, shape = CircleShape),
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
                if (userName.isNotBlank() && userName != "زائر") {
                    Text(
                        text = initial,
                        color = darkTealColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = darkTealColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Camera Action Badge Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 2.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(darkTealColor)
                .border(2.dp, IhsanTheme.colors.surfaceBase, CircleShape)
                .clickable(onClick = onEditClick)
                .semantics {
                    role = Role.Button
                    contentDescription = "تعديل الملف الشخصي"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
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
    val darkTealColor = Color(0xFF003B46)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "الملف الشخصي",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = darkTealColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "إدارة حسابك وتفضيلاتك",
            fontSize = 13.sp,
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
    val darkTealColor = Color(0xFF003B46)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = IhsanTheme.colors.surfaceElevated,
        border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            // Section Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IhsanTheme.colors.surfaceMint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = darkTealColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = headerTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTealColor
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
    val darkTealColor = Color(0xFF003B46)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "$title، $subtitle"
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Tile Container
        Box(
            modifier = Modifier
                .size(38.dp)
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
                    tint = darkTealColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = darkTealColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = IhsanTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing Content or Navigation Chevron
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
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
    val darkTealColor = Color(0xFF003B46)
    Text(
        text = "Aa",
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = darkTealColor,
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
                    style = MaterialTheme.typography.titleLarge,
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
                Text(text = "تبرعات", fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$requestsCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "طلبات", fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
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
        headerTitle = "إعدادات التطبيق",
        headerIcon = Icons.Default.Settings,
        modifier = modifier
    ) {
        ProfileSettingRow(
            title = "سجل التبرعات",
            subtitle = "عرض جميع تبرعاتك السابقة",
            icon = Icons.Default.History,
            onClick = onDonationHistory
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = "إعدادات التنبيهات",
            subtitle = "تذكير بالأذكار والصلاة",
            icon = Icons.Default.Notifications,
            onClick = onReminders
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = "إعدادات عامة",
            subtitle = "العرض والصوت والإشعارات",
            icon = Icons.Default.Settings,
            onClick = onSettings
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = "الخصوصية والأمان",
            subtitle = "إدارة بياناتك وحسابك",
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
        headerTitle = "الدعم والاستفسار",
        headerIcon = Icons.AutoMirrored.Filled.Chat,
        modifier = modifier
    ) {
        ProfileSettingRow(
            title = "البريد الإلكتروني",
            subtitle = emailLabel,
            icon = Icons.Filled.Email,
            onClick = onEmailClick
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = IhsanTheme.colors.divider)
        ProfileSettingRow(
            title = "تسجيل الخروج",
            subtitle = "تسجيل الخروج من الملف المحلي",
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
    Text(
        text = "إصدار التطبيق $versionLabel",
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
