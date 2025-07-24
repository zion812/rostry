package com.rio.rostry.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rio.rostry.data.local.dao.ChatDao
import com.rio.rostry.data.local.dao.MessageDao
import com.rio.rostry.data.local.dao.PostDao
import com.rio.rostry.data.local.dao.UserDao
import com.rio.rostry.data.repository.ChatRepository
import com.rio.rostry.data.repository.PostRepository
import com.rio.rostry.data.repository.UserRepository
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
}