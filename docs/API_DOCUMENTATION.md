# ROSTRY API Documentation

> **Version**: 2.0.0
> **Last Updated**: 2025-01-08
> **Status**: Current Implementation with Farm Management System

## 📋 Overview

This document provides comprehensive documentation for ROSTRY's internal API architecture, including repository interfaces, data models, and service contracts. The system now includes extensive farm management, access control, and collaboration features.

## 🏗️ Repository Architecture

### Core Repositories

#### FarmRepository ⭐ **NEW**
**Purpose**: Manages farm entities and comprehensive farm operations

```kotlin
@Singleton
class FarmRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val farmDao: FarmDao,
    private val flockDao: FlockDao
) {

    // Farm Management
    suspend fun createFarm(farm: Farm): Result<String>
    suspend fun updateFarm(farm: Farm): Result<Unit>
    suspend fun deleteFarm(farmId: String): Result<Unit>

    // Farm Queries
    fun getCurrentFarm(): Flow<Farm?>
    fun getUserFarms(ownerId: String): Flow<List<Farm>>
    suspend fun getFarmById(farmId: String): Farm?

    // Flock Management
    suspend fun createFlock(flock: Flock): Result<String>
    suspend fun updateFlock(flock: Flock): Result<Unit>
    fun getAllFlocks(): Flow<List<Flock>>
    fun getFlocksByFarm(farmId: String): Flow<List<Flock>>

    // Analytics & Insights
    suspend fun getFarmAnalytics(): FarmAnalytics
    fun getHealthAlerts(): Flow<List<String>>
    fun getUpcomingTasks(): Flow<List<String>>
    fun getRecentActivities(): Flow<List<String>>

    // Facility Management
    suspend fun addFacility(farmId: String, facility: FarmFacility): Result<Unit>
    suspend fun updateFacility(farmId: String, facility: FarmFacility): Result<Unit>
    fun getFacilitiesNeedingMaintenance(farmId: String): Flow<List<FarmFacility>>
}
```

#### FarmAccessRepository ⭐ **NEW**
**Purpose**: Manages farm access control, invitations, and collaboration

```kotlin
@Singleton
class FarmAccessRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmAccessDao: FarmAccessDao,
    private val invitationDao: InvitationDao,
    private val farmRepository: FarmRepository
) {

    // Invitation Management
    suspend fun sendInvitation(invitation: FarmInvitation): Result<String>
    suspend fun sendBulkInvitations(bulkInvitation: BulkInvitation): Result<String>
    suspend fun acceptInvitation(invitationId: String, userId: String): Result<Unit>
    suspend fun rejectInvitation(invitationId: String, userId: String): Result<Unit>

    // Access Management
    fun getUserFarms(userId: String): Flow<List<FarmWithAccess>>
    suspend fun hasPermission(userId: String, farmId: String, permission: FarmPermission): Boolean
    suspend fun hasPermissions(userId: String, farmId: String, permissions: List<FarmPermission>): Map<FarmPermission, Boolean>

    // Role Management
    suspend fun updateUserRole(farmId: String, userId: String, newRole: FarmRole): Result<Unit>
    suspend fun updateUserPermissions(farmId: String, userId: String, permissions: List<FarmPermission>): Result<Unit>
    suspend fun revokeAccess(farmId: String, userId: String): Result<Unit>

    // Analytics & Audit
    suspend fun getFarmAccessAnalytics(farmId: String): FarmAccessAnalytics
    fun getSecurityAlerts(farmId: String): Flow<List<SecurityAlert>>
    fun getAccessAuditLog(farmId: String): Flow<List<AccessAuditLog>>

    // Templates & Bulk Operations
    suspend fun createInvitationTemplate(template: InvitationTemplate): Result<String>
    fun getInvitationTemplates(farmId: String): Flow<List<InvitationTemplate>>
    suspend fun processBulkInvitation(bulkInvitationId: String): Result<Unit>
}
```

#### FowlRepository
**Purpose**: Manages fowl entities and related operations

```kotlin
@Singleton
class FowlRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val fowlDao: FowlDao
) {
    
    // CRUD Operations
    suspend fun addFowl(fowl: Fowl): Result<String>
    suspend fun updateFowl(fowl: Fowl): Result<Unit>
    suspend fun deleteFowl(fowlId: String): Result<Unit>
    
    // Query Operations
    fun getFowlsByOwnerFlow(ownerId: String): Flow<List<Fowl>>
    fun getMarketplaceFowls(): Flow<List<Fowl>>
    fun getFowlByIdFlow(fowlId: String): Flow<Fowl?>
    suspend fun searchFowls(query: String): List<Fowl>
    
    // Image Management
    suspend fun uploadFowlImage(imageUri: String, fowlId: String): Result<String>
    suspend fun deleteFowlImage(imageUrl: String): Result<Unit>
    
    // Marketplace Operations
    suspend fun markFowlForSale(fowlId: String, price: Double): Result<Unit>
    suspend fun removeFowlFromSale(fowlId: String): Result<Unit>
}
```

#### UserRepository
**Purpose**: Manages user profiles and authentication data

```kotlin
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    
    // User Management
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun getUserById(userId: String): User?
    fun getCurrentUserFlow(): Flow<User?>
    
    // Profile Operations
    suspend fun updateProfile(userId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun uploadProfileImage(userId: String, imageUri: String): Result<String>
    
    // Verification
    suspend fun submitKycVerification(userId: String, documents: List<String>): Result<Unit>
    suspend fun updateVerificationStatus(userId: String, status: VerificationStatus): Result<Unit>
}
```

#### AuthRepository
**Purpose**: Handles authentication operations

```kotlin
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {
    
    // Authentication
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, displayName: String): Result<User>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    
    // Session Management
    fun getCurrentUser(): User?
    fun isUserSignedIn(): Boolean
    fun getCurrentUserFlow(): Flow<User?>
}
```

#### ChatRepository
**Purpose**: Manages chat conversations and messages

```kotlin
@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    // Chat Management
    suspend fun createChat(participantIds: List<String>): Result<String>
    fun getUserChatsFlow(userId: String): Flow<List<ChatUiModel>>
    fun getChatMessagesFlow(chatId: String): Flow<List<Message>>
    
    // Message Operations
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit>
    suspend fun markMessageAsRead(messageId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
    
    // Media Messages
    suspend fun sendImageMessage(chatId: String, imageUri: String, senderId: String): Result<Unit>
}
```

#### MarketplaceRepository
**Purpose**: Manages marketplace listings and operations

```kotlin
@Singleton
class MarketplaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fowlRepository: FowlRepository
) {
    
    // Listing Management
    fun getMarketplaceListings(): Flow<List<Fowl>>
    suspend fun createListing(fowl: Fowl): Result<Unit>
    suspend fun updateListing(fowl: Fowl): Result<Unit>
    suspend fun removeListing(fowlId: String): Result<Unit>
    
    // Search and Filter
    suspend fun searchListings(query: String): List<Fowl>
    suspend fun filterListings(filters: MarketplaceFilters): List<Fowl>
    
    // Featured Listings
    suspend fun getFeaturedListings(): List<Fowl>
    suspend fun promoteToFeatured(fowlId: String): Result<Unit>
}
```

#### WalletRepository
**Purpose**: Manages user wallets and transactions

```kotlin
@Singleton
class WalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val walletDao: WalletDao
) {
    
    // Wallet Operations
    suspend fun getWallet(userId: String): Wallet?
    suspend fun updateCoinBalance(userId: String, amount: Int): Result<Unit>
    suspend fun addCoins(userId: String, amount: Int, reason: String): Result<Unit>
    suspend fun deductCoins(userId: String, amount: Int, reason: String): Result<Unit>
    
    // Transaction History
    fun getTransactionHistory(userId: String): Flow<List<CoinTransaction>>
    suspend fun recordTransaction(transaction: CoinTransaction): Result<Unit>
}
```

#### OrderRepository
**Purpose**: Manages purchase orders and transactions

```kotlin
@Singleton
class OrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val orderDao: OrderDao
) {
    
    // Order Management
    suspend fun createOrder(order: Order): Result<String>
    suspend fun updateOrder(order: Order): Result<Unit>
    suspend fun cancelOrder(orderId: String): Result<Unit>
    
    // Order Queries
    fun getUserOrders(userId: String): Flow<List<Order>>
    fun getSellerOrders(sellerId: String): Flow<List<Order>>
    suspend fun getOrderById(orderId: String): Order?
    
    // Order Status
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
}
```

## 📊 Data Models

### Farm Management Entities ⭐ **NEW**

#### Farm Entity
```kotlin
@Entity(tableName = "farms")
data class Farm(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val farmName: String,
    val location: String,
    val description: String = "",
    val farmType: FarmType = FarmType.SMALL_SCALE,
    val totalArea: Double = 0.0, // in hectares
    val establishedDate: Long = System.currentTimeMillis(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val certificationLevel: CertificationLevel = CertificationLevel.BASIC,
    val certificationDate: Long = 0,
    val certificationUrls: List<String> = emptyList(),
    val contactInfo: FarmContactInfo? = null,
    val facilities: List<FarmFacility> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### Flock Entity
```kotlin
@Entity(tableName = "flocks")
data class Flock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val flockName: String,
    val flockType: FlockType,
    val breed: String,
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val maleCount: Int = 0,
    val femaleCount: Int = 0,
    val averageAge: Int = 0, // in weeks
    val establishedDate: Long = System.currentTimeMillis(),
    val facilityId: String? = null,
    val healthStatus: FlockHealthStatus = FlockHealthStatus.HEALTHY,
    val feedingSchedule: FeedingSchedule? = null,
    val vaccinationSchedule: List<VaccinationRecord> = emptyList(),
    val productionMetrics: ProductionMetrics? = null,
    val environmentalConditions: EnvironmentalMonitoring? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### FarmAccess Entity
```kotlin
@Entity(tableName = "farm_access")
data class FarmAccess(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val userId: String,
    val role: FarmRole,
    val permissions: List<FarmPermission> = emptyList(),
    val invitedBy: String,
    val invitedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val status: AccessStatus = AccessStatus.PENDING,
    val expiresAt: Long? = null,
    val isActive: Boolean = true,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### FarmInvitation Entity
```kotlin
@Entity(tableName = "farm_invitations")
data class FarmInvitation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val farmName: String,
    val inviterUserId: String,
    val inviterName: String,
    val inviterEmail: String,
    val inviteeEmail: String,
    val inviteeUserId: String? = null,
    val proposedRole: FarmRole,
    val customPermissions: List<FarmPermission> = emptyList(),
    val invitationMessage: String = "",
    val invitationCode: String = generateInvitationCode(),
    val invitationLink: String = generateInvitationLink(),
    val status: InvitationStatus = InvitationStatus.SENT,
    val priority: InvitationPriority = InvitationPriority.NORMAL,
    val sentAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000),
    val respondedAt: Long? = null,
    val remindersSent: Int = 0,
    val lastReminderAt: Long? = null,
    val maxReminders: Int = 3,
    val allowCustomRole: Boolean = false,
    val requiresApproval: Boolean = false,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val metadata: InvitationMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### VaccinationRecord Entity
```kotlin
@Entity(tableName = "vaccination_records")
data class VaccinationRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val flockId: String? = null,
    val fowlId: String? = null,
    val vaccineName: String,
    val vaccineType: VaccineType,
    val administrationDate: Long,
    val nextDueDate: Long = 0,
    val dosage: String = "",
    val administrationMethod: AdministrationMethod = AdministrationMethod.INJECTION,
    val administeredBy: String = "",
    val batchNumber: String = "",
    val manufacturer: String = "",
    val expiryDate: Long = 0,
    val storageTemperature: String = "",
    val proofImageUrl: String = "",
    val notes: String = "",
    val sideEffects: String = "",
    val efficacy: Double = 0.0, // percentage
    val cost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### Bloodline Entity
```kotlin
@Entity(tableName = "bloodlines")
data class Bloodline(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val originFowlId: String,
    val founderGeneration: Int = 1,
    val characteristics: List<String> = emptyList(),
    val totalGenerations: Int = 1,
    val activeBreeders: Int = 0,
    val totalOffspring: Int = 0,
    val performanceMetrics: BloodlineMetrics? = null,
    val geneticDiversity: Double = 1.0,
    val breedingGoals: List<String> = emptyList(),
    val certificationLevel: String = "UNVERIFIED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun calculateBloodlineStrength(): BloodlineStrength {
        val metrics = performanceMetrics ?: return BloodlineStrength.UNKNOWN

        val avgScore = (
            (metrics.survivalRate / 100) * 0.3 +
            (metrics.breedingSuccessRate / 100) * 0.3 +
            (geneticDiversity) * 0.2 +
            (if (totalGenerations >= 3) 0.2 else 0.1)
        )

        return when {
            avgScore >= 0.9 -> BloodlineStrength.EXCEPTIONAL
            avgScore >= 0.8 -> BloodlineStrength.STRONG
            avgScore >= 0.7 -> BloodlineStrength.GOOD
            avgScore >= 0.6 -> BloodlineStrength.AVERAGE
            else -> BloodlineStrength.WEAK
        }
    }
}
```

### Analytics Data Classes ⭐ **NEW**

#### Farm Analytics Data Classes
```kotlin
data class FlockTypeCount(
    val flockType: String,
    val count: Int
)

data class FlockHealthCount(
    val healthStatus: String,
    val count: Int
)

data class LifecycleStageCount(
    val currentStage: String,
    val count: Int
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

data class InvitationStatistics(
    val totalInvitations: Int,
    val pendingInvitations: Int,
    val acceptedInvitations: Int,
    val rejectedInvitations: Int,
    val expiredInvitations: Int,
    val avgResponseTime: Double
)
```

### Core Entities

#### Fowl Entity
```kotlin
@Entity(tableName = "fowls")
data class Fowl(
    @PrimaryKey val id: String = "",
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
    val status: String = "Growing",
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
    CHICKEN, DUCK, TURKEY, GOOSE, GUINEA_FOWL, OTHER
}

enum class FowlGender {
    MALE, FEMALE, UNKNOWN
}
```

#### User Entity
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val role: UserRole = UserRole.GENERAL,
    val phoneNumber: String = "",
    val location: String = "",
    val bio: String = "",
    val isKycVerified: Boolean = false,
    val kycDocumentUrl: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val verificationBadges: List<String> = emptyList(),
    val coinBalance: Int = 0,
    val totalCoinsEarned: Int = 0,
    val totalCoinsSpent: Int = 0,
    val sellerRating: Double = 0.0,
    val totalSales: Int = 0,
    val joinedDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

enum class UserRole {
    GENERAL, FARMER, ENTHUSIAST
}
```

#### Order Entity
```kotlin
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val fowlId: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: String = "",
    val deliveryAddress: String = "",
    val deliveryMethod: DeliveryMethod = DeliveryMethod.PICKUP,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}

enum class DeliveryMethod {
    PICKUP, DELIVERY, SHIPPING
}
```

## 🔄 Data Flow Patterns

### Repository Pattern Implementation
```kotlin
// Standard data flow pattern used across all repositories
suspend fun <T> performDataOperation(
    remoteOperation: suspend () -> T,
    localOperation: suspend () -> T,
    cacheOperation: suspend (T) -> Unit
): T {
    return try {
        // 1. Attempt remote operation
        val remoteResult = remoteOperation()
        
        // 2. Cache result locally
        cacheOperation(remoteResult)
        
        // 3. Return remote result
        remoteResult
    } catch (e: Exception) {
        // 4. Fallback to local data
        localOperation()
    }
}
```

### Error Handling
```kotlin
// Standard Result wrapper for error handling
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
}
```

## 🔧 Service Contracts

### Firebase Service Integration
```kotlin
// Firestore collection names
object FirestoreCollections {
    const val USERS = "users"
    const val FOWLS = "fowls"
    const val POSTS = "posts"
    const val CHATS = "chats"
    const val ORDERS = "orders"
    const val TRANSFERS = "transfers"
    const val VERIFICATIONS = "verifications"
    const val MARKETPLACE_LISTINGS = "marketplace_listings"
}

// Storage paths
object StoragePaths {
    const val FOWL_IMAGES = "fowl_images"
    const val PROFILE_IMAGES = "profile_images"
    const val CHAT_IMAGES = "chat_images"
    const val VERIFICATION_DOCUMENTS = "verification_documents"
    const val TRANSFER_PHOTOS = "transfer_photos"
}
```

### API Response Models
```kotlin
// Standard API response wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
)

// Pagination support
data class PaginatedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageSize: Int,
    val currentPage: Int,
    val hasNextPage: Boolean
)
```

## 📱 Usage Examples

### Adding a New Fowl
```kotlin
// In ViewModel
class AddFowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepository
) : ViewModel() {
    
    suspend fun addFowl(fowlData: FowlData) {
        val fowl = Fowl(
            name = fowlData.name,
            breed = fowlData.breed,
            type = fowlData.type,
            ownerId = getCurrentUserId()
        )
        
        fowlRepository.addFowl(fowl)
            .onSuccess { fowlId ->
                // Handle success
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    fowlAdded = true
                )
            }
            .onError { exception ->
                // Handle error
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message
                )
            }
    }
}
```

### Fetching Marketplace Data
```kotlin
// In ViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {
    
    private val _marketplaceFowls = MutableStateFlow<List<Fowl>>(emptyList())
    val marketplaceFowls: StateFlow<List<Fowl>> = _marketplaceFowls.asStateFlow()
    
    init {
        viewModelScope.launch {
            marketplaceRepository.getMarketplaceListings()
                .collect { fowls ->
                    _marketplaceFowls.value = fowls
                }
        }
    }
}
```

---

**This API documentation reflects the current implementation of ROSTRY's internal architecture and should be used as the definitive reference for development.**
