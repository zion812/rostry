package com.rio.rostry.ui.fowls

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.TransferLog
import com.rio.rostry.data.repository.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    fun initiateTransfer(
        fowlId: String,
        receiverEmail: String,
        receiverName: String,
        agreedPrice: Double,
        currentWeight: Double?,
        transferNotes: String,
        recentPhotoUri: Uri?,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // TODO: In a real app, you'd need to look up the receiver's user ID by email
                // For now, we'll use the email as a placeholder
                val receiverId = "user_${receiverEmail.hashCode()}" // Placeholder
                
                val result = transferRepository.initiateTransfer(
                    fowlId = fowlId,
                    giverId = currentUser.uid,
                    giverName = currentUser.displayName ?: "Unknown",
                    receiverId = receiverId,
                    receiverName = receiverName,
                    agreedPrice = agreedPrice,
                    currentWeight = currentWeight,
                    transferNotes = transferNotes,
                    recentPhotoUri = recentPhotoUri
                )

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to initiate transfer: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun verifyTransfer(transferId: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = transferRepository.verifyTransfer(transferId, currentUser.uid)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to verify transfer: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun rejectTransfer(transferId: String, rejectionReason: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = transferRepository.rejectTransfer(transferId, currentUser.uid, rejectionReason)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to reject transfer: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun cancelTransfer(transferId: String, onSuccess: () -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val result = transferRepository.cancelTransfer(transferId, currentUser.uid)

                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to cancel transfer: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun loadUserTransfers() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        viewModelScope.launch {
            transferRepository.getUserTransfers(currentUser.uid).collect { transfers ->
                _uiState.value = _uiState.value.copy(transfers = transfers)
            }
        }
    }

    fun loadPendingTransfers() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }

        viewModelScope.launch {
            transferRepository.getPendingTransfers(currentUser.uid).collect { transfers ->
                _uiState.value = _uiState.value.copy(pendingTransfers = transfers)
            }
        }
    }

    fun loadFowlTransferHistory(fowlId: String) {
        viewModelScope.launch {
            transferRepository.getFowlTransferHistory(fowlId).collect { transfers ->
                _uiState.value = _uiState.value.copy(fowlTransferHistory = transfers)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TransferUiState(
    val isLoading: Boolean = false,
    val transfers: List<TransferLog> = emptyList(),
    val pendingTransfers: List<TransferLog> = emptyList(),
    val fowlTransferHistory: List<TransferLog> = emptyList(),
    val error: String? = null
)