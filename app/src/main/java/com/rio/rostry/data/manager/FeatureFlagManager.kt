package com.rio.rostry.data.manager

import android.content.Context
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.featureFlagDataStore: DataStore<Preferences> by preferencesDataStore(name = "feature_flags")

@Singleton
class FeatureFlagManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) {

    /**
     * Feature flags enum
     */
    enum class FeatureFlag(val key: String, val defaultValue: Boolean = false) {
        // Navigation features
        ROLE_BASED_NAVIGATION("role_based_navigation", true),
        ADAPTIVE_NAVIGATION("adaptive_navigation", true),
        NAVIGATION_ANIMATIONS("navigation_animations", true),

        // Role features
        DYNAMIC_ROLES("dynamic_roles", false),
        CUSTOM_PERMISSIONS("custom_permissions", false),
        ROLE_INHERITANCE("role_inheritance", true),

        // Organization features
        MULTI_TENANT("multi_tenant", false),
        ORGANIZATION_SWITCHING("organization_switching", false),
        TEAM_COLLABORATION("team_collaboration", true),

        // Advanced features
        LIVE_BROADCASTING("live_broadcasting", false),
        ADVANCED_ANALYTICS("advanced_analytics", true),
        API_ACCESS("api_access", false),

        // Performance features
        PERMISSION_CACHING("permission_caching", true),
        OFFLINE_MODE("offline_mode", true),
        BACKGROUND_SYNC("background_sync", true),

        // UI features
        DARK_MODE("dark_mode", true),
        MATERIAL_YOU("material_you", false),
        ACCESSIBILITY_ENHANCEMENTS("accessibility_enhancements", true)
    }

    /**
     * Initialize feature flags
     */
    suspend fun initialize() {
        // Initialize with default values for now
        // Remote config can be added later
    }

    /**
     * Check if feature is enabled
     */
    suspend fun isFeatureEnabled(flag: FeatureFlag): Boolean {
        return try {
            // Check user-specific overrides first
            val userOverride = getUserOverride(flag)
            if (userOverride != null) return userOverride

            // Check role-based access
            if (!hasRoleAccess(flag)) return false

            // Use default value for now (remote config can be added later)
            flag.defaultValue
        } catch (e: Exception) {
            flag.defaultValue
        }
    }

    /**
     * Check if feature is enabled for specific role
     */
    suspend fun isFeatureEnabledForRole(flag: FeatureFlag, roleId: String): Boolean {
        return try {
            // Use default value for now (remote config can be added later)
            flag.defaultValue
        } catch (e: Exception) {
            flag.defaultValue
        }
    }

    /**
     * Get feature flag value as string
     */
    suspend fun getFeatureValue(flag: FeatureFlag): String {
        return try {
            // Return empty string for now (remote config can be added later)
            ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Set user-specific override
     */
    suspend fun setUserOverride(flag: FeatureFlag, enabled: Boolean) {
        context.featureFlagDataStore.edit { preferences ->
            preferences[booleanPreferencesKey(flag.key)] = enabled
        }
    }

    /**
     * Get user-specific override
     */
    private suspend fun getUserOverride(flag: FeatureFlag): Boolean? {
        var result: Boolean? = null
        context.featureFlagDataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(flag.key)]
        }.collect { result = it }
        return result
    }

    /**
     * Check role-based access to feature
     */
    private suspend fun hasRoleAccess(flag: FeatureFlag): Boolean {
        val session = sessionManager.userSession.value ?: return false
        val roleId = session.organizationRoleId ?: "user"

        return when (flag) {
            FeatureFlag.LIVE_BROADCASTING -> roleId in listOf("premium_breeder", "farm_manager", "moderator", "super_admin")
            FeatureFlag.ADVANCED_ANALYTICS -> roleId in listOf("verified_farmer", "premium_breeder", "farm_manager", "moderator", "super_admin")
            FeatureFlag.API_ACCESS -> roleId in listOf("farm_manager", "moderator", "super_admin")
            FeatureFlag.MULTI_TENANT -> roleId in listOf("farm_manager", "moderator", "super_admin")
            else -> true // Most features available to all roles
        }
    }

    /**
     * Get all enabled features for current user
     */
    suspend fun getEnabledFeatures(): Set<FeatureFlag> {
        return FeatureFlag.values().filter { flag ->
            isFeatureEnabled(flag)
        }.toSet()
    }

    /**
     * Refresh feature flags from remote
     */
    suspend fun refresh() {
        // Remote config refresh can be added later
    }

    private fun getDefaultValues(): Map<String, Any> {
        return FeatureFlag.values().associate { flag ->
            flag.key to flag.defaultValue
        }
    }
}

/**
 * Composable for feature flag checking
 */
@Composable
fun FeatureGate(
    flag: FeatureFlagManager.FeatureFlag,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    featureFlagManager: FeatureFlagManager
) {
    var isEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(flag) {
        isEnabled = featureFlagManager.isFeatureEnabled(flag)
    }

    if (isEnabled) {
        content()
    } else {
        fallback?.invoke()
    }
}