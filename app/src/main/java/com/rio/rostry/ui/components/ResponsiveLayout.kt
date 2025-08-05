package com.rio.rostry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive layout system for different screen sizes
 */
@Composable
fun ResponsiveLayout(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    compactContent: @Composable () -> Unit,
    mediumContent: @Composable () -> Unit = compactContent,
    expandedContent: @Composable () -> Unit = mediumContent
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> compactContent()
        WindowWidthSizeClass.Medium -> mediumContent()
        WindowWidthSizeClass.Expanded -> expandedContent()
    }
}

/**
 * Adaptive grid that changes columns based on screen size
 */
@Composable
fun AdaptiveGrid(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable (columns: Int, spacing: Dp) -> Unit
) {
    val (columns, spacing) = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2 to 8.dp
        WindowWidthSizeClass.Medium -> 3 to 12.dp
        WindowWidthSizeClass.Expanded -> 4 to 16.dp
        else -> 2 to 8.dp // Default to compact layout
    }
    
    content(columns, spacing)
}

/**
 * Responsive padding that adapts to screen size
 */
@Composable
fun Modifier.responsivePadding(windowSizeClass: WindowSizeClass): Modifier {
    val padding = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 16.dp
        WindowWidthSizeClass.Medium -> 24.dp
        WindowWidthSizeClass.Expanded -> 32.dp
        else -> 16.dp // Default to compact padding
    }
    return this.padding(padding)
}

/**
 * Responsive content width with maximum constraints
 */
@Composable
fun Modifier.responsiveContentWidth(windowSizeClass: WindowSizeClass): Modifier {
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> this.fillMaxWidth()
        WindowWidthSizeClass.Medium -> this.fillMaxWidth().widthIn(max = 800.dp)
        WindowWidthSizeClass.Expanded -> this.fillMaxWidth().widthIn(max = 1200.dp)
        else -> this.fillMaxWidth() // Default to compact width
    }
}

/**
 * Two-pane layout for larger screens
 */
@Composable
fun TwoPaneLayout(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
    singlePaneContent: @Composable () -> Unit = primaryPane
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            singlePaneContent()
        }
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            Row(modifier = modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    primaryPane()
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    secondaryPane()
                }
            }
        }
    }
}

/**
 * Responsive navigation layout
 */
@Composable
fun ResponsiveNavigationLayout(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    navigationContent: @Composable () -> Unit,
    bodyContent: @Composable () -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Bottom navigation for compact screens
            Column(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    bodyContent()
                }
                navigationContent()
            }
        }
        WindowWidthSizeClass.Medium -> {
            // Navigation rail for medium screens
            Row(modifier = modifier.fillMaxSize()) {
                navigationContent()
                Box(modifier = Modifier.weight(1f)) {
                    bodyContent()
                }
            }
        }
        WindowWidthSizeClass.Expanded -> {
            // Navigation drawer for expanded screens
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.width(280.dp)) {
                    navigationContent()
                }
                Box(modifier = Modifier.weight(1f)) {
                    bodyContent()
                }
            }
        }
    }
}
