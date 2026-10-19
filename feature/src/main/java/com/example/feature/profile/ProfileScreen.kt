package com.example.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R
import com.example.feature.components.AuthBottomSheet
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.ehsan.data.image.EhsanImageStore
import com.example.feature.profile.presentation.ProfileViewModel
import com.example.feature.profile.presentation.components.FontIconAa
import com.example.feature.profile.presentation.components.ProfileAvatarOverlay
import com.example.feature.profile.presentation.components.ProfileGroupedSection
import com.example.feature.profile.presentation.components.ProfileHeroHeader
import com.example.feature.profile.presentation.components.ProfileSettingRow
import com.example.feature.profile.presentation.components.ProfileTitleHeader
import com.example.feature.profile.presentation.components.ProfileVersionText
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNavigateToDonationHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToQuran: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onManageListings: (String) -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val accountSectionText = stringResource(R.string.polish_account_section)
    val editProfileText = stringResource(R.string.polish_edit_profile)
    val editProfileSubtitleText = stringResource(R.string.polish_edit_profile_subtitle)
    val contributionHistoryTitleText = stringResource(R.string.polish_contribution_history_title)
    val appearanceSectionText = stringResource(R.string.polish_appearance_section)
    val allSettingsText = stringResource(R.string.polish_all_settings)
    val allSettingsSubtitleText = stringResource(R.string.polish_all_settings_subtitle)
    val darkModeText = stringResource(R.string.polish_dark_mode)
    val darkModeSubtitleText = stringResource(R.string.polish_dark_mode_subtitle)
    val previewSizeText = stringResource(R.string.polish_preview_size)
    val previewSizeSubtitleText = stringResource(R.string.polish_preview_size_subtitle)
    val prayerQuranSectionText = stringResource(R.string.polish_prayer_quran_section)
    val prayerSettingsText = stringResource(R.string.polish_prayer_settings)
    val prayerSettingsSubtitleText = stringResource(R.string.polish_prayer_settings_subtitle)
    val readingSettingsText = stringResource(R.string.polish_reading_settings)
    val readingSettingsSubtitleText = stringResource(R.string.polish_reading_settings_subtitle)
    val privacySectionText = stringResource(R.string.polish_privacy_section)
    val localDataText = stringResource(R.string.polish_local_data)
    val localDataSubtitleText = stringResource(R.string.polish_local_data_subtitle)
    val aboutAppText = stringResource(R.string.polish_about_app)
    val aboutAppSubtitleText = stringResource(R.string.polish_about_app_subtitle)
    val previewBismillahText = stringResource(R.string.polish_preview_bismillah)
    val currentSizeText = stringResource(R.string.polish_current_size)
    val cancelText = stringResource(R.string.polish_cancel)
    val saveText = stringResource(R.string.polish_save)
    val localProfileInfoText = stringResource(R.string.polish_local_profile_info)
    val localLogoutInfoText = stringResource(R.string.polish_local_logout_info)
    val logoutText = stringResource(R.string.polish_logout)
    val closeText = stringResource(R.string.polish_close)
    val aboutTitleText = stringResource(R.string.polish_about_title)
    val aboutDescriptionText = stringResource(R.string.polish_about_description)
    val privacyDescriptionText = stringResource(R.string.polish_privacy_description)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imageStore: EhsanImageStore = koinInject()
    val avatarModel = remember(uiState.avatarUrl) {
        imageStore.resolve(uiState.avatarUrl)
    }
    var showAuthSheet by remember { mutableStateOf(false) }
    var showLocalDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val appVersion = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName
        }.getOrNull().orEmpty().ifBlank { "1.0.0" }
    }

    val aboutVersionText = stringResource(R.string.polish_about_version , appVersion)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IhsanTheme.colors.surfaceBase
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                // 1. Status Bar + Hero Header with Curved Transition
                ProfileHeroHeader(
                    location = uiState.city,
                    hijriDate = HijriDateFormatter.nowFormatted(),
                    onSearchClick = onNavigateToSearch,
                    onNotificationClick = onNavigateToReminders
                )

                // 2. Avatar straddles the hero/body boundary.
                // 2. Avatar Overlay Centered on the Curved Boundary
                Box(
                    modifier = Modifier.fillMaxWidth().layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val overlap = 48.dp.roundToPx()
                        layout(placeable.width, (placeable.height - overlap).coerceAtLeast(0)) {
                            placeable.placeRelative(0, -overlap)
                        }
                    },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        ProfileAvatarOverlay(
                            userName = uiState.userName,
                            avatarModel = avatarModel,
                            onEditClick = {
                                if (uiState.isUserLoggedIn) {
                                    onEditProfileClick()
                                } else {
                                    showAuthSheet = true
                                }
                            },
                            modifier = Modifier.padding(top = 0.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Title & Subtitle
                        ProfileTitleHeader()

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Section 1 — الحساب
                        ProfileGroupedSection(
                            headerTitle = accountSectionText,
                            headerIcon = Icons.Default.Person
                        ) {
                            ProfileSettingRow(
                                title = editProfileText,
                                subtitle = editProfileSubtitleText,
                                icon = Icons.Default.Edit,
                                onClick = {
                                    if (uiState.isUserLoggedIn) {
                                        onEditProfileClick()
                                    } else {
                                        showAuthSheet = true
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = contributionHistoryTitleText,
                                subtitle = stringResource(R.string.polish_contribution_history),
                                icon = Icons.Default.FavoriteBorder,
                                onClick = onNavigateToDonationHistory
                            )
                            ProfileSettingRow("عروضي", "إدارة عروض التبرع", icon = Icons.Default.FavoriteBorder,
                                onClick = { onManageListings("OFFER") })
                            ProfileSettingRow("طلباتي", "إدارة طلبات المساعدة", icon = Icons.Default.Person,
                                onClick = { onManageListings("REQUEST") })
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Section 2 — المظهر والتفضيلات
                        ProfileGroupedSection(
                            headerTitle = appearanceSectionText,
                            headerIcon = Icons.Default.Settings
                        ) {
                            ProfileSettingRow(
                                title = allSettingsText,
                                subtitle = allSettingsSubtitleText,
                                icon = Icons.Default.Settings,
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = darkModeText,
                                subtitle = darkModeSubtitleText,
                                icon = Icons.Default.DarkMode,
                                onClick = { viewModel.setDarkMode(!uiState.isDarkMode) },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.isDarkMode,
                                        onCheckedChange = { viewModel.setDarkMode(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = IhsanTheme.colors.textSecondary,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = previewSizeText,
                                subtitle = previewSizeSubtitleText,
                                customIconComposable = { FontIconAa() },
                                onClick = { showFontSizeDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 6. Section 3 — الصلاة والقرآن
                        ProfileGroupedSection(
                            headerTitle = prayerQuranSectionText,
                            headerIcon = Icons.Default.Mosque
                        ) {
                            ProfileSettingRow(
                                title = prayerSettingsText,
                                subtitle = prayerSettingsSubtitleText,
                                icon = Icons.Default.Notifications,
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = readingSettingsText,
                                subtitle = readingSettingsSubtitleText,
                                icon = Icons.Default.TextFields,
                                onClick = { showFontSizeDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 7. Section 4 — الخصوصية والبيانات
                        ProfileGroupedSection(
                            headerTitle = privacySectionText,
                            headerIcon = Icons.Default.Shield
                        ) {
                            ProfileSettingRow(
                                title = localDataText,
                                subtitle = localDataSubtitleText,
                                icon = Icons.Default.Storage,
                                onClick = { showLocalDataDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = aboutAppText,
                                subtitle = aboutAppSubtitleText,
                                icon = Icons.Default.Info,
                                onClick = { showAboutDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ProfileVersionText(versionLabel = appVersion)

                        // Bottom content breathing room for bottom bar
                        Spacer(modifier = Modifier.height(IhsanTheme.dimens.contentBottomSpacing))
                    }
                }
            }

            // Auth Bottom Sheet
            if (showAuthSheet) {
                AuthBottomSheet(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = { showAuthSheet = false }
                )
            }

            // Font Size Adjustment Dialog
            if (showFontSizeDialog) {
                var currentSize by remember { mutableFloatStateOf(uiState.fontSize) }
                Dialog(
                    onDismissRequest = { showFontSizeDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(28.dp)),
                        color = IhsanTheme.colors.surfaceElevated,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Dialog Title & Badge Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = IhsanTheme.colors.surfaceMint,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        FontIconAa()
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = previewSizeText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.textPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Dynamic Preview Box Card
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = IhsanTheme.colors.surfaceMint.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                                ) {
                                    Text(
                                        text = previewBismillahText,
                                        fontSize = currentSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IhsanTheme.colors.accentContent,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Font Size Info Row with Pill Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentSizeText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IhsanTheme.colors.textSecondary
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = IhsanTheme.colors.surfaceMuted
                                ) {
                                    Text(
                                        text = "${currentSize.toInt()} نقطة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IhsanTheme.colors.accentContent,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Clean Continuous Slider with Start/End Scale Labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "A",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.textSecondary
                                )
                                Slider(
                                    value = currentSize,
                                    onValueChange = { currentSize = it },
                                    valueRange = 16f..36f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                        activeTickColor = Color.Transparent,
                                        inactiveTickColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                Text(
                                    text = "A",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IhsanTheme.colors.accentContent
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Modern Side-By-Side Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Cancel Button
                                Surface(
                                    onClick = { showFontSizeDialog = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = IhsanTheme.colors.surfaceMint,
                                    border = BorderStroke(1.dp, IhsanTheme.colors.borderSubtle)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = cancelText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IhsanTheme.colors.textSecondary
                                        )
                                    }
                                }

                                // Save Button
                                Surface(
                                    onClick = {
                                        viewModel.setFontSize(currentSize)
                                        showFontSizeDialog = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = IhsanTheme.colors.accentContent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = saveText,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Local Data Management Dialog
            if (showLocalDataDialog) {
                AlertDialog(
                    onDismissRequest = { showLocalDataDialog = false },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = IhsanTheme.colors.surfaceElevated,
                    titleContentColor = IhsanTheme.colors.textPrimary,
                    textContentColor = IhsanTheme.colors.textPrimary,
                    title = {
                        Text(
                            text = localDataText,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = IhsanTheme.colors.textPrimary
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = localProfileInfoText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = IhsanTheme.colors.textPrimary
                            )
                            if (uiState.isUserLoggedIn) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = localLogoutInfoText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IhsanTheme.colors.textSecondary
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (uiState.isUserLoggedIn) {
                            TextButton(
                                onClick = {
                                    viewModel.logout()
                                    showLocalDataDialog = false
                                }
                            , colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                                Text(logoutText, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            TextButton(onClick = { showLocalDataDialog = false }, colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                                Text(stringResource(R.string.polish_ok), fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocalDataDialog = false }, colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                            Text(closeText, fontWeight = FontWeight.Medium)
                        }
                    }
                )
            }

            if (showSourcesDialog) com.example.feature.core.presentation.ContentAttributionsDialog { showSourcesDialog = false }

            // About App & Privacy Policy Dialog
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    shape = RoundedCornerShape(28.dp),
                    containerColor = IhsanTheme.colors.surfaceElevated,
                    titleContentColor = IhsanTheme.colors.textPrimary,
                    textContentColor = IhsanTheme.colors.textPrimary,
                    title = {
                        Text(
                            text = aboutTitleText,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = IhsanTheme.colors.textPrimary
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            TextButton(onClick = { showAboutDialog = false; showSourcesDialog = true }) { Text("المصادر والتراخيص") }
                            Text(
                                text = aboutDescriptionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = IhsanTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = aboutVersionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = IhsanTheme.colors.selectedContent
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = privacyDescriptionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }, colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                            Text(closeText, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}
