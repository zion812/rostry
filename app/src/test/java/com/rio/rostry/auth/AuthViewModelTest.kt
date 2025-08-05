package com.rio.rostry.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseUser
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.model.UserRole
import com.rio.rostry.ui.auth.AuthViewModel
import com.rio.rostry.ui.auth.AuthUiState
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockFirebaseUser: FirebaseUser
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockAuthRepository = mockk()
        mockFirebaseUser = mockk()
        authViewModel = AuthViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should update UI state to authenticated on successful login`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { mockAuthRepository.signInWithEmail(email, password) } returns 
            Result.success(mockFirebaseUser)

        // When
        authViewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should be authenticated", state.isAuthenticated)
        assertNull("Should not have error", state.error)
        assertFalse("Should not show password reset sent", state.passwordResetSent)

        coVerify { mockAuthRepository.signInWithEmail(email, password) }
    }

    @Test
    fun `should update UI state with error message on authentication failure`() = runTest {
        // Given
        val email = "invalid@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"
        val exception = Exception(errorMessage)
        
        coEvery { mockAuthRepository.signInWithEmail(email, password) } returns 
            Result.failure(exception)

        // When
        authViewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be loading", state.isLoading)
        assertFalse("Should not be authenticated", state.isAuthenticated)
        assertEquals("Should have error message", errorMessage, state.error)
        assertFalse("Should not show password reset sent", state.passwordResetSent)

        coVerify { mockAuthRepository.signInWithEmail(email, password) }
    }

    @Test
    fun `should clear error state when requested`() = runTest {
        // Given - Set an error state first
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = Exception("Test error")
        
        coEvery { mockAuthRepository.signInWithEmail(email, password) } returns 
            Result.failure(exception)

        authViewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is set
        assertNotNull("Should have error", authViewModel.uiState.value.error)

        // When
        authViewModel.clearError()

        // Then
        val state = authViewModel.uiState.value
        assertNull("Error should be cleared", state.error)
        assertFalse("Should still not be authenticated", state.isAuthenticated)
        assertFalse("Should not be loading", state.isLoading)
    }

    @Test
    fun `should handle sign out and update authentication state`() = runTest {
        // Given - Start with authenticated state
        val email = "test@example.com"
        val password = "password123"
        
        coEvery { mockAuthRepository.signInWithEmail(email, password) } returns 
            Result.success(mockFirebaseUser)
        coEvery { mockAuthRepository.signOut() } just Runs

        // First sign in
        authViewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify authenticated
        assertTrue("Should be authenticated", authViewModel.uiState.value.isAuthenticated)

        // When
        authViewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be authenticated", state.isAuthenticated)
        assertFalse("Should not be loading", state.isLoading)
        assertNull("Should not have error", state.error)

        coVerify { mockAuthRepository.signOut() }
    }

    @Test
    fun `should handle sign up with email successfully`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val displayName = "New User"
        val role = UserRole.FARMER
        
        coEvery { mockAuthRepository.signUpWithEmail(email, password, displayName, role) } returns 
            Result.success(mockFirebaseUser)

        // When
        authViewModel.signUpWithEmail(email, password, displayName, role)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should be authenticated", state.isAuthenticated)
        assertNull("Should not have error", state.error)

        coVerify { mockAuthRepository.signUpWithEmail(email, password, displayName, role) }
    }

    @Test
    fun `should handle password reset successfully`() = runTest {
        // Given
        val email = "test@example.com"
        
        coEvery { mockAuthRepository.resetPassword(email) } returns Result.success(Unit)

        // When
        authViewModel.resetPassword(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be loading", state.isLoading)
        assertTrue("Should show password reset sent", state.passwordResetSent)
        assertNull("Should not have error", state.error)

        coVerify { mockAuthRepository.resetPassword(email) }
    }

    @Test
    fun `should handle password reset failure`() = runTest {
        // Given
        val email = "invalid@example.com"
        val errorMessage = "User not found"
        val exception = Exception(errorMessage)
        
        coEvery { mockAuthRepository.resetPassword(email) } returns Result.failure(exception)

        // When
        authViewModel.resetPassword(email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertFalse("Should not be loading", state.isLoading)
        assertFalse("Should not show password reset sent", state.passwordResetSent)
        assertEquals("Should have error message", errorMessage, state.error)

        coVerify { mockAuthRepository.resetPassword(email) }
    }

    @Test
    fun `should check auth state correctly`() {
        // Given
        every { mockAuthRepository.isUserLoggedIn() } returns true

        // When
        authViewModel.checkAuthState()

        // Then
        val state = authViewModel.uiState.value
        assertTrue("Should be authenticated", state.isAuthenticated)

        // Given - user not logged in
        every { mockAuthRepository.isUserLoggedIn() } returns false

        // When
        authViewModel.checkAuthState()

        // Then
        val updatedState = authViewModel.uiState.value
        assertFalse("Should not be authenticated", updatedState.isAuthenticated)

        verify(exactly = 2) { mockAuthRepository.isUserLoggedIn() }
    }

    @Test
    fun `should handle sign out failure gracefully`() = runTest {
        // Given
        val errorMessage = "Sign out failed"
        val exception = Exception(errorMessage)
        
        coEvery { mockAuthRepository.signOut() } throws exception

        // When
        authViewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = authViewModel.uiState.value
        assertEquals("Should have error message", "Failed to sign out: $errorMessage", state.error)
        assertFalse("Should not be loading", state.isLoading)

        coVerify { mockAuthRepository.signOut() }
    }

    @Test
    fun `initial state should be correct`() {
        // When
        val initialState = authViewModel.uiState.value

        // Then
        assertEquals("Initial state should match expected", 
            AuthUiState(
                isLoading = false,
                isAuthenticated = false,
                error = null,
                passwordResetSent = false
            ), 
            initialState
        )
    }
}