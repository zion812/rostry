package com.rio.rostry.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rostry.data.model.*
import com.rio.rostry.ui.components.*
import com.rio.rostry.ui.theme.*
import com.rio.rostry.util.formatDate

/**
 * Redesigned Farm Dashboard with improved UX, visual hierarchy, and accessibility
 * Features responsive design, semantic colors, and enhanced user engagement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboardScreenRedesigned(
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFlock: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToFarmSettings: () -> Unit,
    viewModel: FarmDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            FarmDashboardTopBar(
                farmName = uiState.farm?.farmName ?: "My Farm",
                onSettingsClick = onNavigateToFarmSettings,
                onRefreshClick = { viewModel.loadDashboardData() }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.farm == null -> {
                DashboardLoadingState(
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.error != null -> {
                DashboardErrorState(
                    error = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.loadDashboardData() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                DashboardContent(
                    uiState = uiState,
                    isTablet = isTablet,
                    onNavigateToFlockDetail = onNavigateToFlockDetail,
                    onNavigateToAddFlock = onNavigateToAddFlock,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToTasks = onNavigateToTasks,
                    onQuickAction = { /* Handle quick action */ },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarmDashboardTopBar(
    farmName: String,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = farmName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        actions = {
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh dashboard"
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Farm settings"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun DashboardContent(
    uiState: FarmDashboardUiState,
    isTablet: Boolean,
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFlock: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        TabletDashboardLayout(
            uiState = uiState,
            onNavigateToFlockDetail = onNavigateToFlockDetail,
            onNavigateToAddFlock = onNavigateToAddFlock,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToTasks = onNavigateToTasks,
            onQuickAction = onQuickAction,
            modifier = modifier
        )
    } else {
        PhoneDashboardLayout(
            uiState = uiState,
            onNavigateToFlockDetail = onNavigateToFlockDetail,
            onNavigateToAddFlock = onNavigateToAddFlock,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToTasks = onNavigateToTasks,
            onQuickAction = onQuickAction,
            modifier = modifier
        )
    }
}

@Composable
private fun PhoneDashboardLayout(
    uiState: FarmDashboardUiState,
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFlock: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Farm Overview Section
        item {
            FarmOverviewSection(
                farm = uiState.farm,
                isLoading = uiState.isLoading
            )
        }

        // Quick Metrics
        item {
            QuickMetricsSection(
                metrics = uiState.getQuickMetrics(),
                onMetricClick = { metric ->
                    when (metric) {
                        "analytics" -> onNavigateToAnalytics()
                        "tasks" -> onNavigateToTasks()
                    }
                },
                isLoading = uiState.isLoading
            )
        }

        // Health Alerts (if any)
        if (uiState.healthAlerts.isNotEmpty()) {
            item {
                HealthAlertsSection(
                    alerts = uiState.healthAlerts,
                    onAlertClick = { /* Handle alert click */ }
                )
            }
        }

        // Quick Actions
        item {
            QuickActionsSection(
                onAction = onQuickAction
            )
        }

        // Active Flocks
        item {
            ActiveFlocksSection(
                flocks = uiState.flocks,
                onFlockClick = onNavigateToFlockDetail,
                onAddFlockClick = onNavigateToAddFlock,
                isLoading = uiState.isLoading
            )
        }

        // Upcoming Tasks
        if (uiState.upcomingTasks.isNotEmpty()) {
            item {
                UpcomingTasksSection(
                    tasks = uiState.upcomingTasks,
                    onTaskClick = { /* Handle task click */ },
                    onViewAllClick = onNavigateToTasks
                )
            }
        }

        // Recent Activities
        item {
            RecentActivitiesSection(
                activities = uiState.recentActivities
            )
        }
    }
}

@Composable
private fun TabletDashboardLayout(
    uiState: FarmDashboardUiState,
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFlock: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FarmOverviewSection(
                farm = uiState.farm,
                isLoading = uiState.isLoading
            )
            
            QuickMetricsSection(
                metrics = uiState.getQuickMetrics(),
                onMetricClick = { metric ->
                    when (metric) {
                        "analytics" -> onNavigateToAnalytics()
                        "tasks" -> onNavigateToTasks()
                    }
                },
                isLoading = uiState.isLoading
            )
            
            ActiveFlocksSection(
                flocks = uiState.flocks,
                onFlockClick = onNavigateToFlockDetail,
                onAddFlockClick = onNavigateToAddFlock,
                isLoading = uiState.isLoading
            )
        }

        // Right Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.healthAlerts.isNotEmpty()) {
                HealthAlertsSection(
                    alerts = uiState.healthAlerts,
                    onAlertClick = { /* Handle alert click */ }
                )
            }
            
            QuickActionsSection(
                onAction = onQuickAction
            )
            
            if (uiState.upcomingTasks.isNotEmpty()) {
                UpcomingTasksSection(
                    tasks = uiState.upcomingTasks,
                    onTaskClick = { /* Handle task click */ },
                    onViewAllClick = onNavigateToTasks
                )
            }
            
            RecentActivitiesSection(
                activities = uiState.recentActivities
            )
        }
    }
}

@Composable
private fun FarmOverviewSection(
    farm: Farm?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = farm?.farmName ?: "My Farm",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = farm?.location ?: "Location not set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Farm Status Badge
                    AssistChip(
                        onClick = { },
                        label = { Text("Verified") }
                    )
                }

                // Farm Metrics Row
                farm?.let { farmData ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FarmMetricItem(
                            label = "Area",
                            value = "${farmData.totalArea} ha",
                            icon = Icons.Default.Landscape
                        )
                        FarmMetricItem(
                            label = "Capacity",
                            value = "1000",
                            icon = Icons.Default.Home
                        )
                        FarmMetricItem(
                            label = "Occupancy",
                            value = "85%",
                            icon = Icons.Default.PieChart
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FarmMetricItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickMetricsSection(
    metrics: List<DashboardMetric>,
    onMetricClick: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Farm Overview",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(metrics) { metric ->
                MetricCard(
                    metric = metric,
                    onClick = { onMetricClick(metric.id) },
                    modifier = Modifier.width(150.dp),
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    metric: DashboardMetric,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = metric.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HealthAlertsSection(
    alerts: List<HealthAlert>,
    onAlertClick: (HealthAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Health Alerts",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        alerts.take(3).forEach { alert ->
            Card(
                onClick = { onAlertClick(alert) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (alerts.size > 3) {
            TextButton(
                onClick = { /* Navigate to all alerts */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View All Alerts")
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val actions = listOf(
            QuickAction("Add Fowl", Icons.Default.Add, "add_fowl"),
            QuickAction("Record Vaccination", Icons.Default.MedicalServices, "vaccination"),
            QuickAction("Update Growth", Icons.Default.TrendingUp, "growth"),
            QuickAction("Manage Feeding", Icons.Default.Restaurant, "feeding"),
            QuickAction("Health Check", Icons.Default.HealthAndSafety, "health_check"),
            QuickAction("View Reports", Icons.Default.Assessment, "reports")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(actions) { action ->
                QuickActionCard(
                    action = action,
                    onClick = { onAction(action.id) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = action.title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ActiveFlocksSection(
    flocks: List<Flock>,
    onFlockClick: (String) -> Unit,
    onAddFlockClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Flocks",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onAddFlockClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new flock"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(bottom = 8.dp)
                    )
                }
            }
            flocks.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAddFlockClick
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Active Flocks",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Create your first flock to start managing your fowls",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(flocks.take(5)) { flock ->
                        FlockCard(
                            flock = flock,
                            onClick = { onFlockClick(flock.id) },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }

                if (flocks.size > 5) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { /* Navigate to all flocks */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View All Flocks")
                    }
                }
            }
        }
    }
}

@Composable
private fun FlockCard(
    flock: Flock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = flock.flockName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { },
                    label = { Text(flock.healthStatus.displayName) }
                )
            }

            Text(
                text = "${flock.breed} • ${flock.flockType.displayName}",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${flock.activeCount} Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${flock.totalCount} Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpcomingTasksSection(
    tasks: List<UpcomingTask>,
    onTaskClick: (UpcomingTask) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Tasks",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onViewAllClick) {
                Text("View All")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        tasks.take(3).forEach { task ->
            TaskItem(
                task = task,
                onClick = { onTaskClick(task) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun TaskItem(
    task: UpcomingTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Task,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Due Soon",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            AssistChip(
                onClick = { },
                label = { Text(task.priority.name) }
            )
        }
    }
}

@Composable
private fun RecentActivitiesSection(
    activities: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (activities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Recent Activity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Farm activities will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            activities.take(5).forEach { activity ->
                ActivityItem(
                    activity = activity,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(
    activity: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraLarge
                )
        )
        Text(
            text = activity,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun DashboardErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error Loading Dashboard",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

// Extension function for UI state
private fun FarmDashboardUiState.getQuickMetrics(): List<DashboardMetric> {
    return listOf(
        DashboardMetric(
            id = "total_fowls",
            title = "Total Fowls",
            value = totalFowls.toString(),
            icon = Icons.Default.Pets
        ),
        DashboardMetric(
            id = "active_flocks",
            title = "Active Flocks",
            value = activeFlocks.toString(),
            icon = Icons.Default.Group
        ),
        DashboardMetric(
            id = "breeding_stock",
            title = "Breeding Stock",
            value = breedingStock.toString(),
            icon = Icons.Default.Favorite
        ),
        DashboardMetric(
            id = "daily_eggs",
            title = "Daily Eggs",
            value = dailyEggProduction.toString(),
            icon = Icons.Default.Circle
        )
    )
}


data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val id: String
)

data class DashboardMetric(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector
)
