package com.example.feature.azkar.domain.usecase

import com.example.feature.azkar.domain.repository.AzkarRepository

/**
 * Phase 6 — reverses a counter increment (Tasbih undo).
 *
 * Writes an absolute count rather than a relative step, so a concurrent increment can
 * never drive the stored value below zero.
 */
class DecrementCounterUseCase(
    private val repository: AzkarRepository
) {
    suspend operator fun invoke(zikrId: Long, newCount: Int) {
        repository.updateZikrCount(zikrId, newCount.coerceAtLeast(0))
    }
}
