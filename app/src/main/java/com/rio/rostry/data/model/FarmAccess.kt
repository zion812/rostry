package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Farm access management entity for multi-user collaboration
 * Handles user roles, permissions, and access control within farms
 */
@Entity(tableName = "farm_access")
data class FarmAccess(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val userId: String,
    val role: FarmRole,
    val permissions: List<FarmPermission> = emptyList(),
    val invitedBy: String,
    val invitedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val status: AccessStatus = AccessStatus.PENDING,
    val expiresAt: Long? = null,
    val isActive: Boolean = true,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if access is currently valid
     */
    fun isValidAccess(): Boolean {
        return isActive && 
               status == AccessStatus.ACCEPTED && 
               (expiresAt == null || expiresAt > System.currentTimeMillis())
    }

    /**
     * Check if user has specific permission
     */
    fun hasPermission(permission: FarmPermission): Boolean {
        return isValidAccess() && permissions.contains(permission)
    }

    /**
     * Check if user can perform action based on role hierarchy
     */
    fun canManageRole(targetRole: FarmRole): Boolean {
        return isValidAccess() && role.hierarchy < targetRole.hierarchy
    }

    /**
     * Get days since last access
     */
    fun getDaysSinceLastAccess(): Long {
        return (System.currentTimeMillis() - lastAccessedAt) / (24 * 60 * 60 * 1000)
    }

    /**
     * Check if access is expiring soon
     */
    fun isExpiringSoon(daysAhead: Int = 7): Boolean {
        return expiresAt?.let { expiry ->
            val warningTime = System.currentTimeMillis() + (daysAhead * 24 * 60 * 60 * 1000L)
            expiry <= warningTime
        } ?: false
    }
}

/**
 * Farm roles with hierarchical permissions
 */
enum class FarmRole(
    val displayName: String,
    val description: String,
    val defaultPermissions: List<FarmPermission>,
    val hierarchy: Int,
    val color: String
) {
    OWNER(
        "Farm Owner", 
        "Full control over farm and all operations",
        FarmPermission.values().toList(), 
        1,
        "#4CAF50"
    ),
    
    MANAGER(
        "Farm Manager", 
        "Manages daily operations and team members",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.EDIT_FARM,
            FarmPermission.MANAGE_FLOCKS,
            FarmPermission.MANAGE_FOWLS,
            FarmPermission.VIEW_ANALYTICS,
            FarmPermission.MANAGE_RECORDS,
            FarmPermission.INVITE_WORKERS,
            FarmPermission.MANAGE_FACILITIES,
            FarmPermission.MANAGE_TASKS,
            FarmPermission.MARKETPLACE_LISTING
        ), 
        2,
        "#2196F3"
    ),
    
    VETERINARIAN(
        "Veterinarian", 
        "Specialized in health and medical care",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.VIEW_FOWLS,
            FarmPermission.MANAGE_HEALTH_RECORDS,
            FarmPermission.VIEW_ANALYTICS,
            FarmPermission.MANAGE_VACCINATIONS,
            FarmPermission.VIEW_TASKS
        ), 
        3,
        "#9C27B0"
    ),
    
    SUPERVISOR(
        "Supervisor",
        "Supervises workers and daily tasks",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.VIEW_FOWLS,
            FarmPermission.MANAGE_RECORDS,
            FarmPermission.UPDATE_GROWTH_RECORDS,
            FarmPermission.VIEW_TASKS,
            FarmPermission.MANAGE_TASKS,
            FarmPermission.VIEW_ANALYTICS
        ),
        4,
        "#FF9800"
    ),
    
    WORKER(
        "Farm Worker", 
        "Performs daily farm tasks and maintenance",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.VIEW_FOWLS,
            FarmPermission.UPDATE_GROWTH_RECORDS,
            FarmPermission.VIEW_TASKS
        ), 
        5,
        "#607D8B"
    ),
    
    SPECIALIST(
        "Specialist",
        "Expert in specific areas (breeding, nutrition, etc.)",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.VIEW_FOWLS,
            FarmPermission.VIEW_ANALYTICS,
            FarmPermission.MANAGE_RECORDS,
            FarmPermission.VIEW_TASKS
        ),
        6,
        "#795548"
    ),
    
    VIEWER(
        "Viewer", 
        "Read-only access to farm information",
        listOf(
            FarmPermission.VIEW_FARM,
            FarmPermission.VIEW_FOWLS
        ), 
        7,
        "#9E9E9E"
    );

    /**
     * Check if this role can manage another role
     */
    fun canManage(otherRole: FarmRole): Boolean {
        return hierarchy < otherRole.hierarchy
    }

    /**
     * Get roles that this role can manage
     */
    fun getManageableRoles(): List<FarmRole> {
        return values().filter { canManage(it) }
    }

    /**
     * Get roles that can manage this role
     */
    fun getManagedByRoles(): List<FarmRole> {
        return values().filter { it.canManage(this) }
    }
}

/**
 * Comprehensive farm permissions system
 */
enum class FarmPermission(
    val displayName: String, 
    val description: String,
    val category: PermissionCategory,
    val riskLevel: RiskLevel
) {
    // Farm Management
    VIEW_FARM("View Farm", "Can view basic farm information", PermissionCategory.FARM_MANAGEMENT, RiskLevel.LOW),
    EDIT_FARM("Edit Farm", "Can modify farm details and settings", PermissionCategory.FARM_MANAGEMENT, RiskLevel.MEDIUM),
    DELETE_FARM("Delete Farm", "Can delete the entire farm", PermissionCategory.FARM_MANAGEMENT, RiskLevel.CRITICAL),
    
    // Flock Management
    MANAGE_FLOCKS("Manage Flocks", "Can create, edit, and delete flocks", PermissionCategory.FLOCK_MANAGEMENT, RiskLevel.MEDIUM),
    VIEW_FLOCKS("View Flocks", "Can view flock information", PermissionCategory.FLOCK_MANAGEMENT, RiskLevel.LOW),
    
    // Fowl Management
    VIEW_FOWLS("View Fowls", "Can view fowl information", PermissionCategory.FOWL_MANAGEMENT, RiskLevel.LOW),
    MANAGE_FOWLS("Manage Fowls", "Can add, edit, and delete fowls", PermissionCategory.FOWL_MANAGEMENT, RiskLevel.MEDIUM),
    TRANSFER_FOWLS("Transfer Fowls", "Can initiate fowl transfers", PermissionCategory.FOWL_MANAGEMENT, RiskLevel.HIGH),
    
    // Records Management
    MANAGE_RECORDS("Manage Records", "Can add and edit all types of records", PermissionCategory.RECORDS, RiskLevel.MEDIUM),
    MANAGE_HEALTH_RECORDS("Manage Health Records", "Can manage health and medical records", PermissionCategory.RECORDS, RiskLevel.MEDIUM),
    UPDATE_GROWTH_RECORDS("Update Growth Records", "Can update growth measurements", PermissionCategory.RECORDS, RiskLevel.LOW),
    MANAGE_VACCINATIONS("Manage Vaccinations", "Can manage vaccination schedules", PermissionCategory.RECORDS, RiskLevel.MEDIUM),
    
    // Analytics & Reporting
    VIEW_ANALYTICS("View Analytics", "Can view farm analytics and reports", PermissionCategory.ANALYTICS, RiskLevel.LOW),
    EXPORT_DATA("Export Data", "Can export farm data", PermissionCategory.ANALYTICS, RiskLevel.MEDIUM),
    
    // Facility Management
    MANAGE_FACILITIES("Manage Facilities", "Can manage farm facilities and equipment", PermissionCategory.FACILITIES, RiskLevel.MEDIUM),
    VIEW_FACILITIES("View Facilities", "Can view facility information", PermissionCategory.FACILITIES, RiskLevel.LOW),
    
    // User Management
    INVITE_USERS("Invite Users", "Can invite new users to the farm", PermissionCategory.USER_MANAGEMENT, RiskLevel.HIGH),
    INVITE_WORKERS("Invite Workers", "Can invite workers and staff", PermissionCategory.USER_MANAGEMENT, RiskLevel.MEDIUM),
    MANAGE_ACCESS("Manage Access", "Can modify user roles and permissions", PermissionCategory.USER_MANAGEMENT, RiskLevel.CRITICAL),
    REMOVE_USERS("Remove Users", "Can remove users from farm", PermissionCategory.USER_MANAGEMENT, RiskLevel.HIGH),
    
    // Task Management
    VIEW_TASKS("View Tasks", "Can view assigned tasks", PermissionCategory.TASK_MANAGEMENT, RiskLevel.LOW),
    MANAGE_TASKS("Manage Tasks", "Can create and assign tasks", PermissionCategory.TASK_MANAGEMENT, RiskLevel.MEDIUM),
    ASSIGN_TASKS("Assign Tasks", "Can assign tasks to team members", PermissionCategory.TASK_MANAGEMENT, RiskLevel.MEDIUM),
    
    // Marketplace & Financial
    MARKETPLACE_LISTING("Marketplace Listing", "Can list fowls for sale", PermissionCategory.MARKETPLACE, RiskLevel.MEDIUM),
    FINANCIAL_ACCESS("Financial Access", "Can view financial information", PermissionCategory.FINANCIAL, RiskLevel.HIGH),
    MANAGE_TRANSACTIONS("Manage Transactions", "Can manage financial transactions", PermissionCategory.FINANCIAL, RiskLevel.CRITICAL),
    
    // System Administration
    BACKUP_DATA("Backup Data", "Can create data backups", PermissionCategory.SYSTEM, RiskLevel.MEDIUM),
    RESTORE_DATA("Restore Data", "Can restore data from backups", PermissionCategory.SYSTEM, RiskLevel.CRITICAL),
    SYSTEM_SETTINGS("System Settings", "Can modify system settings", PermissionCategory.SYSTEM, RiskLevel.HIGH);

    /**
     * Get permissions by category
     */
    companion object {
        fun getByCategory(category: PermissionCategory): List<FarmPermission> {
            return values().filter { it.category == category }
        }

        fun getByRiskLevel(riskLevel: RiskLevel): List<FarmPermission> {
            return values().filter { it.riskLevel == riskLevel }
        }

        fun getCriticalPermissions(): List<FarmPermission> {
            return getByRiskLevel(RiskLevel.CRITICAL)
        }
    }
}

/**
 * Permission categories for organization
 */
enum class PermissionCategory(val displayName: String, val icon: String) {
    FARM_MANAGEMENT("Farm Management", "🏡"),
    FLOCK_MANAGEMENT("Flock Management", "🐔"),
    FOWL_MANAGEMENT("Fowl Management", "🐓"),
    RECORDS("Records & Documentation", "📋"),
    ANALYTICS("Analytics & Reports", "📊"),
    FACILITIES("Facilities & Equipment", "🏗️"),
    USER_MANAGEMENT("User Management", "👥"),
    TASK_MANAGEMENT("Task Management", "✅"),
    MARKETPLACE("Marketplace", "🛒"),
    FINANCIAL("Financial", "💰"),
    SYSTEM("System Administration", "⚙️")
}

/**
 * Risk levels for permissions
 */
enum class RiskLevel(val displayName: String, val color: String, val description: String) {
    LOW("Low Risk", "#4CAF50", "Safe operations with minimal impact"),
    MEDIUM("Medium Risk", "#FF9800", "Operations that require caution"),
    HIGH("High Risk", "#F44336", "Operations with significant impact"),
    CRITICAL("Critical Risk", "#9C27B0", "Operations that can cause major damage")
}

/**
 * Access status for farm access records
 */
enum class AccessStatus(val displayName: String, val description: String) {
    PENDING("Pending", "Invitation sent, awaiting response"),
    ACCEPTED("Active", "Access granted and active"),
    REJECTED("Rejected", "Invitation was declined"),
    REVOKED("Revoked", "Access was removed"),
    EXPIRED("Expired", "Access has expired"),
    SUSPENDED("Suspended", "Access temporarily suspended")
}

/**
 * Access audit log for tracking permission changes
 */
@Entity(tableName = "access_audit_log")
data class AccessAuditLog(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val targetUserId: String,
    val actionPerformedBy: String,
    val action: AccessAction,
    val previousRole: FarmRole? = null,
    val newRole: FarmRole? = null,
    val previousPermissions: List<FarmPermission> = emptyList(),
    val newPermissions: List<FarmPermission> = emptyList(),
    val reason: String = "",
    val ipAddress: String = "",
    val userAgent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Access actions for audit logging
 */
enum class AccessAction(val displayName: String) {
    INVITED("User Invited"),
    ACCEPTED("Invitation Accepted"),
    REJECTED("Invitation Rejected"),
    ROLE_CHANGED("Role Changed"),
    PERMISSIONS_MODIFIED("Permissions Modified"),
    ACCESS_REVOKED("Access Revoked"),
    ACCESS_SUSPENDED("Access Suspended"),
    ACCESS_RESTORED("Access Restored"),
    LOGIN("User Login"),
    LOGOUT("User Logout")
}

/**
 * Permission request for temporary access elevation
 */
@Entity(tableName = "permission_requests")
data class PermissionRequest(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val requesterId: String,
    val requestedPermissions: List<FarmPermission>,
    val reason: String,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL,
    val requestedDuration: Long? = null, // Duration in milliseconds
    val status: RequestStatus = RequestStatus.PENDING,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val reviewNotes: String = "",
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UrgencyLevel(val displayName: String, val color: String) {
    LOW("Low Priority", "#4CAF50"),
    NORMAL("Normal Priority", "#2196F3"),
    HIGH("High Priority", "#FF9800"),
    URGENT("Urgent", "#F44336")
}

enum class RequestStatus(val displayName: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    REVOKED("Revoked")
}