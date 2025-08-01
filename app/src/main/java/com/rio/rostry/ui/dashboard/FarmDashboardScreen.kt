package com.rio.rostry.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rio.rostry.data.model.*
import com.rio.rostry.ui.components.*
import com.rio.rostry.util.formatDate

/**
 * Comprehensive farm dashboard screen with overview and management features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmDashboardScreen(
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFowl: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToLifecycleManagement: () -> Unit,
    onNavigateToFarmSettings: () -> Unit,
    viewModel: FarmDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Farm Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToFarmSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Farm Settings"
                        )
                    }
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.farm == null -> {
                DashboardLoadingState(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null -> {
                DashboardErrorState(
                    error = uiState.error,
                    onRetry = { viewModel.refreshData() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                DashboardContent(
                    uiState = uiState,
                    onNavigateToFlockDetail = onNavigateToFlockDetail,
                    onNavigateToAddFowl = onNavigateToAddFowl,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToLifecycleManagement = onNavigateToLifecycleManagement,
                    onEditFarm = { viewModel.editFarm() },
                    onRecordVaccination = { viewModel.recordVaccination() },
                    onUpdateGrowth = { viewModel.updateGrowth() },
                    onManageFeeding = { viewModel.manageFeeding() },
                    onCreateFlock = { viewModel.createFlock() },
                    onHandleAlert = { viewModel.handleAlert(it) },
                    onCompleteTask = { viewModel.completeTask(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: FarmDashboardUiState,
    onNavigateToFlockDetail: (String) -> Unit,
    onNavigateToAddFowl: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToLifecycleManagement: () -> Unit,
    onEditFarm: () -> Unit,
    onRecordVaccination: () -> Unit,
    onUpdateGrowth: () -> Unit,
    onManageFeeding: () -> Unit,
    onCreateFlock: () -> Unit,
    onHandleAlert: (String) -> Unit,
    onCompleteTask: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FarmHeaderCard(
                farm = uiState.farm,
                onEditFarm = onEditFarm
            )
        }

        item {
            FarmOverviewCards(
                totalFowls = uiState.totalFowls,
                activeFlocks = uiState.activeFlocks,
                breedingStock = uiState.breedingStock,
                eggProduction = uiState.dailyEggProduction,
                onViewAnalytics = onNavigateToAnalytics
            )
        }

        item {
            QuickActionsRow(
                onAddFowl = onNavigateToAddFowl,
                onRecordVaccination = onRecordVaccination,
                onUpdateGrowth = onUpdateGrowth,
                onManageFeeding = onManageFeeding,
                onLifecycleManagement = onNavigateToLifecycleManagement
            )
        }

        item {
            FlockManagementSection(
                flocks = uiState.flocks,
                onFlockClick = onNavigateToFlockDetail,
                onCreateFlock = onCreateFlock
            )
        }

        if (uiState.healthAlerts.isNotEmpty()) {
            item {
                HealthAlertsCard(
                    alerts = uiState.healthAlerts,
                    onAlertClick = onHandleAlert
                )
            }
        }

        if (uiState.upcomingTasks.isNotEmpty()) {
            item {
                UpcomingTasksCard(
                    tasks = uiState.upcomingTasks,
                    onTaskComplete = onCompleteTask
                )
            }
        }

        item {
            RecentActivityCard(
                activities = uiState.recentActivities
            )
        }
    }
}

@Composable
private fun FarmHeaderCard(
    farm: Farm?,
    onEditFarm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = farm?.farmName ?: "My Farm",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = farm?.location ?: "Location not set",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (farm?.verificationStatus == VerificationStatus.VERIFIED) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("Verified", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = farm?.certificationLevel?.displayName ?: "Basic",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                IconButton(onClick = onEditFarm) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Farm"
                    )
                }
            }

            farm?.let { farmData ->
                Spacer(modifier = Modifier.height(16.dp))
                
                // Farm metrics
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
                        value = farmData.getTotalCapacity().toString(),
                        icon = Icons.Default.Home
                    )
                    FarmMetricItem(
                        label = "Occupancy",
                        value = "${farmData.getOccupancyRate().toInt()}%",
                        icon = Icons.Default.PieChart
                    )
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FarmOverviewCards(
    totalFowls: Int,
    activeFlocks: Int,
    breedingStock: Int,
    eggProduction: Int,
    onViewAnalytics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Farm Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewAnalytics) {
                    Text("View Analytics")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OverviewMetricCard(
                        title = "Total Fowls",
                        value = totalFowls.toString(),
                        icon = Icons.Default.Home,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    OverviewMetricCard(
                        title = "Active Flocks",
                        value = activeFlocks.toString(),
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    OverviewMetricCard(
                        title = "Breeding Stock",
                        value = breedingStock.toString(),
                        icon = Icons.Default.Favorite,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    OverviewMetricCard(
                        title = "Daily Eggs",
                        value = eggProduction.toString(),
                        icon = Icons.Default.Circle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAddFowl: () -> Unit,
    onRecordVaccination: () -> Unit,
    onUpdateGrowth: () -> Unit,
    onManageFeeding: () -> Unit,
    onLifecycleManagement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickActionCard(
                        title = "Add Fowl",
                        icon = Icons.Default.Add,
                        onClick = onAddFowl
                    )
                }
                item {
                    QuickActionCard(
                        title = "Vaccination",
                        icon = Icons.Default.MedicalServices,
                        onClick = onRecordVaccination
                    )
                }
                item {
                    QuickActionCard(
                        title = "Growth",
                        icon = Icons.Default.ArrowUpward,
                        onClick = onUpdateGrowth
                    )
                }
                item {
                    QuickActionCard(
                        title = "Feeding",
                        icon = Icons.Default.Restaurant,
                        onClick = onManageFeeding
                    )
                }
                item {
                    QuickActionCard(
                        title = "Lifecycle",
                        icon = Icons.Default.Timeline,
                        onClick = onLifecycleManagement
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun FlockManagementSection(
    flocks: List<Flock>,
    onFlockClick: (String) -> Unit,
    onCreateFlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flock Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onCreateFlock) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Flock")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (flocks.isEmpty()) {
                EmptyStates.NoData(
                    title = "No Flocks",
                    description = "Create your first flock to start managing your fowls",
                    actionText = "Create Flock",
                    onAction = onCreateFlock
                )
            } else {
                flocks.take(3).forEach { flock ->
                    FlockCard(
                        flock = flock,
                        onClick = { onFlockClick(flock.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (flocks.size > 3) {
                    TextButton(
                        onClick = { /* Navigate to full flock list */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View All ${flocks.size} Flocks")
                    }
                }
            }
        }
    }
}

@Composable
private fun FlockCard(
    flock: Flock,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flock.flockName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${flock.flockType.displayName} • ${flock.activeCount}/${flock.totalCount} active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Badge(
                containerColor = when (flock.healthStatus) {
                    FlockHealthStatus.HEALTHY -> MaterialTheme.colorScheme.primary
                    FlockHealthStatus.MONITORING -> MaterialTheme.colorScheme.secondary
                    FlockHealthStatus.TREATMENT -> MaterialTheme.colorScheme.error
                    FlockHealthStatus.QUARANTINE -> MaterialTheme.colorScheme.error
                    FlockHealthStatus.RECOVERED -> MaterialTheme.colorScheme.tertiary
                }
            ) {
                Text(
                    text = flock.healthStatus.displayName,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun HealthAlertsCard(
    alerts: List<String>,
    onAlertClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            alerts.take(3).forEach { alert ->
                AlertItem(
                    alert = alert,
                    onClick = { onAlertClick(alert) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AlertItem(
    alert: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = alert,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun UpcomingTasksCard(
    tasks: List<String>,
    onTaskComplete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            tasks.take(3).forEach { task ->
                TaskItem(
                    task = task,
                    onComplete = { onTaskComplete(task) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: String,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = false,
            onCheckedChange = { onComplete() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = task,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecentActivityCard(
    activities: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (activities.isEmpty()) {
                Text(
                    text = "No recent activity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activities.take(5).forEach { activity ->
                    ActivityItem(activity = activity)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = activity,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
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
        items(5) {
            ShimmerComponents.ShimmerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Error Loading Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}