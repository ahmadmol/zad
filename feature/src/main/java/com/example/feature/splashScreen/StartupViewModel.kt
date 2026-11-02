package com.example.feature.splashScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StartupState(
    val onboardingCompleted: Boolean? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val completionSaved: Boolean = false,
    val error: String? = null
)

/** Failed reads never masquerade as first launch, and failed saves never navigate away. */
class StartupViewModel(private val preferences: UserPreferences) : ViewModel() {
    private val mutable = MutableStateFlow(StartupState())
    val state = mutable.asStateFlow()
    private var readJob: Job? = null

    init { retry() }

    fun retry() {
        if (mutable.value.saving) return
        readJob?.cancel()
        mutable.update { it.copy(loading = true, onboardingCompleted = null, error = null) }
        readJob = viewModelScope.launch {
            try {
                val completed = preferences.hasCompletedOnboarding.first()
                mutable.update { it.copy(loading = false, onboardingCompleted = completed) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutable.update { it.copy(loading = false, error = "تعذر قراءة إعدادات البداية. أعد المحاولة دون مسح بياناتك.") }
            }
        }
    }

    fun completeOnboarding() {
        if (mutable.value.loading || mutable.value.onboardingCompleted == null ||
            mutable.value.saving || mutable.value.completionSaved) return
        mutable.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                preferences.setOnboardingCompleted(true)
                mutable.update { it.copy(completionSaved = true, onboardingCompleted = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutable.update { it.copy(error = "تعذر حفظ إعداد البداية؛ أعد المحاولة. بياناتك السابقة محفوظة.") }
            } finally {
                mutable.update { it.copy(saving = false) }
            }
        }
    }
}
