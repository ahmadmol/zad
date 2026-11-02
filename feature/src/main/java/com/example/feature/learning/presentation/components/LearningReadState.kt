package com.example.feature.learning.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState

/** Prevents a storage failure or initial read being rendered as an empty saved library. */
@Composable
fun LearningReadState(loading: Boolean, error: String?, onRetry: () -> Unit): Boolean {
    when {
        loading -> IhsanLoadingState(message = "جارٍ قراءة المحتوى المحلي", modifier = Modifier.fillMaxWidth().padding(16.dp))
        error != null -> IhsanErrorState("تعذر تحميل المحتوى", message = error,
            retryLabel = "إعادة المحاولة", onRetry = onRetry, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
    return loading || error != null
}

@Composable
fun LearningActionError(error: String?, onRetry: () -> Unit) {
    if (error != null) Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(error, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("إعادة قراءة البيانات") }
    }
}
