package com.rio.rostry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.FarmPermission
import com.rio.rostry.data.model.FarmRole
import com.rio.rostry.data.repository.FarmAccessRepository
import kotlinx.coroutines.flow.first

/**
 * Permission gate component for conditional UI rendering based on user permissions
 * Provides secure access control throughout the application
 */
@Composable
fun PermissionGate(
    farmId: String,
    permission: FarmPermission,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true,
    accessRepository: FarmAccessRepository = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(farmId, permission, currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            try {
                hasPermission = accessRepository.hasPermission(userId, farmId, permission)
            } catch (e: Exception) {
                hasPermission = false
            } finally {
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            if (showFallback) {
                PermissionLoadingState()
            }
        }
        hasPermission -> content()
        showFallback -> {
            fallback?.invoke() ?: PermissionDeniedState(permission)
        }
    }
}

/**
 * Multi-permission gate for checking multiple permissions
 */
@Composable
fun MultiPermissionGate(
    farmId: String,
    permissions: List<FarmPermission>,
    requireAll: Boolean = true,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true,
    accessRepository: FarmAccessRepository = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var permissionResults by remember { mutableStateOf<Map<FarmPermission, Boolean>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(farmId, permissions, currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            try {
                permissionResults = accessRepository.hasPermissions(userId, farmId, permissions)
            } catch (e: Exception) {
                permissionResults = permissions.associateWith { false }
            } finally {
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    val hasAccess = when {
        isLoading -> false
        requireAll -> permissionResults.values.all { it }
        else -> permissionResults.values.any { it }
    }

    when {
        isLoading -> {
            if (showFallback) {
                PermissionLoadingState()
            }
        }
        hasAccess -> content()
        showFallback -> {
            fallback?.invoke() ?: MultiPermissionDeniedState(permissions, requireAll)
        }
    }
}

/**
 * Role-based gate for checking user role
 */
@Composable
fun RoleGate(
    farmId: String,
    allowedRoles: List<FarmRole>,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true,
    accessRepository: FarmAccessRepository = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var userRole by remember { mutableStateOf<FarmRole?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(farmId, currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            try {
                val access = accessRepository.getFarmAccessByUserAndFarm(userId, farmId).first()
                userRole = access?.role
            } catch (e: Exception) {
                userRole = null
            } finally {
                isLoading = false
            }
        } ?: run {
            isLoading = false
        }
    }

    val hasAccess = userRole in allowedRoles

    when {
        isLoading -> {
            if (showFallback) {
                PermissionLoadingState()
            }
        }
        hasAccess -> content()
        showFallback -> {
            fallback?.invoke() ?: RoleDeniedState(allowedRoles)
        }
    }
}

/**
 * Owner-only gate for farm owner exclusive features
 */
@Composable
fun OwnerOnlyGate(
    farmId: String,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true
) {
    RoleGate(
        farmId = farmId,
        allowedRoles = listOf(FarmRole.OWNER),
        content = content,
        fallback = fallback,
        showFallback = showFallback
    )
}

/**
 * Management gate for owners and managers
 */
@Composable
fun ManagementGate(
    farmId: String,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true
) {
    RoleGate(
        farmId = farmId,
        allowedRoles = listOf(FarmRole.OWNER, FarmRole.MANAGER),
        content = content,
        fallback = fallback,
        showFallback = showFallback
    )
}

/**
 * Staff gate for staff-level access (excludes viewers)
 */
@Composable
fun StaffGate(
    farmId: String,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true
) {
    RoleGate(
        farmId = farmId,
        allowedRoles = listOf(
            FarmRole.OWNER, 
            FarmRole.MANAGER, 
            FarmRole.VETERINARIAN, 
            FarmRole.SUPERVISOR, 
            FarmRole.WORKER, 
            FarmRole.SPECIALIST
        ),
        content = content,
        fallback = fallback,
        showFallback = showFallback
    )
}

/**
 * Conditional permission button
 */
@Composable
fun PermissionButton(
    farmId: String,
    permission: FarmPermission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    PermissionGate(
        farmId = farmId,
        permission = permission,
        content = {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content
            )
        },
        fallback = {
            Button(
                onClick = { /* No action */ },
                modifier = modifier,
                enabled = false,
                content = content
            )
        }
    )
}

/**
 * Conditional permission icon button
 */
@Composable
fun PermissionIconButton(
    farmId: String,
    permission: FarmPermission,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    PermissionGate(
        farmId = farmId,
        permission = permission,
        content = {
            IconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        },
        showFallback = false
    )
}

/**
 * Conditional permission floating action button
 */
@Composable
fun PermissionFAB(
    farmId: String,
    permission: FarmPermission,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null
) {
    PermissionGate(
        farmId = farmId,
        permission = permission,
        content = {
            if (text != null) {
                ExtendedFloatingActionButton(
                    onClick = onClick,
                    modifier = modifier,
                    icon = icon,
                    text = text
                )
            } else {
                FloatingActionButton(
                    onClick = onClick,
                    modifier = modifier,
                    content = icon
                )
            }
        },
        showFallback = false
    )
}

/**
 * Permission-aware menu item
 */
@Composable
fun PermissionMenuItem(
    farmId: String,
    permission: FarmPermission,
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    PermissionGate(
        farmId = farmId,
        permission = permission,
        content = {
            DropdownMenuItem(
                text = { Text(text) },
                onClick = onClick,
                leadingIcon = leadingIcon?.let { icon ->
                    { Icon(icon, contentDescription = null) }
                },
                enabled = enabled
            )
        },
        showFallback = false
    )
}

// Loading and error states
@Composable
private fun PermissionLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun PermissionDeniedState(permission: FarmPermission) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Access Restricted",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "You need '${permission.displayName}' permission to access this feature.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MultiPermissionDeniedState(permissions: List<FarmPermission>, requireAll: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Access Restricted",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = if (requireAll) {
                    "You need all of the following permissions:"
                } else {
                    "You need at least one of the following permissions:"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            permissions.forEach { permission ->
                Text(
                    text = "• ${permission.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun RoleDeniedState(allowedRoles: List<FarmRole>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Role Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "This feature requires one of the following roles:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            allowedRoles.forEach { role ->
                Text(
                    text = "• ${role.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// Usage examples and helper composables
@Composable
fun FarmManagementButton(farmId: String, onClick: () -> Unit) {
    PermissionButton(
        farmId = farmId,
        permission = FarmPermission.EDIT_FARM,
        onClick = onClick
    ) {
        Icon(Icons.Default.Edit, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Edit Farm")
    }
}

@Composable
fun AddFowlFAB(farmId: String, onClick: () -> Unit) {
    PermissionFAB(
        farmId = farmId,
        permission = FarmPermission.MANAGE_FOWLS,
        onClick = onClick,
        icon = {
            Icon(Icons.Default.Add, contentDescription = "Add Fowl")
        },
        text = {
            Text("Add Fowl")
        }
    )
}

@Composable
fun InviteUserButton(farmId: String, onClick: () -> Unit) {
    PermissionButton(
        farmId = farmId,
        permission = FarmPermission.INVITE_USERS,
        onClick = onClick
    ) {
        Icon(Icons.Default.PersonAdd, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Invite User")
    }
}

@Composable
fun DeleteFarmButton(farmId: String, onClick: () -> Unit) {
    OwnerOnlyGate(farmId = farmId) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete Farm")
        }
    }
}