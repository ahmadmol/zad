package com.example.feature.ihsanplus.integration.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.feature.ihsanplus.integration.model.IhsanPlusDailySourceSnapshot
import org.koin.androidx.compose.koinViewModel

/**
 * Controlled Daily experience — production read-only snapshot.
 * Does not replace Home. Mutations are not exposed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlledDailyExperienceScreen(
    onNavigateBack: () -> Unit,
    viewModel: ControlledDailyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "تجربة اليوم",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
            }
        ) { padding ->
            when (val state = uiState) {
                ControlledDailyUiState.Loading -> IhsanLoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
                is ControlledDailyUiState.Error -> IhsanErrorState(
                    title = "تعذر تحميل ملخص اليوم",
                    onRetry = { viewModel.refresh() },
                    retryLabel = "إعادة المحاولة",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
                is ControlledDailyUiState.Content -> ControlledDailyContent(
                    snapshot = state.snapshot,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ControlledDailyContent(
    snapshot: IhsanPlusDailySourceSnapshot,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ملخص للقراءة فقط من بيانات التطبيق المحلية",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SnapshotCard(
            title = "الملف المحلي",
            body = snapshot.profileDisplayName ?: "لا يوجد ملف شخصي محلي بعد"
        )
        SnapshotCard(
            title = "الصلاة التالية",
            body = snapshot.nextPrayerNameArabic?.let { "$it" }
                ?: "غير متاحة (تحقق من الموقع)"
        )
        SnapshotCard(
            title = "متابعة القرآن",
            body = if (snapshot.quranSurahId != null && snapshot.quranAyahNumber != null) {
                "سورة ${snapshot.quranSurahId} — آية ${snapshot.quranAyahNumber}"
            } else {
                "لا يوجد موضع قراءة محفوظ"
            }
        )
        SnapshotCard(
            title = "تقدم الذكر اليومي",
            body = "${snapshot.dhikrTodayCount}٪ تقديرًا من الورد المحلي"
        )
        SnapshotCard(
            title = "النشاطات اليومية",
            body = if (snapshot.activityIds.isEmpty()) {
                "لا توجد نشاطات مسجلة اليوم"
            } else {
                "مكتمل ${snapshot.completedActivityIds.size} من ${snapshot.activityIds.size}"
            }
        )

        if (snapshot.activityIds.isEmpty() &&
            snapshot.profileDisplayName == null &&
            snapshot.quranSurahId == null
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            IhsanEmptyState(
                title = "ابدأ من الشاشة الرئيسية",
                message = "هذه الشاشة تعرض ملخصًا فقط ولا تستبدل الرئيسية"
            )
        }
    }
}

@Composable
private fun SnapshotCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
