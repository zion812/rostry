package com.rio.rostry.ui.navigation

/**
 * Simple Permission system for role-based navigation
 */
sealed class Permission {
    object Marketplace {
        object VIEW : Permission()
    }
    object Farm {
        object VIEW_OWN : Permission()
        object MANAGE_BASIC : Permission()
    }
    object Analytics {
        object BASIC : Permission()
    }
    object Team {
        object MANAGE : Permission()
    }
}