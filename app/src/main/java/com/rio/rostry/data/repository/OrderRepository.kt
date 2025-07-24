package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rio.rostry.data.local.dao.OrderDao
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val orderDao: OrderDao
) {
    
    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val orderId = order.orderId.ifEmpty { UUID.randomUUID().toString() }
            val orderWithId = order.copy(orderId = orderId)
            
            firestore.collection("orders").document(orderId).set(orderWithId).await()
            orderDao.insertOrder(orderWithId)
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateOrder(order: Order): Result<Unit> {
        return try {
            firestore.collection("orders").document(order.orderId).set(order).await()
            orderDao.updateOrder(order)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrderById(orderId: String): Order? {
        return try {
            val snapshot = firestore.collection("orders").document(orderId).get().await()
            snapshot.toObject(Order::class.java)
        } catch (e: Exception) {
            orderDao.getOrderById(orderId)
        }
    }
    
    fun getBuyerOrders(buyerId: String): Flow<List<Order>> = flow {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            orders.forEach { orderDao.insertOrder(it) }
            emit(orders)
        } catch (e: Exception) {
            orderDao.getBuyerOrders(buyerId).collect { emit(it) }
        }
    }
    
    fun getSellerOrders(sellerId: String): Flow<List<Order>> = flow {
        try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            orders.forEach { orderDao.insertOrder(it) }
            emit(orders)
        } catch (e: Exception) {
            orderDao.getSellerOrders(sellerId).collect { emit(it) }
        }
    }
    
    fun getUserOrders(userId: String): Flow<List<Order>> = flow {
        try {
            val buyerSnapshot = firestore.collection("orders")
                .whereEqualTo("buyerId", userId)
                .get()
                .await()
            
            val sellerSnapshot = firestore.collection("orders")
                .whereEqualTo("sellerId", userId)
                .get()
                .await()
            
            val allOrders = (buyerSnapshot.documents + sellerSnapshot.documents)
                .mapNotNull { it.toObject(Order::class.java) }
                .sortedByDescending { it.createdAt }
            
            allOrders.forEach { orderDao.insertOrder(it) }
            emit(allOrders)
        } catch (e: Exception) {
            orderDao.getUserOrders(userId).collect { emit(it) }
        }
    }
    
    suspend fun calculateOrderSummary(
        basePrice: Double,
        quantity: Int,
        deliveryLocation: Location?
    ): OrderSummary {
        val productTotal = basePrice * quantity
        val platformFee = productTotal * 0.05 // 5% platform fee
        val handlingCharge = 2.50
        val packagingCharge = 1.50
        val processingCharge = 3.00
        val deliveryCharge = calculateDeliveryCharge(deliveryLocation)
        
        val grandTotal = productTotal + platformFee + handlingCharge + 
                        packagingCharge + processingCharge + deliveryCharge
        
        return OrderSummary(
            productTotal = productTotal,
            platformFee = platformFee,
            handlingCharge = handlingCharge,
            packagingCharge = packagingCharge,
            processingCharge = processingCharge,
            deliveryCharge = deliveryCharge,
            grandTotal = grandTotal
        )
    }
    
    private suspend fun calculateDeliveryCharge(location: Location?): Double {
        // In a real implementation, you would use Google Maps Distance Matrix API
        // For now, we'll use a simple distance-based calculation
        return when {
            location == null -> 0.0
            location.city.isEmpty() -> 10.0 // Default local delivery
            else -> {
                // Simplified calculation - in reality, use actual distance
                when (location.state.lowercase()) {
                    "california", "ca" -> 8.50
                    "texas", "tx" -> 12.00
                    "florida", "fl" -> 15.00
                    else -> 20.00 // Out of state
                }
            }
        }
    }
    
    suspend fun createPaymentIntent(order: Order): Result<PaymentIntent> {
        return try {
            // In a real implementation, this would call your backend API
            // which would create a Stripe payment intent
            val paymentIntent = PaymentIntent(
                id = "pi_${UUID.randomUUID()}",
                amount = order.grandTotal,
                currency = "USD",
                status = "requires_payment_method",
                clientSecret = "pi_${UUID.randomUUID()}_secret_${UUID.randomUUID()}",
                orderId = order.orderId
            )
            
            // Store payment intent in Firestore
            firestore.collection("payment_intents")
                .document(paymentIntent.id)
                .set(paymentIntent)
                .await()
            
            Result.success(paymentIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun confirmPayment(paymentIntentId: String, orderId: String): Result<Unit> {
        return try {
            // Update order payment status
            val order = getOrderById(orderId)
            if (order != null) {
                val updatedOrder = order.copy(
                    paymentStatus = PaymentStatus.COMPLETED,
                    status = OrderStatus.CONFIRMED,
                    paymentIntentId = paymentIntentId,
                    updatedAt = System.currentTimeMillis()
                )
                updateOrder(updatedOrder)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSellerStats(sellerId: String): SellerStats {
        return try {
            val completedOrders = orderDao.getSellerCompletedOrdersCount(sellerId)
            val averageRating = orderDao.getSellerAverageRating(sellerId)
            val totalRevenue = orderDao.getSellerTotalRevenue(sellerId) ?: 0.0
            
            SellerStats(
                totalSales = completedOrders,
                averageRating = averageRating,
                totalRevenue = totalRevenue
            )
        } catch (e: Exception) {
            SellerStats()
        }
    }
}

data class SellerStats(
    val totalSales: Int = 0,
    val averageRating: Double = 0.0,
    val totalRevenue: Double = 0.0
)