package com.rio.rostry.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId

@Entity(tableName = "fowl_records")
data class FowlRecord(
    @PrimaryKey
    @DocumentId
    val recordId: String = "",
    val fowlId: String = "",
    val recordType: String = "", // e.g., Vaccination, Weight Update, Health Check, Feeding, etc.
    val date: Long = System.currentTimeMillis(),
    val details: String = "",
    val proofImageUrl: String? = null,
    val weight: Double? = null,
    val temperature: Double? = null,
    val medication: String = "",
    val veterinarian: String = "",
    val cost: Double? = null,
    val notes: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class FowlRecordType {
    VACCINATION,
    WEIGHT_UPDATE,
    HEALTH_CHECK,
    FEEDING,
    BREEDING,
    TREATMENT,
    SALE,
    PURCHASE,
    DEATH,
    TRANSFER,
    OTHER
}

data class FowlRecordWithDetails(
    val record: FowlRecord,
    val fowlName: String = "",
    val fowlBreed: String = ""
)