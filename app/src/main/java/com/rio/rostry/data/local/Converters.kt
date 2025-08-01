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

    // Farm Management Type Converters
    @TypeConverter
    fun fromFarmType(value: FarmType): String {
        return value.name
    }

    @TypeConverter
    fun toFarmType(value: String): FarmType {
        return try {
            FarmType.valueOf(value)
        } catch (e: Exception) {
            FarmType.SMALL_SCALE
        }
    }

    @TypeConverter
    fun fromCertificationLevel(value: CertificationLevel): String {
        return value.name
    }

    @TypeConverter
    fun toCertificationLevel(value: String): CertificationLevel {
        return try {
            CertificationLevel.valueOf(value)
        } catch (e: Exception) {
            CertificationLevel.BASIC
        }
    }

    @TypeConverter
    fun fromFarmContactInfo(value: FarmContactInfo?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toFarmContactInfo(value: String?): FarmContactInfo? {
        return if (value == null) null else try {
            Gson().fromJson(value, FarmContactInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromFarmFacilityList(value: List<FarmFacility>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toFarmFacilityList(value: String): List<FarmFacility> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<FarmFacility>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Flock Management Type Converters
    @TypeConverter
    fun fromFlockType(value: FlockType): String {
        return value.name
    }

    @TypeConverter
    fun toFlockType(value: String): FlockType {
        return try {
            FlockType.valueOf(value)
        } catch (e: Exception) {
            FlockType.MIXED
        }
    }

    @TypeConverter
    fun fromFlockHealthStatus(value: FlockHealthStatus): String {
        return value.name
    }

    @TypeConverter
    fun toFlockHealthStatus(value: String): FlockHealthStatus {
        return try {
            FlockHealthStatus.valueOf(value)
        } catch (e: Exception) {
            FlockHealthStatus.HEALTHY
        }
    }

    @TypeConverter
    fun fromFeedingSchedule(value: FeedingSchedule?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toFeedingSchedule(value: String?): FeedingSchedule? {
        return if (value == null) null else try {
            Gson().fromJson(value, FeedingSchedule::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromVaccinationRecordList(value: List<VaccinationRecord>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toVaccinationRecordList(value: String): List<VaccinationRecord> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<VaccinationRecord>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromProductionMetrics(value: ProductionMetrics?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toProductionMetrics(value: String?): ProductionMetrics? {
        return if (value == null) null else try {
            Gson().fromJson(value, ProductionMetrics::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromEnvironmentalMonitoring(value: EnvironmentalMonitoring?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toEnvironmentalMonitoring(value: String?): EnvironmentalMonitoring? {
        return if (value == null) null else try {
            Gson().fromJson(value, EnvironmentalMonitoring::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Lifecycle Type Converters
    @TypeConverter
    fun fromLifecycleStage(value: LifecycleStage): String {
        return value.name
    }

    @TypeConverter
    fun toLifecycleStage(value: String): LifecycleStage {
        return try {
            LifecycleStage.valueOf(value)
        } catch (e: Exception) {
            LifecycleStage.EGG
        }
    }

    @TypeConverter
    fun fromLifecycleMilestoneList(value: List<LifecycleMilestone>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLifecycleMilestoneList(value: String): List<LifecycleMilestone> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<LifecycleMilestone>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromGrowthMetricList(value: List<GrowthMetric>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGrowthMetricList(value: String): List<GrowthMetric> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<GrowthMetric>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromHatchingDetails(value: HatchingDetails?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toHatchingDetails(value: String?): HatchingDetails? {
        return if (value == null) null else try {
            Gson().fromJson(value, HatchingDetails::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Lineage Type Converters
    @TypeConverter
    fun fromBreedingRecordList(value: List<BreedingRecord>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toBreedingRecordList(value: String): List<BreedingRecord> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<BreedingRecord>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromGeneticTraitList(value: List<GeneticTrait>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGeneticTraitList(value: String): List<GeneticTrait> {
        return try {
            Gson().fromJson(value, object : TypeToken<List<GeneticTrait>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromBloodlineMetrics(value: BloodlineMetrics?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toBloodlineMetrics(value: String?): BloodlineMetrics? {
        return if (value == null) null else try {
            Gson().fromJson(value, BloodlineMetrics::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Vaccination Type Converters
    @TypeConverter
    fun fromVaccineType(value: VaccineType): String {
        return value.name
    }

    @TypeConverter
    fun toVaccineType(value: String): VaccineType {
        return try {
            VaccineType.valueOf(value)
        } catch (e: Exception) {
            VaccineType.OTHER
        }
    }

    @TypeConverter
    fun fromAdministrationMethod(value: AdministrationMethod): String {
        return value.name
    }

    @TypeConverter
    fun toAdministrationMethod(value: String): AdministrationMethod {
        return try {
            AdministrationMethod.valueOf(value)
        } catch (e: Exception) {
            AdministrationMethod.INJECTION
        }
    }

    // Farm Access Management Type Converters
    @TypeConverter
    fun fromFarmRole(value: FarmRole): String {
        return value.name
    }

    @TypeConverter
    fun toFarmRole(value: String): FarmRole {
        return try {
            FarmRole.valueOf(value)
        } catch (e: Exception) {
            FarmRole.VIEWER
        }
    }

    @TypeConverter
    fun fromFarmPermissionList(value: List<FarmPermission>): String {
        return Gson().toJson(value.map { it.name })
    }

    @TypeConverter
    fun toFarmPermissionList(value: String): List<FarmPermission> {
        return try {
            val stringList: List<String> = Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
            stringList.mapNotNull { permissionName ->
                try {
                    FarmPermission.valueOf(permissionName)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAccessStatus(value: AccessStatus): String {
        return value.name
    }

    @TypeConverter
    fun toAccessStatus(value: String): AccessStatus {
        return try {
            AccessStatus.valueOf(value)
        } catch (e: Exception) {
            AccessStatus.PENDING
        }
    }

    @TypeConverter
    fun fromInvitationStatus(value: InvitationStatus): String {
        return value.name
    }

    @TypeConverter
    fun toInvitationStatus(value: String): InvitationStatus {
        return try {
            InvitationStatus.valueOf(value)
        } catch (e: Exception) {
            InvitationStatus.SENT
        }
    }

    @TypeConverter
    fun fromInvitationPriority(value: InvitationPriority): String {
        return value.name
    }

    @TypeConverter
    fun toInvitationPriority(value: String): InvitationPriority {
        return try {
            InvitationPriority.valueOf(value)
        } catch (e: Exception) {
            InvitationPriority.NORMAL
        }
    }

    @TypeConverter
    fun fromInvitationMetadata(value: InvitationMetadata?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    @TypeConverter
    fun toInvitationMetadata(value: String?): InvitationMetadata? {
        return if (value == null) null else try {
            Gson().fromJson(value, InvitationMetadata::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromAccessAction(value: AccessAction): String {
        return value.name
    }

    @TypeConverter
    fun toAccessAction(value: String): AccessAction {
        return try {
            AccessAction.valueOf(value)
        } catch (e: Exception) {
            AccessAction.LOGIN
        }
    }

    @TypeConverter
    fun fromUrgencyLevel(value: UrgencyLevel): String {
        return value.name
    }

    @TypeConverter
    fun toUrgencyLevel(value: String): UrgencyLevel {
        return try {
            UrgencyLevel.valueOf(value)
        } catch (e: Exception) {
            UrgencyLevel.NORMAL
        }
    }

    @TypeConverter
    fun fromRequestStatus(value: RequestStatus): String {
        return value.name
    }

    @TypeConverter
    fun toRequestStatus(value: String): RequestStatus {
        return try {
            RequestStatus.valueOf(value)
        } catch (e: Exception) {
            RequestStatus.PENDING
        }
    }

    @TypeConverter
    fun fromBulkInvitationStatus(value: BulkInvitationStatus): String {
        return value.name
    }

    @TypeConverter
    fun toBulkInvitationStatus(value: String): BulkInvitationStatus {
        return try {
            BulkInvitationStatus.valueOf(value)
        } catch (e: Exception) {
            BulkInvitationStatus.PENDING
        }
    }

    @TypeConverter
    fun fromInvitationEvent(value: InvitationEvent): String {
        return value.name
    }

    @TypeConverter
    fun toInvitationEvent(value: String): InvitationEvent {
        return try {
            InvitationEvent.valueOf(value)
        } catch (e: Exception) {
            InvitationEvent.CREATED
        }
    }

    // Performance Rating Converter
    @TypeConverter
    fun fromPerformanceRating(value: PerformanceRating): String {
        return value.name
    }

    @TypeConverter
    fun toPerformanceRating(value: String): PerformanceRating {
        return try {
            PerformanceRating.valueOf(value)
        } catch (e: Exception) {
            PerformanceRating.AVERAGE
        }
    }
}