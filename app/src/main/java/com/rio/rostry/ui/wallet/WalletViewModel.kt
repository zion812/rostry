package com.rio.rostry.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    
    init {
        loadWalletData()
    }
    
    private fun loadWalletData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    // Load wallet
                    walletRepository.getWalletFlow(user.id).collect { wallet ->
                        _uiState.value = _uiState.value.copy(
                            wallet = wallet,
                            isLoading = false
                        )
                    }
                    
                    // Load transactions
                    walletRepository.getUserTransactions(user.id).collect { transactions ->
                        _uiState.value = _uiState.value.copy(
                            transactions = transactions
                        )
                    }
                    
                    // Load coin packages
                    val coinPackages = walletRepository.getCoinPackages()
                    _uiState.value = _uiState.value.copy(
                        coinPackages = coinPackages
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "User not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load wallet data",
                    isLoading = false
                )
            }
        }
    }
    
    fun selectCoinPackage(coinPackage: CoinPackage) {
        _uiState.value = _uiState.value.copy(
            selectedCoinPackage = coinPackage,
            showPurchaseDialog = true
        )
    }
    
    fun purchaseCoinPackage() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val user = authRepository.getCurrentUser()
            val coinPackage = currentState.selectedCoinPackage
            
            if (user == null || coinPackage == null) {
                _uiState.value = currentState.copy(error = "Invalid purchase request")
                return@launch
            }
            
            _uiState.value = currentState.copy(isPurchasing = true, error = null)
            
            try {
                // In a real implementation, this would integrate with Google Play Billing
                // For now, we'll simulate a successful purchase
                val result = walletRepository.purchaseCoins(
                    userId = user.id,
                    coinPackage = coinPackage,
                    purchaseToken = "simulated_token_${System.currentTimeMillis()}"
                )
                
                if (result.isSuccess) {
                    _uiState.value = currentState.copy(
                        isPurchasing = false,
                        showPurchaseDialog = false,
                        selectedCoinPackage = null,
                        successMessage = "Successfully purchased ${coinPackage.totalCoins} coins!"
                    )
                    
                    // Refresh wallet data
                    loadWalletData()
                } else {
                    _uiState.value = currentState.copy(
                        isPurchasing = false,
                        error = result.exceptionOrNull()?.message ?: "Purchase failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isPurchasing = false,
                    error = e.message ?: "Purchase failed"
                )
            }
        }
    }
    
    fun dismissPurchaseDialog() {
        _uiState.value = _uiState.value.copy(
            showPurchaseDialog = false,
            selectedCoinPackage = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun refreshWallet() {
        loadWalletData()
    }
}

data class WalletUiState(
    val wallet: Wallet? = null,
    val transactions: List<CoinTransaction> = emptyList(),
    val coinPackages: List<CoinPackage> = emptyList(),
    val selectedCoinPackage: CoinPackage? = null,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val showPurchaseDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)