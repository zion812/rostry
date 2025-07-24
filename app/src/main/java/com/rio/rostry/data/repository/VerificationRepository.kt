package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.VerificationDao
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val verificationDao: VerificationDao,
    private val walletRepository: WalletRepository
) {
    
    suspend fun submitVerificationRequest(
        userId: String,
        userName: String,
        userEmail: String,
        verificationType: VerificationType,
        entityId: String? = null,
        documents: List<android.net.Uri>,
        notes: String
    ): Result<String> {
        return try {
            val requestId = UUID.randomUUID().toString()
            
            // Check if user has sufficient coins
            val coinPricing = walletRepository.getCoinPricing()
            val requiredCoins = coinPricing.verificationFee
            val currentBalance = walletRepository.getCoinBalance(userId)
            
            if (currentBalance < requiredCoins) {
                return Result.failure(InsufficientCoinsException("Insufficient coins for verification. Required: $requiredCoins, Available: $currentBalance"))
            }
            
            // Upload documents
            val documentUrls = mutableListOf<String>()
            documents.forEachIndexed { index, uri ->
                val documentRef = storage.reference.child("verification_documents/$requestId/document_$index.jpg")
                val uploadTask = documentRef.putFile(uri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                documentUrls.add(downloadUrl.toString())
            }
            
            // Create verification request
            val verificationRequest = VerificationRequest(
                requestId = requestId,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                verificationType = verificationType,
                entityId = entityId,
                submittedDocuments = documentUrls,
                verificationNotes = notes,
                coinsDeducted = requiredCoins
            )
            
            // Deduct coins
            walletRepository.deductCoins(
                userId = userId,
                amount = requiredCoins,
                description = "Verification request - ${verificationType.name}",
                relatedEntityId = requestId,
                relatedEntityType = "verification"
            )
            
            // Save verification request
            firestore.collection("verification_requests").document(requestId).set(verificationRequest).await()
            verificationDao.insertVerificationRequest(verificationRequest)
            
            Result.success(requestId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateVerificationStatus(
        requestId: String,
        status: VerificationStatus,
        adminNotes: String,
        reviewedBy: String
    ): Result<Unit> {
        return try {
            val request = getVerificationRequest(requestId)
            if (request == null) {
                return Result.failure(Exception("Verification request not found"))
            }
            
            val updatedRequest = request.copy(
                status = status,
                adminNotes = adminNotes,
                reviewedAt = System.currentTimeMillis(),
                reviewedBy = reviewedBy
            )
            
            firestore.collection("verification_requests").document(requestId).set(updatedRequest).await()
            verificationDao.updateVerificationRequest(updatedRequest)
            
            // If approved, update user verification status
            if (status == VerificationStatus.VERIFIED) {
                updateUserVerificationStatus(request.userId, request.verificationType)
            }
            
            // If rejected, refund coins
            if (status == VerificationStatus.REJECTED) {
                walletRepository.addCoins(
                    userId = request.userId,
                    amount = request.coinsDeducted,
                    description = "Verification rejected - refund",
                    relatedEntityId = requestId,
                    relatedEntityType = "verification_refund"
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun updateUserVerificationStatus(userId: String, verificationType: VerificationType) {
        try {
            val userRef = firestore.collection("users").document(userId)
            val userSnapshot = userRef.get().await()
            val user = userSnapshot.toObject(User::class.java)
            
            if (user != null) {
                val updatedBadges = user.verificationBadges.toMutableList()
                val badgeType = when (verificationType) {
                    VerificationType.USER -> "verified"
                    VerificationType.BREEDER -> "breeder"
                    VerificationType.FARM -> "farm"
                    VerificationType.FOWL -> "fowl_verified"
                }
                
                if (!updatedBadges.contains(badgeType)) {
                    updatedBadges.add(badgeType)
                }
                
                val updatedUser = user.copy(
                    verificationStatus = VerificationStatus.VERIFIED,
                    verificationBadges = updatedBadges,
                    updatedAt = System.currentTimeMillis()
                )
                
                userRef.set(updatedUser).await()
            }
        } catch (e: Exception) {
            // Log error but don't fail the verification update
        }
    }
    
    suspend fun getVerificationRequest(requestId: String): VerificationRequest? {
        return try {
            val snapshot = firestore.collection("verification_requests").document(requestId).get().await()
            snapshot.toObject(VerificationRequest::class.java)
        } catch (e: Exception) {
            verificationDao.getVerificationRequest(requestId)
        }
    }
    
    fun getUserVerificationRequests(userId: String): Flow<List<VerificationRequest>> = flow {
        try {
            val snapshot = firestore.collection("verification_requests")
                .whereEqualTo("userId", userId)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val requests = snapshot.documents.mapNotNull { it.toObject(VerificationRequest::class.java) }
            requests.forEach { verificationDao.insertVerificationRequest(it) }
            emit(requests)
        } catch (e: Exception) {
            verificationDao.getUserVerificationRequests(userId).collect { emit(it) }
        }
    }
    
    fun getPendingVerificationRequests(): Flow<List<VerificationRequest>> = flow {
        try {
            val snapshot = firestore.collection("verification_requests")
                .whereEqualTo("status", VerificationStatus.PENDING.name)
                .orderBy("submittedAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            
            val requests = snapshot.documents.mapNotNull { it.toObject(VerificationRequest::class.java) }
            requests.forEach { verificationDao.insertVerificationRequest(it) }
            emit(requests)
        } catch (e: Exception) {
            verificationDao.getVerificationRequestsByStatus(VerificationStatus.PENDING).collect { emit(it) }
        }
    }
    
    suspend fun canRequestVerification(userId: String, verificationType: VerificationType): Boolean {
        return try {
            val pendingCount = verificationDao.getPendingRequestsCount(userId)
            val latestApproved = verificationDao.getLatestApprovedRequest(userId, verificationType)
            
            // Allow if no pending requests and no approved request of same type
            pendingCount == 0 && latestApproved == null
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getVerificationCost(verificationType: VerificationType): Int {
        val coinPricing = walletRepository.getCoinPricing()
        return coinPricing.verificationFee
    }
}