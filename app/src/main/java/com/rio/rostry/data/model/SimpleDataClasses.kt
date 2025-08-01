package com.rio.rostry.data.model

/**
 * Simple data classes for Room queries that can't handle complex Map<Enum, Type> returns
 */

data class FlockTypeCount(
    val flockType: String,
    val count: Int
)

data class FlockHealthCount(
    val healthStatus: String,
    val count: Int
)

data class FlockSizeByType(
    val flockType: String,
    val avgSize: Double
)

data class LifecycleStageCount(
    val currentStage: String,
    val count: Int
)

data class GrowthMetrics(
    val avgWeight: Double,
    val avgHeight: Double
)

data class BreedingMetrics(
    val totalBreeders: Int,
    val activeBreederRate: Double,
    val breedingCandidates: Int
)

data class BatchCompletionData(
    val batchId: String,
    val total: Int,
    val completed: Int
)

data class LineageStatistics(
    val totalLineages: Int,
    val verifiedCount: Int,
    val avgGeneration: Double,
    val maxGeneration: Int,
    val avgInbreeding: Double
)

data class FarmAccessStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val pendingUsers: Int,
    val owners: Int,
    val managers: Int,
    val workers: Int,
    val recentlyActive: Int
)

data class RoleCount(
    val role: String,
    val count: Int
)

data class InvitationStatistics(
    val totalInvitations: Int,
    val pendingInvitations: Int,
    val acceptedInvitations: Int,
    val rejectedInvitations: Int,
    val expiredInvitations: Int,
    val avgResponseTime: Double
)

data class EventCount(
    val event: String,
    val count: Int
)

data class InvitationDashboardSummary(
    val totalInvitations: Int,
    val activeInvitations: Int,
    val acceptedInvitations: Int,
    val recentInvitations: Int
)