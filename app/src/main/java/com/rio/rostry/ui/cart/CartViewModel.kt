package com.rio.rostry.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.CartItem
import com.rio.rostry.data.model.Order
import com.rio.rostry.data.model.OrderStatus
import com.rio.rostry.data.local.dao.CartDao
import com.rio.rostry.data.repository.AuthRepository
import com.rio.rostry.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
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
    
    fun checkout(onSuccess: (String) -> Unit) {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                checkoutMessage = "Please log in to complete checkout"
            )
            return
        }
        
        val cartItems = _uiState.value.cartItems
        if (cartItems.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                checkoutMessage = "Your cart is empty"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessingCheckout = true)
            
            try {
                // Create order from cart items (using first cart item for now)
                val firstItem = cartItems.first()
                val order = Order(
                    orderId = UUID.randomUUID().toString(),
                    buyerId = currentUser.uid,
                    sellerId = firstItem.sellerId,
                    fowlId = firstItem.fowlId,
                    fowlName = firstItem.fowlName,
                    fowlBreed = firstItem.fowlBreed ?: "",
                    fowlImageUrl = firstItem.fowlImageUrl ?: "",
                    quantity = cartItems.sumOf { it.quantity },
                    basePrice = firstItem.price,
                    productTotal = _uiState.value.total,
                    grandTotal = _uiState.value.total,
                    status = OrderStatus.PENDING,
                    deliveryAddress = "", // TODO: Add address selection
                    createdAt = System.currentTimeMillis()
                )
                
                // Save order
                orderRepository.createOrder(order)
                    .onSuccess { orderId ->
                        // Clear cart after successful order
                        clearCart()
                        _uiState.value = _uiState.value.copy(
                            isProcessingCheckout = false,
                            checkoutMessage = "Order placed successfully! Order ID: $orderId"
                        )
                        onSuccess(orderId)
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isProcessingCheckout = false,
                            checkoutMessage = "Checkout failed: ${error.message}"
                        )
                    }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessingCheckout = false,
                    checkoutMessage = "Checkout failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearCheckoutMessage() {
        _uiState.value = _uiState.value.copy(checkoutMessage = null)
    }
}

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val isProcessingCheckout: Boolean = false,
    val checkoutMessage: String? = null
)