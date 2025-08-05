package com.rio.rostry.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import com.rio.rostry.data.local.dao.*
import com.rio.rostry.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified Firebase sync manager for ROSTRY application.
 * 
 * This class provides basic synchronization between local Room database
 * and Firebase Firestore with proper error handling and state management.
 * 
 * Features:
 * - Real-time data synchronization
 * - Offline-first architecture support
 * - Conflict resolution with last-write-wins strategy
 * - Comprehensive error handling and logging
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
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    /**
     * Start comprehensive real-time synchronization for user data.
     * 
     * This method establishes real-time listeners for:
     * - User's fowl data
     * - Farm information
     * - Lineage tracking data
     * 
     * @param userId The ID of the user to sync data for
     */
    fun startSync(userId: String) {
        if (userId.isBlank()) {
            _syncStatus.value = SyncStatus.ERROR("Invalid user ID")
            return
        }
        
        syncScope.launch {
            try {
                _syncStatus.value = SyncStatus.SYNCING
                
                // Start real-time listeners for comprehensive sync
                startFowlSync(userId)
                startFarmSync(userId)
                startLineageSync(userId)
                
                // Perform initial data sync
                performInitialSync(userId)
                
                _syncStatus.value = SyncStatus.SYNCED
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.ERROR(e.message ?: "Sync failed")
            }
        }
    }
    
    /**
     * Stop all synchronization and clean up resources.
     */
    fun stopSync() {
        listeners.forEach { it.remove() }
        listeners.clear()
        _syncStatus.value = SyncStatus.IDLE
    }
    
    /**
     * Perform manual synchronization with error handling.
     * 
     * @param userId The ID of the user to sync data for
     * @return Result indicating success or failure
     */
    suspend fun performManualSync(userId: String): Result<Unit> {
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            
            // Perform basic sync operations
            performBasicSync(userId)
            
            _syncStatus.value = SyncStatus.SYNCED
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR(e.message ?: "Manual sync failed")
            Result.failure(e)
        }
    }
    
    /**
     * Perform basic synchronization operations.
     * 
     * @param userId The ID of the user to sync data for
     */
    private suspend fun performBasicSync(userId: String) {
        try {
            // Basic fowl synchronization
            syncFowlsBasic(userId)
            
            // Basic farm synchronization  
            syncFarmsBasic(userId)
            
            // Basic lineage synchronization
            syncLineageBasic(userId)
            
        } catch (e: Exception) {
            throw Exception("Basic sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Basic fowl synchronization without complex conflict resolution.
     */
    private suspend fun syncFowlsBasic(userId: String) {
        try {
            // Get remote fowls
            val remoteFowls = firestore.collection("fowls")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(Fowl::class.java)
                    } catch (e: Exception) {
                        null // Skip invalid documents
                    }
                }
            
            // Update local database
            remoteFowls.forEach { fowl ->
                fowlDao.insertFowl(fowl)
            }
            
        } catch (e: Exception) {
            throw Exception("Fowl sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Basic farm synchronization without complex conflict resolution.
     */
    private suspend fun syncFarmsBasic(userId: String) {
        try {
            // Get remote farms
            val remoteFarms = firestore.collection("farms")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(Farm::class.java)
                    } catch (e: Exception) {
                        null // Skip invalid documents
                    }
                }
            
            // Update local database
            remoteFarms.forEach { farm ->
                farmDao.insertFarm(farm)
            }
            
        } catch (e: Exception) {
            throw Exception("Farm sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Basic lineage synchronization without complex conflict resolution.
     */
    private suspend fun syncLineageBasic(userId: String) {
        try {
            // Get remote lineages
            val remoteLineages = firestore.collection("fowl_lineages")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(FowlLineage::class.java)
                    } catch (e: Exception) {
                        null // Skip invalid documents
                    }
                }
            
            // Update local database
            remoteLineages.forEach { lineage ->
                lineageDao.insertLineage(lineage)
            }
            
        } catch (e: Exception) {
            throw Exception("Lineage sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Get current sync status.
     * 
     * @return Current synchronization status
     */
    fun getCurrentSyncStatus(): SyncStatus = _syncStatus.value
    
    /**
     * Check if sync is currently active.
     * 
     * @return True if sync is in progress, false otherwise
     */
    fun isSyncing(): Boolean = _syncStatus.value is SyncStatus.SYNCING
    
    // ==================== REAL-TIME SYNC METHODS ====================
    
    /**
     * Start real-time fowl synchronization with Firestore listeners.
     * 
     * @param userId The ID of the user whose fowls to sync
     */
    private fun startFowlSync(userId: String) {
        val listener = firestore.collection("fowls")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR("Fowl sync error: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        try {
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
                        } catch (e: Exception) {
                            _syncStatus.value = SyncStatus.ERROR("Fowl processing error: ${e.message}")
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    /**
     * Start real-time farm synchronization with Firestore listeners.
     * 
     * @param userId The ID of the user whose farms to sync
     */
    private fun startFarmSync(userId: String) {
        val listener = firestore.collection("farms")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR("Farm sync error: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        try {
                            querySnapshot.documentChanges.forEach { change ->
                                val farm = change.document.toObject(Farm::class.java)
                                
                                when (change.type) {
                                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                        farmDao.insertFarm(farm)
                                    }
                                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                        farmDao.deleteFarm(farm)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            _syncStatus.value = SyncStatus.ERROR("Farm processing error: ${e.message}")
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    /**
     * Start real-time lineage synchronization with Firestore listeners.
     * 
     * @param userId The ID of the user (lineage data is global but filtered by access)
     */
    private fun startLineageSync(userId: String) {
        val listener = firestore.collection("fowl_lineages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.ERROR("Lineage sync error: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    syncScope.launch {
                        try {
                            querySnapshot.documentChanges.forEach { change ->
                                val lineage = change.document.toObject(FowlLineage::class.java)
                                
                                when (change.type) {
                                    com.google.firebase.firestore.DocumentChange.Type.ADDED,
                                    com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                        lineageDao.insertLineage(lineage)
                                    }
                                    com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                        lineageDao.deleteLineage(lineage)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            _syncStatus.value = SyncStatus.ERROR("Lineage processing error: ${e.message}")
                        }
                    }
                }
            }
        
        listeners.add(listener)
    }
    
    /**
     * Perform initial comprehensive data synchronization.
     * 
     * This method ensures all user data is up-to-date when sync starts.
     * 
     * @param userId The ID of the user to sync data for
     */
    private suspend fun performInitialSync(userId: String) {
        try {
            // Perform comprehensive sync with conflict resolution
            syncFowlsWithConflictResolution(userId)
            syncFarmsWithConflictResolution(userId)
            syncLineageWithConflictResolution(userId)
            
        } catch (e: Exception) {
            throw Exception("Initial sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Advanced fowl synchronization with conflict resolution.
     * 
     * Uses last-write-wins strategy based on updatedAt timestamp.
     * 
     * @param userId The ID of the user whose fowls to sync
     */
    private suspend fun syncFowlsWithConflictResolution(userId: String) {
        try {
            // Get local fowls
            val localFowls = fowlDao.getFowlsByOwnerSync(userId)
            
            // Get remote fowls
            val remoteFowls = firestore.collection("fowls")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(Fowl::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            
            // Resolve conflicts using last-write-wins strategy
            val resolvedFowls = resolveConflicts(localFowls, remoteFowls) { local, remote ->
                if (local.updatedAt > remote.updatedAt) local else remote
            }
            
            // Update local database
            resolvedFowls.forEach { fowl ->
                fowlDao.insertFowl(fowl)
            }
            
            // Update remote database for any local changes that won
            val localWins = resolvedFowls.filter { resolved ->
                localFowls.any { local: Fowl -> 
                    local.id == resolved.id && local.updatedAt >= resolved.updatedAt 
                }
            }
            
            localWins.forEach { fowl ->
                firestore.collection("fowls").document(fowl.id).set(fowl).await()
            }
            
        } catch (e: Exception) {
            throw Exception("Fowl conflict resolution failed: ${e.message}", e)
        }
    }
    
    /**
     * Advanced farm synchronization with conflict resolution.
     * 
     * @param userId The ID of the user whose farms to sync
     */
    private suspend fun syncFarmsWithConflictResolution(userId: String) {
        try {
            // Get local farms
            val localFarms = farmDao.getUserFarms(userId).first()
            
            // Get remote farms
            val remoteFarms = firestore.collection("farms")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(Farm::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            
            // Resolve conflicts
            val resolvedFarms = resolveConflicts(localFarms, remoteFarms) { local, remote ->
                if (local.updatedAt > remote.updatedAt) local else remote
            }
            
            // Update local database
            resolvedFarms.forEach { farm ->
                farmDao.insertFarm(farm)
            }
            
            // Update remote for local wins
            val localWins = resolvedFarms.filter { resolved ->
                localFarms.any { local -> 
                    local.id == resolved.id && local.updatedAt >= resolved.updatedAt 
                }
            }
            
            localWins.forEach { farm ->
                firestore.collection("farms").document(farm.id).set(farm).await()
            }
            
        } catch (e: Exception) {
            throw Exception("Farm conflict resolution failed: ${e.message}", e)
        }
    }
    
    /**
     * Advanced lineage synchronization with conflict resolution.
     * 
     * @param userId The ID of the user (for access control)
     */
    private suspend fun syncLineageWithConflictResolution(userId: String) {
        try {
            // Get local lineages
            val localLineages = lineageDao.getAllLineages().first()
            
            // Get remote lineages
            val remoteLineages = firestore.collection("fowl_lineages")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    try {
                        document.toObject(FowlLineage::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            
            // Resolve conflicts
            val resolvedLineages = resolveConflicts(localLineages, remoteLineages) { local, remote ->
                if (local.updatedAt > remote.updatedAt) local else remote
            }
            
            // Update local database
            resolvedLineages.forEach { lineage ->
                lineageDao.insertLineage(lineage)
            }
            
            // Update remote for local wins
            val localWins = resolvedLineages.filter { resolved ->
                localLineages.any { local -> 
                    local.id == resolved.id && local.updatedAt >= resolved.updatedAt 
                }
            }
            
            localWins.forEach { lineage ->
                firestore.collection("fowl_lineages").document(lineage.id).set(lineage).await()
            }
            
        } catch (e: Exception) {
            throw Exception("Lineage conflict resolution failed: ${e.message}", e)
        }
    }
    
    /**
     * Generic conflict resolution using a resolver function.
     * 
     * @param local List of local entities
     * @param remote List of remote entities
     * @param resolver Function to resolve conflicts between local and remote entities
     * @return List of resolved entities
     */
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
    
    /**
     * Extract entity ID for conflict resolution.
     * 
     * @param entity The entity to extract ID from
     * @return The entity's unique identifier
     */
    private fun <T> getEntityId(entity: T): String {
        return when (entity) {
            is Fowl -> entity.id
            is Farm -> entity.id
            is FowlLineage -> entity.id
            else -> throw IllegalArgumentException("Unknown entity type: ${entity!!::class.simpleName}")
        }
    }
}

/**
 * Synchronization status sealed class for type-safe state management.
 */
sealed class SyncStatus {
    data object IDLE : SyncStatus()
    data object SYNCING : SyncStatus()
    data object SYNCED : SyncStatus()
    data class ERROR(val message: String) : SyncStatus()
}