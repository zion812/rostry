package com.rio.rostry.ui.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rio.rostry.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWallet: () -> Unit = {},
    viewModel: VerificationViewModel = hiltViewModel()
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
            title = { Text("Verification") },
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
                // Current verification status
                item {
                    VerificationStatusCard(
                        user = uiState.currentUser,
                        requests = uiState.verificationRequests
                    )
                }
                
                // Available verification types
                item {
                    VerificationTypesCard(
                        coinPricing = uiState.coinPricing,
                        onRequestVerification = { type ->
                            if (viewModel.canRequestVerification(type)) {
                                viewModel.selectVerificationType(type)
                            }
                        },
                        canRequest = { type -> viewModel.canRequestVerification(type) }
                    )
                }
                
                // Verification history
                if (uiState.verificationRequests.isNotEmpty()) {
                    item {
                        VerificationHistoryCard(
                            requests = uiState.verificationRequests
                        )
                    }
                }
                
                // Benefits of verification
                item {
                    VerificationBenefitsCard()
                }
            }
        }
    }
    
    // Verification request dialog
    if (uiState.showRequestDialog) {
        VerificationRequestDialog(
            verificationType = uiState.selectedVerificationType,
            selectedDocuments = uiState.selectedDocuments,
            notes = uiState.verificationNotes,
            coinCost = uiState.coinPricing.verificationFee,
            isSubmitting = uiState.isSubmitting,
            onAddDocument = viewModel::addDocument,
            onRemoveDocument = viewModel::removeDocument,
            onNotesChange = viewModel::updateNotes,
            onSubmit = viewModel::submitVerificationRequest,
            onDismiss = viewModel::dismissDialog
        )
    }
    
    // Insufficient coins dialog
    if (uiState.showInsufficientCoinsDialog) {
        InsufficientCoinsDialog(
            requiredCoins = uiState.coinPricing.verificationFee,
            currentCoins = uiState.coinBalance,
            onBuyCoins = {
                viewModel.dismissDialog()
                onNavigateToWallet()
            },
            onDismiss = viewModel::dismissDialog
        )
    }
    
    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    // Success snackbar
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success snackbar
        }
    }
}

@Composable
private fun VerificationStatusCard(
    user: User?,
    requests: List<VerificationRequest>
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Verification Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            user?.let { u ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = u.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusChip(
                                text = u.verificationStatus.name,
                                isVerified = u.isVerified
                            )
                            
                            if (u.verificationBadges.isNotEmpty()) {
                                u.verificationBadges.forEach { badge ->
                                    BadgeChip(badge = badge)
                                }
                            }
                        }
                    }
                    
                    if (u.isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationTypesCard(
    coinPricing: CoinPricing,
    onRequestVerification: (VerificationType) -> Unit,
    canRequest: (VerificationType) -> Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Available Verifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val verificationTypes = listOf(
                Triple(VerificationType.USER, "User Verification", "Verify your identity"),
                Triple(VerificationType.BREEDER, "Breeder Certification", "Certified poultry breeder"),
                Triple(VerificationType.FARM, "Farm Verification", "Verified farm operation"),
                Triple(VerificationType.FOWL, "Fowl Verification", "Individual fowl certification")
            )
            
            verificationTypes.forEach { (type, title, description) ->
                VerificationTypeItem(
                    type = type,
                    title = title,
                    description = description,
                    coinCost = coinPricing.verificationFee,
                    canRequest = canRequest(type),
                    onRequest = { onRequestVerification(type) }
                )
                
                if (type != verificationTypes.last().first) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationTypeItem(
    type: VerificationType,
    title: String,
    description: String,
    coinCost: Int,
    canRequest: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "$coinCost coins",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Button(
            onClick = onRequest,
            enabled = canRequest,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(if (canRequest) "Request" else "Requested")
        }
    }
}

@Composable
private fun VerificationHistoryCard(
    requests: List<VerificationRequest>
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Verification History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            requests.forEach { request ->
                VerificationRequestItem(request = request)
                
                if (request != requests.last()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationRequestItem(
    request: VerificationRequest
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = request.verificationType.name.replace("_", " "),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(request.submittedAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            StatusChip(
                text = request.status.name,
                isVerified = request.status == VerificationStatus.VERIFIED
            )
        }
        
        if (request.adminNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Note: ${request.adminNotes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerificationBenefitsCard() {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Benefits of Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val benefits = listOf(
                "Increased trust from buyers",
                "Higher visibility in marketplace",
                "Access to premium features",
                "Verified badge on profile",
                "Priority customer support"
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
private fun StatusChip(
    text: String,
    isVerified: Boolean
) {
    val backgroundColor = when {
        isVerified -> MaterialTheme.colorScheme.primaryContainer
        text.contains("PENDING") -> MaterialTheme.colorScheme.secondaryContainer
        text.contains("REJECTED") -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isVerified -> MaterialTheme.colorScheme.onPrimaryContainer
        text.contains("PENDING") -> MaterialTheme.colorScheme.onSecondaryContainer
        text.contains("REJECTED") -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = text.replace("_", " "),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Composable
private fun BadgeChip(badge: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badge.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun VerificationRequestDialog(
    verificationType: VerificationType,
    selectedDocuments: List<Uri>,
    notes: String,
    coinCost: Int,
    isSubmitting: Boolean,
    onAddDocument: (Uri) -> Unit,
    onRemoveDocument: (Uri) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onAddDocument(it) }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${verificationType.name.replace("_", " ")} Verification")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Upload required documents and provide additional information.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Document upload section
                Text(
                    text = "Documents (${selectedDocuments.size}/5)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedDocuments.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedDocuments) { uri ->
                            DocumentPreview(
                                uri = uri,
                                onRemove = { onRemoveDocument(uri) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (selectedDocuments.size < 5) {
                    OutlinedButton(
                        onClick = { documentLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Document")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes section
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Additional Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cost information
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Coins",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Cost: $coinCost coins",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && selectedDocuments.isNotEmpty()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Request")
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
private fun DocumentPreview(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box {
        AsyncImage(
            model = uri,
            contentDescription = "Document",
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Crop
        )
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
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
                    text = "You need $requiredCoins coins for this verification, but you only have $currentCoins coins.",
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