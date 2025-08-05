package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Comprehensive fowl lifecycle tracking entity
 * Tracks complete lifecycle from egg to adult breeder stage
 */
@Entity(tableName = "fowl_lifecycles")
data class FowlLifecycle(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String,
    val farmId: String? = null,
    val currentStage: String = "EGG", // Store as string for Room compatibility
    val stageStartDate: Long = System.currentTimeMillis(),
    val expectedTransitionDate: Long? = null,
    val actualTransitionDate: Long? = null,
    val stageProgress: Double = 0.0,
    val milestones: String = "[]", // Store as JSON string
    val growthRecords: String = "[]", // Add missing growthRecords as JSON string
    val healthMetrics: String? = null, // Store as JSON string
    val growthMetrics: String? = null, // Store as JSON string
    val environmentalFactors: String? = null, // Store as JSON string
    val careInstructions: String = "[]", // Store as JSON string
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get current stage as enum
     */
    fun getCurrentStageEnum(): LifecycleStage {
        return try {
            LifecycleStage.valueOf(currentStage)
        } catch (e: Exception) {
            LifecycleStage.EGG
        }
    }

    /**
     * Calculate progress percentage for current stage
     */
    fun getStageProgressCalculated(): Float {
        val currentTime = System.currentTimeMillis()
        val stage = getCurrentStageEnum()
        val stageDuration = stage.durationWeeks * 7 * 24 * 60 * 60 * 1000L // Convert to milliseconds
        
        return if (stageDuration > 0) {
            val elapsed = currentTime - stageStartDate
            (elapsed.toFloat() / stageDuration).coerceIn(0f, 1f)
        } else {
            1f // For stages with indefinite duration
        }
    }

    /**
     * Get the next expected stage based on current progress
     */
    fun getNextStage(): LifecycleStage? {
        val stages = LifecycleStage.values()
        val currentStageEnum = getCurrentStageEnum()
        val currentIndex = stages.indexOf(currentStageEnum)
        return if (currentIndex < stages.size - 1) stages[currentIndex + 1] else null
    }

    /**
     * Check if fowl is ready for next stage transition
     */
    fun isReadyForNextStage(): Boolean {
        return getStageProgressCalculated() >= 0.9f && getNextStage() != null
    }
}

/**
 * Lifecycle stages with duration and characteristics
 */
enum class LifecycleStage(
    val displayName: String, 
    val durationWeeks: Int,
    val description: String,
    val keyMilestones: List<String>
) {
    EGG(
        "Egg Stage", 
        3,
        "Incubation period before hatching",
        listOf("Fertilization confirmed", "Development visible", "Ready to hatch")
    ),
    HATCHING(
        "Hatching", 
        1,
        "Breaking out of shell and first days",
        listOf("Shell cracking", "Emergence", "First feeding")
    ),
    CHICK(
        "Chick Development", 
        16,
        "Rapid growth and development phase",
        listOf("Feather development", "Walking", "Independent feeding", "First vaccinations")
    ),
    JUVENILE(
        "Growth & Development", 
        32,
        "Continued growth towards maturity",
        listOf("Sexual maturity", "Full feather development", "Adult size reached")
    ),
    ADULT(
        "Adult/Breeder", 
        -1,
        "Mature fowl capable of breeding",
        listOf("First breeding", "Egg laying", "Territory establishment")
    ),
    BREEDER_ACTIVE(
        "Active Breeder", 
        -1,
        "Proven breeding fowl with offspring",
        listOf("Successful breeding", "Multiple clutches", "Lineage established")
    );

    /**
     * Get the minimum age in weeks for this stage
     */
    fun getMinimumAgeWeeks(): Int {
        return when (this) {
            EGG -> 0
            HATCHING -> 3
            CHICK -> 4
            JUVENILE -> 20
            ADULT -> 52
            BREEDER_ACTIVE -> 78
        }
    }
}

/**
 * Detailed hatching information for batch tracking
 */
data class HatchingDetails(
    val eggCount: Int = 0,
    val hatchedCount: Int = 0,
    val hatchRate: Double = 0.0,
    val hatchingLocation: String = "",
    val batchNumber: String = "",
    val incubationStartDate: Long = 0,
    val hatchingDate: Long = 0,
    val proofImageUrls: List<String> = emptyList(),
    val notes: String = ""
) {
    /**
     * Calculate hatching success rate
     */
    fun calculateHatchRate(): Double {
        return if (eggCount > 0) {
            (hatchedCount.toDouble() / eggCount.toDouble()) * 100
        } else {
            0.0
        }
    }

    /**
     * Determine hatching quality rating
     */
    fun getHatchingQuality(): HatchingQuality {
        val rate = calculateHatchRate()
        return when {
            rate >= 90 -> HatchingQuality.EXCELLENT
            rate >= 80 -> HatchingQuality.GOOD
            rate >= 70 -> HatchingQuality.AVERAGE
            rate >= 60 -> HatchingQuality.POOR
            else -> HatchingQuality.FAILED
        }
    }
}

enum class HatchingQuality(val displayName: String, val description: String) {
    EXCELLENT("Excellent", "90%+ hatch rate"),
    GOOD("Good", "80-89% hatch rate"),
    AVERAGE("Average", "70-79% hatch rate"),
    POOR("Poor", "60-69% hatch rate"),
    FAILED("Failed", "Below 60% hatch rate")
}
