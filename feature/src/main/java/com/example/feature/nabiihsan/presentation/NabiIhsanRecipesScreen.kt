package com.example.feature.nabiihsan.presentation

import com.example.designsystem.theme.IhsanTheme

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanSearchBar
import com.example.feature.nabiihsan.presentation.components.NabiRecipeCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanRecipesScreen(
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val allRecipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل") + allRecipes.map { it.category }.filter { it.isNotBlank() }.distinct()

    val filteredRecipes = remember(allRecipes, query, selectedCategory) {
        allRecipes.filter { recipe ->
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    recipe.title.lowercase().contains(q) ||
                    recipe.meaning.lowercase().contains(q) ||
                    recipe.seerahContext.lowercase().contains(q)
            val matchesCat = selectedCategory == "الكل" || recipe.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else MaterialTheme.colorScheme.surface)
    ) {
        // Header
        com.example.designsystem.component.IhsanTopBar(
            title = "الوصفات النبوية", style = com.example.designsystem.component.IhsanTopBarStyle.CompactSkyline,
            onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Bar
            IhsanSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "ابحث عن وصفة، موضوع أو معنى..."
            )

            // Category Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) IhsanTheme.colors.selectedContainer
                                else if (IhsanTheme.isDark) IhsanTheme.colors.surfaceMuted else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Recipe List
            if (filteredRecipes.isEmpty()) {
                IhsanEmptyState(
                    title = "لا توجد نتائج",
                    message = "لم نتمكن من العثور على وصفات تطابق البحث المحدد.",
                    primaryActionLabel = "إعادة التعيين",
                    onPrimaryAction = {
                        query = ""
                        selectedCategory = "الكل"
                    }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredRecipes.forEach { recipe ->
                        NabiRecipeCard(
                            recipe = recipe,
                            onRecipeClick = onRecipeClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
