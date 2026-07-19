package com.example.feature.profile.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R
import com.example.designsystem.theme.IhsanTheme

@Composable
fun ProfileTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_islamic_pattern_tile),
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.55f
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .defaultMinSize(minHeight = IhsanTheme.dimens.minTouchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(IhsanTheme.dimens.minTouchTarget)
                    .semantics {
                        role = Role.Button
                        contentDescription = "رجوع"
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.size(IhsanTheme.dimens.minTouchTarget))
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    userPhone: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initial = profileInitialFromName(userName)
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileAvatar(
            initial = initial,
            contentDescription = "صورة الملف الشخصي، $userName",
            onEditClick = onEditClick
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (userPhone.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = userPhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    initial: String,
    contentDescription: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(onClick = onEditClick)
                .semantics {
                    role = Role.Button
                    this.contentDescription = "تعديل الملف الشخصي"
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ProfileImpactCard(
    donationsCount: Int,
    requestsCount: Int,
    levelLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "نشاطك المحلي، تبرع $donationsCount، طلب $requestsCount"
            },
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusSheet),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ic_islamic_pattern_tile),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(120.dp)
                    .padding(8.dp),
                contentScale = ContentScale.Fit,
                alpha = 0.18f
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مستوى التأثير",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = levelLabel,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileStatItem(value = donationsCount.toString(), label = "تبرع")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f))
                    )
                    ProfileStatItem(value = requestsCount.toString(), label = "طلب")
                }
            }
        }
    }
}

@Composable
fun ProfileStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
            fontSize = 11.sp
        )
    }
}

@Composable
fun ProfileSettingsCard(
    onDonationHistory: () -> Unit,
    onReminders: () -> Unit,
    onSettings: () -> Unit,
    onLanguage: () -> Unit,
    onPrivacy: () -> Unit,
    languageSubtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProfileMenuRow(
                icon = Icons.Default.History,
                title = "سجل التبرعات",
                onClick = onDonationHistory
            )
            ProfileMenuDivider()
            ProfileMenuRow(
                icon = Icons.Default.Notifications,
                title = "إعدادات التنبيهات",
                onClick = onReminders
            )
            ProfileMenuDivider()
            ProfileMenuRow(
                icon = Icons.Default.Settings,
                title = "إعدادات التطبيق",
                onClick = onSettings
            )
            ProfileMenuDivider()
            ProfileMenuRow(
                icon = Icons.Default.Language,
                title = "اللغة",
                subtitle = languageSubtitle,
                onClick = onLanguage
            )
            ProfileMenuDivider()
            ProfileMenuRow(
                icon = Icons.Default.Shield,
                title = "الخصوصية والأمان",
                onClick = onPrivacy
            )
        }
    }
}

@Composable
fun ProfileSupportCard(
    onHelp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IhsanTheme.dimens.radiusPill),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            ProfileMenuRow(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "مركز المساعدة",
                onClick = onHelp
            )
            ProfileMenuDivider()
            ProfileMenuRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "تسجيل الخروج",
                onClick = onLogout,
                titleColor = MaterialTheme.colorScheme.error,
                iconTint = MaterialTheme.colorScheme.error,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
                showChevron = true
            )
        }
    }
}

@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBackground: Color = IhsanTheme.colors.surfaceMint,
    showChevron: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (subtitle != null) "$title، $subtitle" else title
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = IhsanTheme.colors.textSecondaryMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = IhsanTheme.colors.textSecondaryMuted,
                modifier = Modifier.size(20.dp)
            )
        }
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
        color = IhsanTheme.colors.textSecondaryMuted,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = IhsanTheme.colors.borderSubtle
    )
}

private fun profileInitialFromName(userName: String): String {
    val trimmed = userName.trim()
    val letter = trimmed.firstOrNull { it.isLetter() }
    return letter?.uppercaseChar()?.toString() ?: "؟"
}
