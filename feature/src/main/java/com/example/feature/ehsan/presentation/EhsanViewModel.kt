package com.example.feature.ehsan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.ehsan.domain.model.Donation
import com.example.feature.ehsan.domain.usecase.GetDonationsUseCase
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
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
    val selectedSort: String = "DEFAULT",
    val selectedStatus: String = "ALL",
    val selectedOrigin: String = "ALL",
    val selectedType: String = "OFFER", // OFFER, REQUEST, ALL (legacy/internal only)
    val locationName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class EhsanViewModel(
    private val getDonationsUseCase: GetDonationsUseCase,
    private val prayerLocationRepository: PrayerLocationRepository,
    private val demoManager: com.example.feature.ehsan.data.demo.CommunityDemoManager? = null,
    private val savedState: androidx.lifecycle.SavedStateHandle = androidx.lifecycle.SavedStateHandle()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EhsanUiState(
        searchQuery = savedState["query"] ?: "", selectedLocation = savedState["location"] ?: "الكل",
        selectedCategory = savedState["category"] ?: "الكل", selectedSort = savedState["sort"] ?: "DEFAULT",
        selectedType = savedState["type"] ?: "OFFER", selectedStatus = savedState["status"] ?: "ALL",
        selectedOrigin = savedState["origin"] ?: "ALL"))
    val uiState: StateFlow<EhsanUiState> = _uiState.asStateFlow()

    fun resetDemoOnly() {
        viewModelScope.launch {
            try { requireNotNull(demoManager) { "إعادة أمثلة DEMO غير متاحة" }.resetDemoOnly() }
            catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; _uiState.update { it.copy(error = e.message ?: "تعذر إعادة أمثلة DEMO") } }
        }
    }

    private var donationsJob: kotlinx.coroutines.Job? = null
    init {
        observeDonations()
        observeLocation()
    }

    private fun observeLocation() {
        prayerLocationRepository.observeLocation()
            .onEach { state ->
                val locationName = (state as? PrayerLocationState.Available)
                    ?.location
                    ?.displayName
                    ?.takeIf { it.isNotBlank() }
                _uiState.update { it.copy(locationName = locationName) }
            }
            .catch { error -> if (error is kotlinx.coroutines.CancellationException) throw error }
            .launchIn(viewModelScope)
    }

    fun retry() = observeDonations()

    private fun observeDonations() {
        donationsJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        donationsJob = getDonationsUseCase()
            .onEach { list ->
                val donors = list.map { it.donorName }.distinct().size
                val completed = list.count { it.status == "COMPLETED" }
                _uiState.update { 
                    it.copy(
                        isLoading = false, error = null,
                        donations = list,
                        donorCount = donors,
                        completedCount = completed,
                        activeProjects = list.count { it.status == "AVAILABLE" || it.status == "PENDING" }
                    ) 
                }
                applyFilters()
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, error = "تعذر قراءة السجلات المحلية؛ أعد المحاولة") }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        savedState["query"] = query
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onLocationChange(location: String) {
        savedState["location"] = location
        _uiState.update { it.copy(selectedLocation = location) }
        applyFilters()
    }

    fun onCategoryChange(category: String) {
        savedState["category"] = category
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun onSortChange(sort: String) {
        savedState["sort"] = sort
        _uiState.update { it.copy(selectedSort = sort) }
        applyFilters()
    }

    fun onTypeChange(type: String) {
        savedState["type"] = type
        _uiState.update { it.copy(selectedType = type) }
        applyFilters()
    }

    fun onStatusChange(status: String) {
        savedState["status"] = status
        _uiState.update { it.copy(selectedStatus = status) }; applyFilters()
    }
    fun onOriginChange(origin: String) {
        savedState["origin"] = origin
        _uiState.update { it.copy(selectedOrigin = origin) }; applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = currentState.donations.filter { donation ->
            val matchesQuery = com.example.feature.core.search.ArabicSearch.matches(donation.title, currentState.searchQuery) ||
                              com.example.feature.core.search.ArabicSearch.matches(donation.description, currentState.searchQuery)
            val matchesLocation = currentState.selectedLocation == "الكل" || donation.location == currentState.selectedLocation
            val matchesCategory = currentState.selectedCategory == "الكل" || donation.category == currentState.selectedCategory
            val matchesType = currentState.selectedType == "ALL" || donation.type == currentState.selectedType
            
            matchesQuery && matchesLocation && matchesCategory && matchesType &&
                (currentState.selectedStatus == "ALL" || donation.status == currentState.selectedStatus) &&
                (currentState.selectedOrigin == "ALL" || donation.origin == currentState.selectedOrigin)
        }
        val sorted = when (currentState.selectedSort) {
            "LATEST" -> filtered.sortedByDescending { donation ->
                donation.createdAt.takeIf { it > 0L } ?: Long.MIN_VALUE
            }
            else -> filtered
        }
        _uiState.update { it.copy(filteredDonations = sorted) }
    }
}
