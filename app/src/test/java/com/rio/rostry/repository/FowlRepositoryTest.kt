package com.rio.rostry.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.FowlDao
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.common.NetworkResult
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.UUID

class FowlRepositoryTest {
    
    private lateinit var repository: FowlRepository
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockStorage: FirebaseStorage
    private lateinit var mockFowlDao: FowlDao
    
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
        mockFirestore = mockk(relaxed = true)
        mockStorage = mockk(relaxed = true)
        mockFowlDao = mockk(relaxed = true)
        
        repository = FowlRepository(mockFirestore, mockStorage, mockFowlDao)
    }
    
    @Test
    fun `addFowl should save to both Firestore and local database`() = runTest {
        // Given
        val fowlWithoutId = testFowl.copy(id = "")
        coEvery { mockFowlDao.insertFowl(any()) } just Runs
        
        // When
        val result = repository.addFowl(fowlWithoutId)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        assertNotNull("Should generate ID", result.getOrNull())
        
        coVerify { mockFowlDao.insertFowl(any()) }
        verify { mockFirestore.collection("fowls") }
    }
    
    @Test
    fun `addFowl should handle Firestore errors gracefully`() = runTest {
        // Given
        every { mockFirestore.collection(any()) } throws RuntimeException("Network error")
        
        // When
        val result = repository.addFowl(testFowl)
        
        // Then
        assertTrue("Should return failure", result.isFailure)
        assertEquals("Should contain error message", "Network error", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `getMarketplaceFowls should return cached data when network fails`() = runTest {
        // Given
        val cachedFowls = listOf(testFowl)
        every { mockFowlDao.getMarketplaceFowls() } returns flowOf(cachedFowls)
        every { mockFirestore.collection(any()) } throws RuntimeException("Network error")
        
        // When
        val result = repository.getMarketplaceFowls().first()
        
        // Then
        assertEquals("Should return cached data", cachedFowls, result)
        verify { mockFowlDao.getMarketplaceFowls() }
    }
    
    @Test
    fun `updateFowl should sync to both local and remote`() = runTest {
        // Given
        val updatedFowl = testFowl.copy(price = 600.0)
        coEvery { mockFowlDao.updateFowl(any()) } just Runs
        
        // When
        val result = repository.updateFowl(updatedFowl)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { mockFowlDao.updateFowl(updatedFowl) }
        verify { mockFirestore.collection("fowls").document(updatedFowl.id) }
    }
    
    @Test
    fun `deleteFowl should remove from both local and remote`() = runTest {
        // Given
        val fowlId = "test-fowl-id"
        coEvery { mockFowlDao.deleteFowlById(any()) } just Runs
        
        // When
        val result = repository.deleteFowl(fowlId)
        
        // Then
        assertTrue("Should return success", result.isSuccess)
        coVerify { mockFowlDao.deleteFowlById(fowlId) }
        verify { mockFirestore.collection("fowls").document(fowlId) }
    }
    
    @Test
    fun `searchFowls should handle empty results`() = runTest {
        // Given
        val query = "nonexistent breed"
        every { mockFowlDao.searchFowls(any()) } returns emptyList()
        
        // When
        val result = repository.searchFowls(query)
        
        // Then
        assertTrue("Should return empty list", result.isEmpty())
        verify { mockFowlDao.searchFowls("%$query%") }
    }
    
    @Test
    fun `getUserFowls should return user-specific fowls`() = runTest {
        // Given
        val userId = "test-user-id"
        val userFowls = listOf(testFowl.copy(ownerId = userId))
        every { mockFowlDao.getUserFowls(userId) } returns flowOf(userFowls)
        
        // When
        val result = repository.getUserFowls(userId).first()
        
        // Then
        assertEquals("Should return user fowls", userFowls, result)
        verify { mockFowlDao.getUserFowls(userId) }
    }
}
