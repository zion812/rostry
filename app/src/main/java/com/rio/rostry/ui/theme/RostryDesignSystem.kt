package com.rio.rostry.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * ROSTRY Design System - Consistent spacing, shapes, and components
 * Provides a unified design language for the entire application
 */

@Immutable
object RostrySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

@Immutable
object RostryShapes {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(24.dp)
    val circular = RoundedCornerShape(50)
}

@Immutable
object RostryElevation {
    val none = 0.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val extraLarge = 16.dp
}

/**
 * Semantic color tokens specifically designed for farm management
 * These colors convey meaning and status across the application
 */
@Immutable
object RostrySemanticColors {
    // Health status colors
    val healthyGreen = Color(0xFF4CAF50)
    val warningAmber = Color(0xFFFF9800)
    val criticalRed = Color(0xFFF44336)
    val infoBlue = Color(0xFF2196F3)
    val neutralGray = Color(0xFF9E9E9E)
    
    // Farm status colors
    val activeStatus = Color(0xFF4CAF50)
    val pendingStatus = Color(0xFFFF9800)
    val inactiveStatus = Color(0xFF9E9E9E)
    val errorStatus = Color(0xFFF44336)
    
    // Lifecycle stage colors
    val eggStage = Color(0xFFFFF3E0)
    val chickStage = Color(0xFFE8F5E8)
    val juvenileStage = Color(0xFFE3F2FD)
    val adultStage = Color(0xFFF3E5F5)
    val breederStage = Color(0xFFFFE0B2)
    
    // Performance indicators
    val excellentPerformance = Color(0xFF2E7D32)
    val goodPerformance = Color(0xFF4CAF50)
    val averagePerformance = Color(0xFFFF9800)
    val poorPerformance = Color(0xFFF44336)
    
    // Marketplace colors
    val availableForSale = Color(0xFF4CAF50)
    val reserved = Color(0xFFFF9800)
    val sold = Color(0xFF9E9E9E)
    val featured = Color(0xFF2196F3)
}

/**
 * Typography scale with semantic naming for farm management context
 */
@Immutable
object RostryTypography {
    @Composable
    fun farmTitle() = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Bold
    )
    
    @Composable
    fun sectionHeader() = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.SemiBold
    )
    
    @Composable
    fun cardTitle() = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Medium
    )
    
    @Composable
    fun metricValue() = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold
    )
    
    @Composable
    fun bodyText() = MaterialTheme.typography.bodyMedium
    
    @Composable
    fun caption() = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    @Composable
    fun statusLabel() = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Medium
    )
}

/**
 * Standard dimensions for consistent UI elements
 */
@Immutable
object RostryDimensions {
    // Touch targets
    val minTouchTarget = 48.dp
    val buttonHeight = 48.dp
    val iconButtonSize = 40.dp
    
    // Card dimensions
    val cardMinHeight = 120.dp
    val metricCardWidth = 160.dp
    val flockCardWidth = 200.dp
    
    // Image dimensions
    val avatarSize = 40.dp
    val largeAvatarSize = 80.dp
    val fowlImageSize = 120.dp
    val heroImageHeight = 200.dp
    
    // Layout constraints
    val maxContentWidth = 1200.dp
    val sidebarWidth = 280.dp
    val navigationRailWidth = 80.dp
}

/**
 * Animation durations for consistent motion design
 */
@Immutable
object RostryAnimations {
    const val fast = 150
    const val medium = 300
    const val slow = 500
    const val extraSlow = 1000
}

/**
 * Grid system for responsive layouts
 */
@Immutable
object RostryGrid {
    val compactColumns = 2
    val mediumColumns = 3
    val expandedColumns = 4
    
    val compactSpacing = 8.dp
    val mediumSpacing = 12.dp
    val expandedSpacing = 16.dp
}

/**
 * Accessibility constants
 */
@Immutable
object RostryAccessibility {
    const val minimumContrastRatio = 4.5f
    val minimumTouchTarget = 48.dp
    const val animationDurationScale = 1.0f
}

/**
 * Enhanced color tokens for semantic usage
 */
@Immutable
object RostryColorTokens {
    @Composable
    fun success() = Color(0xFF4CAF50)

    @Composable
    fun warning() = Color(0xFFFF9800)

    @Composable
    fun error() = MaterialTheme.colorScheme.error

    @Composable
    fun info() = Color(0xFF2196F3)

    @Composable
    fun fowlHealthy() = success()

    @Composable
    fun fowlSick() = error()

    @Composable
    fun fowlVaccinated() = info()

    @Composable
    fun priceHigh() = Color(0xFF4CAF50)

    @Composable
    fun priceMedium() = Color(0xFFFF9800)

    @Composable
    fun priceLow() = Color(0xFFF44336)
}

/**
 * Status indicators with consistent styling
 */
@Composable
fun StatusIndicator(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "healthy", "active", "available" -> RostryColorTokens.success() to Color.White
        "sick", "inactive", "sold" -> RostryColorTokens.error() to Color.White
        "vaccinated", "verified" -> RostryColorTokens.info() to Color.White
        "pending", "review" -> RostryColorTokens.warning() to Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}