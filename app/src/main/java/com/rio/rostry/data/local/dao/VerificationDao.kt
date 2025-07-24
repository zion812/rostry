package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.VerificationRequest
import com.rio.rostry.data.model.VerificationType
import com.rio.rostry.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    
    @Query("SELECT * FROM verification_requests WHERE requestId = :requestId")
    suspend fun getVerificationRequest(requestId: String): VerificationRequest?
    
    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getUserVerificationRequests(userId: String): Flow<List<VerificationRequest>>
    
    @Query("SELECT * FROM verification_requests WHERE status = :status ORDER BY submittedAt ASC")
    fun getVerificationRequestsByStatus(status: VerificationStatus): Flow<List<VerificationRequest>>
    
    @Query("SELECT * FROM verification_requests WHERE verificationType = :type ORDER BY submittedAt DESC")
    fun getVerificationRequestsByType(type: VerificationType): Flow<List<VerificationRequest>>
    
    @Query("SELECT * FROM verification_requests ORDER BY submittedAt DESC")
    fun getAllVerificationRequests(): Flow<List<VerificationRequest>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(request: VerificationRequest)
    
    @Update
    suspend fun updateVerificationRequest(request: VerificationRequest)
    
    @Delete
    suspend fun deleteVerificationRequest(request: VerificationRequest)
    
    @Query("DELETE FROM verification_requests WHERE requestId = :requestId")
    suspend fun deleteVerificationRequestById(requestId: String)
    
    @Query("SELECT COUNT(*) FROM verification_requests WHERE userId = :userId AND status = 'PENDING'")
    suspend fun getPendingRequestsCount(userId: String): Int
    
    @Query("SELECT * FROM verification_requests WHERE userId = :userId AND verificationType = :type AND status = 'APPROVED' ORDER BY reviewedAt DESC LIMIT 1")
    suspend fun getLatestApprovedRequest(userId: String, type: VerificationType): VerificationRequest?
}