package com.rio.rostry.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Home",
        icon = Icons.Default.Home,
        screen = Screen.Home
    ),
    BottomNavItem(
        title = "Dashboard",
        icon = Icons.Default.Settings,
        screen = Screen.Dashboard
    ),
    BottomNavItem(
        title = "Marketplace",
        icon = Icons.Default.Search,
        screen = Screen.Marketplace
    ),
    BottomNavItem(
        title = "My Fowls",
        icon = Icons.Default.ShoppingCart,
        screen = Screen.MyFowls
    ),
    BottomNavItem(
        title = "Profile",
        icon = Icons.Default.Person,
        screen = Screen.Profile
    )
)