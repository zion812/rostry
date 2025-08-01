package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val role: UserRole = UserRole.GENERAL,
    val phoneNumber: String = "",
    val location: String = "",
    val bio: String = "",
    val isKycVerified: Boolean = false,
    val kycDocumentUrl: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val verificationBadges: List<String> = emptyList(), // "breeder", "farm", "premium"
    val coinBalance: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalCoinsSpent: Int = 0,
    val sellerRating: Double = 0.0,
    val totalSales: Int = 0,
    val joinedDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val uid: String get() = id
    val isVerified: Boolean get() = verificationStatus == VerificationStatus.VERIFIED
    val isPremiumSeller: Boolean get() = verificationBadges.contains("premium")
    val isBreederVerified: Boolean get() = verificationBadges.contains("breeder")
    val isFarmVerified: Boolean get() = verificationBadges.contains("farm")
}

enum class UserRole {
    GENERAL,
    FARMER,
    ENTHUSIAST
}

// VerificationStatus is defined in VerificationModels.kt to avoid redeclaration