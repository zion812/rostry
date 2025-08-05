package com.rio.rostry.ui.marketplace.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rio.rostry.data.model.MarketplaceListing

/**
 * Enhanced marketplace listing card with conditional lineage information display
 * Only shows lineage information when hasTraceableLineage is true
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingCard(
    listing: MarketplaceListing,
    onListingClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onListingClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Featured Image
            AsyncImage(
                model = listing.featuredImageUrl.takeIf { it.isNotBlank() } 
                    ?: "https://via.placeholder.com/300x200",
                contentDescription = listing.fowlName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            // Content
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with name and price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.fowlName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${listing.fowlBreed} • ${listing.fowlType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "$${listing.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Basic fowl information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.Cake,
                        text = listing.fowlAge
                    )
                    InfoChip(
                        icon = if (listing.fowlGender == "MALE") Icons.Default.Male else Icons.Default.Female,
                        text = listing.fowlGender
                    )
                    if (listing.isBreederReady) {
                        InfoChip(
                            icon = Icons.Default.Star,
                            text = "Breeder Ready",
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
                
                // Purpose and location
                Text(
                    text = "Purpose: ${listing.purpose}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Location: ${listing.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Description
                if (listing.description.isNotBlank()) {
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // ENHANCED: Lineage information section - only show if traceable
                if (listing.hasTraceableLineage) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LineageInfoSection(listing = listing)
                }
                
                // Seller information
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seller: ${listing.sellerName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        if (listing.sellerRating > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${listing.sellerRating}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = onAddToCart,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}

/**
 * Enhanced lineage information section
 * Only displays when listing has traceable lineage
 */
@Composable
private fun LineageInfoSection(
    listing: MarketplaceListing,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Lineage header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Verified,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Traceable Lineage",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            if (listing.lineageVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            "Verified",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
        
        // Lineage details in a grid
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listing.motherId?.let { motherId ->
                LineageDetailRow(
                    label = "Mother ID:",
                    value = motherId
                )
            }
            
            listing.fatherId?.let { fatherId ->
                LineageDetailRow(
                    label = "Father ID:",
                    value = fatherId
                )
            }
            
            listing.generation?.let { generation ->
                LineageDetailRow(
                    label = "Generation:",
                    value = generation.toString()
                )
            }
            
            listing.bloodlineId?.let { bloodlineId ->
                LineageDetailRow(
                    label = "Bloodline:",
                    value = bloodlineId
                )
            }
            
            if (listing.lineageNotes.isNotBlank()) {
                LineageDetailRow(
                    label = "Notes:",
                    value = listing.lineageNotes,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun LineageDetailRow(
    label: String,
    value: String,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text,
                style = MaterialTheme.typography.labelSmall
            ) 
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        ),
        modifier = modifier
    )
}
