package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.rio.rostry.data.local.dao.WalletDao
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val walletDao: WalletDao
) {
    
    suspend fun getOrCreateWallet(userId: String): Wallet {
        return try {
            val snapshot = firestore.collection("wallets").document(userId).get().await()
            val wallet = snapshot.toObject(Wallet::class.java) ?: Wallet(userId = userId)
            
            if (!snapshot.exists()) {
                firestore.collection("wallets").document(userId).set(wallet).await()
            }
            
            walletDao.insertWallet(wallet)
            wallet
        } catch (e: Exception) {
            walletDao.getWallet(userId) ?: Wallet(userId = userId).also {
                walletDao.insertWallet(it)
            }
        }
    }
    
    fun getWalletFlow(userId: String): Flow<Wallet?> = flow {
        try {
            val snapshot = firestore.collection("wallets").document(userId).get().await()
            val wallet = snapshot.toObject(Wallet::class.java)
            wallet?.let { walletDao.insertWallet(it) }
            emit(wallet)
        } catch (e: Exception) {
            walletDao.getWalletFlow(userId).collect { emit(it) }
        }
    }
    
    suspend fun addCoins(
        userId: String, 
        amount: Int, 
        description: String,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val walletRef = firestore.collection("wallets").document(userId)
                val walletSnapshot = transaction.get(walletRef)
                
                val currentWallet = walletSnapshot.toObject(Wallet::class.java) 
                    ?: Wallet(userId = userId)
                
                val newBalance = currentWallet.coinBalance + amount
                val updatedWallet = currentWallet.copy(
                    coinBalance = newBalance,
                    totalCoinsEarned = currentWallet.totalCoinsEarned + amount,
                    lastUpdated = System.currentTimeMillis()
                )
                
                transaction.set(walletRef, updatedWallet)
                
                // Create transaction record
                val coinTransaction = CoinTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    type = CoinTransactionType.CREDIT,
                    amount = amount,
                    description = description,
                    relatedEntityId = relatedEntityId,
                    relatedEntityType = relatedEntityType,
                    balanceBefore = currentWallet.coinBalance,
                    balanceAfter = newBalance
                )
                
                transaction.set(
                    firestore.collection("coin_transactions").document(coinTransaction.transactionId),
                    coinTransaction
                )
                
                // Update local database
                
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deductCoins(
        userId: String, 
        amount: Int, 
        description: String,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val walletRef = firestore.collection("wallets").document(userId)
                val walletSnapshot = transaction.get(walletRef)
                
                val currentWallet = walletSnapshot.toObject(Wallet::class.java) 
                    ?: Wallet(userId = userId)
                
                if (currentWallet.coinBalance < amount) {
                    throw InsufficientCoinsException("Insufficient coins. Required: $amount, Available: ${currentWallet.coinBalance}")
                }
                
                val newBalance = currentWallet.coinBalance - amount
                val updatedWallet = currentWallet.copy(
                    coinBalance = newBalance,
                    totalCoinsSpent = currentWallet.totalCoinsSpent + amount,
                    lastUpdated = System.currentTimeMillis()
                )
                
                transaction.set(walletRef, updatedWallet)
                
                // Create transaction record
                val coinTransaction = CoinTransaction(
                    transactionId = UUID.randomUUID().toString(),
                    userId = userId,
                    type = CoinTransactionType.DEBIT,
                    amount = amount,
                    description = description,
                    relatedEntityId = relatedEntityId,
                    relatedEntityType = relatedEntityType,
                    balanceBefore = currentWallet.coinBalance,
                    balanceAfter = newBalance
                )
                
                transaction.set(
                    firestore.collection("coin_transactions").document(coinTransaction.transactionId),
                    coinTransaction
                )
                
                // Update local database
                
                null
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCoinBalance(userId: String): Int {
        return try {
            val snapshot = firestore.collection("wallets").document(userId).get().await()
            snapshot.toObject(Wallet::class.java)?.coinBalance ?: 0
        } catch (e: Exception) {
            walletDao.getCoinBalance(userId) ?: 0
        }
    }
    
    fun getUserTransactions(userId: String): Flow<List<CoinTransaction>> = flow {
        try {
            val snapshot = firestore.collection("coin_transactions")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val transactions = snapshot.documents.mapNotNull { it.toObject(CoinTransaction::class.java) }
            transactions.forEach { walletDao.insertTransaction(it) }
            emit(transactions)
        } catch (e: Exception) {
            walletDao.getUserTransactions(userId).collect { emit(it) }
        }
    }
    
    suspend fun purchaseCoins(userId: String, coinPackage: CoinPackage, purchaseToken: String): Result<Unit> {
        return try {
            // Simple mock payment - simulates successful purchase without external SDKs
            kotlinx.coroutines.delay(2000) // Simulate processing time
            
            addCoins(
                userId = userId,
                amount = coinPackage.totalCoins,
                description = "Purchased ${coinPackage.name} (Demo Mode)",
                relatedEntityId = coinPackage.id,
                relatedEntityType = "coin_package"
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCoinPackages(): List<CoinPackage> {
        return listOf(
            CoinPackage(
                id = "starter_pack",
                name = "Starter Pack",
                coinAmount = 100,
                price = 4.99,
                bonusCoins = 10,
                description = "Perfect for getting started",
                googlePlayProductId = "coins_starter_pack"
            ),
            CoinPackage(
                id = "value_pack",
                name = "Value Pack",
                coinAmount = 250,
                price = 9.99,
                bonusCoins = 50,
                isPopular = true,
                description = "Most popular choice",
                googlePlayProductId = "coins_value_pack"
            ),
            CoinPackage(
                id = "premium_pack",
                name = "Premium Pack",
                coinAmount = 500,
                price = 19.99,
                bonusCoins = 150,
                description = "Best value for power users",
                googlePlayProductId = "coins_premium_pack"
            ),
            CoinPackage(
                id = "mega_pack",
                name = "Mega Pack",
                coinAmount = 1000,
                price = 34.99,
                bonusCoins = 400,
                description = "Ultimate coin package",
                googlePlayProductId = "coins_mega_pack"
            )
        )
    }
    
    fun getCoinPricing(): CoinPricing {
        return CoinPricing()
    }
}

class InsufficientCoinsException(message: String) : Exception(message)