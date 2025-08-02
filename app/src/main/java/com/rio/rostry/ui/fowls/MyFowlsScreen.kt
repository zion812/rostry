package com.rio.rostry.ui.fowls

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.ui.components.ShimmerComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFowlsScreen(
    onNavigateToAddFowl: () -> Unit,
    onNavigateToFowlDetail: (String) -> Unit,
    onNavigateToEditFowl: (String) -> Unit,
    viewModel: MyFowlsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Fowls",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = onNavigateToAddFowl,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Fowl"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enhanced Content with better layout
        if (uiState.isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(6) { // Show 6 shimmer cards while loading
                    ShimmerComponents.ShimmerFowlCard()
                }
            }
        } else if (uiState.fowls.isEmpty()) {
            EmptyFowlsState(onAddFowlClick = onNavigateToAddFowl)
        } else {
            // Statistics summary
            FowlStatsSummary(
                totalFowls = uiState.fowls.size,
                forSale = uiState.fowls.count { it.isForSale },
                breeders = uiState.fowls.count { it.status.contains("Breeder", ignoreCase = true) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = uiState.fowls,
                    key = { fowl -> fowl.id }
                ) { fowl ->
                    MyFowlCard(
                        fowl = fowl,
                        onFowlClick = { onNavigateToFowlDetail(fowl.id) },
                        onEditClick = { onNavigateToEditFowl(fowl.id) },
                        onDeleteClick = { viewModel.deleteFowl(fowl.id) },
                        onToggleForSale = { viewModel.toggleForSale(fowl) }
                    )
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or handle error
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun FowlStatsSummary(
    totalFowls: Int,
    forSale: Int,
    breeders: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total",
            value = totalFowls.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "For Sale",
            value = forSale.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Breeders",
            value = breeders.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyFowlsState(
    onAddFowlClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No fowls yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start building your flock by adding your first fowl",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddFowlClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Fowl")
        }
    }
}