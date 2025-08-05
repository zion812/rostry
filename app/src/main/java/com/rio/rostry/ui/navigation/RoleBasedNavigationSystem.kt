package com.rio.rostry.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rio.rostry.data.model.role.*
import com.rio.rostry.ui.navigation.Permission
import com.rio.rostry.data.model.organization.Organization
import com.rio.rostry.ui.auth.AuthViewModel
import com.rio.rostry.ui.auth.LoginScreen
import com.rio.rostry.ui.dashboard.FarmDashboardScreenRedesigned
import com.rio.rostry.ui.marketplace.MarketplaceScreenRedesigned
import com.rio.rostry.ui.theme.*
import com.rio.rostry.viewmodel.RoleBasedNavigationViewModel
import com.rio.rostry.viewmodel.RoleBasedNavigationState

/**
 * Comprehensive Role-Based Navigation System for ROSTRY
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleBasedNavigationSystem(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    navigationViewModel: RoleBasedNavigationViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val navigationState by navigationViewModel.uiState.collectAsStateWithLifecycle()

    // Handle authentication state
    if (!authState.isAuthenticated) {
        LoginScreen(
            onNavigateToRegister = { /* Handle registration navigation */ },
            onNavigateToForgotPassword = { /* Handle forgot password navigation */ },
            onLoginSuccess = { /* Handle login success */ }
        )
        return
    }

    // Handle loading and error states
    when {
        navigationState.isLoading -> {
            LoadingScreen()
            return
        }
        navigationState.error != null -> {
            ErrorScreen(
                error = navigationState.error ?: "Unknown error",
                onRetry = { navigationViewModel.refreshUserAccess() }
            )
            return
        }
        navigationState.currentUser == null -> {
            ErrorScreen(
                error = "User data not available",
                onRetry = { navigationViewModel.refreshUserAccess() }
            )
            return
        }
    }

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Initialize navigation system
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            navigationViewModel.initializeNavigation()
        }
    }

    val useNavigationRail = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium
    val useNavigationDrawer = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    when {
        useNavigationDrawer -> {
            // Desktop/Large Tablet layout with navigation drawer
            PermanentNavigationDrawer(
                drawerContent = {
                    RoleBasedNavigationDrawerContent(
                        navigationState = navigationState,
                        currentDestination = currentDestination,
                        onNavigate = { route -> 
                            navigateWithPermissionCheck(
                                navController = navController,
                                route = route,
                                navigationState = navigationState,
                                onPermissionDenied = { navigationViewModel.showPermissionDeniedMessage(it) }
                            )
                        },
                        onFarmSwitch = { farmId -> navigationViewModel.switchFarm(farmId) },
                        onSignOut = { authViewModel.signOut() }
                    )
                },
                modifier = modifier
            ) {
                RoleBasedNavHost(
                    navController = navController,
                    navigationState = navigationState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        useNavigationRail -> {
            // Medium Tablet layout with navigation rail
            Row(modifier = modifier.fillMaxSize()) {
                RoleBasedNavigationRail(
                    navigationState = navigationState,
                    currentDestination = currentDestination,
                    onNavigate = { route -> 
                        navigateWithPermissionCheck(
                            navController = navController,
                            route = route,
                            navigationState = navigationState,
                            onPermissionDenied = { navigationViewModel.showPermissionDeniedMessage(it) }
                        )
                    },
                    onFarmSwitch = { farmId -> navigationViewModel.switchFarm(farmId) }
                )
                RoleBasedNavHost(
                    navController = navController,
                    navigationState = navigationState,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        else -> {
            // Phone layout with bottom navigation
            Scaffold(
                bottomBar = {
                    RoleBasedBottomNavigation(
                        navigationState = navigationState,
                        currentDestination = currentDestination,
                        onNavigate = { route -> 
                            navigateWithPermissionCheck(
                                navController = navController,
                                route = route,
                                navigationState = navigationState,
                                onPermissionDenied = { navigationViewModel.showPermissionDeniedMessage(it) }
                            )
                        }
                    )
                },
                modifier = modifier
            ) { paddingValues ->
                RoleBasedNavHost(
                    navController = navController,
                    navigationState = navigationState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleBasedNavigationDrawerContent(
    navigationState: RoleBasedNavigationState,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    onFarmSwitch: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    PermanentDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App branding and user info
            UserProfileSection(
                user = navigationState.currentUser!!,
                currentFarm = navigationState.selectedFarm,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Dynamic navigation sections based on role and permissions
            val navigationSections = getNavigationSections(navigationState)
            
            navigationSections.forEach { section ->
                NavigationSection(
                    section = section,
                    currentDestination = currentDestination,
                    onNavigate = onNavigate,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign out button
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}

@Composable
private fun RoleBasedNavigationRail(
    navigationState: RoleBasedNavigationState,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    onFarmSwitch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = "ROSTRY",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show only main navigation items for rail
        val mainItems = getMainNavigationItems(navigationState)
        mainItems.forEach { item ->
            NavigationRailItem(
                icon = { 
                    Icon(
                        imageVector = if (isCurrentDestination(currentDestination, item.route)) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label
                    )
                },
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isCurrentDestination(currentDestination, item.route),
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun RoleBasedBottomNavigation(
    navigationState: RoleBasedNavigationState,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val mainItems = getMainNavigationItems(navigationState).take(5) // Limit to 5 items for bottom nav
        
        mainItems.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (isCurrentDestination(currentDestination, item.route)) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = item.label
                    )
                },
                label = { 
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isCurrentDestination(currentDestination, item.route),
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun RoleBasedNavHost(
    navController: NavHostController,
    navigationState: RoleBasedNavigationState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = getStartDestination(navigationState),
        modifier = modifier
    ) {
        // Dashboard - Available to all authenticated users
        composable(RoleBasedDestinations.DASHBOARD) {
            FarmDashboardScreenRedesigned(
                onNavigateToFlockDetail = { flockId ->
                    if (hasPermission(navigationState, Permission.Farm.VIEW_OWN)) {
                        navController.navigate("${RoleBasedDestinations.FLOCK_DETAIL}/$flockId")
                    }
                },
                onNavigateToAddFlock = {
                    if (hasPermission(navigationState, Permission.Farm.MANAGE_BASIC)) {
                        navController.navigate(RoleBasedDestinations.ADD_FLOCK)
                    }
                },
                onNavigateToAnalytics = {
                    if (hasPermission(navigationState, Permission.Analytics.BASIC)) {
                        navController.navigate(RoleBasedDestinations.ANALYTICS)
                    }
                },
                onNavigateToTasks = {
                    if (hasPermission(navigationState, Permission.Farm.VIEW_OWN)) {
                        navController.navigate(RoleBasedDestinations.TASKS)
                    }
                },
                onNavigateToFarmSettings = {
                    if (hasPermission(navigationState, Permission.Farm.MANAGE_BASIC)) {
                        navController.navigate(RoleBasedDestinations.FARM_SETTINGS)
                    }
                }
            )
        }

        // Marketplace - Available to all users
        composable(RoleBasedDestinations.MARKETPLACE) {
            MarketplaceScreenRedesigned(
                onNavigateToFowlDetail = { fowlId ->
                    navController.navigate("${RoleBasedDestinations.FOWL_DETAIL}/$fowlId")
                },
                onNavigateToSearch = {
                    navController.navigate(RoleBasedDestinations.SEARCH)
                },
                onNavigateToCart = {
                    navController.navigate(RoleBasedDestinations.CART)
                },
                onNavigateToCategories = {
                    navController.navigate(RoleBasedDestinations.CATEGORIES)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate("${RoleBasedDestinations.PROFILE}/$userId")
                }
            )
        }

        // Role-specific screens
        if (hasPermission(navigationState, Permission.Farm.VIEW_OWN)) {
            composable(RoleBasedDestinations.FOWLS) {
                // FowlManagementScreen with role-based features
            }
        }

        if (hasPermission(navigationState, Permission.Analytics.BASIC)) {
            composable(RoleBasedDestinations.ANALYTICS) {
                // AnalyticsScreen with role-based data access
            }
        }

        if (hasPermission(navigationState, Permission.Team.MANAGE)) {
            composable(RoleBasedDestinations.TEAM_MANAGEMENT) {
                // TeamManagementScreen for managing farm access
            }
        }
    }
}

// Helper functions for navigation logic
private fun navigateWithPermissionCheck(
    navController: NavHostController,
    route: String,
    navigationState: RoleBasedNavigationState,
    onPermissionDenied: (String) -> Unit
) {
    val requiredPermission = getRequiredPermissionForRoute(route)
    
    if (requiredPermission == null || hasPermission(navigationState, requiredPermission)) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    } else {
        onPermissionDenied("You don't have permission to access this feature")
    }
}

private fun hasPermission(
    navigationState: RoleBasedNavigationState,
    permission: Permission
): Boolean {
    return when (permission) {
        Permission.Marketplace.VIEW -> true
        Permission.Farm.VIEW_OWN -> navigationState.currentUser?.role == com.rio.rostry.data.model.UserRole.FARMER
        Permission.Analytics.BASIC -> navigationState.currentUser?.role == com.rio.rostry.data.model.UserRole.FARMER
        Permission.Team.MANAGE -> navigationState.currentUser?.role == com.rio.rostry.data.model.UserRole.FARMER
        Permission.Farm.MANAGE_BASIC -> navigationState.currentUser?.role == com.rio.rostry.data.model.UserRole.FARMER
        else -> false
    }
}

private fun getRequiredPermissionForRoute(route: String): Permission? {
    return when (route) {
        RoleBasedDestinations.ANALYTICS -> Permission.Analytics.BASIC
        RoleBasedDestinations.FOWLS -> Permission.Farm.VIEW_OWN
        RoleBasedDestinations.TEAM_MANAGEMENT -> Permission.Team.MANAGE
        RoleBasedDestinations.FARM_SETTINGS -> Permission.Farm.MANAGE_BASIC
        else -> null // No permission required
    }
}

private fun getStartDestination(navigationState: RoleBasedNavigationState): String {
    return when {
        navigationState.selectedFarm != null -> RoleBasedDestinations.DASHBOARD
        else -> RoleBasedDestinations.MARKETPLACE
    }
}

// Placeholder composables for missing components
@Composable
private fun UserProfileSection(
    user: com.rio.rostry.data.model.User,
    currentFarm: Organization?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ROSTRY",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Farm Management",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodyMedium
        )
        currentFarm?.let {
            Text(
                text = it.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NavigationSection(
    section: NavigationSection,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        section.items.forEach { item ->
            NavigationDrawerItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                label = { Text(item.label) },
                selected = isCurrentDestination(currentDestination, item.route),
                onClick = { onNavigate(item.route) },
                modifier = Modifier.padding(vertical = 2.dp),
                shape = MaterialTheme.shapes.small
            )
        }
    }
}

private fun getNavigationSections(navigationState: RoleBasedNavigationState): List<NavigationSection> {
    val sections = mutableListOf<NavigationSection>()
    
    // Main section - always available
    sections.add(
        NavigationSection(
            title = "Main",
            items = listOf(
                NavigationItemData(
                    id = "dashboard",
                    route = RoleBasedDestinations.DASHBOARD,
                    label = "Dashboard",
                    icon = Icons.Filled.Dashboard
                ),
                NavigationItemData(
                    id = "marketplace",
                    route = RoleBasedDestinations.MARKETPLACE,
                    label = "Marketplace",
                    icon = Icons.Filled.Store
                )
            )
        )
    )
    
    // Farm management section - based on permissions
    val farmItems = mutableListOf<NavigationItemData>()
    
    if (hasPermission(navigationState, Permission.Farm.VIEW_OWN)) {
        farmItems.add(
            NavigationItemData(
                id = "fowls",
                route = RoleBasedDestinations.FOWLS,
                label = "My Fowls",
                icon = Icons.Filled.Pets
            )
        )
    }
    
    if (hasPermission(navigationState, Permission.Analytics.BASIC)) {
        farmItems.add(
            NavigationItemData(
                id = "analytics",
                route = RoleBasedDestinations.ANALYTICS,
                label = "Analytics",
                icon = Icons.Filled.Analytics
            )
        )
    }
    
    if (farmItems.isNotEmpty()) {
        sections.add(
            NavigationSection(
                title = "Farm Management",
                items = farmItems
            )
        )
    }
    
    return sections
}

private fun getMainNavigationItems(navigationState: RoleBasedNavigationState): List<SimpleNavigationItem> {
    val items = mutableListOf<SimpleNavigationItem>()
    
    // Always available
    items.add(
        SimpleNavigationItem(
            route = RoleBasedDestinations.DASHBOARD,
            label = "Dashboard",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard
        )
    )
    
    items.add(
        SimpleNavigationItem(
            route = RoleBasedDestinations.MARKETPLACE,
            label = "Marketplace",
            selectedIcon = Icons.Filled.Store,
            unselectedIcon = Icons.Outlined.Store
        )
    )
    
    // Role-based items
    if (hasPermission(navigationState, Permission.Farm.VIEW_OWN)) {
        items.add(
            SimpleNavigationItem(
                route = RoleBasedDestinations.FOWLS,
                label = "Fowls",
                selectedIcon = Icons.Filled.Pets,
                unselectedIcon = Icons.Outlined.Pets
            )
        )
    }
    
    if (hasPermission(navigationState, Permission.Analytics.BASIC)) {
        items.add(
            SimpleNavigationItem(
                route = RoleBasedDestinations.ANALYTICS,
                label = "Analytics",
                selectedIcon = Icons.Filled.Analytics,
                unselectedIcon = Icons.Outlined.Analytics
            )
        )
    }
    
    // Profile is always available
    items.add(
        SimpleNavigationItem(
            route = RoleBasedDestinations.PROFILE,
            label = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )
    
    return items
}

private fun isCurrentDestination(
    currentDestination: NavDestination?,
    route: String
): Boolean {
    return currentDestination?.hierarchy?.any { it.route == route } == true
}

// Loading and Error screens
@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your farm access...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry
            ) {
                Text("Try Again")
            }
        }
    }
}

// Navigation destinations for role-based system
object RoleBasedDestinations {
    const val DASHBOARD = "dashboard"
    const val MARKETPLACE = "marketplace"
    const val FOWLS = "fowls"
    const val ANALYTICS = "analytics"
    const val TASKS = "tasks"
    const val TEAM_MANAGEMENT = "team_management"
    const val FINANCIAL = "financial"
    const val PROFILE = "profile"
    const val FARM_SETTINGS = "farm_settings"
    
    // Detail screens
    const val FLOCK_DETAIL = "flock_detail"
    const val FOWL_DETAIL = "fowl_detail"
    const val ADD_FLOCK = "add_flock"
    const val SEARCH = "search"
    const val CART = "cart"
    const val CATEGORIES = "categories"
}