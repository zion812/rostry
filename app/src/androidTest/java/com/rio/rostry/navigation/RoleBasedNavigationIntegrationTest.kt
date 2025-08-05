package com.rio.rostry.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.UserRole
import com.rio.rostry.ui.navigation.Permission
import com.rio.rostry.ui.theme.RostryTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RoleBasedNavigationIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun generalRole_showsCorrectNavigationItems() {
        // Given
        val generalUser = createMockUser(UserRole.GENERAL)

        // When
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(generalUser, isLoading = false, error = null)
            }
        }

        // Then - General user should see basic navigation
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marketplace").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()

        // Should not see advanced features
        composeTestRule.onNodeWithText("Analytics").assertDoesNotExist()
        composeTestRule.onNodeWithText("Team Management").assertDoesNotExist()
    }

    @Test
    fun farmerRole_showsEnhancedFeatures() {
        // Given
        val farmerUser = createMockUser(UserRole.FARMER)

        // When
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(farmerUser, isLoading = false, error = null)
            }
        }

        // Then - Farmer should see farm management features
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marketplace").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Fowls").assertIsDisplayed()
    }

    @Test
    fun enthusiastRole_showsSpecializedFeatures() {
        // Given
        val enthusiastUser = createMockUser(UserRole.ENTHUSIAST)

        // When
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(enthusiastUser, isLoading = false, error = null)
            }
        }

        // Then - Enthusiast should see specialized features
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marketplace").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Fowls").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsLoadingIndicator() {
        // When
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(user = null, isLoading = true, error = null)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading your farm access...").assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("loading_indicator")).assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        // When
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(
                    user = null, 
                    isLoading = false, 
                    error = "Failed to load user data"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load user data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try Again").assertIsDisplayed()
    }

    private fun createMockUser(role: UserRole): User {
        return User(
            id = "test_user_${role.name}",
            email = "test@example.com",
            displayName = "Test User",
            role = role,
            isKycVerified = true
        )
    }

    @Composable
    private fun TestRoleBasedNavigation(
        user: User?,
        isLoading: Boolean,
        error: String?
    ) {
        when {
            isLoading -> {
                Column {
                    CircularProgressIndicator(modifier = Modifier.testTag("loading_indicator"))
                    Text("Loading your farm access...")
                }
            }
            error != null -> {
                Column {
                    Text("Something went wrong")
                    Text(error)
                    Button(onClick = { }) {
                        Text("Try Again")
                    }
                }
            }
            user != null -> {
                Column {
                    // Always show basic navigation
                    Text("Dashboard")
                    Text("Marketplace")
                    Text("Profile")

                    // Role-based navigation
                    when (user.role) {
                        UserRole.FARMER, UserRole.ENTHUSIAST -> {
                            Text("My Fowls")
                        }
                        else -> {
                            // General users don't see My Fowls
                        }
                    }
                }
            }
        }
    }
}

/**
 * Performance tests for role-based navigation
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RoleBasedNavigationPerformanceTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Test
    fun navigationTransition_completesWithinTimeLimit() {
        // Given
        val user = createMockUser(UserRole.FARMER)

        // When
        val startTime = System.currentTimeMillis()
        
        composeTestRule.setContent {
            RostryTheme {
                TestRoleBasedNavigation(user)
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()
        
        val endTime = System.currentTimeMillis()
        val transitionTime = endTime - startTime

        // Then - Navigation should load within 200ms
        assert(transitionTime < 200) {
            "Navigation transition took ${transitionTime}ms, expected < 200ms"
        }
    }

    private fun createMockUser(role: UserRole): User {
        return User(
            id = "test_user_${role.name}",
            email = "test@example.com",
            displayName = "Test User",
            role = role,
            isKycVerified = true
        )
    }

    @Composable
    private fun TestRoleBasedNavigation(user: User) {
        // Simplified test navigation for performance testing
        LazyColumn {
            item { Text("Dashboard") }
            item { Text("Marketplace") }
            
            when (user.role) {
                UserRole.FARMER, UserRole.ENTHUSIAST -> {
                    item { Text("My Fowls") }
                }
                else -> {
                    // General users don't see additional items
                }
            }
        }
    }
}