package com.rio.rostry.ui.verification

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.VerificationRepository
import com.rio.rostry.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        loadVerificationRequests()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                val coinBalance = user?.let { walletRepository.getCoinBalance(it.id) } ?: 0
                val coinPricing = walletRepository.getCoinPricing()
                
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    coinBalance = coinBalance,
                    coinPricing = coinPricing
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user data: ${e.message}"
                )
            }
        }
    }
    
    private fun loadVerificationRequests() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                verificationRepository.getUserVerificationRequests(user.id).collect { requests ->
                    _uiState.value = _uiState.value.copy(
                        verificationRequests = requests,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun selectVerificationType(type: VerificationType) {
        _uiState.value = _uiState.value.copy(
            selectedVerificationType = type,
            showRequestDialog = true
        )
    }
    
    fun addDocument(uri: Uri) {
        val currentDocuments = _uiState.value.selectedDocuments.toMutableList()
        if (currentDocuments.size < 5) { // Max 5 documents
            currentDocuments.add(uri)
            _uiState.value = _uiState.value.copy(selectedDocuments = currentDocuments)
        }
    }
    
    fun removeDocument(uri: Uri) {
        val currentDocuments = _uiState.value.selectedDocuments.toMutableList()
        currentDocuments.remove(uri)
        _uiState.value = _uiState.value.copy(selectedDocuments = currentDocuments)
    }
    
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(verificationNotes = notes)
    }
    
    fun submitVerificationRequest() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val user = currentState.currentUser
            
            if (user == null) {
                _uiState.value = currentState.copy(error = "User not found")
                return@launch
            }
            
            if (currentState.selectedDocuments.isEmpty()) {
                _uiState.value = currentState.copy(error = "Please select at least one document")
                return@launch
            }
            
            val requiredCoins = currentState.coinPricing.verificationFee
            if (currentState.coinBalance < requiredCoins) {
                _uiState.value = currentState.copy(
                    error = "Insufficient coins. Required: $requiredCoins, Available: ${currentState.coinBalance}",
                    showInsufficientCoinsDialog = true
                )
                return@launch
            }
            
            _uiState.value = currentState.copy(isSubmitting = true, error = null)
            
            try {
                val result = verificationRepository.submitVerificationRequest(
                    userId = user.id,
                    userName = user.displayName,
                    userEmail = user.email,
                    verificationType = currentState.selectedVerificationType,
                    documents = currentState.selectedDocuments,
                    notes = currentState.verificationNotes
                )
                
                if (result.isSuccess) {
                    _uiState.value = currentState.copy(
                        isSubmitting = false,
                        showRequestDialog = false,
                        selectedDocuments = emptyList(),
                        verificationNotes = "",
                        successMessage = "Verification request submitted successfully!"
                    )
                    
                    // Refresh data
                    loadUserData()
                    loadVerificationRequests()
                } else {
                    _uiState.value = currentState.copy(
                        isSubmitting = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to submit verification request"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to submit verification request"
                )
            }
        }
    }
    
    fun canRequestVerification(type: VerificationType): Boolean {
        val requests = _uiState.value.verificationRequests
        return !requests.any { 
            it.verificationType == type && 
            (it.status == VerificationStatus.PENDING || it.status == VerificationStatus.VERIFIED)
        }
    }
    
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showRequestDialog = false,
            showInsufficientCoinsDialog = false,
            selectedDocuments = emptyList(),
            verificationNotes = ""
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class VerificationUiState(
    val currentUser: User? = null,
    val coinBalance: Int = 0,
    val coinPricing: CoinPricing = CoinPricing(),
    val verificationRequests: List<VerificationRequest> = emptyList(),
    val selectedVerificationType: VerificationType = VerificationType.USER,
    val selectedDocuments: List<Uri> = emptyList(),
    val verificationNotes: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val showRequestDialog: Boolean = false,
    val showInsufficientCoinsDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)