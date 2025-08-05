package com.rio.rostry.utils

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.rio.rostry.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

/**
 * Centralized error handling system
 */
object ErrorHandler {
    
    /**
     * Convert exceptions to user-friendly messages
     */
    fun getErrorMessage(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthException -> getAuthErrorMessage(context, throwable)
            is FirebaseFirestoreException -> getFirestoreErrorMessage(context, throwable)
            is UnknownHostException -> context.getString(R.string.error_no_internet)
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            is SecurityException -> context.getString(R.string.error_permission_denied)
            is IllegalArgumentException -> context.getString(R.string.error_invalid_input)
            else -> throwable.message ?: context.getString(R.string.error_unknown)
        }
    }
    
    private fun getAuthErrorMessage(context: Context, exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> context.getString(R.string.error_invalid_email)
            "ERROR_WRONG_PASSWORD" -> context.getString(R.string.error_wrong_password)
            "ERROR_USER_NOT_FOUND" -> context.getString(R.string.error_user_not_found)
            "ERROR_USER_DISABLED" -> context.getString(R.string.error_user_disabled)
            "ERROR_TOO_MANY_REQUESTS" -> context.getString(R.string.error_too_many_requests)
            "ERROR_EMAIL_ALREADY_IN_USE" -> context.getString(R.string.error_email_in_use)
            "ERROR_WEAK_PASSWORD" -> context.getString(R.string.error_weak_password)
            else -> exception.message ?: context.getString(R.string.error_auth_failed)
        }
    }
    
    private fun getFirestoreErrorMessage(context: Context, exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                context.getString(R.string.error_permission_denied)
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                context.getString(R.string.error_service_unavailable)
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                context.getString(R.string.error_timeout)
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                context.getString(R.string.error_quota_exceeded)
            else -> exception.message ?: context.getString(R.string.error_database_failed)
        }
    }
    
    /**
     * Handle error with appropriate action
     */
    fun handleError(
        context: Context,
        throwable: Throwable,
        onRetry: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null
    ): ErrorAction {
        val message = getErrorMessage(context, throwable)
        
        return when (throwable) {
            is UnknownHostException, is SocketTimeoutException -> {
                ErrorAction.Retry(message, onRetry)
            }
            is FirebaseAuthException -> {
                if (throwable.errorCode == "ERROR_TOO_MANY_REQUESTS") {
                    ErrorAction.Wait(message, 60) // Wait 60 seconds
                } else {
                    ErrorAction.Dismiss(message, onDismiss)
                }
            }
            else -> ErrorAction.Dismiss(message, onDismiss)
        }
    }
}

/**
 * Error action types
 */
sealed class ErrorAction {
    data class Retry(val message: String, val onRetry: (() -> Unit)?) : ErrorAction()
    data class Dismiss(val message: String, val onDismiss: (() -> Unit)?) : ErrorAction()
    data class Wait(val message: String, val waitSeconds: Int) : ErrorAction()
}

/**
 * Composable error handler with snackbar
 */
@Composable
fun ErrorSnackbar(
    error: Throwable?,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(error) {
        error?.let { throwable ->
            val errorAction = ErrorHandler.handleError(
                context = context,
                throwable = throwable,
                onRetry = onRetry,
                onDismiss = onDismiss
            )
            
            when (errorAction) {
                is ErrorAction.Retry -> {
                    val result = snackbarHostState.showSnackbar(
                        message = errorAction.message,
                        actionLabel = context.getString(R.string.action_retry)
                    )
                    
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        errorAction.onRetry?.invoke()
                    }
                }
                
                is ErrorAction.Dismiss -> {
                    snackbarHostState.showSnackbar(errorAction.message)
                    errorAction.onDismiss?.invoke()
                }
                
                is ErrorAction.Wait -> {
                    snackbarHostState.showSnackbar(
                        message = "${errorAction.message}. Please wait ${errorAction.waitSeconds} seconds."
                    )
                }
            }
        }
    }
}

/**
 * Global error boundary for unhandled exceptions
 */
class GlobalErrorHandler(
    private val context: Context,
    private val onError: (String) -> Unit
) : Thread.UncaughtExceptionHandler {
    
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            val errorMessage = ErrorHandler.getErrorMessage(context, exception)
            onError(errorMessage)
            
            // Log to crash reporting
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(exception)
                
        } catch (e: Exception) {
            // If error handling fails, use default handler
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}

/**
 * Retry mechanism with exponential backoff
 */
class RetryMechanism {
    suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        factor: Double = 2.0,
        operation: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
                }
            }
        }
        
        throw lastException ?: Exception("Retry failed")
    }
}
