package com.rio.rostry.ui.marketplace

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.rio.rostry.data.model.CartItem
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.MarketplaceListing
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.MarketplaceRepository
import com.rio.rostry.data.repository.UserRepository
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
    private val userRepository: UserRepository,
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
                // Get seller name from user repository
                val seller = userRepository.getUserById(fowl.ownerId)
                val sellerName = seller?.displayName ?: "Unknown Seller"
                
                val cartItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    fowlId = fowl.id,
                    fowlName = fowl.name,
                    fowlImageUrl = fowl.imageUrls.firstOrNull() ?: "",
                    sellerId = fowl.ownerId,
                    sellerName = sellerName,
                    price = fowl.price,
                    quantity = 1
                )
                cartDao.insertCartItem(cartItem)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    // Enhanced methods for MarketplaceScreenRedesigned
    fun loadMarketplaceData() {
        loadMarketplaceFowls()
        loadFeaturedData()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isNotBlank()) {
            searchFowls(query)
        } else {
            loadMarketplaceFowls()
        }
    }

    fun refreshData() {
        loadMarketplaceFowls()
        loadFeaturedData()
    }

    fun selectCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = categoryId)
        filterFowlsByCategory(categoryId)
    }

    fun toggleFilter(filterId: String) {
        val currentFilters = _uiState.value.selectedFilters.toMutableList()
        if (currentFilters.contains(filterId)) {
            currentFilters.remove(filterId)
        } else {
            currentFilters.add(filterId)
        }
        _uiState.value = _uiState.value.copy(selectedFilters = currentFilters)
        applyFilters()
    }

    fun handleFowlAction(fowlId: String, action: String) {
        when (action) {
            "toggle_favorite" -> toggleFavorite(fowlId)
            "add_to_cart" -> addToCartById(fowlId)
            "refresh" -> refreshData()
            else -> { }
        }
    }

    private fun loadFeaturedData() {
        viewModelScope.launch {
            try {
                // Load featured categories
                val categories = listOf(
                    FowlCategory("chickens", "Chickens", Icons.Default.Pets, 45),
                    FowlCategory("ducks", "Ducks", Icons.Default.Pets, 23),
                    FowlCategory("turkeys", "Turkeys", Icons.Default.Pets, 12),
                    FowlCategory("geese", "Geese", Icons.Default.Pets, 8)
                )
                
                // Load available filters
                val filters = listOf(
                    MarketplaceFilter("available", "Available", "status"),
                    MarketplaceFilter("featured", "Featured", "promotion"),
                    MarketplaceFilter("nearby", "Nearby", "location"),
                    MarketplaceFilter("verified", "Verified Sellers", "verification")
                )

                _uiState.value = _uiState.value.copy(
                    featuredCategories = categories,
                    availableFilters = filters,
                    featuredFowls = _uiState.value.fowls.take(5) // First 5 as featured
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun filterFowlsByCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                val filteredFowls = if (categoryId == "all") {
                    _uiState.value.fowls
                } else {
                    _uiState.value.fowls.filter { fowl ->
                        fowl.breed.lowercase().contains(categoryId.lowercase())
                    }
                }
                _uiState.value = _uiState.value.copy(fowls = filteredFowls)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun applyFilters() {
        viewModelScope.launch {
            try {
                var filteredFowls = _uiState.value.fowls
                val selectedFilters = _uiState.value.selectedFilters

                if (selectedFilters.contains("available")) {
                    filteredFowls = filteredFowls.filter { it.status == "Available" }
                }
                if (selectedFilters.contains("featured")) {
                    filteredFowls = filteredFowls.filter { it.isForSale }
                }
                if (selectedFilters.contains("verified")) {
                    filteredFowls = filteredFowls.filter { it.isForSale }
                }

                _uiState.value = _uiState.value.copy(fowls = filteredFowls)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun toggleFavorite(fowlId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implement favorite functionality
                // For now, just show success message
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun addToCartById(fowlId: String) {
        viewModelScope.launch {
            try {
                val fowl = _uiState.value.fowls.find { it.id == fowlId }
                if (fowl != null) {
                    addToCart(fowl)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Methods for CreateListingScreen
    fun createListing(
        fowlId: String,
        price: Double,
        purpose: String,
        description: String,
        location: String,
        hasTraceableLineage: Boolean = false,
        motherId: String? = null,
        fatherId: String? = null,
        generation: Int? = null,
        bloodlineId: String? = null,
        lineageNotes: String = "",
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
                val result = marketplaceRepository.createListingWithLineage(
                    fowlId = fowlId,
                    sellerId = currentUser.uid,
                    sellerName = currentUser.displayName ?: "Unknown",
                    price = price,
                    purpose = purpose,
                    description = description,
                    location = location,
                    hasTraceableLineage = hasTraceableLineage,
                    motherId = motherId,
                    fatherId = fatherId,
                    generation = generation,
                    bloodlineId = bloodlineId,
                    lineageNotes = lineageNotes
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
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

    /**
     * Get breeding candidates for lineage tracking
     * Returns fowls owned by the current user that are suitable for breeding
     */
    fun getBreedingCandidates(currentFowl: Fowl?): List<Fowl> {
        val currentUser = auth.currentUser ?: return emptyList()
        return _uiState.value.fowls.filter { fowl ->
            fowl.ownerId == currentUser.uid &&
            fowl.id != currentFowl?.id &&
            fowl.status.contains("Breeder Ready", ignoreCase = true)
        }
    }
}

// Enhanced UI State with all required properties
data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val fowls: List<Fowl> = emptyList(),
    val featuredFowls: List<Fowl> = emptyList(),
    val featuredCategories: List<FowlCategory> = emptyList(),
    val availableFilters: List<MarketplaceFilter> = emptyList(),
    val selectedFilters: List<String> = emptyList(),
    val selectedCategory: String = "all",
    val searchQuery: String = "",
    val listings: List<MarketplaceListing> = emptyList(),
    val cartItemCount: Int = 0,
    val error: String? = null
)