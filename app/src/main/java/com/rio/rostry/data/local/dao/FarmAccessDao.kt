package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for farm access management
 * Handles user permissions, roles, and access control
 */
@Dao
interface FarmAccessDao {

    // ==================== FARM ACCESS OPERATIONS ====================

    /**
     * Insert farm access record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmAccess(farmAccess: FarmAccess)

    /**
     * Insert multiple farm access records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmAccesses(farmAccesses: List<FarmAccess>)

    /**
     * Update farm access record
     */
    @Update
    suspend fun updateFarmAccess(farmAccess: FarmAccess)

    /**
     * Delete farm access record
     */
    @Delete
    suspend fun deleteFarmAccess(farmAccess: FarmAccess)

    /**
     * Get farm access by ID
     */
    @Query("SELECT * FROM farm_access WHERE id = :accessId LIMIT 1")
    suspend fun getFarmAccessById(accessId: String): FarmAccess?

    /**
     * Get farm access by user and farm
     */
    @Query("SELECT * FROM farm_access WHERE userId = :userId AND farmId = :farmId AND isActive = 1 LIMIT 1")
    suspend fun getFarmAccessByUserAndFarm(userId: String, farmId: String): FarmAccess?

    /**
     * Get farm access by user and farm as Flow
     */
    @Query("SELECT * FROM farm_access WHERE userId = :userId AND farmId = :farmId AND isActive = 1 LIMIT 1")
    fun getFarmAccessByUserAndFarmFlow(userId: String, farmId: String): Flow<FarmAccess?>

    /**
     * Get all farms accessible by user
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE userId = :userId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        ORDER BY lastAccessedAt DESC
    """)
    fun getUserFarmsFlow(userId: String): Flow<List<FarmAccess>>

    /**
     * Get farm team members
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        ORDER BY role, acceptedAt ASC
    """)
    fun getFarmTeamFlow(farmId: String): Flow<List<FarmAccess>>

    /**
     * Get farm access by email (for checking existing invitations)
     */
    @Query("""
        SELECT fa.* FROM farm_access fa
        INNER JOIN users u ON fa.userId = u.id
        WHERE fa.farmId = :farmId AND u.email = :email
        AND fa.isActive = 1
    """)
    suspend fun getFarmAccessByEmail(farmId: String, email: String): FarmAccess?

    /**
     * Check if user has specific permission
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM farm_access 
        WHERE userId = :userId 
        AND farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        AND permissions LIKE '%' || :permission || '%'
    """)
    suspend fun hasPermission(userId: String, farmId: String, permission: String): Boolean

    /**
     * Get users with specific permission
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        AND permissions LIKE '%' || :permission || '%'
        ORDER BY role
    """)
    fun getUsersWithPermission(farmId: String, permission: String): Flow<List<FarmAccess>>

    /**
     * Get users by role
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND role = :role 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        ORDER BY acceptedAt ASC
    """)
    fun getUsersByRole(farmId: String, role: FarmRole): Flow<List<FarmAccess>>

    /**
     * Get farm owners
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND role = 'OWNER' 
        AND isActive = 1 
        AND status = 'ACCEPTED'
    """)
    suspend fun getFarmOwners(farmId: String): List<FarmAccess>

    /**
     * Get pending access requests
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND status = 'PENDING'
        ORDER BY invitedAt DESC
    """)
    fun getPendingAccessRequests(farmId: String): Flow<List<FarmAccess>>

    /**
     * Get expiring access records
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        AND expiresAt IS NOT NULL 
        AND expiresAt <= :expirationThreshold
        ORDER BY expiresAt ASC
    """)
    fun getExpiringAccess(farmId: String, expirationThreshold: Long): Flow<List<FarmAccess>>

    /**
     * Get inactive users (haven't accessed recently)
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        AND lastAccessedAt < :inactivityThreshold
        ORDER BY lastAccessedAt ASC
    """)
    fun getInactiveUsers(farmId: String, inactivityThreshold: Long): Flow<List<FarmAccess>>

    /**
     * Update last accessed time
     */
    @Query("""
        UPDATE farm_access 
        SET lastAccessedAt = :timestamp, updatedAt = :timestamp
        WHERE userId = :userId AND farmId = :farmId
    """)
    suspend fun updateLastAccessed(userId: String, farmId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Revoke user access
     */
    @Query("""
        UPDATE farm_access 
        SET status = 'REVOKED', isActive = 0, updatedAt = :timestamp
        WHERE userId = :userId AND farmId = :farmId
    """)
    suspend fun revokeAccess(userId: String, farmId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Suspend user access
     */
    @Query("""
        UPDATE farm_access 
        SET status = 'SUSPENDED', isActive = 0, updatedAt = :timestamp
        WHERE userId = :userId AND farmId = :farmId
    """)
    suspend fun suspendAccess(userId: String, farmId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Restore user access
     */
    @Query("""
        UPDATE farm_access 
        SET status = 'ACCEPTED', isActive = 1, updatedAt = :timestamp
        WHERE userId = :userId AND farmId = :farmId
    """)
    suspend fun restoreAccess(userId: String, farmId: String, timestamp: Long = System.currentTimeMillis())

    // ==================== ANALYTICS AND REPORTING ====================

    /**
     * Get farm access statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as totalUsers,
            COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) as activeUsers,
            COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pendingUsers,
            COUNT(CASE WHEN role = 'OWNER' THEN 1 END) as owners,
            COUNT(CASE WHEN role = 'MANAGER' THEN 1 END) as managers,
            COUNT(CASE WHEN role = 'WORKER' THEN 1 END) as workers,
            COUNT(CASE WHEN lastAccessedAt > :recentThreshold THEN 1 END) as recentlyActive
        FROM farm_access 
        WHERE farmId = :farmId AND isActive = 1
    """)
    suspend fun getFarmAccessStatistics(
        farmId: String, 
        recentThreshold: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    ): Map<String, Int>

    /**
     * Get role distribution
     */
    @Query("""
        SELECT role, COUNT(*) as count 
        FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        GROUP BY role
    """)
    suspend fun getRoleDistribution(farmId: String): Map<FarmRole, Int>

    /**
     * Get access trends over time
     */
    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(acceptedAt/1000, 'unixepoch')) as month,
            COUNT(*) as newUsers
        FROM farm_access 
        WHERE farmId = :farmId 
        AND status = 'ACCEPTED'
        AND acceptedAt >= :startDate
        GROUP BY strftime('%Y-%m', datetime(acceptedAt/1000, 'unixepoch'))
        ORDER BY month DESC
    """)
    suspend fun getAccessTrends(
        farmId: String, 
        startDate: Long = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
    ): List<Map<String, Any>>

    /**
     * Get user activity summary
     */
    @Query("""
        SELECT 
            userId,
            role,
            lastAccessedAt,
            (julianday('now') - julianday(datetime(lastAccessedAt/1000, 'unixepoch'))) as daysSinceLastAccess
        FROM farm_access 
        WHERE farmId = :farmId 
        AND isActive = 1 
        AND status = 'ACCEPTED'
        ORDER BY lastAccessedAt DESC
    """)
    suspend fun getUserActivitySummary(farmId: String): List<Map<String, Any>>

    // ==================== PERMISSION REQUESTS ====================

    /**
     * Insert permission request
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissionRequest(request: PermissionRequest)

    /**
     * Update permission request
     */
    @Update
    suspend fun updatePermissionRequest(request: PermissionRequest)

    /**
     * Get pending permission requests
     */
    @Query("""
        SELECT * FROM permission_requests 
        WHERE farmId = :farmId 
        AND status = 'PENDING'
        ORDER BY urgencyLevel, createdAt DESC
    """)
    fun getPendingPermissionRequests(farmId: String): Flow<List<PermissionRequest>>

    /**
     * Get user's permission requests
     */
    @Query("""
        SELECT * FROM permission_requests 
        WHERE farmId = :farmId 
        AND requesterId = :userId
        ORDER BY createdAt DESC
    """)
    fun getUserPermissionRequests(farmId: String, userId: String): Flow<List<PermissionRequest>>

    // ==================== AUDIT LOG ====================

    /**
     * Insert audit log entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(auditLog: AccessAuditLog)

    /**
     * Get audit log for farm
     */
    @Query("""
        SELECT * FROM access_audit_log 
        WHERE farmId = :farmId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getAuditLog(farmId: String, limit: Int = 100): Flow<List<AccessAuditLog>>

    /**
     * Get audit log for specific user
     */
    @Query("""
        SELECT * FROM access_audit_log 
        WHERE farmId = :farmId 
        AND targetUserId = :userId
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getUserAuditLog(farmId: String, userId: String, limit: Int = 50): Flow<List<AccessAuditLog>>

    /**
     * Get recent security events
     */
    @Query("""
        SELECT * FROM access_audit_log 
        WHERE farmId = :farmId 
        AND action IN ('ACCESS_REVOKED', 'ACCESS_SUSPENDED', 'ROLE_CHANGED', 'PERMISSIONS_MODIFIED')
        AND timestamp >= :since
        ORDER BY timestamp DESC
    """)
    fun getRecentSecurityEvents(
        farmId: String, 
        since: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
    ): Flow<List<AccessAuditLog>>

    // ==================== CLEANUP OPERATIONS ====================

    /**
     * Delete expired access records
     */
    @Query("""
        DELETE FROM farm_access 
        WHERE expiresAt IS NOT NULL 
        AND expiresAt < :currentTime 
        AND status != 'ACCEPTED'
    """)
    suspend fun deleteExpiredAccess(currentTime: Long = System.currentTimeMillis())

    /**
     * Delete old audit logs
     */
    @Query("""
        DELETE FROM access_audit_log 
        WHERE timestamp < :cutoffTime
    """)
    suspend fun deleteOldAuditLogs(cutoffTime: Long)

    /**
     * Archive inactive access records
     */
    @Query("""
        UPDATE farm_access 
        SET isActive = 0, updatedAt = :timestamp
        WHERE lastAccessedAt < :inactivityThreshold 
        AND status = 'ACCEPTED'
        AND role != 'OWNER'
    """)
    suspend fun archiveInactiveAccess(
        inactivityThreshold: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== SEARCH AND FILTERING ====================

    /**
     * Search farm access records
     */
    @Query("""
        SELECT fa.* FROM farm_access fa
        INNER JOIN users u ON fa.userId = u.id
        WHERE fa.farmId = :farmId 
        AND fa.isActive = 1
        AND (u.name LIKE '%' || :query || '%' 
             OR u.email LIKE '%' || :query || '%'
             OR fa.role LIKE '%' || :query || '%')
        ORDER BY fa.lastAccessedAt DESC
    """)
    fun searchFarmAccess(farmId: String, query: String): Flow<List<FarmAccess>>

    /**
     * Filter access by multiple criteria
     */
    @Query("""
        SELECT * FROM farm_access 
        WHERE farmId = :farmId 
        AND (:role IS NULL OR role = :role)
        AND (:status IS NULL OR status = :status)
        AND (:isActive IS NULL OR isActive = :isActive)
        AND (:fromDate IS NULL OR acceptedAt >= :fromDate)
        AND (:toDate IS NULL OR acceptedAt <= :toDate)
        ORDER BY acceptedAt DESC
    """)
    fun filterFarmAccess(
        farmId: String,
        role: FarmRole? = null,
        status: AccessStatus? = null,
        isActive: Boolean? = null,
        fromDate: Long? = null,
        toDate: Long? = null
    ): Flow<List<FarmAccess>>

    /**
     * Get access summary for dashboard
     */
    @Query("""
        SELECT 
            role,
            status,
            COUNT(*) as count,
            MAX(lastAccessedAt) as lastActivity
        FROM farm_access 
        WHERE farmId = :farmId 
        GROUP BY role, status
        ORDER BY role, status
    """)
    suspend fun getAccessSummary(farmId: String): List<Map<String, Any>>
}