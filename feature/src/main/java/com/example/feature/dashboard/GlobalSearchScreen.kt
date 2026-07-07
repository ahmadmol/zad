package com.example.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanActionCard
import com.example.designsystem.component.IhsanSearchBar
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.feature.duas.presentation.DuaViewModel
import com.example.feature.quran.presentation.QuranViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onNavigateToQuran: (Int, Int) -> Unit,
    onNavigateToDua: (Long) -> Unit,
    onNavigateToZikr: (Long) -> Unit,
    quranViewModel: QuranViewModel = koinViewModel(),
    duaViewModel: DuaViewModel = koinViewModel(),
    azkarViewModel: AzkarViewModel = koinViewModel()
) {
    var query by remember { mutableStateOf("") }
    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val duaState by duaViewModel.uiState.collectAsStateWithLifecycle()
    val azkarState by azkarViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            quranViewModel.onAction(com.example.feature.quran.presentation.QuranAction.Search(query))
            duaViewModel.onAction(com.example.feature.duas.presentation.DuaAction.OnSearchQueryChanged(query))
            azkarViewModel.onAction(com.example.feature.azkar.presentation.AzkarAction.OnSearchQueryChanged(query))
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        IhsanSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            placeholder = "ابحث عن آية، دعاء، أو ذكر...",
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (query.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ابحث في كل محتوى التطبيق", color = Color.Gray)
                        }
                    }
                } else {
                    // Quran Results
                    if (quranState.searchResults.isNotEmpty()) {
                        item { SearchSectionTitle("آيات قرآنية", Icons.Default.MenuBook) }
                        items(quranState.searchResults) { verse ->
                            IhsanActionCard(
                                title = "سورة ${verse.surahId}", // Ideally would have name
                                subtitle = verse.text,
                                icon = Icons.Default.MenuBook,
                                onClick = { onNavigateToQuran(verse.surahId, verse.verseNumber) }
                            )
                        }
                    }

                    // Dua Results
                    if (duaState.duas.isNotEmpty()) {
                        item { SearchSectionTitle("أدعية مأثورة", Icons.Default.AutoAwesome) }
                        items(duaState.duas) { dua ->
                            IhsanActionCard(
                                title = dua.title,
                                subtitle = dua.text,
                                icon = Icons.Default.AutoAwesome,
                                onClick = { onNavigateToDua(dua.id) }
                            )
                        }
                    }

                    // Azkar Results
                    if (azkarState.azkarList.isNotEmpty()) {
                        item { SearchSectionTitle("أذكار وتسبيح", Icons.Default.SelfImprovement) }
                        items(azkarState.azkarList) { zikr ->
                            IhsanActionCard(
                                title = zikr.title,
                                subtitle = zikr.text,
                                icon = Icons.Default.SelfImprovement,
                                onClick = { onNavigateToZikr(zikr.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0D4D3D), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D4D3D))
    }
}
