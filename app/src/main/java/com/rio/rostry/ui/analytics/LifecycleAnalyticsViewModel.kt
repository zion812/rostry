package com.rio.rostry.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.repository.LifecycleRepository
import com.rio.rostry.data.repository.LifecycleAnalytics
import com.rio.rostry.data.repository.BloodlineAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for lifecycle analytics screen
 * Manages analytics data loading and state management
 */
@HiltViewModel
class LifecycleAnalyticsViewModel @Inject constructor(
    private val lifecycleRepository: LifecycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LifecycleAnalyticsUiState())
    val uiState: StateFlow<LifecycleAnalyticsUiState> = _uiState.asStateFlow()

    /**
     * Load comprehensive analytics data
     */
    fun loadAnalyticsData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                combine(
                    lifecycleRepository.getLifecycleAnalytics(),
                    lifecycleRepository.getBloodlineAnalytics()
                ) { lifecycleAnalytics, bloodlineAnalytics ->
                    AnalyticsData(lifecycleAnalytics, bloodlineAnalytics)
                }.collect { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        analytics = data.lifecycleAnalytics,
                        bloodlineAnalytics = data.bloodlineAnalytics,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Refresh analytics data
     */
    fun refreshData() {
        loadAnalyticsData()
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for lifecycle analytics screen
 */
data class LifecycleAnalyticsUiState(
    val isLoading: Boolean = false,
    val analytics: LifecycleAnalytics? = null,
    val bloodlineAnalytics: List<BloodlineAnalytics> = emptyList(),
    val error: String? = null
)

/**
 * Combined analytics data container
 */
private data class AnalyticsData(
    val lifecycleAnalytics: LifecycleAnalytics,
    val bloodlineAnalytics: List<BloodlineAnalytics>
)