package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.MarketplaceListing
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceListingDao {
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveListings(): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getSellerListings(sellerId: String): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE listingId = :listingId")
    suspend fun getListingById(listingId: String): MarketplaceListing?
    
    @Query("SELECT * FROM marketplace_listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<MarketplaceListing>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: MarketplaceListing)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<MarketplaceListing>)
    
    @Update
    suspend fun updateListing(listing: MarketplaceListing)
    
    @Delete
    suspend fun deleteListing(listing: MarketplaceListing)
    
    @Query("DELETE FROM marketplace_listings WHERE listingId = :listingId")
    suspend fun deleteListingById(listingId: String)
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND purpose = :purpose ORDER BY createdAt DESC")
    fun getListingsByPurpose(purpose: String): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getListingsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND fowlType = :fowlType ORDER BY createdAt DESC")
    fun getListingsByFowlType(fowlType: String): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND isBreederReady = 1 ORDER BY createdAt DESC")
    fun getBreederReadyListings(): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND (fowlName LIKE :query OR fowlBreed LIKE :query OR description LIKE :query) ORDER BY createdAt DESC")
    fun searchListings(query: String): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND motherId = :motherId ORDER BY createdAt DESC")
    fun getListingsByMother(motherId: String): Flow<List<MarketplaceListing>>
    
    @Query("SELECT * FROM marketplace_listings WHERE isActive = 1 AND fatherId = :fatherId ORDER BY createdAt DESC")
    fun getListingsByFather(fatherId: String): Flow<List<MarketplaceListing>>
}