package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EhsanUiState(
    val donations: List<Donation> = emptyList(),
    val filteredDonations: List<Donation> = emptyList(),
    val donorCount: Int = 0,
    val completedCount: Int = 0,
    val activeProjects: Int = 0,
    val searchQuery: String = "",
    val selectedLocation: String = "الكل",
    val selectedCategory: String = "الكل",
    val selectedType: String = "ALL", // ALL, OFFER, REQUEST
    val isLoading: Boolean = false,
    val error: String? = null
)

class EhsanViewModel(
    private val getDonationsUseCase: GetDonationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EhsanUiState())
    val uiState: StateFlow<EhsanUiState> = _uiState.asStateFlow()

    init {
        observeDonations()
    }

    private fun observeDonations() {
        _uiState.update { it.copy(isLoading = true) }
        getDonationsUseCase()
            .onEach { list ->
                val donors = list.map { it.donorName }.distinct().size
                val completed = list.count { it.status == "COMPLETED" }
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        donations = list,
                        donorCount = donors,
                        completedCount = completed,
                        activeProjects = list.size
                    ) 
                }
                applyFilters()
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onLocationChange(location: String) {
        _uiState.update { it.copy(selectedLocation = location) }
        applyFilters()
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun onTypeChange(type: String) {
        _uiState.update { it.copy(selectedType = type) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = currentState.donations.filter { donation ->
            val matchesQuery = donation.title.contains(currentState.searchQuery, ignoreCase = true) ||
                              donation.description.contains(currentState.searchQuery, ignoreCase = true)
            val matchesLocation = currentState.selectedLocation == "الكل" || donation.location == currentState.selectedLocation
            val matchesCategory = currentState.selectedCategory == "الكل" || donation.category == currentState.selectedCategory
            val matchesType = currentState.selectedType == "ALL" || donation.type == currentState.selectedType
            
            matchesQuery && matchesLocation && matchesCategory && matchesType
        }
        _uiState.update { it.copy(filteredDonations = filtered) }
    }
}
