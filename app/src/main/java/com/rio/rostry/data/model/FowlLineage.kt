package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Comprehensive fowl lineage and family tree tracking
 */
@Entity(tableName = "fowl_lineage")
data class FowlLineage(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val fowlId: String,
    val generation: Int = 1,
    val bloodlineId: String = "",
    val parentMaleId: String? = null,
    val parentFemaleId: String? = null,
    val grandparentIds: List<String> = emptyList(),
    val offspringIds: List<String> = emptyList(),
    val breedingHistory: List<BreedingRecord> = emptyList(),
    val lineageVerified: Boolean = false,
    val verificationDate: Long = 0,
    val verifiedBy: String = "",
    val geneticTraits: List<GeneticTrait> = emptyList(),
    val inbreedingCoefficient: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate total number of descendants
     */
    fun getTotalDescendants(): Int {
        return offspringIds.size
    }

    /**
     * Check if this fowl has breeding potential based on lineage
     */
    fun hasBreedingPotential(): Boolean {
        return generation <= 5 && inbreedingCoefficient < 0.25 && lineageVerified
    }

    /**
     * Get breeding compatibility score with another fowl
     */
    fun getCompatibilityScore(otherLineage: FowlLineage): BreedingCompatibility {
        val sharedAncestors = getSharedAncestors(otherLineage)
        val generationDiff = kotlin.math.abs(generation - otherLineage.generation)
        val combinedInbreeding = (inbreedingCoefficient + otherLineage.inbreedingCoefficient) / 2

        return when {
            sharedAncestors.isNotEmpty() && generationDiff < 2 -> BreedingCompatibility.POOR
            combinedInbreeding > 0.3 -> BreedingCompatibility.POOR
            generationDiff > 3 -> BreedingCompatibility.AVERAGE
            sharedAncestors.isEmpty() && combinedInbreeding < 0.1 -> BreedingCompatibility.EXCELLENT
            else -> BreedingCompatibility.GOOD
        }
    }

    /**
     * Find shared ancestors between two lineages
     */
    private fun getSharedAncestors(otherLineage: FowlLineage): List<String> {
        val thisAncestors = getAllAncestors()
        val otherAncestors = otherLineage.getAllAncestors()
        return thisAncestors.intersect(otherAncestors.toSet()).toList()
    }

    /**
     * Get all ancestors up to great-grandparents
     */
    private fun getAllAncestors(): List<String> {
        return listOfNotNull(parentMaleId, parentFemaleId) + grandparentIds
    }
}

enum class BreedingCompatibility(val displayName: String, val score: Int) {
    EXCELLENT("Excellent Match", 5),
    GOOD("Good Match", 4),
    AVERAGE("Average Match", 3),
    POOR("Poor Match", 2),
    INCOMPATIBLE("Not Recommended", 1)
}

/**
 * Individual breeding record with detailed tracking
 */
data class BreedingRecord(
    val id: String = UUID.randomUUID().toString(),
    val mateId: String,
    val breedingDate: Long,
    val breedingMethod: BreedingMethod = BreedingMethod.NATURAL,
    val offspringCount: Int = 0,
    val successfulOffspring: Int = 0,
    val successRate: Double = 0.0,
    val gestationPeriod: Int = 0, // Days
    val notes: String = "",
    val proofImageUrls: List<String> = emptyList(),
    val veterinarySupervision: Boolean = false,
    val breedingLocation: String = "",
    val environmentalConditions: BreedingConditions? = null,
    val offspringDetails: List<OffspringDetail> = emptyList()
) {
    /**
     * Calculate breeding success rate
     */
    fun calculateSuccessRate(): Double {
        return if (offspringCount > 0) {
            (successfulOffspring.toDouble() / offspringCount.toDouble()) * 100
        } else {
            0.0
        }
    }

    /**
     * Determine breeding quality rating
     */
    fun getBreedingQuality(): BreedingQuality {
        val rate = calculateSuccessRate()
        return when {
            rate >= 90 -> BreedingQuality.EXCEPTIONAL
            rate >= 80 -> BreedingQuality.EXCELLENT
            rate >= 70 -> BreedingQuality.GOOD
            rate >= 60 -> BreedingQuality.AVERAGE
            else -> BreedingQuality.POOR
        }
    }
}

enum class BreedingMethod(val displayName: String) {
    NATURAL("Natural Breeding"),
    ARTIFICIAL_INSEMINATION("Artificial Insemination"),
    CONTROLLED_MATING("Controlled Mating"),
    SELECTIVE_BREEDING("Selective Breeding")
}

data class BreedingConditions(
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val season: String = "",
    val nutritionLevel: NutritionLevel = NutritionLevel.STANDARD,
    val stressLevel: StressLevel = StressLevel.LOW
)

enum class NutritionLevel(val displayName: String) {
    POOR("Poor Nutrition"),
    STANDARD("Standard Nutrition"),
    ENHANCED("Enhanced Nutrition"),
    PREMIUM("Premium Nutrition")
}

enum class StressLevel(val displayName: String) {
    LOW("Low Stress"),
    MODERATE("Moderate Stress"),
    HIGH("High Stress"),
    SEVERE("Severe Stress")
}

enum class BreedingQuality(val displayName: String, val description: String) {
    EXCEPTIONAL("Exceptional", "90%+ success rate"),
    EXCELLENT("Excellent", "80-89% success rate"),
    GOOD("Good", "70-79% success rate"),
    AVERAGE("Average", "60-69% success rate"),
    POOR("Poor", "Below 60% success rate")
}

data class OffspringDetail(
    val fowlId: String,
    val birthWeight: Double = 0.0,
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val geneticMarkers: List<String> = emptyList(),
    val survivalToAdulthood: Boolean = false
)

enum class HealthStatus(val displayName: String) {
    HEALTHY("Healthy"),
    MINOR_ISSUES("Minor Health Issues"),
    MAJOR_ISSUES("Major Health Issues"),
    DECEASED("Deceased")
}

/**
 * Bloodline tracking for genetic lineage management
 */
@Entity(tableName = "bloodlines")
data class Bloodline(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val originFowlId: String,
    val founderGeneration: Int = 1,
    val characteristics: List<String> = emptyList(),
    val totalGenerations: Int = 1,
    val activeBreeders: Int = 0,
    val totalOffspring: Int = 0,
    val performanceMetrics: BloodlineMetrics? = null,
    val geneticDiversity: Double = 1.0,
    val breedingGoals: List<String> = emptyList(),
    val certificationLevel: CertificationLevel = CertificationLevel.UNVERIFIED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate bloodline strength based on various factors
     */
    fun calculateBloodlineStrength(): BloodlineStrength {
        val metrics = performanceMetrics ?: return BloodlineStrength.UNKNOWN
        
        val avgScore = (
            (metrics.survivalRate / 100) * 0.3 +
            (metrics.breedingSuccessRate / 100) * 0.3 +
            (geneticDiversity) * 0.2 +
            (if (totalGenerations >= 3) 0.2 else 0.1)
        )

        return when {
            avgScore >= 0.9 -> BloodlineStrength.EXCEPTIONAL
            avgScore >= 0.8 -> BloodlineStrength.STRONG
            avgScore >= 0.7 -> BloodlineStrength.GOOD
            avgScore >= 0.6 -> BloodlineStrength.AVERAGE
            else -> BloodlineStrength.WEAK
        }
    }

    /**
     * Check if bloodline needs genetic diversity improvement
     */
    fun needsGeneticDiversification(): Boolean {
        return geneticDiversity < 0.7 || totalGenerations > 6
    }
}

enum class BloodlineStrength(val displayName: String, val description: String) {
    EXCEPTIONAL("Exceptional", "Outstanding genetic line with proven performance"),
    STRONG("Strong", "Reliable genetic line with good performance"),
    GOOD("Good", "Solid genetic line with average performance"),
    AVERAGE("Average", "Standard genetic line"),
    WEAK("Weak", "Genetic line needs improvement"),
    UNKNOWN("Unknown", "Insufficient data for assessment")
}

enum class CertificationLevel(val displayName: String) {
    UNVERIFIED("Unverified"),
    BASIC("Basic Certification"),
    ADVANCED("Advanced Certification"),
    PREMIUM("Premium Certification"),
    CHAMPION("Champion Bloodline")
}

/**
 * Comprehensive bloodline performance metrics
 */
data class BloodlineMetrics(
    val averageWeight: Double = 0.0,
    val averageHeight: Double = 0.0,
    val averageLifespan: Double = 0.0,
    val survivalRate: Double = 0.0,
    val breedingSuccessRate: Double = 0.0,
    val eggProductionRate: Double = 0.0,
    val diseaseResistance: Double = 0.0,
    val commonTraits: List<String> = emptyList(),
    val strengthTraits: List<String> = emptyList(),
    val weaknessTraits: List<String> = emptyList(),
    val marketValue: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Calculate overall bloodline score
     */
    fun calculateOverallScore(): Double {
        return (survivalRate * 0.25 + 
                breedingSuccessRate * 0.25 + 
                diseaseResistance * 0.2 + 
                eggProductionRate * 0.15 + 
                (marketValue / 1000) * 0.15) / 100
    }

    /**
     * Get performance rating
     */
    fun getPerformanceRating(): PerformanceRating {
        val score = calculateOverallScore()
        return when {
            score >= 0.9 -> PerformanceRating.OUTSTANDING
            score >= 0.8 -> PerformanceRating.EXCELLENT
            score >= 0.7 -> PerformanceRating.GOOD
            score >= 0.6 -> PerformanceRating.AVERAGE
            else -> PerformanceRating.BELOW_AVERAGE
        }
    }
}

enum class PerformanceRating(val displayName: String, val color: String) {
    OUTSTANDING("Outstanding", "#4CAF50"),
    EXCELLENT("Excellent", "#8BC34A"),
    GOOD("Good", "#CDDC39"),
    AVERAGE("Average", "#FFC107"),
    BELOW_AVERAGE("Below Average", "#FF5722")
}

/**
 * Genetic trait tracking for selective breeding
 */
data class GeneticTrait(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: TraitCategory,
    val dominance: TraitDominance,
    val expression: TraitExpression,
    val heritability: Double = 0.5, // 0.0 to 1.0
    val desirability: TraitDesirability = TraitDesirability.NEUTRAL,
    val description: String = ""
)

enum class TraitCategory(val displayName: String) {
    PHYSICAL("Physical Traits"),
    BEHAVIORAL("Behavioral Traits"),
    PRODUCTION("Production Traits"),
    HEALTH("Health Traits"),
    ADAPTATION("Adaptation Traits")
}

enum class TraitDominance(val displayName: String) {
    DOMINANT("Dominant"),
    RECESSIVE("Recessive"),
    CODOMINANT("Co-dominant"),
    INCOMPLETE_DOMINANT("Incomplete Dominant")
}

enum class TraitExpression(val displayName: String) {
    STRONG("Strong Expression"),
    MODERATE("Moderate Expression"),
    WEAK("Weak Expression"),
    VARIABLE("Variable Expression")
}

enum class TraitDesirability(val displayName: String) {
    HIGHLY_DESIRABLE("Highly Desirable"),
    DESIRABLE("Desirable"),
    NEUTRAL("Neutral"),
    UNDESIRABLE("Undesirable"),
    HIGHLY_UNDESIRABLE("Highly Undesirable")
}