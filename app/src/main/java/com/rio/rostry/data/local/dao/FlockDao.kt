package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Flock
import com.rio.rostry.data.model.FlockType
import com.rio.rostry.data.model.FlockHealthStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for flock operations
 * Provides comprehensive flock management functionality
 */
@Dao
interface FlockDao {

    /**
     * Insert a new flock
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlock(flock: Flock)

    /**
     * Insert multiple flocks
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlocks(flocks: List<Flock>)

    /**
     * Update existing flock
     */
    @Update
    suspend fun updateFlock(flock: Flock)

    /**
     * Delete flock
     */
    @Delete
    suspend fun deleteFlock(flock: Flock)

    /**
     * Get flock by ID
     */
    @Query("SELECT * FROM flocks WHERE id = :flockId LIMIT 1")
    suspend fun getFlockById(flockId: String): Flock?

    /**
     * Get flock by ID as Flow for reactive updates
     */
    @Query("SELECT * FROM flocks WHERE id = :flockId LIMIT 1")
    fun getFlockByIdFlow(flockId: String): Flow<Flock?>

    /**
     * Get all flocks
     */
    @Query("SELECT * FROM flocks WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllFlocks(): Flow<List<Flock>>

    /**
     * Get flocks by farm ID
     */
    @Query("SELECT * FROM flocks WHERE farmId = :farmId AND isActive = 1 ORDER BY createdAt DESC")
    fun getFlocksByFarm(farmId: String): Flow<List<Flock>>

    /**
     * Get flocks by type
     */
    @Query("SELECT * FROM flocks WHERE flockType = :flockType AND isActive = 1 ORDER BY createdAt DESC")
    fun getFlocksByType(flockType: FlockType): Flow<List<Flock>>

    /**
     * Get flocks by health status
     */
    @Query("SELECT * FROM flocks WHERE healthStatus = :healthStatus AND isActive = 1 ORDER BY updatedAt DESC")
    fun getFlocksByHealthStatus(healthStatus: FlockHealthStatus): Flow<List<Flock>>

    /**
     * Get flocks by breed
     */
    @Query("SELECT * FROM flocks WHERE breed LIKE '%' || :breed || '%' AND isActive = 1 ORDER BY flockName ASC")
    fun getFlocksByBreed(breed: String): Flow<List<Flock>>

    /**
     * Get flocks by facility
     */
    @Query("SELECT * FROM flocks WHERE facilityId = :facilityId AND isActive = 1 ORDER BY createdAt DESC")
    fun getFlocksByFacility(facilityId: String): Flow<List<Flock>>

    /**
     * Search flocks by name
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE flockName LIKE '%' || :query || '%' 
        AND isActive = 1 
        ORDER BY flockName ASC
    """)
    fun searchFlocks(query: String): Flow<List<Flock>>

    /**
     * Get flocks with high mortality rate
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE ((totalCount - activeCount) * 100.0 / totalCount) > :mortalityThreshold 
        AND totalCount > 0 
        AND isActive = 1 
        ORDER BY ((totalCount - activeCount) * 100.0 / totalCount) DESC
    """)
    fun getFlocksWithHighMortality(mortalityThreshold: Double = 5.0): Flow<List<Flock>>

    /**
     * Get flocks needing attention (health issues, overdue vaccinations, etc.)
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE (healthStatus IN ('MONITORING', 'TREATMENT', 'QUARANTINE')
        OR ((totalCount - activeCount) * 100.0 / totalCount) > 5.0)
        AND isActive = 1 
        ORDER BY updatedAt DESC
    """)
    fun getFlocksNeedingAttention(): Flow<List<Flock>>

    /**
     * Get breeding flocks
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE flockType IN ('BREEDING_STOCK', 'ROOSTERS') 
        AND healthStatus = 'HEALTHY' 
        AND isActive = 1 
        ORDER BY averageAge DESC
    """)
    fun getBreedingFlocks(): Flow<List<Flock>>

    /**
     * Get laying hens
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE flockType = 'LAYING_HENS' 
        AND isActive = 1 
        ORDER BY createdAt DESC
    """)
    fun getLayingHens(): Flow<List<Flock>>

    /**
     * Get flocks by age range
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE averageAge BETWEEN :minAge AND :maxAge 
        AND isActive = 1 
        ORDER BY averageAge ASC
    """)
    fun getFlocksByAgeRange(minAge: Int, maxAge: Int): Flow<List<Flock>>

    /**
     * Get flocks by size range
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE activeCount BETWEEN :minSize AND :maxSize 
        AND isActive = 1 
        ORDER BY activeCount DESC
    """)
    fun getFlocksBySizeRange(minSize: Int, maxSize: Int): Flow<List<Flock>>

    /**
     * Get recently updated flocks
     */
    @Query("""
        SELECT * FROM flocks 
        WHERE updatedAt >= :since 
        AND isActive = 1 
        ORDER BY updatedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyUpdatedFlocks(
        since: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
        limit: Int = 10
    ): Flow<List<Flock>>

    /**
     * Get flock statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalFlocks,
            SUM(activeCount) as totalFowls,
            AVG(activeCount) as averageFlockSize,
            AVG(averageAge) as averageAge,
            COUNT(CASE WHEN healthStatus = 'HEALTHY' THEN 1 END) as healthyFlocks,
            AVG((totalCount - activeCount) * 100.0 / NULLIF(totalCount, 0)) as averageMortalityRate
        FROM flocks 
        WHERE isActive = 1
    """)
    suspend fun getFlockStatistics(): Map<String, Double>

    /**
     * Get production summary for laying hens
     */
    @Query("""
        SELECT 
            COUNT(*) as layingFlocks,
            SUM(activeCount) as totalLayingHens,
            AVG(json_extract(productionMetrics, '$.eggProductionRate')) as avgProductionRate,
            AVG(json_extract(productionMetrics, '$.averageEggWeight')) as avgEggWeight,
            AVG(json_extract(productionMetrics, '$.feedConversionRatio')) as avgFeedConversion
        FROM flocks 
        WHERE flockType = 'LAYING_HENS' 
        AND isActive = 1
        AND productionMetrics IS NOT NULL
    """)
    suspend fun getProductionSummary(): Map<String, Double>

    /**
     * Get flocks with overdue vaccinations
     */
    @Query("""
        SELECT f.* FROM flocks f
        WHERE EXISTS (
            SELECT 1 FROM json_each(f.vaccinationSchedule) v
            WHERE json_extract(v.value, '$.nextDueDate') > 0
            AND json_extract(v.value, '$.nextDueDate') < :currentTime
        )
        AND f.isActive = 1
        ORDER BY f.updatedAt DESC
    """)
    fun getFlocksWithOverdueVaccinations(
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<Flock>>

    /**
     * Get flocks with upcoming vaccinations
     */
    @Query("""
        SELECT f.* FROM flocks f
        WHERE EXISTS (
            SELECT 1 FROM json_each(f.vaccinationSchedule) v
            WHERE json_extract(v.value, '$.nextDueDate') BETWEEN :currentTime AND :dueDate
        )
        AND f.isActive = 1
        ORDER BY f.updatedAt DESC
    """)
    fun getFlocksWithUpcomingVaccinations(
        currentTime: Long = System.currentTimeMillis(),
        dueDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days from now
    ): Flow<List<Flock>>

    /**
     * Get flocks by performance rating
     */
    @Query("""
        SELECT f.* FROM flocks f
        WHERE f.productionMetrics IS NOT NULL
        AND json_extract(f.productionMetrics, '$.eggProductionRate') >= :minProductionRate
        AND json_extract(f.productionMetrics, '$.feedConversionRatio') <= :maxFeedConversion
        AND f.isActive = 1
        ORDER BY json_extract(f.productionMetrics, '$.eggProductionRate') DESC
    """)
    fun getHighPerformingFlocks(
        minProductionRate: Double = 0.7,
        maxFeedConversion: Double = 2.5
    ): Flow<List<Flock>>

    /**
     * Update flock count
     */
    @Query("""
        UPDATE flocks 
        SET activeCount = :activeCount, 
            totalCount = :totalCount, 
            updatedAt = :timestamp 
        WHERE id = :flockId
    """)
    suspend fun updateFlockCount(
        flockId: String, 
        activeCount: Int, 
        totalCount: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update flock health status
     */
    @Query("""
        UPDATE flocks 
        SET healthStatus = :healthStatus, 
            updatedAt = :timestamp 
        WHERE id = :flockId
    """)
    suspend fun updateHealthStatus(
        flockId: String, 
        healthStatus: FlockHealthStatus,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update flock average age
     */
    @Query("""
        UPDATE flocks 
        SET averageAge = :averageAge, 
            updatedAt = :timestamp 
        WHERE id = :flockId
    """)
    suspend fun updateAverageAge(
        flockId: String, 
        averageAge: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Deactivate flock
     */
    @Query("""
        UPDATE flocks 
        SET isActive = 0, 
            updatedAt = :timestamp 
        WHERE id = :flockId
    """)
    suspend fun deactivateFlock(flockId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Get flock count by type
     */
    @Query("""
        SELECT flockType, COUNT(*) as count 
        FROM flocks 
        WHERE isActive = 1 
        GROUP BY flockType
    """)
    suspend fun getFlockCountByType(): Map<FlockType, Int>

    /**
     * Get flock count by health status
     */
    @Query("""
        SELECT healthStatus, COUNT(*) as count 
        FROM flocks 
        WHERE isActive = 1 
        GROUP BY healthStatus
    """)
    suspend fun getFlockCountByHealthStatus(): Map<FlockHealthStatus, Int>

    /**
     * Get total fowl count
     */
    @Query("SELECT SUM(activeCount) FROM flocks WHERE isActive = 1")
    suspend fun getTotalFowlCount(): Int

    /**
     * Get average flock size by type
     */
    @Query("""
        SELECT flockType, AVG(activeCount) as avgSize 
        FROM flocks 
        WHERE isActive = 1 
        GROUP BY flockType
    """)
    suspend fun getAverageFlockSizeByType(): Map<FlockType, Double>

    /**
     * Delete old inactive flocks
     */
    @Query("""
        DELETE FROM flocks 
        WHERE isActive = 0 AND updatedAt < :cutoffDate
    """)
    suspend fun deleteOldInactiveFlocks(cutoffDate: Long)

    /**
     * Get flocks with environmental alerts
     */
    @Query("""
        SELECT f.* FROM flocks f
        WHERE f.environmentalConditions IS NOT NULL
        AND (
            json_extract(f.environmentalConditions, '$.temperature.current') NOT BETWEEN 18 AND 26
            OR json_extract(f.environmentalConditions, '$.humidity.current') NOT BETWEEN 50 AND 70
            OR json_extract(f.environmentalConditions, '$.airQuality.co2Level') > 3000
        )
        AND f.isActive = 1
        ORDER BY f.updatedAt DESC
    """)
    fun getFlocksWithEnvironmentalAlerts(): Flow<List<Flock>>

    /**
     * Get flocks by facility utilization
     */
    @Query("""
        SELECT f.* FROM flocks f
        INNER JOIN farms farm ON f.farmId = farm.id
        WHERE f.facilityId IS NOT NULL
        AND f.isActive = 1
        ORDER BY f.activeCount DESC
    """)
    fun getFlocksByFacilityUtilization(): Flow<List<Flock>>

    /**
     * Get monthly production trends
     */
    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(createdAt/1000, 'unixepoch')) as month,
            COUNT(*) as flockCount,
            SUM(activeCount) as totalFowls,
            AVG(json_extract(productionMetrics, '$.eggProductionRate')) as avgProduction
        FROM flocks 
        WHERE flockType = 'LAYING_HENS' 
        AND isActive = 1
        AND createdAt >= :startDate
        GROUP BY strftime('%Y-%m', datetime(createdAt/1000, 'unixepoch'))
        ORDER BY month DESC
    """)
    suspend fun getMonthlyProductionTrends(
        startDate: Long = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L) // 1 year ago
    ): List<Map<String, Any>>
}