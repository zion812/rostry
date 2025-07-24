package com.rio.rostry.di

import android.content.Context
import androidx.room.Room
import com.rio.rostry.data.local.RostryDatabase
import com.rio.rostry.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        .addMigrations(RostryDatabase.MIGRATION_1_2, RostryDatabase.MIGRATION_2_3, RostryDatabase.MIGRATION_3_4)
        .fallbackToDestructiveMigration() // Allow destructive migration for development
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
}