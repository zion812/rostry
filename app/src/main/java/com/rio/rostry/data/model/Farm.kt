package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Core farm entity representing a complete farming operation
 */
@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val farmName: String,
    val location: String,
    val description: String = "",
    val farmType: FarmType = FarmType.SMALL_SCALE,
    val totalArea: Double = 0.0, // in hectares
    val establishedDate: Long = System.currentTimeMillis(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val certificationLevel: CertificationLevel = CertificationLevel.BASIC,
    val certificationDate: Long = 0,
    val certificationUrls: List<String> = emptyList(),
    val contactInfo: FarmContactInfo? = null,
    val facilities: List<FarmFacility> = emptyList(),
    val operatingLicense: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate total facility capacity
     */
    fun getTotalCapacity(): Int {
        return facilities.sumOf { it.capacity }
    }

    /**
     * Calculate current occupancy across all facilities
     */
    fun getCurrentOccupancy(): Int {
        return facilities.sumOf { it.currentOccupancy }
    }

    /**
     * Get occupancy rate as percentage
     */
    fun getOccupancyRate(): Double {
        val totalCapacity = getTotalCapacity()
        return if (totalCapacity > 0) {
            (getCurrentOccupancy().toDouble() / totalCapacity) * 100
        } else 0.0
    }

    /**
     * Check if farm needs maintenance based on facility conditions
     */
    fun needsMaintenance(): Boolean {
        return facilities.any { 
            it.condition in listOf(FacilityCondition.POOR, FacilityCondition.NEEDS_REPAIR) 
        }
    }

    /**
     * Get facilities that need attention
     */
    fun getFacilitiesNeedingAttention(): List<FarmFacility> {
        return facilities.filter { 
            it.condition in listOf(FacilityCondition.POOR, FacilityCondition.NEEDS_REPAIR) ||
            (it.lastMaintenance > 0 && System.currentTimeMillis() - it.lastMaintenance > 90 * 24 * 60 * 60 * 1000L) // 90 days
        }
    }

    /**
     * Calculate farm efficiency score
     */
    fun calculateEfficiencyScore(): Double {
        val occupancyScore = (getOccupancyRate() / 100) * 0.4
        val facilityScore = (facilities.count { it.condition in listOf(FacilityCondition.EXCELLENT, FacilityCondition.GOOD) }.toDouble() / facilities.size.coerceAtLeast(1)) * 0.3
        val certificationScore = when (certificationLevel) {
            CertificationLevel.EXPORT_QUALITY -> 1.0
            CertificationLevel.PREMIUM -> 0.8
            CertificationLevel.ORGANIC, CertificationLevel.FREE_RANGE -> 0.7
            CertificationLevel.BASIC -> 0.5
        } * 0.3

        return (occupancyScore + facilityScore + certificationScore).coerceIn(0.0, 1.0)
    }
}

enum class FarmType(val displayName: String, val description: String) {
    SMALL_SCALE("Small Scale Farm", "Family-owned farm with limited capacity"),
    COMMERCIAL("Commercial Farm", "Large-scale commercial operation"),
    BREEDING_FACILITY("Breeding Facility", "Specialized in breeding and genetics"),
    HATCHERY("Hatchery", "Focused on egg incubation and hatching"),
    INTEGRATED("Integrated Farm", "Complete farm-to-table operation")
}

enum class CertificationLevel(val displayName: String, val requirements: List<String>) {
    BASIC("Basic Certification", listOf("Basic health standards", "Record keeping")),
    ORGANIC("Organic Certified", listOf("Organic feed", "No antibiotics", "Free range access")),
    FREE_RANGE("Free Range Certified", listOf("Outdoor access", "Natural behavior", "Space requirements")),
    PREMIUM("Premium Certified", listOf("High welfare standards", "Quality feed", "Regular health checks")),
    EXPORT_QUALITY("Export Quality", listOf("International standards", "HACCP compliance", "Traceability"))
}

/**
 * Farm contact and location information
 */
data class FarmContactInfo(
    val primaryPhone: String = "",
    val secondaryPhone: String = "",
    val email: String = "",
    val address: String = "",
    val coordinates: FarmCoordinates? = null,
    val emergencyContact: String = "",
    val veterinarianContact: String = "",
    val businessRegistration: String = ""
) {
    /**
     * Validate contact information completeness
     */
    fun isComplete(): Boolean {
        return primaryPhone.isNotEmpty() && 
               email.isNotEmpty() && 
               address.isNotEmpty()
    }

    /**
     * Get formatted address for display
     */
    fun getFormattedAddress(): String {
        return if (coordinates != null) {
            "$address (${String.format("%.6f", coordinates.latitude)}, ${String.format("%.6f", coordinates.longitude)})"
        } else {
            address
        }
    }
}

/**
 * GPS coordinates for farm location
 */
data class FarmCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double = 0.0, // in meters
    val altitude: Double = 0.0, // in meters
    val recordedAt: Long = System.currentTimeMillis()
) {
    /**
     * Calculate distance to another coordinate point
     */
    fun distanceTo(other: FarmCoordinates): Double {
        val earthRadius = 6371000.0 // Earth radius in meters
        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(other.latitude)
        val deltaLatRad = Math.toRadians(other.latitude - latitude)
        val deltaLonRad = Math.toRadians(other.longitude - longitude)

        val a = kotlin.math.sin(deltaLatRad / 2) * kotlin.math.sin(deltaLatRad / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLonRad / 2) * kotlin.math.sin(deltaLonRad / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * Check if coordinates are valid
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }
}

/**
 * Individual farm facility (coops, storage, etc.)
 */
data class FarmFacility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FacilityType,
    val capacity: Int,
    val currentOccupancy: Int = 0,
    val condition: FacilityCondition = FacilityCondition.GOOD,
    val lastMaintenance: Long = 0,
    val nextMaintenanceDate: Long = 0,
    val maintenanceHistory: List<MaintenanceRecord> = emptyList(),
    val dimensions: FacilityDimensions? = null,
    val equipment: List<Equipment> = emptyList(),
    val notes: String = "",
    val imageUrls: List<String> = emptyList(),
    val isActive: Boolean = true
) {
    /**
     * Calculate occupancy percentage
     */
    fun getOccupancyPercentage(): Double {
        return if (capacity > 0) {
            (currentOccupancy.toDouble() / capacity) * 100
        } else 0.0
    }

    /**
     * Check if facility is overcrowded
     */
    fun isOvercrowded(): Boolean {
        return currentOccupancy > capacity
    }

    /**
     * Get available space
     */
    fun getAvailableSpace(): Int {
        return (capacity - currentOccupancy).coerceAtLeast(0)
    }

    /**
     * Check if maintenance is overdue
     */
    fun isMaintenanceOverdue(): Boolean {
        return nextMaintenanceDate > 0 && System.currentTimeMillis() > nextMaintenanceDate
    }

    /**
     * Calculate facility utilization score
     */
    fun getUtilizationScore(): Double {
        val occupancyScore = (getOccupancyPercentage() / 100).coerceIn(0.0, 1.0)
        val conditionScore = when (condition) {
            FacilityCondition.EXCELLENT -> 1.0
            FacilityCondition.GOOD -> 0.8
            FacilityCondition.FAIR -> 0.6
            FacilityCondition.POOR -> 0.4
            FacilityCondition.NEEDS_REPAIR -> 0.2
        }
        val maintenanceScore = if (isMaintenanceOverdue()) 0.5 else 1.0

        return (occupancyScore * 0.4 + conditionScore * 0.4 + maintenanceScore * 0.2)
    }
}

enum class FacilityType(val displayName: String, val icon: String, val recommendedCapacity: IntRange) {
    COOP("Chicken Coop", "🏠", 50..200),
    BROODER("Brooder House", "🐣", 100..500),
    BREEDING_PEN("Breeding Pen", "💕", 10..50),
    QUARANTINE("Quarantine Area", "🏥", 20..100),
    FEED_STORAGE("Feed Storage", "🌾", 0..0), // Capacity measured differently
    EQUIPMENT_SHED("Equipment Shed", "🔧", 0..0),
    PROCESSING_AREA("Processing Area", "⚙️", 0..0),
    OFFICE("Farm Office", "🏢", 0..0),
    VETERINARY_CLINIC("Veterinary Clinic", "🩺", 10..30)
}

enum class FacilityCondition(val displayName: String, val color: String, val priority: Int) {
    EXCELLENT("Excellent", "#4CAF50", 1),
    GOOD("Good", "#8BC34A", 2),
    FAIR("Fair", "#FFC107", 3),
    POOR("Poor", "#FF5722", 4),
    NEEDS_REPAIR("Needs Repair", "#F44336", 5)
}

/**
 * Facility dimensions and specifications
 */
data class FacilityDimensions(
    val length: Double, // in meters
    val width: Double,  // in meters
    val height: Double, // in meters
    val area: Double = length * width,
    val volume: Double = length * width * height,
    val unit: String = "meters"
) {
    /**
     * Calculate space per bird
     */
    fun getSpacePerBird(birdCount: Int): Double {
        return if (birdCount > 0) area / birdCount else 0.0
    }

    /**
     * Check if space meets minimum requirements
     */
    fun meetsSpaceRequirements(birdCount: Int, minSpacePerBird: Double): Boolean {
        return getSpacePerBird(birdCount) >= minSpacePerBird
    }
}

/**
 * Equipment within facilities
 */
data class Equipment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: EquipmentType,
    val brand: String = "",
    val model: String = "",
    val purchaseDate: Long = 0,
    val warrantyExpiry: Long = 0,
    val condition: FacilityCondition = FacilityCondition.GOOD,
    val lastService: Long = 0,
    val nextServiceDate: Long = 0,
    val serviceHistory: List<ServiceRecord> = emptyList(),
    val isOperational: Boolean = true,
    val notes: String = ""
)

enum class EquipmentType(val displayName: String) {
    FEEDER("Automatic Feeder"),
    WATERER("Water System"),
    VENTILATION("Ventilation Fan"),
    HEATING("Heating System"),
    LIGHTING("LED Lighting"),
    INCUBATOR("Incubator"),
    SCALE("Digital Scale"),
    GENERATOR("Backup Generator"),
    SECURITY_CAMERA("Security Camera"),
    TEMPERATURE_SENSOR("Temperature Sensor"),
    HUMIDITY_SENSOR("Humidity Sensor")
}

/**
 * Maintenance record for facilities and equipment
 */
data class MaintenanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val type: MaintenanceType,
    val description: String,
    val performedBy: String,
    val cost: Double = 0.0,
    val partsReplaced: List<String> = emptyList(),
    val beforeCondition: FacilityCondition,
    val afterCondition: FacilityCondition,
    val imageUrls: List<String> = emptyList(),
    val nextMaintenanceDate: Long = 0,
    val notes: String = ""
)

enum class MaintenanceType(val displayName: String) {
    ROUTINE("Routine Maintenance"),
    REPAIR("Repair"),
    UPGRADE("Upgrade"),
    INSPECTION("Inspection"),
    EMERGENCY("Emergency Repair"),
    CLEANING("Deep Cleaning"),
    CALIBRATION("Equipment Calibration")
}

/**
 * Service record for equipment
 */
data class ServiceRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val serviceType: ServiceType,
    val technician: String,
    val company: String = "",
    val description: String,
    val cost: Double = 0.0,
    val warranty: Int = 0, // days
    val certificateUrl: String = "",
    val notes: String = ""
)

enum class ServiceType(val displayName: String) {
    INSTALLATION("Installation"),
    REPAIR("Repair"),
    MAINTENANCE("Maintenance"),
    CALIBRATION("Calibration"),
    UPGRADE("Upgrade"),
    REPLACEMENT("Replacement")
}

/**
 * Farm performance metrics and analytics
 */
data class FarmMetrics(
    val farmId: String,
    val totalFowls: Int = 0,
    val activeFlocks: Int = 0,
    val breedingStock: Int = 0,
    val dailyEggProduction: Int = 0,
    val weeklyEggProduction: Int = 0,
    val monthlyEggProduction: Int = 0,
    val feedConsumption: Double = 0.0, // kg per day
    val feedConversionRatio: Double = 0.0,
    val mortalityRate: Double = 0.0,
    val averageWeight: Double = 0.0,
    val profitability: Double = 0.0,
    val efficiency: Double = 0.0,
    val lastCalculated: Long = System.currentTimeMillis()
) {
    /**
     * Calculate overall farm performance score
     */
    fun getPerformanceScore(): Double {
        val productionScore = if (totalFowls > 0) (dailyEggProduction.toDouble() / totalFowls) else 0.0
        val efficiencyScore = if (feedConversionRatio > 0) (1.0 / feedConversionRatio).coerceIn(0.0, 1.0) else 0.0
        val healthScore = (100 - mortalityRate) / 100
        val profitabilityScore = (profitability / 100).coerceIn(0.0, 1.0)

        return (productionScore * 0.3 + efficiencyScore * 0.25 + healthScore * 0.25 + profitabilityScore * 0.2)
    }

    /**
     * Get performance rating
     */
    fun getPerformanceRating(): PerformanceRating {
        val score = getPerformanceScore()
        return when {
            score >= 0.9 -> PerformanceRating.OUTSTANDING
            score >= 0.8 -> PerformanceRating.EXCELLENT
            score >= 0.7 -> PerformanceRating.GOOD
            score >= 0.6 -> PerformanceRating.AVERAGE
            else -> PerformanceRating.BELOW_AVERAGE
        }
    }
}