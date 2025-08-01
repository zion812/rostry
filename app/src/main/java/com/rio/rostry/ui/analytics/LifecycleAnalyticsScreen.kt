package com.rio.rostry.ui.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.LifecycleAnalytics
import com.rio.rostry.data.repository.BloodlineAnalytics

/**
 * Comprehensive lifecycle analytics dashboard with interactive charts and insights
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleAnalyticsScreen(
    viewModel: LifecycleAnalyticsViewModel = hiltViewModel(),
    onNavigateToFowlDetail: (String) -> Unit = {},
    onNavigateToBloodlineDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalyticsData()
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            ErrorScreen(
                error = uiState.error!!,
                onRetry = { viewModel.loadAnalyticsData() }
            )
        }
        else -> {
            AnalyticsContent(
                analytics = uiState.analytics,
                bloodlineAnalytics = uiState.bloodlineAnalytics,
                onNavigateToFowlDetail = onNavigateToFowlDetail,
                onNavigateToBloodlineDetail = onNavigateToBloodlineDetail
            )
        }
    }
}

@Composable
private fun AnalyticsContent(
    analytics: LifecycleAnalytics?,
    bloodlineAnalytics: List<BloodlineAnalytics>,
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToBloodlineDetail: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Lifecycle Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        analytics?.let { data ->
            item {
                AnalyticsOverviewCard(
                    totalFowls = data.totalFowls,
                    activeBreeders = data.activeBreeders,
                    averageGrowthRate = data.averageGrowthRate,
                    survivalRate = data.survivalRate
                )
            }

            item {
                StageDistributionChart(
                    stageDistribution = data.stageDistribution
                )
            }

            item {
                BloodlinePerformanceCard(
                    bloodlineAnalytics = bloodlineAnalytics,
                    onBloodlineClick = onNavigateToBloodlineDetail
                )
            }

            item {
                PerformanceInsights(
                    analytics = data,
                    bloodlineAnalytics = bloodlineAnalytics
                )
            }
        }
    }
}

@Composable
private fun AnalyticsOverviewCard(
    totalFowls: Int,
    activeBreeders: Int,
    averageGrowthRate: Double,
    survivalRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnalyticsMetric(
                    label = "Total Fowls",
                    value = totalFowls.toString(),
                    icon = Icons.Default.Home,
                    color = MaterialTheme.colorScheme.primary
                )

                AnalyticsMetric(
                    label = "Active Breeders",
                    value = activeBreeders.toString(),
                    icon = Icons.Default.Favorite,
                    color = MaterialTheme.colorScheme.secondary
                )

                AnalyticsMetric(
                    label = "Growth Rate",
                    value = "${String.format("%.1f", averageGrowthRate)}%",
                    icon = Icons.Default.ArrowUpward,
                    color = MaterialTheme.colorScheme.tertiary
                )

                AnalyticsMetric(
                    label = "Survival Rate",
                    value = "${String.format("%.1f", survivalRate)}%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StageDistributionChart(
    stageDistribution: Map<LifecycleStage, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Stage Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pie chart
                PieChart(
                    data = stageDistribution,
                    modifier = Modifier
                        .size(150.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stageDistribution.entries.forEachIndexed { index, (stage, count) ->
                        LegendItem(
                            label = stage.displayName,
                            count = count,
                            color = getStageColor(index),
                            percentage = if (stageDistribution.values.sum() > 0) {
                                (count.toFloat() / stageDistribution.values.sum() * 100).toInt()
                            } else 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChart(
    data: Map<LifecycleStage, Int>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().toFloat()
    if (total == 0f) return

    // Animation for pie chart
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "pie_animation"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        var startAngle = -90f

        data.entries.forEachIndexed { index, (_, count) ->
            val sweepAngle = (count / total * 360f) * animationProgress

            drawArc(
                color = getStageColor(index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )

            startAngle += sweepAngle
        }

        // Draw center circle for donut effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.4f,
            center = center
        )
    }
}

@Composable
private fun LegendItem(
    label: String,
    count: Int,
    color: Color,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count fowls ($percentage%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BloodlinePerformanceCard(
    bloodlineAnalytics: List<BloodlineAnalytics>,
    onBloodlineClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Bloodline Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (bloodlineAnalytics.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(bloodlineAnalytics.take(5)) { analytics ->
                        BloodlineCard(
                            analytics = analytics,
                            onClick = { onBloodlineClick(analytics.bloodline.id) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No bloodline data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BloodlineCard(
    analytics: BloodlineAnalytics,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = analytics.bloodline.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = getPerformanceColor(analytics.performanceRating),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = analytics.performanceRating.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = getPerformanceColor(analytics.performanceRating)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Gen ${analytics.bloodline.totalGenerations}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${analytics.bloodline.activeBreeders} active",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (analytics.needsDiversification) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Needs diversification",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PerformanceInsights(
    analytics: LifecycleAnalytics,
    bloodlineAnalytics: List<BloodlineAnalytics>
) {
    val insights = generatePerformanceInsights(analytics, bloodlineAnalytics)

    if (insights.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Performance Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                insights.forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading analytics",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

// Helper functions
private fun getStageColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[index % colors.size]
}

private fun getPerformanceColor(rating: PerformanceRating): Color {
    return Color(android.graphics.Color.parseColor(rating.color))
}

private fun generatePerformanceInsights(
    analytics: LifecycleAnalytics,
    bloodlineAnalytics: List<BloodlineAnalytics>
): List<String> {
    val insights = mutableListOf<String>()

    // Overall performance insights
    when {
        analytics.survivalRate > 90 -> insights.add("Excellent survival rate indicates optimal care conditions")
        analytics.survivalRate > 80 -> insights.add("Good survival rate with room for improvement")
        else -> insights.add("Survival rate needs attention - review care protocols")
    }

    // Growth insights
    when {
        analytics.averageGrowthRate > 0.15 -> insights.add("Outstanding growth rates across the flock")
        analytics.averageGrowthRate > 0.1 -> insights.add("Healthy growth rates maintained")
        else -> insights.add("Growth rates below optimal - consider nutrition review")
    }

    // Breeding insights
    val breedingRate = if (analytics.totalFowls > 0) {
        (analytics.activeBreeders.toDouble() / analytics.totalFowls) * 100
    } else 0.0

    when {
        breedingRate > 30 -> insights.add("High breeding activity indicates healthy mature population")
        breedingRate > 20 -> insights.add("Good breeding potential in the flock")
        else -> insights.add("Consider expanding breeding program for genetic diversity")
    }

    // Bloodline insights
    val needsDiversification = bloodlineAnalytics.count { it.needsDiversification }
    if (needsDiversification > 0) {
        insights.add("$needsDiversification bloodlines need genetic diversification")
    }

    val excellentBloodlines = bloodlineAnalytics.count {
        it.performanceRating == PerformanceRating.OUTSTANDING
    }
    if (excellentBloodlines > 0) {
        insights.add("$excellentBloodlines bloodlines showing exceptional performance")
    }

    return insights
}