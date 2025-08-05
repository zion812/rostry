package com.rio.rostry.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rostry.data.local.dao.CartDao
import com.rio.rostry.data.local.dao.ChatDao
import com.rio.rostry.data.local.dao.FarmAccessDao
import com.rio.rostry.data.local.dao.FarmDao
import com.rio.rostry.data.local.dao.FlockDao
import com.rio.rostry.data.local.dao.FlockSummaryDao
import com.rio.rostry.data.local.dao.FowlDao
import com.rio.rostry.data.local.dao.FowlRecordDao
import com.rio.rostry.data.local.dao.InvitationDao
import com.rio.rostry.data.local.dao.LifecycleDao
import com.rio.rostry.data.local.dao.LineageDao
import com.rio.rostry.data.local.dao.MarketplaceListingDao
import com.rio.rostry.data.local.dao.MessageDao
import com.rio.rostry.data.local.dao.OrderDao
import com.rio.rostry.data.local.dao.PostDao
import com.rio.rostry.data.local.dao.ShowcaseDao
import com.rio.rostry.data.local.dao.TransferLogDao
import com.rio.rostry.data.local.dao.UserDao
import com.rio.rostry.data.local.dao.VerificationDao
import com.rio.rostry.data.local.dao.WalletDao
import com.rio.rostry.data.model.AccessAuditLog
import com.rio.rostry.data.model.Bloodline
import com.rio.rostry.data.model.BulkInvitation
import com.rio.rostry.data.model.CartItem
import com.rio.rostry.data.model.Chat
import com.rio.rostry.data.model.CoinTransaction
import com.rio.rostry.data.model.Farm
import com.rio.rostry.data.model.FarmAccess
import com.rio.rostry.data.model.FarmInvitation
import com.rio.rostry.data.model.Flock
import com.rio.rostry.data.model.FlockSummary
import com.rio.rostry.data.model.Fowl
import com.rio.rostry.data.model.FowlLifecycle
import com.rio.rostry.data.model.FowlLineage
import com.rio.rostry.data.model.FowlRecord
import com.rio.rostry.data.model.GrowthMetric
import com.rio.rostry.data.model.HealthAlert
import com.rio.rostry.data.model.InvitationAnalytics
import com.rio.rostry.data.model.InvitationTemplate
import com.rio.rostry.data.model.LifecycleMilestone
import com.rio.rostry.data.model.MarketplaceListing
import com.rio.rostry.data.model.Message
import com.rio.rostry.data.model.Order
import com.rio.rostry.data.model.PermissionRequest
import com.rio.rostry.data.model.Post
import com.rio.rostry.data.model.ShowcaseSlot
import com.rio.rostry.data.model.TransferLog
import com.rio.rostry.data.model.UpcomingTask
import com.rio.rostry.data.model.User
import com.rio.rostry.data.model.VaccinationRecord
import com.rio.rostry.data.model.VerificationRequest
import com.rio.rostry.data.model.Wallet

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
        Bloodline::class,
        // Farm Access Management Entities
        FarmAccess::class,
        FarmInvitation::class,
        AccessAuditLog::class,
        PermissionRequest::class,
        InvitationTemplate::class,
        BulkInvitation::class,
        InvitationAnalytics::class,
        // Dashboard Entities
        HealthAlert::class,
        UpcomingTask::class,
        GrowthMetric::class,
        LifecycleMilestone::class
    ],
    version = 12,
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
                database.execSQL(
                    """
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
                """
                )

                // Create transfer_logs table
                database.execSQL(
                    """
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
                """
                )

                // Create marketplace_listings table
                database.execSQL(
                    """
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
                """
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create orders table
                database.execSQL(
                    """
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
                """
                )

                // Create wallets table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wallets (
                        userId TEXT PRIMARY KEY NOT NULL,
                        coinBalance INTEGER NOT NULL,
                        totalCoinsEarned INTEGER NOT NULL,
                        totalCoinsSpent INTEGER NOT NULL,
                        lastUpdated INTEGER NOT NULL
                    )
                """
                )

                // Create coin_transactions table
                database.execSQL(
                    """
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
                """
                )

                // Create verification_requests table
                database.execSQL(
                    """
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
                """
                )

                // Create showcase_slots table
                database.execSQL(
                    """
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
                """
                )
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
                database.execSQL(
                    """
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
                """
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Create farms table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create flocks table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create fowl_lifecycles table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create fowl_lineages table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create vaccination_records table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create farm_access table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create farm_invitations table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create access_audit_log table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create permission_requests table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create invitation_templates table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create bulk_invitations table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create invitation_analytics table
                    database.execSQL(
                        """
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
                    """
                    )

                    // Create bloodlines table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS bloodlines (
                            id TEXT PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            originFowlId TEXT NOT NULL,
                            founderGeneration INTEGER NOT NULL DEFAULT 1,
                            characteristics TEXT NOT NULL DEFAULT '[]',
                            totalGenerations INTEGER NOT NULL DEFAULT 1,
                            activeBreeders INTEGER NOT NULL DEFAULT 0,
                            totalOffspring INTEGER NOT NULL DEFAULT 0,
                            performanceMetrics TEXT,
                            geneticDiversity REAL NOT NULL DEFAULT 1.0,
                            breedingGoals TEXT NOT NULL DEFAULT '[]',
                            certificationLevel TEXT NOT NULL DEFAULT 'UNVERIFIED',
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """
                    )

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Create health_alerts table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS health_alerts (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL DEFAULT '',
                            flockId TEXT,
                            fowlId TEXT,
                            title TEXT NOT NULL DEFAULT '',
                            description TEXT NOT NULL DEFAULT '',
                            severity TEXT NOT NULL DEFAULT 'LOW',
                            category TEXT NOT NULL DEFAULT 'HEALTH',
                            isRead INTEGER NOT NULL DEFAULT 0,
                            isResolved INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            resolvedAt INTEGER,
                            actionRequired INTEGER NOT NULL DEFAULT 0,
                            actionUrl TEXT,
                            metadata TEXT NOT NULL DEFAULT '{}'
                        )
                    """
                    )

                    // Create upcoming_tasks table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS upcoming_tasks (
                            id TEXT PRIMARY KEY NOT NULL,
                            farmId TEXT NOT NULL DEFAULT '',
                            flockId TEXT,
                            fowlId TEXT,
                            title TEXT NOT NULL DEFAULT '',
                            description TEXT NOT NULL DEFAULT '',
                            taskType TEXT NOT NULL DEFAULT 'GENERAL',
                            priority TEXT NOT NULL DEFAULT 'MEDIUM',
                            status TEXT NOT NULL DEFAULT 'PENDING',
                            assignedTo TEXT,
                            assignedBy TEXT NOT NULL DEFAULT '',
                            dueDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            estimatedDuration INTEGER NOT NULL DEFAULT 30,
                            isRecurring INTEGER NOT NULL DEFAULT 0,
                            recurringPattern TEXT,
                            completedAt INTEGER,
                            completedBy TEXT,
                            notes TEXT NOT NULL DEFAULT '',
                            attachments TEXT NOT NULL DEFAULT '[]',
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """
                    )

                    // Create growth_metrics table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS growth_metrics (
                            id TEXT PRIMARY KEY NOT NULL,
                            fowlId TEXT NOT NULL DEFAULT '',
                            flockId TEXT,
                            measurementDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            weight REAL NOT NULL DEFAULT 0.0,
                            height REAL,
                            length REAL,
                            wingSpan REAL,
                            bodyConditionScore INTEGER NOT NULL DEFAULT 5,
                            notes TEXT NOT NULL DEFAULT '',
                            measuredBy TEXT NOT NULL DEFAULT '',
                            imageUrl TEXT,
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """
                    )

                    // Create lifecycle_milestones table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS lifecycle_milestones (
                            id TEXT PRIMARY KEY NOT NULL,
                            fowlId TEXT NOT NULL DEFAULT '',
                            stage TEXT NOT NULL DEFAULT 'EGG',
                            milestone TEXT NOT NULL DEFAULT '',
                            description TEXT NOT NULL DEFAULT '',
                            achievedDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                            expectedDate INTEGER,
                            isOnSchedule INTEGER NOT NULL DEFAULT 1,
                            notes TEXT NOT NULL DEFAULT '',
                            imageUrl TEXT,
                            recordedBy TEXT NOT NULL DEFAULT '',
                            createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                        )
                    """
                    )

                    // Create indexes for performance
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_health_alerts_farm ON health_alerts(farmId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_upcoming_tasks_farm ON upcoming_tasks(farmId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_growth_metrics_fowl ON growth_metrics(fowlId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS idx_lifecycle_milestones_fowl ON lifecycle_milestones(fowlId)")

                } catch (e: Exception) {
                    println("Error in MIGRATION_7_8: ${e.message}")
                    throw e
                }
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Fix MIGRATION_3_4 column detection logic
                    val cursor = database.query("PRAGMA table_info(users)")
                    val existingColumns = mutableSetOf<String>()

                    while (cursor.moveToNext()) {
                        val columnName = cursor.getString(1) // Column name is at index 1 in PRAGMA table_info
                        existingColumns.add(columnName)
                    }
                    cursor.close()

                    // Add missing User columns with proper defaults
                    val columnsToAdd = mapOf(
                        "phoneNumber" to "ALTER TABLE users ADD COLUMN phoneNumber TEXT NOT NULL DEFAULT ''",
                        "location" to "ALTER TABLE users ADD COLUMN location TEXT NOT NULL DEFAULT ''",
                        "bio" to "ALTER TABLE users ADD COLUMN bio TEXT NOT NULL DEFAULT ''",
                        "createdAt" to "ALTER TABLE users ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}",
                        "updatedAt" to "ALTER TABLE users ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                    )

                    columnsToAdd.forEach { (columnName, sql) ->
                        if (!existingColumns.contains(columnName)) {
                            try {
                                database.execSQL(sql)
                            } catch (e: Exception) {
                                println("Error adding column $columnName: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error in MIGRATION_8_9: ${e.message}")
                    throw e
                }
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Complete User table recreation with all 22 fields
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS users_new (
                            id TEXT PRIMARY KEY NOT NULL,
                            email TEXT NOT NULL DEFAULT '',
                            displayName TEXT NOT NULL DEFAULT '',
                            profileImageUrl TEXT NOT NULL DEFAULT '',
                            roleId TEXT NOT NULL DEFAULT 'consumer',
                            phoneNumber TEXT NOT NULL DEFAULT '',
                            location TEXT NOT NULL DEFAULT '',
                            bio TEXT NOT NULL DEFAULT '',
                            isKycVerified INTEGER NOT NULL DEFAULT 0,
                            kycDocumentUrl TEXT NOT NULL DEFAULT '',
                            verificationStatus TEXT NOT NULL DEFAULT 'PENDING',
                            verificationBadges TEXT NOT NULL DEFAULT '[]',
                            coinBalance INTEGER NOT NULL DEFAULT 0,
                            totalCoinsEarned INTEGER NOT NULL DEFAULT 0,
                            totalCoinsSpent INTEGER NOT NULL DEFAULT 0,
                            sellerRating REAL NOT NULL DEFAULT 0.0,
                            totalSales INTEGER NOT NULL DEFAULT 0,
                            joinedDate INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL DEFAULT 0,
                            updatedAt INTEGER NOT NULL DEFAULT 0,
                            isOnline INTEGER NOT NULL DEFAULT 0,
                            lastSeen INTEGER NOT NULL DEFAULT 0
                        )
                    """)

                    // Safe data migration with COALESCE for missing columns
                    database.execSQL("""
                        INSERT INTO users_new (
                            id, email, displayName, profileImageUrl, roleId,
                            phoneNumber, location, bio, isKycVerified, kycDocumentUrl,
                            verificationStatus, verificationBadges, coinBalance, totalCoinsEarned, totalCoinsSpent,
                            sellerRating, totalSales, joinedDate, createdAt, updatedAt,
                            isOnline, lastSeen
                        )
                        SELECT
                            id,
                            COALESCE(email, '') as email,
                            COALESCE(displayName, '') as displayName,
                            COALESCE(profileImageUrl, '') as profileImageUrl,
                            COALESCE(roleId, 'consumer') as roleId,
                            COALESCE(phoneNumber, '') as phoneNumber,
                            COALESCE(location, '') as location,
                            COALESCE(bio, '') as bio,
                            COALESCE(isKycVerified, 0) as isKycVerified,
                            COALESCE(kycDocumentUrl, '') as kycDocumentUrl,
                            COALESCE(verificationStatus, 'PENDING') as verificationStatus,
                            COALESCE(verificationBadges, '[]') as verificationBadges,
                            COALESCE(coinBalance, 0) as coinBalance,
                            COALESCE(totalCoinsEarned, 0) as totalCoinsEarned,
                            COALESCE(totalCoinsSpent, 0) as totalCoinsSpent,
                            COALESCE(sellerRating, 0.0) as sellerRating,
                            COALESCE(totalSales, 0) as totalSales,
                            COALESCE(joinedDate, ${System.currentTimeMillis()}) as joinedDate,
                            COALESCE(createdAt, ${System.currentTimeMillis()}) as createdAt,
                            COALESCE(updatedAt, ${System.currentTimeMillis()}) as updatedAt,
                            COALESCE(isOnline, 0) as isOnline,
                            COALESCE(lastSeen, ${System.currentTimeMillis()}) as lastSeen
                        FROM users
                    """)

                    // Replace old table
                    database.execSQL("DROP TABLE users")
                    database.execSQL("ALTER TABLE users_new RENAME TO users")

                } catch (e: Exception) {
                    println("Error in MIGRATION_9_10: ${e.message}")
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}