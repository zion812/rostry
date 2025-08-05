package com.rio.rostry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.rio.rostry.ui.theme.*

/**
 * Adaptive layout system for responsive design across different screen sizes
 * Provides consistent spacing, grid layouts, and component sizing
 */

@Composable
fun AdaptiveGrid(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable (columns: Int, spacing: androidx.compose.ui.unit.Dp) -> Unit
) {
    val (columns, spacing) = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> RostryGrid.compactColumns to RostryGrid.compactSpacing
        WindowWidthSizeClass.Medium -> RostryGrid.mediumColumns to RostryGrid.mediumSpacing
        WindowWidthSizeClass.Expanded -> RostryGrid.expandedColumns to RostryGrid.expandedSpacing
        else -> RostryGrid.compactColumns to RostryGrid.compactSpacing
    }

    content(columns, spacing)
}

@Composable
fun AdaptiveContentPadding(
    windowSizeClass: WindowSizeClass
): PaddingValues {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> PaddingValues(RostrySpacing.md)
        WindowWidthSizeClass.Medium -> PaddingValues(RostrySpacing.lg)
        WindowWidthSizeClass.Expanded -> PaddingValues(RostrySpacing.xl)
        else -> PaddingValues(RostrySpacing.md)
    }
}

@Composable
fun AdaptiveCardWidth(
    windowSizeClass: WindowSizeClass,
    minWidth: androidx.compose.ui.unit.Dp = 200.dp,
    maxWidth: androidx.compose.ui.unit.Dp = 400.dp
): androidx.compose.ui.unit.Dp {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> minWidth
        WindowWidthSizeClass.Medium -> (minWidth + maxWidth) / 2
        WindowWidthSizeClass.Expanded -> maxWidth
        else -> minWidth
    }
}

/**
 * Screen size utilities
 */
object ScreenSize {
    @Composable
    fun isCompact(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp < 600
    }

    @Composable
    fun isMedium(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp in 600..839
    }

    @Composable
    fun isExpanded(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 840
    }
}