package com.rio.rostry.ui.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShowcaseUiState())
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()
    
    init {
        loadShowcaseData()
    }
    
    private fun loadShowcaseData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    // Load user's fowls for showcase
                    fowlRepository.getFowlsByOwner(user.id).collect { fowls ->
                        _uiState.value = _uiState.value.copy(
                            userFowls = fowls,
                            isLoading = false
                        )
                    }
                    
                    // Load coin balance
                    val coinBalance = walletRepository.getCoinBalance(user.id)
                    _uiState.value = _uiState.value.copy(coinBalance = coinBalance)
                }
                
                // Load showcase fowls by category
                loadShowcaseFowls()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load showcase data",
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadShowcaseFowls() {
        viewModelScope.launch {
            try {
                // Load marketplace fowls and categorize them for showcase
                fowlRepository.getMarketplaceFowls().collect { fowls ->
                    val showcaseFowls = categorizeShowcaseFowls(fowls)
                    _uiState.value = _uiState.value.copy(showcaseFowls = showcaseFowls)
                }
            } catch (e: Exception) {
                // Handle error silently for showcase loading
            }
        }
    }
    
    private fun categorizeShowcaseFowls(fowls: List<Fowl>): Map<ShowcaseCategory, List<Fowl>> {
        return fowls.groupBy { fowl ->
            when {
                fowl.type.name.contains("LAYER", ignoreCase = true) -> ShowcaseCategory.LAYERS
                fowl.type.name.contains("BROILER", ignoreCase = true) -> ShowcaseCategory.BROILERS
                fowl.status.contains("Breeder Ready", ignoreCase = true) -> ShowcaseCategory.BREEDING
                fowl.breed.contains("rare", ignoreCase = true) -> ShowcaseCategory.RARE_BREEDS
                fowl.status.contains("Champion", ignoreCase = true) -> ShowcaseCategory.CHAMPIONS
                else -> ShowcaseCategory.CHICKS
            }
        }
    }
    
    fun selectCategory(category: ShowcaseCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun selectFowlForShowcase(fowl: Fowl) {
        _uiState.value = _uiState.value.copy(
            selectedFowl = fowl,
            showShowcaseDialog = true
        )
    }
    
    fun selectShowcaseDuration(duration: ShowcaseDuration) {
        _uiState.value = _uiState.value.copy(selectedDuration = duration)
    }
    
    fun purchaseShowcaseSlot() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val user = authRepository.getCurrentUser()
            val fowl = currentState.selectedFowl
            val duration = currentState.selectedDuration
            
            if (user == null || fowl == null) {
                _uiState.value = currentState.copy(error = "Invalid showcase request")
                return@launch
            }
            
            val coinCost = duration.coinCost
            if (currentState.coinBalance < coinCost) {
                _uiState.value = currentState.copy(
                    error = "Insufficient coins. Required: $coinCost, Available: ${currentState.coinBalance}",
                    showInsufficientCoinsDialog = true
                )
                return@launch
            }
            
            _uiState.value = currentState.copy(isPurchasing = true, error = null)
            
            try {
                // Deduct coins for showcase
                val deductResult = walletRepository.deductCoins(
                    userId = user.id,
                    amount = coinCost,
                    description = "Showcase placement - ${fowl.name}",
                    relatedEntityId = fowl.id,
                    relatedEntityType = "showcase"
                )
                
                if (deductResult.isSuccess) {
                    // Create showcase slot (in a real implementation, this would be stored in Firestore)
                    val showcaseSlot = ShowcaseSlot(
                        slotId = UUID.randomUUID().toString(),
                        category = currentState.selectedCategory,
                        fowlId = fowl.id,
                        userId = user.id,
                        duration = duration,
                        coinsSpent = coinCost,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (duration.days * 24 * 60 * 60 * 1000L)
                    )
                    
                    _uiState.value = currentState.copy(
                        isPurchasing = false,
                        showShowcaseDialog = false,
                        selectedFowl = null,
                        successMessage = "Fowl added to showcase successfully!",
                        coinBalance = currentState.coinBalance - coinCost
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isPurchasing = false,
                        error = deductResult.exceptionOrNull()?.message ?: "Failed to purchase showcase slot"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isPurchasing = false,
                    error = e.message ?: "Failed to purchase showcase slot"
                )
            }
        }
    }
    
    fun dismissShowcaseDialog() {
        _uiState.value = _uiState.value.copy(
            showShowcaseDialog = false,
            showInsufficientCoinsDialog = false,
            selectedFowl = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun refreshShowcase() {
        loadShowcaseData()
    }
}

data class ShowcaseUiState(
    val userFowls: List<Fowl> = emptyList(),
    val showcaseFowls: Map<ShowcaseCategory, List<Fowl>> = emptyMap(),
    val selectedCategory: ShowcaseCategory = ShowcaseCategory.BREEDING,
    val selectedFowl: Fowl? = null,
    val selectedDuration: ShowcaseDuration = ShowcaseDuration.WEEK,
    val coinBalance: Int = 0,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val showShowcaseDialog: Boolean = false,
    val showInsufficientCoinsDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)