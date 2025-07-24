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
}