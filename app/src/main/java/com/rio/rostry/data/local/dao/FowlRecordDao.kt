package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.FowlRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FowlRecordDao {
    
    @Query("SELECT * FROM fowl_records WHERE fowlId = :fowlId ORDER BY date DESC")
    fun getRecordsByFowlId(fowlId: String): Flow<List<FowlRecord>>
    
    @Query("SELECT * FROM fowl_records WHERE recordId = :recordId")
    suspend fun getRecordById(recordId: String): FowlRecord?
    
    @Query("SELECT * FROM fowl_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<FowlRecord>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FowlRecord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<FowlRecord>)
    
    @Update
    suspend fun updateRecord(record: FowlRecord)
    
    @Delete
    suspend fun deleteRecord(record: FowlRecord)
    
    @Query("DELETE FROM fowl_records WHERE recordId = :recordId")
    suspend fun deleteRecordById(recordId: String)
    
    @Query("DELETE FROM fowl_records WHERE fowlId = :fowlId")
    suspend fun deleteRecordsByFowlId(fowlId: String)
    
    @Query("SELECT * FROM fowl_records WHERE createdBy = :userId ORDER BY date DESC")
    fun getRecordsByUser(userId: String): Flow<List<FowlRecord>>
    
    @Query("SELECT * FROM fowl_records WHERE recordType = :type ORDER BY date DESC")
    fun getRecordsByType(type: String): Flow<List<FowlRecord>>
}