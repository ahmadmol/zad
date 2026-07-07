package com.example.feature.asma.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.asma.domain.model.AllahName
import org.koin.androidx.compose.koinViewModel

@Composable
fun AsmaScreen(
    viewModel: AsmaViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = { DashboardTopBar(onNavigateBack) },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    uiState.error != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("تعذر تحميل الأسماء", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.error.orEmpty(), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.onAction(AsmaAction.OnRetry) }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }

                    else -> {
                        AsmaGrid(
                            asmaList = uiState.visibleAsmaList,
                            showFavoritesOnly = uiState.showFavoritesOnly,
                            onToggleFavoritesOnly = {
                                viewModel.onAction(AsmaAction.OnToggleFavoritesOnly(!uiState.showFavoritesOnly))
                            },
                            onNameClick = { name -> viewModel.onAction(AsmaAction.OnNameClick(name)) },
                            onToggleFavorite = { id -> viewModel.onAction(AsmaAction.OnToggleFavorite(id)) }
                        )
                    }
                }

                uiState.selectedName?.let { selected ->
                    AsmaDetailsSheet(
                        name = selected,
                        onDismiss = { viewModel.onAction(AsmaAction.OnDismissDetails) },
                        onToggleFavorite = { viewModel.onAction(AsmaAction.OnToggleFavorite(selected.id)) },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("${selected.name} - ${selected.meaning}\n${selected.explanation}"))
                        },
                        onShare = {
                            val shareText = "${selected.name} (${selected.transliteration})\n${selected.meaning}\n${selected.explanation}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "اسم الله ${selected.name}")
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة الاسم"))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onMenuClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "أسماء الله الحسنى",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        },
        navigationIcon = {
            TextButton(onClick = onMenuClick) {
                Text("رجوع", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AsmaGrid(
    asmaList: List<AllahName>,
    showFavoritesOnly: Boolean,
    onToggleFavoritesOnly: () -> Unit,
    onNameClick: (AllahName) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = "استكشف أسماء الله الحسنى",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "اختر اسمًا لعرض المعنى والتفسير، أو اجعل المفضلة فقط في لمحة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = onToggleFavoritesOnly,
                    label = { Text("المفضلة فقط") }
                )
            }
        }

        if (asmaList.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        modifier = Modifier.padding(24.dp),
                        text = if (showFavoritesOnly) "لا توجد أسماء مفضلة بعد." else "لا توجد أسماء متاحة حاليًا.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(asmaList) { asma ->
                AsmaCard(
                    asma = asma,
                    onNameClick = { onNameClick(asma) },
                    onToggleFavorite = { onToggleFavorite(asma.id) }
                )
            }
        }
    }
}

@Composable
private fun AsmaCard(
    asma: AllahName,
    onNameClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp))
            .clickable { onNameClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (asma.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (asma.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = asma.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = asma.transliteration,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = asma.meaning,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline),
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsmaDetailsSheet(
    name: AllahName,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name.name,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = name.transliteration,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = name.meaning,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = name.explanation,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (name.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(if (name.isFavorite) "إزالة من المفضلة" else "أضف للمفضلة")
                }
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("نسخ")
                }
                TextButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.size(4.dp))
                    Text("مشاركة")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

