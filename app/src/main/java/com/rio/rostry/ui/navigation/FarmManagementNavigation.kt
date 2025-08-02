package com.rio.rostry.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rio.rostry.ui.analytics.LifecycleAnalyticsScreen
import com.rio.rostry.ui.dashboard.FarmDashboardScreen

/**
 * Navigation graph for the comprehensive farm management system
 * Integrates all farm management screens and features
 */
@Composable
fun FarmManagementNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = FarmManagementDestinations.DASHBOARD
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Farm Dashboard - Main entry point
        composable(FarmManagementDestinations.DASHBOARD) {
            FarmDashboardScreen(
                onNavigateToFlockDetail = { flockId ->
                    navController.navigate("${FarmManagementDestinations.FLOCK_DETAIL}/$flockId")
                },
                onNavigateToAddFowl = {
                    navController.navigate(FarmManagementDestinations.ADD_FOWL)
                },
                onNavigateToAnalytics = {
                    navController.navigate(FarmManagementDestinations.ANALYTICS)
                },
                onNavigateToLifecycleManagement = {
                    navController.navigate(FarmManagementDestinations.LIFECYCLE_MANAGEMENT)
                },
                onNavigateToFarmSettings = {
                    navController.navigate(FarmManagementDestinations.FARM_SETTINGS)
                }
            )
        }

        // Lifecycle Analytics
        composable(FarmManagementDestinations.ANALYTICS) {
            LifecycleAnalyticsScreen()
        }

        // Lifecycle Management
        composable(FarmManagementDestinations.LIFECYCLE_MANAGEMENT) {
            // LifecycleManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Farm Settings
        composable(FarmManagementDestinations.FARM_SETTINGS) {
            // FarmSettingsScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Flock Detail
        composable("${FarmManagementDestinations.FLOCK_DETAIL}/{flockId}") { backStackEntry ->
            val flockId = backStackEntry.arguments?.getString("flockId") ?: ""
            // FlockDetailScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Breeding Management
        composable(FarmManagementDestinations.BREEDING_MANAGEMENT) {
            // BreedingManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Add Fowl
        composable(FarmManagementDestinations.ADD_FOWL) {
            // AddFowlScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Facility Management
        composable(FarmManagementDestinations.FACILITY_MANAGEMENT) {
            // FacilityManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // User Management
        composable(FarmManagementDestinations.USER_MANAGEMENT) {
            // UserManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Data Backup
        composable(FarmManagementDestinations.DATA_BACKUP) {
            // DataBackupScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Vaccination Management
        composable(FarmManagementDestinations.VACCINATION_MANAGEMENT) {
            // VaccinationManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Feeding Management
        composable(FarmManagementDestinations.FEEDING_MANAGEMENT) {
            // FeedingManagementScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }

        // Reports
        composable(FarmManagementDestinations.REPORTS) {
            // ReportsScreen implementation would go here
            // For now, navigate back to dashboard
            navController.popBackStack()
        }
    }
}

/**
 * Farm management navigation destinations
 */
object FarmManagementDestinations {
    const val DASHBOARD = "farm_dashboard"
    const val ANALYTICS = "lifecycle_analytics"
    const val LIFECYCLE_MANAGEMENT = "lifecycle_management"
    const val FARM_SETTINGS = "farm_settings"
    const val FLOCK_DETAIL = "flock_detail"
    const val BREEDING_MANAGEMENT = "breeding_management"
    const val ADD_FOWL = "add_fowl"
    const val FACILITY_MANAGEMENT = "facility_management"
    const val USER_MANAGEMENT = "user_management"
    const val DATA_BACKUP = "data_backup"
    const val VACCINATION_MANAGEMENT = "vaccination_management"
    const val FEEDING_MANAGEMENT = "feeding_management"
    const val REPORTS = "reports"
}

/**
 * Navigation actions for farm management
 */
class FarmManagementNavigationActions(private val navController: NavHostController) {
    
    fun navigateToDashboard() {
        navController.navigate(FarmManagementDestinations.DASHBOARD) {
            popUpTo(FarmManagementDestinations.DASHBOARD) { inclusive = true }
        }
    }
    
    fun navigateToAnalytics() {
        navController.navigate(FarmManagementDestinations.ANALYTICS)
    }
    
    fun navigateToLifecycleManagement() {
        navController.navigate(FarmManagementDestinations.LIFECYCLE_MANAGEMENT)
    }
    
    fun navigateToFarmSettings() {
        navController.navigate(FarmManagementDestinations.FARM_SETTINGS)
    }
    
    fun navigateToFlockDetail(flockId: String) {
        navController.navigate("${FarmManagementDestinations.FLOCK_DETAIL}/$flockId")
    }
    
    fun navigateToBreedingManagement() {
        navController.navigate(FarmManagementDestinations.BREEDING_MANAGEMENT)
    }
    
    fun navigateToAddFowl() {
        navController.navigate(FarmManagementDestinations.ADD_FOWL)
    }
    
    fun navigateToFacilityManagement() {
        navController.navigate(FarmManagementDestinations.FACILITY_MANAGEMENT)
    }
    
    fun navigateToUserManagement() {
        navController.navigate(FarmManagementDestinations.USER_MANAGEMENT)
    }
    
    fun navigateToDataBackup() {
        navController.navigate(FarmManagementDestinations.DATA_BACKUP)
    }
    
    fun navigateToVaccinationManagement() {
        navController.navigate(FarmManagementDestinations.VACCINATION_MANAGEMENT)
    }
    
    fun navigateToFeedingManagement() {
        navController.navigate(FarmManagementDestinations.FEEDING_MANAGEMENT)
    }
    
    fun navigateToReports() {
        navController.navigate(FarmManagementDestinations.REPORTS)
    }
    
    fun navigateBack() {
        navController.popBackStack()
    }
}

/**
 * Extension functions for easier navigation
 */
fun NavHostController.navigateToFarmDashboard() {
    navigate(FarmManagementDestinations.DASHBOARD) {
        popUpTo(FarmManagementDestinations.DASHBOARD) { inclusive = true }
    }
}

fun NavHostController.navigateToFarmAnalytics() {
    navigate(FarmManagementDestinations.ANALYTICS)
}

fun NavHostController.navigateToLifecycleManagement() {
    navigate(FarmManagementDestinations.LIFECYCLE_MANAGEMENT)
}

fun NavHostController.navigateToFarmSettings() {
    navigate(FarmManagementDestinations.FARM_SETTINGS)
}

fun NavHostController.navigateToFlockDetail(flockId: String) {
    navigate("${FarmManagementDestinations.FLOCK_DETAIL}/$flockId")
}

fun NavHostController.navigateToBreedingManagement() {
    navigate(FarmManagementDestinations.BREEDING_MANAGEMENT)
}