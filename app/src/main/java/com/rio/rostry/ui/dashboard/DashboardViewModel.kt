package com.rio.rostry.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.FlockSummary
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.repository.DashboardRepository
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val flockSummary: FlockSummary? = null,
    val recentFowls: List<Fowl> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val fowlRepository: FowlRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // For now, use a dummy user ID. In a real app, get from auth
                val userId = "dummy_user_id"
                
                // Load flock summary
                val flockSummary = dashboardRepository.getFlockSummary(userId)
                
                // Load recent fowls
                val recentFowls = fowlRepository.getRecentFowls(userId, 5)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    flockSummary = flockSummary,
                    recentFowls = recentFowls
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
}