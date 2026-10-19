package com.example.feature.hadith.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.hadith.domain.model.Hadith
import com.example.feature.hadith.domain.repository.HadithRepository
import com.example.feature.hadith.domain.usecase.GetHadithsUseCase
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import kotlinx.coroutines.launch

data class HadithUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val hadithOfTheDay: Hadith? = null
)

sealed interface HadithAction {
    data class OnSearchQueryChanged(val query: String) : HadithAction
    data class OnCategorySelected(val category: String?) : HadithAction
    data class OnToggleFavorite(val hadithId: Long) : HadithAction
}

class HadithViewModel(
    private val getHadithsUseCase: GetHadithsUseCase,
    private val repository: HadithRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HadithUiState> = combine(
        getHadithsUseCase(),
        _searchQuery,
        _selectedCategory
    ) { hadiths, query, category ->
        val filtered = hadiths.filter {
            val categoryOk = when (category) {
                null -> true
                "المفضلة" -> it.isFavorite
                else -> it.category == category
            }
            categoryOk && (query.isBlank() || com.example.feature.core.search.ArabicSearch.matches(it.text, query)
                    || com.example.feature.core.search.ArabicSearch.matches(it.narrator, query)
                    || com.example.feature.core.search.ArabicSearch.matches(it.source, query)
                    || com.example.feature.core.search.ArabicSearch.matches(it.category, query))
        }

        val hadithOfTheDay = if (hadiths.isNotEmpty()) {
            val day = LocalDate.now().toEpochDay()
            val idx = (Math.abs(day) % hadiths.size).toInt()
            hadiths[idx]
        } else null
        HadithUiState(
            hadiths = filtered,
            searchQuery = query,
            selectedCategory = category
            , hadithOfTheDay = hadithOfTheDay
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HadithUiState(isLoading = true)
    )

    fun onAction(action: HadithAction) {
        when (action) {
            is HadithAction.OnSearchQueryChanged -> _searchQuery.value = action.query
            is HadithAction.OnCategorySelected -> _selectedCategory.value = action.category
            is HadithAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(action.hadithId)
                }
            }
        }
    }
}
