package com.rio.rostry.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.rio.rostry.data.manager.SessionManager
import com.rio.rostry.data.model.role.Permission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationAnalytics @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val sessionManager: SessionManager
) {

    private val analyticsScope = CoroutineScope(Dispatchers.IO)

    /**
     * Track navigation events
     */
    fun trackNavigation(
        from: String,
        to: String,
        method: NavigationMethod = NavigationMethod.TAP
    ) {
        analyticsScope.launch {
            val session = sessionManager.userSession.value

            firebaseAnalytics.logEvent("navigation") {
                param("from_screen", from)
                param("to_screen", to)
                param("method", method.name)
                param("user_role", session?.organizationRoleId ?: "user" ?: "unknown")
                param("organization_id", session?.activeOrganizationId ?: "none")
                param("timestamp", System.currentTimeMillis())
            }
        }
    }

    /**
     * Track permission checks
     */
    fun trackPermissionCheck(
        permission: Permission,
        granted: Boolean,
        context: String
    ) {
        analyticsScope.launch {
            val session = sessionManager.userSession.value

            firebaseAnalytics.logEvent("permission_check") {
                param("permission", permission.id)
                param("granted", if (granted) 1L else 0L)
                param("context", context)
                param("user_role", session?.organizationRoleId ?: "user" ?: "unknown")
                param("role_level", getRoleLevelFromId(session?.organizationRoleId ?: "user"))
            }
        }
    }

    /**
     * Track role upgrades
     */
    fun trackRoleUpgrade(
        fromRoleId: String,
        toRoleId: String,
        method: UpgradeMethod
    ) {
        analyticsScope.launch {
            firebaseAnalytics.logEvent("role_upgrade") {
                param("from_role", fromRoleId)
                param("to_role", toRoleId)
                param("from_level", getRoleLevelFromId(fromRoleId))
                param("to_level", getRoleLevelFromId(toRoleId))
                param("method", method.name)
                param("timestamp", System.currentTimeMillis())
            }
        }
    }

    /**
     * Track feature usage
     */
    fun trackFeatureUsage(
        feature: String,
        action: String,
        success: Boolean = true
    ) {
        analyticsScope.launch {
            val session = sessionManager.userSession.value

            firebaseAnalytics.logEvent("feature_usage") {
                param("feature", feature)
                param("action", action)
                param("success", if (success) 1L else 0L)
                param("user_role", session?.organizationRoleId ?: "user" ?: "unknown")
                param("session_duration", getSessionDuration())
            }
        }
    }

    /**
     * Track navigation performance
     */
    fun trackNavigationPerformance(
        screen: String,
        loadTime: Long,
        cacheHit: Boolean = false
    ) {
        analyticsScope.launch {
            firebaseAnalytics.logEvent("navigation_performance") {
                param("screen", screen)
                param("load_time_ms", loadTime)
                param("cache_hit", if (cacheHit) 1L else 0L)
                param("timestamp", System.currentTimeMillis())
            }
        }
    }

    /**
     * Track errors
     */
    fun trackNavigationError(
        error: String,
        context: String,
        recoverable: Boolean = true
    ) {
        analyticsScope.launch {
            val session = sessionManager.userSession.value

            firebaseAnalytics.logEvent("navigation_error") {
                param("error", error)
                param("context", context)
                param("recoverable", if (recoverable) 1L else 0L)
                param("user_role", session?.organizationRoleId ?: "user" ?: "unknown")
                param("app_version", getAppVersion())
            }
        }
    }

    private fun getSessionDuration(): Long {
        val session = sessionManager.userSession.value
        return if (session != null) {
            System.currentTimeMillis() - session.createdAt
        } else 0L
    }

    private fun getAppVersion(): String {
        // Return app version
        return "1.0.0" // This should come from BuildConfig
    }

    private fun getRoleLevelFromId(roleId: String?): Long {
        return when (roleId) {
            "consumer" -> 1L
            "basic_farmer" -> 2L
            "verified_farmer" -> 3L
            "premium_breeder" -> 4L
            "farm_manager" -> 5L
            "moderator" -> 6L
            "super_admin" -> 7L
            else -> 0L
        }
    }

    enum class NavigationMethod {
        TAP, SWIPE, BACK_BUTTON, DEEP_LINK, PROGRAMMATIC
    }

    enum class UpgradeMethod {
        IN_APP_PURCHASE, VERIFICATION, ADMIN_GRANT, AUTOMATIC
    }
}