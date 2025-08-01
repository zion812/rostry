package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Farm
import com.rio.rostry.data.model.FarmType
import com.rio.rostry.data.model.CertificationLevel
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for farm operations
 * Provides comprehensive farm management functionality
 */
@Dao
interface FarmDao {

    /**
     * Insert a new farm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarm(farm: Farm)

    /**
     * Insert multiple farms
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarms(farms: List<Farm>)

    /**
     * Update existing farm
     */
    @Update
    suspend fun updateFarm(farm: Farm)

    /**
     * Delete farm
     */
    @Delete
    suspend fun deleteFarm(farm: Farm)

    /**
     * Get farm by ID
     */
    @Query("SELECT * FROM farms WHERE id = :farmId LIMIT 1")
    suspend fun getFarmById(farmId: String): Farm?

    /**
     * Get farm by ID as Flow for reactive updates
     */
    @Query("SELECT * FROM farms WHERE id = :farmId LIMIT 1")
    fun getFarmByIdFlow(farmId: String): Flow<Farm?>

    /**
     * Get current user's farm (assumes single farm per user)
     */
    @Query("SELECT * FROM farms WHERE ownerId = :ownerId AND isActive = 1 LIMIT 1")
    fun getCurrentUserFarm(ownerId: String = "current_user"): Flow<Farm?>

    /**
     * Get all farms for current user
     */
    @Query("SELECT * FROM farms WHERE ownerId = :ownerId AND isActive = 1 ORDER BY createdAt DESC")
    fun getUserFarms(ownerId: String = "current_user"): Flow<List<Farm>>

    /**
     * Get all active farms
     */
    @Query("SELECT * FROM farms WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveFarms(): Flow<List<Farm>>

    /**
     * Get farms by type
     */
    @Query("SELECT * FROM farms WHERE farmType = :farmType AND isActive = 1 ORDER BY createdAt DESC")
    fun getFarmsByType(farmType: FarmType): Flow<List<Farm>>

    /**
     * Get farms by certification level
     */
    @Query("SELECT * FROM farms WHERE certificationLevel = :certificationLevel AND isActive = 1 ORDER BY createdAt DESC")
    fun getFarmsByCertification(certificationLevel: CertificationLevel): Flow<List<Farm>>

    /**
     * Get verified farms
     */
    @Query("SELECT * FROM farms WHERE verificationStatus = 'VERIFIED' AND isActive = 1 ORDER BY createdAt DESC")
    fun getVerifiedFarms(): Flow<List<Farm>>

    /**
     * Search farms by name or location
     */
    @Query("""
        SELECT * FROM farms 
        WHERE (farmName LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%') 
        AND isActive = 1 
        ORDER BY farmName ASC
    """)
    fun searchFarms(query: String): Flow<List<Farm>>

    /**
     * Get farms within area range
     */
    @Query("""
        SELECT * FROM farms 
        WHERE totalArea BETWEEN :minArea AND :maxArea 
        AND isActive = 1 
        ORDER BY totalArea DESC
    """)
    fun getFarmsByAreaRange(minArea: Double, maxArea: Double): Flow<List<Farm>>

    /**
     * Get farms needing maintenance
     */
    @Query("""
        SELECT * FROM farms 
        WHERE json_extract(facilities, '$[*].condition') LIKE '%POOR%' 
        OR json_extract(facilities, '$[*].condition') LIKE '%NEEDS_REPAIR%'
        AND isActive = 1
    """)
    fun getFarmsNeedingMaintenance(): Flow<List<Farm>>

    /**
     * Get total farm count
     */
    @Query("SELECT COUNT(*) FROM farms WHERE isActive = 1")
    suspend fun getTotalFarmCount(): Int

    /**
     * Get verified farm count
     */
    @Query("SELECT COUNT(*) FROM farms WHERE verificationStatus = 'VERIFIED' AND isActive = 1")
    suspend fun getVerifiedFarmCount(): Int

    /**
     * Get average farm area
     */
    @Query("SELECT AVG(totalArea) FROM farms WHERE isActive = 1")
    suspend fun getAverageFarmArea(): Double

    /**
     * Get farms by establishment date range
     */
    @Query("""
        SELECT * FROM farms 
        WHERE establishedDate BETWEEN :startDate AND :endDate 
        AND isActive = 1 
        ORDER BY establishedDate DESC
    """)
    fun getFarmsByDateRange(startDate: Long, endDate: Long): Flow<List<Farm>>

    /**
     * Get recently updated farms
     */
    @Query("""
        SELECT * FROM farms 
        WHERE updatedAt >= :since 
        AND isActive = 1 
        ORDER BY updatedAt DESC 
        LIMIT :limit
    """)
    fun getRecentlyUpdatedFarms(
        since: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000), // 7 days ago
        limit: Int = 10
    ): Flow<List<Farm>>

    /**
     * Get farm count by owner
     */
    @Query("SELECT COUNT(*) FROM farms WHERE ownerId = :ownerId AND isActive = 1")
    suspend fun getFarmCountByOwner(ownerId: String): Int

    /**
     * Get farms with facilities
     */
    @Query("""
        SELECT * FROM farms 
        WHERE json_array_length(facilities) > 0 
        AND isActive = 1 
        ORDER BY json_array_length(facilities) DESC
    """)
    fun getFarmsWithFacilities(): Flow<List<Farm>>

    /**
     * Get farms by capacity range
     */
    @Query("""
        SELECT f.* FROM farms f
        WHERE (
            SELECT SUM(json_extract(facility.value, '$.capacity'))
            FROM json_each(f.facilities) facility
        ) BETWEEN :minCapacity AND :maxCapacity
        AND f.isActive = 1
        ORDER BY f.farmName ASC
    """)
    fun getFarmsByCapacityRange(minCapacity: Int, maxCapacity: Int): Flow<List<Farm>>

    /**
     * Update farm verification status
     */
    @Query("""
        UPDATE farms 
        SET verificationStatus = :status, updatedAt = :timestamp 
        WHERE id = :farmId
    """)
    suspend fun updateVerificationStatus(
        farmId: String, 
        status: String, 
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update farm certification
     */
    @Query("""
        UPDATE farms 
        SET certificationLevel = :level, certificationDate = :date, updatedAt = :timestamp 
        WHERE id = :farmId
    """)
    suspend fun updateCertification(
        farmId: String, 
        level: CertificationLevel, 
        date: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Deactivate farm
     */
    @Query("""
        UPDATE farms 
        SET isActive = 0, updatedAt = :timestamp 
        WHERE id = :farmId
    """)
    suspend fun deactivateFarm(farmId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Delete old inactive farms
     */
    @Query("""
        DELETE FROM farms 
        WHERE isActive = 0 AND updatedAt < :cutoffDate
    """)
    suspend fun deleteOldInactiveFarms(cutoffDate: Long)

    /**
     * Get farms by location pattern
     */
    @Query("""
        SELECT * FROM farms 
        WHERE location LIKE '%' || :locationPattern || '%' 
        AND isActive = 1 
        ORDER BY location ASC
    """)
    fun getFarmsByLocation(locationPattern: String): Flow<List<Farm>>

    /**
     * Count farms by type - simplified
     */
    @Query("SELECT COUNT(*) FROM farms WHERE farmType = :farmType AND isActive = 1")
    suspend fun getFarmCountByType(farmType: String): Int

    /**
     * Get total farm area
     */
    @Query("SELECT SUM(totalArea) FROM farms WHERE isActive = 1")
    suspend fun getTotalFarmArea(): Double

    /**
     * Get farms with upcoming certification renewal
     */
    @Query("""
        SELECT * FROM farms 
        WHERE certificationDate > 0 
        AND (certificationDate + (365 * 24 * 60 * 60 * 1000)) <= :renewalDate
        AND isActive = 1
        ORDER BY certificationDate ASC
    """)
    fun getFarmsWithUpcomingRenewal(
        renewalDate: Long = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000) // 30 days from now
    ): Flow<List<Farm>>
}