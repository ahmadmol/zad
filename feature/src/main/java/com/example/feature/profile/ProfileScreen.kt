package com.example.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.components.AuthBottomSheet
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.profile.presentation.ProfileViewModel
import com.example.feature.profile.presentation.components.FontIconAa
import com.example.feature.profile.presentation.components.ProfileAvatarOverlay
import com.example.feature.profile.presentation.components.ProfileGroupedSection
import com.example.feature.profile.presentation.components.ProfileHeroHeader
import com.example.feature.profile.presentation.components.ProfileSettingRow
import com.example.feature.profile.presentation.components.ProfileTitleHeader
import com.example.feature.profile.presentation.components.ProfileVersionText
import org.koin.androidx.compose.koinViewModel

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
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }
    var showLocalDataDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val appVersion = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName
        }.getOrNull().orEmpty().ifBlank { "1.0.0" }
    }

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

                // 2. Avatar Overlay Centered on the Curved Boundary
                Box(
                    modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // 4. Section 1 — الحساب
                        ProfileGroupedSection(
                            headerTitle = "الحساب",
                            headerIcon = Icons.Default.Person
                        ) {
                            ProfileSettingRow(
                                title = "تحرير الملف الشخصي",
                                subtitle = "تحديث معلوماتك وصورتك",
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
                                title = "سجل المساهمات",
                                subtitle = "اطلع على تاريخ تبرعاتك وإحسانك",
                                icon = Icons.Default.FavoriteBorder,
                                onClick = onNavigateToDonationHistory
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 5. Section 2 — المظهر والتفضيلات
                        ProfileGroupedSection(
                            headerTitle = "المظهر والتفضيلات",
                            headerIcon = Icons.Default.Settings
                        ) {
                            ProfileSettingRow(
                                title = "جميع الإعدادات",
                                subtitle = "تخصيص المظهر والإشعارات والموقع والبيانات",
                                icon = Icons.Default.Settings,
                                onClick = onNavigateToSettings
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = "الوضع الداكن",
                                subtitle = "تغيير مظهر التطبيق",
                                icon = Icons.Default.DarkMode,
                                onClick = { viewModel.setDarkMode(!uiState.isDarkMode) },
                                trailingContent = {
                                    Switch(
                                        checked = uiState.isDarkMode,
                                        onCheckedChange = { viewModel.setDarkMode(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = IhsanTheme.colors.textSecondary,
                                            uncheckedTrackColor = IhsanTheme.colors.surfaceMint
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
                                title = "حجم نص المعاينة",
                                subtitle = "يؤثر على نموذج النص في نافذة المعاينة فقط",
                                customIconComposable = { FontIconAa() },
                                onClick = { showFontSizeDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 6. Section 3 — الصلاة والقرآن
                        ProfileGroupedSection(
                            headerTitle = "الصلاة والقرآن",
                            headerIcon = Icons.Default.Mosque
                        ) {
                            ProfileSettingRow(
                                title = "إعدادات الصلاة",
                                subtitle = "تنبيهات الأذان وأوقات الصلاة",
                                icon = Icons.Default.Notifications,
                                onClick = onNavigateToPrayer
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = "القرآن الكريم",
                                subtitle = "فتح القرآن والقراءة المحفوظة",
                                icon = Icons.Default.Download,
                                onClick = onNavigateToQuran
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 7. Section 4 — الخصوصية والبيانات
                        ProfileGroupedSection(
                            headerTitle = "الخصوصية والبيانات",
                            headerIcon = Icons.Default.Shield
                        ) {
                            ProfileSettingRow(
                                title = "البيانات المحلية",
                                subtitle = "إدارة بياناتك على الجهاز (نشاطك المحلي)",
                                icon = Icons.Default.Storage,
                                onClick = { showLocalDataDialog = true }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = IhsanTheme.colors.divider
                            )
                            ProfileSettingRow(
                                title = "حول التطبيق",
                                subtitle = "الإصدار وسياسة الخصوصية",
                                icon = Icons.Default.Info,
                                onClick = { showAboutDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ProfileVersionText(versionLabel = appVersion)

                        // Bottom content breathing room for bottom bar
                        Spacer(modifier = Modifier.height(96.dp))
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
                AlertDialog(
                    onDismissRequest = { showFontSizeDialog = false },
                    title = { Text("حجم نص المعاينة") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "حجم نص المعاينة: ${currentSize.toInt()} نقطة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IhsanTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "بسم الله الرحمن الرحيم",
                                fontSize = currentSize.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Slider(
                                value = currentSize,
                                onValueChange = { currentSize = it },
                                valueRange = 16f..36f,
                                steps = 10
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.setFontSize(currentSize)
                                showFontSizeDialog = false
                            }
                        ) {
                            Text("حفظ")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showFontSizeDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            // Local Data Management Dialog
            if (showLocalDataDialog) {
                AlertDialog(
                    onDismissRequest = { showLocalDataDialog = false },
                    title = { Text("البيانات المحلية") },
                    text = {
                        Column {
                            Text(
                                text = "تطبيق إحسان يحفظ بيانات ملفك الشخصي ونشاطك المحلي وتفضيلاتك على هذا الجهاز فقط لحماية خصوصيتك.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IhsanTheme.colors.textPrimary
                            )
                            if (uiState.isUserLoggedIn) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "يمكنك تسجيل الخروج لإلغاء القيد المحلي على هذا الجهاز.",
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
                            ) {
                                Text("تسجيل الخروج", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            TextButton(onClick = { showLocalDataDialog = false }) {
                                Text("موافق")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocalDataDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }

            // About App & Privacy Policy Dialog
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("حول تطبيق إحسان") },
                    text = {
                        Column {
                            Text(
                                text = "إحسان - تطبيق إسلامي شامل للقرآن الكريم، الأذكار، أوقات الصلاة، والعمل الخيري.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IhsanTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "الإصدار: $appVersion",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "سياسة الخصوصية: جميع البيانات محلية ومحفوظة بأمان على جهازك، ولا يتم مشاركتها مع أي جهة خارجية.",
                                style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }
        }
    }
}
