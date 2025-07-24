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
        FlockSummary::class
    ],
    version = 6,
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
        
        fun getDatabase(context: Context): RostryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RostryDatabase::class.java,
                    "rostry_database"
                )
                .fallbackToDestructiveMigration() // Allow destructive migration for development
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}