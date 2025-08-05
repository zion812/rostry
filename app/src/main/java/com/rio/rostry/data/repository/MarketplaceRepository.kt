package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.model.MarketplaceListing
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlRepository: FowlRepository
) {
    
    suspend fun createListing(
        fowlId: String,
        sellerId: String,
        sellerName: String,
        price: Double,
        purpose: String,
        description: String,
        location: String
    ): Result<String> {
        return createListingWithLineage(
            fowlId = fowlId,
            sellerId = sellerId,
            sellerName = sellerName,
            price = price,
            purpose = purpose,
            description = description,
            location = location,
            hasTraceableLineage = false
        )
    }

    /**
     * Create a marketplace listing with enhanced lineage tracking
     *
     * @param fowlId ID of the fowl to list
     * @param sellerId ID of the seller
     * @param sellerName Name of the seller
     * @param price Listing price
     * @param purpose Purpose of the fowl (Breeding Stock, Meat, etc.)
     * @param description Listing description
     * @param location Location of the fowl
     * @param hasTraceableLineage Whether the fowl has traceable lineage
     * @param motherId ID of the mother fowl (if traceable)
     * @param fatherId ID of the father fowl (if traceable)
     * @param generation Generation number (if traceable)
     * @param bloodlineId Bloodline identifier (if traceable)
     * @param lineageNotes Additional lineage notes
     * @return Result with listing ID on success or error on failure
     */
    suspend fun createListingWithLineage(
        fowlId: String,
        sellerId: String,
        sellerName: String,
        price: Double,
        purpose: String,
        description: String,
        location: String,
        hasTraceableLineage: Boolean = false,
        motherId: String? = null,
        fatherId: String? = null,
        generation: Int? = null,
        bloodlineId: String? = null,
        lineageNotes: String = ""
    ): Result<String> {
        return try {
            // Get fowl details
            val fowl = fowlRepository.getFowlById(fowlId)
                ?: return Result.failure(Exception("Fowl not found"))

            // Verify ownership
            if (fowl.ownerId != sellerId) {
                return Result.failure(Exception("You can only list fowls you own"))
            }

            // ENHANCED: Strict lineage data processing
            val processedLineageData = if (hasTraceableLineage) {
                // Validate that at least one parent is provided for traceable lineage
                if (motherId == null && fatherId == null) {
                    return Result.failure(Exception("Traceable lineage requires at least one parent to be selected"))
                }

                // Validate parent fowls exist and are owned by the seller
                motherId?.let { id ->
                    val mother = fowlRepository.getFowlById(id)
                    if (mother == null || mother.ownerId != sellerId) {
                        return Result.failure(Exception("Invalid mother fowl selection"))
                    }
                }

                fatherId?.let { id ->
                    val father = fowlRepository.getFowlById(id)
                    if (father == null || father.ownerId != sellerId) {
                        return Result.failure(Exception("Invalid father fowl selection"))
                    }
                }

                // Validate generation is positive
                generation?.let { gen ->
                    if (gen < 1) {
                        return Result.failure(Exception("Generation must be a positive number"))
                    }
                }

                // Validate lineage notes length
                if (lineageNotes.length > 500) {
                    return Result.failure(Exception("Lineage notes must be 500 characters or less"))
                }

                LineageData(
                    motherId = motherId,
                    fatherId = fatherId,
                    generation = generation,
                    bloodlineId = bloodlineId?.takeIf { it.isNotBlank() },
                    lineageNotes = lineageNotes.takeIf { it.isNotBlank() } ?: ""
                )
            } else {
                // Force all lineage data to null/empty for non-traceable mode
                LineageData(
                    motherId = null,
                    fatherId = null,
                    generation = null,
                    bloodlineId = null,
                    lineageNotes = ""
                )
            }
            
            // Get fowl records for vaccination history
            val records = mutableListOf<FowlRecord>()
            fowlRepository.getFowlRecords(fowlId).collect { recordList ->
                records.addAll(recordList.filter { it.recordType.contains("Vaccination", ignoreCase = true) })
            }
            
            val listingId = UUID.randomUUID().toString()
            
            // Calculate age
            val ageInDays = if (fowl.dateOfHatching > 0) {
                (System.currentTimeMillis() - fowl.dateOfHatching) / (1000 * 60 * 60 * 24)
            } else 0
            
            val ageString = when {
                ageInDays < 30 -> "${ageInDays} days"
                ageInDays < 365 -> "${ageInDays / 30} months"
                else -> "${ageInDays / 365} years"
            }
            
            val listing = MarketplaceListing(
                listingId = listingId,
                fowlId = fowlId,
                sellerId = sellerId,
                sellerName = sellerName,
                price = price,
                purpose = purpose,
                description = description,
                location = location,
                featuredImageUrl = fowl.imageUrls.firstOrNull() ?: fowl.proofImageUrl ?: "",
                // Auto-populated from fowl profile
                fowlName = fowl.name,
                fowlBreed = fowl.breed,
                fowlType = fowl.type.name,
                fowlGender = fowl.gender.name,
                fowlAge = ageString,
                motherId = processedLineageData.motherId,
                fatherId = processedLineageData.fatherId,
                vaccinationRecords = records.map { "${it.recordType} - ${it.details}" },
                healthStatus = fowl.status,
                isBreederReady = fowl.status.contains("Breeder Ready", ignoreCase = true),
                // Enhanced lineage tracking fields with strict data handling
                hasTraceableLineage = hasTraceableLineage,
                lineageVerified = hasTraceableLineage && (processedLineageData.motherId != null || processedLineageData.fatherId != null),
                generation = processedLineageData.generation,
                bloodlineId = processedLineageData.bloodlineId,
                lineageNotes = processedLineageData.lineageNotes,
                createdAt = System.currentTimeMillis()
            )
            
            // Save listing
            firestore.collection("marketplace_listings").document(listingId).set(listing).await()
            
            // Update fowl to mark as for sale and update lineage information with strict data handling
            val updatedFowl = fowl.copy(
                isForSale = true,
                price = price,
                hasTraceableLineage = hasTraceableLineage,
                lineageVerified = hasTraceableLineage && (processedLineageData.motherId != null || processedLineageData.fatherId != null),
                generation = processedLineageData.generation ?: fowl.generation,
                bloodlineId = processedLineageData.bloodlineId ?: fowl.bloodlineId,
                updatedAt = System.currentTimeMillis()
            )
            fowlRepository.updateFowl(updatedFowl)
            
            Result.success(listingId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateListing(
        listingId: String,
        sellerId: String,
        price: Double,
        purpose: String,
        description: String,
        location: String
    ): Result<Unit> {
        return try {
            val listingRef = firestore.collection("marketplace_listings").document(listingId)
            val snapshot = listingRef.get().await()
            val listing = snapshot.toObject(MarketplaceListing::class.java)
                ?: return Result.failure(Exception("Listing not found"))
            
            if (listing.sellerId != sellerId) {
                return Result.failure(Exception("You can only update your own listings"))
            }
            
            val updatedListing = listing.copy(
                price = price,
                purpose = purpose,
                description = description,
                location = location,
                updatedAt = System.currentTimeMillis()
            )
            
            listingRef.set(updatedListing).await()
            
            // Update fowl price
            val fowl = fowlRepository.getFowlById(listing.fowlId)
            fowl?.let { existingFowl ->
                val updatedFowl = existingFowl.copy(
                    price = price,
                    updatedAt = System.currentTimeMillis()
                )
                fowlRepository.updateFowl(updatedFowl)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deactivateListing(listingId: String, sellerId: String): Result<Unit> {
        return try {
            val listingRef = firestore.collection("marketplace_listings").document(listingId)
            val snapshot = listingRef.get().await()
            val listing = snapshot.toObject(MarketplaceListing::class.java)
                ?: return Result.failure(Exception("Listing not found"))
            
            if (listing.sellerId != sellerId) {
                return Result.failure(Exception("You can only deactivate your own listings"))
            }
            
            val updatedListing = listing.copy(
                isActive = false,
                updatedAt = System.currentTimeMillis()
            )
            
            listingRef.set(updatedListing).await()
            
            // Update fowl to mark as not for sale
            val fowl = fowlRepository.getFowlById(listing.fowlId)
            fowl?.let { existingFowl ->
                val updatedFowl = existingFowl.copy(
                    isForSale = false,
                    updatedAt = System.currentTimeMillis()
                )
                fowlRepository.updateFowl(updatedFowl)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getActiveListings(): Flow<List<MarketplaceListing>> = flow {
        try {
            val snapshot = firestore.collection("marketplace_listings")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val listings = snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getFilteredListings(
        purpose: String? = null,
        isBreederReady: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        motherId: String? = null,
        fatherId: String? = null,
        fowlType: String? = null,
        location: String? = null
    ): Flow<List<MarketplaceListing>> = flow {
        try {
            var query = firestore.collection("marketplace_listings")
                .whereEqualTo("isActive", true)
            
            purpose?.let { query = query.whereEqualTo("purpose", it) }
            isBreederReady?.let { query = query.whereEqualTo("isBreederReady", it) }
            fowlType?.let { query = query.whereEqualTo("fowlType", it) }
            location?.let { query = query.whereEqualTo("location", it) }
            motherId?.let { query = query.whereEqualTo("motherId", it) }
            fatherId?.let { query = query.whereEqualTo("fatherId", it) }
            
            val snapshot = query.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            var listings = snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
            
            // Apply price filters (Firestore doesn't support multiple range queries)
            minPrice?.let { min -> listings = listings.filter { it.price >= min } }
            maxPrice?.let { max -> listings = listings.filter { it.price <= max } }
            
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun searchListings(query: String): Flow<List<MarketplaceListing>> = flow {
        try {
            val snapshot = firestore.collection("marketplace_listings")
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val listings = snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
                .filter { listing ->
                    listing.fowlName.contains(query, ignoreCase = true) ||
                    listing.fowlBreed.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true) ||
                    listing.sellerName.contains(query, ignoreCase = true) ||
                    listing.location.contains(query, ignoreCase = true)
                }
            
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getUserListings(sellerId: String): Flow<List<MarketplaceListing>> = flow {
        try {
            val snapshot = firestore.collection("marketplace_listings")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val listings = snapshot.documents.mapNotNull { it.toObject(MarketplaceListing::class.java) }
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun getListingById(listingId: String): MarketplaceListing? {
        return try {
            val snapshot = firestore.collection("marketplace_listings").document(listingId).get().await()
            snapshot.toObject(MarketplaceListing::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Helper data class for processing lineage information
 * Ensures consistent handling of lineage data across traceable and non-traceable modes
 */
private data class LineageData(
    val motherId: String?,
    val fatherId: String?,
    val generation: Int?,
    val bloodlineId: String?,
    val lineageNotes: String
)