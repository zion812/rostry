package com.rio.rostry.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.FamilyTreeData

/**
 * Interactive family tree visualization component with lineage tracking
 */
@Composable
fun FamilyTreeView(
    familyTreeData: FamilyTreeData,
    currentFowlName: String = "Current Fowl",
    onFowlClick: (String) -> Unit,
    onBreedingRecommendationClick: () -> Unit = {},
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
            // Header with family tree title and controls
            FamilyTreeHeader(
                familyTreeData = familyTreeData,
                onBreedingRecommendationClick = onBreedingRecommendationClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (familyTreeData.currentFowl != null) {
                // Family tree visualization
                FamilyTreeVisualization(
                    familyTreeData = familyTreeData,
                    currentFowlName = currentFowlName,
                    onFowlClick = onFowlClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Lineage information
                LineageInformation(
                    lineage = familyTreeData.currentFowl,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breeding compatibility insights
                BreedingCompatibilityInsights(
                    lineage = familyTreeData.currentFowl,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EmptyFamilyTree(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun FamilyTreeHeader(
    familyTreeData: FamilyTreeData,
    onBreedingRecommendationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Family Tree",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            familyTreeData.currentFowl?.let { lineage ->
                Text(
                    text = "Generation ${lineage.generation} • ${if (lineage.lineageVerified) "Verified" else "Unverified"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row {
            // Breeding recommendations button
            IconButton(
                onClick = onBreedingRecommendationClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Breeding Recommendations",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun FamilyTreeVisualization(
    familyTreeData: FamilyTreeData,
    currentFowlName: String,
    onFowlClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grandparents level (if available)
        val grandparents = familyTreeData.ancestors.filter { 
            it.generation == (familyTreeData.currentFowl?.generation ?: 1) - 2 
        }
        
        if (grandparents.isNotEmpty()) {
            Text(
                text = "Grandparents",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(grandparents) { grandparent ->
                    FamilyTreeNode(
                        fowlId = grandparent.fowlId,
                        label = "Grandparent",
                        generation = grandparent.generation,
                        isVerified = grandparent.lineageVerified,
                        onClick = { onFowlClick(grandparent.fowlId) },
                        nodeType = FamilyNodeType.GRANDPARENT
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection lines to parents
            FamilyConnectionLines(
                fromCount = grandparents.size,
                toCount = 2, // Assuming 2 parents
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }

        // Parents level
        val parents = listOfNotNull(
            familyTreeData.currentFowl?.parentMaleId,
            familyTreeData.currentFowl?.parentFemaleId
        )
        
        if (parents.isNotEmpty()) {
            Text(
                text = "Parents",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                familyTreeData.currentFowl?.parentMaleId?.let { parentId ->
                    FamilyTreeNode(
                        fowlId = parentId,
                        label = "Father",
                        generation = (familyTreeData.currentFowl?.generation ?: 1) - 1,
                        isVerified = true, // Assume verified for display
                        onClick = { onFowlClick(parentId) },
                        nodeType = FamilyNodeType.FATHER
                    )
                }

                familyTreeData.currentFowl?.parentFemaleId?.let { parentId ->
                    FamilyTreeNode(
                        fowlId = parentId,
                        label = "Mother",
                        generation = (familyTreeData.currentFowl?.generation ?: 1) - 1,
                        isVerified = true, // Assume verified for display
                        onClick = { onFowlClick(parentId) },
                        nodeType = FamilyNodeType.MOTHER
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection lines to current fowl
            FamilyConnectionLines(
                fromCount = parents.size,
                toCount = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
        }

        // Current fowl (highlighted)
        Text(
            text = "Current",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FamilyTreeNode(
            fowlId = familyTreeData.currentFowl?.fowlId ?: "",
            label = currentFowlName,
            generation = familyTreeData.currentFowl?.generation ?: 1,
            isVerified = familyTreeData.currentFowl?.lineageVerified ?: false,
            onClick = { familyTreeData.currentFowl?.fowlId?.let { onFowlClick(it) } },
            nodeType = FamilyNodeType.CURRENT,
            isHighlighted = true
        )

        // Offspring level
        val offspring = familyTreeData.descendants.filter { 
            it.generation == (familyTreeData.currentFowl?.generation ?: 1) + 1 
        }
        
        if (offspring.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection lines to offspring
            FamilyConnectionLines(
                fromCount = 1,
                toCount = offspring.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )
            
            Text(
                text = "Offspring (${offspring.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(offspring) { child ->
                    FamilyTreeNode(
                        fowlId = child.fowlId,
                        label = "Offspring",
                        generation = child.generation,
                        isVerified = child.lineageVerified,
                        onClick = { onFowlClick(child.fowlId) },
                        nodeType = FamilyNodeType.OFFSPRING
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyTreeNode(
    fowlId: String,
    label: String,
    generation: Int,
    isVerified: Boolean,
    onClick: () -> Unit,
    nodeType: FamilyNodeType,
    isHighlighted: Boolean = false
) {
    // Animation for node appearance
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_scale"
    )

    val containerColor = when (nodeType) {
        FamilyNodeType.GRANDPARENT -> MaterialTheme.colorScheme.primaryContainer
        FamilyNodeType.FATHER -> MaterialTheme.colorScheme.secondaryContainer
        FamilyNodeType.MOTHER -> MaterialTheme.colorScheme.secondaryContainer
        FamilyNodeType.CURRENT -> MaterialTheme.colorScheme.tertiaryContainer
        FamilyNodeType.OFFSPRING -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 8.dp else 4.dp
        ),
        modifier = Modifier
            .size(if (nodeType == FamilyNodeType.CURRENT) 100.dp else 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Node icon with verification indicator
            Box {
                Icon(
                    imageVector = getNodeIcon(nodeType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (nodeType == FamilyNodeType.CURRENT) 32.dp else 24.dp)
                )
                
                if (isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = if (nodeType == FamilyNodeType.CURRENT) 
                    MaterialTheme.typography.labelMedium 
                else 
                    MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            )

            if (nodeType != FamilyNodeType.CURRENT) {
                Text(
                    text = "Gen $generation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FamilyConnectionLines(
    fromCount: Int,
    toCount: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawConnectionLines(fromCount, toCount, size.width, size.height)
    }
}

private fun DrawScope.drawConnectionLines(
    fromCount: Int,
    toCount: Int,
    width: Float,
    height: Float
) {
    val lineColor = Color(0xFF9E9E9E)
    val strokeWidth = 2.dp.toPx()
    
    // Simple connection lines - can be enhanced with more complex routing
    if (fromCount == 1 && toCount > 1) {
        // One to many (current fowl to offspring)
        val startX = width / 2
        val startY = 0f
        val endY = height
        
        // Vertical line down
        drawLine(
            color = lineColor,
            start = Offset(startX, startY),
            end = Offset(startX, height / 2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // Horizontal distribution line
        val spacing = width / (toCount + 1)
        for (i in 1..toCount) {
            val endX = spacing * i
            drawLine(
                color = lineColor,
                start = Offset(startX, height / 2),
                end = Offset(endX, height / 2),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = lineColor,
                start = Offset(endX, height / 2),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    } else if (fromCount > 1 && toCount == 1) {
        // Many to one (parents to current fowl)
        val endX = width / 2
        val endY = height
        
        val spacing = width / (fromCount + 1)
        for (i in 1..fromCount) {
            val startX = spacing * i
            drawLine(
                color = lineColor,
                start = Offset(startX, 0f),
                end = Offset(startX, height / 2),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = lineColor,
                start = Offset(startX, height / 2),
                end = Offset(endX, height / 2),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        
        drawLine(
            color = lineColor,
            start = Offset(endX, height / 2),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun LineageInformation(
    lineage: FowlLineage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Lineage Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LineageInfoItem(
                    label = "Generation",
                    value = lineage.generation.toString(),
                    icon = Icons.Default.Timeline
                )

                LineageInfoItem(
                    label = "Bloodline",
                    value = if (lineage.bloodlineId.isNotEmpty()) "Established" else "New",
                    icon = Icons.Default.Favorite
                )

                LineageInfoItem(
                    label = "Inbreeding",
                    value = "${(lineage.inbreedingCoefficient * 100).toInt()}%",
                    icon = Icons.Default.Warning,
                    isWarning = lineage.inbreedingCoefficient > 0.25
                )

                LineageInfoItem(
                    label = "Offspring",
                    value = lineage.offspringIds.size.toString(),
                    icon = Icons.Default.Group
                )
            }
        }
    }
}

@Composable
private fun LineageInfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    isWarning: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isWarning) MaterialTheme.colorScheme.error 
                  else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isWarning) MaterialTheme.colorScheme.error 
                   else MaterialTheme.colorScheme.onSurface
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
private fun BreedingCompatibilityInsights(
    lineage: FowlLineage,
    modifier: Modifier = Modifier
) {
    val insights = generateBreedingInsights(lineage)
    
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
                        text = "Breeding Insights",
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
private fun EmptyFamilyTree(
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
                imageVector = Icons.Default.AccountTree,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No family tree data available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add parent information to build the family tree",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// Helper functions and enums
private enum class FamilyNodeType {
    GRANDPARENT, FATHER, MOTHER, CURRENT, OFFSPRING
}

private fun getNodeIcon(nodeType: FamilyNodeType): ImageVector {
    return when (nodeType) {
        FamilyNodeType.GRANDPARENT -> Icons.Default.Person
        FamilyNodeType.FATHER -> Icons.Default.Person
        FamilyNodeType.MOTHER -> Icons.Default.Person
        FamilyNodeType.CURRENT -> Icons.Default.Home
        FamilyNodeType.OFFSPRING -> Icons.Default.PersonAdd
    }
}

private fun generateBreedingInsights(lineage: FowlLineage): List<String> {
    val insights = mutableListOf<String>()
    
    // Breeding potential assessment
    if (lineage.hasBreedingPotential()) {
        insights.add("Excellent breeding potential with good genetic diversity")
    } else {
        insights.add("Limited breeding potential - consider genetic diversification")
    }
    
    // Inbreeding warnings
    when {
        lineage.inbreedingCoefficient > 0.25 -> 
            insights.add("High inbreeding risk - avoid breeding with related fowls")
        lineage.inbreedingCoefficient > 0.125 -> 
            insights.add("Moderate inbreeding risk - careful mate selection recommended")
        else -> 
            insights.add("Low inbreeding risk - good genetic diversity")
    }
    
    // Generation insights
    when {
        lineage.generation == 1 -> 
            insights.add("Foundation generation - excellent for establishing bloodlines")
        lineage.generation <= 3 -> 
            insights.add("Early generation - good for selective breeding programs")
        lineage.generation <= 5 -> 
            insights.add("Established lineage - proven genetic line")
        else -> 
            insights.add("Advanced generation - consider outcrossing for genetic vigor")
    }
    
    // Offspring insights
    when {
        lineage.offspringIds.size > 10 -> 
            insights.add("Proven breeder with multiple successful offspring")
        lineage.offspringIds.size > 5 -> 
            insights.add("Good breeding record with several offspring")
        lineage.offspringIds.size > 0 -> 
            insights.add("Has produced offspring - breeding capability confirmed")
        else -> 
            insights.add("No recorded offspring yet")
    }
    
    return insights
}