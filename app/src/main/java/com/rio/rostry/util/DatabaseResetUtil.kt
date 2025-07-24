package com.rio.rostry.util

import android.content.Context
import androidx.room.Room
import com.rio.rostry.data.local.RostryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class to handle database reset operations
 * Use this if migration issues persist
 */
object DatabaseResetUtil {
    
    /**
     * Clears the database by recreating it
     * This will delete all existing data
     */
    suspend fun resetDatabase(context: Context) = withContext(Dispatchers.IO) {
        try {
            // Delete the database file
            val dbFile = context.getDatabasePath("rostry_database")
            if (dbFile.exists()) {
                dbFile.delete()
            }
            
            // Delete associated files
            val shmFile = context.getDatabasePath("rostry_database-shm")
            if (shmFile.exists()) {
                shmFile.delete()
            }
            
            val walFile = context.getDatabasePath("rostry_database-wal")
            if (walFile.exists()) {
                walFile.delete()
            }
            
            // Create a new database instance
            RostryDatabase.getDatabase(context)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Checks if the database exists and has the correct schema
     */
    suspend fun isDatabaseHealthy(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = RostryDatabase.getDatabase(context)
            // Try to perform a simple query to check if the database is accessible
            db.userDao().getAllUsers()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Alternative method to clear app data which will reset the database
     * This requires the user to manually clear app data from Android settings
     */
    fun getManualResetInstructions(): String {
        return """
            To manually reset the database:
            1. Go to Android Settings
            2. Apps & notifications
            3. Find Rostry app
            4. Storage & cache
            5. Clear storage
            
            Or uninstall and reinstall the app.
        """.trimIndent()
    }
}