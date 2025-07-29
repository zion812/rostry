package com.rio.rostry.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.rio.rostry.data.local.dao.FlockSummaryDao
import com.rio.rostry.data.local.dao.FowlDao
import com.rio.rostry.data.model.FlockSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlDao: FowlDao,
    private val flockSummaryDao: FlockSummaryDao,
    private val fowlRepository: FowlRepository
) {
    
    fun getFlockSummaryFlow(ownerId: String): Flow<FlockSummary?> {
        return flockSummaryDao.getFlockSummaryFlow(ownerId)
    }
    
    suspend fun getFlockSummary(ownerId: String): FlockSummary? {
        return flockSummaryDao.getFlockSummary(ownerId)
    }
    
    suspend fun refreshFlockSummary(ownerId: String): FlockSummary {
        val fowls = fowlRepository.getFowlsByOwner(ownerId)
        
        val summary = FlockSummary(
            ownerId = ownerId,
            totalFowls = fowls.size,
            chicks = fowls.count { fowl -> fowl.status == "Growing" },
            juveniles = fowls.count { fowl -> fowl.status == "Juvenile" },
            adults = fowls.count { fowl -> fowl.status == "Adult" },
            breeders = fowls.count { fowl -> fowl.status == "Breeder Ready" },
            forSale = fowls.count { fowl -> fowl.isForSale },
            sold = fowls.count { fowl -> fowl.status == "Sold" },
            deceased = fowls.count { fowl -> fowl.status == "Deceased" },
            totalValue = fowls.filter { fowl -> fowl.isForSale }.sumOf { fowl -> fowl.price },
            lastUpdated = System.currentTimeMillis()
        )
        
        flockSummaryDao.insertFlockSummary(summary)
        return summary
    }
}