# ROSTRY Database Schema Documentation

> **Version**: 7.0
> **Last Updated**: 2025-01-08
> **Database Type**: Hybrid (Room + Firestore)
> **Status**: Current Implementation with Farm Management System

## 📋 Overview

ROSTRY uses a hybrid database architecture combining Room (local SQLite) for offline capabilities and Firebase Firestore (cloud NoSQL) for real-time synchronization and backup. The system now includes comprehensive farm management, access control, and collaboration features.

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
        // Core User & Social Entities
        User::class,
        Post::class,
        Chat::class,
        Message::class,

        // Fowl Management Entities
        Fowl::class,
        FowlRecord::class,
        FowlLifecycle::class,
        FowlLineage::class,

        // Farm Management Entities
        Farm::class,
        Flock::class,
        FlockSummary::class,

        // Farm Access & Collaboration
        FarmAccess::class,
        FarmInvitation::class,
        InvitationTemplate::class,
        BulkInvitation::class,
        AccessAuditLog::class,
        PermissionRequest::class,
        InvitationAnalytics::class,

        // Marketplace & Commerce
        CartItem::class,
        MarketplaceListing::class,
        Order::class,
        TransferLog::class,

        // Wallet & Verification
        Wallet::class,
        CoinTransaction::class,
        VerificationRequest::class,
        ShowcaseSlot::class
    ],
    version = 7,
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

#### 8. Farm Table
```sql
CREATE TABLE farms (
    id TEXT PRIMARY KEY NOT NULL,
    ownerId TEXT NOT NULL,
    farmName TEXT NOT NULL,
    location TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    farmType TEXT NOT NULL DEFAULT 'SMALL_SCALE',
    totalArea REAL NOT NULL DEFAULT 0.0,
    establishedDate INTEGER NOT NULL,
    verificationStatus TEXT NOT NULL DEFAULT 'PENDING',
    certificationLevel TEXT NOT NULL DEFAULT 'BASIC',
    certificationDate INTEGER NOT NULL DEFAULT 0,
    certificationUrls TEXT NOT NULL DEFAULT '[]',
    contactInfo TEXT,
    facilities TEXT NOT NULL DEFAULT '[]',
    isActive INTEGER NOT NULL DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,

    FOREIGN KEY(ownerId) REFERENCES users(id) ON DELETE CASCADE
);
```

#### 9. Flock Table
```sql
CREATE TABLE flocks (
    id TEXT PRIMARY KEY NOT NULL,
    farmId TEXT NOT NULL,
    flockName TEXT NOT NULL,
    flockType TEXT NOT NULL,
    breed TEXT NOT NULL,
    totalCount INTEGER NOT NULL DEFAULT 0,
    activeCount INTEGER NOT NULL DEFAULT 0,
    maleCount INTEGER NOT NULL DEFAULT 0,
    femaleCount INTEGER NOT NULL DEFAULT 0,
    averageAge INTEGER NOT NULL DEFAULT 0,
    establishedDate INTEGER NOT NULL,
    facilityId TEXT,
    healthStatus TEXT NOT NULL DEFAULT 'HEALTHY',
    feedingSchedule TEXT,
    vaccinationSchedule TEXT NOT NULL DEFAULT '[]',
    productionMetrics TEXT,
    environmentalConditions TEXT,
    notes TEXT NOT NULL DEFAULT '',
    isActive INTEGER NOT NULL DEFAULT 1,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,

    FOREIGN KEY(farmId) REFERENCES farms(id) ON DELETE CASCADE
);
```

#### 10. Farm Access Table
```sql
CREATE TABLE farm_access (
    id TEXT PRIMARY KEY NOT NULL,
    farmId TEXT NOT NULL,
    userId TEXT NOT NULL,
    role TEXT NOT NULL,
    permissions TEXT NOT NULL DEFAULT '[]',
    invitedBy TEXT NOT NULL,
    invitedAt INTEGER NOT NULL,
    acceptedAt INTEGER,
    status TEXT NOT NULL DEFAULT 'PENDING',
    expiresAt INTEGER,
    isActive INTEGER NOT NULL DEFAULT 1,
    lastAccessedAt INTEGER NOT NULL,
    accessNotes TEXT NOT NULL DEFAULT '',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,

    FOREIGN KEY(farmId) REFERENCES farms(id) ON DELETE CASCADE,
    FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(invitedBy) REFERENCES users(id)
);
```

### Database Indexes
```sql
-- Core entity indexes
CREATE INDEX idx_fowls_owner ON fowls(ownerId);
CREATE INDEX idx_fowls_for_sale ON fowls(isForSale);
CREATE INDEX idx_fowls_type ON fowls(type);
CREATE INDEX idx_messages_chat ON messages(chatId);
CREATE INDEX idx_orders_buyer ON orders(buyerId);
CREATE INDEX idx_orders_seller ON orders(sellerId);
CREATE INDEX idx_posts_author ON posts(authorId);

-- Farm management indexes
CREATE INDEX idx_farms_owner ON farms(ownerId);
CREATE INDEX idx_farms_active ON farms(isActive);
CREATE INDEX idx_flocks_farm ON flocks(farmId);
CREATE INDEX idx_flocks_type ON flocks(flockType);
CREATE INDEX idx_flocks_health ON flocks(healthStatus);

-- Farm access indexes
CREATE INDEX idx_farm_access_user_farm ON farm_access(userId, farmId);
CREATE INDEX idx_farm_access_farm ON farm_access(farmId);
CREATE INDEX idx_farm_access_status ON farm_access(status);
CREATE INDEX idx_farm_invitations_email ON farm_invitations(inviteeEmail);
CREATE INDEX idx_farm_invitations_farm ON farm_invitations(farmId);

-- Lifecycle and lineage indexes
CREATE INDEX idx_fowl_lifecycle_fowl ON fowl_lifecycle(fowlId);
CREATE INDEX idx_fowl_lifecycle_farm ON fowl_lifecycle(farmId);
CREATE INDEX idx_fowl_lineage_fowl ON fowl_lineage(fowlId);
CREATE INDEX idx_fowl_lineage_parents ON fowl_lineage(motherId, fatherId);

-- Audit and analytics indexes
CREATE INDEX idx_access_audit_farm ON access_audit_log(farmId);
CREATE INDEX idx_access_audit_timestamp ON access_audit_log(timestamp);
CREATE INDEX idx_permission_requests_farm ON permission_requests(farmId);
```

### Migration History
```kotlin
// Migration from version 6 to 7 - Farm Management System
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Farm Management Tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS farms (
                id TEXT PRIMARY KEY NOT NULL,
                ownerId TEXT NOT NULL,
                farmName TEXT NOT NULL,
                location TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                farmType TEXT NOT NULL DEFAULT 'SMALL_SCALE',
                totalArea REAL NOT NULL DEFAULT 0.0,
                establishedDate INTEGER NOT NULL,
                verificationStatus TEXT NOT NULL DEFAULT 'PENDING',
                certificationLevel TEXT NOT NULL DEFAULT 'BASIC',
                certificationDate INTEGER NOT NULL DEFAULT 0,
                certificationUrls TEXT NOT NULL DEFAULT '[]',
                contactInfo TEXT,
                facilities TEXT NOT NULL DEFAULT '[]',
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS flocks (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                flockName TEXT NOT NULL,
                flockType TEXT NOT NULL,
                breed TEXT NOT NULL,
                totalCount INTEGER NOT NULL DEFAULT 0,
                activeCount INTEGER NOT NULL DEFAULT 0,
                maleCount INTEGER NOT NULL DEFAULT 0,
                femaleCount INTEGER NOT NULL DEFAULT 0,
                averageAge INTEGER NOT NULL DEFAULT 0,
                establishedDate INTEGER NOT NULL,
                facilityId TEXT,
                healthStatus TEXT NOT NULL DEFAULT 'HEALTHY',
                feedingSchedule TEXT,
                vaccinationSchedule TEXT NOT NULL DEFAULT '[]',
                productionMetrics TEXT,
                environmentalConditions TEXT,
                notes TEXT NOT NULL DEFAULT '',
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        // Farm Access Management Tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_access (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                userId TEXT NOT NULL,
                role TEXT NOT NULL,
                permissions TEXT NOT NULL DEFAULT '[]',
                invitedBy TEXT NOT NULL,
                invitedAt INTEGER NOT NULL,
                acceptedAt INTEGER,
                status TEXT NOT NULL DEFAULT 'PENDING',
                expiresAt INTEGER,
                isActive INTEGER NOT NULL DEFAULT 1,
                lastAccessedAt INTEGER NOT NULL,
                accessNotes TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS farm_invitations (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                farmName TEXT NOT NULL,
                inviterUserId TEXT NOT NULL,
                inviterName TEXT NOT NULL,
                inviterEmail TEXT NOT NULL,
                inviteeEmail TEXT NOT NULL,
                inviteeUserId TEXT,
                proposedRole TEXT NOT NULL,
                customPermissions TEXT NOT NULL DEFAULT '[]',
                invitationMessage TEXT NOT NULL DEFAULT '',
                invitationCode TEXT NOT NULL,
                invitationLink TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'SENT',
                priority TEXT NOT NULL DEFAULT 'NORMAL',
                sentAt INTEGER NOT NULL,
                expiresAt INTEGER NOT NULL,
                respondedAt INTEGER,
                remindersSent INTEGER NOT NULL DEFAULT 0,
                lastReminderAt INTEGER,
                maxReminders INTEGER NOT NULL DEFAULT 3,
                allowCustomRole INTEGER NOT NULL DEFAULT 0,
                requiresApproval INTEGER NOT NULL DEFAULT 0,
                approvedBy TEXT,
                approvedAt INTEGER,
                metadata TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        // Additional Farm Management Tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS invitation_templates (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                farmId TEXT NOT NULL,
                defaultRole TEXT NOT NULL,
                defaultPermissions TEXT NOT NULL DEFAULT '[]',
                messageTemplate TEXT NOT NULL,
                subjectTemplate TEXT NOT NULL DEFAULT 'Invitation to join {farmName}',
                expirationDays INTEGER NOT NULL DEFAULT 7,
                maxReminders INTEGER NOT NULL DEFAULT 3,
                requiresApproval INTEGER NOT NULL DEFAULT 0,
                isActive INTEGER NOT NULL DEFAULT 1,
                usageCount INTEGER NOT NULL DEFAULT 0,
                createdBy TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS bulk_invitations (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                name TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                inviterUserId TEXT NOT NULL,
                templateId TEXT,
                defaultRole TEXT NOT NULL,
                inviteeEmails TEXT NOT NULL,
                customMessage TEXT NOT NULL DEFAULT '',
                status TEXT NOT NULL DEFAULT 'PENDING',
                totalInvitations INTEGER NOT NULL,
                sentCount INTEGER NOT NULL DEFAULT 0,
                acceptedCount INTEGER NOT NULL DEFAULT 0,
                rejectedCount INTEGER NOT NULL DEFAULT 0,
                expiredCount INTEGER NOT NULL DEFAULT 0,
                startedAt INTEGER,
                completedAt INTEGER,
                createdAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS access_audit_log (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                targetUserId TEXT NOT NULL,
                actionPerformedBy TEXT NOT NULL,
                action TEXT NOT NULL,
                previousRole TEXT,
                newRole TEXT,
                previousPermissions TEXT NOT NULL DEFAULT '[]',
                newPermissions TEXT NOT NULL DEFAULT '[]',
                reason TEXT NOT NULL DEFAULT '',
                ipAddress TEXT NOT NULL DEFAULT '',
                userAgent TEXT NOT NULL DEFAULT '',
                timestamp INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS permission_requests (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                requesterId TEXT NOT NULL,
                requestedPermissions TEXT NOT NULL,
                reason TEXT NOT NULL,
                urgencyLevel TEXT NOT NULL DEFAULT 'NORMAL',
                requestedDuration INTEGER,
                status TEXT NOT NULL DEFAULT 'PENDING',
                reviewedBy TEXT,
                reviewedAt INTEGER,
                reviewNotes TEXT NOT NULL DEFAULT '',
                expiresAt INTEGER,
                createdAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS invitation_analytics (
                id TEXT PRIMARY KEY NOT NULL,
                farmId TEXT NOT NULL,
                totalInvitations INTEGER NOT NULL DEFAULT 0,
                sentInvitations INTEGER NOT NULL DEFAULT 0,
                acceptedInvitations INTEGER NOT NULL DEFAULT 0,
                rejectedInvitations INTEGER NOT NULL DEFAULT 0,
                expiredInvitations INTEGER NOT NULL DEFAULT 0,
                averageResponseTime INTEGER NOT NULL DEFAULT 0,
                acceptanceRate REAL NOT NULL DEFAULT 0.0,
                lastCalculated INTEGER NOT NULL
            )
        """)

        // Enhanced Lifecycle and Lineage Tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS fowl_lifecycle (
                id TEXT PRIMARY KEY NOT NULL,
                fowlId TEXT NOT NULL,
                farmId TEXT,
                currentStage TEXT NOT NULL,
                stageStartDate INTEGER NOT NULL,
                expectedNextStage TEXT,
                expectedStageDate INTEGER,
                growthMetrics TEXT,
                healthMetrics TEXT,
                productionMetrics TEXT,
                notes TEXT NOT NULL DEFAULT '',
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS fowl_lineage (
                id TEXT PRIMARY KEY NOT NULL,
                fowlId TEXT NOT NULL,
                farmId TEXT,
                motherId TEXT,
                fatherId TEXT,
                generation INTEGER NOT NULL DEFAULT 1,
                bloodlineId TEXT,
                breedingValue REAL NOT NULL DEFAULT 0.0,
                geneticTraits TEXT NOT NULL DEFAULT '[]',
                breedingHistory TEXT NOT NULL DEFAULT '[]',
                lineageNotes TEXT NOT NULL DEFAULT '',
                isVerified INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)

        // Create indexes for performance
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_farms_owner ON farms(ownerId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_flocks_farm ON flocks(farmId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_farm_access_user_farm ON farm_access(userId, farmId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_farm_invitations_email ON farm_invitations(inviteeEmail)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_fowl_lifecycle_fowl ON fowl_lifecycle(fowlId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_fowl_lineage_fowl ON fowl_lineage(fowlId)")
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
│           ├── transfers/          # Transfer history
│           ├── lifecycle/          # Lifecycle tracking
│           └── lineage/            # Breeding lineage
├── farms/                          # Farm entities
│   └── {farmId}/
│       ├── farm data
│       └── subcollections/
│           ├── flocks/             # Farm flocks
│           ├── access/             # Access control
│           ├── invitations/        # Farm invitations
│           └── analytics/          # Farm analytics
├── farm_access/                    # Farm access records
├── farm_invitations/               # Farm invitation system
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
