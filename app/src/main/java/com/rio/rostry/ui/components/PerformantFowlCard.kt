package com.rio.rostry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Size
import com.rio.rostry.data.local.dao.OptimizedFowlDao.FowlLight

/**
 * Performance-optimized fowl card component
 * - Uses lightweight data model
 * - Optimized image loading with proper sizing
 * - Minimal recomposition
 * - Memory-efficient rendering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformantFowlCard(
    fowl: FowlLight,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true
) {
    // Stable references to prevent recomposition
    val stableOnClick = remember(fowl.id) { { onClick(fowl.id) } }
    
    Card(
        onClick = stableOnClick,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Optimized image loading
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fowl.imageUrl)
                    .size(Size(300, 120)) // Fixed size for better performance
                    .crossfade(200)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Image of ${fowl.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Breed
                Text(
                    text = fowl.breed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Price and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showPrice && fowl.isForSale) {
                        Text(
                            text = "₱${fowl.price}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (fowl.isForSale) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "For Sale",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lazy loading grid for fowl cards with pagination
 */
@Composable
fun PerformantFowlGrid(
    fowls: List<FowlLight>,
    onFowlClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showPrice: Boolean = true,
    isLoading: Boolean = false,
    onLoadMore: () -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = fowls,
            key = { it.id } // Stable key for better performance
        ) { fowl ->
            PerformantFowlCard(
                fowl = fowl,
                onClick = onFowlClick,
                showPrice = showPrice
            )
        }
        
        // Loading indicator
        if (isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        // Load more trigger
        if (fowls.isNotEmpty() && !isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }
    }
}
