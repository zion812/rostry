package com.rio.rostry.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector


/**
 * Shared navigation data classes for ROSTRY application
 */

/**
 * Represents a navigation item with comprehensive metadata
 */
data class NavigationItemData(
    val id: String,
    val label: String,
    val route: String,
    val icon: ImageVector,
    val requiredPermissions: List<String> = emptyList(),
    val badgeCount: Int = 0,
    val isEnabled: Boolean = true
)

/**
 * Represents a navigation section containing multiple items
 */
data class NavigationSection(
    val title: String,
    val items: List<NavigationItemData>
)

/**
 * Simple navigation item for basic navigation components
 */
data class SimpleNavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: String? = null
)