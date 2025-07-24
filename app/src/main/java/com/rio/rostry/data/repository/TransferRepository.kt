package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.model.TransferLog
import com.rio.rostry.data.model.TransferNotification
import com.rio.rostry.data.model.Fowl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val fowlRepository: FowlRepository
) {
    
    suspend fun initiateTransfer(
        fowlId: String,
        giverId: String,
        giverName: String,
        receiverId: String,
        receiverName: String,
        agreedPrice: Double,
        currentWeight: Double?,
        transferNotes: String,
        recentPhotoUri: android.net.Uri?
    ): Result<String> {
        return try {
            val transferId = UUID.randomUUID().toString()
            
            // Upload recent photo if provided
            var recentPhotoUrl: String? = null
            recentPhotoUri?.let { uri ->
                val photoRef = storage.reference.child("transfer_photos/$transferId/${System.currentTimeMillis()}.jpg")
                val uploadTask = photoRef.putFile(uri).await()
                recentPhotoUrl = uploadTask.storage.downloadUrl.await().toString()
            }
            
            // Create verification details map
            val verificationDetails = mutableMapOf<String, String>().apply {
                put("agreed_price", agreedPrice.toString())
                currentWeight?.let { put("current_weight", it.toString()) }
                recentPhotoUrl?.let { put("recent_photo_url", it) }
                if (transferNotes.isNotBlank()) put("transfer_notes", transferNotes)
            }
            
            val transferLog = TransferLog(
                transferId = transferId,
                fowlId = fowlId,
                giverId = giverId,
                giverName = giverName,
                receiverId = receiverId,
                receiverName = receiverName,
                status = "pending",
                verificationDetails = verificationDetails,
                agreedPrice = agreedPrice,
                currentWeight = currentWeight,
                recentPhotoUrl = recentPhotoUrl,
                transferNotes = transferNotes,
                timestamp = System.currentTimeMillis()
            )
            
            // Save transfer log
            firestore.collection("transfer_logs").document(transferId).set(transferLog).await()
            
            // Create notification for receiver
            val notification = TransferNotification(
                id = UUID.randomUUID().toString(),
                userId = receiverId,
                transferId = transferId,
                type = "transfer_request",
                title = "Transfer Request",
                message = "$giverName wants to transfer a fowl to you. Please verify the details.",
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("notifications").document(notification.id).set(notification).await()
            
            Result.success(transferId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyTransfer(transferId: String, receiverId: String): Result<Unit> {
        return try {
            val transferRef = firestore.collection("transfer_logs").document(transferId)
            val snapshot = transferRef.get().await()
            val transfer = snapshot.toObject(TransferLog::class.java)
            
            if (transfer == null) {
                return Result.failure(Exception("Transfer not found"))
            }
            
            if (transfer.receiverId != receiverId) {
                return Result.failure(Exception("Unauthorized to verify this transfer"))
            }
            
            if (transfer.status != "pending") {
                return Result.failure(Exception("Transfer is not in pending status"))
            }
            
            // Update transfer status
            val updatedTransfer = transfer.copy(
                status = "verified",
                verifiedAt = System.currentTimeMillis()
            )
            transferRef.set(updatedTransfer).await()
            
            // Transfer fowl ownership
            val fowl = fowlRepository.getFowlById(transfer.fowlId)
            if (fowl != null) {
                val updatedFowl = fowl.copy(
                    ownerId = transfer.receiverId,
                    isForSale = false,
                    updatedAt = System.currentTimeMillis()
                )
                fowlRepository.updateFowl(updatedFowl)
            }
            
            // Create notifications for both parties
            val giverNotification = TransferNotification(
                id = UUID.randomUUID().toString(),
                userId = transfer.giverId,
                transferId = transferId,
                type = "transfer_verified",
                title = "Transfer Verified",
                message = "${transfer.receiverName} has verified the transfer. Ownership has been transferred.",
                createdAt = System.currentTimeMillis()
            )
            
            val receiverNotification = TransferNotification(
                id = UUID.randomUUID().toString(),
                userId = transfer.receiverId,
                transferId = transferId,
                type = "transfer_verified",
                title = "Transfer Complete",
                message = "You have successfully received the fowl from ${transfer.giverName}.",
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("notifications").document(giverNotification.id).set(giverNotification).await()
            firestore.collection("notifications").document(receiverNotification.id).set(receiverNotification).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectTransfer(transferId: String, receiverId: String, rejectionReason: String): Result<Unit> {
        return try {
            val transferRef = firestore.collection("transfer_logs").document(transferId)
            val snapshot = transferRef.get().await()
            val transfer = snapshot.toObject(TransferLog::class.java)
            
            if (transfer == null) {
                return Result.failure(Exception("Transfer not found"))
            }
            
            if (transfer.receiverId != receiverId) {
                return Result.failure(Exception("Unauthorized to reject this transfer"))
            }
            
            if (transfer.status != "pending") {
                return Result.failure(Exception("Transfer is not in pending status"))
            }
            
            // Update transfer status
            val updatedTransfer = transfer.copy(
                status = "rejected",
                rejectionReason = rejectionReason,
                rejectedAt = System.currentTimeMillis()
            )
            transferRef.set(updatedTransfer).await()
            
            // Create notifications
            val giverNotification = TransferNotification(
                id = UUID.randomUUID().toString(),
                userId = transfer.giverId,
                transferId = transferId,
                type = "transfer_rejected",
                title = "Transfer Rejected",
                message = "${transfer.receiverName} has rejected the transfer. Reason: $rejectionReason",
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("notifications").document(giverNotification.id).set(giverNotification).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelTransfer(transferId: String, giverId: String): Result<Unit> {
        return try {
            val transferRef = firestore.collection("transfer_logs").document(transferId)
            val snapshot = transferRef.get().await()
            val transfer = snapshot.toObject(TransferLog::class.java)
            
            if (transfer == null) {
                return Result.failure(Exception("Transfer not found"))
            }
            
            if (transfer.giverId != giverId) {
                return Result.failure(Exception("Unauthorized to cancel this transfer"))
            }
            
            if (transfer.status != "pending") {
                return Result.failure(Exception("Transfer is not in pending status"))
            }
            
            // Update transfer status
            val updatedTransfer = transfer.copy(
                status = "cancelled",
                rejectedAt = System.currentTimeMillis()
            )
            transferRef.set(updatedTransfer).await()
            
            // Create notification for receiver
            val receiverNotification = TransferNotification(
                id = UUID.randomUUID().toString(),
                userId = transfer.receiverId,
                transferId = transferId,
                type = "transfer_cancelled",
                title = "Transfer Cancelled",
                message = "${transfer.giverName} has cancelled the transfer request.",
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("notifications").document(receiverNotification.id).set(receiverNotification).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFowlTransferHistory(fowlId: String): Flow<List<TransferLog>> = flow {
        try {
            val snapshot = firestore.collection("transfer_logs")
                .whereEqualTo("fowlId", fowlId)
                .whereEqualTo("status", "verified")
                .orderBy("verifiedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transfers = snapshot.documents.mapNotNull { it.toObject(TransferLog::class.java) }
            emit(transfers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getUserTransfers(userId: String): Flow<List<TransferLog>> = flow {
        try {
            // Get transfers where user is either giver or receiver
            val giverSnapshot = firestore.collection("transfer_logs")
                .whereEqualTo("giverId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val receiverSnapshot = firestore.collection("transfer_logs")
                .whereEqualTo("receiverId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val giverTransfers = giverSnapshot.documents.mapNotNull { it.toObject(TransferLog::class.java) }
            val receiverTransfers = receiverSnapshot.documents.mapNotNull { it.toObject(TransferLog::class.java) }
            
            val allTransfers = (giverTransfers + receiverTransfers)
                .distinctBy { it.transferId }
                .sortedByDescending { it.timestamp }
            
            emit(allTransfers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getPendingTransfers(userId: String): Flow<List<TransferLog>> = flow {
        try {
            val snapshot = firestore.collection("transfer_logs")
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transfers = snapshot.documents.mapNotNull { it.toObject(TransferLog::class.java) }
            emit(transfers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getUserNotifications(userId: String): Flow<List<TransferNotification>> = flow {
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val notifications = snapshot.documents.mapNotNull { it.toObject(TransferNotification::class.java) }
            emit(notifications)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}