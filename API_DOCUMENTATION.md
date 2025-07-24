# 📡 Rostry API Documentation

> Comprehensive API documentation for the Rostry fowl management platform

## 📋 Table of Contents

1. [Overview](#overview)
2. [Data Models](#data-models)
3. [Repository APIs](#repository-apis)
4. [Firebase Integration](#firebase-integration)
5. [Authentication](#authentication)
6. [Error Handling](#error-handling)
7. [Usage Examples](#usage-examples)

## 🎯 Overview

The Rostry API provides a comprehensive interface for managing fowl data, marketplace transactions, user communication, and social features. Built on top of Firebase services with local Room database caching.

### **Architecture**
```
UI Layer → ViewModel → Repository → Data Source (Firebase/Room)
```

### **Key Features**
- **Offline-First**: Local database with cloud synchronization
- **Real-time Updates**: Firebase listeners for live data
- **Type Safety**: Kotlin data classes with null safety
- **Error Handling**: Comprehensive Result types
- **Caching**: Intelligent local caching strategy

## 📊 Data Models

### **Core Models**

#### **Fowl**
```kotlin
@Entity(tableName = "fowls")
data class Fowl(
    @PrimaryKey @DocumentId
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

#### **FowlRecord**
```kotlin
@Entity(tableName = "fowl_records")
data class FowlRecord(
    @PrimaryKey @DocumentId
    val recordId: String = "",
    val fowlId: String = "",
    val recordType: String = "",
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
    VACCINATION, WEIGHT_UPDATE, HEALTH_CHECK, FEEDING,
    BREEDING, TREATMENT, SALE, PURCHASE, DEATH, TRANSFER, OTHER
}
```

#### **TransferLog**
```kotlin
@Entity(tableName = "transfer_logs")
data class TransferLog(
    @PrimaryKey @DocumentId
    val transferId: String = "",
    val fowlId: String = "",
    val giverId: String = "",
    val giverName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val status: String = "pending",
    val verificationDetails: Map<String, String> = emptyMap(),
    val rejectionReason: String? = null,
    val agreedPrice: Double = 0.0,
    val currentWeight: Double? = null,
    val recentPhotoUrl: String? = null,
    val transferNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null,
    val rejectedAt: Long? = null
)

enum class TransferStatus {
    PENDING, VERIFIED, REJECTED, CANCELLED, COMPLETED
}
```

#### **MarketplaceListing**
```kotlin
@Entity(tableName = "marketplace_listings")
data class MarketplaceListing(
    @PrimaryKey @DocumentId
    val listingId: String = "",
    val fowlId: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val sellerRating: Double = 0.0,
    val price: Double = 0.0,
    val purpose: String = "",
    val isActive: Boolean = true,
    val featuredImageUrl: String = "",
    val description: String = "",
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Auto-populated from fowl profile
    val fowlName: String = "",
    val fowlBreed: String = "",
    val fowlType: String = "",
    val fowlGender: String = "",
    val fowlAge: String = "",
    val motherId: String? = null,
    val fatherId: String? = null,
    val vaccinationRecords: List<String> = emptyList(),
    val healthStatus: String = "",
    val isBreederReady: Boolean = false
)
```

#### **Chat & Messaging**
```kotlin
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey @DocumentId
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class MessageType {
    TEXT, IMAGE, FOWL_LISTING
}
```

## 🏗️ Repository APIs

### **FowlRepository**

#### **Basic Operations**

```kotlin
interface FowlRepository {
    // Create
    suspend fun addFowl(fowl: Fowl): Result<String>
    suspend fun addFowlWithRecord(fowl: Fowl, initialRecord: FowlRecord): Result<String>
    
    // Read
    suspend fun getFowlById(fowlId: String): Fowl?
    fun getFowlByIdFlow(fowlId: String): Flow<Fowl?>
    fun getFowlsByOwner(ownerId: String): Flow<List<Fowl>>
    fun getMarketplaceFowls(): Flow<List<Fowl>>
    fun searchFowls(query: String): Flow<List<Fowl>>
    
    // Update
    suspend fun updateFowl(fowl: Fowl): Result<Unit>
    
    // Delete
    suspend fun deleteFowl(fowlId: String): Result<Unit>
    
    // Image Management
    suspend fun uploadFowlImage(imageUri: String, fowlId: String): Result<String>
    suspend fun uploadProofImage(imageUri: android.net.Uri, fowlId: String): Result<String>
}
```

#### **Record Management**

```kotlin
interface FowlRecordOperations {
    // Records
    suspend fun addFowlRecord(record: FowlRecord): Result<String>
    fun getFowlRecords(fowlId: String): Flow<List<FowlRecord>>
    suspend fun updateFowlRecord(record: FowlRecord): Result<Unit>
    suspend fun deleteFowlRecord(recordId: String): Result<Unit>
}
```

#### **Usage Examples**

```kotlin
// Add new fowl with initial record
val fowl = Fowl(
    name = "Henrietta",
    breed = "Rhode Island Red",
    type = FowlType.CHICKEN,
    gender = FowlGender.FEMALE,
    ownerId = currentUserId
)

val initialRecord = FowlRecord(
    fowlId = fowl.id,
    recordType = "Initial Record",
    details = "Healthy chick, good appetite",
    weight = 0.5
)

val result = fowlRepository.addFowlWithRecord(fowl, initialRecord)
if (result.isSuccess) {
    println("Fowl added successfully: ${result.getOrNull()}")
} else {
    println("Error: ${result.exceptionOrNull()?.message}")
}

// Get fowls by owner with real-time updates
fowlRepository.getFowlsByOwner(userId).collect { fowls ->
    // Update UI with fowl list
    updateFowlList(fowls)
}

// Search fowls
fowlRepository.searchFowls("Rhode Island").collect { searchResults ->
    // Display search results
    displaySearchResults(searchResults)
}
```

### **TransferRepository**

#### **Transfer Operations**

```kotlin
interface TransferRepository {
    // Initiate transfer
    suspend fun initiateTransfer(
        fowlId: String,
        giverId: String,
        giverName: String,
        receiverId: String,
        receiverName: String,
        agreedPrice: Double,
        currentWeight: Double?,
        transferNotes: String,
        recentPhotoUri: android.net.Uri?
    ): Result<String>
    
    // Verify transfer
    suspend fun verifyTransfer(transferId: String, receiverId: String): Result<Unit>
    
    // Reject transfer
    suspend fun rejectTransfer(
        transferId: String, 
        receiverId: String, 
        rejectionReason: String
    ): Result<Unit>
    
    // Cancel transfer
    suspend fun cancelTransfer(transferId: String, giverId: String): Result<Unit>
    
    // Get transfer history
    fun getFowlTransferHistory(fowlId: String): Flow<List<TransferLog>>
    fun getUserTransfers(userId: String): Flow<List<TransferLog>>
    fun getPendingTransfers(userId: String): Flow<List<TransferLog>>
}
```

#### **Usage Examples**

```kotlin
// Initiate transfer
val result = transferRepository.initiateTransfer(
    fowlId = "fowl123",
    giverId = currentUserId,
    giverName = "John Doe",
    receiverId = "user456",
    receiverName = "Jane Smith",
    agreedPrice = 150.0,
    currentWeight = 2.5,
    transferNotes = "Healthy bird, ready for breeding",
    recentPhotoUri = selectedImageUri
)

// Verify transfer (receiver side)
transferRepository.verifyTransfer("transfer123", currentUserId)
    .onSuccess { 
        // Transfer completed, ownership changed
        showSuccessMessage("Transfer completed successfully")
    }
    .onFailure { error ->
        showErrorMessage("Transfer failed: ${error.message}")
    }

// Get pending transfers
transferRepository.getPendingTransfers(currentUserId).collect { transfers ->
    // Show pending transfer notifications
    updatePendingTransfers(transfers)
}
```

### **MarketplaceRepository**

#### **Marketplace Operations**

```kotlin
interface MarketplaceRepository {
    // Listing management
    suspend fun createListing(
        fowlId: String,
        sellerId: String,
        sellerName: String,
        price: Double,
        purpose: String,
        description: String,
        location: String
    ): Result<String>
    
    suspend fun updateListing(
        listingId: String,
        sellerId: String,
        price: Double,
        purpose: String,
        description: String,
        location: String
    ): Result<Unit>
    
    suspend fun deactivateListing(listingId: String, sellerId: String): Result<Unit>
    
    // Browse listings
    fun getActiveListings(): Flow<List<MarketplaceListing>>
    fun getFilteredListings(
        purpose: String? = null,
        isBreederReady: Boolean? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        motherId: String? = null,
        fatherId: String? = null,
        fowlType: String? = null,
        location: String? = null
    ): Flow<List<MarketplaceListing>>
    
    fun searchListings(query: String): Flow<List<MarketplaceListing>>
    fun getUserListings(sellerId: String): Flow<List<MarketplaceListing>>
    
    suspend fun getListingById(listingId: String): MarketplaceListing?
}
```

#### **Usage Examples**

```kotlin
// Create marketplace listing
val result = marketplaceRepository.createListing(
    fowlId = "fowl123",
    sellerId = currentUserId,
    sellerName = "John's Farm",
    price = 200.0,
    purpose = "Breeding Stock",
    description = "Excellent breeding hen with proven lineage",
    location = "California, USA"
)

// Advanced filtering
marketplaceRepository.getFilteredListings(
    purpose = "Breeding Stock",
    isBreederReady = true,
    minPrice = 100.0,
    maxPrice = 500.0,
    fowlType = "CHICKEN"
).collect { listings ->
    // Display filtered results
    updateMarketplaceListings(listings)
}

// Search listings
marketplaceRepository.searchListings("Rhode Island Red").collect { results ->
    // Show search results
    displaySearchResults(results)
}
```

### **ChatRepository**

#### **Messaging Operations**

```kotlin
interface ChatRepository {
    // Chat management
    suspend fun createOrGetChatFirebase(
        user1Id: String,
        user2Id: String,
        user1Name: String,
        user2Name: String
    ): Result<String>
    
    // Real-time messaging
    fun getUserChatsFlow(userId: String): Flow<List<Chat>>
    fun getChatMessagesRealTime(chatId: String): Flow<List<Message>>
    
    // Send messages
    suspend fun sendMessageFirebase(message: Message): Result<String>
    suspend fun sendImageMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        imageUri: android.net.Uri
    ): Result<String>
    
    // Message status
    suspend fun markMessagesAsReadFirebase(chatId: String, userId: String): Result<Unit>
}
```

#### **Usage Examples**

```kotlin
// Start conversation
val chatResult = chatRepository.createOrGetChatFirebase(
    user1Id = currentUserId,
    user2Id = sellerId,
    user1Name = "John Doe",
    user2Name = "Jane Smith"
)

chatResult.onSuccess { chatId ->
    // Navigate to chat screen
    navigateToChat(chatId)
}

// Send text message
val message = Message(
    chatId = chatId,
    senderId = currentUserId,
    senderName = "John Doe",
    content = "Is this fowl still available?",
    type = MessageType.TEXT
)

chatRepository.sendMessageFirebase(message)

// Listen for real-time messages
chatRepository.getChatMessagesRealTime(chatId).collect { messages ->
    // Update chat UI
    updateChatMessages(messages)
}

// Send image message
chatRepository.sendImageMessage(
    chatId = chatId,
    senderId = currentUserId,
    senderName = "John Doe",
    imageUri = selectedImageUri
)
```

## 🔥 Firebase Integration

### **Firestore Collections Structure**

```
/users/{userId}
├── profile: UserProfile
├── settings: UserSettings
└── notifications: List<Notification>

/fowls/{fowlId}
├── basic_info: Fowl
├── health_records: List<HealthRecord>
└── images: List<ImageUrl>

/fowl_records/{recordId}
├── record_data: FowlRecord
└── proof_images: List<ImageUrl>

/transfer_logs/{transferId}
├── transfer_info: TransferLog
├── verification_details: Map<String, String>
└── status_history: List<StatusChange>

/marketplace_listings/{listingId}
├── listing_data: MarketplaceListing
├── auto_populated_data: FowlData
└── seller_info: SellerProfile

/chats/{chatId}
├── chat_info: Chat
└── participants: List<UserId>

/messages/{messageId}
├── message_data: Message
└── attachments: List<Attachment>

/posts/{postId}
├── post_data: Post
├── comments: List<Comment>
└── likes: List<UserId>
```

### **Security Rules**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Fowls: read by all, write by owner
    match /fowls/{fowlId} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.ownerId;
    }
    
    // Fowl records: read by fowl owner, write by record creator
    match /fowl_records/{recordId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        request.auth.uid == resource.data.createdBy;
    }
    
    // Transfer logs: read by participants, limited write access
    match /transfer_logs/{transferId} {
      allow read: if request.auth != null && 
        (request.auth.uid == resource.data.giverId || 
         request.auth.uid == resource.data.receiverId);
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        resource.data.status == "pending" &&
        (request.auth.uid == resource.data.giverId || 
         request.auth.uid == resource.data.receiverId);
    }
    
    // Marketplace listings: read by all, write by seller
    match /marketplace_listings/{listingId} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.sellerId;
    }
    
    // Chats: read/write by participants only
    match /chats/{chatId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participants;
    }
    
    // Messages: read by chat participants, write by sender
    match /messages/{messageId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
        request.auth.uid == request.resource.data.senderId;
    }
    
    // Posts: read by all, write by author
    match /posts/{postId} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.authorId;
    }
  }
}
```

### **Cloud Storage Rules**

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Fowl images: read by all, write by authenticated users
    match /fowl_images/{fowlId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Proof images: read by all, write by authenticated users
    match /fowl_proofs/{fowlId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Transfer photos: read by transfer participants
    match /transfer_photos/{transferId}/{imageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Chat images: read/write by authenticated users
    match /chat_images/{chatId}/{imageId} {
      allow read, write: if request.auth != null;
    }
    
    // Post images: read by all, write by authenticated users
    match /post_images/{postId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 🔐 Authentication

### **Firebase Auth Integration**

```kotlin
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    // Email/Password authentication
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUserWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Google Sign-In
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Password reset
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Current user
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
    
    fun getCurrentUserFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
    
    // Sign out
    fun signOut() = firebaseAuth.signOut()
}
```

### **User Session Management**

```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Sign in failed")
                }
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

## ⚠️ Error Handling

### **Result Type Pattern**

```kotlin
// Custom Result extensions
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (isSuccess) action(getOrNull()!!)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (isFailure) action(exceptionOrNull()!!)
    return this
}

// Repository error handling
suspend fun <T> safeCall(call: suspend () -> T): Result<T> {
    return try {
        Result.success(call())
    } catch (e: Exception) {
        when (e) {
            is FirebaseFirestoreException -> {
                Result.failure(DatabaseException("Database error: ${e.message}"))
            }
            is FirebaseAuthException -> {
                Result.failure(AuthenticationException("Auth error: ${e.message}"))
            }
            is IOException -> {
                Result.failure(NetworkException("Network error: ${e.message}"))
            }
            else -> {
                Result.failure(UnknownException("Unknown error: ${e.message}"))
            }
        }
    }
}
```

### **Custom Exceptions**

```kotlin
sealed class RostryException(message: String) : Exception(message)

class DatabaseException(message: String) : RostryException(message)
class NetworkException(message: String) : RostryException(message)
class AuthenticationException(message: String) : RostryException(message)
class ValidationException(message: String) : RostryException(message)
class UnknownException(message: String) : RostryException(message)
```

### **Error Handling in ViewModels**

```kotlin
@HiltViewModel
class FowlViewModel @Inject constructor(
    private val fowlRepository: FowlRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FowlUiState())
    val uiState: StateFlow<FowlUiState> = _uiState.asStateFlow()
    
    fun addFowl(fowl: Fowl) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            fowlRepository.addFowl(fowl)
                .onSuccess { fowlId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = "Fowl added successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = when (error) {
                            is ValidationException -> "Please check your input: ${error.message}"
                            is NetworkException -> "Network error. Please try again."
                            is DatabaseException -> "Database error. Please try again."
                            else -> "An unexpected error occurred."
                        }
                    )
                }
        }
    }
}
```

## 📝 Usage Examples

### **Complete Fowl Management Flow**

```kotlin
class FowlManagementExample {
    
    suspend fun completeFlowExample() {
        // 1. Add new fowl
        val fowl = Fowl(
            name = "Henrietta",
            breed = "Rhode Island Red",
            type = FowlType.CHICKEN,
            gender = FowlGender.FEMALE,
            ownerId = "user123"
        )
        
        val initialRecord = FowlRecord(
            fowlId = fowl.id,
            recordType = "Initial Record",
            details = "Healthy chick, 1 day old",
            weight = 0.05
        )
        
        fowlRepository.addFowlWithRecord(fowl, initialRecord)
            .onSuccess { fowlId ->
                println("Fowl added: $fowlId")
                
                // 2. Add health record
                addHealthRecord(fowlId)
                
                // 3. Create marketplace listing
                createMarketplaceListing(fowlId)
                
                // 4. Handle transfer request
                handleTransferRequest(fowlId)
            }
    }
    
    private suspend fun addHealthRecord(fowlId: String) {
        val healthRecord = FowlRecord(
            fowlId = fowlId,
            recordType = "Vaccination",
            details = "Newcastle disease vaccine administered",
            medication = "Newcastle vaccine",
            veterinarian = "Dr. Smith",
            cost = 25.0
        )
        
        fowlRepository.addFowlRecord(healthRecord)
    }
    
    private suspend fun createMarketplaceListing(fowlId: String) {
        marketplaceRepository.createListing(
            fowlId = fowlId,
            sellerId = "user123",
            sellerName = "John's Farm",
            price = 150.0,
            purpose = "Breeding Stock",
            description = "Excellent breeding hen",
            location = "California"
        )
    }
    
    private suspend fun handleTransferRequest(fowlId: String) {
        // Initiate transfer
        transferRepository.initiateTransfer(
            fowlId = fowlId,
            giverId = "user123",
            giverName = "John Doe",
            receiverId = "user456",
            receiverName = "Jane Smith",
            agreedPrice = 150.0,
            currentWeight = 2.5,
            transferNotes = "Healthy breeding hen",
            recentPhotoUri = null
        ).onSuccess { transferId ->
            println("Transfer initiated: $transferId")
            
            // Simulate receiver verification
            transferRepository.verifyTransfer(transferId, "user456")
                .onSuccess {
                    println("Transfer completed successfully")
                }
        }
    }
}
```

### **Real-time Data Subscription**

```kotlin
class RealTimeExample {
    
    fun setupRealTimeSubscriptions() {
        // Subscribe to user's fowls
        fowlRepository.getFowlsByOwner("user123").collect { fowls ->
            // Update UI with fowl list
            updateFowlList(fowls)
        }
        
        // Subscribe to marketplace listings
        marketplaceRepository.getActiveListings().collect { listings ->
            // Update marketplace UI
            updateMarketplace(listings)
        }
        
        // Subscribe to chat messages
        chatRepository.getChatMessagesRealTime("chat123").collect { messages ->
            // Update chat UI
            updateChatMessages(messages)
        }
        
        // Subscribe to pending transfers
        transferRepository.getPendingTransfers("user123").collect { transfers ->
            // Show notifications
            showTransferNotifications(transfers)
        }
    }
}
```

### **Advanced Search and Filtering**

```kotlin
class SearchExample {
    
    suspend fun advancedSearchExample() {
        // Search fowls by breed
        fowlRepository.searchFowls("Rhode Island").collect { results ->
            println("Found ${results.size} fowls")
        }
        
        // Filter marketplace listings
        marketplaceRepository.getFilteredListings(
            purpose = "Breeding Stock",
            isBreederReady = true,
            minPrice = 100.0,
            maxPrice = 300.0,
            fowlType = "CHICKEN",
            location = "California"
        ).collect { filteredListings ->
            println("Filtered results: ${filteredListings.size}")
        }
        
        // Search by bloodline
        marketplaceRepository.getFilteredListings(
            motherId = "fowl123"
        ).collect { offspring ->
            println("Found offspring: ${offspring.size}")
        }
    }
}
```

---

**📚 This API documentation is continuously updated as new features are added. Please refer to the source code for the most current implementation details.**