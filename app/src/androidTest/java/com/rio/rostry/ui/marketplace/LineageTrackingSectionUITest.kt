package com.rio.rostry.ui.marketplace

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlGender
import com.rio.rostry.data.model.FowlType
import com.rio.rostry.ui.marketplace.components.LineageTrackingSection
import com.rio.rostry.ui.theme.RostryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the enhanced LineageTrackingSection component
 * Tests the confirmation dialog and data clearing functionality
 */
@RunWith(AndroidJUnit4::class)
class LineageTrackingSectionUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testFowls = listOf(
        Fowl(
            id = "mother-123",
            ownerId = "user-456",
            name = "Mother Fowl",
            breed = "Rhode Island Red",
            type = FowlType.CHICKEN,
            gender = FowlGender.FEMALE,
            status = "Breeder Ready"
        ),
        Fowl(
            id = "father-456",
            ownerId = "user-456",
            name = "Father Fowl",
            breed = "Rhode Island Red",
            type = FowlType.CHICKEN,
            gender = FowlGender.MALE,
            status = "Breeder Ready"
        )
    )

    @Test
    fun testLineageTrackingSectionDisplaysCorrectly() {
        var hasTraceableLineage = false
        var selectedMotherId: String? = null
        var selectedFatherId: String? = null
        var generation = ""
        var bloodlineId = ""
        var lineageNotes = ""
        var clearDataCalled = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = { selectedMotherId = it },
                    selectedFatherId = selectedFatherId,
                    onFatherSelected = { selectedFatherId = it },
                    generation = generation,
                    onGenerationChange = { generation = it },
                    bloodlineId = bloodlineId,
                    onBloodlineChange = { bloodlineId = it },
                    lineageNotes = lineageNotes,
                    onLineageNotesChange = { lineageNotes = it },
                    availableFowls = testFowls,
                    onClearLineageData = { clearDataCalled = true }
                )
            }
        }

        // Verify initial state
        composeTestRule.onNodeWithText("Lineage Tracking").assertIsDisplayed()
        composeTestRule.onNodeWithText("Traceable Lineage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Non-Traceable").assertIsDisplayed()
        composeTestRule.onNodeWithText("Include parent info").assertIsDisplayed()
        composeTestRule.onNodeWithText("No lineage info").assertIsDisplayed()
    }

    @Test
    fun testSwitchingToTraceableLineageShowsFields() {
        var hasTraceableLineage = false
        var selectedMotherId: String? = null
        var selectedFatherId: String? = null
        var generation = ""
        var bloodlineId = ""
        var lineageNotes = ""

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = { selectedMotherId = it },
                    selectedFatherId = selectedFatherId,
                    onFatherSelected = { selectedFatherId = it },
                    generation = generation,
                    onGenerationChange = { generation = it },
                    bloodlineId = bloodlineId,
                    onBloodlineChange = { bloodlineId = it },
                    lineageNotes = lineageNotes,
                    onLineageNotesChange = { lineageNotes = it },
                    availableFowls = testFowls,
                    onClearLineageData = { }
                )
            }
        }

        // Initially, lineage fields should not be visible
        composeTestRule.onNodeWithText("Mother Fowl").assertDoesNotExist()
        composeTestRule.onNodeWithText("Father Fowl").assertDoesNotExist()

        // Click on traceable lineage
        composeTestRule.onNodeWithText("Traceable Lineage").performClick()

        // Wait for animation and verify fields appear
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Mother Fowl").assertIsDisplayed()
        composeTestRule.onNodeWithText("Father Fowl").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bloodline ID").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lineage Notes").assertIsDisplayed()
    }

    @Test
    fun testConfirmationDialogAppearsWhenSwitchingFromTraceableToNonTraceable() {
        var hasTraceableLineage = true
        var selectedMotherId: String? = "mother-123"
        var selectedFatherId: String? = "father-456"
        var generation = "3"
        var bloodlineId = "BL001"
        var lineageNotes = "Test notes"
        var clearDataCalled = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = { selectedMotherId = it },
                    selectedFatherId = selectedFatherId,
                    onFatherSelected = { selectedFatherId = it },
                    generation = generation,
                    onGenerationChange = { generation = it },
                    bloodlineId = bloodlineId,
                    onBloodlineChange = { bloodlineId = it },
                    lineageNotes = lineageNotes,
                    onLineageNotesChange = { lineageNotes = it },
                    availableFowls = testFowls,
                    onClearLineageData = { clearDataCalled = true }
                )
            }
        }

        // Verify traceable mode is active and fields are visible
        composeTestRule.onNodeWithText("Generation").assertIsDisplayed()

        // Click on non-traceable
        composeTestRule.onNodeWithText("Non-Traceable").performClick()

        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Switch to Non-Traceable Mode?").assertIsDisplayed()
        composeTestRule.onNodeWithText("This will permanently clear all lineage information including parent selections, generation, bloodline, and notes. This action cannot be undone.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear & Continue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun testConfirmationDialogClearAndContinueButton() {
        var hasTraceableLineage = true
        var selectedMotherId: String? = "mother-123"
        var selectedFatherId: String? = "father-456"
        var generation = "3"
        var bloodlineId = "BL001"
        var lineageNotes = "Test notes"
        var clearDataCalled = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = { selectedMotherId = it },
                    selectedFatherId = selectedFatherId,
                    onFatherSelected = { selectedFatherId = it },
                    generation = generation,
                    onGenerationChange = { generation = it },
                    bloodlineId = bloodlineId,
                    onBloodlineChange = { bloodlineId = it },
                    lineageNotes = lineageNotes,
                    onLineageNotesChange = { lineageNotes = it },
                    availableFowls = testFowls,
                    onClearLineageData = { 
                        clearDataCalled = true
                        selectedMotherId = null
                        selectedFatherId = null
                        generation = ""
                        bloodlineId = ""
                        lineageNotes = ""
                    }
                )
            }
        }

        // Click on non-traceable to show dialog
        composeTestRule.onNodeWithText("Non-Traceable").performClick()

        // Click "Clear & Continue"
        composeTestRule.onNodeWithText("Clear & Continue").performClick()

        // Verify dialog disappears and data is cleared
        composeTestRule.onNodeWithText("Switch to Non-Traceable Mode?").assertDoesNotExist()
        
        // Verify clear data callback was called
        assert(clearDataCalled) { "Clear data callback should have been called" }
    }

    @Test
    fun testConfirmationDialogCancelButton() {
        var hasTraceableLineage = true
        var selectedMotherId: String? = "mother-123"
        var clearDataCalled = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = { selectedMotherId = it },
                    selectedFatherId = null,
                    onFatherSelected = { },
                    generation = "",
                    onGenerationChange = { },
                    bloodlineId = "",
                    onBloodlineChange = { },
                    lineageNotes = "",
                    onLineageNotesChange = { },
                    availableFowls = testFowls,
                    onClearLineageData = { clearDataCalled = true }
                )
            }
        }

        // Click on non-traceable to show dialog
        composeTestRule.onNodeWithText("Non-Traceable").performClick()

        // Click "Cancel"
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Verify dialog disappears and data is NOT cleared
        composeTestRule.onNodeWithText("Switch to Non-Traceable Mode?").assertDoesNotExist()
        
        // Verify clear data callback was NOT called
        assert(!clearDataCalled) { "Clear data callback should not have been called" }
        
        // Verify still in traceable mode
        assert(hasTraceableLineage) { "Should still be in traceable mode" }
    }

    @Test
    fun testSwitchingToNonTraceableWhenAlreadyNonTraceableDoesNotShowDialog() {
        var hasTraceableLineage = false
        var clearDataCalled = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = null,
                    onMotherSelected = { },
                    selectedFatherId = null,
                    onFatherSelected = { },
                    generation = "",
                    onGenerationChange = { },
                    bloodlineId = "",
                    onBloodlineChange = { },
                    lineageNotes = "",
                    onLineageNotesChange = { },
                    availableFowls = testFowls,
                    onClearLineageData = { clearDataCalled = true }
                )
            }
        }

        // Click on non-traceable (already selected)
        composeTestRule.onNodeWithText("Non-Traceable").performClick()

        // Verify no dialog appears
        composeTestRule.onNodeWithText("Switch to Non-Traceable Mode?").assertDoesNotExist()
        
        // Verify clear data callback was NOT called
        assert(!clearDataCalled) { "Clear data callback should not have been called" }
    }

    @Test
    fun testSwitchingToTraceableFromNonTraceableDoesNotShowDialog() {
        var hasTraceableLineage = false

        composeTestRule.setContent {
            RostryTheme {
                LineageTrackingSection(
                    hasTraceableLineage = hasTraceableLineage,
                    onLineageToggle = { hasTraceableLineage = it },
                    selectedMotherId = null,
                    onMotherSelected = { },
                    selectedFatherId = null,
                    onFatherSelected = { },
                    generation = "",
                    onGenerationChange = { },
                    bloodlineId = "",
                    onBloodlineChange = { },
                    lineageNotes = "",
                    onLineageNotesChange = { },
                    availableFowls = testFowls,
                    onClearLineageData = { }
                )
            }
        }

        // Click on traceable lineage
        composeTestRule.onNodeWithText("Traceable Lineage").performClick()

        // Verify no dialog appears
        composeTestRule.onNodeWithText("Switch to Non-Traceable Mode?").assertDoesNotExist()
        
        // Verify mode switched to traceable
        assert(hasTraceableLineage) { "Should be in traceable mode" }
    }
}
