package com.rio.rostry.marketplace

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlGender
import com.rio.rostry.data.model.FowlType
import com.rio.rostry.data.model.MarketplaceListing
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.MarketplaceRepository
import com.rio.rostry.ui.marketplace.MarketplaceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.local.dao.CartDao
import com.rio.rostry.data.repository.UserRepository
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

/**
 * Comprehensive test suite for the dynamic lineage tracking feature
 * 
 * Tests cover:
 * - Data model validation
 * - Repository functionality
 * - ViewModel business logic
 * - UI component behavior
 * - Error handling scenarios
 * - Edge cases and validation
 */
@ExperimentalCoroutinesApi
class LineageTrackingTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock dependencies
    private lateinit var fowlRepository: FowlRepository
    private lateinit var marketplaceRepository: MarketplaceRepository
    private lateinit var userRepository: UserRepository
    private lateinit var cartDao: CartDao
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    
    // Test subject
    private lateinit var viewModel: MarketplaceViewModel
    
    // Test data
    private val testUserId = "test-user-123"
    private val testUserName = "Test User"
    
    private val motherFowl = Fowl(
        id = "mother-fowl-123",
        ownerId = testUserId,
        name = "Mother Hen",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.FEMALE,
        status = "Breeder Ready",
        hasTraceableLineage = true,
        generation = 2,
        bloodlineId = "BL001"
    )
    
    private val fatherFowl = Fowl(
        id = "father-fowl-123",
        ownerId = testUserId,
        name = "Father Rooster",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.MALE,
        status = "Breeder Ready",
        hasTraceableLineage = true,
        generation = 2,
        bloodlineId = "BL001"
    )
    
    private val testFowl = Fowl(
        id = "test-fowl-123",
        ownerId = testUserId,
        name = "Test Fowl",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.FEMALE,
        status = "Growing",
        motherId = motherFowl.id,
        fatherId = fatherFowl.id
    )

    @Before
    fun setup() {
        // Set up test dispatcher
        Dispatchers.setMain(testDispatcher)
        
        // Initialize mocks
        fowlRepository = mockk()
        marketplaceRepository = mockk()
        userRepository = mockk()
        cartDao = mockk()
        firebaseAuth = mockk()
        firebaseUser = mockk()
        firestore = mockk()
        
        // Setup Firebase Auth mocks
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns testUserId
        every { firebaseUser.displayName } returns testUserName
        
        // Setup repository mocks
        coEvery { fowlRepository.getFowlById(testFowl.id) } returns testFowl
        coEvery { fowlRepository.getFowlById(motherFowl.id) } returns motherFowl
        coEvery { fowlRepository.getFowlById(fatherFowl.id) } returns fatherFowl
        every { fowlRepository.getMarketplaceFowls() } returns flowOf(listOf(testFowl, motherFowl, fatherFowl))
        every { cartDao.getCartItemCount() } returns flowOf(0)
        
        // Initialize ViewModel
        viewModel = MarketplaceViewModel(
            fowlRepository = fowlRepository,
            marketplaceRepository = marketplaceRepository,
            userRepository = userRepository,
            cartDao = cartDao,
            auth = firebaseAuth
        )
    }

    @After
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `test MarketplaceListing data model with lineage fields`() {
        val listing = MarketplaceListing(
            listingId = "listing-123",
            fowlId = testFowl.id,
            sellerId = testUserId,
            sellerName = testUserName,
            price = 50.0,
            purpose = "Breeding Stock",
            description = "High-quality breeding fowl",
            location = "Test Location",
            hasTraceableLineage = true,
            lineageVerified = true,
            motherId = motherFowl.id,
            fatherId = fatherFowl.id,
            generation = 3,
            bloodlineId = "BL001",
            lineageNotes = "Excellent breeding history"
        )
        
        // Verify all lineage fields are properly set
        assertTrue("Listing should have traceable lineage", listing.hasTraceableLineage)
        assertTrue("Lineage should be verified", listing.lineageVerified)
        assertEquals("Mother ID should match", motherFowl.id, listing.motherId)
        assertEquals("Father ID should match", fatherFowl.id, listing.fatherId)
        assertEquals("Generation should be 3", 3, listing.generation)
        assertEquals("Bloodline ID should match", "BL001", listing.bloodlineId)
        assertEquals("Lineage notes should match", "Excellent breeding history", listing.lineageNotes)
    }

    @Test
    fun `test Fowl data model with enhanced lineage fields`() {
        val fowl = Fowl(
            id = "fowl-123",
            ownerId = testUserId,
            name = "Test Fowl",
            breed = "Rhode Island Red",
            type = FowlType.CHICKEN,
            gender = FowlGender.FEMALE,
            hasTraceableLineage = true,
            lineageVerified = true,
            generation = 3,
            bloodlineId = "BL001"
        )
        
        // Verify enhanced lineage fields
        assertTrue("Fowl should have traceable lineage", fowl.hasTraceableLineage)
        assertTrue("Lineage should be verified", fowl.lineageVerified)
        assertEquals("Generation should be 3", 3, fowl.generation)
        assertEquals("Bloodline ID should match", "BL001", fowl.bloodlineId)
    }

    @Test
    fun `test getBreedingCandidates returns correct fowls`() = runTest {
        // Setup test data
        val breedingFowls = listOf(motherFowl, fatherFowl)
        every { fowlRepository.getMarketplaceFowls() } returns flowOf(breedingFowls + testFowl)
        
        // Load data
        viewModel.loadMarketplaceFowls()
        
        // Get breeding candidates
        val candidates = viewModel.getBreedingCandidates(testFowl)
        
        // Verify results
        assertEquals("Should return 2 breeding candidates", 2, candidates.size)
        assertTrue("Should include mother fowl", candidates.contains(motherFowl))
        assertTrue("Should include father fowl", candidates.contains(fatherFowl))
        assertFalse("Should not include current fowl", candidates.contains(testFowl))
    }

    @Test
    fun `test getBreedingCandidates filters by owner`() = runTest {
        // Setup test data with fowl from different owner
        val otherOwnerFowl = motherFowl.copy(
            id = "other-fowl-123",
            ownerId = "other-user-123"
        )
        
        every { fowlRepository.getMarketplaceFowls() } returns flowOf(listOf(motherFowl, otherOwnerFowl))
        
        // Load data
        viewModel.loadMarketplaceFowls()
        
        // Get breeding candidates
        val candidates = viewModel.getBreedingCandidates(testFowl)
        
        // Verify results
        assertEquals("Should return only owned fowls", 1, candidates.size)
        assertEquals("Should return mother fowl", motherFowl.id, candidates.first().id)
    }

    @Test
    fun `test getBreedingCandidates filters by breeder status`() = runTest {
        // Setup test data with non-breeder fowl
        val nonBreederFowl = motherFowl.copy(
            id = "non-breeder-123",
            status = "Growing"
        )
        
        every { fowlRepository.getMarketplaceFowls() } returns flowOf(listOf(motherFowl, nonBreederFowl))
        
        // Load data
        viewModel.loadMarketplaceFowls()
        
        // Get breeding candidates
        val candidates = viewModel.getBreedingCandidates(testFowl)
        
        // Verify results
        assertEquals("Should return only breeder ready fowls", 1, candidates.size)
        assertEquals("Should return mother fowl", motherFowl.id, candidates.first().id)
    }

    @Test
    fun `test createListingWithLineage success scenario`() = runTest {
        // Setup successful repository response
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = testFowl.id,
                sellerId = testUserId,
                sellerName = testUserName,
                price = 50.0,
                purpose = "Breeding Stock",
                description = "Test description",
                location = "Test location",
                hasTraceableLineage = true,
                motherId = motherFowl.id,
                fatherId = fatherFowl.id,
                generation = 3,
                bloodlineId = "BL001",
                lineageNotes = "Test notes"
            )
        } returns Result.success("listing-123")
        
        var successCalled = false
        
        // Call createListing with lineage data
        viewModel.createListing(
            fowlId = testFowl.id,
            price = 50.0,
            purpose = "Breeding Stock",
            description = "Test description",
            location = "Test location",
            hasTraceableLineage = true,
            motherId = motherFowl.id,
            fatherId = fatherFowl.id,
            generation = 3,
            bloodlineId = "BL001",
            lineageNotes = "Test notes",
            onSuccess = { successCalled = true }
        )
        
        // Verify success callback was called
        assertTrue("Success callback should be called", successCalled)
        
        // Verify repository was called with correct parameters
        coVerify {
            marketplaceRepository.createListingWithLineage(
                fowlId = testFowl.id,
                sellerId = testUserId,
                sellerName = testUserName,
                price = 50.0,
                purpose = "Breeding Stock",
                description = "Test description",
                location = "Test location",
                hasTraceableLineage = true,
                motherId = motherFowl.id,
                fatherId = fatherFowl.id,
                generation = 3,
                bloodlineId = "BL001",
                lineageNotes = "Test notes"
            )
        }
    }

    @Test
    fun `test createListingWithLineage without lineage data`() = runTest {
        // Setup successful repository response
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = testFowl.id,
                sellerId = testUserId,
                sellerName = testUserName,
                price = 50.0,
                purpose = "Breeding Stock",
                description = "Test description",
                location = "Test location",
                hasTraceableLineage = false,
                motherId = null,
                fatherId = null,
                generation = null,
                bloodlineId = null,
                lineageNotes = ""
            )
        } returns Result.success("listing-123")
        
        var successCalled = false
        
        // Call createListing without lineage data
        viewModel.createListing(
            fowlId = testFowl.id,
            price = 50.0,
            purpose = "Breeding Stock",
            description = "Test description",
            location = "Test location",
            hasTraceableLineage = false,
            onSuccess = { successCalled = true }
        )
        
        // Verify success callback was called
        assertTrue("Success callback should be called", successCalled)
        
        // Verify repository was called with correct parameters
        coVerify {
            marketplaceRepository.createListingWithLineage(
                fowlId = testFowl.id,
                sellerId = testUserId,
                sellerName = testUserName,
                price = 50.0,
                purpose = "Breeding Stock",
                description = "Test description",
                location = "Test location",
                hasTraceableLineage = false,
                motherId = null,
                fatherId = null,
                generation = null,
                bloodlineId = null,
                lineageNotes = ""
            )
        }
    }

    @Test
    fun `test createListingWithLineage error handling`() = runTest {
        // Setup repository error response
        coEvery { 
            marketplaceRepository.createListingWithLineage(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Test error"))
        
        var successCalled = false
        
        // Call createListing
        viewModel.createListing(
            fowlId = testFowl.id,
            price = 50.0,
            purpose = "Breeding Stock",
            description = "Test description",
            location = "Test location",
            onSuccess = { successCalled = true }
        )
        
        // Verify success callback was not called
        assertFalse("Success callback should not be called", successCalled)
        
        // Verify error state is set
        val uiState = viewModel.uiState.value
        assertNotNull("Error should be set", uiState.error)
        assertEquals("Error message should match", "Test error", uiState.error)
    }

    @Test
    fun `test createListingWithLineage authentication error`() = runTest {
        // Setup no authenticated user
        every { firebaseAuth.currentUser } returns null
        
        var successCalled = false
        
        // Call createListing
        viewModel.createListing(
            fowlId = testFowl.id,
            price = 50.0,
            purpose = "Breeding Stock",
            description = "Test description",
            location = "Test location",
            onSuccess = { successCalled = true }
        )
        
        // Verify success callback was not called
        assertFalse("Success callback should not be called", successCalled)
        
        // Verify error state is set
        val uiState = viewModel.uiState.value
        assertNotNull("Error should be set", uiState.error)
        assertEquals("Error message should match", "User not authenticated", uiState.error)
    }

    @Test
    fun `test getUserOwnedFowls returns correct fowls`() = runTest {
        // Setup test data
        val ownedFowls = listOf(testFowl, motherFowl)
        val otherUserFowl = testFowl.copy(id = "other-fowl", ownerId = "other-user")
        val forSaleFowl = testFowl.copy(id = "for-sale-fowl", isForSale = true)
        
        every { fowlRepository.getMarketplaceFowls() } returns flowOf(ownedFowls + otherUserFowl + forSaleFowl)
        
        // Load data
        viewModel.loadMarketplaceFowls()
        
        // Get owned fowls
        val result = viewModel.getUserOwnedFowls()
        
        // Verify results
        assertEquals("Should return 2 owned fowls", 2, result.size)
        assertTrue("Should include test fowl", result.any { it.id == testFowl.id })
        assertTrue("Should include mother fowl", result.any { it.id == motherFowl.id })
        assertFalse("Should not include other user fowl", result.any { it.id == otherUserFowl.id })
        assertFalse("Should not include for sale fowl", result.any { it.id == forSaleFowl.id })
    }
}