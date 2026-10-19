package com.example.feature.nabiihsan.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.presentation.components.RelatedContentCard

@Composable
fun NabiSourceCard(sourceTitle: String, modifier: Modifier = Modifier, authorName: String = "",
    onYouTubeClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val uri = Uri.parse(sourceTitle)
    val isLink = uri.scheme in listOf("https", "http")
    if (isLink || onYouTubeClick != null) {
        RelatedContentCard("المصدر", listOfNotNull(uri.host.takeIf { isLink }, authorName.takeIf { it.isNotBlank() })
            .joinToString(" • "), onClick = {
                if (onYouTubeClick != null) onYouTubeClick()
                else runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            }, modifier = modifier)
    } else if (sourceTitle.isNotBlank()) {
        Surface(color = IhsanTheme.colors.surfaceElevated, modifier = modifier) {
            Text(sourceTitle, Modifier.padding(14.dp), color = IhsanTheme.colors.textBody)
        }
    }
}
