package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlRepository: FowlRepository,
    private val dashboardRepository: DashboardRepository
) {
    
    suspend fun generateFlockReport(ownerId: String): Map<String, Any> {
        return try {
            val summary = dashboardRepository.getFlockSummary(ownerId)
            val fowls = fowlRepository.getFowlsByOwner(ownerId)
            
            mapOf(
                "ownerId" to ownerId,
                "totalFowls" to fowls.size,
                "summary" to (summary ?: emptyMap<String, Any>()),
                "fowls" to fowls,
                "generatedAt" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            mapOf(
                "error" to "Failed to generate report",
                "message" to (e.message ?: "Unknown error")
            )
        }
    }
    
    suspend fun generateSalesReport(ownerId: String, startDate: Long, endDate: Long): Map<String, Any> {
        return try {
            val fowls = fowlRepository.getFowlsByOwner(ownerId)
            val soldFowls = fowls.filter { fowl -> 
                fowl.status == "Sold" && 
                fowl.updatedAt >= startDate && 
                fowl.updatedAt <= endDate 
            }
            
            mapOf(
                "ownerId" to ownerId,
                "period" to mapOf(
                    "start" to startDate,
                    "end" to endDate
                ),
                "totalSales" to soldFowls.size,
                "totalRevenue" to soldFowls.sumOf { fowl -> fowl.price },
                "soldFowls" to soldFowls,
                "generatedAt" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            mapOf(
                "error" to "Failed to generate sales report",
                "message" to (e.message ?: "Unknown error")
            )
        }
    }
    
    suspend fun saveReport(report: Map<String, Any>) {
        try {
            firestore.collection("reports")
                .add(report)
                .await()
        } catch (e: Exception) {
            // Handle error silently for reports
        }
    }
}