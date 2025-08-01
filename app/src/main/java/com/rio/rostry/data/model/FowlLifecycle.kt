package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Comprehensive fowl lifecycle tracking entity
 * Tracks complete lifecycle from egg to adult breeder stage
 */
@Entity(tableName = "fowl_lifecycle")
data class FowlLifecycle(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String,
    val currentStage: LifecycleStage = LifecycleStage.EGG,
    val stageStartDate: Long = System.currentTimeMillis(),
    val expectedNextStageDate: Long = 0,
    val batchId: String? = null,
    val parentMaleId: String? = null,
    val parentFemaleId: String? = null,
    val hatchingDetails: HatchingDetails? = null,
    val growthMetrics: List<GrowthMetric> = emptyList(),
    val milestones: List<LifecycleMilestone> = emptyList(),
    val isBreederCandidate: Boolean = false,
    val breederCertificationUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate progress percentage for current stage
     */
    fun getStageProgress(): Float {
        val currentTime = System.currentTimeMillis()
        val stageDuration = currentStage.durationWeeks * 7 * 24 * 60 * 60 * 1000L // Convert to milliseconds
        
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
        val currentIndex = stages.indexOf(currentStage)
        return if (currentIndex < stages.size - 1) stages[currentIndex + 1] else null
    }

    /**
     * Check if fowl is ready for next stage transition
     */
    fun isReadyForNextStage(): Boolean {
        return getStageProgress() >= 0.9f && getNextStage() != null
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

/**
 * Individual growth measurement record
 */
data class GrowthMetric(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val wingspan: Double = 0.0,
    val bodyConditionScore: Int = 5, // 1-10 scale
    val notes: String = "",
    val proofImageUrl: String = "",
    val recordedBy: String = "",
    val measurementMethod: MeasurementMethod = MeasurementMethod.MANUAL,
    val environmentalConditions: EnvironmentalConditions? = null
) {
    /**
     * Calculate growth rate compared to previous measurement
     */
    fun calculateGrowthRate(previousMetric: GrowthMetric?): Double {
        return previousMetric?.let { prev ->
            val daysDiff = (date - prev.date) / (24 * 60 * 60 * 1000)
            if (daysDiff > 0) {
                ((weight - prev.weight) / daysDiff) * 7 // Weekly growth rate
            } else {
                0.0
            }
        } ?: 0.0
    }

    /**
     * Assess if growth is within normal range
     */
    fun isGrowthNormal(expectedWeight: Double, tolerance: Double = 0.15): Boolean {
        val deviation = kotlin.math.abs(weight - expectedWeight) / expectedWeight
        return deviation <= tolerance
    }
}

enum class MeasurementMethod(val displayName: String) {
    MANUAL("Manual Measurement"),
    DIGITAL_SCALE("Digital Scale"),
    AUTOMATED("Automated System"),
    ESTIMATED("Visual Estimation")
}

data class EnvironmentalConditions(
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val lightHours: Double = 0.0,
    val feedType: String = "",
    val weatherConditions: String = ""
)

/**
 * Lifecycle milestone achievement record
 */
data class LifecycleMilestone(
    val id: String = UUID.randomUUID().toString(),
    val stage: LifecycleStage,
    val achievedDate: Long,
    val description: String,
    val proofImageUrls: List<String> = emptyList(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val verifiedBy: String = "",
    val verificationDate: Long = 0,
    val notes: String = "",
    val isSignificant: Boolean = false // Major milestones like first egg, first breeding
) {
    /**
     * Check if milestone is overdue based on expected timing
     */
    fun isOverdue(expectedDate: Long): Boolean {
        return System.currentTimeMillis() > expectedDate && verificationStatus == VerificationStatus.PENDING
    }

    /**
     * Get days since achievement
     */
    fun getDaysSinceAchievement(): Long {
        return (System.currentTimeMillis() - achievedDate) / (24 * 60 * 60 * 1000)
    }
}