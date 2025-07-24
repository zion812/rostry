package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey
    val userId: String = "",
    val coinBalance: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalCoinsSpent: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "coin_transactions")
data class CoinTransaction(
    @PrimaryKey
    val transactionId: String = "",
    val userId: String = "",
    val type: CoinTransactionType = CoinTransactionType.DEBIT,
    val amount: Int = 0,
    val description: String = "",
    val relatedEntityId: String? = null, // fowlId, listingId, etc.
    val relatedEntityType: String? = null, // "fowl", "listing", "verification"
    val balanceBefore: Int = 0,
    val balanceAfter: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CoinTransactionType {
    CREDIT, // Adding coins (purchase, reward)
    DEBIT   // Spending coins (listing, verification)
}

data class CoinPackage(
    val id: String = "",
    val name: String = "",
    val coinAmount: Int = 0,
    val price: Double = 0.0,
    val bonusCoins: Int = 0,
    val totalCoins: Int = coinAmount + bonusCoins,
    val isPopular: Boolean = false,
    val description: String = "",
    val googlePlayProductId: String = ""
)

data class CoinPricing(
    val listingFee: Int = 10,           // Cost to create a marketplace listing
    val featuredListingFee: Int = 25,   // Cost for featured listing
    val verificationFee: Int = 50,      // Cost for verification request
    val showcaseFee: Int = 15,          // Cost for showcase placement
    val premiumBadgeFee: Int = 100,     // Cost for premium seller badge
    val boostListingFee: Int = 20       // Cost to boost listing visibility
)

@Entity(tableName = "showcase_slots")
data class ShowcaseSlot(
    @PrimaryKey
    val slotId: String = "",
    val category: ShowcaseCategory = ShowcaseCategory.BREEDING,
    val fowlId: String = "",
    val userId: String = "",
    val position: Int = 0,
    val duration: ShowcaseDuration = ShowcaseDuration.WEEK,
    val coinsSpent: Int = 0,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 1 week
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ShowcaseCategory {
    BREEDING,
    CHICKS,
    LAYERS,
    BROILERS,
    RARE_BREEDS,
    CHAMPIONS
}

enum class ShowcaseDuration {
    DAY(1, 5),
    WEEK(7, 15),
    MONTH(30, 40);
    
    val days: Int
    val coinCost: Int
    
    constructor(days: Int, coinCost: Int) {
        this.days = days
        this.coinCost = coinCost
    }
}