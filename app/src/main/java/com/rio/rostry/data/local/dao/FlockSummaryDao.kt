package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.FlockSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface FlockSummaryDao {
    
    @Query("SELECT * FROM flock_summary WHERE ownerId = :ownerId")
    suspend fun getFlockSummary(ownerId: String): FlockSummary?
    
    @Query("SELECT * FROM flock_summary WHERE ownerId = :ownerId")
    fun getFlockSummaryFlow(ownerId: String): Flow<FlockSummary?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlockSummary(flockSummary: FlockSummary)
    
    @Update
    suspend fun updateFlockSummary(flockSummary: FlockSummary)
    
    @Delete
    suspend fun deleteFlockSummary(flockSummary: FlockSummary)
    
    @Query("DELETE FROM flock_summary WHERE ownerId = :ownerId")
    suspend fun deleteFlockSummaryByOwnerId(ownerId: String)
    
    @Query("SELECT * FROM flock_summary")
    suspend fun getAllFlockSummaries(): List<FlockSummary>
}