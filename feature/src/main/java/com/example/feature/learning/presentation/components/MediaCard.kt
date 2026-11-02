package com.example.feature.learning.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.designsystem.theme.IhsanTheme

/** Presentation only: callers own content identity, persistence and navigation. */
@Composable
fun MediaCard(title: String, subtitle: String, image: String?, badge: String?,
    isFavorite: Boolean, onClick: () -> Unit, onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier, metadata: String = "", selected: Boolean = false,
    showFavorite: Boolean = true, trailing: (@Composable () -> Unit)? = null) {
    var imageReady by remember(image) { mutableStateOf(false) }
    Card(modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) IhsanTheme.colors.selectedContainer else MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(88.dp, 72.dp).clip(RoundedCornerShape(12.dp))
                .background(if (selected) IhsanTheme.colors.selectedContainer else IhsanTheme.colors.surfaceMuted)) {
                if (!imageReady) Icon(Icons.AutoMirrored.Filled.MenuBook, null,
                    Modifier.align(Alignment.Center).size(28.dp), tint = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.accentContent)
                AsyncImage(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                    onSuccess = { imageReady = true }, onError = { imageReady = false })
                badge?.takeIf { it.isNotBlank() }?.let {
                    Surface(Modifier.align(Alignment.BottomStart).padding(3.dp),
                        color = Color.Black.copy(alpha = .75f), shape = RoundedCornerShape(4.dp)) {
                        Text(it, Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold,
                    maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.accentContent)
                if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, lineHeight = 17.sp,
                    maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.textSecondary)
                if (metadata.isNotBlank()) Text(metadata, fontSize = 11.sp,
                    color = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.textSecondary)
            }
            if (trailing != null) trailing() else if (showFavorite) FavoriteButton(isFavorite, onFavoriteClick)
        }
    }
}

@Composable
fun FavoriteButton(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick, modifier.size(48.dp)) {
        Icon(if (selected) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            if (selected) "إزالة من المفضلة" else "إضافة للمفضلة",
            tint = if (selected) IhsanTheme.colors.favorite else IhsanTheme.colors.accentContent,
            modifier = Modifier.size(20.dp))
    }
}

@Composable
fun WatchLaterButton(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick, modifier.size(48.dp)) {
        Icon(Icons.Default.WatchLater, if (selected) "إزالة من المشاهدة لاحقًا" else "المشاهدة لاحقًا",
            tint = if (selected) IhsanTheme.colors.accentContent else IhsanTheme.colors.textSecondary)
    }
}

@Composable
fun VideoHero(videoId: String?, image: String?, isOnline: Boolean, isEmbeddable: Boolean,
    onOpenExternal: () -> Unit, modifier: Modifier = Modifier, startSeconds: Int = 0) {
    IhsanYouTubePlayer(videoId, image, isOnline, isEmbeddable, modifier, startSeconds, onOpenExternal)
}

@Composable
fun RelatedContentCard(title: String, subtitle: String, onClick: () -> Unit,
    modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth().clickable(onClick = onClick),
        color = IhsanTheme.colors.surfaceElevated, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IhsanTheme.colors.accentContent)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
        }
    }
}
