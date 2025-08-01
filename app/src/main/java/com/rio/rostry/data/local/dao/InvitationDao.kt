package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for farm invitation management
 * Handles invitation lifecycle, templates, and analytics
 */
@Dao
interface InvitationDao {

    // ==================== INVITATION OPERATIONS ====================

    /**
     * Insert farm invitation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: FarmInvitation)

    /**
     * Insert multiple invitations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitations(invitations: List<FarmInvitation>)

    /**
     * Update invitation
     */
    @Update
    suspend fun updateInvitation(invitation: FarmInvitation)

    /**
     * Delete invitation
     */
    @Delete
    suspend fun deleteInvitation(invitation: FarmInvitation)

    /**
     * Get invitation by ID
     */
    @Query("SELECT * FROM farm_invitations WHERE id = :invitationId LIMIT 1")
    suspend fun getInvitationById(invitationId: String): FarmInvitation?

    /**
     * Get invitation by code
     */
    @Query("SELECT * FROM farm_invitations WHERE invitationCode = :code LIMIT 1")
    suspend fun getInvitationByCode(code: String): FarmInvitation?

    /**
     * Get invitations for farm
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE farmId = :farmId 
        ORDER BY sentAt DESC
    """)
    fun getFarmInvitations(farmId: String): Flow<List<FarmInvitation>>

    /**
     * Get invitations sent by user
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE inviterUserId = :userId 
        ORDER BY sentAt DESC
    """)
    fun getInvitationsSentByUser(userId: String): Flow<List<FarmInvitation>>

    /**
     * Get invitations for email
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE inviteeEmail = :email 
        ORDER BY sentAt DESC
    """)
    fun getInvitationsForEmail(email: String): Flow<List<FarmInvitation>>

    /**
     * Get pending invitations
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE status = 'SENT' 
        AND expiresAt > :currentTime
        ORDER BY sentAt DESC
    """)
    fun getPendingInvitations(currentTime: Long = System.currentTimeMillis()): Flow<List<FarmInvitation>>

    /**
     * Get pending invitations for farm
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE farmId = :farmId 
        AND status = 'SENT' 
        AND expiresAt > :currentTime
        ORDER BY sentAt DESC
    """)
    fun getFarmPendingInvitations(farmId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<FarmInvitation>>

    /**
     * Get expired invitations
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE status = 'SENT' 
        AND expiresAt <= :currentTime
        ORDER BY expiresAt DESC
    """)
    fun getExpiredInvitations(currentTime: Long = System.currentTimeMillis()): Flow<List<FarmInvitation>>

    /**
     * Get invitations needing reminders
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE status = 'SENT' 
        AND expiresAt > :currentTime
        AND remindersSent < maxReminders
        AND (lastReminderAt IS NULL OR lastReminderAt < :reminderThreshold)
        ORDER BY sentAt ASC
    """)
    fun getInvitationsNeedingReminders(
        currentTime: Long = System.currentTimeMillis(),
        reminderThreshold: Long = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours ago
    ): Flow<List<FarmInvitation>>

    /**
     * Update invitation status
     */
    @Query("""
        UPDATE farm_invitations 
        SET status = :status, respondedAt = :timestamp, updatedAt = :timestamp
        WHERE id = :invitationId
    """)
    suspend fun updateInvitationStatus(
        invitationId: String, 
        status: InvitationStatus,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Update reminder count
     */
    @Query("""
        UPDATE farm_invitations 
        SET remindersSent = remindersSent + 1, 
            lastReminderAt = :timestamp,
            updatedAt = :timestamp
        WHERE id = :invitationId
    """)
    suspend fun incrementReminderCount(invitationId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Mark invitations as expired
     */
    @Query("""
        UPDATE farm_invitations 
        SET status = 'EXPIRED', updatedAt = :timestamp
        WHERE status = 'SENT' 
        AND expiresAt <= :currentTime
    """)
    suspend fun markExpiredInvitations(
        currentTime: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== INVITATION ANALYTICS ====================

    /**
     * Get invitation statistics for farm
     */
    @Query("""
        SELECT 
            COUNT(*) as totalInvitations,
            COUNT(CASE WHEN status = 'SENT' THEN 1 END) as pendingInvitations,
            COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) as acceptedInvitations,
            COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejectedInvitations,
            COUNT(CASE WHEN status = 'EXPIRED' THEN 1 END) as expiredInvitations,
            AVG(CASE WHEN respondedAt IS NOT NULL THEN respondedAt - sentAt END) as avgResponseTime
        FROM farm_invitations 
        WHERE farmId = :farmId
    """)
    suspend fun getInvitationStatistics(farmId: String): Map<String, Any>

    /**
     * Get invitation acceptance rate
     */
    @Query("""
        SELECT 
            COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) * 100.0 / 
            COUNT(CASE WHEN status IN ('ACCEPTED', 'REJECTED', 'EXPIRED') THEN 1 END) as acceptanceRate
        FROM farm_invitations 
        WHERE farmId = :farmId 
        AND status IN ('ACCEPTED', 'REJECTED', 'EXPIRED')
    """)
    suspend fun getAcceptanceRate(farmId: String): Double

    /**
     * Get invitation trends
     */
    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(sentAt/1000, 'unixepoch')) as month,
            COUNT(*) as invitationsSent,
            COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) as acceptedCount
        FROM farm_invitations 
        WHERE farmId = :farmId 
        AND sentAt >= :startDate
        GROUP BY strftime('%Y-%m', datetime(sentAt/1000, 'unixepoch'))
        ORDER BY month DESC
    """)
    suspend fun getInvitationTrends(
        farmId: String, 
        startDate: Long = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
    ): List<Map<String, Any>>

    /**
     * Get role distribution of invitations
     */
    @Query("""
        SELECT proposedRole, COUNT(*) as count 
        FROM farm_invitations 
        WHERE farmId = :farmId 
        GROUP BY proposedRole
    """)
    suspend fun getInvitationRoleDistribution(farmId: String): Map<FarmRole, Int>

    // ==================== INVITATION TEMPLATES ====================

    /**
     * Insert invitation template
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitationTemplate(template: InvitationTemplate)

    /**
     * Update invitation template
     */
    @Update
    suspend fun updateInvitationTemplate(template: InvitationTemplate)

    /**
     * Delete invitation template
     */
    @Delete
    suspend fun deleteInvitationTemplate(template: InvitationTemplate)

    /**
     * Get invitation templates for farm
     */
    @Query("""
        SELECT * FROM invitation_templates 
        WHERE farmId = :farmId 
        AND isActive = 1
        ORDER BY name ASC
    """)
    fun getFarmInvitationTemplates(farmId: String): Flow<List<InvitationTemplate>>

    /**
     * Get invitation template by ID
     */
    @Query("SELECT * FROM invitation_templates WHERE id = :templateId LIMIT 1")
    suspend fun getInvitationTemplateById(templateId: String): InvitationTemplate?

    /**
     * Update template usage count
     */
    @Query("""
        UPDATE invitation_templates 
        SET usageCount = usageCount + 1, updatedAt = :timestamp
        WHERE id = :templateId
    """)
    suspend fun incrementTemplateUsage(templateId: String, timestamp: Long = System.currentTimeMillis())

    // ==================== BULK INVITATIONS ====================

    /**
     * Insert bulk invitation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBulkInvitation(bulkInvitation: BulkInvitation)

    /**
     * Update bulk invitation
     */
    @Update
    suspend fun updateBulkInvitation(bulkInvitation: BulkInvitation)

    /**
     * Get bulk invitations for farm
     */
    @Query("""
        SELECT * FROM bulk_invitations 
        WHERE farmId = :farmId 
        ORDER BY createdAt DESC
    """)
    fun getFarmBulkInvitations(farmId: String): Flow<List<BulkInvitation>>

    /**
     * Get bulk invitation by ID
     */
    @Query("SELECT * FROM bulk_invitations WHERE id = :bulkId LIMIT 1")
    suspend fun getBulkInvitationById(bulkId: String): BulkInvitation?

    /**
     * Update bulk invitation progress
     */
    @Query("""
        UPDATE bulk_invitations 
        SET sentCount = :sentCount,
            acceptedCount = :acceptedCount,
            rejectedCount = :rejectedCount,
            expiredCount = :expiredCount,
            status = :status
        WHERE id = :bulkId
    """)
    suspend fun updateBulkInvitationProgress(
        bulkId: String,
        sentCount: Int,
        acceptedCount: Int,
        rejectedCount: Int,
        expiredCount: Int,
        status: BulkInvitationStatus
    )

    // ==================== INVITATION ANALYTICS TRACKING ====================

    /**
     * Insert invitation analytics event
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitationAnalytics(analytics: InvitationAnalytics)

    /**
     * Get invitation analytics
     */
    @Query("""
        SELECT * FROM invitation_analytics 
        WHERE farmId = :farmId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun getInvitationAnalytics(farmId: String, limit: Int = 1000): Flow<List<InvitationAnalytics>>

    /**
     * Get analytics for specific invitation
     */
    @Query("""
        SELECT * FROM invitation_analytics 
        WHERE invitationId = :invitationId 
        ORDER BY timestamp ASC
    """)
    fun getInvitationEventHistory(invitationId: String): Flow<List<InvitationAnalytics>>

    /**
     * Get event counts by type
     */
    @Query("""
        SELECT event, COUNT(*) as count 
        FROM invitation_analytics 
        WHERE farmId = :farmId 
        AND timestamp >= :startDate
        GROUP BY event
    """)
    suspend fun getEventCounts(
        farmId: String, 
        startDate: Long = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
    ): Map<InvitationEvent, Int>

    // ==================== SEARCH AND FILTERING ====================

    /**
     * Search invitations
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE farmId = :farmId 
        AND (inviteeEmail LIKE '%' || :query || '%' 
             OR inviterName LIKE '%' || :query || '%'
             OR invitationCode LIKE '%' || :query || '%')
        ORDER BY sentAt DESC
    """)
    fun searchInvitations(farmId: String, query: String): Flow<List<FarmInvitation>>

    /**
     * Filter invitations by criteria
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE farmId = :farmId 
        AND (:status IS NULL OR status = :status)
        AND (:role IS NULL OR proposedRole = :role)
        AND (:fromDate IS NULL OR sentAt >= :fromDate)
        AND (:toDate IS NULL OR sentAt <= :toDate)
        ORDER BY sentAt DESC
    """)
    fun filterInvitations(
        farmId: String,
        status: InvitationStatus? = null,
        role: FarmRole? = null,
        fromDate: Long? = null,
        toDate: Long? = null
    ): Flow<List<FarmInvitation>>

    // ==================== CLEANUP OPERATIONS ====================

    /**
     * Delete old expired invitations
     */
    @Query("""
        DELETE FROM farm_invitations 
        WHERE status = 'EXPIRED' 
        AND expiresAt < :cutoffTime
    """)
    suspend fun deleteOldExpiredInvitations(cutoffTime: Long)

    /**
     * Delete old analytics data
     */
    @Query("""
        DELETE FROM invitation_analytics 
        WHERE timestamp < :cutoffTime
    """)
    suspend fun deleteOldAnalytics(cutoffTime: Long)

    /**
     * Archive completed bulk invitations
     */
    @Query("""
        UPDATE bulk_invitations 
        SET status = 'COMPLETED'
        WHERE status = 'IN_PROGRESS' 
        AND sentCount >= totalInvitations
    """)
    suspend fun archiveCompletedBulkInvitations()

    // ==================== DASHBOARD QUERIES ====================

    /**
     * Get invitation dashboard summary
     */
    @Query("""
        SELECT 
            COUNT(*) as totalInvitations,
            COUNT(CASE WHEN status = 'SENT' AND expiresAt > :currentTime THEN 1 END) as activeInvitations,
            COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) as acceptedInvitations,
            COUNT(CASE WHEN sentAt >= :recentThreshold THEN 1 END) as recentInvitations
        FROM farm_invitations 
        WHERE farmId = :farmId
    """)
    suspend fun getInvitationDashboardSummary(
        farmId: String,
        currentTime: Long = System.currentTimeMillis(),
        recentThreshold: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
    ): Map<String, Int>

    /**
     * Get recent invitation activity
     */
    @Query("""
        SELECT * FROM farm_invitations 
        WHERE farmId = :farmId 
        AND sentAt >= :since
        ORDER BY sentAt DESC 
        LIMIT :limit
    """)
    fun getRecentInvitationActivity(
        farmId: String,
        since: Long = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L),
        limit: Int = 10
    ): Flow<List<FarmInvitation>>
}