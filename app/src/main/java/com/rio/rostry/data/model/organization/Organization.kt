package com.rio.rostry.data.model.organization

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.rio.rostry.data.model.role.Permission
import com.rio.rostry.data.model.role.UserRole
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Multi-tenant organization system
 */
@Entity(tableName = "organizations")
@Serializable
data class Organization(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: OrganizationType,
    val description: String = "",
    val location: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val website: String = "",
    val logoUrl: String = "",
    val settings: OrganizationSettings = OrganizationSettings(),
    val subscription: SubscriptionTier = SubscriptionTier.FREE,
    val ownerId: String,
    val memberCount: Int = 1,
    val maxMembers: Int = SubscriptionTier.FREE.maxMembers,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if organization can add more members
     */
    fun canAddMembers(): Boolean = memberCount < maxMembers

    /**
     * Get remaining member slots
     */
    fun getRemainingSlots(): Int = maxMembers - memberCount

    /**
     * Check if organization has feature
     */
    fun hasFeature(feature: String): Boolean = subscription.features.contains(feature)

    /**
     * Check if role is allowed in this organization type
     */
    fun isRoleAllowed(roleId: String): Boolean = type.allowedRoles.contains(roleId)
}

@Serializable
enum class OrganizationType(
    val displayName: String,
    val description: String,
    val maxMembers: Int,
    val allowedRoles: Set<String> // Using String for serialization compatibility
) {
    INDIVIDUAL_FARM(
        displayName = "Individual Farm",
        description = "Single farmer operation",
        maxMembers = 5,
        allowedRoles = setOf("basic_farmer", "verified_farmer", "premium_breeder")
    ),
    COOPERATIVE(
        displayName = "Cooperative",
        description = "Farmer cooperative",
        maxMembers = 50,
        allowedRoles = setOf("basic_farmer", "verified_farmer", "premium_breeder", "farm_manager")
    ),
    COMMERCIAL_FARM(
        displayName = "Commercial Farm",
        description = "Large commercial operation",
        maxMembers = 200,
        allowedRoles = setOf("basic_farmer", "verified_farmer", "premium_breeder", "farm_manager", "moderator")
    ),
    BREEDING_FACILITY(
        displayName = "Breeding Facility",
        description = "Specialized breeding operation",
        maxMembers = 100,
        allowedRoles = setOf("premium_breeder", "farm_manager")
    ),
    RESEARCH_INSTITUTION(
        displayName = "Research Institution",
        description = "Agricultural research facility",
        maxMembers = 500,
        allowedRoles = setOf("verified_farmer", "premium_breeder", "farm_manager", "moderator", "super_admin")
    ),
    MARKETPLACE_VENDOR(
        displayName = "Marketplace Vendor",
        description = "Product vendor",
        maxMembers = 20,
        allowedRoles = setOf("verified_farmer", "premium_breeder")
    ),
    EDUCATIONAL(
        displayName = "Educational",
        description = "Educational institution",
        maxMembers = 1000,
        allowedRoles = setOf("consumer", "basic_farmer", "verified_farmer", "moderator")
    )
}

@Serializable
data class OrganizationSettings(
    val allowPublicProfile: Boolean = true,
    val enableTeamCollaboration: Boolean = false,
    val enableAdvancedAnalytics: Boolean = false,
    val enableAPIAccess: Boolean = false,
    val customBranding: Boolean = false,
    val dataRetentionDays: Int = 365,
    val enableAuditLogs: Boolean = false,
    val requireTwoFactorAuth: Boolean = false,
    val allowGuestAccess: Boolean = false,
    val enableNotifications: Boolean = true,
    val autoApproveInvitations: Boolean = false,
    val maxProjectsPerUser: Int = 10
)

@Serializable
enum class SubscriptionTier(
    val displayName: String,
    val monthlyPrice: Double,
    val maxMembers: Int,
    val maxFarms: Int,
    val features: Set<String>
) {
    FREE(
        displayName = "Free",
        monthlyPrice = 0.0,
        maxMembers = 3,
        maxFarms = 1,
        features = setOf("basic_farm_management", "marketplace_access")
    ),
    BASIC(
        displayName = "Basic",
        monthlyPrice = 9.99,
        maxMembers = 10,
        maxFarms = 3,
        features = setOf("basic_farm_management", "marketplace_access", "basic_analytics", "team_collaboration")
    ),
    PROFESSIONAL(
        displayName = "Professional",
        monthlyPrice = 29.99,
        maxMembers = 50,
        maxFarms = 10,
        features = setOf(
            "advanced_farm_management", "marketplace_access", "advanced_analytics",
            "team_collaboration", "api_access", "custom_reports"
        )
    ),
    ENTERPRISE(
        displayName = "Enterprise",
        monthlyPrice = 99.99,
        maxMembers = 500,
        maxFarms = 50,
        features = setOf(
            "enterprise_farm_management", "marketplace_access", "enterprise_analytics",
            "team_collaboration", "api_access", "custom_integrations", "priority_support",
            "advanced_security", "audit_logs", "custom_branding"
        )
    )
}

/**
 * User membership in organization with role context
 */
@Entity(tableName = "organization_memberships")
@Serializable
data class OrganizationMembership(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val userId: String,
    val roleId: String, // Using String for serialization
    val customPermissions: Set<String> = emptySet(), // Using String for serialization
    val isActive: Boolean = true,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val invitedBy: String? = null,
    val notes: String = "",
    val department: String = "",
    val title: String = "",
    val accessLevel: AccessLevel = AccessLevel.STANDARD
) {
    /**
     * Check if membership is currently valid
     */
    fun isValid(): Boolean = isActive

    /**
     * Get days since joining
     */
    fun getDaysSinceJoining(): Long {
        return (System.currentTimeMillis() - joinedAt) / (24 * 60 * 60 * 1000)
    }

    /**
     * Get days since last activity
     */
    fun getDaysSinceLastActivity(): Long {
        return (System.currentTimeMillis() - lastActiveAt) / (24 * 60 * 60 * 1000)
    }

    /**
     * Get effective permissions for this membership
     */
    fun getEffectivePermissions(): Set<Permission> {
        // For now, return empty set - this would be implemented based on roleId and customPermissions
        return emptySet()
    }
}

@Serializable
enum class AccessLevel(val displayName: String, val priority: Int) {
    GUEST("Guest", 1),
    STANDARD("Standard", 2),
    ELEVATED("Elevated", 3),
    ADMIN("Admin", 4)
}

/**
 * Organization invitation system
 */
@Entity(tableName = "organization_invitations")
@Serializable
data class OrganizationInvitation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val organizationId: String,
    val organizationName: String,
    val inviterUserId: String,
    val inviterName: String,
    val inviteeEmail: String,
    val inviteeUserId: String? = null,
    val proposedRoleId: String,
    val message: String = "",
    val status: InvitationStatus = InvitationStatus.PENDING,
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days
    val sentAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val responseMessage: String = ""
) {
    /**
     * Check if invitation is still valid
     */
    fun isValid(): Boolean {
        return status == InvitationStatus.PENDING && System.currentTimeMillis() < expiresAt
    }

    /**
     * Check if invitation is expiring soon
     */
    fun isExpiringSoon(hoursAhead: Int = 24): Boolean {
        val warningTime = System.currentTimeMillis() + (hoursAhead * 60 * 60 * 1000L)
        return expiresAt <= warningTime
    }
}

@Serializable
enum class InvitationStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
}