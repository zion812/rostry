package com.rio.rostry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.manager.SessionManager
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.organization.Organization
import com.rio.rostry.data.model.role.UserRole
import com.rio.rostry.data.repository.AuthRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoleBasedNavigationState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val selectedFarm: Organization? = null,
    val selectedFarmId: String? = null,
    val accessibleFarms: List<Organization> = emptyList(),
    val permissionDeniedMessage: String? = null
)

@HiltViewModel
class RoleBasedNavigationViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoleBasedNavigationState())
    val uiState: StateFlow<RoleBasedNavigationState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.userSession.collect { session ->
                if (session != null) {
                    loadUserData(session.userId)
                    loadAccessibleFarms(session.userId)
                } else {
                    _uiState.value = RoleBasedNavigationState()
                }
            }
        }
    }

    fun initializeNavigation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUser = currentUser,
                        isLoading = false
                    )
                    loadAccessibleFarms(currentUser.id)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "User not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to initialize navigation",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadUserData(userId: String) {
        try {
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(currentUser = user)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load user data: ${e.message}"
            )
        }
    }

    private suspend fun loadAccessibleFarms(userId: String) {
        try {
            // This would be implemented based on your organization repository
            // For now, using placeholder logic
            val farms = emptyList<Organization>() // organizationRepository.getUserFarms(userId)
            
            _uiState.value = _uiState.value.copy(
                accessibleFarms = farms,
                selectedFarmId = farms.firstOrNull()?.id,
                selectedFarm = farms.firstOrNull()
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load farms: ${e.message}"
            )
        }
    }

    fun loadFarmAccess(farmId: String) {
        viewModelScope.launch {
            try {
                // Load farm-specific access and permissions
                val farm = _uiState.value.accessibleFarms.find { it.id == farmId }
                _uiState.value = _uiState.value.copy(
                    selectedFarm = farm,
                    selectedFarmId = farmId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load farm access: ${e.message}"
                )
            }
        }
    }

    fun switchFarm(farmId: String) {
        viewModelScope.launch {
            try {
                sessionManager.switchOrganization(farmId)
                loadFarmAccess(farmId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to switch farm: ${e.message}"
                )
            }
        }
    }

    fun refreshUserAccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val session = sessionManager.userSession.value
                if (session != null) {
                    loadUserData(session.userId)
                    loadAccessibleFarms(session.userId)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh access",
                    isLoading = false
                )
            }
        }
    }

    fun showPermissionDeniedMessage(message: String) {
        _uiState.value = _uiState.value.copy(permissionDeniedMessage = message)
    }

    fun clearPermissionDeniedMessage() {
        _uiState.value = _uiState.value.copy(permissionDeniedMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}