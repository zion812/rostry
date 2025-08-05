package com.rio.rostry.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.cache.PermissionCache
import com.rio.rostry.data.manager.SessionManager
import com.rio.rostry.data.model.role.UserRole
// Use local Permission class to avoid conflicts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationStateManager @Inject constructor(
    private val sessionManager: SessionManager,
    private val permissionCache: PermissionCache
) : ViewModel() {

    // Core navigation state
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Loading)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Permission checking state
    private val _permissionChecks = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionChecks: StateFlow<Map<String, Boolean>> = _permissionChecks.asStateFlow()

    // Navigation items cache
    private val _navigationItems = MutableStateFlow<List<NavigationItemData>>(emptyList())
    val navigationItems: StateFlow<List<NavigationItemData>> = _navigationItems.asStateFlow()

    init {
        observeSessionChanges()
        observeCacheInvalidation()
    }

    private fun observeSessionChanges() {
        viewModelScope.launch {
            sessionManager.userSession
                .collect { session ->
                    if (session != null) {
                        updateNavigationState(session)
                    } else {
                        _navigationState.value = NavigationState.Unauthenticated
                    }
                }
        }
    }

    private fun observeCacheInvalidation() {
        viewModelScope.launch {
            permissionCache.cacheVersion.collect {
                // Refresh navigation state when cache is invalidated
                sessionManager.userSession.value?.let { session ->
                    updateNavigationState(session)
                }
            }
        }
    }

    private suspend fun updateNavigationState(session: SessionManager.UserSession) {
        try {
            _navigationState.value = NavigationState.Loading

            val roleId = session.organizationRoleId ?: "user"
            val navigationItems = buildNavigationItems(roleId, session)

            _navigationItems.value = navigationItems

            _navigationState.value = NavigationState.Ready(
                userRoleId = roleId,
                organizationId = session.activeOrganizationId,
                navigationItems = navigationItems
            )

            // Pre-cache common permission checks
            preloadPermissionChecks(session)

        } catch (e: Exception) {
            _navigationState.value = NavigationState.Error(e.message ?: "Navigation error")
        }
    }

    private suspend fun buildNavigationItems(
        roleId: String,
        session: SessionManager.UserSession
    ): List<NavigationItemData> {
        return getNavigationItemsForRole(roleId).filter { item ->
            item.requiredPermissions.all { permissionId ->
                checkPermissionForRoleById(permissionId, roleId)
            }
        }
    }
    
    private fun checkPermissionForRole(permission: Permission, roleId: String): Boolean {
        return when (permission) {
            Permission.Marketplace.VIEW -> true // Available to all users
            Permission.Farm.VIEW_OWN -> roleId in listOf("basic_farmer", "verified_farmer", "premium_breeder", "farm_manager")
            Permission.Farm.MANAGE_BASIC -> roleId in listOf("verified_farmer", "premium_breeder", "farm_manager")
            Permission.Analytics.BASIC -> roleId in listOf("verified_farmer", "premium_breeder", "farm_manager")
            Permission.Team.MANAGE -> roleId in listOf("farm_manager", "moderator", "super_admin")
            else -> false
        }
    }

    private fun checkPermissionForRoleById(permissionId: String, roleId: String): Boolean {
        return when (permissionId) {
            "marketplace.view" -> true // Available to all users
            "farm.view.own" -> roleId in listOf("basic_farmer", "verified_farmer", "premium_breeder", "farm_manager")
            "farm.manage.basic" -> roleId in listOf("verified_farmer", "premium_breeder", "farm_manager")
            "analytics.basic" -> roleId in listOf("verified_farmer", "premium_breeder", "farm_manager")
            "team.manage" -> roleId in listOf("farm_manager", "moderator", "super_admin")
            else -> true // Default to allow for navigation items without specific permissions
        }
    }

    private fun getNavigationItemsForRole(roleId: String): List<NavigationItemData> {
        return when (roleId) {
            "consumer" -> listOf(
                NavigationItemData("marketplace", "Market", "marketplace", Icons.Default.Store),
                NavigationItemData("explore", "Explore", "explore", Icons.Default.Explore),
                NavigationItemData("create", "Create", "create_social", Icons.Default.Add),
                NavigationItemData("cart", "Cart", "cart", Icons.Default.ShoppingCart),
                NavigationItemData("profile", "Profile", "profile", Icons.Default.Person)
            )
            "basic_farmer", "verified_farmer" -> listOf(
                NavigationItemData("home", "Home", "farmer_home", Icons.Default.Home),
                NavigationItemData("marketplace", "Market", "marketplace", Icons.Default.Store),
                NavigationItemData("create", "Create", "create_farmer", Icons.Default.Add),
                NavigationItemData("community", "Community", "community", Icons.Default.Groups),
                NavigationItemData("profile", "Profile", "farmer_profile", Icons.Default.Person)
            )
            "premium_breeder", "farm_manager" -> listOf(
                NavigationItemData("home", "Home", "breeder_home", Icons.Default.Home),
                NavigationItemData("explore", "Explore", "explore", Icons.Default.Explore),
                NavigationItemData("create", "Create", "create_breeder", Icons.Default.Add),
                NavigationItemData("dashboard", "Dashboard", "farm_dashboard", Icons.Default.Dashboard),
                NavigationItemData("transfers", "Transfers", "transfers", Icons.Default.SwapHoriz)
            )
            "moderator", "super_admin" -> listOf(
                NavigationItemData("dashboard", "Dashboard", "admin_dashboard", Icons.Default.Dashboard),
                NavigationItemData("users", "Users", "user_management", Icons.Default.People),
                NavigationItemData("reports", "Reports", "reports", Icons.Default.Assessment),
                NavigationItemData("settings", "Settings", "admin_settings", Icons.Default.Settings)
            )
            else -> listOf(
                NavigationItemData("marketplace", "Market", "marketplace", Icons.Default.Store),
                NavigationItemData("profile", "Profile", "profile", Icons.Default.Person)
            )
        }
    }

    private suspend fun preloadPermissionChecks(session: SessionManager.UserSession) {
        val commonPermissions = listOf(
            Permission.Marketplace.VIEW,
            Permission.Farm.VIEW_OWN,
            Permission.Analytics.BASIC
        )

        val roleId = session.organizationRoleId ?: "user"
        val checks = commonPermissions.associate { permission ->
            getPermissionId(permission) to checkPermissionForRole(permission, roleId)
        }

        _permissionChecks.value = checks
    }

    /**
     * Fast permission check with caching
     */
    fun hasPermission(permission: Permission): Boolean {
        val permissionId = getPermissionId(permission)
        
        // Check cache first
        _permissionChecks.value[permissionId]?.let { return it }

        // Fallback to session check
        val session = sessionManager.userSession.value
        val hasPermission = sessionManager.hasPermission(permissionId)

        // Update cache
        _permissionChecks.value = _permissionChecks.value + (permissionId to hasPermission)

        return hasPermission
    }

    /**
     * Get permission ID for local Permission objects
     */
    private fun getPermissionId(permission: Permission): String {
        return when (permission) {
            Permission.Marketplace.VIEW -> "marketplace.view"
            Permission.Farm.VIEW_OWN -> "farm.view.own"
            Permission.Farm.MANAGE_BASIC -> "farm.manage.basic"
            Permission.Analytics.BASIC -> "analytics.basic"
            Permission.Team.MANAGE -> "team.manage"
            else -> "unknown"
        }
    }

    /**
     * Batch permission check
     */
    fun hasPermissions(permissions: List<Permission>, requireAll: Boolean = true): Boolean {
        return if (requireAll) {
            permissions.all { hasPermission(it) }
        } else {
            permissions.any { hasPermission(it) }
        }
    }

    /**
     * Refresh navigation state
     */
    fun refresh() {
        viewModelScope.launch {
            sessionManager.userSession.value?.let { session ->
                updateNavigationState(session)
            }
        }
    }
}

sealed class NavigationState {
    object Loading : NavigationState()
    object Unauthenticated : NavigationState()
    data class Ready(
        val userRoleId: String,
        val organizationId: String?,
        val navigationItems: List<NavigationItemData>
    ) : NavigationState()
    data class Error(val message: String) : NavigationState()
}