package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.TransferLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferLogDao {
    
    @Query("SELECT * FROM transfer_logs WHERE fowlId = :fowlId ORDER BY timestamp DESC")
    fun getTransfersByFowlId(fowlId: String): Flow<List<TransferLog>>
    
    @Query("SELECT * FROM transfer_logs WHERE giverId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getUserTransfers(userId: String): Flow<List<TransferLog>>
    
    @Query("SELECT * FROM transfer_logs WHERE receiverId = :userId AND status = 'pending' ORDER BY timestamp DESC")
    fun getPendingTransfers(userId: String): Flow<List<TransferLog>>
    
    @Query("SELECT * FROM transfer_logs WHERE transferId = :transferId")
    suspend fun getTransferById(transferId: String): TransferLog?
    
    @Query("SELECT * FROM transfer_logs ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferLog)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfers(transfers: List<TransferLog>)
    
    @Update
    suspend fun updateTransfer(transfer: TransferLog)
    
    @Delete
    suspend fun deleteTransfer(transfer: TransferLog)
    
    @Query("DELETE FROM transfer_logs WHERE transferId = :transferId")
    suspend fun deleteTransferById(transferId: String)
    
    @Query("SELECT * FROM transfer_logs WHERE status = :status ORDER BY timestamp DESC")
    fun getTransfersByStatus(status: String): Flow<List<TransferLog>>
    
    @Query("SELECT * FROM transfer_logs WHERE giverId = :userId ORDER BY timestamp DESC")
    fun getOutgoingTransfers(userId: String): Flow<List<TransferLog>>
    
    @Query("SELECT * FROM transfer_logs WHERE receiverId = :userId ORDER BY timestamp DESC")
    fun getIncomingTransfers(userId: String): Flow<List<TransferLog>>
}