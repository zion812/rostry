package com.rio.rostry.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rio.rostry.data.model.GrowthMetric
import com.rio.rostry.util.formatDate
import kotlin.math.max
import kotlin.math.min

/**
 * Advanced growth chart component with interactive features and trend analysis
 */
@Composable
fun GrowthChart(
    growthMetrics: List<GrowthMetric>,
    modifier: Modifier = Modifier,
    showTrendLine: Boolean = true,
    showDataPoints: Boolean = true,
    onMetricClick: (GrowthMetric) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with controls
            GrowthChartHeader(
                metricsCount = growthMetrics.size,
                latestMetric = growthMetrics.maxByOrNull { it.measurementDate }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (growthMetrics.isNotEmpty()) {
                // Chart area
                GrowthChartCanvas(
                    metrics = growthMetrics,
                    showTrendLine = showTrendLine,
                    showDataPoints = showDataPoints,
                    onMetricClick = onMetricClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Metrics summary
                GrowthMetricsSummary(
                    metrics = growthMetrics,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Growth insights
                GrowthInsights(
                    metrics = growthMetrics,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EmptyGrowthChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun GrowthChartHeader(
    metricsCount: Int,
    latestMetric: GrowthMetric?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Growth Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$metricsCount measurements recorded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        latestMetric?.let { metric ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Latest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${metric.weight} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = formatDate(metric.measurementDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun GrowthChartCanvas(
    metrics: List<GrowthMetric>,
    showTrendLine: Boolean,
    showDataPoints: Boolean,
    onMetricClick: (GrowthMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedMetrics = metrics.sortedBy { it.measurementDate }
    
    // Animation for chart drawing
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "chart_animation"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (sortedMetrics.size >= 2) {
                    drawGrowthChart(
                        metrics = sortedMetrics,
                        progress = animationProgress,
                        showTrendLine = showTrendLine,
                        showDataPoints = showDataPoints
                    )
                }
            }

            // Chart labels and axes
            ChartLabels(
                metrics = sortedMetrics,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun DrawScope.drawGrowthChart(
    metrics: List<GrowthMetric>,
    progress: Float,
    showTrendLine: Boolean,
    showDataPoints: Boolean
) {
    if (metrics.size < 2) return

    val weights = metrics.map { it.weight }
    val minWeight = weights.minOrNull() ?: 0.0
    val maxWeight = weights.maxOrNull() ?: 1.0
    val weightRange = maxWeight - minWeight
    
    val padding = 40.dp.toPx()
    val chartWidth = size.width - (padding * 2)
    val chartHeight = size.height - (padding * 2)

    // Calculate points
    val points = metrics.mapIndexed { index, metric ->
        val x = padding + (index.toFloat() / (metrics.size - 1)) * chartWidth
        val y = padding + chartHeight - ((metric.weight - minWeight) / weightRange * chartHeight).toFloat()
        Offset(x, y)
    }

    // Draw grid lines
    drawGridLines(padding, chartWidth, chartHeight, minWeight, maxWeight)

    // Draw weight line with animation
    if (points.size >= 2) {
        val animatedPoints = points.take((points.size * progress).toInt().coerceAtLeast(2))
        
        // Main growth line
        val path = Path().apply {
            moveTo(animatedPoints.first().x, animatedPoints.first().y)
            for (i in 1 until animatedPoints.size) {
                lineTo(animatedPoints[i].x, animatedPoints[i].y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Gradient fill under the line
        val fillPath = Path().apply {
            moveTo(animatedPoints.first().x, padding + chartHeight)
            lineTo(animatedPoints.first().x, animatedPoints.first().y)
            for (i in 1 until animatedPoints.size) {
                lineTo(animatedPoints[i].x, animatedPoints[i].y)
            }
            lineTo(animatedPoints.last().x, padding + chartHeight)
            close()
        }

        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4CAF50).copy(alpha = 0.3f),
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            ),
            startY = padding,
            endY = padding + chartHeight
        )

        drawPath(
            path = fillPath,
            brush = gradient
        )
    }

    // Draw trend line if enabled
    if (showTrendLine && points.size >= 3) {
        drawTrendLine(points, progress)
    }

    // Draw data points if enabled
    if (showDataPoints) {
        points.forEachIndexed { index, point ->
            if (index < (points.size * progress).toInt()) {
                drawDataPoint(point, metrics[index])
            }
        }
    }
}

private fun DrawScope.drawGridLines(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    minWeight: Double,
    maxWeight: Double
) {
    val gridColor = Color(0xFFE0E0E0)
    val gridStroke = Stroke(width = 1.dp.toPx())

    // Horizontal grid lines (weight levels)
    for (i in 0..4) {
        val y = padding + (i.toFloat() / 4) * chartHeight
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = gridStroke.width
        )
    }

    // Vertical grid lines (time intervals)
    for (i in 0..4) {
        val x = padding + (i.toFloat() / 4) * chartWidth
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = gridStroke.width
        )
    }
}

private fun DrawScope.drawTrendLine(points: List<Offset>, progress: Float) {
    // Simple linear regression for trend line
    val n = points.size
    val sumX = points.indices.sum().toFloat()
    val sumY = points.sumOf { it.y.toDouble() }.toFloat()
    val sumXY = points.mapIndexed { index, point -> index * point.y }.sum()
    val sumX2 = points.indices.sumOf { it * it }.toFloat()

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    val startX = points.first().x
    val endX = points.last().x
    val startY = slope * 0 + intercept
    val endY = slope * (n - 1) + intercept

    val trendPath = Path().apply {
        moveTo(startX, startY)
        lineTo(startX + (endX - startX) * progress, startY + (endY - startY) * progress)
    }

    drawPath(
        path = trendPath,
        color = Color(0xFFFF9800),
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )
    )
}

private fun DrawScope.drawDataPoint(point: Offset, metric: GrowthMetric) {
    // Outer circle
    drawCircle(
        color = Color(0xFF4CAF50),
        radius = 8.dp.toPx(),
        center = point
    )
    
    // Inner circle
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = point
    )
}

@Composable
private fun ChartLabels(
    metrics: List<GrowthMetric>,
    modifier: Modifier = Modifier
) {
    if (metrics.isEmpty()) return

    val weights = metrics.map { it.weight }
    val minWeight = weights.minOrNull() ?: 0.0
    val maxWeight = weights.maxOrNull() ?: 1.0

    Box(modifier = modifier) {
        // Y-axis labels (weights)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 4 downTo 0) {
                val weight = minWeight + (maxWeight - minWeight) * (i.toDouble() / 4)
                Text(
                    text = "${weight.toInt()}kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // X-axis labels (dates)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp, start = 40.dp, end = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayMetrics = if (metrics.size <= 5) {
                metrics
            } else {
                listOf(
                    metrics.first(),
                    metrics[metrics.size / 4],
                    metrics[metrics.size / 2],
                    metrics[metrics.size * 3 / 4],
                    metrics.last()
                )
            }

            displayMetrics.forEach { metric ->
                Text(
                    text = formatDate(metric.measurementDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GrowthMetricsSummary(
    metrics: List<GrowthMetric>,
    modifier: Modifier = Modifier
) {
    val latest = metrics.maxByOrNull { it.measurementDate }
    val previous = metrics.filter { it.measurementDate < (latest?.measurementDate ?: 0) }.maxByOrNull { it.measurementDate }
    val growthRate = latest?.calculateGrowthRate(previous) ?: 0.0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricCard(
            label = "Current Weight",
            value = "${latest?.weight ?: 0.0} kg",
            icon = Icons.Default.Assessment,
            trend = if (growthRate > 0) TrendDirection.UP else TrendDirection.STABLE
        )

        MetricCard(
            label = "Height",
            value = "${latest?.height ?: 0.0} cm",
            icon = Icons.Default.Height,
            trend = TrendDirection.STABLE
        )

        MetricCard(
            label = "Growth Rate",
            value = "${String.format("%.2f", growthRate)} kg/week",
            icon = Icons.Default.TrendingUp,
            trend = when {
                growthRate > 0.1 -> TrendDirection.UP
                growthRate < -0.1 -> TrendDirection.DOWN
                else -> TrendDirection.STABLE
            }
        )

        MetricCard(
            label = "Body Condition",
            value = "${latest?.bodyConditionScore ?: 5}/10",
            icon = Icons.Default.Favorite,
            trend = TrendDirection.STABLE
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    trend: TrendDirection
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = when (trend) {
                        TrendDirection.UP -> Icons.Default.TrendingUp
                        TrendDirection.DOWN -> Icons.Default.TrendingDown
                        TrendDirection.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (trend) {
                        TrendDirection.UP -> Color(0xFF4CAF50)
                        TrendDirection.DOWN -> Color(0xFFF44336)
                        TrendDirection.STABLE -> Color(0xFF9E9E9E)
                    },
                    modifier = Modifier.size(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun GrowthInsights(
    metrics: List<GrowthMetric>,
    modifier: Modifier = Modifier
) {
    val insights = generateGrowthInsights(metrics)
    
    if (insights.isNotEmpty()) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Growth Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                insights.forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGrowthChart(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No growth data recorded yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Start recording measurements to track growth progress",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// Helper functions and enums
enum class TrendDirection {
    UP, DOWN, STABLE
}

private fun generateGrowthInsights(metrics: List<GrowthMetric>): List<String> {
    if (metrics.size < 3) return emptyList()
    
    val insights = mutableListOf<String>()
    val sortedMetrics = metrics.sortedBy { it.measurementDate }
    
    // Growth rate analysis
    val recentGrowthRates = sortedMetrics.takeLast(3).zipWithNext { prev, curr ->
        curr.calculateGrowthRate(prev)
    }
    
    val avgGrowthRate = recentGrowthRates.average()
    
    when {
        avgGrowthRate > 0.2 -> insights.add("Excellent growth rate - above average")
        avgGrowthRate > 0.1 -> insights.add("Good steady growth progress")
        avgGrowthRate > 0 -> insights.add("Slow but consistent growth")
        else -> insights.add("Growth has plateaued - consider nutrition review")
    }
    
    // Weight consistency
    val weights = sortedMetrics.map { it.weight }
    val weightVariation = weights.maxOrNull()!! - weights.minOrNull()!!
    
    if (weightVariation > weights.average() * 0.5) {
        insights.add("High weight variation detected - monitor feeding consistency")
    }
    
    // Body condition analysis
    val latestCondition = sortedMetrics.last().bodyConditionScore
    when {
        latestCondition >= 8 -> insights.add("Excellent body condition maintained")
        latestCondition >= 6 -> insights.add("Good body condition")
        latestCondition >= 4 -> insights.add("Body condition needs improvement")
        else -> insights.add("Poor body condition - veterinary consultation recommended")
    }
    
    return insights
}