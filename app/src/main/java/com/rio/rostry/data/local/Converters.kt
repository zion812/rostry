package com.rio.rostry.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rio.rostry.data.model.*

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromHealthRecordList(value: List<HealthRecord>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toHealthRecordList(value: String): List<HealthRecord> {
        return Gson().fromJson(value, object : TypeToken<List<HealthRecord>>() {}.type)
    }

    @TypeConverter
    fun fromCommentList(value: List<Comment>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCommentList(value: String): List<Comment> {
        return Gson().fromJson(value, object : TypeToken<List<Comment>>() {}.type)
    }

    @TypeConverter
    fun fromIntMap(value: Map<String, Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntMap(value: String): Map<String, Int> {
        return Gson().fromJson(value, object : TypeToken<Map<String, Int>>() {}.type)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return Gson().fromJson(value, object : TypeToken<Map<String, String>>() {}.type)
    }

    // Location converter
    @TypeConverter
    fun fromLocation(value: Location?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toLocation(value: String?): Location? {
        return if (value == null) null else Gson().fromJson(value, Location::class.java)
    }

    // Order enums
    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String {
        return value.name
    }

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus {
        return OrderStatus.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }

    // Wallet enums
    @TypeConverter
    fun fromCoinTransactionType(value: CoinTransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toCoinTransactionType(value: String): CoinTransactionType {
        return CoinTransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromShowcaseCategory(value: ShowcaseCategory): String {
        return value.name
    }

    @TypeConverter
    fun toShowcaseCategory(value: String): ShowcaseCategory {
        return ShowcaseCategory.valueOf(value)
    }

    @TypeConverter
    fun fromShowcaseDuration(value: ShowcaseDuration): String {
        return value.name
    }

    @TypeConverter
    fun toShowcaseDuration(value: String): ShowcaseDuration {
        return ShowcaseDuration.valueOf(value)
    }

    // Verification enums
    @TypeConverter
    fun fromVerificationType(value: VerificationType): String {
        return value.name
    }

    @TypeConverter
    fun toVerificationType(value: String): VerificationType {
        return VerificationType.valueOf(value)
    }

    @TypeConverter
    fun fromVerificationStatus(value: VerificationStatus): String {
        return value.name
    }

    @TypeConverter
    fun toVerificationStatus(value: String): VerificationStatus {
        return VerificationStatus.valueOf(value)
    }

    // Fowl enums
    @TypeConverter
    fun fromFowlType(value: FowlType): String {
        return value.name
    }

    @TypeConverter
    fun toFowlType(value: String): FowlType {
        return FowlType.valueOf(value)
    }

    @TypeConverter
    fun fromFowlGender(value: FowlGender): String {
        return value.name
    }

    @TypeConverter
    fun toFowlGender(value: String): FowlGender {
        return FowlGender.valueOf(value)
    }

    @TypeConverter
    fun fromHealthRecordType(value: HealthRecordType): String {
        return value.name
    }

    @TypeConverter
    fun toHealthRecordType(value: String): HealthRecordType {
        return HealthRecordType.valueOf(value)
    }

    // User enums
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }

    // Chat enums
    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }

    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }

    // FowlRecord enums
    @TypeConverter
    fun fromFowlRecordType(value: FowlRecordType): String {
        return value.name
    }

    @TypeConverter
    fun toFowlRecordType(value: String): FowlRecordType {
        return FowlRecordType.valueOf(value)
    }

    // Transfer enums
    @TypeConverter
    fun fromTransferStatus(value: TransferStatus): String {
        return value.name
    }

    @TypeConverter
    fun toTransferStatus(value: String): TransferStatus {
        return TransferStatus.valueOf(value)
    }
}