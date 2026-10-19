package com.example.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

/** Screen state; retry must reconnect to the caller's real operation. */
@Composable
fun IhsanOfflineState(modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null, compact: Boolean = false) {
    if (compact) {
        Surface(modifier.fillMaxSize(), color = IhsanTheme.colors.surfaceElevated) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.SignalWifiOff, null, Modifier.size(28.dp), tint = IhsanTheme.colors.accentContent)
                Spacer(Modifier.height(8.dp))
                Text("أنت غير متصل", style = MaterialTheme.typography.titleSmall, color = IhsanTheme.colors.textPrimary)
                Text("تشغيل الفيديو يحتاج إلى اتصال بالإنترنت.", style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                if (onRetry != null) TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = onRetry) { Text("تجربة مرة أخرى") }
            }
        }
        return
    }
    IhsanEmptyState(
        title = "أنت غير متصل",
        message = "يمكنك قراءة المحتوى المحلي. يتطلب تشغيل الفيديو اتصالًا بالإنترنت.",
        icon = Icons.Default.SignalWifiOff,
        primaryActionLabel = if (onRetry != null) "تجربة مرة أخرى" else null,
        onPrimaryAction = onRetry,
        modifier = modifier
    )
}
