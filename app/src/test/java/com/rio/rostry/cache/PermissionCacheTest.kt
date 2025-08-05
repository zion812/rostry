package com.rio.rostry.cache

import com.rio.rostry.data.cache.PermissionCache
import com.rio.rostry.data.model.role.Permission
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class PermissionCacheTest {

    private lateinit var permissionCache: PermissionCache

    @Before
    fun setup() {
        permissionCache = PermissionCache()
    }

    @Test
    fun `should cache and retrieve user permissions correctly`() = runTest {
        // Given
        val userId = "user123"
        val permissions = setOf(
            Permission.Marketplace.VIEW,
            Permission.Farm.VIEW_OWN,
            Permission.Analytics.BASIC
        )

        // When
        permissionCache.cacheUserPermissions(userId, permissions)
        val cachedPermissions = permissionCache.getUserPermissions(userId)

        // Then
        assertEquals("Should return cached permissions", permissions, cachedPermissions)
    }

    @Test
    fun `should return null for non-existent user permissions`() = runTest {
        // Given
        val nonExistentUserId = "nonexistent"

        // When
        val permissions = permissionCache.getUserPermissions(nonExistentUserId)

        // Then
        assertNull("Should return null for non-existent user", permissions)
    }

    @Test
    fun `should handle cache eviction correctly`() = runTest {
        // Given
        val permissions = setOf(Permission.Marketplace.VIEW)

        // When - Fill cache beyond capacity
        for (i in 1..150) { // Assuming cache size is 100
            permissionCache.cacheUserPermissions("user$i", permissions)
        }

        val firstUserPermissions = permissionCache.getUserPermissions("user1")
        val lastUserPermissions = permissionCache.getUserPermissions("user150")

        // Then
        assertNull("First user should be evicted", firstUserPermissions)
        assertEquals("Last user should still be cached", permissions, lastUserPermissions)
    }
}