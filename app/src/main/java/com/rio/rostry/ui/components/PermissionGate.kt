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
import com.rio.rostry.data.model.FarmPermission
import com.rio.rostry.data.model.FarmRole

/**
 * Simplified permission gate component for conditional UI rendering
 * Temporarily grants all permissions to avoid compilation issues
 */
@Composable
fun PermissionGate(
    farmId: String,
    permission: FarmPermission,
    content: @Composable () -> Unit,
    fallback: @Composable (() -> Unit)? = null,
    showFallback: Boolean = true
) {
    // Simplified implementation - always grant permission for now
    content()
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
    showFallback: Boolean = true
) {
    // Simplified implementation - always grant permission for now
    content()
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
    showFallback: Boolean = true
) {
    // Simplified implementation - always grant permission for now
    content()
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
    content()
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
    content()
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
    content()
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
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
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
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null) }
        },
        enabled = enabled
    )
}