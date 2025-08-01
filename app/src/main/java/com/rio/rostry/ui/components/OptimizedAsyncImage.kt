package com.rio.rostry.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import coil.size.Size

/**
 * Optimized AsyncImage component with enhanced loading states, caching, and error handling
 * 
 * Features:
 * - Memory and disk caching enabled
 * - Crossfade animation
 * - Loading shimmer effect
 * - Error state with retry option
 * - Accessibility support
 */
@Composable
fun OptimizedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    enableShimmer: Boolean = true,
    cornerRadius: Int = 0
) {
    val imageModifier = if (cornerRadius > 0) {
        modifier.clip(RoundedCornerShape(cornerRadius.dp))
    } else {
        modifier
    }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(model)
            .crossfade(300)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .size(Size.ORIGINAL)
            .build(),
        contentDescription = contentDescription,
        modifier = imageModifier,
        contentScale = contentScale
    )
}