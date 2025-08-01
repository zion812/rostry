package com.rio.rostry.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.rio.rostry.data.local.dao.*
import com.rio.rostry.data.model.*

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
        FlockSummary::class,
        // Farm Management System Entities
        Farm::class,
        Flock::class,
        FowlLifecycle::class,
        FowlLineage::class,
        VaccinationRecord::class,
        // Farm Access Management Entities
        FarmAccess::class,
        FarmInvitation::class,
        AccessAuditLog::class,
        PermissionRequest::class,
        InvitationTemplate::class,
        BulkInvitation::class,
        InvitationAnalytics::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RostryDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun fowlDao(): FowlDao
    abstract fun cartDao(): CartDao
    abstract fun postDao(): PostDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun fowlRecordDao(): FowlRecordDao
    abstract fun transferLogDao(): TransferLogDao
    abstract fun marketplaceListingDao(): MarketplaceListingDao
    abstract fun orderDao(): OrderDao
    abstract fun walletDao(): WalletDao
    abstract fun verificationDao(): VerificationDao
    abstract fun showcaseDao(): ShowcaseDao
    abstract fun flockSummaryDao(): FlockSummaryDao
    
    // Farm Management System DAOs
    abstract fun farmDao(): FarmDao
    abstract fun flockDao(): FlockDao
    abstract fun lifecycleDao(): LifecycleDao
    abstract fun lineageDao(): LineageDao
    
    // Farm Access Management DAOs
    abstract fun farmAccessDao(): FarmAccessDao
    abstract fun invitationDao(): InvitationDao
    
    companion object {
        @Volatile
        private var INSTANCE: RostryDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create fowl_records table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS fowl_records (
                        recordId TEXT PRIMARY KEY NOT NULL,
                        fowlId TEXT NOT NULL,
                        recordType TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        details TEXT NOT NULL,
                        proofImageUrl TEXT,
                        weight REAL,
                        temperature REAL,
                        medication TEXT NOT NULL,
                        veterinarian TEXT NOT NULL,
                        cost REAL,
                        notes TEXT NOT NULL,
                        createdBy TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                // Create transfer_logs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transfer_logs (
                        transferId TEXT PRIMARY KEY NOT NULL,
                        fowlId TEXT NOT NULL,
                        giverId TEXT NOT NULL,
                        giverName TEXT NOT NULL,
                        receiverId TEXT NOT NULL,
                        receiverName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        verificationDetails TEXT NOT NULL,
                        rejectionReason TEXT,
                        agreedPrice REAL NOT NULL,
                        currentWeight REAL,
                        recentPhotoUrl TEXT,
                        transferNotes TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        verifiedAt INTEGER,
                        rejectedAt INTEGER
                    )
                """)
                
                // Create marketplace_listings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS marketplace_listings (
                        listingId TEXT PRIMARY KEY NOT NULL,
                        fowlId TEXT NOT NULL,
                        sellerId TEXT NOT NULL,
                        sellerName TEXT NOT NULL,
                        sellerRating REAL NOT NULL,
                        price REAL NOT NULL,
                        purpose TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        featuredImageUrl TEXT NOT NULL,
                        description TEXT NOT NULL,
                        location TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        fowlName TEXT NOT NULL,
                        fowlBreed TEXT NOT NULL,
                        fowlType TEXT NOT NULL,
                        fowlGender TEXT NOT NULL,
                        fowlAge TEXT NOT NULL,
                        motherId TEXT,
                        fatherId TEXT,
                        vaccinationRecords TEXT NOT NULL,
                        healthStatus TEXT NOT NULL,
                        isBreederReady INTEGER NOT NULL
                    )
                """)
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create orders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS orders (
                        orderId TEXT PRIMARY KEY NOT NULL,
                        buyerId TEXT NOT NULL,
                        sellerId TEXT NOT NULL,
                        fowlId TEXT NOT NULL,
                        fowlName TEXT NOT NULL,
                        fowlBreed TEXT NOT NULL,
                        fowlImageUrl TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        basePrice REAL NOT NULL,
                        productTotal REAL NOT NULL,
                        platformFee REAL NOT NULL,
                        handlingCharge REAL NOT NULL,
                        packagingCharge REAL NOT NULL,
                        processingCharge REAL NOT NULL,
                        deliveryCharge REAL NOT NULL,
                        grandTotal REAL NOT NULL,
                        status TEXT NOT NULL,
                        paymentStatus TEXT NOT NULL,
                        paymentIntentId TEXT,
                        deliveryAddress TEXT NOT NULL,
                        estimatedDeliveryDate INTEGER,
                        trackingNumber TEXT,
                        notes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Create wallets table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS wallets (
                        userId TEXT PRIMARY KEY NOT NULL,
                        coinBalance INTEGER NOT NULL,
                        totalCoinsEarned INTEGER NOT NULL,
                        totalCoinsSpent INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """)
                
                // Create coin_transactions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS coin_transactions (
                        transactionId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        relatedEntityId TEXT,
                        relatedEntityType TEXT,
                        balanceBefore INTEGER NOT NULL,
                        balanceAfter INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
                
                // Create verification_requests table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS verification_requests (
                        requestId TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        userName TEXT NOT NULL,
                        userEmail TEXT NOT NULL,
                        verificationType TEXT NOT NULL,
                        entityId TEXT,
                        status TEXT NOT NULL,
                        submittedDocuments TEXT NOT NULL,
                        verificationNotes TEXT NOT NULL,
                        adminNotes TEXT NOT NULL,
                        coinsDeducted INTEGER NOT NULL,
                        submittedAt INTEGER NOT NULL,
                        reviewedAt INTEGER,
                        reviewedBy TEXT
                    )
                """)
                
                // Create showcase_slots table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS showcase_slots (
                        slotId TEXT PRIMARY KEY NOT NULL,
                        category TEXT NOT NULL,
                        fowlId TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        position INTEGER NOT NULL,
                        duration TEXT NOT NULL,
                        coinsSpent INTEGER NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Update users table to add missing columns for monetization features
                // Use a more robust approach to handle schema changes
                
                try {
                    // Get current table schema
                    val cursor = database.query("PRAGMA table_info(users)")
                    val existingColumns = mutableSetOf<String>()
                    
                    while (cursor.moveToNext()) {
                        val nameIndex = cursor.getColumnIndex("name")
                        if (nameIndex >= 0) {
                            existingColumns.add(cursor.getString(nameIndex))
                        }
                    }
                    cursor.close()
                    
                    // Define columns to add with their SQL
                    val columnsToAdd = mapOf(
                        "isKycVerified" to "ALTER TABLE users ADD COLUMN isKycVerified INTEGER NOT NULL DEFAULT 0",
                        "kycDocumentUrl" to "ALTER TABLE users ADD COLUMN kycDocumentUrl TEXT NOT NULL DEFAULT ''",
                        "verificationStatus" to "ALTER TABLE users ADD COLUMN verificationStatus TEXT NOT NULL DEFAULT 'UNVERIFIED'",
                        "verificationBadges" to "ALTER TABLE users ADD COLUMN verificationBadges TEXT NOT NULL DEFAULT '[]'",
                        "coinBalance" to "ALTER TABLE users ADD COLUMN coinBalance INTEGER NOT NULL DEFAULT 0",
                        "totalCoinsEarned" to "ALTER TABLE users ADD COLUMN totalCoinsEarned INTEGER NOT NULL DEFAULT 0",
                        "totalCoinsSpent" to "ALTER TABLE users ADD COLUMN totalCoinsSpent INTEGER NOT NULL DEFAULT 0",
                        "sellerRating" to "ALTER TABLE users ADD COLUMN sellerRating REAL NOT NULL DEFAULT 0.0",
                        "totalSales" to "ALTER TABLE users ADD COLUMN totalSales INTEGER NOT NULL DEFAULT 0",
                        "joinedDate" to "ALTER TABLE users ADD COLUMN joinedDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}",
                        "isOnline" to "ALTER TABLE users ADD COLUMN isOnline INTEGER NOT NULL DEFAULT 0",
                        "lastSeen" to "ALTER TABLE users ADD COLUMN lastSeen INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                    )
                    
                    // Add only missing columns
                    columnsToAdd.forEach { (columnName, sql) ->
                        if (!existingColumns.contains(columnName)) {
                            try {
                                database.execSQL(sql)
                            } catch (e: Exception) {
                                // Log the error but continue with other columns
                                println("Error adding column $columnName: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // If there's any error, we'll let the fallbackToDestructiveMigration handle it
                    throw e
                }
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This migration handles any remaining schema inconsistencies for fowls table
                try {
                    // Get current fowls table schema
                    val cursor = database.query("PRAGMA table_info(fowls)")
                    val existingColumns = mutableSetOf<String>()
                    
                    while (cursor.moveToNext()) {
                        val nameIndex = cursor.getColumnIndex("name")
                        if (nameIndex >= 0) {
                            existingColumns.add(cursor.getString(nameIndex))
                        }
                    }
                    cursor.close()
                    
                    // Define columns that should exist in fowls table
                    val requiredColumns = mapOf(
                        "id" to "TEXT PRIMARY KEY NOT NULL",
                        "ownerId" to "TEXT NOT NULL DEFAULT ''",
                        "name" to "TEXT NOT NULL DEFAULT ''",
                        "breed" to "TEXT NOT NULL DEFAULT ''",
                        "type" to "TEXT NOT NULL DEFAULT 'CHICKEN'",
                        "gender" to "TEXT NOT NULL DEFAULT 'UNKNOWN'",
                        "dateOfBirth" to "INTEGER",
                        "motherId" to "TEXT",
                        "fatherId" to "TEXT",
                        "dateOfHatching" to "INTEGER NOT NULL DEFAULT 0",
                        "initialCount" to "INTEGER",
                        "status" to "TEXT NOT NULL DEFAULT 'Growing'",
                        "weight" to "REAL NOT NULL DEFAULT 0.0",
                        "color" to "TEXT NOT NULL DEFAULT ''",
                        "description" to "TEXT NOT NULL DEFAULT ''",
                        "imageUrls" to "TEXT NOT NULL DEFAULT '[]'",
                        "proofImageUrl" to "TEXT",
                        "healthRecords" to "TEXT NOT NULL DEFAULT '[]'",
                        "isForSale" to "INTEGER NOT NULL DEFAULT 0",
                        "price" to "REAL NOT NULL DEFAULT 0.0",
                        "location" to "TEXT NOT NULL DEFAULT ''",
                        "createdAt" to "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}",
                        "updatedAt" to "INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                    )
                    
                    // Add missing columns to fowls table
                    requiredColumns.forEach { (columnName, columnDef) ->
                        if (!existingColumns.contains(columnName) && columnName != "id") {
                            try {
                                val alterSql = "ALTER TABLE fowls ADD COLUMN $columnName $columnDef"
                                database.execSQL(alterSql)
                            } catch (e: Exception) {
                                println("Error adding column $columnName to fowls: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // If there's any error, we'll let the fallbackToDestructiveMigration handle it
                    println("Error in MIGRATION_4_5: ${e.message}")
                    throw e
                }
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create flock_summary table for dashboard functionality
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS flock_summary (
                        ownerId TEXT PRIMARY KEY NOT NULL,
                        totalFowls INTEGER NOT NULL,
                        chicks INTEGER NOT NULL,
                        juveniles INTEGER NOT NULL,
                        adults INTEGER NOT NULL,
                        breeders INTEGER NOT NULL,
                        forSale INTEGER NOT NULL,
                        sold INTEGER NOT NULL,
                        deceased INTEGER NOT NULL,
                        totalValue REAL NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """)
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Create farms table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS farms (
                            id TEXT PRIMARY KEY NOT NULL,
                            ownerId TEXT NOT NULL,
                            farmName TEXT NOT NULL,
                            location TEXT NOT NULL,
                            description TEXT NOT NULL DEFAULT '',
                            farmType TEXT NOT NULL DEFAULT 'SMALL_SCALE',
                            totalArea REAL NOT NULL DEFAULT 0.0,
                            establishedDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            verificationStatus TEXT NOT NULL DEFAULT 'PENDING',
                            certificationLevel TEXT NOT NULL DEFAULT 'BASIC',
                            certificationDate INTEGER NOT NULL DEFAULT 0,
                            certificationUrls TEXT NOT NULL DEFAULT '[]',
                            contactInfo TEXT,
                            facilities TEXT NOT NULL DEFAULT '[]',
                            operatingLicense TEXT NOT NULL DEFAULT '',
                            isActive INTEGER NOT NULL DEFAULT 1,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create flocks table
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
                            establishedDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            facilityId TEXT,
                            healthStatus TEXT NOT NULL DEFAULT 'HEALTHY',
                            feedingSchedule TEXT,
                            vaccinationSchedule TEXT NOT NULL DEFAULT '[]',
                            productionMetrics TEXT,
                            environmentalConditions TEXT,
                            notes TEXT NOT NULL DEFAULT '',
                            isActive INTEGER NOT NULL DEFAULT 1,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create fowl_lifecycles table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS fowl_lifecycles (
                            id TEXT PRIMARY KEY NOT NULL,
                            fowlId TEXT NOT NULL,
                            farmId TEXT,
                            currentStage TEXT NOT NULL DEFAULT 'EGG',
                            stageStartDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            expectedTransitionDate INTEGER,
                            actualTransitionDate INTEGER,
                            stageProgress REAL NOT NULL DEFAULT 0.0,
                            milestones TEXT NOT NULL DEFAULT '[]',
                            healthMetrics TEXT,
                            growthMetrics TEXT,
                            environmentalFactors TEXT,
                            careInstructions TEXT NOT NULL DEFAULT '[]',
                            notes TEXT NOT NULL DEFAULT '',
                            isActive INTEGER NOT NULL DEFAULT 1,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create fowl_lineages table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS fowl_lineages (
                            id TEXT PRIMARY KEY NOT NULL,
                            fowlId TEXT NOT NULL,
                            farmId TEXT,
                            motherId TEXT,
                            fatherId TEXT,
                            maternalGrandmotherId TEXT,
                            maternalGrandfatherId TEXT,
                            paternalGrandmotherId TEXT,
                            paternalGrandfatherId TEXT,
                            bloodlineId TEXT,
                            bloodlineName TEXT NOT NULL DEFAULT '',
                            generationNumber INTEGER NOT NULL DEFAULT 1,
                            inbreedingCoefficient REAL NOT NULL DEFAULT 0.0,
                            geneticDiversity REAL NOT NULL DEFAULT 0.0,
                            breedingValue REAL NOT NULL DEFAULT 0.0,
                            traits TEXT NOT NULL DEFAULT '[]',
                            healthHistory TEXT NOT NULL DEFAULT '[]',
                            performanceMetrics TEXT,
                            breedingRecommendations TEXT NOT NULL DEFAULT '[]',
                            notes TEXT NOT NULL DEFAULT '',
                            isActive INTEGER NOT NULL DEFAULT 1,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create vaccination_records table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS vaccination_records (
                            id TEXT PRIMARY KEY NOT NULL,
                            flockId TEXT,
                            fowlId TEXT,
                            vaccineName TEXT NOT NULL,
                            vaccineType TEXT NOT NULL,
                            administrationDate INTEGER NOT NULL,
                            nextDueDate INTEGER NOT NULL DEFAULT 0,
                            dosage TEXT NOT NULL DEFAULT '',
                            administrationMethod TEXT NOT NULL DEFAULT 'INJECTION',
                            administeredBy TEXT NOT NULL DEFAULT '',
                            batchNumber TEXT NOT NULL DEFAULT '',
                            manufacturer TEXT NOT NULL DEFAULT '',
                            expiryDate INTEGER NOT NULL DEFAULT 0,
                            storageTemperature TEXT NOT NULL DEFAULT '',
                            proofImageUrl TEXT NOT NULL DEFAULT '',
                            notes TEXT NOT NULL DEFAULT '',
                            sideEffects TEXT NOT NULL DEFAULT '',
                            efficacy REAL NOT NULL DEFAULT 0.0,
                            cost REAL NOT NULL DEFAULT 0.0,
                            veterinarianApproval INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create farm_access table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS farm_access (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL,
                            userId TEXT NOT NULL,
                            role TEXT NOT NULL,
                            permissions TEXT NOT NULL DEFAULT '[]',
                            invitedBy TEXT NOT NULL,
                            invitedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            acceptedAt INTEGER,
                            status TEXT NOT NULL DEFAULT 'PENDING',
                            expiresAt INTEGER,
                            isActive INTEGER NOT NULL DEFAULT 1,
                            lastAccessedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            accessNotes TEXT NOT NULL DEFAULT '',
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create farm_invitations table
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
                            sentAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            expiresAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis() + 604800000},
                            respondedAt INTEGER,
                            remindersSent INTEGER NOT NULL DEFAULT 0,
                            lastReminderAt INTEGER,
                            maxReminders INTEGER NOT NULL DEFAULT 3,
                            allowCustomRole INTEGER NOT NULL DEFAULT 0,
                            requiresApproval INTEGER NOT NULL DEFAULT 0,
                            approvedBy TEXT,
                            approvedAt INTEGER,
                            metadata TEXT,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create access_audit_log table
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
                            timestamp INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create permission_requests table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS permission_requests (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL,
                            requesterId TEXT NOT NULL,
                            requestedPermissions TEXT NOT NULL DEFAULT '[]',
                            reason TEXT NOT NULL,
                            urgencyLevel TEXT NOT NULL DEFAULT 'NORMAL',
                            requestedDuration INTEGER,
                            status TEXT NOT NULL DEFAULT 'PENDING',
                            reviewedBy TEXT,
                            reviewedAt INTEGER,
                            reviewNotes TEXT NOT NULL DEFAULT '',
                            expiresAt INTEGER,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create invitation_templates table
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
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create bulk_invitations table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS bulk_invitations (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT NOT NULL DEFAULT '',
                            inviterUserId TEXT NOT NULL,
                            templateId TEXT,
                            defaultRole TEXT NOT NULL,
                            inviteeEmails TEXT NOT NULL DEFAULT '[]',
                            customMessage TEXT NOT NULL DEFAULT '',
                            status TEXT NOT NULL DEFAULT 'PENDING',
                            totalInvitations INTEGER NOT NULL DEFAULT 0,
                            sentCount INTEGER NOT NULL DEFAULT 0,
                            acceptedCount INTEGER NOT NULL DEFAULT 0,
                            rejectedCount INTEGER NOT NULL DEFAULT 0,
                            expiredCount INTEGER NOT NULL DEFAULT 0,
                            startedAt INTEGER,
                            completedAt INTEGER,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """)

                    // Create invitation_analytics table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS invitation_analytics (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL,
                            invitationId TEXT NOT NULL,
                            event TEXT NOT NULL,
                            timestamp INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            userAgent TEXT NOT NULL DEFAULT '',
                            ipAddress TEXT NOT NULL DEFAULT '',
                            deviceType TEXT NOT NULL DEFAULT '',
                            location TEXT NOT NULL DEFAULT '',
                            additionalData TEXT NOT NULL DEFAULT '{}'
                        )
                    """)

                    // Create indexes for performance
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_farms_owner ON farms(ownerId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_flocks_farm ON flocks(farmId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_fowl_lifecycles_fowl ON fowl_lifecycles(fowlId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_fowl_lineages_fowl ON fowl_lineages(fowlId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_farm_access_farm_user ON farm_access(farmId, userId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_farm_invitations_farm ON farm_invitations(farmId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_farm_invitations_email ON farm_invitations(inviteeEmail)")

                } catch (e: Exception) {
                    // Log error but allow fallback to destructive migration
                    println("Error in MIGRATION_6_7: ${e.message}")
                    throw e
                }
            }
        }
        
        fun getDatabase(context: Context): RostryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RostryDatabase::class.java,
                    "rostry_database"
                )
                .fallbackToDestructiveMigration() // Allow destructive migration for development
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}