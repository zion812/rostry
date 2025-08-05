package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val id: String = "",
    val fowlId: String = "",
    val fowlName: String = "",
    val fowlBreed: String = "", // Add missing fowlBreed property
    val fowlImageUrl: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)