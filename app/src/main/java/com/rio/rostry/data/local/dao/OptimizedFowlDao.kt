package com.rio.rostry.data.local.dao

import androidx.room.*
import androidx.paging.PagingSource
import com.rio.rostry.data.model.Fowl
import kotlinx.coroutines.flow.Flow

/**
 * Optimized FowlDao with performance improvements and pagination support
 * 
 * This DAO provides high-performance data access with:
 * - Pagination support for large datasets
 * - Optimized queries with proper indexing
 * - Lightweight data models for UI performance
 * - Full-text search capabilities
 * - Batch operations for better throughput
 */
@Dao
interface OptimizedFowlDao {
    
    // ==================== PAGINATED QUERIES ====================
    
    /**
     * Paginated fowl loading for better performance with large datasets
     * 
     * @param ownerId The ID of the fowl owner
     * @return PagingSource for efficient data loading
     */
    @Query("""
        SELECT * FROM fowls 
        WHERE ownerId = :ownerId 
        ORDER BY createdAt DESC
    """)
    fun getUserFowlsPaged(ownerId: String): PagingSource<Int, Fowl>
    
    /**
     * Marketplace fowls with pagination and comprehensive filtering
     * 
     * @param breed Optional breed filter (partial match)
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param location Optional location filter (partial match)
     * @param sortBy Sorting option: 'price_asc', 'price_desc', 'date_desc'
     * @return PagingSource with filtered and sorted results
     */
    @Query("""
        SELECT * FROM fowls 
        WHERE isForSale = 1 
        AND (:breed IS NULL OR breed LIKE '%' || :breed || '%')
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        AND (:location IS NULL OR location LIKE '%' || :location || '%')
        ORDER BY 
            CASE WHEN :sortBy = 'price_asc' THEN price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN price END DESC,
            CASE WHEN :sortBy = 'date_desc' THEN createdAt END DESC,
            createdAt DESC
    """)
    fun getMarketplaceFowlsPaged(
        breed: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        location: String? = null,
        sortBy: String = "date_desc"
    ): PagingSource<Int, Fowl>
    
    /**
     * Optimized search with FTS (Full Text Search) support
     */
    @Query("""
        SELECT * FROM fowls 
        WHERE fowls MATCH :searchQuery
        ORDER BY rank
        LIMIT :limit
    """)
    suspend fun searchFowlsFTS(searchQuery: String, limit: Int = 20): List<Fowl>
    
    /**
     * Batch operations for better performance
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowlsBatch(fowls: List<Fowl>)
    
    @Update
    suspend fun updateFowlsBatch(fowls: List<Fowl>)
    
    /**
     * Optimized count queries
     */
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId")
    suspend fun getUserFowlCount(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE isForSale = 1")
    suspend fun getMarketplaceFowlCount(): Int
    
    /**
     * Lightweight queries for UI performance
     */
    @Query("""
        SELECT id, name, breed, imageUrl, price, isForSale 
        FROM fowls 
        WHERE ownerId = :ownerId 
        ORDER BY createdAt DESC
    """)
    fun getUserFowlsLight(ownerId: String): Flow<List<FowlLight>>
    
    /**
     * Memory-efficient data class for lists
     */
    data class FowlLight(
        val id: String,
        val name: String,
        val breed: String,
        val imageUrl: String,
        val price: Double,
        val isForSale: Boolean
    )
    
    // ==================== ANALYTICS QUERIES ====================
    
    @Query("""
        SELECT breed, COUNT(*) as count 
        FROM fowls 
        WHERE ownerId = :ownerId 
        GROUP BY breed 
        ORDER BY count DESC
    """)
    suspend fun getBreedDistribution(ownerId: String): List<BreedCount>
    
    @Query("""
        SELECT 
            AVG(price) as avgPrice,
            MIN(price) as minPrice,
            MAX(price) as maxPrice,
            COUNT(*) as totalCount
        FROM fowls 
        WHERE isForSale = 1 AND breed = :breed
    """)
    suspend fun getBreedPriceStats(breed: String): PriceStats?
    
    data class BreedCount(val breed: String, val count: Int)
    data class PriceStats(
        val avgPrice: Double,
        val minPrice: Double,
        val maxPrice: Double,
        val totalCount: Int
    )
}
