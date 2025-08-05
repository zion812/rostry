package com.rio.rostry.analytics

import android.os.Bundle
import android.os.SystemClock
import androidx.compose.runtime.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance monitoring and analytics system for ROSTRY application.
 * 
 * This class provides comprehensive performance tracking including:
 * - Screen load times
 * - Database operation performance
 * - Network request monitoring
 * - Memory usage tracking
 * - User interaction analytics
 * 
 * All metrics are automatically logged to Firebase Analytics and stored locally
 * for performance reporting and optimization.
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {
    
    companion object {
        // Constants for memory calculations
        private const val BYTES_TO_MB = 1024 * 1024
        private const val MAX_METRICS_HISTORY = 100
        private const val MAX_SCREEN_LOAD_HISTORY = 50
        private const val MAX_DB_OPERATIONS_HISTORY = 200
        
        // Analytics event names
        private const val EVENT_SCREEN_LOAD_TIME = "screen_load_time"
        private const val EVENT_DATABASE_OPERATION = "database_operation"
        private const val EVENT_USER_INTERACTION = "user_interaction"
        private const val EVENT_MEMORY_USAGE = "memory_usage"
        private const val EVENT_NETWORK_REQUEST = "network_request"
    }
    
    private val performanceMetrics = MutableStateFlow(PerformanceMetrics())
    private val metricsMutex = Mutex()
    
    /**
     * Track screen load time with input validation and proper error handling.
     * 
     * @param screenName The name of the screen being tracked (must not be blank)
     * @param startTime The start time in milliseconds (defaults to current time)
     */
    fun trackScreenLoad(screenName: String, startTime: Long = SystemClock.elapsedRealtime()) {
        if (screenName.isBlank()) {
            crashlytics.log("Invalid screen name provided to trackScreenLoad: '$screenName'")
            return
        }
        
        val endTime = SystemClock.elapsedRealtime()
        val loadTime = endTime - startTime
        
        if (loadTime < 0) {
            crashlytics.log("Invalid load time calculated: $loadTime ms for screen: $screenName")
            return
        }
        
        // Log to Firebase Analytics with proper bundle
        val bundle = Bundle().apply {
            putString("screen_name", screenName)
            putLong("load_time_ms", loadTime)
        }
        firebaseAnalytics.logEvent(EVENT_SCREEN_LOAD_TIME, bundle)
        
        // Update local metrics with thread safety
        updateMetricsThreadSafe { metrics ->
            val updatedLoadTimes = (metrics.screenLoadTimes + (screenName to loadTime))
                .toList()
                .takeLast(MAX_SCREEN_LOAD_HISTORY)
                .toMap()
            
            metrics.copy(screenLoadTimes = updatedLoadTimes)
        }
    }
    
    /**
     * Track database operation performance with proper error handling and thread safety.
     * 
     * @param operationName The name of the database operation (must not be blank)
     * @param operation The database operation to execute
     * @return The result of the operation
     * @throws Exception if the operation fails
     */
    suspend fun <T> trackDatabaseOperation(
        operationName: String,
        operation: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        if (operationName.isBlank()) {
            crashlytics.log("Invalid operation name provided to trackDatabaseOperation: '$operationName'")
            throw IllegalArgumentException("Operation name cannot be blank")
        }
        
        val startTime = SystemClock.elapsedRealtime()
        
        return@withContext try {
            val result = operation()
            val duration = SystemClock.elapsedRealtime() - startTime
            
            // Log to Firebase Analytics with proper bundle
            val bundle = Bundle().apply {
                putString("operation_name", operationName)
                putLong("duration_ms", duration)
                putBoolean("success", true)
            }
            firebaseAnalytics.logEvent(EVENT_DATABASE_OPERATION, bundle)
            
            // Update local metrics with thread safety and bounded history
            updateMetricsThreadSafe { metrics ->
                val updatedOperations = (metrics.databaseOperations + DatabaseOperation(
                    name = operationName,
                    duration = duration,
                    success = true
                )).takeLast(MAX_DB_OPERATIONS_HISTORY)
                
                metrics.copy(databaseOperations = updatedOperations)
            }
            
            result
        } catch (e: Exception) {
            val duration = SystemClock.elapsedRealtime() - startTime
            
            // Log error to Firebase Analytics
            val bundle = Bundle().apply {
                putString("operation_name", operationName)
                putLong("duration_ms", duration)
                putBoolean("success", false)
                putString("error", e.message ?: "Unknown error")
            }
            firebaseAnalytics.logEvent(EVENT_DATABASE_OPERATION, bundle)
            
            crashlytics.recordException(e)
            
            // Update local metrics with error information
            updateMetricsThreadSafe { metrics ->
                val updatedOperations = (metrics.databaseOperations + DatabaseOperation(
                    name = operationName,
                    duration = duration,
                    success = false,
                    error = e.message
                )).takeLast(MAX_DB_OPERATIONS_HISTORY)
                
                metrics.copy(databaseOperations = updatedOperations)
            }
            
            throw e
        }
    }
    
    /**
     * Track user interaction with input validation and proper error handling.
     * 
     * @param action The user action being tracked (must not be blank)
     * @param screen The screen where the action occurred (must not be blank)
     * @param additionalParams Additional parameters to log (optional)
     */
    fun trackUserInteraction(
        action: String, 
        screen: String, 
        additionalParams: Map<String, Any> = emptyMap()
    ) {
        if (action.isBlank() || screen.isBlank()) {
            crashlytics.log("Invalid parameters for trackUserInteraction: action='$action', screen='$screen'")
            return
        }
        
        try {
            val bundle = Bundle().apply {
                putString("action", action)
                putString("screen", screen)
                
                // Handle additional parameters with type safety
                additionalParams.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Long -> putLong(key, value)
                        is Int -> putInt(key, value)
                        is Double -> putDouble(key, value)
                        is Float -> putFloat(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> {
                            // Log unsupported type and convert to string
                            crashlytics.log("Unsupported parameter type for key '$key': ${value::class.simpleName}")
                            putString(key, value.toString())
                        }
                    }
                }
            }
            firebaseAnalytics.logEvent(EVENT_USER_INTERACTION, bundle)
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
    }
    
    /**
     * Track memory usage with proper error handling and constants.
     */
    fun trackMemoryUsage() {
        try {
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            val maxMemory = runtime.maxMemory()
            
            if (maxMemory <= 0) {
                crashlytics.log("Invalid max memory value: $maxMemory")
                return
            }
            
            val memoryUsagePercent = (usedMemory.toDouble() / maxMemory.toDouble()) * 100
            
            val bundle = Bundle().apply {
                putLong("used_memory_mb", usedMemory / BYTES_TO_MB)
                putLong("max_memory_mb", maxMemory / BYTES_TO_MB)
                putDouble("usage_percent", memoryUsagePercent)
            }
            firebaseAnalytics.logEvent(EVENT_MEMORY_USAGE, bundle)
            
            updateMetricsThreadSafe { metrics ->
                metrics.copy(
                    memoryUsage = MemoryUsage(
                        usedMemoryMB = usedMemory / BYTES_TO_MB,
                        maxMemoryMB = maxMemory / BYTES_TO_MB,
                        usagePercent = memoryUsagePercent
                    )
                )
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }
    }
    
    /**
     * Track network request performance with proper error handling and context.
     * 
     * @param requestName The name of the network request (must not be blank)
     * @param request The network request to execute
     * @return The result of the request
     * @throws Exception if the request fails
     */
    suspend fun <T> trackNetworkRequest(
        requestName: String,
        request: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        if (requestName.isBlank()) {
            crashlytics.log("Invalid request name provided to trackNetworkRequest: '$requestName'")
            throw IllegalArgumentException("Request name cannot be blank")
        }
        
        val startTime = SystemClock.elapsedRealtime()
        
        return@withContext try {
            val result = request()
            val duration = SystemClock.elapsedRealtime() - startTime
            
            val bundle = Bundle().apply {
                putString("request_name", requestName)
                putLong("duration_ms", duration)
                putBoolean("success", true)
            }
            firebaseAnalytics.logEvent(EVENT_NETWORK_REQUEST, bundle)
            
            result
        } catch (e: Exception) {
            val duration = SystemClock.elapsedRealtime() - startTime
            
            val bundle = Bundle().apply {
                putString("request_name", requestName)
                putLong("duration_ms", duration)
                putBoolean("success", false)
                putString("error", e.message ?: "Unknown error")
            }
            firebaseAnalytics.logEvent(EVENT_NETWORK_REQUEST, bundle)
            
            crashlytics.recordException(e)
            throw e
        }
    }
    
    /**
     * Get performance metrics as a StateFlow for reactive UI updates.
     * 
     * @return StateFlow of current performance metrics
     */
    fun getPerformanceMetrics(): StateFlow<PerformanceMetrics> = performanceMetrics.asStateFlow()
    
    /**
     * Generate comprehensive performance report with proper error handling.
     * 
     * @return PerformanceReport containing aggregated metrics
     */
    fun generatePerformanceReport(): PerformanceReport {
        val metrics = performanceMetrics.value
        
        return try {
            PerformanceReport(
                averageScreenLoadTime = if (metrics.screenLoadTimes.isNotEmpty()) {
                    metrics.screenLoadTimes.values.average()
                } else 0.0,
                slowestScreen = metrics.screenLoadTimes.maxByOrNull { it.value }?.key,
                databaseOperationSuccessRate = if (metrics.databaseOperations.isNotEmpty()) {
                    metrics.databaseOperations.count { it.success }.toDouble() / metrics.databaseOperations.size
                } else 1.0,
                averageDatabaseOperationTime = if (metrics.databaseOperations.isNotEmpty()) {
                    metrics.databaseOperations.map { it.duration }.average()
                } else 0.0,
                currentMemoryUsage = metrics.memoryUsage
            )
        } catch (e: Exception) {
            crashlytics.recordException(e)
            // Return default report on error
            PerformanceReport(
                averageScreenLoadTime = 0.0,
                slowestScreen = null,
                databaseOperationSuccessRate = 1.0,
                averageDatabaseOperationTime = 0.0,
                currentMemoryUsage = MemoryUsage()
            )
        }
    }
    
    /**
     * Clear old metrics to prevent memory leaks.
     * Should be called periodically to maintain performance.
     */
    fun clearOldMetrics() {
        updateMetricsThreadSafe { metrics ->
            metrics.copy(
                screenLoadTimes = metrics.screenLoadTimes.toList()
                    .takeLast(MAX_SCREEN_LOAD_HISTORY)
                    .toMap(),
                databaseOperations = metrics.databaseOperations
                    .takeLast(MAX_DB_OPERATIONS_HISTORY)
            )
        }
    }
    
    /**
     * Thread-safe metrics update using mutex for concurrency control.
     * 
     * @param update Function to transform the current metrics
     */
    private fun updateMetricsThreadSafe(update: (PerformanceMetrics) -> PerformanceMetrics) {
        // Use a coroutine scope for the mutex operation
        CoroutineScope(Dispatchers.Default).launch {
            metricsMutex.withLock {
                performanceMetrics.value = update(performanceMetrics.value)
            }
        }
    }
    
    /**
     * Legacy update method for backward compatibility.
     * Consider using updateMetricsThreadSafe for new code.
     * 
     * @param update Function to transform the current metrics
     */
    @Deprecated("Use updateMetricsThreadSafe for better thread safety")
    private fun updateMetrics(update: (PerformanceMetrics) -> PerformanceMetrics) {
        performanceMetrics.value = update(performanceMetrics.value)
    }
}

/**
 * Data class containing all performance metrics collected by the monitor.
 * 
 * @property screenLoadTimes Map of screen names to their load times in milliseconds
 * @property databaseOperations List of database operations with timing and success information
 * @property memoryUsage Current memory usage statistics
 */
data class PerformanceMetrics(
    val screenLoadTimes: Map<String, Long> = emptyMap(),
    val databaseOperations: List<DatabaseOperation> = emptyList(),
    val memoryUsage: MemoryUsage = MemoryUsage()
)

/**
 * Data class representing a single database operation's performance metrics.
 * 
 * @property name The name/identifier of the database operation
 * @property duration The time taken to complete the operation in milliseconds
 * @property success Whether the operation completed successfully
 * @property error Error message if the operation failed (null if successful)
 */
data class DatabaseOperation(
    val name: String,
    val duration: Long,
    val success: Boolean,
    val error: String? = null
)

/**
 * Data class containing memory usage statistics.
 * 
 * @property usedMemoryMB Amount of memory currently in use (in megabytes)
 * @property maxMemoryMB Maximum memory available to the application (in megabytes)
 * @property usagePercent Percentage of memory currently being used (0-100)
 */
data class MemoryUsage(
    val usedMemoryMB: Long = 0,
    val maxMemoryMB: Long = 0,
    val usagePercent: Double = 0.0
)

/**
 * Comprehensive performance report containing aggregated metrics and insights.
 * 
 * @property averageScreenLoadTime Average time for screens to load (in milliseconds)
 * @property slowestScreen Name of the screen with the longest load time
 * @property databaseOperationSuccessRate Percentage of successful database operations (0.0-1.0)
 * @property averageDatabaseOperationTime Average time for database operations (in milliseconds)
 * @property currentMemoryUsage Current memory usage statistics
 */
data class PerformanceReport(
    val averageScreenLoadTime: Double,
    val slowestScreen: String?,
    val databaseOperationSuccessRate: Double,
    val averageDatabaseOperationTime: Double,
    val currentMemoryUsage: MemoryUsage
)

/**
 * Composable function for automatically tracking screen performance.
 * 
 * This composable should be used in screens where you want to automatically
 * track load times. It measures the time from composition to disposal.
 * 
 * @param screenName The name of the screen to track (should be unique and descriptive)
 * @param performanceMonitor The PerformanceMonitor instance to use for tracking
 * 
 * Usage example:
 * ```
 * @Composable
 * fun MyScreen(performanceMonitor: PerformanceMonitor = hiltViewModel()) {
 *     TrackScreenPerformance("MyScreen", performanceMonitor)
 *     
 *     // Your screen content here
 * }
 * ```
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
