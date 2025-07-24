package com.rio.rostry.ui.fowls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyFowlsViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MyFowlsUiState())
    val uiState: StateFlow<MyFowlsUiState> = _uiState.asStateFlow()
    
    init {
        loadMyFowls()
    }
    
    private fun loadMyFowls() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                fowlRepository.getFowlsByOwner(currentUser.uid).collectLatest { fowls ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fowls = fowls
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
            }
        }
    }
    
    fun deleteFowl(fowlId: String) {
        viewModelScope.launch {
            fowlRepository.deleteFowl(fowlId)
                .onSuccess {
                    // Fowl deleted successfully, the flow will automatically update
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun toggleForSale(fowl: Fowl) {
        viewModelScope.launch {
            val updatedFowl = fowl.copy(
                isForSale = !fowl.isForSale,
                updatedAt = System.currentTimeMillis()
            )
            
            fowlRepository.updateFowl(updatedFowl)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MyFowlsUiState(
    val isLoading: Boolean = false,
    val fowls: List<Fowl> = emptyList(),
    val error: String? = null
)