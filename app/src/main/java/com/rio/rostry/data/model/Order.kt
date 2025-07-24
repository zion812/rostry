package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val fowlId: String = "",
    val fowlName: String = "",
    val fowlBreed: String = "",
    val fowlImageUrl: String = "",
    val quantity: Int = 1,
    val basePrice: Double = 0.0,
    val productTotal: Double = 0.0,
    val platformFee: Double = 0.0,
    val handlingCharge: Double = 0.0,
    val packagingCharge: Double = 0.0,
    val processingCharge: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentIntentId: String? = null,
    val deliveryAddress: String = "",
    val deliveryLocation: Location? = null,
    val estimatedDeliveryDate: Long? = null,
    val trackingNumber: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED
}

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = ""
)

data class OrderSummary(
    val productTotal: Double,
    val platformFee: Double,
    val handlingCharge: Double,
    val packagingCharge: Double,
    val processingCharge: Double,
    val deliveryCharge: Double,
    val grandTotal: Double,
    val feeBreakdown: Map<String, Double> = mapOf(
        "Platform Fee" to platformFee,
        "Handling Charge" to handlingCharge,
        "Packaging Charge" to packagingCharge,
        "Processing Charge" to processingCharge,
        "Delivery Charge" to deliveryCharge
    )
)

data class PaymentIntent(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val status: String = "",
    val clientSecret: String = "",
    val orderId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)