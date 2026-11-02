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
    val hadithOfTheDay: Hadith? = null,
    val error: String? = null, val actionError: String? = null
)

sealed interface HadithAction {
    data object Retry : HadithAction
    data class OnSearchQueryChanged(val query: String) : HadithAction
    data class OnCategorySelected(val category: String?) : HadithAction
    data class OnToggleFavorite(val hadithId: Long) : HadithAction
}

class HadithViewModel @JvmOverloads constructor(
    private val getHadithsUseCase: GetHadithsUseCase,
    private val repository: HadithRepository,
    private val handle: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(handle.get<String>("query") ?: "")
    val queryState = _searchQuery.asStateFlow()
    private val _selectedCategory = MutableStateFlow(handle.get<String>("category"))

    private val searchIndex = com.example.feature.hadith.domain.model.HadithSearchIndex()
    private val refresh = MutableStateFlow(0)
    private val actionError = MutableStateFlow<String?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HadithUiState> = refresh.flatMapLatest { combine(
        flow { emitAll(getHadithsUseCase()) },
        _searchQuery,
        _selectedCategory, actionError
    ) { hadiths, query, category, writeError ->
        HadithUiState(hadiths = hadiths, searchQuery = query, selectedCategory = category, actionError = writeError)
    }.mapLatest { input ->
        val hadiths = input.hadiths
        val query = input.searchQuery
        val category = input.selectedCategory
        val filtered = searchIndex.filter(hadiths, query).filter {
            when (category) { null -> true; "المفضلة" -> it.isFavorite; else -> it.category == category }
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
            , hadithOfTheDay = hadithOfTheDay, actionError = input.actionError
        )
    }.onStart { emit(HadithUiState(isLoading = true, searchQuery = _searchQuery.value, selectedCategory = _selectedCategory.value)) }
      .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error
          emit(HadithUiState(error = "تعذر قراءة الأحاديث؛ أعد المحاولة", searchQuery = _searchQuery.value, selectedCategory = _selectedCategory.value)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HadithUiState(isLoading = true)
    )

    fun onAction(action: HadithAction) {
        when (action) {
            is HadithAction.OnSearchQueryChanged -> { _searchQuery.value = action.query; handle["query"] = action.query }
            is HadithAction.OnCategorySelected -> { _selectedCategory.value = action.category; handle["category"] = action.category }
            HadithAction.Retry -> { actionError.value = null; refresh.value++ }
            is HadithAction.OnToggleFavorite -> {
                viewModelScope.launch {
                    try { repository.toggleFavorite(action.hadithId); actionError.value = null }
                    catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                    catch (error: Exception) { actionError.value = "تعذر حفظ المفضلة؛ أعد المحاولة" }
                }
            }
        }
    }
}
