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
    @Query("SELECT * FROM fowl_lifecycle WHERE fowlId = :fowlId LIMIT 1")
    suspend fun getLifecycleByFowlId(fowlId: String): FowlLifecycle?

    /**
     * Get lifecycle by fowl ID as Flow for reactive updates
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE fowlId = :fowlId LIMIT 1")
    fun getLifecycleByFowlIdFlow(fowlId: String): Flow<FowlLifecycle?>

    /**
     * Get lifecycle by ID
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE id = :id LIMIT 1")
    suspend fun getLifecycleById(id: String): FowlLifecycle?

    /**
     * Get all lifecycles
     */
    @Query("SELECT * FROM fowl_lifecycle ORDER BY createdAt DESC")
    fun getAllLifecycles(): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycles by current stage
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE currentStage = :stage ORDER BY stageStartDate DESC")
    fun getLifecyclesByStage(stage: LifecycleStage): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycles by batch ID
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE batchId = :batchId ORDER BY createdAt DESC")
    fun getLifecyclesByBatch(batchId: String): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycles by parent (male or female)
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE parentMaleId = :parentId OR parentFemaleId = :parentId ORDER BY createdAt DESC")
    fun getLifecyclesByParent(parentId: String): Flow<List<FowlLifecycle>>

    /**
     * Get breeding candidates (adult stage fowls)
     */
    @Query("SELECT * FROM fowl_lifecycle WHERE isBreederCandidate = 1 AND currentStage IN ('ADULT', 'BREEDER_ACTIVE') ORDER BY updatedAt DESC")
    fun getBreedingCandidates(): Flow<List<FowlLifecycle>>

    /**
     * Get fowls ready for stage transition
     */
    @Query("""
        SELECT * FROM fowl_lifecycle 
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
     * Get lifecycle statistics by stage
     */
    @Query("""
        SELECT currentStage, COUNT(*) as count 
        FROM fowl_lifecycle 
        GROUP BY currentStage
    """)
    suspend fun getStageDistribution(): Map<LifecycleStage, Int>

    /**
     * Get average growth metrics for a specific stage
     */
    @Query("""
        SELECT AVG(json_extract(growthMetrics, '$[*].weight')) as avgWeight,
               AVG(json_extract(growthMetrics, '$[*].height')) as avgHeight
        FROM fowl_lifecycle 
        WHERE currentStage = :stage
    """)
    suspend fun getAverageGrowthForStage(stage: LifecycleStage): Map<String, Double>

    /**
     * Get fowls with overdue milestones
     */
    @Query("""
        SELECT * FROM fowl_lifecycle 
        WHERE expectedNextStageDate > 0 
        AND expectedNextStageDate < :currentTime 
        AND currentStage NOT IN ('ADULT', 'BREEDER_ACTIVE')
        ORDER BY expectedNextStageDate ASC
    """)
    fun getOverdueFowls(currentTime: Long = System.currentTimeMillis()): Flow<List<FowlLifecycle>>

    /**
     * Search lifecycles by fowl characteristics
     */
    @Query("""
        SELECT fl.* FROM fowl_lifecycle fl
        INNER JOIN fowls f ON fl.fowlId = f.id
        WHERE f.name LIKE '%' || :query || '%' 
        OR f.breed LIKE '%' || :query || '%'
        OR fl.batchId LIKE '%' || :query || '%'
        ORDER BY fl.updatedAt DESC
    """)
    fun searchLifecycles(query: String): Flow<List<FowlLifecycle>>

    /**
     * Get lifecycle count by user/owner
     */
    @Query("""
        SELECT COUNT(*) FROM fowl_lifecycle fl
        INNER JOIN fowls f ON fl.fowlId = f.id
        WHERE f.ownerId = :ownerId
    """)
    suspend fun getLifecycleCountByOwner(ownerId: String): Int

    /**
     * Get recent lifecycle activities (last 30 days)
     */
    @Query("""
        SELECT * FROM fowl_lifecycle 
        WHERE updatedAt >= :thirtyDaysAgo 
        ORDER BY updatedAt DESC
        LIMIT :limit
    """)
    fun getRecentLifecycleActivities(
        thirtyDaysAgo: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000),
        limit: Int = 50
    ): Flow<List<FowlLifecycle>>

    /**
     * Get breeding performance metrics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalBreeders,
            AVG(CASE WHEN currentStage = 'BREEDER_ACTIVE' THEN 1 ELSE 0 END) as activeBreederRate,
            COUNT(CASE WHEN isBreederCandidate = 1 THEN 1 END) as breedingCandidates
        FROM fowl_lifecycle 
        WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
    """)
    suspend fun getBreedingMetrics(): Map<String, Double>

    /**
     * Delete lifecycles older than specified date
     */
    @Query("DELETE FROM fowl_lifecycle WHERE createdAt < :cutoffDate")
    suspend fun deleteOldLifecycles(cutoffDate: Long)

    /**
     * Get lifecycle completion rate by batch
     */
    @Query("""
        SELECT 
            batchId,
            COUNT(*) as total,
            COUNT(CASE WHEN currentStage IN ('ADULT', 'BREEDER_ACTIVE') THEN 1 END) as completed
        FROM fowl_lifecycle 
        WHERE batchId IS NOT NULL 
        GROUP BY batchId
    """)
    suspend fun getBatchCompletionRates(): Map<String, Map<String, Int>>

    /**
     * Get fowls by generation (for lineage analysis)
     */
    @Query("""
        SELECT fl.* FROM fowl_lifecycle fl
        INNER JOIN fowl_lineage lin ON fl.fowlId = lin.fowlId
        WHERE lin.generation = :generation
        ORDER BY fl.createdAt DESC
    """)
    fun getFowlsByGeneration(generation: Int): Flow<List<FowlLifecycle>>
}