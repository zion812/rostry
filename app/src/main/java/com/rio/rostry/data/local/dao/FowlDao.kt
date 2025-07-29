package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Fowl
import kotlinx.coroutines.flow.Flow

@Dao
interface FowlDao {
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    suspend fun getFowlById(fowlId: String): Fowl?
    
    @Query("SELECT * FROM fowls WHERE id = :fowlId")
    fun getFowlByIdFlow(fowlId: String): Flow<Fowl?>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId")
    fun getFowlsByOwner(ownerId: String): Flow<List<Fowl>>
    
    @Query("SELECT * FROM fowls WHERE isForSale = 1")
    fun getMarketplaceFowls(): Flow<List<Fowl>>
    
    @Query("SELECT * FROM fowls")
    fun getAllFowls(): Flow<List<Fowl>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFowl(fowl: Fowl)
    
    @Update
    suspend fun updateFowl(fowl: Fowl)
    
    @Delete
    suspend fun deleteFowl(fowl: Fowl)
    
    @Query("DELETE FROM fowls WHERE id = :fowlId")
    suspend fun deleteFowlById(fowlId: String)
    
    @Query("SELECT * FROM fowls WHERE isForSale = 1 AND (name LIKE :query OR breed LIKE :query OR description LIKE :query)")
    fun searchMarketplaceFowls(query: String): Flow<List<Fowl>>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId")
    suspend fun getFowlsByOwnerSync(ownerId: String): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE id IN (:fowlIds)")
    suspend fun getFowlsByIds(fowlIds: List<String>): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND status = 'deceased' ORDER BY updatedAt DESC")
    suspend fun getDeceasedFowls(ownerId: String): List<Fowl>
    
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId")
    suspend fun getFowlCount(ownerId: String): Int
    
    @Query("SELECT COUNT(*) FROM fowls WHERE ownerId = :ownerId AND status = 'deceased'")
    suspend fun getDeceasedCount(ownerId: String): Int
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND (motherId = :fowlId OR fatherId = :fowlId)")
    suspend fun getOffspring(ownerId: String, fowlId: String): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE id = :motherId OR id = :fatherId")
    suspend fun getParents(motherId: String?, fatherId: String?): List<Fowl>
    
    // Additional methods needed by FowlRepository and DashboardRepository
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFowls(ownerId: String, limit: Int): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND isForSale = 1")
    suspend fun getActiveSales(ownerId: String): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND createdAt BETWEEN :startDate AND :endDate")
    suspend fun getFowlsCreatedBetween(ownerId: String, startDate: Long, endDate: Long): List<Fowl>
    
    @Query("SELECT * FROM fowls WHERE ownerId = :ownerId AND status = 'Sold' AND updatedAt BETWEEN :startDate AND :endDate")
    suspend fun getFowlsSoldBetween(ownerId: String, startDate: Long, endDate: Long): List<Fowl>
}