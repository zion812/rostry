package com.rio.rostry.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.*
import com.rio.rostry.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        messageDao: MessageDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ChatRepository {
        return ChatRepository(chatDao, messageDao, firestore, storage)
    }
    
    @Provides
    @Singleton
    fun providePostRepository(
        postDao: PostDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PostRepository {
        return PostRepository(postDao, firestore, storage)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firestore: FirebaseFirestore
    ): UserRepository {
        return UserRepository(userDao, firestore)
    }
    
    @Provides
    @Singleton
    fun provideFowlRepository(
        fowlDao: FowlDao,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FowlRepository {
        return FowlRepository(firestore, storage, fowlDao)
    }
    
    @Provides
    @Singleton
    fun provideDashboardRepository(
        firestore: FirebaseFirestore,
        fowlDao: FowlDao,
        flockSummaryDao: FlockSummaryDao,
        fowlRepository: FowlRepository
    ): DashboardRepository {
        return DashboardRepository(firestore, fowlDao, flockSummaryDao, fowlRepository)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: com.google.firebase.auth.FirebaseAuth,
        firestore: FirebaseFirestore,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepository(firebaseAuth, firestore, userDao)
    }
    
    @Provides
    @Singleton
    fun provideWalletRepository(
        firestore: FirebaseFirestore,
        walletDao: WalletDao
    ): WalletRepository {
        return WalletRepository(firestore, walletDao)
    }
    
    @Provides
    @Singleton
    fun provideMarketplaceRepository(
        firestore: FirebaseFirestore,
        fowlRepository: FowlRepository
    ): MarketplaceRepository {
        return MarketplaceRepository(firestore, fowlRepository)
    }
    
    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        orderDao: OrderDao
    ): OrderRepository {
        return OrderRepository(firestore, orderDao)
    }
    
    @Provides
    @Singleton
    fun provideTransferRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        fowlRepository: FowlRepository
    ): TransferRepository {
        return TransferRepository(firestore, storage, fowlRepository)
    }
    
    @Provides
    @Singleton
    fun provideVerificationRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        verificationDao: VerificationDao,
        walletDao: WalletDao
    ): VerificationRepository {
        return VerificationRepository(firestore, storage, verificationDao, walletDao)
    }
    
    @Provides
    @Singleton
    fun provideMockPaymentRepository(): MockPaymentRepository {
        return MockPaymentRepository()
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        fowlRepository: FowlRepository
    ): NotificationRepository {
        return NotificationRepository(firestore, fowlRepository)
    }
    
    @Provides
    @Singleton
    fun provideReportRepository(
        firestore: FirebaseFirestore,
        fowlRepository: FowlRepository,
        dashboardRepository: DashboardRepository
    ): ReportRepository {
        return ReportRepository(firestore, fowlRepository, dashboardRepository)
    }
}