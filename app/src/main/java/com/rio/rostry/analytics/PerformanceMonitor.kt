package com.rio.rostry.analytics

import android.os.SystemClock
import androidx.compose.runtime.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitoring and analytics system
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {
    
    private val performanceMetrics = MutableStateFlow<PerformanceMetrics>(PerformanceMetrics())
    
    /**
     * Track screen load time
     */
    fun trackScreenLoad(screenName: String, startTime: Long = SystemClock.elapsedRealtime()) {
        val endTime = SystemClock.elapsedRealtime()
        val loadTime = endTime - startTime
        
        // Log to Firebase Analytics
        firebaseAnalytics.logEvent("screen_load_time") {
            param("screen_name", screenName)
            param("load_time_ms", loadTime)
        }
        
        // Update local metrics
        updateMetrics { it.copy(
            screenLoadTimes = it.screenLoadTimes + (screenName to loadTime)
        )}
    }
    
    /**
     * Track database operation performance
     */
    suspend fun <T> trackDatabaseOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        val startTime = SystemClock.elapsedRealtime()
        
        return try {
            val result = operation()
            val duration = SystemClock.elapsedRealtime() - startTime
            
            firebaseAnalytics.logEvent("database_operation") {
                param("operation_name", operationName)
                param("duration_ms", duration)
                param("success", true)
            }
            
            updateMetrics { it.copy(
                databaseOperations = it.databaseOperations + DatabaseOperation(
                    name = operationName,
                    duration = duration,
                    success = true
                )
            )}
            
            result
        } catch (e: Exception) {
            val duration = SystemClock.elapsedRealtime() - startTime
            
            firebaseAnalytics.logEvent("database_operation") {
                param("operation_name", operationName)
                param("duration_ms", duration)
                param("success", false)
                param("error", e.message ?: "Unknown error")
            }
            
            crashlytics.recordException(e)
            
            updateMetrics { it.copy(
                databaseOperations = it.databaseOperations + DatabaseOperation(
                    name = operationName,
                    duration = duration,
                    success = false,
                    error = e.message
                )
            )}
            
            throw e
        }
    }
    
    /**
     * Track user interaction
     */
    fun trackUserInteraction(action: String, screen: String, additionalParams: Map<String, Any> = emptyMap()) {
        firebaseAnalytics.logEvent("user_interaction") {
            param("action", action)
            param("screen", screen)
            additionalParams.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Boolean -> param(key, value)
                }
            }
        }
    }
    
    /**
     * Track memory usage
     */
    fun trackMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
        
        firebaseAnalytics.logEvent("memory_usage") {
            param("used_memory_mb", usedMemory / (1024 * 1024))
            param("max_memory_mb", maxMemory / (1024 * 1024))
            param("usage_percent", memoryUsagePercent)
        }
        
        updateMetrics { it.copy(
            memoryUsage = MemoryUsage(
                usedMemoryMB = usedMemory / (1024 * 1024),
                maxMemoryMB = maxMemory / (1024 * 1024),
                usagePercent = memoryUsagePercent
            )
        )}
    }
    
    /**
     * Track network request performance
     */
    suspend fun <T> trackNetworkRequest(
        requestName: String,
        request: suspend () -> T
    ): T {
        val startTime = SystemClock.elapsedRealtime()
        
        return try {
            val result = request()
            val duration = SystemClock.elapsedRealtime() - startTime
            
            firebaseAnalytics.logEvent("network_request") {
                param("request_name", requestName)
                param("duration_ms", duration)
                param("success", true)
            }
            
            result
        } catch (e: Exception) {
            val duration = SystemClock.elapsedRealtime() - startTime
            
            firebaseAnalytics.logEvent("network_request") {
                param("request_name", requestName)
                param("duration_ms", duration)
                param("success", false)
                param("error", e.message ?: "Unknown error")
            }
            
            crashlytics.recordException(e)
            throw e
        }
    }
    
    /**
     * Get performance metrics
     */
    fun getPerformanceMetrics(): StateFlow<PerformanceMetrics> = performanceMetrics.asStateFlow()
    
    /**
     * Generate performance report
     */
    fun generatePerformanceReport(): PerformanceReport {
        val metrics = performanceMetrics.value
        
        return PerformanceReport(
            averageScreenLoadTime = metrics.screenLoadTimes.values.average(),
            slowestScreen = metrics.screenLoadTimes.maxByOrNull { it.value }?.key,
            databaseOperationSuccessRate = metrics.databaseOperations.count { it.success }.toDouble() / 
                                          metrics.databaseOperations.size.coerceAtLeast(1),
            averageDatabaseOperationTime = metrics.databaseOperations.map { it.duration }.average(),
            currentMemoryUsage = metrics.memoryUsage
        )
    }
    
    private fun updateMetrics(update: (PerformanceMetrics) -> PerformanceMetrics) {
        performanceMetrics.value = update(performanceMetrics.value)
    }
}

/**
 * Performance metrics data classes
 */
data class PerformanceMetrics(
    val screenLoadTimes: Map<String, Long> = emptyMap(),
    val databaseOperations: List<DatabaseOperation> = emptyList(),
    val memoryUsage: MemoryUsage = MemoryUsage()
)

data class DatabaseOperation(
    val name: String,
    val duration: Long,
    val success: Boolean,
    val error: String? = null
)

data class MemoryUsage(
    val usedMemoryMB: Long = 0,
    val maxMemoryMB: Long = 0,
    val usagePercent: Double = 0.0
)

data class PerformanceReport(
    val averageScreenLoadTime: Double,
    val slowestScreen: String?,
    val databaseOperationSuccessRate: Double,
    val averageDatabaseOperationTime: Double,
    val currentMemoryUsage: MemoryUsage
)

/**
 * Composable for tracking screen performance
 */
@Composable
fun TrackScreenPerformance(
    screenName: String,
    performanceMonitor: PerformanceMonitor
) {
    val startTime = remember { SystemClock.elapsedRealtime() }
    
    DisposableEffect(screenName) {
        onDispose {
            performanceMonitor.trackScreenLoad(screenName, startTime)
        }
    }
}

/**
 * Extension function for Firebase Analytics
 */
private inline fun FirebaseAnalytics.logEvent(
    name: String,
    block: FirebaseAnalytics.Event.() -> Unit
) {
    val event = FirebaseAnalytics.Event()
    event.block()
    // Note: This is a simplified version. In real implementation, 
    // you would use the actual Firebase Analytics API
}
