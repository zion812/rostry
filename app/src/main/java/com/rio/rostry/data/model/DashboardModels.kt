package com.rio.rostry.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health alert model for dashboard notifications
 */
@Entity(tableName = "health_alerts")
data class HealthAlert(
    @PrimaryKey
    val id: String = "",
    val farmId: String = "",
    val flockId: String? = null,
    val fowlId: String? = null,
    val title: String = "",
    val description: String = "",
    val severity: AlertSeverity = AlertSeverity.LOW,
    val category: AlertCategory = AlertCategory.HEALTH,
    val isRead: Boolean = false,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val actionRequired: Boolean = false,
    val actionUrl: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val isActive: Boolean get() = !isResolved
    val isUrgent: Boolean get() = severity in listOf(AlertSeverity.HIGH, AlertSeverity.CRITICAL)
}

/**
 * Alert severity levels
 */
enum class AlertSeverity(val displayName: String, val priority: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

/**
 * Alert categories
 */
enum class AlertCategory(val displayName: String, val icon: ImageVector) {
    HEALTH("Health", Icons.Default.HealthAndSafety),
    VACCINATION("Vaccination", Icons.Default.MedicalServices),
    FEEDING("Feeding", Icons.Default.Restaurant),
    ENVIRONMENT("Environment", Icons.Default.Thermostat),
    BREEDING("Breeding", Icons.Default.Favorite),
    SECURITY("Security", Icons.Default.Security),
    MAINTENANCE("Maintenance", Icons.Default.Build),
    SYSTEM("System", Icons.Default.Settings)
}

/**
 * Upcoming task model for dashboard
 */
@Entity(tableName = "upcoming_tasks")
data class UpcomingTask(
    @PrimaryKey
    val id: String = "",
    val farmId: String = "",
    val flockId: String? = null,
    val fowlId: String? = null,
    val title: String = "",
    val description: String = "",
    val taskType: TaskType = TaskType.GENERAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val assignedTo: String? = null,
    val assignedBy: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val estimatedDuration: Int = 30, // minutes
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null,
    val completedAt: Long? = null,
    val completedBy: String? = null,
    val notes: String = "",
    val attachments: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean get() = dueDate < System.currentTimeMillis() && status != TaskStatus.COMPLETED
    val isDueToday: Boolean get() = dueDate < System.currentTimeMillis() + 86400000 // 24 hours
    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
}

/**
 * Task types
 */
enum class TaskType(val displayName: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Task),
    FEEDING("Feeding", Icons.Default.Restaurant),
    VACCINATION("Vaccination", Icons.Default.MedicalServices),
    HEALTH_CHECK("Health Check", Icons.Default.HealthAndSafety),
    CLEANING("Cleaning", Icons.Default.CleaningServices),
    MAINTENANCE("Maintenance", Icons.Default.Build),
    BREEDING("Breeding", Icons.Default.Favorite),
    RECORD_KEEPING("Record Keeping", Icons.Default.Assignment),
    INSPECTION("Inspection", Icons.Default.Visibility),
    EMERGENCY("Emergency", Icons.Default.Emergency)
}

/**
 * Task priority levels
 */
enum class TaskPriority(val displayName: String, val color: String) {
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FF9800"),
    HIGH("High", "#F44336"),
    URGENT("Urgent", "#9C27B0")
}

/**
 * Task status
 */
enum class TaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    OVERDUE("Overdue")
}

/**
 * Dashboard metric model
 */
data class DashboardMetric(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val trend: MetricTrend? = null,
    val trendValue: Double? = null,
    val isClickable: Boolean = true,
    val category: MetricCategory = MetricCategory.GENERAL
)

/**
 * Metric trend direction
 */
enum class MetricTrend {
    UP, DOWN, STABLE
}

/**
 * Metric categories
 */
enum class MetricCategory {
    GENERAL, PRODUCTION, HEALTH, FINANCIAL, BREEDING
}
/**
 * Performance rating enum
 */
enum class PerformanceRating(val displayName: String, val color: String) {
    OUTSTANDING("Outstanding", "#4CAF50"),
    EXCELLENT("Excellent", "#8BC34A"),
    GOOD("Good", "#CDDC39"),
    AVERAGE("Average", "#FF9800"),
    BELOW_AVERAGE("Below Average", "#F44336")
}

/**
 * Quick action model for dashboard
 */
data class QuickAction(
    val id: String,
    val title: String,
    val description: String = "",
    val icon: ImageVector,
    val category: ActionCategory = ActionCategory.GENERAL,
    val isEnabled: Boolean = true,
    val requiresPermission: String? = null
)

/**
 * Action categories
 */
enum class ActionCategory {
    GENERAL, FOWL_MANAGEMENT, HEALTH, BREEDING, RECORDS, ANALYTICS
}

/**
 * Navigation item data for role-based navigation
 */
data class NavigationItemData(
    val id: String,
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isEnabled: Boolean = true,
    val badgeCount: Int = 0
)

/**
 * Navigation section for drawer/rail navigation
 */
data class NavigationSection(
    val title: String,
    val items: List<NavigationItemData>
)

/**
 * Simple navigation item for bottom navigation
 */
data class SimpleNavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * Farm statistics summary
 */
data class FarmStatistics(
    val totalFowls: Int = 0,
    val activeFlocks: Int = 0,
    val breedingStock: Int = 0,
    val dailyEggProduction: Int = 0,
    val monthlyEggProduction: Int = 0,
    val healthScore: Double = 0.0,
    val productionEfficiency: Double = 0.0,
    val facilityUtilization: Double = 0.0,
    val averageWeight: Double = 0.0,
    val mortalityRate: Double = 0.0,
    val feedConversionRatio: Double = 0.0,
    val profitMargin: Double = 0.0
)

/**
 * Environmental conditions for monitoring
 */
data class EnvironmentalConditions(
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val airQuality: Double = 100.0,
    val lightLevel: Double = 0.0,
    val noiseLevel: Double = 0.0,
    val ventilationRate: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
