package com.rio.rostry.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rio.rostry.data.model.*
import com.rio.rostry.data.repository.FowlRepository
import com.rio.rostry.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val fowlRepository: FowlRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()
    
    fun loadFowlDetails(fowlId: String, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val fowl = fowlRepository.getFowlById(fowlId)
                if (fowl != null) {
                    val orderSummary = orderRepository.calculateOrderSummary(
                        basePrice = fowl.price,
                        quantity = quantity,
                        deliveryLocation = _uiState.value.deliveryLocation
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        fowl = fowl,
                        quantity = quantity,
                        orderSummary = orderSummary,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Fowl not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load fowl details",
                    isLoading = false
                )
            }
        }
    }
    
    fun updateDeliveryAddress(address: String, location: Location?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                deliveryAddress = address,
                deliveryLocation = location
            )
            
            // Recalculate order summary with new delivery location
            val fowl = _uiState.value.fowl
            if (fowl != null) {
                val orderSummary = orderRepository.calculateOrderSummary(
                    basePrice = fowl.price,
                    quantity = _uiState.value.quantity,
                    deliveryLocation = location
                )
                
                _uiState.value = _uiState.value.copy(orderSummary = orderSummary)
            }
        }
    }
    
    fun selectPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }
    
    fun placeOrder() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val fowl = currentState.fowl
            val orderSummary = currentState.orderSummary
            
            if (fowl == null || orderSummary == null) {
                _uiState.value = currentState.copy(error = "Missing order information")
                return@launch
            }
            
            if (currentState.deliveryAddress.isBlank()) {
                _uiState.value = currentState.copy(error = "Please add a delivery address")
                return@launch
            }
            
            if (currentState.selectedPaymentMethod.isBlank()) {
                _uiState.value = currentState.copy(error = "Please select a payment method")
                return@launch
            }
            
            _uiState.value = currentState.copy(isProcessingPayment = true, error = null)
            
            try {
                val order = Order(
                    orderId = UUID.randomUUID().toString(),
                    buyerId = "current_user_id", // TODO: Get from auth
                    sellerId = fowl.ownerId,
                    fowlId = fowl.id,
                    fowlName = fowl.name,
                    fowlBreed = fowl.breed,
                    fowlImageUrl = fowl.imageUrls.firstOrNull() ?: "",
                    quantity = currentState.quantity,
                    basePrice = fowl.price,
                    productTotal = orderSummary.productTotal,
                    platformFee = orderSummary.platformFee,
                    handlingCharge = orderSummary.handlingCharge,
                    packagingCharge = orderSummary.packagingCharge,
                    processingCharge = orderSummary.processingCharge,
                    deliveryCharge = orderSummary.deliveryCharge,
                    grandTotal = orderSummary.grandTotal,
                    deliveryAddress = currentState.deliveryAddress,
                    deliveryLocation = currentState.deliveryLocation,
                    notes = ""
                )
                
                val result = orderRepository.createOrder(order)
                
                if (result.isSuccess) {
                    val orderId = result.getOrNull()
                    
                    // Create payment intent
                    val paymentResult = processMockPayment(order, currentState.selectedPaymentMethod)
                    
                    if (paymentResult.isSuccess) {
                        _uiState.value = currentState.copy(
                            orderPlaced = true,
                            orderId = orderId,
                            isProcessingPayment = false
                        )
                    } else {
                        _uiState.value = currentState.copy(
                            error = "Failed to create payment: ${paymentResult.exceptionOrNull()?.message}",
                            isProcessingPayment = false
                        )
                    }
                } else {
                    _uiState.value = currentState.copy(
                        error = "Failed to create order: ${result.exceptionOrNull()?.message}",
                        isProcessingPayment = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    error = e.message ?: "Failed to place order",
                    isProcessingPayment = false
                )
            }
        }
    }
    
    private suspend fun processMockPayment(order: Order, paymentMethod: String): Result<String> {
        return try {
            // Simulate payment processing time
            kotlinx.coroutines.delay(3000)
            
            // Simulate different success rates based on payment method
            val successRate = when (paymentMethod.lowercase()) {
                "credit/debit card" -> 92
                "paypal" -> 88
                "google pay" -> 95
                "cash on delivery" -> 98
                else -> 85
            }
            
            val isSuccess = (1..100).random() <= successRate
            
            if (isSuccess) {
                val transactionId = "txn_mock_${System.currentTimeMillis()}"
                
                // Update order status
                val updatedOrder = order.copy(
                    paymentStatus = PaymentStatus.COMPLETED,
                    status = OrderStatus.CONFIRMED,
                    paymentIntentId = transactionId,
                    updatedAt = System.currentTimeMillis()
                )
                
                orderRepository.updateOrder(updatedOrder)
                Result.success(transactionId)
            } else {
                val errorMessages = listOf(
                    "Card declined - Insufficient funds",
                    "Payment method not supported",
                    "Network error - Please try again",
                    "Card expired",
                    "Invalid payment details"
                )
                Result.failure(Exception(errorMessages.random()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CheckoutUiState(
    val fowl: Fowl? = null,
    val quantity: Int = 1,
    val orderSummary: OrderSummary? = null,
    val deliveryAddress: String = "",
    val deliveryLocation: Location? = null,
    val selectedPaymentMethod: String = "",
    val isLoading: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val orderPlaced: Boolean = false,
    val orderId: String? = null,
    val error: String? = null
)