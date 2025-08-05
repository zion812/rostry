package com.rio.rostry.data.common

/**
 * Enhanced Result wrapper for better error handling and state management
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable, val message: String? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }
    
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): NetworkResult<T> {
        if (this is Error) action(exception)
        return this
    }
    
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * Extension functions for common operations
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(exception, message)
        is NetworkResult.Loading -> NetworkResult.Loading
    }
}

inline fun <T> NetworkResult<T>.fold(
    onSuccess: (T) -> Unit,
    onError: (Throwable) -> Unit,
    onLoading: () -> Unit = {}
) {
    when (this) {
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.Error -> onError(exception)
        is NetworkResult.Loading -> onLoading()
    }
}

/**
 * Safe API call wrapper with automatic error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: Exception) {
        when (e) {
            is java.net.UnknownHostException -> 
                NetworkResult.Error(e, "No internet connection")
            is java.net.SocketTimeoutException -> 
                NetworkResult.Error(e, "Request timeout")
            is com.google.firebase.firestore.FirebaseFirestoreException -> 
                NetworkResult.Error(e, "Database error: ${e.message}")
            else -> NetworkResult.Error(e, "Unknown error occurred")
        }
    }
}
