package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.LifecycleDao
import com.rio.rostry.data.local.dao.LineageDao
import com.rio.rostry.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for comprehensive fowl lifecycle management
 * Handles lifecycle tracking, lineage management, and breeding analytics
 */
@Singleton
class LifecycleRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val lifecycleDao: LifecycleDao,
    private val lineageDao: LineageDao
) {

    // ==================== LIFECYCLE MANAGEMENT ====================

    /**
     * Create a new lifecycle record for a fowl
     */
    suspend fun createLifecycleRecord(
        fowlId: String,
        parentMaleId: String? = null,
        parentFemaleId: String? = null,
        batchId: String? = null,
        initialStage: LifecycleStage = LifecycleStage.EGG
    ): Result<String> {
        return try {
            val lifecycle = FowlLifecycle(
                fowlId = fowlId,
                currentStage = initialStage,
                parentMaleId = parentMaleId,
                parentFemaleId = parentFemaleId,
                batchId = batchId,
                expectedNextStageDate = calculateExpectedStageDate(initialStage)
            )

            // Save to Firestore
            firestore.collection("fowl_lifecycles")
                .document(lifecycle.id)
                .set(lifecycle)
                .await()

            // Save locally
            lifecycleDao.insertLifecycle(lifecycle)

            // Create lineage record if parents are specified
            if (parentMaleId != null || parentFemaleId != null) {
                createLineageRecord(fowlId, parentMaleId, parentFemaleId)
            }

            Result.success(lifecycle.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update lifecycle stage with milestone tracking
     */
    suspend fun updateLifecycleStage(
        fowlId: String,
        newStage: LifecycleStage,
        milestone: LifecycleMilestone,
        proofImageUrls: List<String> = emptyList()
    ): Result<Unit> {
        return try {
            val lifecycle = lifecycleDao.getLifecycleByFowlId(fowlId)
                ?: return Result.failure(Exception("Lifecycle not found"))

            val updatedMilestone = milestone.copy(
                proofImageUrls = proofImageUrls,
                achievedDate = System.currentTimeMillis()
            )

            val updatedLifecycle = lifecycle.copy(
                currentStage = newStage,
                stageStartDate = System.currentTimeMillis(),
                expectedNextStageDate = calculateExpectedStageDate(newStage),
                milestones = lifecycle.milestones + updatedMilestone,
                updatedAt = System.currentTimeMillis()
            )

            // Update Firestore
            firestore.collection("fowl_lifecycles")
                .document(lifecycle.id)
                .set(updatedLifecycle)
                .await()

            // Update locally
            lifecycleDao.updateLifecycle(updatedLifecycle)

            // Update breeding candidate status if reached adult stage
            if (newStage == LifecycleStage.ADULT) {
                updateBreederCandidateStatus(fowlId, true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add growth metric with image proof
     */
    suspend fun addGrowthMetric(
        fowlId: String,
        metric: GrowthMetric,
        imageUri: String? = null
    ): Result<Unit> {
        return try {
            val lifecycle = lifecycleDao.getLifecycleByFowlId(fowlId)
                ?: return Result.failure(Exception("Lifecycle not found"))

            // Upload proof image if provided
            val proofImageUrl = imageUri?.let { uri ->
                uploadProofImage(fowlId, uri, "growth_${metric.id}")
            } ?: ""

            val updatedMetric = metric.copy(
                proofImageUrl = proofImageUrl,
                date = System.currentTimeMillis()
            )

            val updatedLifecycle = lifecycle.copy(
                growthMetrics = lifecycle.growthMetrics + updatedMetric,
                updatedAt = System.currentTimeMillis()
            )

            // Update both Firestore and local
            firestore.collection("fowl_lifecycles")
                .document(lifecycle.id)
                .set(updatedLifecycle)
                .await()

            lifecycleDao.updateLifecycle(updatedLifecycle)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get lifecycle with real-time updates
     */
    fun getLifecycleFlow(fowlId: String): Flow<FowlLifecycle?> {
        return lifecycleDao.getLifecycleByFowlIdFlow(fowlId)
    }

    /**
     * Get fowls ready for stage transition
     */
    fun getFowlsReadyForTransition(): Flow<List<FowlLifecycle>> {
        return lifecycleDao.getFowlsReadyForTransition()
    }

    /**
     * Get lifecycle analytics data
     */
    fun getLifecycleAnalytics(): Flow<LifecycleAnalytics> {
        return combine(
            lifecycleDao.getAllLifecycles(),
            lineageDao.getAllBloodlines()
        ) { lifecycles, bloodlines ->
            calculateLifecycleAnalytics(lifecycles, bloodlines)
        }
    }

    // ==================== LINEAGE MANAGEMENT ====================

    /**
     * Create lineage record with parent tracking
     */
    private suspend fun createLineageRecord(
        fowlId: String,
        parentMaleId: String?,
        parentFemaleId: String?
    ) {
        val generation = calculateGeneration(parentMaleId, parentFemaleId)
        val bloodlineId = determineBloodline(parentMaleId, parentFemaleId)

        val lineage = FowlLineage(
            fowlId = fowlId,
            generation = generation,
            bloodlineId = bloodlineId,
            parentMaleId = parentMaleId,
            parentFemaleId = parentFemaleId,
            inbreedingCoefficient = calculateInbreedingCoefficient(parentMaleId, parentFemaleId)
        )

        firestore.collection("fowl_lineages")
            .document(lineage.id)
            .set(lineage)
            .await()

        lineageDao.insertLineage(lineage)

        // Update parent records to include this offspring
        updateParentOffspring(parentMaleId, fowlId)
        updateParentOffspring(parentFemaleId, fowlId)
    }

    /**
     * Get family tree data
     */
    fun getFamilyTree(fowlId: String): Flow<FamilyTreeData> {
        return lineageDao.getLineageByFowlIdFlow(fowlId).map { lineage ->
            if (lineage != null) {
                // Simplified family tree - just return current fowl for now
                FamilyTreeData(
                    currentFowl = lineage,
                    ancestors = emptyList(), // Simplified
                    descendants = emptyList() // Simplified
                )
            } else {
                FamilyTreeData()
            }
        }
    }

    /**
     * Get breeding recommendations
     */
    suspend fun getBreedingRecommendations(fowlId: String): Result<List<BreedingRecommendation>> {
        return try {
            val currentLineage = lineageDao.getLineageByFowlId(fowlId)
                ?: return Result.failure(Exception("Lineage not found"))

            // Simplified breeding recommendations - get all lineages and calculate compatibility
            val allLineages = lineageDao.getAllLineagesList()
            val recommendations = allLineages
                .filter { lineage -> lineage.fowlId != fowlId } // Exclude self
                .map { mateLineage ->
                    val compatibilityScore = calculateCompatibilityScore(currentLineage, mateLineage)
                    
                    BreedingRecommendation(
                        mateId = mateLineage.fowlId,
                        compatibilityScore = compatibilityScore,
                        compatibility = determineCompatibility(compatibilityScore),
                        expectedOffspringTraits = predictOffspringTraits(currentLineage, mateLineage),
                        riskFactors = assessBreedingRisks(currentLineage, mateLineage)
                    )
                }
                .sortedByDescending { recommendation -> recommendation.compatibilityScore }
                .take(10) // Top 10 recommendations

            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateCompatibilityScore(lineage1: FowlLineage, lineage2: FowlLineage): Double {
        var score = 1.0
        
        // Reduce score for same bloodline
        if (lineage1.bloodlineId == lineage2.bloodlineId) {
            score -= 0.3
        }
        
        // Reduce score for high inbreeding
        val avgInbreeding = (lineage1.inbreedingCoefficient + lineage2.inbreedingCoefficient) / 2
        score -= avgInbreeding
        
        // Reduce score for close relatives
        if (lineage1.parentMaleId == lineage2.parentMaleId || 
            lineage1.parentFemaleId == lineage2.parentFemaleId) {
            score -= 0.4
        }
        
        return maxOf(0.0, score)
    }

    // ==================== BLOODLINE MANAGEMENT ====================

    /**
     * Create new bloodline
     */
    suspend fun createBloodline(
        name: String,
        originFowlId: String,
        characteristics: List<String>,
        breedingGoals: List<String>
    ): Result<String> {
        return try {
            val bloodline = Bloodline(
                name = name,
                originFowlId = originFowlId,
                characteristics = characteristics,
                breedingGoals = breedingGoals
            )

            firestore.collection("bloodlines")
                .document(bloodline.id)
                .set(bloodline)
                .await()

            lineageDao.insertBloodline(bloodline)

            Result.success(bloodline.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get bloodline performance analytics
     */
    fun getBloodlineAnalytics(): Flow<List<BloodlineAnalytics>> {
        return lineageDao.getAllBloodlines().map { bloodlines ->
            bloodlines.map { bloodline ->
                BloodlineAnalytics(
                    bloodline = bloodline,
                    strength = bloodline.calculateBloodlineStrength(),
                    needsDiversification = bloodline.needsGeneticDiversification(),
                    performanceRating = bloodline.performanceMetrics?.getPerformanceRating() 
                        ?: PerformanceRating.BELOW_AVERAGE
                )
            }
        }
    }

    // ==================== HELPER METHODS ====================

    private fun calculateExpectedStageDate(stage: LifecycleStage): Long {
        val currentTime = System.currentTimeMillis()
        val durationMs = stage.durationWeeks * 7 * 24 * 60 * 60 * 1000L
        return if (stage.durationWeeks > 0) currentTime + durationMs else 0
    }

    private suspend fun calculateGeneration(parentMaleId: String?, parentFemaleId: String?): Int {
        val parentGenerations = listOfNotNull(
            parentMaleId?.let { lineageDao.getLineageByFowlId(it)?.generation },
            parentFemaleId?.let { lineageDao.getLineageByFowlId(it)?.generation }
        )

        return if (parentGenerations.isNotEmpty()) {
            parentGenerations.maxOrNull()!! + 1
        } else {
            1
        }
    }

    private suspend fun determineBloodline(parentMaleId: String?, parentFemaleId: String?): String {
        // Logic to determine bloodline based on parents
        // For now, use the male parent's bloodline or create new one
        return parentMaleId?.let { 
            lineageDao.getLineageByFowlId(it)?.bloodlineId 
        } ?: UUID.randomUUID().toString()
    }

    private suspend fun calculateInbreedingCoefficient(
        parentMaleId: String?, 
        parentFemaleId: String?
    ): Double {
        if (parentMaleId == null || parentFemaleId == null) return 0.0
        
        val maleLineage = lineageDao.getLineageByFowlId(parentMaleId)
        val femaleLineage = lineageDao.getLineageByFowlId(parentFemaleId)
        
        // Simplified inbreeding calculation
        return when {
            maleLineage?.bloodlineId == femaleLineage?.bloodlineId -> 0.25
            maleLineage?.parentMaleId == femaleLineage?.parentMaleId ||
            maleLineage?.parentFemaleId == femaleLineage?.parentFemaleId -> 0.125
            else -> 0.0
        }
    }

    private suspend fun updateParentOffspring(parentId: String?, offspringId: String) {
        parentId?.let { id ->
            val parentLineage = lineageDao.getLineageByFowlId(id)
            parentLineage?.let { lineage ->
                val updatedLineage = lineage.copy(
                    offspringIds = lineage.offspringIds + offspringId,
                    updatedAt = System.currentTimeMillis()
                )
                lineageDao.updateLineage(updatedLineage)
            }
        }
    }

    private suspend fun updateBreederCandidateStatus(fowlId: String, isCandidate: Boolean) {
        val lifecycle = lifecycleDao.getLifecycleByFowlId(fowlId)
        lifecycle?.let {
            val updated = it.copy(
                isBreederCandidate = isCandidate,
                updatedAt = System.currentTimeMillis()
            )
            lifecycleDao.updateLifecycle(updated)
        }
    }

    private suspend fun uploadProofImage(fowlId: String, imageUri: String, filename: String): String {
        return try {
            val imageRef = storage.reference
                .child("lifecycle_proofs")
                .child(fowlId)
                .child("$filename.jpg")
            
            // In a real implementation, you would upload the actual image
            // For now, return a placeholder URL
            "https://storage.googleapis.com/lifecycle_proofs/$fowlId/$filename.jpg"
        } catch (e: Exception) {
            ""
        }
    }

    private fun calculateLifecycleAnalytics(
        lifecycles: List<FowlLifecycle>,
        bloodlines: List<Bloodline>
    ): LifecycleAnalytics {
        val stageDistribution = lifecycles.groupBy { it.currentStage }
            .mapValues { it.value.size }
        
        val totalFowls = lifecycles.size
        val activeBreeders = lifecycles.count { 
            it.currentStage in listOf(LifecycleStage.ADULT, LifecycleStage.BREEDER_ACTIVE) 
        }
        
        val averageGrowthRate = lifecycles.mapNotNull { lifecycle ->
            lifecycle.growthMetrics.takeIf { it.size >= 2 }?.let { metrics ->
                val sorted = metrics.sortedBy { it.date }
                val latest = sorted.last()
                val previous = sorted[sorted.size - 2]
                latest.calculateGrowthRate(previous)
            }
        }.average().takeIf { !it.isNaN() } ?: 0.0

        val survivalRate = if (totalFowls > 0) {
            (lifecycles.count { it.currentStage != LifecycleStage.EGG } / totalFowls.toDouble()) * 100
        } else 0.0

        return LifecycleAnalytics(
            totalFowls = totalFowls,
            activeBreeders = activeBreeders,
            stageDistribution = stageDistribution,
            averageGrowthRate = averageGrowthRate,
            survivalRate = survivalRate,
            topPerformingBloodlines = bloodlines.sortedByDescending { 
                it.performanceMetrics?.calculateOverallScore() ?: 0.0 
            }.take(5)
        )
    }

    private fun determineCompatibility(score: Double): BreedingCompatibility {
        return when {
            score >= 0.9 -> BreedingCompatibility.EXCELLENT
            score >= 0.8 -> BreedingCompatibility.GOOD
            score >= 0.7 -> BreedingCompatibility.AVERAGE
            score >= 0.6 -> BreedingCompatibility.POOR
            else -> BreedingCompatibility.INCOMPATIBLE
        }
    }

    private fun predictOffspringTraits(
        parent1: FowlLineage?,
        parent2: FowlLineage?
    ): List<String> {
        // Simplified trait prediction
        return listOf("Strong genetics", "Good growth potential", "Disease resistance")
    }

    private fun assessBreedingRisks(
        parent1: FowlLineage?,
        parent2: FowlLineage?
    ): List<String> {
        val risks = mutableListOf<String>()
        
        if (parent1?.inbreedingCoefficient ?: 0.0 > 0.2) {
            risks.add("High inbreeding coefficient")
        }
        
        if (parent1?.bloodlineId == parent2?.bloodlineId) {
            risks.add("Same bloodline - genetic diversity concern")
        }
        
        return risks
    /**
     * Get all lifecycles
     */
    fun getAllLifecycles(): Flow<List<FowlLifecycle>> {
        return lifecycleDao.getAllLifecycles()
    }

}

// ==================== DATA CLASSES ====================

data class FamilyTreeData(
    val currentFowl: FowlLineage? = null,
    val ancestors: List<FowlLineage> = emptyList(),
    val descendants: List<FowlLineage> = emptyList()
)

data class BreedingRecommendation(
    val mateId: String,
    val compatibilityScore: Double,
    val compatibility: BreedingCompatibility,
    val expectedOffspringTraits: List<String>,
    val riskFactors: List<String>
)

data class LifecycleAnalytics(
    val totalFowls: Int,
    val activeBreeders: Int,
    val stageDistribution: Map<LifecycleStage, Int>,
    val averageGrowthRate: Double,
    val survivalRate: Double,
    val topPerformingBloodlines: List<Bloodline>
)

data class BloodlineAnalytics(
    val bloodline: Bloodline,
    val strength: BloodlineStrength,
    val needsDiversification: Boolean,
    val performanceRating: PerformanceRating
)