package com.rio.rostry.ui.fowls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlRecord
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.FowlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FowlDetailViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FowlDetailUiState())
    val uiState: StateFlow<FowlDetailUiState> = _uiState.asStateFlow()

    fun loadFowlDetails(fowlId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                // Combine fowl data and records
                combine(
                    fowlRepository.getFowlByIdFlow(fowlId),
                    fowlRepository.getFowlRecords(fowlId)
                ) { fowl, records ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fowl = fowl,
                        records = records,
                        error = if (fowl == null) "Fowl not found" else null,
                        isOwner = fowl?.ownerId == getCurrentUserId()
                    )
                }.collect { }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load fowl details: ${e.message}"
                )
            }
        }
    }

    fun loadFowl(fowlId: String) {
        loadFowlDetails(fowlId)
    }

    fun addRecord(record: FowlRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = fowlRepository.addFowlRecord(record)
                if (result.isSuccess) {
                    onSuccess()
                    // Refresh records
                    _uiState.value.fowl?.let { fowl ->
                        loadFowlDetails(fowl.id)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to add record: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error adding record: ${e.message}"
                )
            }
        }
    }

    fun updateRecord(record: FowlRecord, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = fowlRepository.updateFowlRecord(record)
                if (result.isSuccess) {
                    onSuccess()
                    // Refresh records
                    _uiState.value.fowl?.let { fowl ->
                        loadFowlDetails(fowl.id)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update record: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error updating record: ${e.message}"
                )
            }
        }
    }

    fun deleteRecord(recordId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = fowlRepository.deleteFowlRecord(recordId)
                if (result.isSuccess) {
                    onSuccess()
                    // Refresh records
                    _uiState.value.fowl?.let { fowl ->
                        loadFowlDetails(fowl.id)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to delete record: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error deleting record: ${e.message}"
                )
            }
        }
    }

    fun addToCart(fowlId: String) {
        viewModelScope.launch {
            try {
                // Add to cart logic here
                _uiState.value = _uiState.value.copy(addedToCart = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add to cart: ${e.message}"
                )
            }
        }
    }

    fun clearAddedToCart() {
        _uiState.value = _uiState.value.copy(addedToCart = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun getCurrentUserId(): String? {
        return authRepository.currentUser?.uid
    }
}

data class FowlDetailUiState(
    val isLoading: Boolean = false,
    val fowl: Fowl? = null,
    val records: List<FowlRecord> = emptyList(),
    val error: String? = null,
    val addedToCart: Boolean = false,
    val isOwner: Boolean = false
)