package com.rio.rostry.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.CartItem
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.MarketplaceListing
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.MarketplaceRepository
import com.rio.rostry.data.local.dao.CartDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val marketplaceRepository: MarketplaceRepository,
    private val cartDao: CartDao,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState.asStateFlow()
    
    init {
        observeCartItemCount()
    }
    
    private fun observeCartItemCount() {
        viewModelScope.launch {
            cartDao.getCartItemCount().collectLatest { count ->
                _uiState.value = _uiState.value.copy(cartItemCount = count)
            }
        }
    }
    
    fun loadMarketplaceFowls() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                fowlRepository.getMarketplaceFowls().collectLatest { fowls ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fowls = fowls
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun searchFowls(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                fowlRepository.searchFowls(query).collectLatest { fowls ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        fowls = fowls
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun addToCart(fowl: Fowl) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    fowlId = fowl.id,
                    fowlName = fowl.name,
                    fowlImageUrl = fowl.imageUrls.firstOrNull() ?: "",
                    sellerId = fowl.ownerId,
                    sellerName = "", // TODO: Get seller name
                    price = fowl.price,
                    quantity = 1
                )
                cartDao.insertCartItem(cartItem)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // Enhanced Marketplace Features
    fun loadMarketplaceListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                marketplaceRepository.getActiveListings().collectLatest { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun searchListings(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                if (query.isBlank()) {
                    loadMarketplaceListings()
                } else {
                    marketplaceRepository.searchListings(query).collectLatest { listings ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            listings = listings
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun applyFilters(
        purpose: String? = null,
        isBreederReady: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        motherId: String? = null,
        fatherId: String? = null,
        fowlType: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                marketplaceRepository.getFilteredListings(
                    purpose = purpose,
                    isBreederReady = isBreederReady,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    motherId = motherId,
                    fatherId = fatherId,
                    fowlType = fowlType,
                    location = location
                ).collectLatest { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun createListing(
        fowlId: String,
        price: Double,
        purpose: String,
        description: String,
        location: String,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(error = "User not authenticated")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = marketplaceRepository.createListing(
                    fowlId = fowlId,
                    sellerId = currentUser.uid,
                    sellerName = currentUser.displayName ?: "Unknown",
                    price = price,
                    purpose = purpose,
                    description = description,
                    location = location
                )
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun getUserOwnedFowls(): List<Fowl> {
        val currentUser = auth.currentUser ?: return emptyList()
        return _uiState.value.fowls.filter { it.ownerId == currentUser.uid && !it.isForSale }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val fowls: List<Fowl> = emptyList(),
    val listings: List<MarketplaceListing> = emptyList(),
    val cartItemCount: Int = 0,
    val error: String? = null
)