package com.rio.rostry.ui.marketplace

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Shared data classes for marketplace functionality
 */

data class FowlCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val count: Int
)

data class MarketplaceFilter(
    val id: String,
    val name: String,
    val type: String
)

