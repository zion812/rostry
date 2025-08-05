package com.rio.rostry.data.model

// Room-compatible data classes for vaccination queries

data class VaccineTypeCount(
    val vaccineType: String, // Store as String, convert to enum in repository
    val count: Int
)

data class AdministratorCount(
    val administeredBy: String,
    val count: Int
)

data class VaccinationStatsResult(
    val vaccineType: String,
    val averageEfficacy: Double,
    val totalCount: Int,
    val lastAdministered: Long
)

data class FlockVaccinationSummary(
    val flockId: String,
    val totalVaccinations: Int,
    val uniqueVaccineTypes: Int,
    val averageEfficacy: Double,
    val lastVaccinationDate: Long,
    val overdueCount: Int
)

data class ManufacturerCount(
    val manufacturer: String,
    val count: Int
)

data class VaccinationEfficiencyMetrics(
    val flockId: String,
    val vaccineType: String,
    val totalAdministered: Int,
    val averageEfficacy: Double,
    val costPerDose: Double,
    val totalCost: Double
)