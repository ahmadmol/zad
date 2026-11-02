package com.example.feature.settings

import androidx.datastore.preferences.core.*
import com.example.designsystem.theme.IhsanPalette
import com.example.feature.azkar.data.local.SettingsManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class PalettePersistenceTest {
    @Test fun `all palettes and modes persist independently while legacy and unknown keys survive`() = runTest {
        val file = Files.createTempDirectory("ihsan-palettes").resolve("settings.preferences_pb").toFile()
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        var store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        store.edit { it[SettingsManager.DARK_MODE] = true; it[stringPreferencesKey("future_key")] = "retained" }
        var settings = SettingsManager(store)
        assertEquals(SettingsManager.ThemeMode.DARK, settings.themeModeFlow.first())
        assertEquals(IhsanPalette.EMERALD, settings.paletteFlow.first())
        for (palette in IhsanPalette.entries) for (mode in SettingsManager.ThemeMode.entries) {
            settings.setThemeMode(mode); settings.setPalette(palette)
            assertEquals(mode, settings.themeModeFlow.first())
            assertEquals(palette, settings.paletteFlow.first())
        }
        scope.cancel(); scope.coroutineContext[Job]!!.join()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }); settings = SettingsManager(store)
            assertEquals(IhsanPalette.MIDNIGHT, settings.paletteFlow.first())
            assertEquals(SettingsManager.ThemeMode.DARK, settings.themeModeFlow.first())
            assertEquals("retained", store.data.first()[stringPreferencesKey("future_key")])
            store.edit { it[stringPreferencesKey("appearance_palette_v1")] = "future_palette" }
            assertEquals(IhsanPalette.EMERALD, settings.paletteFlow.first())
            assertEquals("future_palette", store.data.first()[stringPreferencesKey("appearance_palette_v1")])
        } finally { scope.cancel(); scope.coroutineContext[Job]!!.join() }
    }
}
