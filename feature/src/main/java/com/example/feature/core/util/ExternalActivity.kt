package com.example.feature.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** External apps and OEM settings screens are optional capabilities. */
fun openExternalActivity(
    context: Context,
    intent: Intent,
    onUnavailable: () -> Unit = {
        runCatching { Toast.makeText(context,"تعذر فتح التطبيق المطلوب؛ أعد المحاولة أو اختر تطبيقًا آخر",Toast.LENGTH_LONG).show() }
        Unit
    }
): Boolean = try {
    context.startActivity(intent)
    true
} catch (_: ActivityNotFoundException) {
    onUnavailable(); false
} catch (_: SecurityException) {
    onUnavailable(); false
}
