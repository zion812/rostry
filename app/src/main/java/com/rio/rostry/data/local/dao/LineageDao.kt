package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.FowlLineage
import com.rio.rostry.data.model.Bloodline
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for fowl lineage and bloodline operations
 * Provides comprehensive family tree and genetic tracking functionality
 */
@Dao
interface LineageDao {

    // ==================== FOWL LINEAGE OPERATIONS ====================

    /**
     * Insert a new lineage record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineage(lineage: FowlLineage)

    /**
     * Insert multiple lineage records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineages(lineages: List<FowlLineage>)

    /**
     * Update existing lineage record
     */
    @Update
    suspend fun updateLineage(lineage: FowlLineage)

    /**
     * Delete lineage record
     */
    @Delete
    suspend fun deleteLineage(lineage: FowlLineage)

    /**
     * Get lineage by fowl ID
     */
    @Query("SELECT * FROM fowl_lineage WHERE fowlId = :fowlId LIMIT 1")
    suspend fun getLineageByFowlId(fowlId: String): FowlLineage?

    /**
     * Get lineage by fowl ID as Flow for reactive updates
     */
    @Query("SELECT * FROM fowl_lineage WHERE fowlId = :fowlId LIMIT 1")
    fun getLineageByFowlIdFlow(fowlId: String): Flow<FowlLineage?>

    /**
     * Get lineage by ID
     */
    @Query("SELECT * FROM fowl_lineage WHERE id = :id LIMIT 1")
    suspend fun getLineageById(id: String): FowlLineage?

    /**
     * Get all lineages
     */
    @Query("SELECT * FROM fowl_lineage ORDER BY generation ASC, createdAt DESC")
    fun getAllLineages(): Flow<List<FowlLineage>>

    /**
     * Get lineages by generation
     */
    @Query("SELECT * FROM fowl_lineage WHERE generation = :generation ORDER BY createdAt DESC")
    fun getLineagesByGeneration(generation: Int): Flow<List<FowlLineage>>

    /**
     * Get lineages by bloodline
     */
    @Query("SELECT * FROM fowl_lineage WHERE bloodlineId = :bloodlineId ORDER BY generation ASC")
    fun getLineagesByBloodline(bloodlineId: String): Flow<List<FowlLineage>>

    /**
     * Get offspring of a specific fowl
     */
    @Query("SELECT * FROM fowl_lineage WHERE parentMaleId = :parentId OR parentFemaleId = :parentId ORDER BY createdAt DESC")
    fun getOffspringByParent(parentId: String): Flow<List<FowlLineage>>

    /**
     * Get siblings (same parents)
     */
    @Query("""
        SELECT * FROM fowl_lineage 
        WHERE (parentMaleId = :parentMaleId AND parentFemaleId = :parentFemaleId)
        AND fowlId != :excludeFowlId
        ORDER BY createdAt DESC
    """)
    fun getSiblings(parentMaleId: String?, parentFemaleId: String?, excludeFowlId: String): Flow<List<FowlLineage>>

    /**
     * Get verified lineages only
     */
    @Query("SELECT * FROM fowl_lineage WHERE lineageVerified = 1 ORDER BY generation ASC")
    fun getVerifiedLineages(): Flow<List<FowlLineage>>

    /**
     * Get lineages needing verification
     */
    @Query("SELECT * FROM fowl_lineage WHERE lineageVerified = 0 ORDER BY createdAt ASC")
    fun getUnverifiedLineages(): Flow<List<FowlLineage>>

    /**
     * Get breeding pairs (potential mates with good compatibility)
     */
    @Query("""
        SELECT l1.*, l2.fowlId as potentialMateId 
        FROM fowl_lineage l1, fowl_lineage l2
        WHERE l1.fowlId != l2.fowlId
        AND l1.bloodlineId != l2.bloodlineId
        AND l1.inbreedingCoefficient < 0.25
        AND l2.inbreedingCoefficient < 0.25
        AND l1.lineageVerified = 1
        AND l2.lineageVerified = 1
        ORDER BY (l1.inbreedingCoefficient + l2.inbreedingCoefficient) ASC
    """)
    suspend fun getOptimalBreedingPairs(): List<Map<String, Any>>

    /**
     * Get family tree ancestors (parents and grandparents)
     */
    @Query("""
        WITH RECURSIVE ancestors AS (
            SELECT fowlId, parentMaleId, parentFemaleId, generation, 0 as level
            FROM fowl_lineage 
            WHERE fowlId = :fowlId
            
            UNION ALL
            
            SELECT l.fowlId, l.parentMaleId, l.parentFemaleId, l.generation, a.level + 1
            FROM fowl_lineage l
            INNER JOIN ancestors a ON (l.fowlId = a.parentMaleId OR l.fowlId = a.parentFemaleId)
            WHERE a.level < 3
        )
        SELECT * FROM ancestors WHERE level > 0
    """)
    suspend fun getAncestors(fowlId: String): List<FowlLineage>

    /**
     * Get family tree descendants (children and grandchildren)
     */
    @Query("""
        WITH RECURSIVE descendants AS (
            SELECT fowlId, parentMaleId, parentFemaleId, generation, 0 as level
            FROM fowl_lineage 
            WHERE fowlId = :fowlId
            
            UNION ALL
            
            SELECT l.fowlId, l.parentMaleId, l.parentFemaleId, l.generation, d.level + 1
            FROM fowl_lineage l
            INNER JOIN descendants d ON (l.parentMaleId = d.fowlId OR l.parentFemaleId = d.fowlId)
            WHERE d.level < 3
        )
        SELECT * FROM descendants WHERE level > 0
    """)
    suspend fun getDescendants(fowlId: String): List<FowlLineage>

    /**
     * Calculate inbreeding coefficient for a fowl
     */
    @Query("""
        SELECT COUNT(*) as sharedAncestors
        FROM fowl_lineage l1, fowl_lineage l2
        WHERE l1.fowlId = :fowlId
        AND (l1.parentMaleId = l2.parentMaleId OR l1.parentFemaleId = l2.parentFemaleId)
        AND l1.fowlId != l2.fowlId
    """)
    suspend fun calculateInbreedingRisk(fowlId: String): Int

    /**
     * Get lineage statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalLineages,
            COUNT(CASE WHEN lineageVerified = 1 THEN 1 END) as verifiedCount,
            AVG(generation) as avgGeneration,
            MAX(generation) as maxGeneration,
            AVG(inbreedingCoefficient) as avgInbreeding
        FROM fowl_lineage
    """)
    suspend fun getLineageStatistics(): Map<String, Double>

    // ==================== BLOODLINE OPERATIONS ====================

    /**
     * Insert a new bloodline
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodline(bloodline: Bloodline)

    /**
     * Insert multiple bloodlines
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodlines(bloodlines: List<Bloodline>)

    /**
     * Update existing bloodline
     */
    @Update
    suspend fun updateBloodline(bloodline: Bloodline)

    /**
     * Delete bloodline
     */
    @Delete
    suspend fun deleteBloodline(bloodline: Bloodline)

    /**
     * Get bloodline by ID
     */
    @Query("SELECT * FROM bloodlines WHERE id = :id LIMIT 1")
    suspend fun getBloodlineById(id: String): Bloodline?

    /**
     * Get bloodline by ID as Flow
     */
    @Query("SELECT * FROM bloodlines WHERE id = :id LIMIT 1")
    fun getBloodlineByIdFlow(id: String): Flow<Bloodline?>

    /**
     * Get all bloodlines
     */
    @Query("SELECT * FROM bloodlines ORDER BY totalGenerations DESC, activeBreeders DESC")
    fun getAllBloodlines(): Flow<List<Bloodline>>

    /**
     * Get bloodlines by certification level
     */
    @Query("SELECT * FROM bloodlines WHERE certificationLevel = :level ORDER BY totalGenerations DESC")
    fun getBloodlinesByCertification(level: String): Flow<List<Bloodline>>

    /**
     * Get top performing bloodlines
     */
    @Query("""
        SELECT * FROM bloodlines 
        WHERE performanceMetrics IS NOT NULL
        ORDER BY json_extract(performanceMetrics, '$.survivalRate') DESC,
                 json_extract(performanceMetrics, '$.breedingSuccessRate') DESC
        LIMIT :limit
    """)
    fun getTopPerformingBloodlines(limit: Int = 10): Flow<List<Bloodline>>

    /**
     * Get bloodlines needing genetic diversification
     */
    @Query("SELECT * FROM bloodlines WHERE geneticDiversity < 0.7 OR totalGenerations > 6 ORDER BY geneticDiversity ASC")
    fun getBloodlinesNeedingDiversification(): Flow<List<Bloodline>>

    /**
     * Search bloodlines by name or characteristics
     */
    @Query("""
        SELECT * FROM bloodlines 
        WHERE name LIKE '%' || :query || '%' 
        OR characteristics LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchBloodlines(query: String): Flow<List<Bloodline>>

    /**
     * Get bloodline performance summary
     */
    @Query("""
        SELECT 
            COUNT(*) as totalBloodlines,
            AVG(activeBreeders) as avgActiveBreeders,
            AVG(totalGenerations) as avgGenerations,
            AVG(geneticDiversity) as avgGeneticDiversity,
            COUNT(CASE WHEN certificationLevel != 'UNVERIFIED' THEN 1 END) as certifiedCount
        FROM bloodlines
    """)
    suspend fun getBloodlinePerformanceSummary(): Map<String, Double>

    /**
     * Get bloodlines by founder fowl
     */
    @Query("SELECT * FROM bloodlines WHERE originFowlId = :fowlId")
    fun getBloodlinesByFounder(fowlId: String): Flow<List<Bloodline>>

    /**
     * Update bloodline metrics
     */
    @Query("""
        UPDATE bloodlines 
        SET activeBreeders = (
            SELECT COUNT(*) FROM fowl_lineage 
            WHERE bloodlineId = :bloodlineId 
            AND fowlId IN (
                SELECT fowlId FROM fowl_lifecycle 
                WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
            )
        ),
        totalOffspring = (
            SELECT COUNT(*) FROM fowl_lineage 
            WHERE bloodlineId = :bloodlineId
        ),
        updatedAt = :currentTime
        WHERE id = :bloodlineId
    """)
    suspend fun updateBloodlineMetrics(bloodlineId: String, currentTime: Long = System.currentTimeMillis())

    /**
     * Delete lineages older than specified date
     */
    @Query("DELETE FROM fowl_lineage WHERE createdAt < :cutoffDate")
    suspend fun deleteOldLineages(cutoffDate: Long)

    /**
     * Delete bloodlines with no active members
     */
    @Query("DELETE FROM bloodlines WHERE activeBreeders = 0 AND totalOffspring = 0")
    suspend fun deleteInactiveBloodlines()

    /**
     * Get breeding recommendations based on genetic compatibility
     */
    @Query("""
        SELECT 
            l1.fowlId as fowl1,
            l2.fowlId as fowl2,
            (1.0 - (l1.inbreedingCoefficient + l2.inbreedingCoefficient) / 2.0) as compatibilityScore
        FROM fowl_lineage l1, fowl_lineage l2
        WHERE l1.fowlId != l2.fowlId
        AND l1.bloodlineId != l2.bloodlineId
        AND l1.lineageVerified = 1
        AND l2.lineageVerified = 1
        AND l1.fowlId IN (
            SELECT fowlId FROM fowl_lifecycle 
            WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
        )
        AND l2.fowlId IN (
            SELECT fowlId FROM fowl_lifecycle 
            WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
        )
        ORDER BY compatibilityScore DESC
        LIMIT :limit
    """)
    suspend fun getBreedingRecommendations(limit: Int = 20): List<Map<String, Any>>
}