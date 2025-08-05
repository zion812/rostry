package com.rio.rostry.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase dependency injection module with optimized configurations
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().apply {
            // Configure auth settings for better performance
            useAppLanguage()
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance().apply {
            // Configure Firestore settings for optimal performance
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance().apply {
            // Configure storage settings
            maxDownloadRetryTimeMillis = 120000 // 2 minutes
            maxUploadRetryTimeMillis = 120000   // 2 minutes
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context).apply {
            // Configure analytics settings
            setAnalyticsCollectionEnabled(true)
        }
    }
    
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance().apply {
            // Configure crashlytics settings
            setCrashlyticsCollectionEnabled(true)
            
            // Safe BuildConfig access
            try {
                val buildConfigClass = Class.forName("com.rio.rostry.BuildConfig")
                val versionName = buildConfigClass.getField("VERSION_NAME").get(null) as String
                val buildType = buildConfigClass.getField("BUILD_TYPE").get(null) as String
                
                setCustomKey("app_version", versionName)
                setCustomKey("build_type", buildType)
            } catch (e: Exception) {
                // Fallback values if BuildConfig is not available
                setCustomKey("app_version", "unknown")
                setCustomKey("build_type", "unknown")
            }
        }
    }
}