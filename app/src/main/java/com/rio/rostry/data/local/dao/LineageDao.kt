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
    @Query("SELECT * FROM fowl_lineages WHERE fowlId = :fowlId LIMIT 1")
    suspend fun getLineageByFowlId(fowlId: String): FowlLineage?

    /**
     * Get lineage by fowl ID as Flow for reactive updates
     */
    @Query("SELECT * FROM fowl_lineages WHERE fowlId = :fowlId LIMIT 1")
    fun getLineageByFowlIdFlow(fowlId: String): Flow<FowlLineage?>

    /**
     * Get lineage by ID
     */
    @Query("SELECT * FROM fowl_lineages WHERE id = :id LIMIT 1")
    suspend fun getLineageById(id: String): FowlLineage?

    /**
     * Get all lineages
     */
    @Query("SELECT * FROM fowl_lineages ORDER BY generation ASC, createdAt DESC")
    fun getAllLineages(): Flow<List<FowlLineage>>

    /**
     * Get all lineages as list (for breeding recommendations)
     */
    @Query("SELECT * FROM fowl_lineages ORDER BY generation ASC, createdAt DESC")
    suspend fun getAllLineagesList(): List<FowlLineage>

    /**
     * Get lineages by generation
     */
    @Query("SELECT * FROM fowl_lineages WHERE generation = :generation ORDER BY createdAt DESC")
    fun getLineagesByGeneration(generation: Int): Flow<List<FowlLineage>>

    /**
     * Get lineages by bloodline
     */
    @Query("SELECT * FROM fowl_lineages WHERE bloodlineId = :bloodlineId ORDER BY generation ASC")
    fun getLineagesByBloodline(bloodlineId: String): Flow<List<FowlLineage>>

    /**
     * Get offspring of a specific fowl
     */
    @Query("SELECT * FROM fowl_lineages WHERE parentMaleId = :parentId OR parentFemaleId = :parentId ORDER BY createdAt DESC")
    fun getOffspringByParent(parentId: String): Flow<List<FowlLineage>>

    /**
     * Get lineages by parent ID (for family tree)
     */
    @Query("SELECT * FROM fowl_lineages WHERE parentMaleId = :parentId OR parentFemaleId = :parentId ORDER BY createdAt DESC")
    suspend fun getLineagesByParentId(parentId: String): List<FowlLineage>

    /**
     * Get siblings (same parents)
     */
    @Query("""
        SELECT * FROM fowl_lineages 
        WHERE (parentMaleId = :parentMaleId AND parentFemaleId = :parentFemaleId)
        AND fowlId != :excludeFowlId
        ORDER BY createdAt DESC
    """)
    fun getSiblings(parentMaleId: String?, parentFemaleId: String?, excludeFowlId: String): Flow<List<FowlLineage>>

    /**
     * Get verified lineages only
     */
    @Query("SELECT * FROM fowl_lineages WHERE lineageVerified = 1 ORDER BY generation ASC")
    fun getVerifiedLineages(): Flow<List<FowlLineage>>

    /**
     * Get lineages needing verification
     */
    @Query("SELECT * FROM fowl_lineages WHERE lineageVerified = 0 ORDER BY createdAt ASC")
    fun getUnverifiedLineages(): Flow<List<FowlLineage>>

    /**
     * Get potential breeding pairs count
     */
    @Query("""
        SELECT COUNT(*) FROM fowl_lineages l1, fowl_lineages l2
        WHERE l1.fowlId != l2.fowlId
        AND l1.bloodlineId != l2.bloodlineId
        AND l1.inbreedingCoefficient < 0.25
        AND l2.inbreedingCoefficient < 0.25
        AND l1.lineageVerified = 1
        AND l2.lineageVerified = 1
    """)
    suspend fun getOptimalBreedingPairsCount(): Int

    /**
     * Get direct offspring count
     */
    @Query("SELECT COUNT(*) FROM fowl_lineages WHERE parentMaleId = :fowlId OR parentFemaleId = :fowlId")
    suspend fun getDirectOffspringCount(fowlId: String): Int

    /**
     * Get generation count for fowl
     */
    @Query("SELECT generation FROM fowl_lineages WHERE fowlId = :fowlId LIMIT 1")
    suspend fun getFowlGeneration(fowlId: String): Int?

    /**
     * Calculate inbreeding coefficient for a fowl
     */
    @Query("""
        SELECT COUNT(*) as sharedAncestors
        FROM fowl_lineages l1, fowl_lineages l2
        WHERE l1.fowlId = :fowlId
        AND (l1.parentMaleId = l2.parentMaleId OR l1.parentFemaleId = l2.parentFemaleId)
        AND l1.fowlId != l2.fowlId
    """)
    suspend fun calculateInbreedingRisk(fowlId: String): Int

    /**
     * Get total lineage count
     */
    @Query("SELECT COUNT(*) FROM fowl_lineages")
    suspend fun getTotalLineageCount(): Int

    /**
     * Get verified lineage count
     */
    @Query("SELECT COUNT(*) FROM fowl_lineages WHERE lineageVerified = 1")
    suspend fun getVerifiedLineageCount(): Int

    /**
     * Get average generation
     */
    @Query("SELECT AVG(generation) FROM fowl_lineages")
    suspend fun getAverageGeneration(): Double

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
     * Get total bloodline count
     */
    @Query("SELECT COUNT(*) FROM bloodlines")
    suspend fun getTotalBloodlineCount(): Int

    /**
     * Get average genetic diversity
     */
    @Query("SELECT AVG(geneticDiversity) FROM bloodlines")
    suspend fun getAverageGeneticDiversity(): Double

    /**
     * Get certified bloodline count
     */
    @Query("SELECT COUNT(*) FROM bloodlines WHERE certificationLevel != 'UNVERIFIED'")
    suspend fun getCertifiedBloodlineCount(): Int

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
            SELECT COUNT(*) FROM fowl_lineages 
            WHERE bloodlineId = :bloodlineId 
            AND fowlId IN (
                SELECT fowlId FROM fowl_lifecycles 
                WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
            )
        ),
        totalOffspring = (
            SELECT COUNT(*) FROM fowl_lineages 
            WHERE bloodlineId = :bloodlineId
        ),
        updatedAt = :currentTime
        WHERE id = :bloodlineId
    """)
    suspend fun updateBloodlineMetrics(bloodlineId: String, currentTime: Long = System.currentTimeMillis())

    /**
     * Delete lineages older than specified date
     */
    @Query("DELETE FROM fowl_lineages WHERE createdAt < :cutoffDate")
    suspend fun deleteOldLineages(cutoffDate: Long)

    /**
     * Delete bloodlines with no active members
     */
    @Query("DELETE FROM bloodlines WHERE activeBreeders = 0 AND totalOffspring = 0")
    suspend fun deleteInactiveBloodlines()

    /**
     * Get breeding candidate count
     */
    @Query("""
        SELECT COUNT(*) FROM fowl_lineages
        WHERE fowlId IN (
            SELECT fowlId FROM fowl_lifecycles 
            WHERE currentStage IN ('ADULT', 'BREEDER_ACTIVE')
        )
    """)
    suspend fun getBreedingCandidateCount(): Int
}