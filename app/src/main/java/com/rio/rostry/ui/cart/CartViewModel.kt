package com.rio.rostry.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.CartItem
import com.rio.rostry.data.local.dao.CartDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    init {
        loadCartItems()
    }
    
    private fun loadCartItems() {
        viewModelScope.launch {
            cartDao.getAllCartItems().collectLatest { items ->
                val total = items.sumOf { it.price * it.quantity }
                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    total = total
                )
            }
        }
    }
    
    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeItem(cartItem)
            return
        }
        
        viewModelScope.launch {
            val updatedItem = cartItem.copy(quantity = newQuantity)
            cartDao.updateCartItem(updatedItem)
        }
    }
    
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            cartDao.deleteCartItem(cartItem)
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }
    
    fun checkout() {
        // TODO: Implement checkout functionality
        _uiState.value = _uiState.value.copy(
            checkoutMessage = "Checkout functionality coming soon!"
        )
    }
    
    fun clearCheckoutMessage() {
        _uiState.value = _uiState.value.copy(checkoutMessage = null)
    }
}

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val checkoutMessage: String? = null
)