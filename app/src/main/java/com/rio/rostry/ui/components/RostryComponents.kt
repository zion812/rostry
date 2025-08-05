package com.rio.rostry.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rio.rostry.ui.theme.*

/**
 * Enhanced Metric Card with improved accessibility and visual hierarchy
 * Designed for farm management metrics with trend indicators and semantic colors
 */
@Composable
fun RostryMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    trend: MetricTrend? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$title: $value${subtitle?.let { ", $it" } ?: ""}"
            },
        shape = RostryShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = RostryElevation.small),
        onClick = onClick ?: {}
    ) {
        if (isLoading) {
            ShimmerComponents.ShimmerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RostryDimensions.cardMinHeight)
            )
        } else {
            Column(
                modifier = Modifier.padding(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    trend?.let { TrendIndicator(trend = it) }
                }

                Text(
                    text = value,
                    style = RostryTypography.metricValue(),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = title,
                    style = RostryTypography.bodyText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                subtitle?.let {
                    Text(
                        text = it,
                        style = RostryTypography.caption(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Trend indicator for metrics with semantic colors and animations
 */
@Composable
fun TrendIndicator(
    trend: MetricTrend,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (trend.direction) {
        TrendDirection.UP -> Triple(
            Icons.Default.TrendingUp,
            if (trend.isPositive) RostrySemanticColors.healthyGreen else RostrySemanticColors.criticalRed,
            "+${trend.percentage}%"
        )
        TrendDirection.DOWN -> Triple(
            Icons.Default.TrendingDown,
            if (trend.isPositive) RostrySemanticColors.criticalRed else RostrySemanticColors.healthyGreen,
            "-${trend.percentage}%"
        )
        TrendDirection.STABLE -> Triple(
            Icons.Default.TrendingFlat,
            RostrySemanticColors.neutralGray,
            "0%"
        )
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(RostryAnimations.medium),
        label = "trend_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = animatedAlpha),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = RostryTypography.statusLabel(),
            color = color.copy(alpha = animatedAlpha)
        )
    }
}

/**
 * Enhanced Status Chip with semantic colors and accessibility
 */
@Composable
fun RostryStatusChip(
    status: String,
    type: StatusType = StatusType.NEUTRAL,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, contentColor) = when (type) {
        StatusType.SUCCESS -> RostrySemanticColors.healthyGreen to Color.White
        StatusType.WARNING -> RostrySemanticColors.warningAmber to Color.White
        StatusType.ERROR -> RostrySemanticColors.criticalRed to Color.White
        StatusType.INFO -> RostrySemanticColors.infoBlue to Color.White
        StatusType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .semantics {
                contentDescription = "Status: $status"
            },
        shape = RostryShapes.small,
        color = backgroundColor,
        onClick = onClick ?: {}
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = RostryTypography.statusLabel(),
            color = contentColor
        )
    }
}

/**
 * Enhanced Action Button with consistent styling and accessibility
 */
@Composable
fun RostryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    isLoading: Boolean = false
) {
    val (containerColor, contentColor) = when (variant) {
        ButtonVariant.PRIMARY -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        ButtonVariant.OUTLINE -> Color.Transparent to MaterialTheme.colorScheme.primary
        ButtonVariant.TEXT -> Color.Transparent to MaterialTheme.colorScheme.primary
    }

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .heightIn(min = RostryDimensions.buttonHeight)
            .semantics {
                contentDescription = if (isLoading) "Loading: $text" else text
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RostryShapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(RostrySpacing.sm))
        } else {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(RostrySpacing.sm))
            }
        }
        Text(
            text = text,
            style = RostryTypography.statusLabel()
        )
    }
}

/**
 * Enhanced Section Header with consistent styling
 */
@Composable
fun RostrySectionHeader(
    title: String,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = RostryTypography.sectionHeader(),
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = RostryTypography.caption(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        action?.invoke()
    }
}

/**
 * Enhanced Alert Card with severity levels and actions
 */
@Composable
fun RostryAlertCard(
    title: String,
    message: String,
    severity: AlertSeverity = AlertSeverity.INFO,
    onDismiss: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    actionText: String? = null,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, icon) = when (severity) {
        AlertSeverity.SUCCESS -> Triple(
            RostrySemanticColors.healthyGreen.copy(alpha = 0.1f),
            RostrySemanticColors.healthyGreen,
            Icons.Default.CheckCircle
        )
        AlertSeverity.WARNING -> Triple(
            RostrySemanticColors.warningAmber.copy(alpha = 0.1f),
            RostrySemanticColors.warningAmber,
            Icons.Default.Warning
        )
        AlertSeverity.ERROR -> Triple(
            RostrySemanticColors.criticalRed.copy(alpha = 0.1f),
            RostrySemanticColors.criticalRed,
            Icons.Default.Error
        )
        AlertSeverity.INFO -> Triple(
            RostrySemanticColors.infoBlue.copy(alpha = 0.1f),
            RostrySemanticColors.infoBlue,
            Icons.Default.Info
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RostryShapes.medium
    ) {
        Column(
            modifier = Modifier.padding(RostrySpacing.md),
            verticalArrangement = Arrangement.spacedBy(RostrySpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RostrySpacing.sm)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = RostryTypography.cardTitle(),
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                onDismiss?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = message,
                style = RostryTypography.bodyText(),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (onAction != null && actionText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAction) {
                        Text(
                            text = actionText,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Empty State component with actions
 */
@Composable
fun RostryEmptyState(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(RostrySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RostrySpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = title,
            style = RostryTypography.sectionHeader(),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            style = RostryTypography.bodyText(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (onAction != null && actionText != null) {
            Spacer(modifier = Modifier.height(RostrySpacing.sm))
            RostryActionButton(
                text = actionText,
                onClick = onAction,
                variant = ButtonVariant.PRIMARY
            )
        }
    }
}

/**
 * Progress indicator with semantic colors
 */
@Composable
fun RostryProgressIndicator(
    progress: Float,
    label: String? = null,
    showPercentage: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        progress >= 0.8f -> RostrySemanticColors.excellentPerformance
        progress >= 0.6f -> RostrySemanticColors.goodPerformance
        progress >= 0.4f -> RostrySemanticColors.averagePerformance
        else -> RostrySemanticColors.poorPerformance
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RostrySpacing.xs)
    ) {
        if (label != null || showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = RostryTypography.bodyText(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = RostryTypography.statusLabel(),
                        color = progressColor
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RostryShapes.small),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// Data classes for component props
data class MetricTrend(
    val direction: TrendDirection,
    val percentage: Float,
    val isPositive: Boolean
)


enum class StatusType { SUCCESS, WARNING, ERROR, INFO, NEUTRAL }

enum class ButtonVariant { PRIMARY, SECONDARY, OUTLINE, TEXT }

enum class AlertSeverity { SUCCESS, WARNING, ERROR, INFO }