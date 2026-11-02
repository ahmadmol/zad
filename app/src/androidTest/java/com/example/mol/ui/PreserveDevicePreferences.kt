package com.example.mol.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.rules.ExternalResource

/** Test-only guard: reader navigation and feature screens may persist progress as
 * a side effect. Restore the exact preference keys, including absent values, even
 * when an assertion fails. Never restores/deletes any production database rows. */
class PreserveDevicePreferences : ExternalResource() {
    private val saved = mutableListOf<Pair<DataStore<Preferences>, Preferences>>()

    @Suppress("UNCHECKED_CAST")
    override fun before() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        check(context.packageName == "com.example.mol.referenceqa")
        check(android.os.Build.MODEL == "23129RAA4G" && android.os.Build.MANUFACTURER.equals("Xiaomi", true))
        for (owner in listOf(
            "com.example.feature.core.preferences.UserPreferencesKt",
            "com.example.feature.azkar.data.local.SettingsManagerKt"
        )) {
            val getter = Class.forName(owner).getDeclaredMethod("getDataStore", Context::class.java)
            getter.isAccessible = true
            val store = getter.invoke(null, context) as DataStore<Preferences>
            saved += store to store.data.first()
        }
        Unit
    }

    override fun after() = runBlocking {
        saved.forEach { (store, snapshot) -> store.updateData { snapshot } }
        saved.clear()
    }
}
