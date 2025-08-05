package com.rio.rostry.data.model

data class GeneticMarkerCount(
    val geneticMarkers: String,
    val count: Int
)

data class BloodlinePerformanceResult(
    val id: String,
    val name: String,
    val geneticMarkers: String,
    val totalBreedings: Int,
    val averageSuccessRate: Double,
    val lastBreedingDate: Long
)

data class BreedingSuccessMetrics(
    val bloodlineId: String,
    val bloodlineName: String,
    val totalOffspring: Int,
    val successfulOffspring: Int,
    val successRate: Double,
    val geneticDiversity: Double
)

data class CertificationLevelCount(
    val certificationLevel: String,
    val count: Int
)

data class GenerationGroupCount(
    val generationGroup: String,
    val count: Int
)

data class DiversityLevelCount(
    val diversityLevel: String,
    val count: Int
)

data class BloodlineAnalytics(
    val totalBloodlines: Int,
    val activeBloodlines: Int,
    val averageGeneticDiversity: Double,
    val maxGenerations: Int,
    val certifiedCount: Int
)