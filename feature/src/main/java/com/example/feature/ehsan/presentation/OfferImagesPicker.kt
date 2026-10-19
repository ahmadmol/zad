package com.example.feature.ehsan.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme

@Composable
fun OfferImagesPicker(images: List<String>, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("صور العرض (${images.size}/3)", style = MaterialTheme.typography.titleMedium)
        Text("اختياري — أول صورة هي صورة الغلاف", style = MaterialTheme.typography.bodySmall,
            color = IhsanTheme.colors.textSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images, key = { it }) { image ->
                Box(Modifier.size(88.dp)) {
                    AsyncImage(image, "صورة مختارة", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    IconButton(onClick = { onRemove(image) }, modifier = Modifier.align(Alignment.TopEnd).size(48.dp)) {
                        Surface(color = IhsanTheme.colors.surfaceElevated) {
                            Icon(Icons.Default.Close, "إزالة الصورة", Modifier.size(22.dp), tint = IhsanTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
        OutlinedButton(onClick = onAdd, enabled = images.size < 3,
            colors = com.example.designsystem.theme.ihsanOutlinedButtonColors()) { Text("إضافة صور") }
    }
}
