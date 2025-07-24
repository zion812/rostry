package com.rio.rostry.ui.showcase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rio.rostry.data.model.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToWallet: () -> Unit = {},
    viewModel: ShowcaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle success message
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            viewModel.clearSuccessMessage()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Showcase") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Coin balance display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Coins",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.coinBalance}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = { viewModel.refreshShowcase() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Showcase info card
                item {
                    ShowcaseInfoCard()
                }
                
                // Category selector
                item {
                    CategorySelectorCard(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::selectCategory
                    )
                }
                
                // Featured fowls in selected category
                item {
                    val fowlsInCategory = uiState.showcaseFowls[uiState.selectedCategory] ?: emptyList()
                    ShowcaseFowlsCard(
                        category = uiState.selectedCategory,
                        fowls = fowlsInCategory,
                        onFowlClick = onNavigateToFowlDetail
                    )
                }
                
                // User's fowls for showcase
                if (uiState.userFowls.isNotEmpty()) {
                    item {
                        UserFowlsForShowcaseCard(
                            fowls = uiState.userFowls,
                            onAddToShowcase = viewModel::selectFowlForShowcase
                        )
                    }
                }
                
                // Showcase benefits
                item {
                    ShowcaseBenefitsCard()
                }
            }
        }
    }
    
    // Showcase purchase dialog
    if (uiState.showShowcaseDialog) {
        uiState.selectedFowl?.let { fowl ->
            ShowcasePurchaseDialog(
                fowl = fowl,
                selectedDuration = uiState.selectedDuration,
                isPurchasing = uiState.isPurchasing,
                onDurationSelected = viewModel::selectShowcaseDuration,
                onPurchase = viewModel::purchaseShowcaseSlot,
                onDismiss = viewModel::dismissShowcaseDialog
            )
        }
    }
    
    // Insufficient coins dialog
    if (uiState.showInsufficientCoinsDialog) {
        InsufficientCoinsDialog(
            requiredCoins = uiState.selectedDuration.coinCost,
            currentCoins = uiState.coinBalance,
            onBuyCoins = {
                viewModel.dismissShowcaseDialog()
                onNavigateToWallet()
            },
            onDismiss = viewModel::dismissShowcaseDialog
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
private fun ShowcaseInfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Showcase",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Column {
                    Text(
                        text = "Premium Showcase",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "Feature your best fowls in premium categories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectorCard(
    selectedCategory: ShowcaseCategory,
    onCategorySelected: (ShowcaseCategory) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Showcase Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ShowcaseCategory.values()) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: ShowcaseCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category.name.replace("_", " "),
                style = MaterialTheme.typography.labelMedium
            )
        },
        selected = isSelected,
        leadingIcon = {
            Icon(
                when (category) {
                    ShowcaseCategory.BREEDING -> Icons.Default.Favorite
                    ShowcaseCategory.CHICKS -> Icons.Default.Star
                    ShowcaseCategory.LAYERS -> Icons.Default.Star
                    ShowcaseCategory.BROILERS -> Icons.Default.Star
                    ShowcaseCategory.RARE_BREEDS -> Icons.Default.Star
                    ShowcaseCategory.CHAMPIONS -> Icons.Default.Star
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
private fun ShowcaseFowlsCard(
    category: ShowcaseCategory,
    fowls: List<Fowl>,
    onFowlClick: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured ${category.name.replace("_", " ")}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (fowls.isNotEmpty()) {
                    TextButton(onClick = { /* Navigate to full category view */ }) {
                        Text("View All")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (fowls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No fowls in this category yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fowls.take(4)) { fowl ->
                        ShowcaseFowlItem(
                            fowl = fowl,
                            onClick = { onFowlClick(fowl.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseFowlItem(
    fowl: Fowl,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = fowl.imageUrls.firstOrNull() ?: "https://via.placeholder.com/150",
                contentDescription = fowl.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = fowl.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (fowl.isForSale) {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US).format(fowl.price),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UserFowlsForShowcaseCard(
    fowls: List<Fowl>,
    onAddToShowcase: (Fowl) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add Your Fowls to Showcase",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Boost visibility and attract more buyers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fowls.take(5)) { fowl ->
                    UserFowlShowcaseItem(
                        fowl = fowl,
                        onAddToShowcase = { onAddToShowcase(fowl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserFowlShowcaseItem(
    fowl: Fowl,
    onAddToShowcase: () -> Unit
) {
    Card(
        modifier = Modifier.width(140.dp)
    ) {
        Column {
            AsyncImage(
                model = fowl.imageUrls.firstOrNull() ?: "https://via.placeholder.com/140",
                contentDescription = fowl.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = fowl.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onAddToShowcase,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        text = "Showcase",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcaseBenefitsCard() {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Showcase Benefits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val benefits = listOf(
                "Premium placement in category",
                "Increased visibility to buyers",
                "Higher chance of sales",
                "Professional presentation",
                "Priority in search results"
            )
            
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (benefit != benefits.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ShowcasePurchaseDialog(
    fowl: Fowl,
    selectedDuration: ShowcaseDuration,
    isPurchasing: Boolean,
    onDurationSelected: (ShowcaseDuration) -> Unit,
    onPurchase: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Showcase") },
        text = {
            Column {
                // Fowl info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = fowl.imageUrls.firstOrNull() ?: "https://via.placeholder.com/60",
                        contentDescription = fowl.name,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Crop
                    )
                    
                    Column {
                        Text(
                            text = fowl.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = fowl.breed,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Select showcase duration:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Duration options
                ShowcaseDuration.values().forEach { duration ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDuration == duration,
                            onClick = { onDurationSelected(duration) }
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${duration.days} ${if (duration.days == 1) "Day" else "Days"}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Coins",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${duration.coinCost} coins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPurchase,
                enabled = !isPurchasing
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add to Showcase")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InsufficientCoinsDialog(
    requiredCoins: Int,
    currentCoins: Int,
    onBuyCoins: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insufficient Coins") },
        text = {
            Column {
                Text(
                    text = "You need $requiredCoins coins for this showcase placement, but you only have $currentCoins coins.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Would you like to purchase more coins?",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onBuyCoins) {
                Text("Buy Coins")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}