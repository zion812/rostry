package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.FarmDao
import com.rio.rostry.data.local.dao.FlockDao
import com.rio.rostry.data.model.*
// Use specific import to avoid conflicts
import com.rio.rostry.data.model.AlertSeverity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for comprehensive farm management
 * Handles farm data, flock management, and operational analytics
 */
@Singleton
class FarmRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val farmDao: FarmDao,
    private val flockDao: FlockDao
) {

    // ==================== FARM MANAGEMENT ====================

    /**
     * Create a new farm
     */
    suspend fun createFarm(
        farmName: String,
        location: String,
        farmType: FarmType,
        ownerId: String,
        description: String = "",
        totalArea: Double = 0.0
    ): Result<String> {
        return try {
            val farm = Farm(
                ownerId = ownerId,
                farmName = farmName,
                location = location,
                farmType = farmType,
                description = description,
                totalArea = totalArea
            )

            // Save to Firestore
            firestore.collection("farms")
                .document(farm.id)
                .set(farm)
                .await()

            // Save locally
            farmDao.insertFarm(farm)

            Result.success(farm.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update farm information
     */
    suspend fun updateFarm(farm: Farm): Result<Unit> {
        return try {
            val updatedFarm = farm.copy(updatedAt = System.currentTimeMillis())

            // Update Firestore
            firestore.collection("farms")
                .document(farm.id)
                .set(updatedFarm)
                .await()

            // Update locally
            farmDao.updateFarm(updatedFarm)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user's farm
     */
    fun getCurrentFarm(): Flow<Farm?> {
        return farmDao.getCurrentUserFarm() // Assumes user ID is available
    }

    /**
     * Get farm by ID
     */
    suspend fun getFarmById(farmId: String): Farm? {
        return farmDao.getFarmById(farmId)
    }

    /**
     * Add facility to farm
     */
    suspend fun addFacility(farmId: String, facility: FarmFacility): Result<Unit> {
        return try {
            val farm = farmDao.getFarmById(farmId)
                ?: return Result.failure(Exception("Farm not found"))

            val updatedFarm = farm.copy(
                facilities = farm.facilities + facility,
                updatedAt = System.currentTimeMillis()
            )

            updateFarm(updatedFarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update facility condition
     */
    suspend fun updateFacilityCondition(
        farmId: String,
        facilityId: String,
        condition: FacilityCondition,
        notes: String = ""
    ): Result<Unit> {
        return try {
            val farm = farmDao.getFarmById(farmId)
                ?: return Result.failure(Exception("Farm not found"))

            val updatedFacilities = farm.facilities.map { facility ->
                if (facility.id == facilityId) {
                    facility.copy(
                        condition = condition,
                        notes = notes,
                        lastMaintenance = if (condition == FacilityCondition.EXCELLENT || condition == FacilityCondition.GOOD) {
                            System.currentTimeMillis()
                        } else {
                            facility.lastMaintenance
                        }
                    )
                } else {
                    facility
                }
            }

            val updatedFarm = farm.copy(
                facilities = updatedFacilities,
                updatedAt = System.currentTimeMillis()
            )

            updateFarm(updatedFarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FLOCK MANAGEMENT ====================

    /**
     * Create a new flock
     */
    suspend fun createFlock(flock: Flock): Result<String> {
        return try {
            // Save to Firestore
            firestore.collection("flocks")
                .document(flock.id)
                .set(flock)
                .await()

            // Save locally
            flockDao.insertFlock(flock)

            Result.success(flock.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update flock information
     */
    suspend fun updateFlock(flock: Flock): Result<Unit> {
        return try {
            val updatedFlock = flock.copy(updatedAt = System.currentTimeMillis())

            // Update Firestore
            firestore.collection("flocks")
                .document(flock.id)
                .set(updatedFlock)
                .await()

            // Update locally
            flockDao.updateFlock(updatedFlock)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all flocks for current farm
     */
    fun getAllFlocks(): Flow<List<Flock>> {
        return flockDao.getAllFlocks()
    }

    /**
     * Get flock by ID
     */
    suspend fun getFlockById(flockId: String): Flock? {
        return flockDao.getFlockById(flockId)
    }

    /**
     * Get flocks by type
     */
    fun getFlocksByType(flockType: FlockType): Flow<List<Flock>> {
        return flockDao.getFlocksByType(flockType)
    }

    /**
     * Update flock health status
     */
    suspend fun updateFlockHealthStatus(
        flockId: String,
        healthStatus: FlockHealthStatus,
        notes: String = ""
    ): Result<Unit> {
        return try {
            val flock = flockDao.getFlockById(flockId)
                ?: return Result.failure(Exception("Flock not found"))

            val updatedFlock = flock.copy(
                healthStatus = healthStatus,
                notes = if (notes.isNotEmpty()) "$notes\n${flock.notes}" else flock.notes,
                updatedAt = System.currentTimeMillis()
            )

            updateFlock(updatedFlock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add vaccination record
     */
    suspend fun addVaccinationRecord(
        flockId: String,
        vaccination: VaccinationRecord
    ): Result<Unit> {
        return try {
            val flock = flockDao.getFlockById(flockId)
                ?: return Result.failure(Exception("Flock not found"))

            val updatedFlock = flock.copy(
                vaccinationSchedule = flock.vaccinationSchedule + vaccination,
                updatedAt = System.currentTimeMillis()
            )

            updateFlock(updatedFlock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update feeding schedule
     */
    suspend fun updateFeedingSchedule(
        flockId: String,
        feedingSchedule: FeedingSchedule
    ): Result<Unit> {
        return try {
            val flock = flockDao.getFlockById(flockId)
                ?: return Result.failure(Exception("Flock not found"))

            val updatedFlock = flock.copy(
                feedingSchedule = feedingSchedule,
                updatedAt = System.currentTimeMillis()
            )

            updateFlock(updatedFlock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update production metrics
     */
    suspend fun updateProductionMetrics(
        flockId: String,
        metrics: ProductionMetrics
    ): Result<Unit> {
        return try {
            val flock = flockDao.getFlockById(flockId)
                ?: return Result.failure(Exception("Flock not found"))

            val updatedFlock = flock.copy(
                productionMetrics = metrics,
                updatedAt = System.currentTimeMillis()
            )

            updateFlock(updatedFlock)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== ANALYTICS AND MONITORING ====================

    /**
     * Get farm analytics
     */
    fun getFarmAnalytics(): Flow<FarmAnalytics> {
        return combine(
            getCurrentFarm(),
            getAllFlocks()
        ) { farm, flocks ->
            calculateFarmAnalytics(farm, flocks)
        }
    }

    /**
     * Get health alerts
     */
    fun getHealthAlerts(): Flow<List<HealthAlert>> {
        return combine(
            getAllFlocks(),
            getCurrentFarm()
        ) { flocks, farm ->
            val alerts = mutableListOf<HealthAlert>()

            // Check for unhealthy flocks
            flocks.forEach { flock ->
                when (flock.healthStatus) {
                    FlockHealthStatus.TREATMENT -> { // Changed from SICK
                        alerts.add(
                            HealthAlert(
                                id = "sick_${flock.id}",
                                title = "Sick Flock Alert",
                                description = "${flock.flockName} is showing signs of illness",
                                severity = AlertSeverity.HIGH,
                                flockId = flock.id,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }

                    FlockHealthStatus.QUARANTINE -> { // Changed from QUARANTINED
                        alerts.add(
                            HealthAlert(
                                id = "quarantine_${flock.id}",
                                title = "Quarantined Flock",
                                description = "${flock.flockName} is currently quarantined",
                                severity = AlertSeverity.MEDIUM,
                                flockId = flock.id,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }

                    FlockHealthStatus.MONITORING -> { // Changed from RECOVERING
                        alerts.add(
                            HealthAlert(
                                id = "recovering_${flock.id}",
                                title = "Flock Recovering",
                                description = "${flock.flockName} is recovering - monitor closely",
                                severity = AlertSeverity.LOW,
                                flockId = flock.id,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }

                    else -> { /* No alert needed */
                    }
                }

                // Check mortality rate
                val mortalityRate = flock.getMortalityRate()
                if (mortalityRate > 5.0) {
                    alerts.add(
                        HealthAlert(
                            id = "mortality_${flock.id}",
                            title = "High Mortality Rate",
                            description = "${flock.flockName} has ${mortalityRate.toInt()}% mortality rate",
                            severity = if (mortalityRate > 10.0) AlertSeverity.HIGH else AlertSeverity.MEDIUM,
                            flockId = flock.id,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                // Check production issues
                flock.productionMetrics?.let { metrics ->
                    if (metrics.eggProductionRate < 0.6) {
                        alerts.add(
                            HealthAlert(
                                id = "production_${flock.id}",
                                title = "Low Production Rate",
                                description = "${flock.flockName} production is below 60%",
                                severity = AlertSeverity.MEDIUM,
                                flockId = flock.id,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

            // Check facility conditions
            farm?.facilities?.forEach { facility ->
                if (facility.condition == FacilityCondition.POOR || facility.condition == FacilityCondition.NEEDS_REPAIR) { // Changed from CRITICAL
                    alerts.add(
                        HealthAlert(
                            id = "facility_${facility.id}",
                            title = "Facility Maintenance Required",
                            description = "${facility.name} is in ${facility.condition.name.lowercase()} condition",
                            severity = if (facility.condition == FacilityCondition.NEEDS_REPAIR) AlertSeverity.HIGH else AlertSeverity.MEDIUM,
                            flockId = null,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
            }

            alerts.sortedByDescending { it.severity.ordinal }
        }
    }

    /**
     * Get upcoming tasks
     */
    fun getUpcomingTasks(): Flow<List<UpcomingTask>> {
        return combine(
            getAllFlocks(),
            getCurrentFarm()
        ) { flocks, farm ->
            val tasks = mutableListOf<UpcomingTask>()
            val currentTime = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L
            7 * oneDayMs

            // Vaccination tasks
            flocks.forEach { flock ->
                flock.vaccinationSchedule.forEach { vaccination ->
                    val nextDueDate = vaccination.nextDueDate
                    if (nextDueDate != null && nextDueDate > currentTime) {
                        val daysUntilDue = (nextDueDate - currentTime) / oneDayMs
                        if (daysUntilDue <= 7) {
                            tasks.add(
                                UpcomingTask(
                                    id = "vaccination_${flock.id}_${vaccination.id}",
                                    title = "Vaccination Due",
                                    description = "Administer ${vaccination.vaccineName} to ${flock.flockName}",
                                    dueDate = nextDueDate,
                                    priority = if (daysUntilDue <= 1) TaskPriority.HIGH else TaskPriority.MEDIUM,
                                    taskType = TaskType.VACCINATION, // Use TaskType instead of category
                                    flockId = flock.id,
                                    farmId = flock.farmId, // Add missing farmId
                                    createdAt = System.currentTimeMillis() // Add missing createdAt
                                )
                            )
                        }
                    }
                }
            }

            // Health check tasks
            flocks.forEach { flock ->
                val daysSinceLastUpdate = (currentTime - flock.updatedAt) / oneDayMs
                if (daysSinceLastUpdate > 3) {
                    tasks.add(
                        UpcomingTask(
                            id = "health_check_${flock.id}",
                            title = "Health Check",
                            description = "Perform routine health check for ${flock.flockName}",
                            dueDate = flock.updatedAt + (3 * oneDayMs),
                            priority = TaskPriority.LOW,
                            taskType = TaskType.HEALTH_CHECK, // Use TaskType instead of category
                            flockId = flock.id,
                            farmId = flock.farmId, // Add missing farmId
                            createdAt = System.currentTimeMillis() // Add missing createdAt
                        )
                    )
                }
            }

            tasks.sortedBy { it.dueDate }
        }
    }

    /**
     * Get recent activities
     */
    fun getRecentActivities(): Flow<List<String>> {
        return combine(
            getAllFlocks(),
            getCurrentFarm()
        ) { flocks, farm ->
            val activities = mutableListOf<String>()

            // Recent flock updates
            flocks.sortedByDescending { it.updatedAt }.take(5).forEach { flock ->
                val daysSinceUpdate =
                    (System.currentTimeMillis() - flock.updatedAt) / (24 * 60 * 60 * 1000)
                if (daysSinceUpdate < 7) {
                    activities.add("Updated ${flock.flockName} ${daysSinceUpdate}d ago")
                }
            }

            // Recent vaccinations
            flocks.forEach { flock ->
                flock.vaccinationSchedule.sortedByDescending { it.administrationDate }.take(2)
                    .forEach { vaccination ->
                        val daysSinceVaccination =
                            (System.currentTimeMillis() - vaccination.administrationDate) / (24 * 60 * 60 * 1000)
                        if (daysSinceVaccination < 7) {
                            activities.add("Vaccinated ${flock.flockName} with ${vaccination.vaccineName} ${daysSinceVaccination}d ago")
                        }
                    }
            }

            activities.take(10)
        }
    }

    /**
     * Mark alert as handled
     */
    suspend fun markAlertAsHandled(alert: String): Result<Unit> {
        return try {
            // In a real implementation, you would store handled alerts
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Complete task
     */
    suspend fun completeTask(task: String): Result<Unit> {
        return try {
            // In a real implementation, you would mark tasks as completed
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== HELPER METHODS ====================

    private fun calculateFarmAnalytics(farm: Farm?, flocks: List<Flock>): FarmAnalytics {
        val totalFowls = flocks.sumOf { it.activeCount }
        val totalCapacity = farm?.getTotalCapacity() ?: 0
        val occupancyRate = farm?.getOccupancyRate() ?: 0.0

        val healthyFlocks = flocks.count { it.healthStatus == FlockHealthStatus.HEALTHY }
        val healthScore = if (flocks.isNotEmpty()) {
            (healthyFlocks.toDouble() / flocks.size) * 100
        } else 100.0

        val averageProductionRate = flocks
            .filter { it.flockType == FlockType.LAYING_HENS }
            .mapNotNull { it.productionMetrics?.eggProductionRate }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0

        val averageFeedConversion = flocks
            .mapNotNull { it.productionMetrics?.feedConversionRatio }
            .filter { it > 0 }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0

        val averageMortalityRate = flocks
            .map { it.getMortalityRate() }
            .average()
            .takeIf { !it.isNaN() } ?: 0.0

        return FarmAnalytics(
            farmId = farm?.id ?: "",
            totalFowls = totalFowls,
            totalFlocks = flocks.size,
            totalCapacity = totalCapacity,
            occupancyRate = occupancyRate,
            healthScore = healthScore,
            averageProductionRate = averageProductionRate,
            averageFeedConversion = averageFeedConversion,
            averageMortalityRate = averageMortalityRate,
            facilitiesNeedingMaintenance = farm?.getFacilitiesNeedingAttention()?.size ?: 0,
            lastCalculated = System.currentTimeMillis()
        )
    }
}

/**
 * Farm analytics data class
 */
data class FarmAnalytics(
    val farmId: String,
    val totalFowls: Int,
    val totalFlocks: Int,
    val totalCapacity: Int,
    val occupancyRate: Double,
    val healthScore: Double,
    val averageProductionRate: Double,
    val averageFeedConversion: Double,
    val averageMortalityRate: Double,
    val facilitiesNeedingMaintenance: Int,
    val lastCalculated: Long
) {
    /**
     * Calculate overall farm efficiency
     */
    fun getOverallEfficiency(): Double {
        val occupancyScore = (occupancyRate / 100).coerceIn(0.0, 1.0)
        val healthScoreNormalized = (healthScore / 100).coerceIn(0.0, 1.0)
        val productionScore = (averageProductionRate).coerceIn(0.0, 1.0)
        val feedEfficiencyScore = if (averageFeedConversion > 0) {
            (2.0 / averageFeedConversion).coerceIn(0.0, 1.0)
        } else 0.5
        val mortalityScore = ((100 - averageMortalityRate) / 100).coerceIn(0.0, 1.0)

        return (occupancyScore * 0.2 +
                healthScoreNormalized * 0.25 +
                productionScore * 0.25 +
                feedEfficiencyScore * 0.15 +
                mortalityScore * 0.15)
    }

    /**
     * Get efficiency rating
     */
    fun getEfficiencyRating(): PerformanceRating {
        val efficiency = getOverallEfficiency()
        return when {
            efficiency >= 0.9 -> PerformanceRating.OUTSTANDING
            efficiency >= 0.8 -> PerformanceRating.EXCELLENT
            efficiency >= 0.7 -> PerformanceRating.GOOD
            efficiency >= 0.6 -> PerformanceRating.AVERAGE
            else -> PerformanceRating.BELOW_AVERAGE
        }
    }
}