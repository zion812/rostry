package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Comprehensive flock management entity for group fowl tracking
 */
@Entity(tableName = "flocks")
data class Flock(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val flockName: String,
    val flockType: FlockType,
    val breed: String,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val maleCount: Int = 0,
    val femaleCount: Int = 0,
    val averageAge: Int = 0, // in weeks
    val establishedDate: Long = System.currentTimeMillis(),
    val facilityId: String? = null,
    val healthStatus: FlockHealthStatus = FlockHealthStatus.HEALTHY,
    val feedingSchedule: FeedingSchedule? = null,
    val vaccinationSchedule: List<VaccinationRecord> = emptyList(),
    val productionMetrics: ProductionMetrics? = null,
    val environmentalConditions: EnvironmentalMonitoring? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate mortality rate
     */
    fun getMortalityRate(): Double {
        return if (totalCount > 0) {
            ((totalCount - activeCount).toDouble() / totalCount) * 100
        } else 0.0
    }

    /**
     * Calculate male to female ratio
     */
    fun getMaleToFemaleRatio(): Double {
        return if (femaleCount > 0) {
            maleCount.toDouble() / femaleCount
        } else if (maleCount > 0) Double.POSITIVE_INFINITY else 0.0
    }

    /**
     * Check if flock needs attention
     */
    fun needsAttention(): Boolean {
        return healthStatus in listOf(FlockHealthStatus.MONITORING, FlockHealthStatus.TREATMENT, FlockHealthStatus.QUARANTINE) ||
               getMortalityRate() > 5.0 ||
               hasOverdueVaccinations()
    }

    /**
     * Check for overdue vaccinations
     */
    fun hasOverdueVaccinations(): Boolean {
        val currentTime = System.currentTimeMillis()
        return vaccinationSchedule.any { it.nextDueDate > 0 && it.nextDueDate < currentTime }
    }

    /**
     * Get next vaccination due
     */
    fun getNextVaccinationDue(): VaccinationRecord? {
        val currentTime = System.currentTimeMillis()
        return vaccinationSchedule
            .filter { it.nextDueDate > currentTime }
            .minByOrNull { it.nextDueDate }
    }

    /**
     * Calculate flock performance score
     */
    fun getPerformanceScore(): Double {
        val healthScore = when (healthStatus) {
            FlockHealthStatus.HEALTHY -> 1.0
            FlockHealthStatus.RECOVERED -> 0.9
            FlockHealthStatus.MONITORING -> 0.7
            FlockHealthStatus.TREATMENT -> 0.5
            FlockHealthStatus.QUARANTINE -> 0.3
        }
        
        val mortalityScore = ((100 - getMortalityRate()) / 100).coerceIn(0.0, 1.0)
        
        val productionScore = productionMetrics?.let { metrics ->
            val eggScore = if (flockType == FlockType.LAYING_HENS) {
                (metrics.eggProductionRate / 0.8).coerceIn(0.0, 1.0) // 80% is excellent
            } else 1.0
            
            val feedScore = if (metrics.feedConversionRatio > 0) {
                (2.0 / metrics.feedConversionRatio).coerceIn(0.0, 1.0) // 2.0 is excellent FCR
            } else 0.5
            
            (eggScore + feedScore) / 2
        } ?: 0.5

        return (healthScore * 0.4 + mortalityScore * 0.3 + productionScore * 0.3)
    }

    /**
     * Get recommended actions for flock management
     */
    fun getRecommendedActions(): List<String> {
        val actions = mutableListOf<String>()
        
        if (getMortalityRate() > 5.0) {
            actions.add("Investigate high mortality rate")
        }
        
        if (hasOverdueVaccinations()) {
            actions.add("Update overdue vaccinations")
        }
        
        if (healthStatus != FlockHealthStatus.HEALTHY) {
            actions.add("Monitor health status closely")
        }
        
        productionMetrics?.let { metrics ->
            if (metrics.feedConversionRatio > 3.0) {
                actions.add("Optimize feeding program")
            }
            if (flockType == FlockType.LAYING_HENS && metrics.eggProductionRate < 0.6) {
                actions.add("Improve laying conditions")
            }
        }
        
        return actions
    }
}

enum class FlockType(val displayName: String, val description: String, val icon: String) {
    BREEDING_STOCK("Breeding Stock", "Selected fowls for reproduction", "💕"),
    LAYING_HENS("Laying Hens", "Hens for egg production", "🥚"),
    BROILERS("Broilers", "Fowls for meat production", "��"),
    CHICKS("Chicks", "Young fowls under 8 weeks", "🐣"),
    PULLETS("Pullets", "Young female fowls 8-20 weeks", "🐤"),
    ROOSTERS("Roosters", "Male fowls for breeding", "🐓"),
    MIXED("Mixed Flock", "Mixed age and purpose flock", "🐔")
}

enum class FlockHealthStatus(val displayName: String, val color: String, val priority: Int) {
    HEALTHY("Healthy", "#4CAF50", 1),
    MONITORING("Under Monitoring", "#FF9800", 2),
    TREATMENT("Under Treatment", "#F44336", 3),
    QUARANTINE("Quarantined", "#9C27B0", 4),
    RECOVERED("Recovered", "#2196F3", 5)
}

/**
 * Feeding schedule and nutrition management
 */
data class FeedingSchedule(
    val feedType: String,
    val feedBrand: String = "",
    val dailyAmount: Double, // kg per bird
    val feedingTimes: List<String> = emptyList(),
    val nutritionalContent: NutritionalContent? = null,
    val supplements: List<Supplement> = emptyList(),
    val specialInstructions: String = "",
    val cost: Double = 0.0, // cost per kg
    val supplier: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val nextReviewDate: Long = 0
) {
    /**
     * Calculate daily feeding cost per bird
     */
    fun getDailyCostPerBird(): Double {
        return dailyAmount * cost
    }

    /**
     * Calculate total daily cost for flock
     */
    fun getTotalDailyCost(flockSize: Int): Double {
        return getDailyCostPerBird() * flockSize
    }

    /**
     * Check if feeding schedule needs review
     */
    fun needsReview(): Boolean {
        return nextReviewDate > 0 && System.currentTimeMillis() > nextReviewDate
    }
}

/**
 * Nutritional content of feed
 */
data class NutritionalContent(
    val protein: Double = 0.0, // percentage
    val fat: Double = 0.0,     // percentage
    val fiber: Double = 0.0,   // percentage
    val ash: Double = 0.0,     // percentage
    val moisture: Double = 0.0, // percentage
    val calcium: Double = 0.0,  // percentage
    val phosphorus: Double = 0.0, // percentage
    val energy: Double = 0.0,   // kcal/kg
    val vitamins: Map<String, Double> = emptyMap(),
    val minerals: Map<String, Double> = emptyMap()
) {
    /**
     * Check if nutritional content meets requirements for flock type
     */
    fun meetsRequirements(flockType: FlockType): Boolean {
        return when (flockType) {
            FlockType.LAYING_HENS -> protein >= 16.0 && calcium >= 3.5
            FlockType.BROILERS -> protein >= 20.0 && energy >= 3000
            FlockType.BREEDING_STOCK -> protein >= 18.0 && calcium >= 3.0
            FlockType.CHICKS -> protein >= 22.0 && energy >= 3100
            FlockType.PULLETS -> protein >= 18.0 && calcium >= 1.0
            else -> protein >= 16.0
        }
    }
}

/**
 * Feed supplements and additives
 */
data class Supplement(
    val name: String,
    val type: SupplementType,
    val dosage: String,
    val frequency: String,
    val purpose: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = 0,
    val cost: Double = 0.0
)

enum class SupplementType(val displayName: String) {
    VITAMIN("Vitamin Supplement"),
    MINERAL("Mineral Supplement"),
    PROBIOTIC("Probiotic"),
    ANTIBIOTIC("Antibiotic"),
    GROWTH_PROMOTER("Growth Promoter"),
    IMMUNE_BOOSTER("Immune Booster"),
    DIGESTIVE_AID("Digestive Aid")
}

/**
 * Production metrics for performance tracking
 */
data class ProductionMetrics(
    val eggProductionRate: Double = 0.0, // eggs per hen per day
    val averageEggWeight: Double = 0.0,  // grams
    val eggQuality: EggQuality? = null,
    val feedConversionRatio: Double = 0.0, // feed consumed / eggs produced
    val mortalityRate: Double = 0.0,
    val bodyWeightGain: Double = 0.0, // grams per day
    val uniformity: Double = 0.0, // percentage
    val livability: Double = 0.0, // percentage
    val profitability: Double = 0.0, // profit per bird
    val lastCalculated: Long = System.currentTimeMillis(),
    val calculationPeriod: String = "weekly" // daily, weekly, monthly
) {
    /**
     * Calculate production efficiency
     */
    fun getProductionEfficiency(): Double {
        val eggScore = eggProductionRate.coerceIn(0.0, 1.0)
        val feedScore = if (feedConversionRatio > 0) (2.0 / feedConversionRatio).coerceIn(0.0, 1.0) else 0.0
        val healthScore = ((100 - mortalityRate) / 100).coerceIn(0.0, 1.0)
        
        return (eggScore * 0.4 + feedScore * 0.3 + healthScore * 0.3)
    }

    /**
     * Get performance rating
     */
    fun getPerformanceRating(): PerformanceRating {
        val efficiency = getProductionEfficiency()
        return when {
            efficiency >= 0.9 -> PerformanceRating.OUTSTANDING
            efficiency >= 0.8 -> PerformanceRating.EXCELLENT
            efficiency >= 0.7 -> PerformanceRating.GOOD
            efficiency >= 0.6 -> PerformanceRating.AVERAGE
            else -> PerformanceRating.BELOW_AVERAGE
        }
    }
}

/**
 * Egg quality assessment
 */
data class EggQuality(
    val shellStrength: Double = 0.0, // kg/cm²
    val shellThickness: Double = 0.0, // mm
    val albumenHeight: Double = 0.0, // mm
    val yolkColor: Int = 0, // Roche color scale 1-15
    val haughUnit: Double = 0.0, // quality measure
    val bloodSpots: Double = 0.0, // percentage
    val cracked: Double = 0.0, // percentage
    val dirty: Double = 0.0, // percentage
    val gradeA: Double = 0.0, // percentage
    val gradeB: Double = 0.0, // percentage
    val gradeC: Double = 0.0  // percentage
) {
    /**
     * Calculate overall egg quality score
     */
    fun getQualityScore(): Double {
        val haughScore = (haughUnit / 100).coerceIn(0.0, 1.0)
        val gradeScore = gradeA / 100
        val defectScore = 1.0 - ((bloodSpots + cracked + dirty) / 300).coerceIn(0.0, 1.0)
        
        return (haughScore * 0.4 + gradeScore * 0.4 + defectScore * 0.2)
    }
}

/**
 * Vaccination record for disease prevention
 */
@Entity(tableName = "vaccination_records")
data class VaccinationRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val flockId: String? = null,
    val fowlId: String? = null,
    val vaccineName: String,
    val vaccineType: VaccineType,
    val administrationDate: Long,
    val nextDueDate: Long = 0,
    val dosage: String = "",
    val administrationMethod: AdministrationMethod = AdministrationMethod.INJECTION,
    val administeredBy: String = "",
    val batchNumber: String = "",
    val manufacturer: String = "",
    val expiryDate: Long = 0,
    val storageTemperature: String = "",
    val proofImageUrl: String = "",
    val notes: String = "",
    val sideEffects: String = "",
    val efficacy: Double = 0.0, // percentage
    val cost: Double = 0.0,
    val veterinarianApproval: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if vaccination is due soon
     */
    fun isDueSoon(daysAhead: Int = 7): Boolean {
        val dueTime = System.currentTimeMillis() + (daysAhead * 24 * 60 * 60 * 1000L)
        return nextDueDate > 0 && nextDueDate <= dueTime
    }

    /**
     * Check if vaccination is overdue
     */
    fun isOverdue(): Boolean {
        return nextDueDate > 0 && nextDueDate < System.currentTimeMillis()
    }

    /**
     * Get days until due
     */
    fun getDaysUntilDue(): Long {
        return if (nextDueDate > 0) {
            (nextDueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)
        } else 0
    }
}

enum class VaccineType(val displayName: String, val description: String, val frequency: String) {
    NEWCASTLE("Newcastle Disease", "Viral respiratory disease", "Every 3-4 months"),
    INFECTIOUS_BRONCHITIS("Infectious Bronchitis", "Respiratory tract infection", "Every 3-4 months"),
    FOWL_POX("Fowl Pox", "Viral skin disease", "Annually"),
    MAREK_DISEASE("Marek's Disease", "Viral cancer disease", "Day 1 (chicks)"),
    INFECTIOUS_BURSAL("Infectious Bursal Disease", "Immune system disease", "2-3 weeks old"),
    AVIAN_INFLUENZA("Avian Influenza", "Highly contagious viral disease", "As required"),
    SALMONELLA("Salmonella", "Bacterial infection", "Every 6 months"),
    COCCIDIOSIS("Coccidiosis", "Parasitic disease", "As required"),
    EGG_DROP_SYNDROME("Egg Drop Syndrome", "Viral disease affecting egg production", "Before laying"),
    OTHER("Other", "Other vaccines as prescribed", "As prescribed")
}

enum class AdministrationMethod(val displayName: String) {
    INJECTION("Subcutaneous/Intramuscular Injection"),
    DRINKING_WATER("Drinking Water"),
    SPRAY("Spray Application"),
    EYE_DROP("Eye Drop"),
    ORAL("Oral Administration"),
    IN_OVO("In-Ovo (Egg Injection)")
}

/**
 * Environmental monitoring for optimal conditions
 */
data class EnvironmentalMonitoring(
    val temperature: TemperatureReading? = null,
    val humidity: HumidityReading? = null,
    val airQuality: AirQualityReading? = null,
    val lighting: LightingConditions? = null,
    val ventilation: VentilationStatus? = null,
    val noiseLevel: Double = 0.0, // decibels
    val lastUpdated: Long = System.currentTimeMillis(),
    val alertsEnabled: Boolean = true,
    val optimalRanges: OptimalRanges? = null
) {
    /**
     * Check if all conditions are within optimal ranges
     */
    fun isOptimal(): Boolean {
        return optimalRanges?.let { ranges ->
            temperature?.isWithinRange(ranges.temperatureRange) == true &&
            humidity?.isWithinRange(ranges.humidityRange) == true &&
            (airQuality?.co2Level ?: 0.0) <= ranges.maxCO2Level &&
            noiseLevel <= ranges.maxNoiseLevel
        } ?: false
    }

    /**
     * Get environmental alerts
     */
    fun getAlerts(): List<String> {
        val alerts = mutableListOf<String>()
        
        optimalRanges?.let { ranges ->
            temperature?.let { temp ->
                if (!temp.isWithinRange(ranges.temperatureRange)) {
                    alerts.add("Temperature out of range: ${temp.current}°C")
                }
            }
            
            humidity?.let { hum ->
                if (!hum.isWithinRange(ranges.humidityRange)) {
                    alerts.add("Humidity out of range: ${hum.current}%")
                }
            }
            
            airQuality?.let { air ->
                if (air.co2Level > ranges.maxCO2Level) {
                    alerts.add("CO2 level too high: ${air.co2Level} ppm")
                }
            }
            
            if (noiseLevel > ranges.maxNoiseLevel) {
                alerts.add("Noise level too high: ${noiseLevel} dB")
            }
        }
        
        return alerts
    }
}

data class TemperatureReading(
    val current: Double,
    val minimum: Double,
    val maximum: Double,
    val unit: String = "°C",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isWithinRange(range: ClosedFloatingPointRange<Double>): Boolean {
        return current in range
    }
}

data class HumidityReading(
    val current: Double,
    val minimum: Double,
    val maximum: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isWithinRange(range: ClosedFloatingPointRange<Double>): Boolean {
        return current in range
    }
}

data class AirQualityReading(
    val co2Level: Double, // ppm
    val ammoniaLevel: Double = 0.0, // ppm
    val dustLevel: Double = 0.0, // mg/m³
    val airflow: Double = 0.0, // m/s
    val timestamp: Long = System.currentTimeMillis()
)

data class LightingConditions(
    val intensity: Double, // lux
    val duration: Double, // hours per day
    val spectrum: String = "full spectrum",
    val isNatural: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class VentilationStatus(
    val airChangesPerHour: Double,
    val fanSpeed: Double = 0.0, // percentage
    val isAutomatic: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class OptimalRanges(
    val temperatureRange: ClosedFloatingPointRange<Double>,
    val humidityRange: ClosedFloatingPointRange<Double>,
    val maxCO2Level: Double,
    val maxAmmoniaLevel: Double,
    val maxNoiseLevel: Double,
    val minLightIntensity: Double,
    val maxLightIntensity: Double
) {
    companion object {
        fun forFlockType(flockType: FlockType): OptimalRanges {
            return when (flockType) {
                FlockType.CHICKS -> OptimalRanges(
                    temperatureRange = 32.0..35.0,
                    humidityRange = 60.0..70.0,
                    maxCO2Level = 3000.0,
                    maxAmmoniaLevel = 25.0,
                    maxNoiseLevel = 60.0,
                    minLightIntensity = 20.0,
                    maxLightIntensity = 40.0
                )
                FlockType.LAYING_HENS -> OptimalRanges(
                    temperatureRange = 18.0..24.0,
                    humidityRange = 50.0..70.0,
                    maxCO2Level = 3000.0,
                    maxAmmoniaLevel = 25.0,
                    maxNoiseLevel = 65.0,
                    minLightIntensity = 10.0,
                    maxLightIntensity = 20.0
                )
                FlockType.BROILERS -> OptimalRanges(
                    temperatureRange = 20.0..26.0,
                    humidityRange = 50.0..70.0,
                    maxCO2Level = 3000.0,
                    maxAmmoniaLevel = 25.0,
                    maxNoiseLevel = 65.0,
                    minLightIntensity = 5.0,
                    maxLightIntensity = 20.0
                )
                else -> OptimalRanges(
                    temperatureRange = 18.0..26.0,
                    humidityRange = 50.0..70.0,
                    maxCO2Level = 3000.0,
                    maxAmmoniaLevel = 25.0,
                    maxNoiseLevel = 65.0,
                    minLightIntensity = 10.0,
                    maxLightIntensity = 30.0
                )
            }
        }
    }
}