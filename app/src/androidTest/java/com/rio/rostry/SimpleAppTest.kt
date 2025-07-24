package com.rio.rostry

import androidx.test.ext.junit4.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit4.rules.ActivityScenarioRule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Rule

/**
 * Simple instrumented test without Hilt dependencies
 */
@RunWith(AndroidJUnit4::class)
class SimpleAppTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rio.rostry", appContext.packageName)
    }

    @Test
    fun activityLaunches() {
        // Test that MainActivity launches without crashing
        activityRule.scenario.onActivity { activity ->
            assertNotNull(activity)
        }
    }
}