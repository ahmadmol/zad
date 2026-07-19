package com.example.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.components.AuthBottomSheet
import com.example.feature.core.notification.UserMessageNotifier
import com.example.feature.profile.presentation.ProfileViewModel
import com.example.feature.profile.presentation.components.ProfileHeader
import com.example.feature.profile.presentation.components.ProfileImpactCard
import com.example.feature.profile.presentation.components.ProfileSettingsCard
import com.example.feature.profile.presentation.components.ProfileSupportCard
import com.example.feature.profile.presentation.components.ProfileTopBar
import com.example.feature.profile.presentation.components.ProfileVersionText
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onNavigateToDonationHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAuthSheet by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val appVersion = remember(context) {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.versionName
        }.getOrNull().orEmpty().ifBlank { "—" }
    }

    val donationsCount = remember(uiState.myDonations) {
        uiState.myDonations.count { it.type == "OFFER" }
    }
    val requestsCount = remember(uiState.myDonations) {
        uiState.myDonations.count { it.type == "REQUEST" }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = IhsanTheme.colors.surfaceMuted
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ProfileTopBar(
                    title = "الملف الشخصي",
                    onBackClick = onBackClick
                )

                when {
                    uiState.isLoading && !uiState.isUserLoggedIn -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    !uiState.isUserLoggedIn -> {
                        MissingLocalProfileContent(
                            onCreateProfile = { showAuthSheet = true },
                            appVersion = appVersion
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileHeader(
                                userName = uiState.userName,
                                userPhone = uiState.userPhone,
                                onEditClick = onEditProfileClick
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            ProfileImpactCard(
                                donationsCount = donationsCount,
                                requestsCount = requestsCount,
                                levelLabel = "نشاطك المحلي"
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            ProfileSettingsCard(
                                onDonationHistory = onNavigateToDonationHistory,
                                onReminders = onNavigateToReminders,
                                onSettings = onNavigateToSettings,
                                onLanguage = { showLanguageDialog = true },
                                onPrivacy = { showPrivacyDialog = true },
                                languageSubtitle = "العربية"
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            ProfileSupportCard(
                                onHelp = { showHelpDialog = true },
                                onLogout = { viewModel.logout() }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ProfileVersionText(versionLabel = appVersion)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            if (showAuthSheet) {
                AuthBottomSheet(
                    onDismiss = { showAuthSheet = false },
                    onAuthSuccess = { showAuthSheet = false }
                )
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text("اللغة") },
                    text = {
                        Text("اللغة الحالية للتطبيق هي العربية. سيتم إضافة لغات أخرى في تحديث قادم.")
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text("حسناً")
                        }
                    }
                )
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("الخصوصية والأمان") },
                    text = {
                        Text(
                            "نحترم خصوصيتك. بيانات الملف والتبرعات تُحفظ محليًا على جهازك قدر الإمكان، " +
                                "ولا نشارك معلوماتك الشخصية مع أطراف ثالثة دون موافقتك."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("موافق")
                        }
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("مركز المساعدة") },
                    text = {
                        Text(
                            "للدعم والاستفسارات تواصل معنا عبر البريد:\nsupport@ihsan.app\n\n" +
                                "أو من خلال إعدادات التطبيق وقسم التنبيهات."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showHelpDialog = false
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:support@ihsan.app")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "دعم تطبيق إحسان")
                                }
                                runCatching { context.startActivity(intent) }
                                    .onFailure {
                                        UserMessageNotifier.notify(
                                            context,
                                            "تعذر فتح تطبيق البريد"
                                        )
                                    }
                            }
                        ) {
                            Text("مراسلة الدعم")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MissingLocalProfileContent(
    onCreateProfile: () -> Unit,
    appVersion: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f, fill = true))
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = IhsanTheme.colors.quickActionSurface
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = IhsanTheme.colors.textSecondaryMuted
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "أنشئ ملفاً شخصياً على الجهاز لإدارة تبرعاتك ومتابعة طلباتك. البيانات محلية وليست حساباً عبر الإنترنت",
            textAlign = TextAlign.Center,
            color = IhsanTheme.colors.textSecondaryMuted,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onCreateProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("إنشاء / فتح ملف شخصي", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f, fill = true))
        ProfileVersionText(versionLabel = appVersion)
    }
}

@Composable
private fun ProfileContentPreviewBody(
    userName: String,
    userPhone: String,
    donations: Int,
    requests: Int
) {
    IhsanTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = IhsanTheme.colors.surfaceMuted
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProfileTopBar(title = "الملف الشخصي", onBackClick = {})
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileHeader(
                            userName = userName,
                            userPhone = userPhone,
                            onEditClick = {}
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileImpactCard(
                            donationsCount = donations,
                            requestsCount = requests,
                            levelLabel = "نشاطك المحلي"
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        ProfileSettingsCard(
                            onDonationHistory = {},
                            onReminders = {},
                            onSettings = {},
                            onLanguage = {},
                            onPrivacy = {},
                            languageSubtitle = "العربية"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        ProfileSupportCard(onHelp = {}, onLogout = {})
                        Spacer(modifier = Modifier.height(24.dp))
                        ProfileVersionText(versionLabel = "1.0.0")
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Preview(name = "Profile Light 360", locale = "ar", widthDp = 360, heightDp = 840, showBackground = true)
@Composable
private fun ProfilePreviewLight360() {
    ProfileContentPreviewBody(
        userName = "عن نت",
        userPhone = "0967225762",
        donations = 0,
        requests = 0
    )
}

@Preview(name = "Profile Light 430", locale = "ar", widthDp = 430, heightDp = 900, showBackground = true)
@Composable
private fun ProfilePreviewLight430() {
    ProfileContentPreviewBody(
        userName = "مستخدم إحسان",
        userPhone = "0999999999",
        donations = 3,
        requests = 1
    )
}

@Preview(
    name = "Profile Dark",
    locale = "ar",
    widthDp = 360,
    heightDp = 840,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ProfilePreviewDark() {
    ProfileContentPreviewBody(
        userName = "أحمد",
        userPhone = "0912345678",
        donations = 2,
        requests = 2
    )
}

@Preview(name = "Profile FontScale", locale = "ar", widthDp = 360, heightDp = 900, fontScale = 1.3f)
@Composable
private fun ProfilePreviewFontScale() {
    ProfileContentPreviewBody(
        userName = "مستخدم",
        userPhone = "0900000000",
        donations = 1,
        requests = 0
    )
}

@Preview(name = "Profile Missing", locale = "ar", widthDp = 360, heightDp = 720, showBackground = true)
@Composable
private fun ProfilePreviewMissing() {
    IhsanTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = IhsanTheme.colors.surfaceMuted
            ) {
                Column {
                    ProfileTopBar(title = "الملف الشخصي", onBackClick = {})
                    MissingLocalProfileContent(onCreateProfile = {}, appVersion = "1.0.0")
                }
            }
        }
    }
}
