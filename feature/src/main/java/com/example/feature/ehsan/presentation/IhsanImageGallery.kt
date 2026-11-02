package com.example.feature.ehsan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ehsan.data.image.EhsanImageStore

@Composable
fun IhsanImageGallery(reference: String?, title: String, imageStore: EhsanImageStore) {
    val references = remember(reference) { imageStore.imageReferences(reference) }
    var index by rememberSaveable(reference) { mutableIntStateOf(0) }
    val selected = index.coerceIn(0, (references.size - 1).coerceAtLeast(0))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(20.dp))
            .background(IhsanTheme.colors.surfaceMuted), contentAlignment = Alignment.Center) {
            val model = references.getOrNull(selected)?.let(imageStore::resolve)
            if (model != null) AsyncImage(model, title, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            else Icon(Icons.Default.Image, "لا تتوفر صورة", tint = IhsanTheme.colors.textSecondary,
                modifier = Modifier.size(48.dp))
        }
        if (references.size > 1) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { index = (selected - 1).coerceAtLeast(0) }, enabled = selected > 0) { Text("السابق") }
            Text("${selected + 1}/${references.size}", Modifier.align(Alignment.CenterVertically))
            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { index = (selected + 1).coerceAtMost(references.lastIndex) },
                enabled = selected < references.lastIndex) { Text("التالي") }
        }
    }
}
