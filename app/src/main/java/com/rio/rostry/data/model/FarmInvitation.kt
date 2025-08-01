package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID
import kotlin.random.Random

/**
 * Farm invitation system for secure user onboarding
 * Handles invitation lifecycle from creation to acceptance/rejection
 */
@Entity(tableName = "farm_invitations")
data class FarmInvitation(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val farmName: String,
    val inviterUserId: String,
    val inviterName: String,
    val inviterEmail: String,
    val inviteeEmail: String,
    val inviteeUserId: String? = null, // Set when user is found in system
    val proposedRole: FarmRole,
    val customPermissions: List<FarmPermission> = emptyList(),
    val invitationMessage: String = "",
    val invitationCode: String = generateInvitationCode(),
    val invitationLink: String = generateInvitationLink(),
    val status: InvitationStatus = InvitationStatus.SENT,
    val priority: InvitationPriority = InvitationPriority.NORMAL,
    val sentAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days
    val respondedAt: Long? = null,
    val remindersSent: Int = 0,
    val lastReminderAt: Long? = null,
    val maxReminders: Int = 3,
    val allowCustomRole: Boolean = false,
    val requiresApproval: Boolean = false,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val metadata: InvitationMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if invitation is still valid
     */
    fun isValid(): Boolean {
        return status == InvitationStatus.SENT && 
               expiresAt > System.currentTimeMillis() &&
               (!requiresApproval || approvedBy != null)
    }

    /**
     * Check if invitation is expired
     */
    fun isExpired(): Boolean {
        return expiresAt <= System.currentTimeMillis()
    }

    /**
     * Check if reminder can be sent
     */
    fun canSendReminder(): Boolean {
        val timeSinceLastReminder = lastReminderAt?.let { 
            System.currentTimeMillis() - it 
        } ?: Long.MAX_VALUE
        
        return isValid() && 
               remindersSent < maxReminders && 
               timeSinceLastReminder > (24 * 60 * 60 * 1000) // 24 hours
    }

    /**
     * Get days until expiration
     */
    fun getDaysUntilExpiration(): Long {
        val timeRemaining = expiresAt - System.currentTimeMillis()
        return if (timeRemaining > 0) timeRemaining / (24 * 60 * 60 * 1000) else 0
    }

    /**
     * Get effective permissions (custom or role default)
     */
    fun getEffectivePermissions(): List<FarmPermission> {
        return if (customPermissions.isNotEmpty()) customPermissions else proposedRole.defaultPermissions
    }

    /**
     * Check if invitation needs approval
     */
    fun needsApproval(): Boolean {
        return requiresApproval && approvedBy == null
    }

    /**
     * Get invitation urgency based on expiration and priority
     */
    fun getUrgency(): InvitationUrgency {
        val daysLeft = getDaysUntilExpiration()
        return when {
            priority == InvitationPriority.URGENT -> InvitationUrgency.CRITICAL
            daysLeft <= 1 -> InvitationUrgency.HIGH
            daysLeft <= 3 -> InvitationUrgency.MEDIUM
            else -> InvitationUrgency.LOW
        }
    }
}

/**
 * Invitation status tracking
 */
enum class InvitationStatus(val displayName: String, val description: String, val color: String) {
    DRAFT("Draft", "Invitation being prepared", "#9E9E9E"),
    SENT("Sent", "Invitation sent to recipient", "#2196F3"),
    DELIVERED("Delivered", "Invitation delivered successfully", "#4CAF50"),
    OPENED("Opened", "Recipient viewed the invitation", "#FF9800"),
    ACCEPTED("Accepted", "Invitation accepted by recipient", "#4CAF50"),
    REJECTED("Rejected", "Invitation declined by recipient", "#F44336"),
    EXPIRED("Expired", "Invitation expired without response", "#9E9E9E"),
    CANCELLED("Cancelled", "Invitation cancelled by sender", "#607D8B"),
    PENDING_APPROVAL("Pending Approval", "Waiting for admin approval", "#FF9800")
}

/**
 * Invitation priority levels
 */
enum class InvitationPriority(val displayName: String, val color: String) {
    LOW("Low Priority", "#4CAF50"),
    NORMAL("Normal Priority", "#2196F3"),
    HIGH("High Priority", "#FF9800"),
    URGENT("Urgent", "#F44336")
}

/**
 * Invitation urgency for UI display
 */
enum class InvitationUrgency(val displayName: String, val color: String) {
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HIGH("High", "#F44336"),
    CRITICAL("Critical", "#9C27B0")
}

/**
 * Additional invitation metadata
 */
data class InvitationMetadata(
    val invitationSource: InvitationSource = InvitationSource.MANUAL,
    val deviceInfo: String = "",
    val ipAddress: String = "",
    val userAgent: String = "",
    val referralCode: String? = null,
    val campaignId: String? = null,
    val customFields: Map<String, String> = emptyMap(),
    val attachments: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * Invitation source tracking
 */
enum class InvitationSource(val displayName: String) {
    MANUAL("Manual Invitation"),
    BULK_IMPORT("Bulk Import"),
    API("API Integration"),
    QR_CODE("QR Code"),
    REFERRAL("User Referral"),
    MARKETPLACE("Marketplace Connection")
}

/**
 * Invitation template for standardized invitations
 */
@Entity(tableName = "invitation_templates")
data class InvitationTemplate(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val farmId: String,
    val defaultRole: FarmRole,
    val defaultPermissions: List<FarmPermission> = emptyList(),
    val messageTemplate: String,
    val subjectTemplate: String = "Invitation to join {farmName}",
    val expirationDays: Int = 7,
    val maxReminders: Int = 3,
    val requiresApproval: Boolean = false,
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Generate invitation message from template
     */
    fun generateMessage(
        farmName: String,
        inviterName: String,
        inviteeEmail: String,
        customVariables: Map<String, String> = emptyMap()
    ): String {
        var message = messageTemplate
        
        // Replace standard variables
        message = message.replace("{farmName}", farmName)
        message = message.replace("{inviterName}", inviterName)
        message = message.replace("{inviteeEmail}", inviteeEmail)
        message = message.replace("{roleName}", defaultRole.displayName)
        
        // Replace custom variables
        customVariables.forEach { (key, value) ->
            message = message.replace("{$key}", value)
        }
        
        return message
    }

    /**
     * Generate subject from template
     */
    fun generateSubject(farmName: String, customVariables: Map<String, String> = emptyMap()): String {
        var subject = subjectTemplate
        subject = subject.replace("{farmName}", farmName)
        
        customVariables.forEach { (key, value) ->
            subject = subject.replace("{$key}", value)
        }
        
        return subject
    }
}

/**
 * Invitation analytics for tracking performance
 */
@Entity(tableName = "invitation_analytics")
data class InvitationAnalytics(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val invitationId: String,
    val event: InvitationEvent,
    val timestamp: Long = System.currentTimeMillis(),
    val userAgent: String = "",
    val ipAddress: String = "",
    val deviceType: String = "",
    val location: String = "",
    val additionalData: Map<String, String> = emptyMap()
)

/**
 * Invitation events for analytics
 */
enum class InvitationEvent(val displayName: String) {
    CREATED("Invitation Created"),
    SENT("Invitation Sent"),
    DELIVERED("Email Delivered"),
    OPENED("Email Opened"),
    CLICKED("Link Clicked"),
    VIEWED("Invitation Viewed"),
    ACCEPTED("Invitation Accepted"),
    REJECTED("Invitation Rejected"),
    EXPIRED("Invitation Expired"),
    REMINDER_SENT("Reminder Sent")
}

/**
 * Bulk invitation for managing multiple invitations
 */
@Entity(tableName = "bulk_invitations")
data class BulkInvitation(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val name: String,
    val description: String = "",
    val inviterUserId: String,
    val templateId: String? = null,
    val defaultRole: FarmRole,
    val inviteeEmails: List<String>,
    val customMessage: String = "",
    val status: BulkInvitationStatus = BulkInvitationStatus.PENDING,
    val totalInvitations: Int = inviteeEmails.size,
    val sentCount: Int = 0,
    val acceptedCount: Int = 0,
    val rejectedCount: Int = 0,
    val expiredCount: Int = 0,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate completion percentage
     */
    fun getCompletionPercentage(): Float {
        return if (totalInvitations > 0) {
            (sentCount.toFloat() / totalInvitations) * 100
        } else 0f
    }

    /**
     * Calculate acceptance rate
     */
    fun getAcceptanceRate(): Float {
        return if (sentCount > 0) {
            (acceptedCount.toFloat() / sentCount) * 100
        } else 0f
    }

    /**
     * Check if bulk invitation is complete
     */
    fun isComplete(): Boolean {
        return status == BulkInvitationStatus.COMPLETED || 
               status == BulkInvitationStatus.CANCELLED ||
               sentCount >= totalInvitations
    }
}

/**
 * Bulk invitation status
 */
enum class BulkInvitationStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    FAILED("Failed")
}

// Helper functions
private fun generateInvitationCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..8)
        .map { chars[Random.nextInt(chars.length)] }
        .joinToString("")
}

private fun generateInvitationLink(): String {
    // In a real implementation, this would generate a proper deep link
    return "https://rostry.app/invite/${generateInvitationCode()}"
}

/**
 * Invitation response for tracking user responses
 */
data class InvitationResponse(
    val invitationId: String,
    val userId: String,
    val response: ResponseType,
    val responseMessage: String = "",
    val responseAt: Long = System.currentTimeMillis(),
    val deviceInfo: String = "",
    val ipAddress: String = ""
)

enum class ResponseType(val displayName: String) {
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    DEFERRED("Deferred"),
    BLOCKED("Blocked")
}