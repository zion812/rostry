package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.local.dao.FarmAccessDao
import com.rio.rostry.data.local.dao.InvitationDao
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for comprehensive farm access management
 * Handles user permissions, roles, invitations, and security
 */
@Singleton
class FarmAccessRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmAccessDao: FarmAccessDao,
    private val invitationDao: InvitationDao,
    private val userRepository: UserRepository,
    private val farmRepository: FarmRepository,
    private val notificationService: NotificationService
) {

    // ==================== INVITATION MANAGEMENT ====================

    /**
     * Invite user to farm with comprehensive validation
     */
    suspend fun inviteUserToFarm(
        farmId: String,
        inviterUserId: String,
        inviteeEmail: String,
        role: FarmRole,
        customPermissions: List<FarmPermission> = emptyList(),
        message: String = "",
        expirationDays: Int = 7,
        requiresApproval: Boolean = false
    ): Result<String> {
        return try {
            // Validate inviter permissions
            val inviterAccess = farmAccessDao.getFarmAccessByUserAndFarm(inviterUserId, farmId)
            if (!canInviteUsers(inviterAccess, role)) {
                return Result.failure(Exception("You don't have permission to invite users with this role"))
            }

            // Check for existing access or pending invitations
            val existingInvitation = invitationDao.getFarmInvitations(farmId)
                .first()
                .find { it.inviteeEmail == inviteeEmail && it.status == InvitationStatus.SENT }
            
            if (existingInvitation != null) {
                return Result.failure(Exception("User already has a pending invitation"))
            }

            // Get farm and inviter details
            val farm = farmRepository.getFarmById(farmId)
                ?: return Result.failure(Exception("Farm not found"))
            
            val inviter = userRepository.getUserById(inviterUserId)
                ?: return Result.failure(Exception("Inviter not found"))

            // Check if invitee exists in system
            val inviteeUser = userRepository.getUserByEmail(inviteeEmail)

            // Create invitation
            val invitation = FarmInvitation(
                farmId = farmId,
                farmName = farm.farmName,
                inviterUserId = inviterUserId,
                inviterName = inviter.name,
                inviterEmail = inviter.email,
                inviteeEmail = inviteeEmail,
                inviteeUserId = inviteeUser?.id,
                proposedRole = role,
                customPermissions = customPermissions,
                invitationMessage = message,
                expiresAt = System.currentTimeMillis() + (expirationDays * 24 * 60 * 60 * 1000L),
                requiresApproval = requiresApproval
            )

            // Save invitation to Firestore and local database
            firestore.collection("farm_invitations")
                .document(invitation.id)
                .set(invitation)
                .await()

            invitationDao.insertInvitation(invitation)

            // Log audit event
            logAuditEvent(
                farmId = farmId,
                targetUserId = inviteeUser?.id ?: "",
                actionPerformedBy = inviterUserId,
                action = AccessAction.INVITED,
                newRole = role
            )

            // Send notification
            sendInvitationNotification(invitation)

            // Track analytics
            trackInvitationEvent(invitation.id, InvitationEvent.CREATED)

            Result.success(invitation.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept farm invitation with validation
     */
    suspend fun acceptInvitation(
        invitationId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val invitation = invitationDao.getInvitationById(invitationId)
                ?: return Result.failure(Exception("Invitation not found"))

            // Validate invitation
            if (!invitation.isValid()) {
                return Result.failure(Exception("Invitation is no longer valid"))
            }

            // Verify user email matches
            val user = userRepository.getUserById(userId)
            if (user?.email != invitation.inviteeEmail) {
                return Result.failure(Exception("Email mismatch"))
            }

            // Check if user already has access
            val existingAccess = farmAccessDao.getFarmAccessByUserAndFarm(userId, invitation.farmId)
            if (existingAccess?.isValidAccess() == true) {
                return Result.failure(Exception("User already has access to this farm"))
            }

            // Create farm access record
            val farmAccess = FarmAccess(
                farmId = invitation.farmId,
                userId = userId,
                role = invitation.proposedRole,
                permissions = invitation.getEffectivePermissions(),
                invitedBy = invitation.inviterUserId,
                acceptedAt = System.currentTimeMillis(),
                status = AccessStatus.ACCEPTED
            )

            // Save to Firestore and local database
            firestore.collection("farm_access")
                .document(farmAccess.id)
                .set(farmAccess)
                .await()

            farmAccessDao.insertFarmAccess(farmAccess)

            // Update invitation status
            val updatedInvitation = invitation.copy(
                status = InvitationStatus.ACCEPTED,
                respondedAt = System.currentTimeMillis()
            )

            firestore.collection("farm_invitations")
                .document(invitationId)
                .set(updatedInvitation)
                .await()

            invitationDao.updateInvitation(updatedInvitation)

            // Log audit events
            logAuditEvent(
                farmId = invitation.farmId,
                targetUserId = userId,
                actionPerformedBy = userId,
                action = AccessAction.ACCEPTED,
                newRole = invitation.proposedRole,
                newPermissions = invitation.getEffectivePermissions()
            )

            // Track analytics
            trackInvitationEvent(invitationId, InvitationEvent.ACCEPTED)

            // Send welcome notification
            sendWelcomeNotification(farmAccess, invitation.farmName)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reject farm invitation
     */
    suspend fun rejectInvitation(
        invitationId: String,
        userId: String,
        reason: String = ""
    ): Result<Unit> {
        return try {
            val invitation = invitationDao.getInvitationById(invitationId)
                ?: return Result.failure(Exception("Invitation not found"))

            // Verify user can reject this invitation
            val user = userRepository.getUserById(userId)
            if (user?.email != invitation.inviteeEmail) {
                return Result.failure(Exception("You cannot reject this invitation"))
            }

            // Update invitation status
            val updatedInvitation = invitation.copy(
                status = InvitationStatus.REJECTED,
                respondedAt = System.currentTimeMillis()
            )

            firestore.collection("farm_invitations")
                .document(invitationId)
                .set(updatedInvitation)
                .await()

            invitationDao.updateInvitation(updatedInvitation)

            // Log audit event
            logAuditEvent(
                farmId = invitation.farmId,
                targetUserId = userId,
                actionPerformedBy = userId,
                action = AccessAction.REJECTED
            )

            // Track analytics
            trackInvitationEvent(invitationId, InvitationEvent.REJECTED)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ACCESS MANAGEMENT ====================

    /**
     * Get user's accessible farms with access details
     */
    fun getUserFarms(userId: String): Flow<List<FarmWithAccess>> {
        return farmAccessDao.getUserFarmsFlow(userId).map { accessList ->
            accessList.mapNotNull { access ->
                val farm = farmRepository.getFarmById(access.farmId)
                farm?.let { FarmWithAccess(it, access) }
            }
        }
    }

    /**
     * Check if user has specific permission for farm
     */
    suspend fun hasPermission(
        userId: String,
        farmId: String,
        permission: FarmPermission
    ): Boolean {
        return try {
            val access = farmAccessDao.getFarmAccessByUserAndFarm(userId, farmId)
            access?.hasPermission(permission) == true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check multiple permissions at once
     */
    suspend fun hasPermissions(
        userId: String,
        farmId: String,
        permissions: List<FarmPermission>
    ): Map<FarmPermission, Boolean> {
        return try {
            val access = farmAccessDao.getFarmAccessByUserAndFarm(userId, farmId)
            permissions.associateWith { permission ->
                access?.hasPermission(permission) == true
            }
        } catch (e: Exception) {
            permissions.associateWith { false }
        }
    }

    /**
     * Get farm team members with user details
     */
    fun getFarmTeam(farmId: String): Flow<List<FarmTeamMember>> {
        return farmAccessDao.getFarmTeamFlow(farmId).map { accessList ->
            accessList.mapNotNull { access ->
                val user = userRepository.getUserById(access.userId)
                user?.let { FarmTeamMember(it, access) }
            }
        }
    }

    /**
     * Update user role and permissions with validation
     */
    suspend fun updateUserAccess(
        farmId: String,
        targetUserId: String,
        newRole: FarmRole,
        customPermissions: List<FarmPermission> = emptyList(),
        updatedBy: String,
        reason: String = ""
    ): Result<Unit> {
        return try {
            // Check if updater has permission
            val updaterAccess = farmAccessDao.getFarmAccessByUserAndFarm(updatedBy, farmId)
            if (!canManageAccess(updaterAccess, newRole)) {
                return Result.failure(Exception("You don't have permission to assign this role"))
            }

            val currentAccess = farmAccessDao.getFarmAccessByUserAndFarm(targetUserId, farmId)
                ?: return Result.failure(Exception("User access not found"))

            // Prevent role escalation beyond updater's level
            if (!updaterAccess!!.canManageRole(newRole)) {
                return Result.failure(Exception("Cannot assign a role higher than your own"))
            }

            // Store previous state for audit
            val previousRole = currentAccess.role
            val previousPermissions = currentAccess.permissions

            val updatedAccess = currentAccess.copy(
                role = newRole,
                permissions = if (customPermissions.isNotEmpty()) customPermissions else newRole.defaultPermissions,
                updatedAt = System.currentTimeMillis()
            )

            // Update Firestore and local database
            firestore.collection("farm_access")
                .document(currentAccess.id)
                .set(updatedAccess)
                .await()

            farmAccessDao.updateFarmAccess(updatedAccess)

            // Log audit event
            logAuditEvent(
                farmId = farmId,
                targetUserId = targetUserId,
                actionPerformedBy = updatedBy,
                action = AccessAction.ROLE_CHANGED,
                previousRole = previousRole,
                newRole = newRole,
                previousPermissions = previousPermissions,
                newPermissions = updatedAccess.permissions,
                reason = reason
            )

            // Send notification to affected user
            sendRoleChangeNotification(updatedAccess, previousRole)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove user from farm with validation
     */
    suspend fun removeUserFromFarm(
        farmId: String,
        targetUserId: String,
        removedBy: String,
        reason: String = ""
    ): Result<Unit> {
        return try {
            // Check permissions
            val removerAccess = farmAccessDao.getFarmAccessByUserAndFarm(removedBy, farmId)
            if (!hasPermission(removedBy, farmId, FarmPermission.REMOVE_USERS)) {
                return Result.failure(Exception("You don't have permission to remove users"))
            }

            val targetAccess = farmAccessDao.getFarmAccessByUserAndFarm(targetUserId, farmId)
                ?: return Result.failure(Exception("User access not found"))

            // Cannot remove farm owner
            if (targetAccess.role == FarmRole.OWNER) {
                return Result.failure(Exception("Cannot remove farm owner"))
            }

            // Cannot remove users with equal or higher role (unless you're owner)
            if (removerAccess!!.role != FarmRole.OWNER && 
                !removerAccess.canManageRole(targetAccess.role)) {
                return Result.failure(Exception("Cannot remove user with equal or higher role"))
            }

            // Revoke access
            val revokedAccess = targetAccess.copy(
                status = AccessStatus.REVOKED,
                isActive = false,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("farm_access")
                .document(targetAccess.id)
                .set(revokedAccess)
                .await()

            farmAccessDao.updateFarmAccess(revokedAccess)

            // Log audit event
            logAuditEvent(
                farmId = farmId,
                targetUserId = targetUserId,
                actionPerformedBy = removedBy,
                action = AccessAction.ACCESS_REVOKED,
                previousRole = targetAccess.role,
                reason = reason
            )

            // Send notification
            sendAccessRevokedNotification(revokedAccess)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Suspend user access temporarily
     */
    suspend fun suspendUserAccess(
        farmId: String,
        targetUserId: String,
        suspendedBy: String,
        reason: String = "",
        duration: Long? = null
    ): Result<Unit> {
        return try {
            if (!hasPermission(suspendedBy, farmId, FarmPermission.MANAGE_ACCESS)) {
                return Result.failure(Exception("You don't have permission to suspend users"))
            }

            val targetAccess = farmAccessDao.getFarmAccessByUserAndFarm(targetUserId, farmId)
                ?: return Result.failure(Exception("User access not found"))

            if (targetAccess.role == FarmRole.OWNER) {
                return Result.failure(Exception("Cannot suspend farm owner"))
            }

            val suspendedAccess = targetAccess.copy(
                status = AccessStatus.SUSPENDED,
                isActive = false,
                expiresAt = duration?.let { System.currentTimeMillis() + it },
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("farm_access")
                .document(targetAccess.id)
                .set(suspendedAccess)
                .await()

            farmAccessDao.updateFarmAccess(suspendedAccess)

            logAuditEvent(
                farmId = farmId,
                targetUserId = targetUserId,
                actionPerformedBy = suspendedBy,
                action = AccessAction.ACCESS_SUSPENDED,
                reason = reason
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore suspended user access
     */
    suspend fun restoreUserAccess(
        farmId: String,
        targetUserId: String,
        restoredBy: String,
        reason: String = ""
    ): Result<Unit> {
        return try {
            if (!hasPermission(restoredBy, farmId, FarmPermission.MANAGE_ACCESS)) {
                return Result.failure(Exception("You don't have permission to restore access"))
            }

            val targetAccess = farmAccessDao.getFarmAccessByUserAndFarm(targetUserId, farmId)
                ?: return Result.failure(Exception("User access not found"))

            val restoredAccess = targetAccess.copy(
                status = AccessStatus.ACCEPTED,
                isActive = true,
                expiresAt = null,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("farm_access")
                .document(targetAccess.id)
                .set(restoredAccess)
                .await()

            farmAccessDao.updateFarmAccess(restoredAccess)

            logAuditEvent(
                farmId = farmId,
                targetUserId = targetUserId,
                actionPerformedBy = restoredBy,
                action = AccessAction.ACCESS_RESTORED,
                reason = reason
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ANALYTICS AND MONITORING ====================

    /**
     * Get farm access analytics
     */
    suspend fun getFarmAccessAnalytics(farmId: String): FarmAccessAnalytics {
        val statistics = farmAccessDao.getFarmAccessStatistics(farmId)
        val roleDistribution = farmAccessDao.getRoleDistribution(farmId)
        val accessTrends = farmAccessDao.getAccessTrends(farmId)
        val invitationStats = invitationDao.getInvitationStatistics(farmId)

        return FarmAccessAnalytics(
            farmId = farmId,
            totalUsers = statistics["totalUsers"] ?: 0,
            activeUsers = statistics["activeUsers"] ?: 0,
            pendingUsers = statistics["pendingUsers"] ?: 0,
            roleDistribution = roleDistribution,
            accessTrends = accessTrends,
            invitationStatistics = invitationStats,
            lastCalculated = System.currentTimeMillis()
        )
    }

    /**
     * Get security alerts for farm
     */
    fun getSecurityAlerts(farmId: String): Flow<List<SecurityAlert>> {
        return combine(
            farmAccessDao.getExpiringAccess(farmId, System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)),
            farmAccessDao.getInactiveUsers(farmId, System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)),
            invitationDao.getExpiredInvitations()
        ) { expiring, inactive, expired ->
            val alerts = mutableListOf<SecurityAlert>()

            expiring.forEach { access ->
                alerts.add(
                    SecurityAlert(
                        type = SecurityAlertType.ACCESS_EXPIRING,
                        message = "Access for ${access.userId} expires soon",
                        severity = AlertSeverity.MEDIUM,
                        farmId = farmId,
                        userId = access.userId
                    )
                )
            }

            inactive.forEach { access ->
                alerts.add(
                    SecurityAlert(
                        type = SecurityAlertType.INACTIVE_USER,
                        message = "User ${access.userId} has been inactive for ${access.getDaysSinceLastAccess()} days",
                        severity = AlertSeverity.LOW,
                        farmId = farmId,
                        userId = access.userId
                    )
                )
            }

            alerts
        }
    }

    // ==================== HELPER METHODS ====================

    private fun canInviteUsers(inviterAccess: FarmAccess?, targetRole: FarmRole): Boolean {
        return inviterAccess?.let { access ->
            access.isValidAccess() &&
            (access.hasPermission(FarmPermission.INVITE_USERS) || 
             (access.hasPermission(FarmPermission.INVITE_WORKERS) && targetRole in listOf(FarmRole.WORKER, FarmRole.VIEWER))) &&
            access.canManageRole(targetRole)
        } ?: false
    }

    private fun canManageAccess(managerAccess: FarmAccess?, targetRole: FarmRole): Boolean {
        return managerAccess?.let { access ->
            access.isValidAccess() &&
            access.hasPermission(FarmPermission.MANAGE_ACCESS) &&
            access.canManageRole(targetRole)
        } ?: false
    }

    private suspend fun logAuditEvent(
        farmId: String,
        targetUserId: String,
        actionPerformedBy: String,
        action: AccessAction,
        previousRole: FarmRole? = null,
        newRole: FarmRole? = null,
        previousPermissions: List<FarmPermission> = emptyList(),
        newPermissions: List<FarmPermission> = emptyList(),
        reason: String = ""
    ) {
        val auditLog = AccessAuditLog(
            farmId = farmId,
            targetUserId = targetUserId,
            actionPerformedBy = actionPerformedBy,
            action = action,
            previousRole = previousRole,
            newRole = newRole,
            previousPermissions = previousPermissions,
            newPermissions = newPermissions,
            reason = reason
        )

        farmAccessDao.insertAuditLog(auditLog)
    }

    private suspend fun sendInvitationNotification(invitation: FarmInvitation) {
        // Implementation depends on your notification system
        notificationService.sendInvitationEmail(invitation)
    }

    private suspend fun sendWelcomeNotification(access: FarmAccess, farmName: String) {
        notificationService.sendWelcomeNotification(access, farmName)
    }

    private suspend fun sendRoleChangeNotification(access: FarmAccess, previousRole: FarmRole) {
        notificationService.sendRoleChangeNotification(access, previousRole)
    }

    private suspend fun sendAccessRevokedNotification(access: FarmAccess) {
        notificationService.sendAccessRevokedNotification(access)
    }

    private suspend fun trackInvitationEvent(invitationId: String, event: InvitationEvent) {
        val analytics = InvitationAnalytics(
            farmId = "", // Will be filled by the service
            invitationId = invitationId,
            event = event
        )
        invitationDao.insertInvitationAnalytics(analytics)
    }
}

// Data classes for repository responses
data class FarmWithAccess(
    val farm: Farm,
    val access: FarmAccess
)

data class FarmTeamMember(
    val user: User,
    val access: FarmAccess
)

data class FarmAccessAnalytics(
    val farmId: String,
    val totalUsers: Int,
    val activeUsers: Int,
    val pendingUsers: Int,
    val roleDistribution: Map<FarmRole, Int>,
    val accessTrends: List<Map<String, Any>>,
    val invitationStatistics: Map<String, Any>,
    val lastCalculated: Long
)

data class SecurityAlert(
    val type: SecurityAlertType,
    val message: String,
    val severity: AlertSeverity,
    val farmId: String,
    val userId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SecurityAlertType {
    ACCESS_EXPIRING,
    INACTIVE_USER,
    SUSPICIOUS_ACTIVITY,
    PERMISSION_ESCALATION,
    MULTIPLE_FAILED_LOGINS
}

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Placeholder interfaces (implement based on your existing architecture)
interface UserRepository {
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByEmail(email: String): User?
}

interface NotificationService {
    suspend fun sendInvitationEmail(invitation: FarmInvitation)
    suspend fun sendWelcomeNotification(access: FarmAccess, farmName: String)
    suspend fun sendRoleChangeNotification(access: FarmAccess, previousRole: FarmRole)
    suspend fun sendAccessRevokedNotification(access: FarmAccess)
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String = ""
)