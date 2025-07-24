package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "fowls")
data class Fowl(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val breed: String = "",
    val type: FowlType = FowlType.CHICKEN,
    val gender: FowlGender = FowlGender.UNKNOWN,
    val dateOfBirth: Long? = null,
    val motherId: String? = null,
    val fatherId: String? = null,
    val dateOfHatching: Long = 0,
    val initialCount: Int? = null,
    val status: String = "Growing", // e.g., Growing, Breeder Ready, For Sale, Sold
    val weight: Double = 0.0,
    val color: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val proofImageUrl: String? = null,
    val healthRecords: List<HealthRecord> = emptyList(),
    val isForSale: Boolean = false,
    val price: Double = 0.0,
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class FowlType {
    CHICKEN,
    DUCK,
    TURKEY,
    GOOSE,
    GUINEA_FOWL,
    OTHER
}

enum class FowlGender {
    MALE,
    FEMALE,
    UNKNOWN
}

data class HealthRecord(
    val id: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: HealthRecordType = HealthRecordType.CHECKUP,
    val description: String = "",
    val veterinarian: String = "",
    val medication: String = "",
    val notes: String = ""
)

enum class HealthRecordType {
    CHECKUP,
    VACCINATION,
    TREATMENT,
    WEIGHT_CHECK,
    OTHER
}