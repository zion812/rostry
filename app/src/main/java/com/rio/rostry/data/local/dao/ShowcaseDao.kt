package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.ShowcaseSlot
import com.rio.rostry.data.model.ShowcaseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowcaseDao {
    
    @Query("SELECT * FROM showcase_slots WHERE slotId = :slotId")
    suspend fun getShowcaseSlot(slotId: String): ShowcaseSlot?
    
    @Query("SELECT * FROM showcase_slots WHERE category = :category AND isActive = 1 AND endDate > :currentTime ORDER BY position ASC")
    fun getActiveShowcaseSlots(category: ShowcaseCategory, currentTime: Long = System.currentTimeMillis()): Flow<List<ShowcaseSlot>>
    
    @Query("SELECT * FROM showcase_slots WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserShowcaseSlots(userId: String): Flow<List<ShowcaseSlot>>
    
    @Query("SELECT * FROM showcase_slots WHERE fowlId = :fowlId ORDER BY createdAt DESC")
    fun getFowlShowcaseSlots(fowlId: String): Flow<List<ShowcaseSlot>>
    
    @Query("SELECT * FROM showcase_slots WHERE isActive = 1 AND endDate > :currentTime ORDER BY category ASC, position ASC")
    fun getAllActiveShowcaseSlots(currentTime: Long = System.currentTimeMillis()): Flow<List<ShowcaseSlot>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowcaseSlot(slot: ShowcaseSlot)
    
    @Update
    suspend fun updateShowcaseSlot(slot: ShowcaseSlot)
    
    @Delete
    suspend fun deleteShowcaseSlot(slot: ShowcaseSlot)
    
    @Query("DELETE FROM showcase_slots WHERE slotId = :slotId")
    suspend fun deleteShowcaseSlotById(slotId: String)
    
    @Query("UPDATE showcase_slots SET isActive = 0 WHERE endDate <= :currentTime")
    suspend fun deactivateExpiredSlots(currentTime: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM showcase_slots WHERE category = :category AND isActive = 1 AND endDate > :currentTime")
    suspend fun getActiveSlotsCount(category: ShowcaseCategory, currentTime: Long = System.currentTimeMillis()): Int
    
    @Query("SELECT MAX(position) FROM showcase_slots WHERE category = :category AND isActive = 1 AND endDate > :currentTime")
    suspend fun getMaxPositionInCategory(category: ShowcaseCategory, currentTime: Long = System.currentTimeMillis()): Int?
}