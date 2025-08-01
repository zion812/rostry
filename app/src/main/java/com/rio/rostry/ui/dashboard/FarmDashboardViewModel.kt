package com.rio.rostry.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.FarmRepository
import com.rio.rostry.data.repository.LifecycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for farm dashboard screen
 * Manages farm overview data and quick actions
 */
@HiltViewModel
class FarmDashboardViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
    private val lifecycleRepository: LifecycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmDashboardUiState())
    val uiState: StateFlow<FarmDashboardUiState> = _uiState.asStateFlow()

    /**
     * Load comprehensive dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load data step by step to avoid complex combine type inference issues
                loadFarmData()
                loadFlockData()
                loadLifecycleData()
                loadAlertsAndTasks()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    /**
     * Load farm data
     */
    private suspend fun loadFarmData() {
        farmRepository.getCurrentFarm().collect { farm ->
            _uiState.value = _uiState.value.copy(farm = farm)
        }
    }

    /**
     * Load flock data and calculate metrics
     */
    private suspend fun loadFlockData() {
        farmRepository.getAllFlocks().collect { flocks ->
            val totalFowls = flocks.sumOf { it.activeCount }
            val dailyEggProduction = flocks
                .filter { it.flockType == FlockType.LAYING_HENS }
                .sumOf { flock ->
                    flock.productionMetrics?.let { metrics ->
                        (metrics.eggProductionRate * flock.activeCount).toInt()
                    } ?: 0
                }

            _uiState.value = _uiState.value.copy(
                flocks = flocks,
                totalFowls = totalFowls,
                activeFlocks = flocks.size,
                dailyEggProduction = dailyEggProduction
            )
        }
    }

    /**
     * Load lifecycle data and calculate breeding stock
     */
    private suspend fun loadLifecycleData() {
        lifecycleRepository.getAllLifecycles().collect { lifecycles ->
            val breedingStock = lifecycles.count { 
                it.currentStage in listOf(LifecycleStage.ADULT, LifecycleStage.BREEDER_ACTIVE) 
            }

            _uiState.value = _uiState.value.copy(
                breedingStock = breedingStock,
                isLoading = false
            )
        }
    }

    /**
     * Load alerts and tasks
     */
    private suspend fun loadAlertsAndTasks() {
        // Load health alerts
        farmRepository.getHealthAlerts().collect { alerts ->
            _uiState.value = _uiState.value.copy(healthAlerts = alerts)
        }

        // Load upcoming tasks
        farmRepository.getUpcomingTasks().collect { tasks ->
            _uiState.value = _uiState.value.copy(upcomingTasks = tasks)
        }

        // Load recent activities
        farmRepository.getRecentActivities().collect { activities ->
            _uiState.value = _uiState.value.copy(recentActivities = activities)
        }
    }

    /**
     * Refresh dashboard data
     */
    fun refreshData() {
        loadDashboardData()
    }

    /**
     * Edit farm information
     */
    fun editFarm() {
        // Navigate to farm edit screen or show dialog
        // Implementation depends on navigation setup
    }

    /**
     * Record vaccination
     */
    fun recordVaccination() {
        // Navigate to vaccination recording screen
        // Implementation depends on navigation setup
    }

    /**
     * Update growth metrics
     */
    fun updateGrowth() {
        // Navigate to growth update screen
        // Implementation depends on navigation setup
    }

    /**
     * Manage feeding schedules
     */
    fun manageFeeding() {
        // Navigate to feeding management screen
        // Implementation depends on navigation setup
    }

    /**
     * Create new flock
     */
    fun createFlock() {
        viewModelScope.launch {
            try {
                // This would typically open a dialog or navigate to a form
                // For now, we'll create a sample flock
                val newFlock = Flock(
                    farmId = _uiState.value.farm?.id ?: "",
                    flockName = "New Flock",
                    flockType = FlockType.MIXED,
                    breed = "Mixed Breed",
                    totalCount = 0
                )

                val result = farmRepository.createFlock(newFlock)
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to create flock"
                    )
                } else {
                    // Refresh data to show new flock
                    refreshData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create flock"
                )
            }
        }
    }

    /**
     * Handle health alert
     */
    fun handleAlert(alert: String) {
        viewModelScope.launch {
            try {
                // Mark alert as handled or navigate to detailed view
                farmRepository.markAlertAsHandled(alert)
                refreshData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to handle alert"
                )
            }
        }
    }

    /**
     * Complete task
     */
    fun completeTask(task: String) {
        viewModelScope.launch {
            try {
                farmRepository.completeTask(task)
                refreshData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to complete task"
                )
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Get farm performance summary
     */
    fun getFarmPerformanceSummary(): FarmPerformanceSummary {
        val state = _uiState.value
        
        return FarmPerformanceSummary(
            totalFowls = state.totalFowls,
            activeFlocks = state.activeFlocks,
            breedingStock = state.breedingStock,
            dailyEggProduction = state.dailyEggProduction,
            healthScore = calculateHealthScore(state.flocks, state.healthAlerts),
            productionEfficiency = calculateProductionEfficiency(state.flocks),
            facilityUtilization = state.farm?.getOccupancyRate() ?: 0.0
        )
    }

    private fun calculateHealthScore(flocks: List<Flock>, alerts: List<String>): Double {
        if (flocks.isEmpty()) return 100.0
        
        val healthyFlocks = flocks.count { it.healthStatus == FlockHealthStatus.HEALTHY }
        val baseScore = (healthyFlocks.toDouble() / flocks.size) * 100
        val alertPenalty = alerts.size * 5.0 // 5% penalty per alert
        
        return (baseScore - alertPenalty).coerceAtLeast(0.0)
    }

    private fun calculateProductionEfficiency(flocks: List<Flock>): Double {
        val layingFlocks = flocks.filter { it.flockType == FlockType.LAYING_HENS }
        if (layingFlocks.isEmpty()) return 0.0
        
        val averageProductionRate = layingFlocks.mapNotNull { flock ->
            flock.productionMetrics?.eggProductionRate
        }.average()
        
        return (averageProductionRate * 100).coerceIn(0.0, 100.0)
    }
}

/**
 * UI state for farm dashboard screen
 */
data class FarmDashboardUiState(
    val isLoading: Boolean = false,
    val farm: Farm? = null,
    val flocks: List<Flock> = emptyList(),
    val totalFowls: Int = 0,
    val activeFlocks: Int = 0,
    val breedingStock: Int = 0,
    val dailyEggProduction: Int = 0,
    val healthAlerts: List<String> = emptyList(),
    val upcomingTasks: List<String> = emptyList(),
    val recentActivities: List<String> = emptyList(),
    val error: String? = null
)

/**
 * Combined dashboard data container
 */
private data class FarmDashboardData(
    val farm: Farm?,
    val flocks: List<Flock>,
    val totalFowls: Int,
    val breedingStock: Int,
    val dailyEggProduction: Int,
    val healthAlerts: List<String>,
    val upcomingTasks: List<String>,
    val recentActivities: List<String>
)

/**
 * Farm performance summary for analytics
 */
data class FarmPerformanceSummary(
    val totalFowls: Int,
    val activeFlocks: Int,
    val breedingStock: Int,
    val dailyEggProduction: Int,
    val healthScore: Double,
    val productionEfficiency: Double,
    val facilityUtilization: Double
) {
    /**
     * Calculate overall farm score
     */
    fun getOverallScore(): Double {
        return (healthScore * 0.3 + 
                productionEfficiency * 0.3 + 
                facilityUtilization * 0.2 + 
                (if (totalFowls > 0) 20.0 else 0.0)) // Base score for having fowls
    }

    /**
     * Get performance rating
     */
    fun getPerformanceRating(): PerformanceRating {
        val score = getOverallScore()
        return when {
            score >= 90 -> PerformanceRating.OUTSTANDING
            score >= 80 -> PerformanceRating.EXCELLENT
            score >= 70 -> PerformanceRating.GOOD
            score >= 60 -> PerformanceRating.AVERAGE
            else -> PerformanceRating.BELOW_AVERAGE
        }
    }
}