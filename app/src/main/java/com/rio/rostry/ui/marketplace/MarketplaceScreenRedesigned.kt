package com.rio.rostry.ui.marketplace

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rio.rostry.data.model.*
import com.rio.rostry.ui.components.*
import com.rio.rostry.ui.theme.*
import com.rio.rostry.ui.marketplace.*
import com.rio.rostry.data.model.Fowl

/**
 * Redesigned Marketplace Screen with enhanced UX, visual hierarchy, and engagement features
 * Features responsive grid layout, improved search, and social commerce elements
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreenRedesigned(
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    LaunchedEffect(Unit) {
        viewModel.loadMarketplaceData()
    }

    Scaffold(
        topBar = {
            MarketplaceTopBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearchClick = onNavigateToSearch,
                onCartClick = onNavigateToCart,
                cartItemCount = uiState.cartItemCount
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.fowls.isEmpty() -> {
                MarketplaceLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.error != null -> {
                MarketplaceErrorState(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.refreshData() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                MarketplaceContent(
                    uiState = uiState,
                    isTablet = isTablet,
                    onNavigateToFowlDetail = onNavigateToFowlDetail,
                    onNavigateToCategories = onNavigateToCategories,
                    onNavigateToProfile = onNavigateToProfile,
                    onCategoryClick = viewModel::selectCategory,
                    onFilterToggle = viewModel::toggleFilter,
                    onFowlAction = { fowlId, action -> viewModel.handleFowlAction(fowlId, action) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int
) {
    TopAppBar(
        title = {
            Text(
                text = "Marketplace",
                style = RostryTypography.sectionHeader()
            )
        },
        actions = {
            // Search Action
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search fowls"
                )
            }
            
            // Cart Action with Badge
            BadgedBox(
                badge = {
                    if (cartItemCount > 0) {
                        Badge(
                            containerColor = RostrySemanticColors.criticalRed
                        ) {
                            Text(
                                text = cartItemCount.toString(),
                                color = Color.White,
                                style = RostryTypography.statusLabel()
                            )
                        }
                    }
                }
            ) {
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Shopping cart"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun MarketplaceContent(
    uiState: MarketplaceUiState,
    isTablet: Boolean,
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onFilterToggle: (String) -> Unit,
    onFowlAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(RostrySpacing.md),
        verticalArrangement = Arrangement.spacedBy(RostrySpacing.lg)
    ) {
        // Featured Categories Section
        item {
            FeaturedCategoriesSection(
                categories = uiState.featuredCategories,
                onCategoryClick = onCategoryClick,
                onViewAllClick = onNavigateToCategories
            )
        }

        // Quick Filters
        item {
            QuickFiltersSection(
                selectedFilters = uiState.selectedFilters,
                availableFilters = uiState.availableFilters,
                onFilterToggle = onFilterToggle
            )
        }

        // Featured Fowls
        if (uiState.featuredFowls.isNotEmpty()) {
            item {
                FeaturedFowlsSection(
                    fowls = uiState.featuredFowls,
                    onFowlClick = onNavigateToFowlDetail,
                    onFowlAction = onFowlAction
                )
            }
        }

        // All Fowls Grid
        item {
            AllFowlsSection(
                fowls = uiState.fowls,
                isTablet = isTablet,
                isLoading = uiState.isLoading,
                onFowlClick = onNavigateToFowlDetail,
                onFowlAction = onFowlAction,
                onSellerClick = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun FeaturedCategoriesSection(
    categories: List<FowlCategory>,
    onCategoryClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RostrySectionHeader(
            title = "Categories",
            subtitle = "Browse by fowl type",
            action = {
                TextButton(onClick = onViewAllClick) {
                    Text("View All")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(RostrySpacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(RostrySpacing.md),
            contentPadding = PaddingValues(horizontal = RostrySpacing.xs)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: FowlCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(120.dp),
        shape = RostryShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(RostrySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RostrySpacing.sm)
        ) {
            // Category Icon/Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = category.name,
                style = RostryTypography.statusLabel(),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${category.count} fowls",
                style = RostryTypography.caption(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickFiltersSection(
    selectedFilters: List<String>,
    availableFilters: List<MarketplaceFilter>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Filters",
            style = RostryTypography.cardTitle(),
            modifier = Modifier.padding(bottom = RostrySpacing.sm)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(RostrySpacing.sm),
            contentPadding = PaddingValues(horizontal = RostrySpacing.xs)
        ) {
            items(availableFilters) { filter ->
                FilterChip(
                    selected = selectedFilters.contains(filter.id),
                    onClick = { onFilterToggle(filter.id) },
                    label = { Text(filter.name) },
                    leadingIcon = if (selectedFilters.contains(filter.id)) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    shape = RostryShapes.small
                )
            }
        }
    }
}

@Composable
private fun FeaturedFowlsSection(
    fowls: List<Fowl>,
    onFowlClick: (String) -> Unit,
    onFowlAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RostrySectionHeader(
            title = "Featured Fowls",
            subtitle = "Handpicked selections"
        )

        Spacer(modifier = Modifier.height(RostrySpacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(RostrySpacing.md),
            contentPadding = PaddingValues(horizontal = RostrySpacing.xs)
        ) {
            items(fowls) { fowl ->
                FeaturedFowlCard(
                    fowl = fowl,
                    onClick = { onFowlClick(fowl.id) },
                    onFavoriteClick = { onFowlAction(fowl.id, "toggle_favorite") },
                    onAddToCart = { onFowlAction(fowl.id, "add_to_cart") },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedFowlCard(
    fowl: Fowl,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RostryShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = RostryElevation.medium)
    ) {
        Column {
            // Image with overlay actions
            Box {
                AsyncImage(
                    model = fowl.imageUrls.firstOrNull() ?: "",
                    contentDescription = fowl.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )

                // Featured Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(RostrySpacing.sm),
                    shape = RostryShapes.small,
                    color = RostrySemanticColors.featured
                ) {
                    Text(
                        text = "Featured",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = RostryTypography.statusLabel(),
                        color = Color.White
                    )
                }

                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(RostrySpacing.sm)
                ) {
                    Icon(
                        imageVector = if (false) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (false) "Remove from favorites" else "Add to favorites",
                        tint = if (false) RostrySemanticColors.criticalRed else Color.White
                    )
                }

                // Status Badge
                RostryStatusChip(
                    status = fowl.status,
                    type = when (fowl.status) {
                        "Available" -> StatusType.SUCCESS
                        "Reserved" -> StatusType.WARNING
                        "Sold" -> StatusType.NEUTRAL
                        else -> StatusType.INFO
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(RostrySpacing.sm)
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.sm)
            ) {
                Text(
                    text = fowl.name,
                    style = RostryTypography.cardTitle(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = fowl.breed,
                    style = RostryTypography.bodyText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Rating and Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = RostrySemanticColors.warningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = 4.5f.toString(),
                            style = RostryTypography.statusLabel()
                        )
                    }

                    Text(
                        text = fowl.location,
                        style = RostryTypography.caption(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price and Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₦${fowl.price}",
                        style = RostryTypography.metricValue(),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (fowl.status == "Available") {
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = "Add to cart",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllFowlsSection(
    fowls: List<Fowl>,
    isTablet: Boolean,
    isLoading: Boolean,
    onFowlClick: (String) -> Unit,
    onFowlAction: (String, String) -> Unit,
    onSellerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RostrySectionHeader(
            title = "All Fowls",
            subtitle = "${fowls.size} fowls available"
        )

        Spacer(modifier = Modifier.height(RostrySpacing.md))

        if (isLoading && fowls.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isTablet) 200.dp else 160.dp),
                horizontalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                modifier = Modifier.height(400.dp)
            ) {
                items(6) {
                    ShimmerComponents.ShimmerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }
            }
        } else if (fowls.isEmpty()) {
            RostryEmptyState(
                title = "No Fowls Available",
                description = "Check back later for new listings",
                icon = Icons.Default.Pets
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isTablet) 200.dp else 160.dp),
                horizontalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                modifier = Modifier.height(600.dp) // Fixed height for nested scrolling
            ) {
                items(fowls) { fowl ->
                    EnhancedFowlCard(
                        fowl = fowl,
                        onFowlClick = { onFowlClick(fowl.id) },
                        onFavoriteClick = { onFowlAction(fowl.id, "toggle_favorite") },
                        onAddToCart = { onFowlAction(fowl.id, "add_to_cart") },
                        onSellerClick = { onSellerClick(fowl.ownerId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedFowlCard(
    fowl: Fowl,
    onFowlClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit,
    onSellerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onFowlClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${fowl.name}, ${fowl.breed}, ${fowl.price} Naira"
            },
        shape = RostryShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = RostryElevation.small)
    ) {
        Column {
            // Image with overlay actions
            Box {
                AsyncImage(
                    model = fowl.imageUrls.firstOrNull() ?: "",
                    contentDescription = fowl.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )

                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(RostrySpacing.sm)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (false) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (false) "Remove from favorites" else "Add to favorites",
                        tint = if (false) RostrySemanticColors.criticalRed else Color.White
                    )
                }

                // Status Badge
                RostryStatusChip(
                    status = fowl.status,
                    type = when (fowl.status) {
                        "Available" -> StatusType.SUCCESS
                        "Reserved" -> StatusType.WARNING
                        "Sold" -> StatusType.NEUTRAL
                        else -> StatusType.INFO
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(RostrySpacing.sm)
                )
            }

            // Content
            Column(
                modifier = Modifier.padding(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.xs)
            ) {
                Text(
                    text = fowl.name,
                    style = RostryTypography.cardTitle(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = fowl.breed,
                    style = RostryTypography.bodyText(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Seller info (clickable)
                TextButton(
                    onClick = onSellerClick,
                    modifier = Modifier.padding(0.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Owner",
                            style = RostryTypography.caption()
                        )
                    }
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = RostrySemanticColors.warningAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = 4.5f.toString(),
                        style = RostryTypography.caption()
                    )
                    Text(
                        text = "• ${fowl.location}",
                        style = RostryTypography.caption(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price and Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₦${fowl.price}",
                        style = RostryTypography.cardTitle(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (fowl.status == "Available") {
                        IconButton(
                            onClick = onAddToCart,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = "Add to cart",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(RostrySpacing.md),
        verticalArrangement = Arrangement.spacedBy(RostrySpacing.lg)
    ) {
        // Categories loading
        item {
            ShimmerComponents.ShimmerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        // Grid loading
        item {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                verticalArrangement = Arrangement.spacedBy(RostrySpacing.md),
                modifier = Modifier.height(600.dp)
            ) {
                items(6) {
                    ShimmerComponents.ShimmerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(RostrySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RostryAlertCard(
            title = "Error Loading Marketplace",
            message = error,
            severity = com.rio.rostry.ui.components.AlertSeverity.ERROR,
            onAction = onRetry,
            actionText = "Retry"
        )
    }
}

