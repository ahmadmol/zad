package com.example.mol

import com.example.mol.R
import org.junit.Assert.assertTrue
import org.junit.Test

class Part4AppLocalizationResourcesTest {

    @Test
    fun `location permission resources exist`() {
        assertTrue(R.string.location_permission_title != 0)
        assertTrue(R.string.location_permission_body != 0)
        assertTrue(R.string.location_permission_grant != 0)
        assertTrue(R.string.location_permission_open_settings != 0)
    }
}
