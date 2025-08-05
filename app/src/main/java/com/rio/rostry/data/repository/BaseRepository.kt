package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.common.NetworkResult
import com.rio.rostry.data.common.safeApiCall
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Base repository with common patterns for offline-first architecture
 */
abstract class BaseRepository {
    
    /**
     * Offline-first data fetching pattern
     * 1. Emit cached data immediately
     * 2. Fetch from network
     * 3. Update cache and emit fresh data
     */
    protected fun <T> offlineFirstFlow(
        fetchFromLocal: suspend () -> T?,
        fetchFromNetwork: suspend () -> T,
        saveToLocal: suspend (T) -> Unit
    ): Flow<NetworkResult<T>> = flow {
        // Emit loading state
        emit(NetworkResult.Loading)
        
        // Try to emit cached data first
        val localData = fetchFromLocal()
        if (localData != null) {
            emit(NetworkResult.Success(localData))
        }
        
        // Fetch from network
        val networkResult = safeApiCall { fetchFromNetwork() }
        
        networkResult.onSuccess { networkData ->
            // Save to local cache
            safeApiCall { saveToLocal(networkData) }
            // Emit fresh data
            emit(NetworkResult.Success(networkData))
        }.onError { error ->
            // If we have local data, don't emit error
            if (localData == null) {
                emit(NetworkResult.Error(error))
            }
        }
    }.catch { error ->
        emit(NetworkResult.Error(error as Throwable))
    }
    
    /**
     * Network-first data fetching pattern
     * For real-time data that should always be fresh
     */
    protected fun <T> networkFirstFlow(
        fetchFromNetwork: suspend () -> T,
        fetchFromLocal: suspend () -> T?,
        saveToLocal: suspend (T) -> Unit
    ): Flow<NetworkResult<T>> = flow {
        emit(NetworkResult.Loading)
        
        // Try network first
        val networkResult = safeApiCall { fetchFromNetwork() }
        
        networkResult.onSuccess { networkData ->
            safeApiCall { saveToLocal(networkData) }
            emit(NetworkResult.Success(networkData))
        }.onError { error ->
            // Fallback to local data
            val localData = fetchFromLocal()
            if (localData != null) {
                emit(NetworkResult.Success(localData))
            } else {
                emit(NetworkResult.Error(error))
            }
        }
    }.catch { error ->
        emit(NetworkResult.Error(error as Throwable))
    }
    
    /**
     * Batch operation with transaction support
     * Note: Firebase transactions require synchronous operations
     */
    protected suspend fun <T> batchOperation(
        operations: (FirebaseFirestore) -> T
    ): NetworkResult<T> {
        return safeApiCall {
            val firestore = FirebaseFirestore.getInstance()
            firestore.runTransaction { transaction ->
                operations(firestore)
            }.await()
        }
    }
    
    /**
     * Batch operation with async support using batched writes
     * Use this for operations that need suspend functions
     */
    protected suspend fun <T> batchOperationAsync(
        operations: suspend (FirebaseFirestore) -> T
    ): NetworkResult<T> {
        return safeApiCall {
            val firestore = FirebaseFirestore.getInstance()
            operations(firestore)
        }
    }
    
    /**
     * Retry mechanism for failed operations
     */
    protected suspend fun <T> retryOperation(
        maxRetries: Int = 3,
        delayMs: Long = 1000,
        operation: suspend () -> T
    ): NetworkResult<T> {
        repeat(maxRetries) { attempt ->
            val result = safeApiCall { operation() }
            if (result.isSuccess) return result
            
            if (attempt < maxRetries - 1) {
                delay(delayMs * (attempt + 1))
            }
        }
        
        return safeApiCall { operation() }
    }
}
