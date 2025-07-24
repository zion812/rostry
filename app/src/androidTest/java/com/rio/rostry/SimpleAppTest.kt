package com.rio.rostry

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*

/**
 * Simple instrumented test without Hilt dependencies
 * Note: Removed @RunWith(AndroidJUnit4::class) to avoid kapt issues
 */
class SimpleAppTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rio.rostry", appContext.packageName)
    }
}