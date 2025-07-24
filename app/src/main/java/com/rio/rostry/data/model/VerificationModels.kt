package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VerificationType {
    USER,           // User profile verification
    FOWL,           // Individual fowl verification
    BREEDER,        // Breeder certification
    FARM           // Farm verification
}

enum class VerificationStatus {
    UNVERIFIED,     // Not verified
    PENDING,        // Verification request submitted
    UNDER_REVIEW,   // Being reviewed by admin
    VERIFIED,       // Successfully verified
    APPROVED,       // Approved (alias for VERIFIED)
    REJECTED,       // Verification rejected
    EXPIRED         // Verification expired
}

@Entity(tableName = "verification_requests")
data class VerificationRequest(
    @PrimaryKey
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val verificationType: VerificationType = VerificationType.USER,
    val entityId: String? = null, // fowlId for fowl verification
    val status: VerificationStatus = VerificationStatus.PENDING,
    val submittedDocuments: List<String> = emptyList(),
    val verificationNotes: String = "",
    val adminNotes: String = "",
    val coinsDeducted: Int = 0,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null
)