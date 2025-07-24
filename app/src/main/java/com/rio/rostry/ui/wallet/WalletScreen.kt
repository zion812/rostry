package com.rio.rostry.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.config.AppConfig
import com.rio.rostry.data.model.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
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
        // Top App Bar with Demo Mode indicator
        TopAppBar(
            title = { 
                Column {
                    Text("Wallet")
                    if (AppConfig.isDemoMode()) {
                        Text(
                            text = "Demo Mode - No Real Payments",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.refreshWallet() }) {
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
                // Demo mode notice
                if (AppConfig.isDemoMode()) {
                    item {
                        DemoModeNoticeCard()
                    }
                }
                
                // Wallet balance card
                item {
                    WalletBalanceCard(
                        wallet = uiState.wallet
                    )
                }
                
                // Coin packages
                item {
                    CoinPackagesCard(
                        coinPackages = uiState.coinPackages,
                        onPurchasePackage = viewModel::selectCoinPackage
                    )
                }
                
                // Transaction history
                if (uiState.transactions.isNotEmpty()) {
                    item {
                        TransactionHistoryCard(
                            transactions = uiState.transactions
                        )
                    }
                }
                
                // Coin usage guide
                item {
                    CoinUsageGuideCard()
                }
            }
        }
    }
    
    // Purchase confirmation dialog
    if (uiState.showPurchaseDialog) {
        uiState.selectedCoinPackage?.let { coinPackage ->
            PurchaseConfirmationDialog(
                coinPackage = coinPackage,
                isPurchasing = uiState.isPurchasing,
                onConfirm = viewModel::purchaseCoinPackage,
                onDismiss = viewModel::dismissPurchaseDialog
            )
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            viewModel.clearError()
        }
    }
}

@Composable
private fun DemoModeNoticeCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Demo Mode",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = "Demo Mode Active",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "All payments are simulated. No real money will be charged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun WalletBalanceCard(
    wallet: Wallet?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Wallet",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "${wallet?.coinBalance ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Coins",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Coins${AppConfig.getDemoModeLabel()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wallet stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WalletStat(
                    label = "Earned",
                    value = "${wallet?.totalCoinsEarned ?: 0}",
                    icon = Icons.Default.Add
                )
                
                WalletStat(
                    label = "Spent",
                    value = "${wallet?.totalCoinsSpent ?: 0}",
                    icon = Icons.Default.Delete
                )
            }
        }
    }
}

@Composable
private fun WalletStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun CoinPackagesCard(
    coinPackages: List<CoinPackage>,
    onPurchasePackage: (CoinPackage) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Buy Coins${AppConfig.getDemoModeLabel()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (AppConfig.isDemoMode()) {
                    "Purchase coin packages (simulated - no real charges)"
                } else {
                    "Purchase coin packages to use premium features"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(coinPackages) { coinPackage ->
                    CoinPackageItem(
                        coinPackage = coinPackage,
                        onPurchase = { onPurchasePackage(coinPackage) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinPackageItem(
    coinPackage: CoinPackage,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = if (coinPackage.isPopular) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (coinPackage.isPopular) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "POPULAR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = coinPackage.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Coins",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${coinPackage.totalCoins}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (coinPackage.bonusCoins > 0) {
                Text(
                    text = "+${coinPackage.bonusCoins} bonus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (AppConfig.isDemoMode()) {
                    "FREE (Demo)"
                } else {
                    NumberFormat.getCurrencyInstance(Locale.US).format(coinPackage.price)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = coinPackage.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (AppConfig.isDemoMode()) "Get (Demo)" else "Buy")
            }
        }
    }
}

@Composable
private fun TransactionHistoryCard(
    transactions: List<CoinTransaction>
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            transactions.take(10).forEach { transaction ->
                TransactionItem(transaction = transaction)
                
                if (transaction != transactions.take(10).last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            if (transactions.size > 10) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { /* Navigate to full history */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View All Transactions")
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: CoinTransaction
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                if (transaction.type == CoinTransactionType.CREDIT) {
                    Icons.Default.Add
                } else {
                    Icons.Default.Delete
                },
                contentDescription = transaction.type.name,
                tint = if (transaction.type == CoinTransactionType.CREDIT) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Text(
            text = "${if (transaction.type == CoinTransactionType.CREDIT) "+" else "-"}${transaction.amount}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (transaction.type == CoinTransactionType.CREDIT) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

@Composable
private fun CoinUsageGuideCard() {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "How to Use Coins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val usageItems = listOf(
                Triple(Icons.Default.CheckCircle, "Verification", "${AppConfig.CoinPricing.VERIFICATION_FEE} coins"),
                Triple(Icons.Default.ShoppingCart, "Marketplace Listing", "${AppConfig.CoinPricing.LISTING_FEE} coins"),
                Triple(Icons.Default.Star, "Featured Listing", "${AppConfig.CoinPricing.FEATURED_LISTING_FEE} coins"),
                Triple(Icons.Default.Star, "Showcase Placement", "${AppConfig.CoinPricing.SHOWCASE_FEE_WEEK} coins"),
                Triple(Icons.Default.Add, "Boost Listing", "${AppConfig.CoinPricing.BOOST_LISTING_FEE} coins")
            )
            
            usageItems.forEach { (icon, title, cost) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = title,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = cost,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (usageItems.indexOf(Triple(icon, title, cost)) != usageItems.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PurchaseConfirmationDialog(
    coinPackage: CoinPackage,
    isPurchasing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (AppConfig.isDemoMode()) "Simulate Purchase" else "Confirm Purchase") 
        },
        text = {
            Column {
                Text(
                    text = if (AppConfig.isDemoMode()) {
                        "You are about to simulate purchasing:"
                    } else {
                        "You are about to purchase:"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = coinPackage.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Coins",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${coinPackage.totalCoins} coins",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = if (AppConfig.isDemoMode()) {
                                "FREE (Demo Mode)"
                            } else {
                                NumberFormat.getCurrencyInstance(Locale.US).format(coinPackage.price)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (AppConfig.isDemoMode()) {
                        "This is a simulation. No real payment will be processed."
                    } else {
                        "This purchase will be charged to your Google Play account."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isPurchasing
            ) {
                if (isPurchasing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (AppConfig.isDemoMode()) "Simulate" else "Purchase")
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