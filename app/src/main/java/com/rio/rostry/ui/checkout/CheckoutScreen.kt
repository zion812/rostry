package com.rio.rostry.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rio.rostry.data.model.Location
import com.rio.rostry.data.model.Order
import com.rio.rostry.data.model.OrderSummary
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    fowlId: String,
    quantity: Int,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    LaunchedEffect(fowlId, quantity) {
        viewModel.loadFowlDetails(fowlId, quantity)
    }
    
    LaunchedEffect(uiState.orderPlaced) {
        if (uiState.orderPlaced) {
            uiState.orderId?.let { onOrderPlaced(it) }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Checkout") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Fowl Details Card
                uiState.fowl?.let { fowl ->
                    FowlOrderCard(
                        fowl = fowl,
                        quantity = quantity,
                        totalPrice = uiState.orderSummary?.productTotal ?: 0.0
                    )
                }
                
                // Delivery Address Card
                DeliveryAddressCard(
                    address = uiState.deliveryAddress,
                    location = uiState.deliveryLocation,
                    onAddressChange = { address, location ->
                        viewModel.updateDeliveryAddress(address, location)
                    }
                )
                
                // Order Summary Card
                uiState.orderSummary?.let { summary ->
                    OrderSummaryCard(summary = summary)
                }
                
                // Payment Method Card
                PaymentMethodCard(
                    selectedMethod = uiState.selectedPaymentMethod,
                    onMethodSelected = { viewModel.selectPaymentMethod(it) }
                )
                
                // Place Order Button
                Button(
                    onClick = { viewModel.placeOrder() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isProcessingPayment && 
                             uiState.deliveryAddress.isNotBlank() &&
                             uiState.selectedPaymentMethod.isNotBlank()
                ) {
                    if (uiState.isProcessingPayment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Place Order - ${formatCurrency(uiState.orderSummary?.grandTotal ?: 0.0)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                // Error Display
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
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
private fun FowlOrderCard(
    fowl: com.rio.rostry.data.model.Fowl,
    quantity: Int,
    totalPrice: Double
) {
    Card {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = fowl.imageUrls.firstOrNull() ?: "https://via.placeholder.com/80",
                contentDescription = fowl.name,
                modifier = Modifier.size(80.dp),
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
                    text = fowl.breed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Quantity: $quantity",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = formatCurrency(totalPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DeliveryAddressCard(
    address: String,
    location: Location?,
    onAddressChange: (String, Location?) -> Unit
) {
    var showAddressDialog by remember { mutableStateOf(false) }
    
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
                    text = "Delivery Address",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = { showAddressDialog = true }) {
                    Text(if (address.isBlank()) "Add Address" else "Change")
                }
            }
            
            if (address.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    text = "Please add a delivery address",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showAddressDialog) {
        AddressInputDialog(
            currentAddress = address,
            onAddressConfirmed = { newAddress ->
                // In a real app, you'd geocode the address to get Location
                val newLocation = Location(address = newAddress)
                onAddressChange(newAddress, newLocation)
                showAddressDialog = false
            },
            onDismiss = { showAddressDialog = false }
        )
    }
}

@Composable
private fun OrderSummaryCard(summary: OrderSummary) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Product total
            SummaryRow("Product Total", summary.productTotal)
            
            // Fees breakdown
            summary.feeBreakdown.forEach { (label, amount) ->
                if (amount > 0) {
                    SummaryRow(label, amount, isSubItem = true)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Grand total
            SummaryRow(
                "Total",
                summary.grandTotal,
                isTotal = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    amount: Double,
    isSubItem: Boolean = false,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isSubItem) "  $label" else label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isSubItem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = formatCurrency(amount),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentMethodCard(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val paymentMethods = listOf(
                "Credit/Debit Card",
                "PayPal",
                "Google Pay",
                "Cash on Delivery"
            )
            
            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { onMethodSelected(method) }
                    )
                    
                    Text(
                        text = method,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressInputDialog(
    currentAddress: String,
    onAddressConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var address by remember { mutableStateOf(currentAddress) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delivery Address") },
        text = {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Enter your delivery address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAddressConfirmed(address) },
                enabled = address.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}