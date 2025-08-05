package com.rio.rostry.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.rio.rostry.data.local.dao.*
import com.rio.rostry.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Firebase sync manager with conflict resolution and offline support
 */
@Singleton
class FirebaseSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlDao: FowlDao,
    private val farmDao: FarmDao,
    private val lineageDao: LineageDao
) {
    
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = mutableListOf<ListenerRegistration>()
    
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    /**
     * Start real-time synchronization
     */
    fun startSync(userId: String) {
        syncScope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            
            try {
                // Start real-time listeners
                startFowlSync(userId)
                startFarmSync(userId)
                startLineageSync(userId)
                
                _syncStatus.value = SyncStatus.SYNCED
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.message ?: "Sync failed")
            }
        }
    }
    
    /**
     * Stop all synchronization
     */
    fun stopSync() {
        listeners.forEach { it.remove() }
        listeners.clear()
        syncScope.cancel()
        _syncStatus.value = SyncStatus.IDLE
    }
    
    /**
     * Manual sync with conflict resolution
     */
    suspend fun performManualSync(userId: String): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            // Sync fowls
            syncFowlsWithConflictResolution(userId)
            
            // Sync farms
            syncFarmsWithConflictResolution(userId)
            
            // Sync lineage data
            syncLineageWithConflictResolution(userId)
            
            _syncStatus.value = SyncStatus.SYNCED
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Manual sync failed")
            Result.failure(e)
        }
    }
    
    private fun startFowlSync(userId: String) {
        val listener = firestore.collection("fowls")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR(error.message ?: "Fowl sync error")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        querySnapshot.documentChanges.forEach { change ->
                            val fowl = change.document.toObject(Fowl::class.java)
                            
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    fowlDao.insertFowl(fowl)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    fowlDao.deleteFowlById(fowl.id)
                                }
                            }
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    private fun startFarmSync(userId: String) {
        val listener = firestore.collection("farms")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR(error.message ?: "Farm sync error")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        querySnapshot.documentChanges.forEach { change ->
                            val farm = change.document.toObject(Farm::class.java)
                            
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    farmDao.insertFarm(farm)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    farmDao.deleteFarm(farm.id)
                                }
                            }
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    private fun startLineageSync(userId: String) {
        val listener = firestore.collection("fowl_lineages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR(error.message ?: "Lineage sync error")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        querySnapshot.documentChanges.forEach { change ->
                            val lineage = change.document.toObject(FowlLineage::class.java)
                            
                            when (change.type) {
                                com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                    lineageDao.insertLineage(lineage)
                                }
                                com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                    lineageDao.deleteLineage(lineage.id)
                                }
                            }
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    private suspend fun syncFowlsWithConflictResolution(userId: String) {
        // Get local fowls
        val localFowls = fowlDao.getUserFowlsSync(userId)
        
        // Get remote fowls
        val remoteFowls = firestore.collection("fowls")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Fowl::class.java) }
        
        // Resolve conflicts (last write wins with timestamp comparison)
        val resolvedFowls = resolveConflicts(localFowls, remoteFowls) { local, remote ->
            if (local.updatedAt > remote.updatedAt) local else remote
        }
        
        // Update both local and remote
        resolvedFowls.forEach { fowl ->
            fowlDao.insertFowl(fowl)
            firestore.collection("fowls").document(fowl.id).set(fowl).await()
        }
    }
    
    private suspend fun syncFarmsWithConflictResolution(userId: String) {
        // Similar implementation for farms
        val localFarms = farmDao.getUserFarmsSync(userId)
        val remoteFarms = firestore.collection("farms")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Farm::class.java) }
        
        val resolvedFarms = resolveConflicts(localFarms, remoteFarms) { local, remote ->
            if (local.updatedAt > remote.updatedAt) local else remote
        }
        
        resolvedFarms.forEach { farm ->
            farmDao.insertFarm(farm)
            firestore.collection("farms").document(farm.id).set(farm).await()
        }
    }
    
    private suspend fun syncLineageWithConflictResolution(userId: String) {
        // Similar implementation for lineage data
        val localLineages = lineageDao.getAllLineagesSync()
        val remoteLineages = firestore.collection("fowl_lineages")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(FowlLineage::class.java) }
        
        val resolvedLineages = resolveConflicts(localLineages, remoteLineages) { local, remote ->
            if (local.updatedAt > remote.updatedAt) local else remote
        }
        
        resolvedLineages.forEach { lineage ->
            lineageDao.insertLineage(lineage)
            firestore.collection("fowl_lineages").document(lineage.id).set(lineage).await()
        }
    }
    
    private fun <T> resolveConflicts(
        local: List<T>,
        remote: List<T>,
        resolver: (T, T) -> T
    ): List<T> where T : Any {
        val localMap = local.associateBy { getEntityId(it) }
        val remoteMap = remote.associateBy { getEntityId(it) }
        val allIds = (localMap.keys + remoteMap.keys).distinct()
        
        return allIds.mapNotNull { id ->
            val localEntity = localMap[id]
            val remoteEntity = remoteMap[id]
            
            when {
                localEntity != null && remoteEntity != null -> resolver(localEntity, remoteEntity)
                localEntity != null -> localEntity
                remoteEntity != null -> remoteEntity
                else -> null
            }
        }
    }
    
    private fun <T> getEntityId(entity: T): String {
        return when (entity) {
            is Fowl -> entity.id
            is Farm -> entity.id
            is FowlLineage -> entity.id
            else -> throw IllegalArgumentException("Unknown entity type")
        }
    }
}

sealed class SyncStatus {
    object IDLE : SyncStatus()
    object SYNCING : SyncStatus()
    object SYNCED : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}
