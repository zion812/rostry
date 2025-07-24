package com.rio.rostry.data.repository

import com.rio.rostry.data.model.*
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockPaymentRepository @Inject constructor() {
    
    /**
     * Mock Google Play Billing - Simulates coin purchases without real SDK
     */
    suspend fun simulateGooglePlayPurchase(
        coinPackage: CoinPackage,
        userId: String
    ): Result<MockPurchaseResult> {
        return try {
            // Simulate network delay
            delay(2000)
            
            // Simulate 95% success rate (5% failure for testing)
            val isSuccess = (1..100).random() <= 95
            
            if (isSuccess) {
                val purchaseToken = "mock_token_${System.currentTimeMillis()}_${UUID.randomUUID()}"
                val orderId = "mock_order_${System.currentTimeMillis()}"
                
                Result.success(
                    MockPurchaseResult(
                        isSuccess = true,
                        purchaseToken = purchaseToken,
                        orderId = orderId,
                        productId = coinPackage.googlePlayProductId,
                        purchaseTime = System.currentTimeMillis(),
                        developerPayload = "user_$userId"
                    )
                )
            } else {
                Result.failure(MockPaymentException("Simulated payment failure - User cancelled or payment failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mock Stripe Payment - Simulates order payments without real SDK
     */
    suspend fun simulateStripePayment(
        order: Order,
        paymentMethod: String
    ): Result<MockPaymentResult> {
        return try {
            // Simulate network delay
            delay(3000)
            
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
                val paymentIntentId = "pi_mock_${System.currentTimeMillis()}_${UUID.randomUUID()}"
                val chargeId = "ch_mock_${System.currentTimeMillis()}"
                
                Result.success(
                    MockPaymentResult(
                        isSuccess = true,
                        paymentIntentId = paymentIntentId,
                        chargeId = chargeId,
                        amount = order.grandTotal,
                        currency = "USD",
                        paymentMethod = paymentMethod,
                        receiptUrl = "https://mock-receipts.rostry.com/receipt/$chargeId",
                        transactionId = chargeId
                    )
                )
            } else {
                val errorMessages = listOf(
                    "Card declined - Insufficient funds",
                    "Payment method not supported",
                    "Network error - Please try again",
                    "Card expired",
                    "Invalid payment details"
                )
                Result.failure(MockPaymentException(errorMessages.random()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Simulate payment method validation
     */
    suspend fun validatePaymentMethod(paymentMethod: String): Result<Boolean> {
        return try {
            delay(1000)
            
            val validMethods = listOf(
                "Credit/Debit Card",
                "PayPal", 
                "Google Pay",
                "Cash on Delivery"
            )
            
            val isValid = validMethods.contains(paymentMethod)
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available coin packages (mock data)
     */
    fun getMockCoinPackages(): List<CoinPackage> {
        return listOf(
            CoinPackage(
                id = "starter_pack",
                name = "Starter Pack",
                coinAmount = 100,
                price = 4.99,
                bonusCoins = 10,
                description = "Perfect for getting started",
                googlePlayProductId = "mock_coins_starter_pack"
            ),
            CoinPackage(
                id = "value_pack",
                name = "Value Pack",
                coinAmount = 250,
                price = 9.99,
                bonusCoins = 50,
                isPopular = true,
                description = "Most popular choice",
                googlePlayProductId = "mock_coins_value_pack"
            ),
            CoinPackage(
                id = "premium_pack",
                name = "Premium Pack",
                coinAmount = 500,
                price = 19.99,
                bonusCoins = 150,
                description = "Best value for power users",
                googlePlayProductId = "mock_coins_premium_pack"
            ),
            CoinPackage(
                id = "mega_pack",
                name = "Mega Pack",
                coinAmount = 1000,
                price = 34.99,
                bonusCoins = 400,
                description = "Ultimate coin package",
                googlePlayProductId = "mock_coins_mega_pack"
            )
        )
    }
    
    /**
     * Simulate refund process
     */
    suspend fun simulateRefund(
        transactionId: String,
        amount: Double,
        reason: String
    ): Result<MockRefundResult> {
        return try {
            delay(2000)
            
            // Simulate 90% refund success rate
            val isSuccess = (1..100).random() <= 90
            
            if (isSuccess) {
                Result.success(
                    MockRefundResult(
                        isSuccess = true,
                        refundId = "re_mock_${System.currentTimeMillis()}",
                        originalTransactionId = transactionId,
                        refundAmount = amount,
                        reason = reason,
                        processedAt = System.currentTimeMillis()
                    )
                )
            } else {
                Result.failure(MockPaymentException("Refund failed - Transaction not eligible for refund"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Mock data classes for simulated payments
 */
data class MockPurchaseResult(
    val isSuccess: Boolean,
    val purchaseToken: String,
    val orderId: String,
    val productId: String,
    val purchaseTime: Long,
    val developerPayload: String,
    val signature: String = "mock_signature_${UUID.randomUUID()}"
)

data class MockPaymentResult(
    val isSuccess: Boolean,
    val paymentIntentId: String,
    val chargeId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val receiptUrl: String,
    val transactionId: String,
    val processedAt: Long = System.currentTimeMillis()
)

data class MockRefundResult(
    val isSuccess: Boolean,
    val refundId: String,
    val originalTransactionId: String,
    val refundAmount: Double,
    val reason: String,
    val processedAt: Long
)

class MockPaymentException(message: String) : Exception(message)