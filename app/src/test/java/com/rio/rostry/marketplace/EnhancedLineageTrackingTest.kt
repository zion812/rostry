package com.rio.rostry.marketplace

import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.MarketplaceRepository
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.ui.marketplace.MarketplaceViewModel
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Enhanced test suite for lineage tracking improvements
 * Tests the new traceable/non-traceable mode handling with data clearing
 */
class EnhancedLineageTrackingTest {

    private lateinit var marketplaceRepository: MarketplaceRepository
    private lateinit var fowlRepository: FowlRepository
    private lateinit var viewModel: MarketplaceViewModel

    private val testFowl = Fowl(
        id = "fowl-123",
        ownerId = "user-456",
        name = "Test Fowl",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.FEMALE,
        status = "Breeder Ready"
    )

    private val motherFowl = Fowl(
        id = "mother-789",
        ownerId = "user-456",
        name = "Mother Fowl",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.FEMALE,
        status = "Breeder Ready"
    )

    private val fatherFowl = Fowl(
        id = "father-012",
        ownerId = "user-456",
        name = "Father Fowl",
        breed = "Rhode Island Red",
        type = FowlType.CHICKEN,
        gender = FowlGender.MALE,
        status = "Breeder Ready"
    )

    @Before
    fun setup() {
        marketplaceRepository = mockk()
        fowlRepository = mockk()
        viewModel = mockk()

        // Mock fowl repository responses
        coEvery { fowlRepository.getFowlById("fowl-123") } returns testFowl
        coEvery { fowlRepository.getFowlById("mother-789") } returns motherFowl
        coEvery { fowlRepository.getFowlById("father-012") } returns fatherFowl
        coEvery { fowlRepository.updateFowl(any()) } returns Result.success(Unit)
    }

    @Test
    fun `test traceable lineage requires at least one parent`() = runTest {
        // Given: Traceable lineage with no parents
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = "fowl-123",
                sellerId = "user-456",
                sellerName = "Test User",
                price = 100.0,
                purpose = "Breeding Stock",
                description = "Test fowl",
                location = "Test Location",
                hasTraceableLineage = true,
                motherId = null,
                fatherId = null,
                generation = null,
                bloodlineId = null,
                lineageNotes = ""
            )
        } returns Result.failure(Exception("Traceable lineage requires at least one parent to be selected"))

        // When: Creating listing with traceable lineage but no parents
        val result = marketplaceRepository.createListingWithLineage(
            fowlId = "fowl-123",
            sellerId = "user-456",
            sellerName = "Test User",
            price = 100.0,
            purpose = "Breeding Stock",
            description = "Test fowl",
            location = "Test Location",
            hasTraceableLineage = true,
            motherId = null,
            fatherId = null,
            generation = null,
            bloodlineId = null,
            lineageNotes = ""
        )

        // Then: Should fail with appropriate error
        assertTrue("Should fail validation", result.isFailure)
        assertEquals(
            "Should have correct error message",
            "Traceable lineage requires at least one parent to be selected",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun `test non-traceable mode forces all lineage data to null`() = runTest {
        // Given: Non-traceable mode with lineage data provided
        val capturedListing = slot<MarketplaceListing>()
        
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = "fowl-123",
                sellerId = "user-456",
                sellerName = "Test User",
                price = 100.0,
                purpose = "Breeding Stock",
                description = "Test fowl",
                location = "Test Location",
                hasTraceableLineage = false,
                motherId = "mother-789", // This should be ignored
                fatherId = "father-012", // This should be ignored
                generation = 3, // This should be ignored
                bloodlineId = "BL001", // This should be ignored
                lineageNotes = "Test notes" // This should be ignored
            )
        } returns Result.success("listing-456")

        // When: Creating listing with non-traceable lineage
        val result = marketplaceRepository.createListingWithLineage(
            fowlId = "fowl-123",
            sellerId = "user-456",
            sellerName = "Test User",
            price = 100.0,
            purpose = "Breeding Stock",
            description = "Test fowl",
            location = "Test Location",
            hasTraceableLineage = false,
            motherId = "mother-789",
            fatherId = "father-012",
            generation = 3,
            bloodlineId = "BL001",
            lineageNotes = "Test notes"
        )

        // Then: Should succeed and ignore all lineage data
        assertTrue("Should create listing successfully", result.isSuccess)
        assertEquals("Should return listing ID", "listing-456", result.getOrNull())
    }

    @Test
    fun `test traceable mode with valid parents succeeds`() = runTest {
        // Given: Traceable mode with valid parent data
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = "fowl-123",
                sellerId = "user-456",
                sellerName = "Test User",
                price = 150.0,
                purpose = "Breeding Stock",
                description = "High-quality breeding fowl",
                location = "Farm Location",
                hasTraceableLineage = true,
                motherId = "mother-789",
                fatherId = "father-012",
                generation = 3,
                bloodlineId = "BL001",
                lineageNotes = "Excellent breeding history"
            )
        } returns Result.success("listing-789")

        // When: Creating listing with traceable lineage and valid data
        val result = marketplaceRepository.createListingWithLineage(
            fowlId = "fowl-123",
            sellerId = "user-456",
            sellerName = "Test User",
            price = 150.0,
            purpose = "Breeding Stock",
            description = "High-quality breeding fowl",
            location = "Farm Location",
            hasTraceableLineage = true,
            motherId = "mother-789",
            fatherId = "father-012",
            generation = 3,
            bloodlineId = "BL001",
            lineageNotes = "Excellent breeding history"
        )

        // Then: Should succeed
        assertTrue("Should create listing successfully", result.isSuccess)
        assertEquals("Should return listing ID", "listing-789", result.getOrNull())
    }

    @Test
    fun `test getBreedingCandidates filters correctly`() = runTest {
        // Given: Mock ViewModel with breeding candidates
        val allFowls = listOf(
            testFowl,
            motherFowl,
            fatherFowl,
            Fowl(
                id = "fowl-999",
                ownerId = "other-user",
                name = "Other User Fowl",
                breed = "Leghorn",
                type = FowlType.CHICKEN,
                gender = FowlGender.FEMALE,
                status = "Breeder Ready"
            ),
            Fowl(
                id = "fowl-888",
                ownerId = "user-456",
                name = "Young Fowl",
                breed = "Rhode Island Red",
                type = FowlType.CHICKEN,
                gender = FowlGender.FEMALE,
                status = "Growing"
            )
        )

        every { viewModel.getBreedingCandidates(testFowl) } returns listOf(motherFowl, fatherFowl)

        // When: Getting breeding candidates
        val candidates = viewModel.getBreedingCandidates(testFowl)

        // Then: Should return only owned, breeder-ready fowls excluding current fowl
        assertEquals("Should return 2 candidates", 2, candidates.size)
        assertTrue("Should include mother fowl", candidates.contains(motherFowl))
        assertTrue("Should include father fowl", candidates.contains(fatherFowl))
        assertFalse("Should not include current fowl", candidates.contains(testFowl))
    }

    @Test
    fun `test MarketplaceListing data model with lineage fields`() {
        // Given: MarketplaceListing with lineage data
        val listing = MarketplaceListing(
            listingId = "listing-123",
            fowlId = "fowl-123",
            sellerId = "user-456",
            sellerName = "Test User",
            price = 150.0,
            purpose = "Breeding Stock",
            description = "Test fowl with lineage",
            location = "Test Location",
            fowlName = "Test Fowl",
            fowlBreed = "Rhode Island Red",
            fowlType = "CHICKEN",
            fowlGender = "FEMALE",
            fowlAge = "6 months",
            motherId = "mother-789",
            fatherId = "father-012",
            hasTraceableLineage = true,
            lineageVerified = true,
            generation = 3,
            bloodlineId = "BL001",
            lineageNotes = "Excellent breeding history with proven genetics"
        )

        // Then: All lineage fields should be properly set
        assertTrue("Listing should have traceable lineage", listing.hasTraceableLineage)
        assertTrue("Lineage should be verified", listing.lineageVerified)
        assertEquals("Mother ID should match", "mother-789", listing.motherId)
        assertEquals("Father ID should match", "father-012", listing.fatherId)
        assertEquals("Generation should be 3", 3, listing.generation)
        assertEquals("Bloodline ID should match", "BL001", listing.bloodlineId)
        assertEquals("Lineage notes should match", "Excellent breeding history with proven genetics", listing.lineageNotes)
    }

    @Test
    fun `test MarketplaceListing with non-traceable lineage`() {
        // Given: MarketplaceListing with non-traceable lineage
        val listing = MarketplaceListing(
            listingId = "listing-456",
            fowlId = "fowl-456",
            sellerId = "user-789",
            sellerName = "Another User",
            price = 75.0,
            purpose = "Meat",
            description = "Regular fowl without lineage",
            location = "Another Location",
            fowlName = "Regular Fowl",
            fowlBreed = "Mixed",
            fowlType = "CHICKEN",
            fowlGender = "MALE",
            fowlAge = "4 months",
            motherId = null,
            fatherId = null,
            hasTraceableLineage = false,
            lineageVerified = false,
            generation = null,
            bloodlineId = null,
            lineageNotes = ""
        )

        // Then: All lineage fields should be null/empty
        assertFalse("Listing should not have traceable lineage", listing.hasTraceableLineage)
        assertFalse("Lineage should not be verified", listing.lineageVerified)
        assertNull("Mother ID should be null", listing.motherId)
        assertNull("Father ID should be null", listing.fatherId)
        assertNull("Generation should be null", listing.generation)
        assertNull("Bloodline ID should be null", listing.bloodlineId)
        assertEquals("Lineage notes should be empty", "", listing.lineageNotes)
    }

    @Test
    fun `test lineage validation with invalid parent ownership`() = runTest {
        // Given: Parent fowl owned by different user
        val otherUserFowl = Fowl(
            id = "other-fowl-123",
            ownerId = "other-user-789",
            name = "Other User Fowl",
            breed = "Leghorn",
            type = FowlType.CHICKEN,
            gender = FowlGender.FEMALE,
            status = "Breeder Ready"
        )

        coEvery { fowlRepository.getFowlById("other-fowl-123") } returns otherUserFowl
        coEvery { 
            marketplaceRepository.createListingWithLineage(
                fowlId = "fowl-123",
                sellerId = "user-456",
                sellerName = "Test User",
                price = 100.0,
                purpose = "Breeding Stock",
                description = "Test fowl",
                location = "Test Location",
                hasTraceableLineage = true,
                motherId = "other-fowl-123",
                fatherId = null,
                generation = null,
                bloodlineId = null,
                lineageNotes = ""
            )
        } returns Result.failure(Exception("Invalid mother fowl selection"))

        // When: Trying to use fowl owned by another user as parent
        val result = marketplaceRepository.createListingWithLineage(
            fowlId = "fowl-123",
            sellerId = "user-456",
            sellerName = "Test User",
            price = 100.0,
            purpose = "Breeding Stock",
            description = "Test fowl",
            location = "Test Location",
            hasTraceableLineage = true,
            motherId = "other-fowl-123",
            fatherId = null,
            generation = null,
            bloodlineId = null,
            lineageNotes = ""
        )

        // Then: Should fail with ownership validation error
        assertTrue("Should fail validation", result.isFailure)
        assertEquals(
            "Should have correct error message",
            "Invalid mother fowl selection",
            result.exceptionOrNull()?.message
        )
    }
}
