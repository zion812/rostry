package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Individual growth measurement record entity
 */
@Entity(tableName = "growth_metrics")
data class GrowthMetric(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String = "",
    val flockId: String? = null,
    val measurementDate: Long = System.currentTimeMillis(),
    val weight: Double = 0.0,
    val height: Double? = null,
    val length: Double? = null,
    val wingSpan: Double? = null,
    val bodyConditionScore: Int = 5, // 1-10 scale
    val notes: String = "",
    val measuredBy: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate growth rate compared to previous measurement
     */
    fun calculateGrowthRate(previousMetric: GrowthMetric?): Double {
        return previousMetric?.let { prev ->
            val daysDiff = (measurementDate - prev.measurementDate) / (24 * 60 * 60 * 1000)
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
