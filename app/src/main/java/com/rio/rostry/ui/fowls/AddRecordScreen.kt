package com.rio.rostry.ui.fowls

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.FowlRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    fowlId: String,
    onNavigateBack: () -> Unit,
    onRecordAdded: () -> Unit,
    viewModel: FowlDetailViewModel = hiltViewModel()
) {
    var recordType by remember { mutableStateOf("Health Check") }
    var details by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var veterinarian by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Add Record") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        if (details.isNotBlank()) {
                            isLoading = true
                            error = null
                            
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                val record = FowlRecord(
                                    fowlId = fowlId,
                                    recordType = recordType,
                                    date = selectedDate,
                                    details = details,
                                    weight = weight.toDoubleOrNull(),
                                    temperature = temperature.toDoubleOrNull(),
                                    medication = medication,
                                    veterinarian = veterinarian,
                                    cost = cost.toDoubleOrNull(),
                                    notes = notes,
                                    createdBy = currentUser.uid
                                )
                                
                                viewModel.addRecord(record) {
                                    isLoading = false
                                    onRecordAdded()
                                }
                            } else {
                                error = "User not authenticated"
                                isLoading = false
                            }
                        } else {
                            error = "Details are required"
                        }
                    },
                    enabled = !isLoading && details.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Save")
                    }
                }
            }
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Record Type Section
            Text(
                text = "Record Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Record Type Dropdown
            var recordTypeExpanded by remember { mutableStateOf(false) }
            val recordTypeOptions = listOf(
                "Health Check", "Vaccination", "Weight Update", "Feeding", 
                "Breeding", "Treatment", "Sale", "Purchase", "Transfer", "Other"
            )
            ExposedDropdownMenuBox(
                expanded = recordTypeExpanded,
                onExpandedChange = { recordTypeExpanded = !recordTypeExpanded }
            ) {
                OutlinedTextField(
                    value = recordType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Record Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recordTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = recordTypeExpanded,
                    onDismissRequest = { recordTypeExpanded = false }
                ) {
                    recordTypeOptions.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                recordType = type
                                recordTypeExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Date Selection
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Details
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("Details *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text("Enter detailed information about this record...") }
            )
            
            // Measurements Section
            Text(
                text = "Measurements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Temperature (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Medical Information Section
            Text(
                text = "Medical Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = medication,
                onValueChange = { medication = it },
                label = { Text("Medication") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Name of medication or treatment") }
            )
            
            OutlinedTextField(
                value = veterinarian,
                onValueChange = { veterinarian = it },
                label = { Text("Veterinarian") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Name of veterinarian or clinic") }
            )
            
            // Cost Information
            OutlinedTextField(
                value = cost,
                onValueChange = { cost = it },
                label = { Text("Cost ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Cost of treatment, medication, etc.") }
            )
            
            // Additional Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                placeholder = { Text("Any additional observations or notes...") }
            )
            
            // Image Upload Section
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
                            text = "Proof Image",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Image")
                        }
                    }
                    
                    selectedImageUri?.let { uri ->
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            // Error Display
            error?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage,
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
                        text = "Record Guidelines",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• Provide detailed and accurate information\n" +
                                "• Include measurements when relevant\n" +
                                "• Upload proof images for verification\n" +
                                "• Record costs for expense tracking\n" +
                                "• Add notes for future reference",
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