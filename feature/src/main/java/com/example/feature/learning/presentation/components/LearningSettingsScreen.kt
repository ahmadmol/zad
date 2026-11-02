package com.example.feature.learning.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun LearningSettingsScreen(title: String, onBack: () -> Unit, modifier: Modifier = Modifier,
    onGeneralSettings: (() -> Unit)? = null) {
    Scaffold(modifier = modifier, containerColor = IhsanTheme.colors.surfaceBase, contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = { IhsanTopBar(title, style = IhsanTopBarStyle.CompactSkyline, onNavigateBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DisabledLearningSetting("جودة الفيديو", "تُضبط من أدوات مشغل YouTube")
            DisabledLearningSetting("التحميل عبر Wi-Fi فقط", "تنزيل فيديوهات هذا القسم غير متاح حاليًا")
            DisabledLearningSetting("إشعارات الحلقات الجديدة", "لا توجد خدمة إشعارات للحلقات حاليًا")
            Text("تتبع اللغة والمظهر وحجم الخط إعدادات التطبيق العامة.",
                style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
            onGeneralSettings?.let { navigate -> Button(onClick = navigate) { Text("الإعدادات العامة") } }
        }
    }
}

@Composable
private fun DisabledLearningSetting(title: String, description: String) {
    Surface(color = IhsanTheme.colors.surfaceElevated, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = IhsanTheme.colors.textPrimary)
                Text(description, style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
            }
            Switch(checked = false, onCheckedChange = null, enabled = false)
        }
    }
}
