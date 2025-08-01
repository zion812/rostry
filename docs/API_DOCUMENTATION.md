# ROSTRY API Documentation

> **Version**: 1.0.0  
> **Last Updated**: 2025-01-08  
> **Status**: Current Implementation  

## 📋 Overview

This document provides comprehensive documentation for ROSTRY's internal API architecture, including repository interfaces, data models, and service contracts.

## 🏗️ Repository Architecture

### Core Repositories

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
