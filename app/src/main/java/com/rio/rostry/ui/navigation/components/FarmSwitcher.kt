package com.rio.rostry.ui.navigation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rio.rostry.data.model.organization.Organization
import com.rio.rostry.ui.theme.*

/**
 * Farm switcher component for multi-farm users
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSwitcherDropdown(
    farms: List<Organization>,
    selectedFarmId: String?,
    onFarmSwitch: (String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFarm = farms.find { it.id == selectedFarmId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        if (compact) {
            // Compact version for navigation rail
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.menuAnchor()
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch Farm",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // Full version for drawer and top bar
            OutlinedTextField(
                value = selectedFarm?.name ?: "Select Farm",
                onValueChange = { },
                readOnly = true,
                label = { Text("Current Farm") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            farms.forEach { farm ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = farm.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = farm.type.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onFarmSwitch(farm.id)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (farm.type) {
                                com.rio.rostry.data.model.organization.OrganizationType.INDIVIDUAL_FARM -> Icons.Default.Home
                                com.rio.rostry.data.model.organization.OrganizationType.COOPERATIVE -> Icons.Default.Groups
                                com.rio.rostry.data.model.organization.OrganizationType.COMMERCIAL_FARM -> Icons.Default.Business
                                com.rio.rostry.data.model.organization.OrganizationType.BREEDING_FACILITY -> Icons.Default.Pets
                                com.rio.rostry.data.model.organization.OrganizationType.RESEARCH_INSTITUTION -> Icons.Default.Science
                                com.rio.rostry.data.model.organization.OrganizationType.MARKETPLACE_VENDOR -> Icons.Default.Store
                                com.rio.rostry.data.model.organization.OrganizationType.EDUCATIONAL -> Icons.Default.School
                            },
                            contentDescription = null,
                            tint = if (farm.id == selectedFarmId) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    trailingIcon = if (farm.id == selectedFarmId) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Farm switcher section for navigation drawer
 */
@Composable
fun FarmSwitcherSection(
    farms: List<Organization>,
    selectedFarmId: String?,
    onFarmSwitch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Organizations",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FarmSwitcherDropdown(
            farms = farms,
            selectedFarmId = selectedFarmId,
            onFarmSwitch = onFarmSwitch,
            compact = false
        )

        if (farms.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${farms.size} organizations available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Farm switcher top bar for mobile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSwitcherTopBar(
    farms: List<Organization>,
    selectedFarmId: String?,
    onFarmSwitch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            FarmSwitcherDropdown(
                farms = farms,
                selectedFarmId = selectedFarmId,
                onFarmSwitch = onFarmSwitch,
                compact = false
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}