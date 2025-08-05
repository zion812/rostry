package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.FowlLifecycle
import com.rio.rostry.data.model.LifecycleStage
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fowl lifecycle operations
 * Provides comprehensive lifecycle tracking functionality
 */
@Dao
interface LifecycleDao {

    /**
     * Insert a new lifecycle record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifecycle(lifecycle: FowlLifecycle)

    /**
     * Insert multiple lifecycle records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifecycles(lifecycles: List<FowlLifecycle>)

    /**
     * Update existing lifecycle record
     */
    @Update
    suspend fun updateLifecycle(lifecycle: FowlLifecycle)

    /**
     * Delete lifecycle record
     */
    @Delete
    suspend fun deleteLifecycle(lifecycle: FowlLifecycle)

    /**
     * Get lifecycle by fowl ID
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE fowlId = :fowlId LIMIT 1")
    suspend fun getLifecycleByFowlId(fowlId: String): FowlLifecycle?

    /**
     * Get lifecycle by fowl ID as Flow for reactive updates
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE fowlId = :fowlId LIMIT 1")
    fun getLifecycleByFowlIdFlow(fowlId: String): Flow<FowlLifecycle?>

    /**
     * Get lifecycle by ID
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE id = :id LIMIT 1")
    suspend fun getLifecycleById(id: String): FowlLifecycle?

    /**
     * Get all lifecycles
     */
    @Query("SELECT * FROM fowl_lifecycles ORDER BY createdAt DESC")
    fun getAllLifecycles(): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycles by current stage
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE currentStage = :stage ORDER BY stageStartDate DESC")
    fun getLifecyclesByStage(stage: String): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycles by farm ID
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE farmId = :farmId ORDER BY createdAt DESC")
    fun getLifecyclesByFarm(farmId: String): Flow<List<FowlLifecycle>>

    /**
     * Get breeding candidates (adult stage fowls)
     */
    @Query("SELECT * FROM fowl_lifecycles WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE') ORDER BY updatedAt DESC")
    fun getBreedingCandidates(): Flow<List<FowlLifecycle>>

    /**
     * Get fowls ready for stage transition
     */
    @Query("""
        SELECT * FROM fowl_lifecycles 
        WHERE (stageStartDate + (CASE 
            WHEN currentStage = 'EGG' THEN 3 * 7 * 24 * 60 * 60 * 1000
            WHEN currentStage = 'HATCHING' THEN 1 * 7 * 24 * 60 * 60 * 1000
            WHEN currentStage = 'CHICK' THEN 16 * 7 * 24 * 60 * 60 * 1000
            WHEN currentStage = 'JUVENILE' THEN 32 * 7 * 24 * 60 * 60 * 1000
            ELSE 0
        END)) <= :currentTime
        AND currentStage NOT IN ('ADULT', 'BREEDER_ACTIVE')
        ORDER BY stageStartDate ASC
    """)
    fun getFowlsReadyForTransition(currentTime: Long = System.currentTimeMillis()): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycle count by stage
     */
    @Query("SELECT COUNT(*) FROM fowl_lifecycles WHERE currentStage = :stage")
    suspend fun getLifecycleCountByStage(stage: String): Int

    /**
     * Get fowls with overdue milestones
     */
    @Query("""
        SELECT * FROM fowl_lifecycles 
        WHERE expectedTransitionDate > 0 
        AND expectedTransitionDate < :currentTime 
        AND currentStage NOT IN ('ADULT', 'BREEDER_ACTIVE')
        ORDER BY expectedTransitionDate ASC
    """)
    fun getOverdueFowls(currentTime: Long = System.currentTimeMillis()): Flow<List<FowlLifecycle>>

    /**
     * Search lifecycles by fowl characteristics
     */
    @Query("""
        SELECT fl.* FROM fowl_lifecycles fl
        INNER JOIN fowls f ON fl.fowlId = f.id
        WHERE f.name LIKE '%' || :query || '%' 
        OR f.breed LIKE '%' || :query || '%'
        ORDER BY fl.updatedAt DESC
    """)
    fun searchLifecycles(query: String): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycle count by user/owner
     */
    @Query("""
        SELECT COUNT(*) FROM fowl_lifecycles fl
        INNER JOIN fowls f ON fl.fowlId = f.id
        WHERE f.ownerId = :ownerId
    """)
    suspend fun getLifecycleCountByOwner(ownerId: String): Int

    /**
     * Get recent lifecycle activities (last 30 days)
     */
    @Query("""
        SELECT * FROM fowl_lifecycles 
        WHERE updatedAt >= :thirtyDaysAgo 
        ORDER BY updatedAt DESC
        LIMIT :limit
    """)
    fun getRecentLifecycleActivities(
        thirtyDaysAgo: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
        limit: Int = 50
    ): Flow<List<FowlLifecycle>>

    /**
     * Get total breeder count
     */
    @Query("SELECT COUNT(*) FROM fowl_lifecycles WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')")
    suspend fun getTotalBreederCount(): Int

    /**
     * Delete lifecycles older than specified date
     */
    @Query("DELETE FROM fowl_lifecycles WHERE createdAt < :cutoffDate")
    suspend fun deleteOldLifecycles(cutoffDate: Long)

    /**
     * Get fowls by generation (for lineage analysis)
     */
    @Query("""
        SELECT fl.* FROM fowl_lifecycles fl
        INNER JOIN fowl_lineages lin ON fl.fowlId = lin.fowlId
        WHERE lin.generation = :generation
        ORDER BY fl.createdAt DESC
    """)
    fun getFowlsByGeneration(generation: Int): Flow<List<FowlLifecycle>>
}