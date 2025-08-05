package com.rio.rostry.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.UserRole
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhanced navigation manager with role-based access control and deep linking
 */
@Stable
class EnhancedNavigationManager(
    private val navController: NavHostController,
    private val userStateFlow: StateFlow<User?>
) {
    
    // Remove Composable context - use direct state access instead
    private fun getCurrentUser(): User? = userStateFlow.value
    
    /**
     * Navigate with permission checking
     */
    fun navigateWithPermission(
        destination: String,
        requiredPermission: Permission? = null,
        onAccessDenied: () -> Unit = {}
    ) {
        if (requiredPermission != null && !hasPermission(requiredPermission)) {
            onAccessDenied()
            return
        }
        
        navController.navigate(destination)
    }
    
    /**
     * Navigate with role checking
     */
    fun navigateWithRole(
        destination: String,
        requiredRole: UserRole,
        onAccessDenied: () -> Unit = {}
    ) {
        if (!hasRole(requiredRole)) {
            onAccessDenied()
            return
        }
        
        navController.navigate(destination)
    }
    
    /**
     * Safe navigation with error handling
     */
    fun safeNavigate(
        destination: String,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            navController.navigate(destination)
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    /**
     * Navigate and clear back stack
     */
    fun navigateAndClearStack(destination: String) {
        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }
    
    /**
     * Navigate with single top behavior
     */
    fun navigateSingleTop(destination: String) {
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }
    
    /**
     * Deep link navigation with validation
     */
    fun handleDeepLink(
        deepLink: String,
        onInvalidLink: () -> Unit = {},
        onAccessDenied: () -> Unit = {}
    ) {
        val destination = parseDeepLink(deepLink)
        
        if (destination == null) {
            onInvalidLink()
            return
        }
        
        val requiredPermission = getRequiredPermissionForDestination(destination)
        if (requiredPermission != null && !hasPermission(requiredPermission)) {
            onAccessDenied()
            return
        }
        
        navController.navigate(destination)
    }
    
    /**
     * Get available destinations for current user
     */
    fun getAvailableDestinations(): List<NavigationDestination> {
        val user = getCurrentUser() ?: return emptyList()
        
        return NavigationDestination.values().filter { destination ->
            val requiredPermission = destination.requiredPermission
            requiredPermission == null || hasPermission(requiredPermission)
        }
    }
    
    /**
     * Check if user has specific permission
     */
    private fun hasPermission(permission: Permission): Boolean {
        val user = getCurrentUser() ?: return false
        return PermissionChecker.hasPermission(user, permission)
    }
    
    /**
     * Check if user has specific role
     */
    private fun hasRole(role: UserRole): Boolean {
        val user = getCurrentUser() ?: return false
        return user.role == role || isHigherRole(user.role, role)
    }
    
    /**
     * Check if user role is higher than required role
     */
    private fun isHigherRole(userRole: UserRole, requiredRole: UserRole): Boolean {
        val roleHierarchy = mapOf(
            UserRole.ENTHUSIAST to 3,  // Highest level user
            UserRole.FARMER to 2,      // Farm management capabilities
            UserRole.GENERAL to 1      // Basic user
        )
        
        return (roleHierarchy[userRole] ?: 0) >= (roleHierarchy[requiredRole] ?: 0)
    }
    
    /**
     * Parse deep link to destination
     */
    private fun parseDeepLink(deepLink: String): String? {
        // Implementation for parsing deep links
        return when {
            deepLink.contains("/fowl/") -> {
                val fowlId = deepLink.substringAfterLast("/")
                "fowl_detail/$fowlId"
            }
            deepLink.contains("/farm/") -> {
                val farmId = deepLink.substringAfterLast("/")
                "farm_detail/$farmId"
            }
            deepLink.contains("/marketplace") -> "marketplace"
            deepLink.contains("/dashboard") -> "dashboard"
            else -> null
        }
    }
    
    /**
     * Get required permission for destination
     */
    private fun getRequiredPermissionForDestination(destination: String): Permission? {
        return when {
            destination.startsWith("farm_") -> Permission.Farm.VIEW_OWN
            destination.startsWith("admin_") -> Permission.Admin.MANAGE_USERS
            destination.startsWith("marketplace") -> null // Public access
            else -> null
        }
    }
}

/**
 * Navigation destinations with permission requirements
 */
enum class NavigationDestination(
    val route: String,
    val title: String,
    val requiredPermission: Permission? = null
) {
    DASHBOARD("dashboard", "Dashboard"),
    MARKETPLACE("marketplace", "Marketplace"),
    MY_FOWLS("my_fowls", "My Fowls", Permission.Farm.VIEW_OWN),
    FARM_MANAGEMENT("farm_management", "Farm Management", Permission.Farm.MANAGE_BASIC),
    LINEAGE_TRACKING("lineage_tracking", "Lineage Tracking", Permission.Farm.MANAGE_BASIC),
    ADMIN_PANEL("admin_panel", "Admin Panel", Permission.Admin.MANAGE_USERS),
    REPORTS("reports", "Reports", Permission.Farm.VIEW_OWN)
}

/**
 * Permission checker utility
 */
object PermissionChecker {
    fun hasPermission(user: User, permission: Permission): Boolean {
        return when (user.role) {
            UserRole.ENTHUSIAST -> true // Enthusiast has all permissions
            UserRole.FARMER -> hasFarmerPermission(permission)
            UserRole.GENERAL -> hasGeneralPermission(permission)
        }
    }
    
    private fun hasFarmerPermission(permission: Permission): Boolean {
        return when (permission) {
            is Permission.Farm -> true
            is Permission.Analytics -> true
            is Permission.Team -> true
            is Permission.Admin -> false
            is Permission.Marketplace -> true
        }
    }
    
    private fun hasGeneralPermission(permission: Permission): Boolean {
        return when (permission) {
            is Permission.Marketplace.VIEW,
            is Permission.Marketplace.PURCHASE -> true
            is Permission.Farm.VIEW_OWN -> true
            else -> false
        }
    }
}

/**
 * Permission hierarchy - Comprehensive permission system for ROSTRY
 */
sealed class Permission {
    sealed class Farm : Permission() {
        object VIEW_OWN : Farm()
        object MANAGE_BASIC : Farm()
        object MANAGE_ADVANCED : Farm()
    }
    
    sealed class Admin : Permission() {
        object MANAGE_USERS : Admin()
        object VIEW_ANALYTICS : Admin()
    }
    
    sealed class Marketplace : Permission() {
        object VIEW : Marketplace()
        object PURCHASE : Marketplace()
        object SELL : Marketplace()
    }
    
    sealed class Analytics : Permission() {
        object BASIC : Analytics()
        object ADVANCED : Analytics()
    }
    
    sealed class Team : Permission() {
        object MANAGE : Team()
        object VIEW : Team()
    }
}
