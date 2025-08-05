package com.rio.rostry.data.error

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationErrorHandler @Inject constructor() {

    private val _errors = MutableSharedFlow<NavigationError>()
    val errors: SharedFlow<NavigationError> = _errors

    /**
     * Navigation error types
     */
    sealed class NavigationError(
        val message: String,
        val action: ErrorAction? = null,
        val severity: ErrorSeverity = ErrorSeverity.MEDIUM
    ) {
        data class SessionExpired(val redirectTo: String = "login") : NavigationError(
            message = "Your session has expired. Please log in again.",
            action = ErrorAction.Redirect(redirectTo),
            severity = ErrorSeverity.HIGH
        )

        data class PermissionDenied(val requiredPermission: String) : NavigationError(
            message = "You don't have permission to access this feature.",
            action = ErrorAction.ShowUpgrade,
            severity = ErrorSeverity.MEDIUM
        )

        data class RoleTransitionFailed(val targetRole: String, val reason: String) : NavigationError(
            message = "Failed to upgrade to $targetRole: $reason",
            action = ErrorAction.Retry,
            severity = ErrorSeverity.HIGH
        )

        data class NetworkError(val operation: String) : NavigationError(
            message = "Network error during $operation. Using offline mode.",
            action = ErrorAction.EnableOffline,
            severity = ErrorSeverity.LOW
        )

        data class OrganizationSwitchFailed(val organizationName: String) : NavigationError(
            message = "Failed to switch to $organizationName",
            action = ErrorAction.Retry,
            severity = ErrorSeverity.MEDIUM
        )

        data class NavigationFailed(val destination: String) : NavigationError(
            message = "Failed to navigate to $destination",
            action = ErrorAction.GoHome,
            severity = ErrorSeverity.MEDIUM
        )
    }

    /**
     * Error actions
     */
    sealed class ErrorAction {
        data class Redirect(val destination: String) : ErrorAction()
        object Retry : ErrorAction()
        object ShowUpgrade : ErrorAction()
        object EnableOffline : ErrorAction()
        object GoHome : ErrorAction()
        object Dismiss : ErrorAction()
    }

    enum class ErrorSeverity { LOW, MEDIUM, HIGH, CRITICAL }

    /**
     * Handle navigation error
     */
    suspend fun handleError(error: NavigationError) {
        _errors.emit(error)
    }

    /**
     * Handle session expiration
     */
    suspend fun handleSessionExpired() {
        handleError(NavigationError.SessionExpired())
    }

    /**
     * Handle permission denied
     */
    suspend fun handlePermissionDenied(permission: String) {
        handleError(NavigationError.PermissionDenied(permission))
    }

    /**
     * Handle role transition failure
     */
    suspend fun handleRoleTransitionFailed(targetRole: String, reason: String) {
        handleError(NavigationError.RoleTransitionFailed(targetRole, reason))
    }

    /**
     * Handle network error
     */
    suspend fun handleNetworkError(operation: String) {
        handleError(NavigationError.NetworkError(operation))
    }
}

/**
 * Composable for handling navigation errors
 */
@Composable
fun NavigationErrorHandler(
    errorHandler: NavigationErrorHandler,
    snackbarHostState: SnackbarHostState,
    onNavigate: (String) -> Unit,
    onRetry: () -> Unit = {},
    onShowUpgrade: () -> Unit = {},
    onEnableOffline: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(errorHandler) {
        errorHandler.errors.collect { error ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = error.message,
                    actionLabel = when (error.action) {
                        is NavigationErrorHandler.ErrorAction.Retry -> "Retry"
                        is NavigationErrorHandler.ErrorAction.ShowUpgrade -> "Upgrade"
                        is NavigationErrorHandler.ErrorAction.EnableOffline -> "Go Offline"
                        is NavigationErrorHandler.ErrorAction.GoHome -> "Go Home"
                        else -> null
                    },
                    withDismissAction = true
                )

                when (result) {
                    androidx.compose.material3.SnackbarResult.ActionPerformed -> {
                        when (val action = error.action) {
                            is NavigationErrorHandler.ErrorAction.Redirect -> onNavigate(action.destination)
                            is NavigationErrorHandler.ErrorAction.Retry -> onRetry()
                            is NavigationErrorHandler.ErrorAction.ShowUpgrade -> onShowUpgrade()
                            is NavigationErrorHandler.ErrorAction.EnableOffline -> onEnableOffline()
                            is NavigationErrorHandler.ErrorAction.GoHome -> onNavigate("home")
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}