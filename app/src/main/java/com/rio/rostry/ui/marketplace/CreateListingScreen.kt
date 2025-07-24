package com.rio.rostry.ui.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.Fowl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    var selectedFowl by remember { mutableStateOf<Fowl?>(null) }
    var price by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Breeding Stock") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showFowlSelection by remember { mutableStateOf(true) }
    
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Load user's fowls
    LaunchedEffect(Unit) {
        viewModel.loadMarketplaceFowls()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Create Listing") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (!showFowlSelection) {
                    TextButton(
                        onClick = {
                            selectedFowl?.let { fowl ->
                                if (price.isNotBlank() && description.isNotBlank()) {
                                    viewModel.createListing(
                                        fowlId = fowl.id,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        purpose = purpose,
                                        description = description,
                                        location = location,
                                        onSuccess = onListingCreated
                                    )
                                }
                            }
                        },
                        enabled = !uiState.isLoading && selectedFowl != null && 
                                price.isNotBlank() && description.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Create")
                        }
                    }
                }
            }
        )
        
        if (showFowlSelection) {
            // Fowl Selection Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select a Fowl to List",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Choose from your owned fowls that are not currently for sale",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val ownedFowls = viewModel.getUserOwnedFowls()
                
                if (ownedFowls.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Available Fowls",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You don't have any fowls available for listing. Add fowls to your inventory first.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ownedFowls) { fowl ->
                            FowlSelectionCard(
                                fowl = fowl,
                                onSelect = {
                                    selectedFowl = fowl
                                    showFowlSelection = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Listing Details Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected Fowl Summary
                selectedFowl?.let { fowl ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = fowl.imageUrls.firstOrNull() ?: fowl.proofImageUrl,
                                contentDescription = "Fowl Image",
                                modifier = Modifier.size(60.dp),
                                contentScale = ContentScale.Crop
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = fowl.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${fowl.breed} • ${fowl.type.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (fowl.motherId != null || fowl.fatherId != null) {
                                    Text(
                                        text = "Traceable Bloodline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            TextButton(
                                onClick = { showFowlSelection = true }
                            ) {
                                Text("Change")
                            }
                        }
                    }
                }
                
                // Listing Details
                Text(
                    text = "Listing Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter selling price") }
                )
                
                // Purpose Dropdown
                var purposeExpanded by remember { mutableStateOf(false) }
                val purposeOptions = listOf("Breeding Stock", "Meat", "Eggs", "Show", "Pet")
                ExposedDropdownMenuBox(
                    expanded = purposeExpanded,
                    onExpandedChange = { purposeExpanded = !purposeExpanded }
                ) {
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Purpose") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purposeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = purposeExpanded,
                        onDismissRequest = { purposeExpanded = false }
                    ) {
                        purposeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    purpose = option
                                    purposeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Describe your fowl, its qualities, health status, etc.") }
                )
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("City, State") }
                )
                
                // Auto-populated Information
                selectedFowl?.let { fowl ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Auto-populated Information",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "The following information will be automatically included from your fowl profile:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "• Breed: ${fowl.breed}\n" +
                                        "• Type: ${fowl.type.name}\n" +
                                        "• Gender: ${fowl.gender.name}\n" +
                                        "• Current Status: ${fowl.status}\n" +
                                        if (fowl.motherId != null) "• Mother ID: ${fowl.motherId}\n" else "" +
                                        if (fowl.fatherId != null) "• Father ID: ${fowl.fatherId}\n" else "" +
                                        "• Health Records: Available\n" +
                                        "• Vaccination History: Available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Error Display
                uiState.error?.let { error ->
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
                
                // Guidelines
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Listing Guidelines",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Only list fowls you own and have in your inventory\n" +
                                    "• Provide accurate and honest descriptions\n" +
                                    "• Set fair and competitive prices\n" +
                                    "• Respond promptly to buyer inquiries\n" +
                                    "• Ensure fowls are healthy before listing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Bottom spacing
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FowlSelectionCard(
    fowl: Fowl,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fowl.imageUrls.firstOrNull() ?: fowl.proofImageUrl,
                contentDescription = "Fowl Image",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fowl.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${fowl.breed} • ${fowl.type.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Status: ${fowl.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (fowl.motherId != null || fowl.fatherId != null) {
                    Text(
                        text = "Traceable Bloodline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Icon(
                Icons.Default.Check,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}