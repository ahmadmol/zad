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
    var query by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var selectedCategory by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("الكل") }

    val categories = listOf("الكل") + allRecipes.map { it.category }.filter { it.isNotBlank() }.distinct()

    val filteredRecipes = remember(allRecipes, query, selectedCategory) {
        allRecipes.filter { recipe ->
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.title, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.meaning, q) ||
                    com.example.feature.core.search.ArabicSearch.matches(recipe.seerahContext, q)
            val matchesCat = selectedCategory == "الكل" || recipe.category == selectedCategory
            matchesQuery && matchesCat
        }
    }

    val readState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val writeError by viewModel.actionError.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else MaterialTheme.colorScheme.surface)
    ) {
        // Header
        com.example.designsystem.component.IhsanTopBar(
            title = "الوصفات النبوية", style = com.example.designsystem.component.IhsanTopBarStyle.CompactSkyline,
            onNavigateBack = onNavigateBack)

        if (com.example.feature.learning.presentation.components.LearningReadState(readState.isLoading, readState.errorMessage, viewModel::retry)) return@Column
        com.example.feature.learning.presentation.components.LearningActionError(writeError, viewModel::retry)
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
                    com.example.designsystem.component.IhsanFilterChip(cat,isSelected,{selectedCategory=cat})
                }
            }

            // Recipe List
            if (filteredRecipes.isEmpty()) {
                IhsanEmptyState(
                    title = if (allRecipes.isEmpty()) "لا توجد وصفات معتمدة متاحة" else "لا توجد نتائج",
                    message = if (allRecipes.isEmpty()) "لم تتوفر بيانات موثقة لربط الوصفات بهذه السلسلة؛ تبقى الحلقات متاحة." else "لم نتمكن من العثور على وصفات تطابق البحث المحدد.",
                    primaryActionLabel = if (allRecipes.isEmpty()) null else "إعادة التعيين",
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
