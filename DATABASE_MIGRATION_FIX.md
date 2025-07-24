# Database Migration Fix for Rostry App

## Issue
The app was encountering a SQLite error: "duplicate column name: isKycVerified (code 1 SQLITE_ERROR)" when trying to run database migrations.

## Root Cause
The migration script was attempting to add the `isKycVerified` column to the users table, but the column already existed in the database schema. This typically happens when:
1. A migration has been partially run before
2. The database schema is out of sync with the migration scripts
3. Manual database modifications were made

## Solution Implemented

### 1. Enhanced Migration Script (MIGRATION_3_4)
Updated the migration to check if columns exist before attempting to add them:

```kotlin
// Helper function to check if column exists
fun columnExists(tableName: String, columnName: String): Boolean {
    val cursor = database.query("PRAGMA table_info($tableName)")
    var exists = false
    while (cursor.moveToNext()) {
        val name = cursor.getString(cursor.getColumnIndex("name"))
        if (name == columnName) {
            exists = true
            break
        }
    }
    cursor.close()
    return exists
}

// Add columns only if they don't exist
if (!columnExists("users", "isKycVerified")) {
    database.execSQL("ALTER TABLE users ADD COLUMN isKycVerified INTEGER NOT NULL DEFAULT 0")
}
```

### 2. Database Version Update
- Updated database version from 4 to 5
- Added MIGRATION_4_5 for version consistency

### 3. Database Reset Utility
Created `DatabaseResetUtil.kt` to handle database issues:
- `resetDatabase()`: Deletes database files and recreates the database
- `isDatabaseHealthy()`: Checks if database is accessible
- `getManualResetInstructions()`: Provides manual reset instructions

## Files Modified
1. `app/src/main/java/com/rio/rostry/data/local/RostryDatabase.kt`
   - Enhanced MIGRATION_3_4 with column existence checks
   - Added MIGRATION_4_5
   - Updated version to 5

2. `app/src/main/java/com/rio/rostry/util/DatabaseResetUtil.kt` (new file)
   - Database reset utilities

## How to Use

### Automatic Fix
The enhanced migration script will automatically handle the duplicate column issue by checking if columns exist before adding them.

### Manual Reset (if needed)
If the automatic fix doesn't work, users can:
1. Use the DatabaseResetUtil in code
2. Manually clear app data from Android settings
3. Uninstall and reinstall the app

## Prevention
To prevent similar issues in the future:
1. Always check if columns/tables exist before creating them in migrations
2. Use `IF NOT EXISTS` clauses where appropriate
3. Test migrations thoroughly on different database states
4. Consider using Room's schema export for better migration testing

## Testing
After implementing this fix:
1. Clean and rebuild the project
2. Test on devices with existing database
3. Test on fresh installations
4. Verify all monetization features work correctly

The fix ensures backward compatibility while preventing the duplicate column error.