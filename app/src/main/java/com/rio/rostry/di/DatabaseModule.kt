package com.rio.rostry.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rio.rostry.data.local.RostryDatabase
import com.rio.rostry.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Database query callback for monitoring database operations in debug builds
 */
private class DatabaseQueryCallback : RoomDatabase.QueryCallback {
    override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
        // Only log in debug builds - using try-catch for BuildConfig safety
        try {
            val isDebug = Class.forName("com.rio.rostry.BuildConfig")
                .getField("DEBUG")
                .getBoolean(null)
            if (isDebug) {
                Log.d("RostryDB", "Query: $sqlQuery")
            }
        } catch (e: Exception) {
            // Fallback: assume debug if BuildConfig is not available
            Log.d("RostryDB", "Query: $sqlQuery")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideRostryDatabase(@ApplicationContext context: Context): RostryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RostryDatabase::class.java,
            "rostry_database"
        )
        .addMigrations(
            RostryDatabase.MIGRATION_1_2,
            RostryDatabase.MIGRATION_2_3,
            RostryDatabase.MIGRATION_3_4,
            RostryDatabase.MIGRATION_4_5,
            RostryDatabase.MIGRATION_5_6,
            RostryDatabase.MIGRATION_6_7,
            RostryDatabase.MIGRATION_7_8,
            RostryDatabase.MIGRATION_8_9,
            RostryDatabase.MIGRATION_9_10
        )
        .apply {
            // Only add query callback in debug builds - with safe BuildConfig access
            try {
                val isDebug = Class.forName("com.rio.rostry.BuildConfig")
                    .getField("DEBUG")
                    .getBoolean(null)
                if (isDebug) {
                    setQueryCallback(
                        DatabaseQueryCallback(), 
                        java.util.concurrent.Executors.newSingleThreadExecutor()
                    )
                }
            } catch (e: Exception) {
                // BuildConfig not available, skip query callback
                Log.w("RostryDB", "BuildConfig not available, skipping query callback")
            }
        }
        .enableMultiInstanceInvalidation()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.i("RostryDB", "Database created successfully")
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.i("RostryDB", "Database opened successfully")
            }
        })
        // Note: fallbackToDestructiveMigration() removed for production safety
        .build()
    }
    
    @Provides
    fun provideUserDao(database: RostryDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideFowlDao(database: RostryDatabase): FowlDao {
        return database.fowlDao()
    }
    
    @Provides
    fun provideCartDao(database: RostryDatabase): CartDao {
        return database.cartDao()
    }
    
    @Provides
    fun providePostDao(database: RostryDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    fun provideChatDao(database: RostryDatabase): ChatDao {
        return database.chatDao()
    }
    
    @Provides
    fun provideMessageDao(database: RostryDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideFowlRecordDao(database: RostryDatabase): FowlRecordDao {
        return database.fowlRecordDao()
    }
    
    @Provides
    fun provideTransferLogDao(database: RostryDatabase): TransferLogDao {
        return database.transferLogDao()
    }
    
    @Provides
    fun provideMarketplaceListingDao(database: RostryDatabase): MarketplaceListingDao {
        return database.marketplaceListingDao()
    }
    
    @Provides
    fun provideOrderDao(database: RostryDatabase): OrderDao {
        return database.orderDao()
    }
    
    @Provides
    fun provideWalletDao(database: RostryDatabase): WalletDao {
        return database.walletDao()
    }
    
    @Provides
    fun provideVerificationDao(database: RostryDatabase): VerificationDao {
        return database.verificationDao()
    }
    
    @Provides
    fun provideShowcaseDao(database: RostryDatabase): ShowcaseDao {
        return database.showcaseDao()
    }
    
    @Provides
    fun provideFlockSummaryDao(database: RostryDatabase): FlockSummaryDao {
        return database.flockSummaryDao()
    }
    
    // Farm Management System DAOs
    @Provides
    fun provideFarmDao(database: RostryDatabase): FarmDao {
        return database.farmDao()
    }
    
    @Provides
    fun provideFlockDao(database: RostryDatabase): FlockDao {
        return database.flockDao()
    }
    
    @Provides
    fun provideLifecycleDao(database: RostryDatabase): LifecycleDao {
        return database.lifecycleDao()
    }
    
    @Provides
    fun provideLineageDao(database: RostryDatabase): LineageDao {
        return database.lineageDao()
    }
    
    // Farm Access Management DAOs
    @Provides
    fun provideFarmAccessDao(database: RostryDatabase): FarmAccessDao {
        return database.farmAccessDao()
    }
    
    @Provides
    fun provideInvitationDao(database: RostryDatabase): InvitationDao {
        return database.invitationDao()
    }
}