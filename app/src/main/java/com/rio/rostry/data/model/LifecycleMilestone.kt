package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Lifecycle milestone achievement record entity
 */
@Entity(tableName = "lifecycle_milestones")
data class LifecycleMilestone(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String = "",
    val stage: String = "EGG", // Store as string for Room compatibility
    val milestone: String = "",
    val description: String = "",
    val achievedDate: Long = System.currentTimeMillis(),
    val actualDate: Long = System.currentTimeMillis(), // Add missing actualDate
    val expectedDate: Long? = null,
    val isOnSchedule: Boolean = true,
    val notes: String = "",
    val imageUrl: String? = null,
    val recordedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if milestone is overdue based on expected timing
     */
    fun isOverdue(expectedDate: Long): Boolean {
        return System.currentTimeMillis() > expectedDate
    }

    /**
     * Get days since achievement
     */
    fun getDaysSinceAchievement(): Long {
        return (System.currentTimeMillis() - achievedDate) / (24 * 60 * 60 * 1000)
    }
}