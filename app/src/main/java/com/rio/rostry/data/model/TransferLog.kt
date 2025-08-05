package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "transfer_logs")
data class TransferLog(
    @PrimaryKey
    val transferId: String = "",
    val fowlId: String = "",
    val giverId: String = "",
    val giverName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val status: String = "pending", // e.g., pending, verified, rejected, cancelled
    val verificationDetails: Map<String, String> = emptyMap(), // Details for receiver to confirm
    val rejectionReason: String? = null,
    val agreedPrice: Double = 0.0,
    val currentWeight: Double? = null,
    val recentPhotoUrl: String? = null,
    val transferNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val rejectedAt: Long? = null
)

enum class TransferStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    CANCELLED,
    COMPLETED
}

data class TransferNotification(
    val id: String = "",
    val userId: String = "",
    val transferId: String = "",
    val type: String = "", // transfer_request, transfer_verified, transfer_rejected
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "marketplace_listings")
data class MarketplaceListing(
    @PrimaryKey
    val listingId: String = "",
    val fowlId: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerRating: Double = 0.0,
    val price: Double = 0.0,
    val purpose: String = "", // Breeding Stock, Meat, Eggs
    val isActive: Boolean = true,
    val featuredImageUrl: String = "",
    val description: String = "",
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Auto-populated from fowl profile
    val fowlName: String = "",
    val fowlBreed: String = "",
    val fowlType: String = "",
    val fowlGender: String = "",
    val fowlAge: String = "",
    val motherId: String? = null,
    val fatherId: String? = null,
    val vaccinationRecords: List<String> = emptyList(),
    val healthStatus: String = "",
    val isBreederReady: Boolean = false,
    // Enhanced lineage tracking fields
    val hasTraceableLineage: Boolean = false,
    val lineageVerified: Boolean = false,
    val generation: Int? = null,
    val bloodlineId: String? = null,
    val inbreedingCoefficient: Double? = null,
    val lineageNotes: String = ""
)