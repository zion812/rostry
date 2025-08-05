package com.rio.rostry.ui.marketplace.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlGender

/**
 * Lineage Tracking Section Component
 * 
 * Provides a toggle-based lineage tracking system with dynamic field visibility,
 * proper validation, and user-friendly interface for marketplace listings.
 * 
 * Features:
 * - Toggle between traceable and non-traceable lineage
 * - Dynamic field visibility with smooth animations
 * - Parent fowl selection with gender filtering
 * - Generation and bloodline tracking
 * - Comprehensive lineage notes
 * - Educational information about lineage benefits
 * 
 * @param hasTraceableLineage Current lineage tracking state
 * @param onLineageToggle Callback for lineage toggle changes
 * @param selectedMotherId Currently selected mother fowl ID
 * @param onMotherSelected Callback for mother selection
 * @param selectedFatherId Currently selected father fowl ID
 * @param onFatherSelected Callback for father selection
 * @param generation Generation number as string
 * @param onGenerationChange Callback for generation changes
 * @param bloodlineId Bloodline identifier
 * @param onBloodlineChange Callback for bloodline changes
 * @param lineageNotes Additional lineage notes
 * @param onLineageNotesChange Callback for lineage notes changes
 * @param availableFowls List of available fowls for parent selection
 * @param modifier Modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineageTrackingSection(
    hasTraceableLineage: Boolean,
    onLineageToggle: (Boolean) -> Unit,
    selectedMotherId: String?,
    onMotherSelected: (String?) -> Unit,
    selectedFatherId: String?,
    onFatherSelected: (String?) -> Unit,
    generation: String,
    onGenerationChange: (String) -> Unit,
    bloodlineId: String,
    onBloodlineChange: (String) -> Unit,
    lineageNotes: String,
    onLineageNotesChange: (String) -> Unit,
    availableFowls: List<Fowl>,
    onClearLineageData: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State for confirmation dialog
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // Enhanced toggle handler with confirmation for data clearing
    val handleLineageToggle = { newValue: Boolean ->
        if (!newValue && hasTraceableLineage) {
            // Switching from traceable to non-traceable - show confirmation
            showConfirmationDialog = true
        } else {
            // Switching to traceable or already non-traceable
            onLineageToggle(newValue)
        }
    }

    // Confirmation dialog for data clearing
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    "Switch to Non-Traceable Mode?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "This will permanently clear all lineage information including parent selections, generation, bloodline, and notes. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLineageToggle(false)
                        onClearLineageData()
                        showConfirmationDialog = false
                    }
                ) {
                    Text(
                        "Clear & Continue",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasTraceableLineage)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Lineage Toggle Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lineage Tracking",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasTraceableLineage)
                            "Include detailed lineage information"
                        else "No lineage information",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = hasTraceableLineage,
                    onCheckedChange = handleLineageToggle
                )
            }

            // Lineage Mode Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { handleLineageToggle(true) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Traceable Lineage",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Include parent info",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    selected = hasTraceableLineage,
                    leadingIcon = if (hasTraceableLineage) {
                        {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    onClick = { handleLineageToggle(false) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Non-Traceable",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "No lineage info",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    selected = !hasTraceableLineage,
                    leadingIcon = if (!hasTraceableLineage) {
                        {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }

            // Conditional Lineage Fields with Animation
            AnimatedVisibility(
                visible = hasTraceableLineage,
                enter = expandVertically(
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                LineageDetailsForm(
                    selectedMotherId = selectedMotherId,
                    onMotherSelected = onMotherSelected,
                    selectedFatherId = selectedFatherId,
                    onFatherSelected = onFatherSelected,
                    generation = generation,
                    onGenerationChange = onGenerationChange,
                    bloodlineId = bloodlineId,
                    onBloodlineChange = onBloodlineChange,
                    lineageNotes = lineageNotes,
                    onLineageNotesChange = onLineageNotesChange,
                    availableFowls = availableFowls
                )
            }
        }
    }
}

/**
 * Lineage Details Form Component
 * 
 * Contains all the detailed lineage tracking fields including parent selection,
 * generation tracking, bloodline information, and notes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LineageDetailsForm(
    selectedMotherId: String?,
    onMotherSelected: (String?) -> Unit,
    selectedFatherId: String?,
    onFatherSelected: (String?) -> Unit,
    generation: String,
    onGenerationChange: (String) -> Unit,
    bloodlineId: String,
    onBloodlineChange: (String) -> Unit,
    lineageNotes: String,
    onLineageNotesChange: (String) -> Unit,
    availableFowls: List<Fowl>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider()

        Text(
            text = "Lineage Details",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        // Parent Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mother Selection
            ParentSelectionDropdown(
                label = "Mother",
                selectedParentId = selectedMotherId,
                onParentSelected = onMotherSelected,
                availableFowls = availableFowls.filter { it.gender == FowlGender.FEMALE },
                modifier = Modifier.weight(1f)
            )

            // Father Selection
            ParentSelectionDropdown(
                label = "Father",
                selectedParentId = selectedFatherId,
                onParentSelected = onFatherSelected,
                availableFowls = availableFowls.filter { it.gender == FowlGender.MALE },
                modifier = Modifier.weight(1f)
            )
        }

        // Generation and Bloodline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = generation,
                onValueChange = { newValue ->
                    // Validate numeric input
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        onGenerationChange(newValue)
                    }
                },
                label = { Text("Generation") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                placeholder = { Text("1") },
                supportingText = { Text("Numeric only") },
                isError = generation.isNotEmpty() && !generation.all { it.isDigit() }
            )

            OutlinedTextField(
                value = bloodlineId,
                onValueChange = onBloodlineChange,
                label = { Text("Bloodline ID") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Optional") },
                supportingText = { Text("e.g., BL001") }
            )
        }

        // Lineage Notes
        OutlinedTextField(
            value = lineageNotes,
            onValueChange = onLineageNotesChange,
            label = { Text("Lineage Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            placeholder = { Text("Additional lineage information, breeding history, genetic traits, etc.") },
            supportingText = { Text("${lineageNotes.length}/500 characters") },
            isError = lineageNotes.length > 500
        )

        // Lineage Information Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lineage Benefits",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Higher buyer confidence and premium pricing\n" +
                            "• Breeding program compatibility\n" +
                            "• Genetic diversity assessment\n" +
                            "• Performance prediction capabilities",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Parent Selection Dropdown Component
 * 
 * Provides a dropdown for selecting parent fowls with proper filtering
 * and display of fowl information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentSelectionDropdown(
    label: String,
    selectedParentId: String?,
    onParentSelected: (String?) -> Unit,
    availableFowls: List<Fowl>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFowl = availableFowls.find { it.id == selectedParentId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedFowl?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            placeholder = { Text("Select $label") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            supportingText = if (selectedFowl != null) {
                { Text("${selectedFowl.breed} • ${selectedFowl.type.name}") }
            } else null
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // None option
            DropdownMenuItem(
                text = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("None")
                    }
                },
                onClick = {
                    onParentSelected(null)
                    expanded = false
                }
            )

            // Available fowls
            availableFowls.forEach { fowl ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = fowl.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${fowl.breed} • ${fowl.type.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (fowl.status.contains("Breeder", ignoreCase = true)) {
                                Text(
                                    text = "✓ Breeder Ready",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onParentSelected(fowl.id)
                        expanded = false
                    }
                )
            }

            // No fowls available message
            if (availableFowls.isEmpty()) {
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "No ${label.lowercase()} fowls available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { expanded = false },
                    enabled = false
                )
            }
        }
    }
}