package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flock_summary")
data class FlockSummary(
    @PrimaryKey
    val ownerId: String = "",
    val totalFowls: Int = 0,
    val chicks: Int = 0,
    val juveniles: Int = 0,
    val adults: Int = 0,
    val breeders: Int = 0,
    val forSale: Int = 0,
    val sold: Int = 0,
    val deceased: Int = 0,
    val totalValue: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)