package com.rio.rostry.data.cache

import androidx.collection.LruCache
import com.rio.rostry.data.model.role.Permission
import com.rio.rostry.data.model.role.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionCache @Inject constructor() {
    private val mutex = Mutex()
    private val permissionCache = LruCache<String, Set<Permission>>(100)
    private val roleCache = LruCache<String, UserRole>(50)
    private val organizationPermissionCache = LruCache<String, Set<Permission>>(200)

    // Cache statistics tracking
    private val _cacheStats = MutableStateFlow(CacheStats())
    val cacheStats: StateFlow<CacheStats> = _cacheStats

    // Cache invalidation tracking
    private val _cacheVersion = MutableStateFlow(0L)
    val cacheVersion: StateFlow<Long> = _cacheVersion

    /**
     * Get cached permissions for user
     */
    suspend fun getUserPermissions(userId: String): Set<Permission>? {
        return mutex.withLock {
            val permissions = permissionCache.get(userId)
            updateStats(permissions != null, CacheType.PERMISSION)
            permissions
        }
    }

    /**
     * Cache user permissions
     */
    suspend fun cacheUserPermissions(userId: String, permissions: Set<Permission>) {
        mutex.withLock {
            permissionCache.put(userId, permissions)
        }
    }

    /**
     * Get cached role
     */
    suspend fun getUserRole(userId: String): UserRole? {
        return mutex.withLock {
            val role = roleCache.get(userId)
            updateStats(role != null, CacheType.ROLE)
            role
        }
    }

    /**
     * Cache user role
     */
    suspend fun cacheUserRole(userId: String, role: UserRole) {
        mutex.withLock {
            roleCache.put(userId, role)
        }
    }

    /**
     * Get organization-specific permissions
     */
    suspend fun getOrganizationPermissions(userId: String, organizationId: String): Set<Permission>? {
        return mutex.withLock {
            val key = "${userId}_${organizationId}"
            val permissions = organizationPermissionCache.get(key)
            updateStats(permissions != null, CacheType.ORGANIZATION)
            permissions
        }
    }

    /**
     * Cache organization-specific permissions
     */
    suspend fun cacheOrganizationPermissions(
        userId: String,
        organizationId: String,
        permissions: Set<Permission>
    ) {
        mutex.withLock {
            organizationPermissionCache.put("${userId}_${organizationId}", permissions)
        }
    }

    /**
     * Invalidate cache for user
     */
    suspend fun invalidateUser(userId: String) {
        mutex.withLock {
            permissionCache.remove(userId)
            roleCache.remove(userId)

            // Remove organization-specific entries
            val keysToRemove = mutableListOf<String>()
            organizationPermissionCache.snapshot().keys.forEach { key ->
                if (key.startsWith("${userId}_")) {
                    keysToRemove.add(key)
                }
            }
            keysToRemove.forEach { organizationPermissionCache.remove(it) }

            _cacheVersion.value = System.currentTimeMillis()
        }
    }

    /**
     * Invalidate entire cache
     */
    suspend fun invalidateAll() {
        mutex.withLock {
            permissionCache.evictAll()
            roleCache.evictAll()
            organizationPermissionCache.evictAll()
            _cacheVersion.value = System.currentTimeMillis()
        }
    }

    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        return _cacheStats.value
    }

    private fun updateStats(hit: Boolean, type: CacheType) {
        val current = _cacheStats.value
        val newStats = when (type) {
            CacheType.PERMISSION -> current.copy(
                permissionHits = if (hit) current.permissionHits + 1 else current.permissionHits,
                permissionMisses = if (!hit) current.permissionMisses + 1 else current.permissionMisses
            )
            CacheType.ROLE -> current.copy(
                roleHits = if (hit) current.roleHits + 1 else current.roleHits,
                roleMisses = if (!hit) current.roleMisses + 1 else current.roleMisses
            )
            CacheType.ORGANIZATION -> current.copy(
                organizationHits = if (hit) current.organizationHits + 1 else current.organizationHits,
                organizationMisses = if (!hit) current.organizationMisses + 1 else current.organizationMisses
            )
        }
        _cacheStats.value = newStats
    }

    private enum class CacheType { PERMISSION, ROLE, ORGANIZATION }
}

data class CacheStats(
    val permissionHits: Long = 0,
    val permissionMisses: Long = 0,
    val roleHits: Long = 0,
    val roleMisses: Long = 0,
    val organizationHits: Long = 0,
    val organizationMisses: Long = 0
) {
    val permissionHitRate: Double get() = if (permissionHits + permissionMisses > 0)
        permissionHits.toDouble() / (permissionHits + permissionMisses) else 0.0

    val roleHitRate: Double get() = if (roleHits + roleMisses > 0)
        roleHits.toDouble() / (roleHits + roleMisses) else 0.0

    val organizationHitRate: Double get() = if (organizationHits + organizationMisses > 0)
        organizationHits.toDouble() / (organizationHits + organizationMisses) else 0.0

    val overallHitRate: Double get() {
        val totalHits = permissionHits + roleHits + organizationHits
        val totalRequests = totalHits + permissionMisses + roleMisses + organizationMisses
        return if (totalRequests > 0) totalHits.toDouble() / totalRequests else 0.0
    }
}