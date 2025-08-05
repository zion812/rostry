package com.rio.rostry.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rio.rostry.data.model.FowlLifecycle
import com.rio.rostry.data.model.LifecycleStage
import com.rio.rostry.data.model.LifecycleMilestone
import com.rio.rostry.util.formatDate
import com.rio.rostry.util.formatDuration
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Comprehensive lifecycle timeline component with animations and progress tracking
 */
@Composable
fun LifecycleTimeline(
    lifecycle: FowlLifecycle,
    onStageClick: (LifecycleStage) -> Unit = {},
    onMilestoneClick: (LifecycleMilestone) -> Unit = {},
    modifier: Modifier = Modifier
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
            // Header with progress overview
            LifecycleHeader(
                lifecycle = lifecycle,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline stages
            LifecycleStage.values().forEachIndexed { index, stage ->
                LifecycleStageItem(
                    stage = stage,
                    lifecycle = lifecycle,
                    isCompleted = lifecycle.getCurrentStageEnum().ordinal > stage.ordinal,
                    isCurrent = lifecycle.getCurrentStageEnum() == stage,
                    milestone = parseMilestoneForStage(lifecycle, stage),
                    onStageClick = { onStageClick(stage) },
                    onMilestoneClick = onMilestoneClick
                )

                if (index < LifecycleStage.values().size - 1) {
                    LifecycleConnector(
                        isCompleted = lifecycle.getCurrentStageEnum().ordinal > stage.ordinal,
                        isActive = lifecycle.getCurrentStageEnum().ordinal >= stage.ordinal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress summary
            LifecycleProgressSummary(
                lifecycle = lifecycle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LifecycleHeader(
    lifecycle: FowlLifecycle,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lifecycle Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Current stage badge
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = lifecycle.getCurrentStageEnum().displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Overall progress bar
        OverallProgressBar(
            lifecycle = lifecycle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Started: ${formatDate(lifecycle.stageStartDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (lifecycle.expectedTransitionDate?.let { it > System.currentTimeMillis() } == true) {
                Text(
                    text = "Next stage: ${formatDate(lifecycle.expectedTransitionDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverallProgressBar(
    lifecycle: FowlLifecycle,
    modifier: Modifier = Modifier
) {
    val totalStages = LifecycleStage.values().size
    val completedStages = lifecycle.getCurrentStageEnum().ordinal + 1
    val progress = completedStages.toFloat() / totalStages.toFloat()

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "progress_animation"
    )

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${(progress * 100).toInt()}% Complete",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LifecycleStageItem(
    stage: LifecycleStage,
    lifecycle: FowlLifecycle,
    isCompleted: Boolean,
    isCurrent: Boolean,
    milestone: LifecycleMilestone?,
    onStageClick: () -> Unit,
    onMilestoneClick: (LifecycleMilestone) -> Unit
) {
    Card(
        onClick = onStageClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                isCompleted -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 4.dp else 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Stage indicator with animation
            StageIndicator(
                stage = stage,
                isCompleted = isCompleted,
                isCurrent = isCurrent
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Stage content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stage.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                            isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (stage.durationWeeks > 0) {
                        Text(
                            text = "${stage.durationWeeks} weeks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stage.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Current stage progress
                if (isCurrent && stage.durationWeeks > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StageProgressIndicator(
                        progress = lifecycle.getStageProgressCalculated(),
                        timeRemaining = calculateTimeRemaining(lifecycle)
                    )
                }

                // Milestone information
                milestone?.let { ms ->
                    Spacer(modifier = Modifier.height(8.dp))
                    MilestoneChip(
                        milestone = ms,
                        onClick = { onMilestoneClick(ms) }
                    )
                }

                // Key milestones for this stage
                if (stage.keyMilestones.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    KeyMilestones(
                        milestones = stage.keyMilestones,
                        isCompleted = isCompleted
                    )
                }
            }
        }
    }
}

@Composable
private fun StageIndicator(
    stage: LifecycleStage,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val icon = getStageIcon(stage)
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    // Pulsing animation for current stage
    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = stage.displayName,
                tint = if (isCurrent) MaterialTheme.colorScheme.onSecondary 
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StageProgressIndicator(
    progress: Float,
    timeRemaining: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stage Progress",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = timeRemaining,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MilestoneChip(
    milestone: LifecycleMilestone,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = milestone.description,
                style = MaterialTheme.typography.labelSmall
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (milestone.isOnSchedule) {
                    true -> Icons.Default.CheckCircle
                    false -> Icons.Default.Schedule
                    else -> Icons.Default.Help
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (milestone.isOnSchedule) {
                true -> 
                    MaterialTheme.colorScheme.primaryContainer
                false -> 
                    MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    )
}

@Composable
private fun KeyMilestones(
    milestones: List<String>,
    isCompleted: Boolean
) {
    Column {
        Text(
            text = "Key Milestones:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        milestones.take(3).forEach { milestone ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = milestone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LifecycleConnector(
    isCompleted: Boolean,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(
            modifier = Modifier
                .width(48.dp)
                .height(24.dp)
                .offset(x = 24.dp) // Align with stage indicators
        ) {
            val color = when {
                isCompleted -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                isActive -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                else -> androidx.compose.ui.graphics.Color(0xFFE0E0E0)
            }

            drawLine(
                color = color,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun LifecycleProgressSummary(
    lifecycle: FowlLifecycle,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                label = "Current Age",
                value = calculateAge(lifecycle),
                icon = Icons.Default.Schedule
            )
            
            SummaryItem(
                label = "Milestones",
                value = parseMilestoneCount(lifecycle).toString(),
                icon = Icons.Default.EmojiEvents
            )
            
            SummaryItem(
                label = "Growth Records",
                value = parseGrowthRecordCount(lifecycle).toString(),
                icon = Icons.Default.TrendingUp
            )
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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

// Helper functions
private fun getStageIcon(stage: LifecycleStage): ImageVector {
    return when (stage) {
        LifecycleStage.EGG -> Icons.Default.Circle
        LifecycleStage.HATCHING -> Icons.Default.Egg
        LifecycleStage.CHICK -> Icons.Default.Home
        LifecycleStage.JUVENILE -> Icons.Default.ArrowUpward
        LifecycleStage.ADULT -> Icons.Default.Star
        LifecycleStage.BREEDER_ACTIVE -> Icons.Default.Favorite
    }
}

private fun calculateTimeRemaining(lifecycle: FowlLifecycle): String {
    val currentTime = System.currentTimeMillis()
    val expectedEnd = lifecycle.expectedTransitionDate
    
    return if (expectedEnd != null && expectedEnd > currentTime) {
        val remainingMs = expectedEnd - currentTime
        formatDuration(remainingMs)
    } else {
        "Overdue"
    }
}

private fun calculateAge(lifecycle: FowlLifecycle): String {
    val ageMs = System.currentTimeMillis() - lifecycle.createdAt
    val ageDays = ageMs / (24 * 60 * 60 * 1000)
    
    return when {
        ageDays < 7 -> "${ageDays}d"
        ageDays < 30 -> "${ageDays / 7}w"
        else -> "${ageDays / 30}m"
    }
}

/**
 * Parse milestone for a specific stage from JSON data
 */
private fun parseMilestoneForStage(lifecycle: FowlLifecycle, stage: LifecycleStage): LifecycleMilestone? {
    return try {
        if (lifecycle.milestones.isNotEmpty()) {
            // Try to parse JSON milestones
            val json = Json { ignoreUnknownKeys = true }
            val milestones = json.decodeFromString<List<LifecycleMilestone>>(lifecycle.milestones)
            milestones.find { it.stage == stage.name }
        } else {
            // Create default milestone for current stage
            if (lifecycle.getCurrentStageEnum() == stage) {
                LifecycleMilestone(
                    id = "default_${stage.name}",
                    stage = stage.name,
                    description = "Stage ${stage.displayName} milestone",
                    expectedDate = lifecycle.expectedTransitionDate,
                    actualDate = System.currentTimeMillis(),
                    isOnSchedule = true
                )
            } else null
        }
    } catch (e: Exception) {
        // Fallback to default milestone if JSON parsing fails
        if (lifecycle.getCurrentStageEnum() == stage) {
            LifecycleMilestone(
                id = "fallback_${stage.name}",
                stage = stage.name,
                description = "Stage ${stage.displayName} in progress",
                expectedDate = lifecycle.expectedTransitionDate,
                actualDate = System.currentTimeMillis(),
                isOnSchedule = true
            )
        } else null
    }
}

/**
 * Parse milestone count from JSON data
 */
private fun parseMilestoneCount(lifecycle: FowlLifecycle): Int {
    return try {
        if (lifecycle.milestones.isNotEmpty()) {
            val json = Json { ignoreUnknownKeys = true }
            val milestones = json.decodeFromString<List<LifecycleMilestone>>(lifecycle.milestones)
            milestones.size
        } else {
            // Default milestone count based on current stage
            lifecycle.getCurrentStageEnum().ordinal + 1
        }
    } catch (e: Exception) {
        // Fallback count
        lifecycle.getCurrentStageEnum().ordinal + 1
    }
}

/**
 * Parse growth record count from JSON data
 */
private fun parseGrowthRecordCount(lifecycle: FowlLifecycle): Int {
    return try {
        if (lifecycle.growthRecords.isNotEmpty()) {
            val json = Json { ignoreUnknownKeys = true }
            // Assuming growth records are stored as a JSON array
            val records = json.decodeFromString<List<Map<String, Any>>>(lifecycle.growthRecords)
            records.size
        } else {
            // Default growth record count based on age
            val ageWeeks = (System.currentTimeMillis() - lifecycle.createdAt) / (7 * 24 * 60 * 60 * 1000)
            (ageWeeks / 2).toInt().coerceAtLeast(0) // Record every 2 weeks
        }
    } catch (e: Exception) {
        // Fallback count based on stage
        when (lifecycle.getCurrentStageEnum()) {
            LifecycleStage.EGG -> 0
            LifecycleStage.HATCHING -> 1
            LifecycleStage.CHICK -> 3
            LifecycleStage.JUVENILE -> 6
            LifecycleStage.ADULT -> 10
            LifecycleStage.BREEDER_ACTIVE -> 15
        }
    }
}