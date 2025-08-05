package com.rio.rostry.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.ui.viewmodel.FowlViewModel
import com.rio.rostry.data.common.NetworkResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class FowlViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var viewModel: FowlViewModel
    private lateinit var mockRepository: FowlRepository
    
    private val testFowl = Fowl(
        id = "test-fowl-id",
        name = "Test Fowl",
        breed = "Rhode Island Red",
        ownerId = "test-owner-id",
        price = 500.0,
        isForSale = true
    )
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = FowlViewModel(mockRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadMarketplaceFowls should update UI state correctly`() = runTest {
        // Given
        val fowls = listOf(testFowl)
        every { mockRepository.getMarketplaceFowls() } returns flowOf(fowls)
        
        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Should not be loading", uiState.isLoading)
        assertEquals("Should contain fowls", fowls, uiState.fowls)
        assertNull("Should have no error", uiState.error)
    }
    
    @Test
    fun `loadMarketplaceFowls should handle errors gracefully`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { mockRepository.getMarketplaceFowls() } throws RuntimeException(errorMessage)
        
        // When
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertFalse("Should not be loading", uiState.isLoading)
        assertTrue("Should have empty fowls list", uiState.fowls.isEmpty())
        assertNotNull("Should have error", uiState.error)
        assertTrue("Should contain error message", uiState.error!!.contains(errorMessage))
    }
    
    @Test
    fun `addFowl should show loading state during operation`() = runTest {
        // Given
        coEvery { mockRepository.addFowl(any()) } returns Result.success("new-fowl-id")
        
        // When
        viewModel.addFowl(testFowl)
        
        // Check loading state before completion
        val loadingState = viewModel.uiState.value
        assertTrue("Should be loading", loadingState.isLoading)
        
        // Complete the operation
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val finalState = viewModel.uiState.value
        assertFalse("Should not be loading", finalState.isLoading)
        assertNull("Should have no error", finalState.error)
    }
    
    @Test
    fun `searchFowls should debounce search queries`() = runTest {
        // Given
        val searchQuery = "Rhode Island"
        val searchResults = listOf(testFowl)
        every { mockRepository.searchFowls(any()) } returns searchResults
        
        // When - Multiple rapid searches
        viewModel.searchFowls("R")
        viewModel.searchFowls("Rh")
        viewModel.searchFowls("Rhode")
        viewModel.searchFowls(searchQuery)
        
        // Advance time to trigger debounced search
        testDispatcher.scheduler.advanceTimeBy(500L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(exactly = 1) { mockRepository.searchFowls(searchQuery) }
        val uiState = viewModel.uiState.value
        assertEquals("Should contain search results", searchResults, uiState.fowls)
    }
    
    @Test
    fun `updateFowl should refresh fowl list after successful update`() = runTest {
        // Given
        val updatedFowl = testFowl.copy(price = 600.0)
        coEvery { mockRepository.updateFowl(any()) } returns Result.success(Unit)
        every { mockRepository.getMarketplaceFowls() } returns flowOf(listOf(updatedFowl))
        
        // When
        viewModel.updateFowl(updatedFowl)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { mockRepository.updateFowl(updatedFowl) }
        verify { mockRepository.getMarketplaceFowls() }
        
        val uiState = viewModel.uiState.value
        assertEquals("Should contain updated fowl", listOf(updatedFowl), uiState.fowls)
    }
    
    @Test
    fun `deleteFowl should remove fowl from list after successful deletion`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        coEvery { mockRepository.deleteFowl(any()) } returns Result.success(Unit)
        every { mockRepository.getMarketplaceFowls() } returns flowOf(emptyList())
        
        // When
        viewModel.deleteFowl(fowlId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { mockRepository.deleteFowl(fowlId) }
        verify { mockRepository.getMarketplaceFowls() }
        
        val uiState = viewModel.uiState.value
        assertTrue("Should have empty fowls list", uiState.fowls.isEmpty())
    }
    
    @Test
    fun `filterFowlsByBreed should update filtered results`() = runTest {
        // Given
        val allFowls = listOf(
            testFowl,
            testFowl.copy(id = "fowl2", breed = "Leghorn")
        )
        every { mockRepository.getMarketplaceFowls() } returns flowOf(allFowls)
        
        // Load initial data
        viewModel.loadMarketplaceFowls()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.filterFowlsByBreed("Rhode Island Red")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals("Should contain only filtered fowls", 1, uiState.fowls.size)
        assertEquals("Should contain correct breed", "Rhode Island Red", uiState.fowls.first().breed)
    }
}
