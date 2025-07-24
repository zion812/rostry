package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Order
import com.rio.rostry.data.model.OrderStatus
import com.rio.rostry.data.model.PaymentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    
    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): Order?
    
    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getBuyerOrders(buyerId: String): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getSellerOrders(sellerId: String): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE paymentStatus = :paymentStatus ORDER BY createdAt DESC")
    fun getOrdersByPaymentStatus(paymentStatus: PaymentStatus): Flow<List<Order>>
    
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)
    
    @Update
    suspend fun updateOrder(order: Order)
    
    @Delete
    suspend fun deleteOrder(order: Order)
    
    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrderById(orderId: String)
    
    @Query("SELECT * FROM orders WHERE buyerId = :userId OR sellerId = :userId ORDER BY createdAt DESC")
    fun getUserOrders(userId: String): Flow<List<Order>>
    
    @Query("SELECT COUNT(*) FROM orders WHERE sellerId = :sellerId AND status = 'DELIVERED'")
    suspend fun getSellerCompletedOrdersCount(sellerId: String): Int
    
    @Query("SELECT AVG(CASE WHEN status = 'DELIVERED' THEN 5.0 ELSE 0.0 END) FROM orders WHERE sellerId = :sellerId")
    suspend fun getSellerAverageRating(sellerId: String): Double
    
    @Query("SELECT SUM(grandTotal) FROM orders WHERE sellerId = :sellerId AND status = 'DELIVERED'")
    suspend fun getSellerTotalRevenue(sellerId: String): Double?
}