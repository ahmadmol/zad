package com.example.feature.tasbih.presentation

import com.example.feature.azkar.domain.model.Zikr

data class TasbihUiState(
    val tasbihList: List<Zikr> = emptyList(),
    val selectedTasbihIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isVibrationEnabled: Boolean = true
) {
    val currentTasbih: Zikr? = tasbihList.getOrNull(selectedTasbihIndex)
}

sealed interface TasbihAction {
    data class OnIncrement(val tasbihId: Long) : TasbihAction
    data class OnReset(val tasbihId: Long) : TasbihAction
    data class OnSelectTasbih(val index: Int) : TasbihAction
    data object OnToggleVibration : TasbihAction
}
