package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.FowlDao
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlRecord
import com.rio.rostry.data.common.NetworkResult
import com.rio.rostry.data.common.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import java.util.UUID
import androidx.core.net.toUri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FowlRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val fowlDao: FowlDao
) : BaseRepository() {
    
    suspend fun addFowl(fowl: Fowl): Result<String> {
        return try {
            val fowlId = fowl.id.ifEmpty { UUID.randomUUID().toString() }
            val fowlWithId = fowl.copy(id = fowlId)
            
            firestore.collection("fowls").document(fowlId).set(fowlWithId).await()
            fowlDao.insertFowl(fowlWithId)
            Result.success(fowlId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateFowl(fowl: Fowl): Result<Unit> {
        return try {
            firestore.collection("fowls").document(fowl.id).set(fowl).await()
            fowlDao.updateFowl(fowl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteFowl(fowlId: String): Result<Unit> {
        return try {
            firestore.collection("fowls").document(fowlId).delete().await()
            fowlDao.deleteFowlById(fowlId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFowlsByOwnerFlow(ownerId: String): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
            fowls.forEach { fowlDao.insertFowl(it) }
            emit(fowls)
        } catch (e: Exception) {
            // Fallback to local data
            fowlDao.getFowlsByOwner(ownerId).collect { emit(it) }
        }
    }
    
    fun getMarketplaceFowls(): Flow<List<Fowl>> = flow {
        try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("isForSale", true)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
            fowls.forEach { fowlDao.insertFowl(it) }
            emit(fowls)
        } catch (e: Exception) {
            // Fallback to local data
            fowlDao.getMarketplaceFowls().collect { emit(it) }
        }
    }
    
    fun getFowlByIdFlow(fowlId: String): Flow<Fowl?> = flow {
        try {
            val snapshot = firestore.collection("fowls").document(fowlId).get().await()
            val fowl = snapshot.toObject(Fowl::class.java)
            fowl?.let { 
                fowlDao.insertFowl(it)
                emit(it)
            } ?: emit(null)
        } catch (e: Exception) {
            // Fallback to local data
            val localFowl = fowlDao.getFowlById(fowlId)
            emit(localFowl)
        }
    }
    
    suspend fun getFowlById(fowlId: String): Fowl? {
        return try {
            val snapshot = firestore.collection("fowls").document(fowlId).get().await()
            snapshot.toObject(Fowl::class.java)
        } catch (e: Exception) {
            fowlDao.getFowlById(fowlId)
        }
    }
    
    suspend fun getUserFowls(userId: String): List<Fowl> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
        } catch (e: Exception) {
            fowlDao.getFowlsByOwnerSync(userId)
        }
    }
    
    fun searchFowls(query: String): Flow<List<Fowl>> {
        return fowlDao.searchMarketplaceFowls("%$query%")
    }
    
    suspend fun uploadFowlImage(imageUri: String, fowlId: String): Result<String> {
        return try {
            val imageRef = storage.reference.child("fowl_images/$fowlId/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(imageUri.toUri()).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Fowl Record Management
    suspend fun addFowlWithRecord(fowl: Fowl, initialRecord: FowlRecord): Result<String> {
        return try {
            val fowlId = fowl.id.ifEmpty { UUID.randomUUID().toString() }
            val recordId = initialRecord.recordId.ifEmpty { UUID.randomUUID().toString() }
            
            val fowlWithId = fowl.copy(id = fowlId)
            val recordWithIds = initialRecord.copy(recordId = recordId, fowlId = fowlId)
            
            // Add fowl and initial record in a batch
            firestore.collection("fowls").document(fowlId).set(fowlWithId).await()
            firestore.collection("fowl_records").document(recordId).set(recordWithIds).await()
            
            fowlDao.insertFowl(fowlWithId)
            Result.success(fowlId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addFowlRecord(record: FowlRecord): Result<String> {
        return try {
            val recordId = record.recordId.ifEmpty { UUID.randomUUID().toString() }
            val recordWithId = record.copy(recordId = recordId)
            
            firestore.collection("fowl_records").document(recordId).set(recordWithId).await()
            Result.success(recordId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFowlRecords(fowlId: String): Flow<List<FowlRecord>> = flow {
        try {
            val snapshot = firestore.collection("fowl_records")
                .whereEqualTo("fowlId", fowlId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val records = snapshot.documents.mapNotNull { it.toObject(FowlRecord::class.java) }
            emit(records)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun updateFowlRecord(record: FowlRecord): Result<Unit> {
        return try {
            firestore.collection("fowl_records").document(record.recordId).set(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteFowlRecord(recordId: String): Result<Unit> {
        return try {
            firestore.collection("fowl_records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadProofImage(imageUri: android.net.Uri, fowlId: String): Result<String> {
        return try {
            val imageRef = storage.reference.child("fowl_proofs/$fowlId/${System.currentTimeMillis()}.jpg")
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Additional methods needed by DashboardRepository
    suspend fun getFowlsByOwner(ownerId: String): List<Fowl> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            val fowls = snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
            fowls.forEach { fowlDao.insertFowl(it) }
            fowls
        } catch (e: Exception) {
            fowlDao.getFowlsByOwnerSync(ownerId)
        }
    }
    
    suspend fun getRecentFowls(ownerId: String, limit: Int): List<Fowl> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
        } catch (e: Exception) {
            fowlDao.getRecentFowls(ownerId, limit)
        }
    }
    
    suspend fun getActiveSales(ownerId: String): List<Fowl> {
        return try {
            val snapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("isForSale", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { it.toObject(Fowl::class.java) }
        } catch (e: Exception) {
            fowlDao.getActiveSales(ownerId)
        }
    }
}