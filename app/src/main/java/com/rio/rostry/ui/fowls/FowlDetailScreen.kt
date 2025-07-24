package com.rio.rostry.ui.fowls

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rio.rostry.data.model.Fowl
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FowlDetailScreen(
    fowlId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: FowlDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(fowlId) {
        viewModel.loadFowl(fowlId)
    }
    
    LaunchedEffect(uiState.addedToCart) {
        if (uiState.addedToCart) {
            // Show snackbar or toast
            viewModel.clearAddedToCart()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(uiState.fowl?.name ?: "Fowl Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (uiState.isOwner) {
                    IconButton(
                        onClick = { 
                            uiState.fowl?.let { onNavigateToEdit(it.id) }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
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
            uiState.fowl?.let { fowl ->
                FowlDetailContent(
                    fowl = fowl,
                    isOwner = uiState.isOwner,
                    onAddToCart = { viewModel.addToCart(fowl.id) },
                    onNavigateToCart = onNavigateToCart,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Fowl not found")
                }
            }
        }
        
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun FowlDetailContent(
    fowl: Fowl,
    isOwner: Boolean,
    onAddToCart: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image
        AsyncImage(
            model = fowl.imageUrls.firstOrNull() ?: "https://via.placeholder.com/400",
            contentDescription = fowl.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )
        
        // Basic Info
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = fowl.breed,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fowl.type.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = when (fowl.gender) {
                            com.rio.rostry.data.model.FowlGender.MALE -> "Male"
                            com.rio.rostry.data.model.FowlGender.FEMALE -> "Female"
                            com.rio.rostry.data.model.FowlGender.UNKNOWN -> "Unknown"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (fowl.weight > 0) {
                    Text(
                        text = "Weight: ${fowl.weight} kg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (fowl.color.isNotEmpty()) {
                    Text(
                        text = "Color: ${fowl.color}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (fowl.location.isNotEmpty()) {
                    Text(
                        text = "Location: ${fowl.location}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Description
        if (fowl.description.isNotEmpty()) {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fowl.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Sale Information
        if (fowl.isForSale && !isOwner) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "For Sale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (fowl.price > 0) {
                        Text(
                            text = "$${fowl.price}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Cart")
                        }
                        
                        OutlinedButton(
                            onClick = onNavigateToCart,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Cart")
                        }
                    }
                }
            }
        }
        
        // Additional Info
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Added: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(fowl.createdAt))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (fowl.updatedAt != fowl.createdAt) {
                    Text(
                        text = "Updated: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(fowl.updatedAt))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
}