package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.Wallet
import com.rio.rostry.data.model.CoinTransaction
import com.rio.rostry.data.model.CoinTransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun getWallet(userId: String): Wallet?
    
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun getWalletFlow(userId: String): Flow<Wallet?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: Wallet)
    
    @Update
    suspend fun updateWallet(wallet: Wallet)
    
    @Query("UPDATE wallets SET coinBalance = :newBalance, lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateCoinBalance(userId: String, newBalance: Int, timestamp: Long)
    
    @Query("SELECT coinBalance FROM wallets WHERE userId = :userId")
    suspend fun getCoinBalance(userId: String): Int?
    
    // Coin Transactions
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserTransactions(userId: String): Flow<List<CoinTransaction>>
    
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getUserTransactionsByType(userId: String, type: CoinTransactionType): Flow<List<CoinTransaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransaction)
    
    @Query("SELECT * FROM coin_transactions WHERE transactionId = :transactionId")
    suspend fun getTransactionById(transactionId: String): CoinTransaction?
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE userId = :userId AND type = 'CREDIT'")
    suspend fun getTotalCoinsEarned(userId: String): Int?
    
    @Query("SELECT SUM(amount) FROM coin_transactions WHERE userId = :userId AND type = 'DEBIT'")
    suspend fun getTotalCoinsSpent(userId: String): Int?
    
    @Query("SELECT * FROM coin_transactions WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<CoinTransaction>>
}