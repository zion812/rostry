# ROSTRY Database Schema Documentation

> **Version**: 6.0  
> **Last Updated**: 2025-01-08  
> **Database Type**: Hybrid (Room + Firestore)  
> **Status**: Current Implementation  

## 📋 Overview

ROSTRY uses a hybrid database architecture combining Room (local SQLite) for offline capabilities and Firebase Firestore (cloud NoSQL) for real-time synchronization and backup.

## 🏗️ Database Architecture

### Hybrid Strategy Benefits
- **Offline Support**: Room provides local data access when network is unavailable
- **Real-time Sync**: Firestore enables real-time updates across devices
- **Performance**: Local queries are fast, cloud provides backup and sync
- **Scalability**: Firestore handles concurrent users and data growth

### Data Flow Pattern
```
User Action → Repository → Local DB (Room) → Cloud DB (Firestore)
                     ↓
UI Updates ← Flow/LiveData ← Local DB ← Sync ← Cloud DB
```

## 🗄️ Room Database (Local Storage)

### Database Configuration
```kotlin
@Database(
    entities = [
        User::class,
        Fowl::class,
        Post::class,
        Chat::class,
        Message::class,
        CartItem::class,
        FowlRecord::class,
        TransferLog::class,
        MarketplaceListing::class,
        Order::class,
        Wallet::class,
        CoinTransaction::class,
        VerificationRequest::class,
        ShowcaseSlot::class,
        FlockSummary::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RostryDatabase : RoomDatabase()
```

### Entity Schemas

#### 1. User Table
```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY NOT NULL,
    email TEXT NOT NULL,
    displayName TEXT NOT NULL,
    profileImageUrl TEXT NOT NULL,
    role TEXT NOT NULL,
    phoneNumber TEXT NOT NULL,
    location TEXT NOT NULL,
    bio TEXT NOT NULL,
    isKycVerified INTEGER NOT NULL,
    kycDocumentUrl TEXT NOT NULL,
    verificationStatus TEXT NOT NULL,
    verificationBadges TEXT NOT NULL,
    coinBalance INTEGER NOT NULL,
    totalCoinsEarned INTEGER NOT NULL,
    totalCoinsSpent INTEGER NOT NULL,
    sellerRating REAL NOT NULL,
    totalSales INTEGER NOT NULL,
    joinedDate INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    isOnline INTEGER NOT NULL,
    lastSeen INTEGER NOT NULL
);
```

#### 2. Fowl Table
```sql
CREATE TABLE fowls (
    id TEXT PRIMARY KEY NOT NULL,
    ownerId TEXT NOT NULL,
    name TEXT NOT NULL,
    breed TEXT NOT NULL,
    type TEXT NOT NULL,
    gender TEXT NOT NULL,
    dateOfBirth INTEGER,
    motherId TEXT,
    fatherId TEXT,
    dateOfHatching INTEGER NOT NULL,
    initialCount INTEGER,
    status TEXT NOT NULL,
    weight REAL NOT NULL,
    color TEXT NOT NULL,
    description TEXT NOT NULL,
    imageUrls TEXT NOT NULL,
    proofImageUrl TEXT,
    healthRecords TEXT NOT NULL,
    isForSale INTEGER NOT NULL,
    price REAL NOT NULL,
    location TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    
    FOREIGN KEY(ownerId) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 3. Post Table
```sql
CREATE TABLE posts (
    id TEXT PRIMARY KEY NOT NULL,
    authorId TEXT NOT NULL,
    content TEXT NOT NULL,
    imageUrls TEXT NOT NULL,
    likes INTEGER NOT NULL,
    comments INTEGER NOT NULL,
    shares INTEGER NOT NULL,
    isPublic INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    
    FOREIGN KEY(authorId) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 4. Chat Table
```sql
CREATE TABLE chats (
    id TEXT PRIMARY KEY NOT NULL,
    participantIds TEXT NOT NULL,
    lastMessage TEXT NOT NULL,
    lastMessageTime INTEGER NOT NULL,
    unreadCount INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

#### 5. Message Table
```sql
CREATE TABLE messages (
    id TEXT PRIMARY KEY NOT NULL,
    chatId TEXT NOT NULL,
    senderId TEXT NOT NULL,
    content TEXT NOT NULL,
    messageType TEXT NOT NULL,
    imageUrl TEXT,
    timestamp INTEGER NOT NULL,
    isRead INTEGER NOT NULL,
    
    FOREIGN KEY(chatId) REFERENCES chats(id) ON DELETE CASCADE,
    FOREIGN KEY(senderId) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 6. Order Table
```sql
CREATE TABLE orders (
    orderId TEXT PRIMARY KEY NOT NULL,
    buyerId TEXT NOT NULL,
    sellerId TEXT NOT NULL,
    fowlId TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unitPrice REAL NOT NULL,
    totalAmount REAL NOT NULL,
    status TEXT NOT NULL,
    paymentMethod TEXT NOT NULL,
    deliveryAddress TEXT NOT NULL,
    deliveryMethod TEXT NOT NULL,
    notes TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    
    FOREIGN KEY(buyerId) REFERENCES users(id),
    FOREIGN KEY(sellerId) REFERENCES users(id),
    FOREIGN KEY(fowlId) REFERENCES fowls(id)
);
```

#### 7. Wallet Table
```sql
CREATE TABLE wallets (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    coinBalance INTEGER NOT NULL,
    totalEarned INTEGER NOT NULL,
    totalSpent INTEGER NOT NULL,
    lastTransactionDate INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    
    FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
);
```

### Database Indexes
```sql
-- Performance optimization indexes
CREATE INDEX idx_fowls_owner ON fowls(ownerId);
CREATE INDEX idx_fowls_for_sale ON fowls(isForSale);
CREATE INDEX idx_fowls_type ON fowls(type);
CREATE INDEX idx_messages_chat ON messages(chatId);
CREATE INDEX idx_orders_buyer ON orders(buyerId);
CREATE INDEX idx_orders_seller ON orders(sellerId);
CREATE INDEX idx_posts_author ON posts(authorId);
```

### Migration History
```kotlin
// Migration from version 5 to 6
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add FlockSummary table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS flockSummary (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                totalFowls INTEGER NOT NULL,
                totalValue REAL NOT NULL,
                healthyCount INTEGER NOT NULL,
                sickCount INTEGER NOT NULL,
                forSaleCount INTEGER NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
        """)
    }
}
```

## ☁️ Firestore Database (Cloud Storage)

### Collection Structure
```
rostry-firestore/
├── users/                          # User profiles
│   └── {userId}/
│       ├── profile data
│       └── subcollections/
│           ├── fowls/              # User's fowls
│           ├── orders/             # User's orders
│           └── transactions/       # Coin transactions
├── fowls/                          # All fowl entities
│   └── {fowlId}/
│       ├── fowl data
│       └── subcollections/
│           ├── records/            # Health records
│           └── transfers/          # Transfer history
├── chats/                          # Chat conversations
│   └── {chatId}/
│       ├── chat metadata
│       └── messages/               # Chat messages
├── posts/                          # Social posts
├── orders/                         # Purchase orders
├── marketplace/                    # Marketplace listings
└── verifications/                  # KYC verification requests
```

### Document Schemas

#### User Document
```json
{
  "id": "string",
  "email": "string",
  "displayName": "string",
  "profileImageUrl": "string",
  "role": "GENERAL|FARMER|ENTHUSIAST",
  "phoneNumber": "string",
  "location": "string",
  "bio": "string",
  "isKycVerified": "boolean",
  "verificationStatus": "UNVERIFIED|PENDING|VERIFIED|REJECTED",
  "verificationBadges": ["string"],
  "coinBalance": "number",
  "sellerRating": "number",
  "totalSales": "number",
  "isOnline": "boolean",
  "lastSeen": "timestamp",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### Fowl Document
```json
{
  "id": "string",
  "ownerId": "string",
  "name": "string",
  "breed": "string",
  "type": "CHICKEN|DUCK|TURKEY|GOOSE|GUINEA_FOWL|OTHER",
  "gender": "MALE|FEMALE|UNKNOWN",
  "dateOfBirth": "timestamp",
  "motherId": "string",
  "fatherId": "string",
  "status": "string",
  "weight": "number",
  "description": "string",
  "imageUrls": ["string"],
  "healthRecords": [
    {
      "id": "string",
      "date": "timestamp",
      "type": "CHECKUP|VACCINATION|TREATMENT|WEIGHT_CHECK|OTHER",
      "description": "string",
      "veterinarian": "string",
      "medication": "string",
      "notes": "string"
    }
  ],
  "isForSale": "boolean",
  "price": "number",
  "location": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### Order Document
```json
{
  "orderId": "string",
  "buyerId": "string",
  "sellerId": "string",
  "fowlId": "string",
  "quantity": "number",
  "unitPrice": "number",
  "totalAmount": "number",
  "status": "PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED|REFUNDED",
  "paymentMethod": "string",
  "deliveryAddress": "string",
  "deliveryMethod": "PICKUP|DELIVERY|SHIPPING",
  "notes": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Fowls can be read by anyone, written by owner
    match /fowls/{fowlId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.ownerId;
    }
    
    // Orders can be accessed by buyer or seller
    match /orders/{orderId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid == resource.data.buyerId || 
         request.auth.uid == resource.data.sellerId);
    }
    
    // Chat participants can access chat data
    match /chats/{chatId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participantIds;
    }
  }
}
```

## 🔄 Data Synchronization

### Sync Strategy
```kotlin
class DataSyncManager @Inject constructor(
    private val localDatabase: RostryDatabase,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun syncUserData(userId: String) {
        try {
            // Fetch from Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject<User>()
            
            // Update local database
            user?.let { localDatabase.userDao().insertUser(it) }
        } catch (e: Exception) {
            // Handle sync failure
            Log.e("DataSync", "Failed to sync user data", e)
        }
    }
    
    suspend fun syncFowlData(ownerId: String) {
        try {
            // Fetch user's fowls from Firestore
            val fowlsSnapshot = firestore.collection("fowls")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            
            val fowls = fowlsSnapshot.toObjects<Fowl>()
            
            // Update local database
            localDatabase.fowlDao().insertAll(fowls)
        } catch (e: Exception) {
            Log.e("DataSync", "Failed to sync fowl data", e)
        }
    }
}
```

### Conflict Resolution
```kotlin
// Last-write-wins strategy for conflict resolution
suspend fun resolveConflict(localEntity: Entity, remoteEntity: Entity): Entity {
    return if (remoteEntity.updatedAt > localEntity.updatedAt) {
        remoteEntity
    } else {
        localEntity
    }
}
```

## 📊 Performance Considerations

### Query Optimization
- **Indexes**: Strategic indexes on frequently queried fields
- **Pagination**: Limit query results and implement pagination
- **Caching**: Room provides automatic caching for offline access
- **Batch Operations**: Use batch writes for multiple document updates

### Storage Optimization
- **Image Compression**: Compress images before storing in Firebase Storage
- **Data Pruning**: Regular cleanup of old data and logs
- **Selective Sync**: Only sync necessary data based on user activity

---

**This database schema documentation reflects the current implementation of ROSTRY's data layer and should be used as the definitive reference for database operations.**
