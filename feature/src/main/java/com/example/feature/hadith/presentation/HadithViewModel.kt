package com.example.feature.hadith.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.hadith.domain.model.Hadith
import com.example.feature.hadith.domain.repository.HadithRepository
import com.example.feature.hadith.domain.usecase.GetHadithsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HadithUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String? = null
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
            (category == null || it.category == category) &&
            (query.isBlank() || it.text.contains(query, ignoreCase = true) || it.narrator.contains(query, ignoreCase = true))
        }
        HadithUiState(
            hadiths = filtered,
            searchQuery = query,
            selectedCategory = category
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
