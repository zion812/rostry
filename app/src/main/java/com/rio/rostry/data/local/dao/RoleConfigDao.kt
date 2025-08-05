package com.rio.rostry.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleConfigDao {

    @Query("SELECT * FROM role_configurations WHERE isActive = 1 ORDER BY level ASC")
    fun getAllConfigurations(): Flow<List<RoleConfigurationEntity>>

    @Query("SELECT * FROM role_configurations WHERE id = :id")
    suspend fun getConfiguration(id: String): RoleConfigurationEntity?

    @Query("SELECT * FROM role_configurations WHERE name = :name AND isActive = 1")
    suspend fun getConfigurationByName(name: String): RoleConfigurationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(config: RoleConfigurationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigurations(configs: List<RoleConfigurationEntity>)

    @Update
    suspend fun updateConfiguration(config: RoleConfigurationEntity)

    @Delete
    suspend fun deleteConfiguration(config: RoleConfigurationEntity)

    @Query("DELETE FROM role_configurations WHERE id = :id")
    suspend fun deleteConfigurationById(id: String)

    @Query("SELECT * FROM role_configurations WHERE level <= :maxLevel AND isActive = 1")
    suspend fun getConfigurationsByMaxLevel(maxLevel: Int): List<RoleConfigurationEntity>

    @Query("SELECT * FROM role_configurations WHERE parentRoleId = :parentId AND isActive = 1")
    suspend fun getChildConfigurations(parentId: String): List<RoleConfigurationEntity>

    @Query("UPDATE role_configurations SET isActive = 0 WHERE id = :id")
    suspend fun deactivateConfiguration(id: String)

    @Query("DELETE FROM role_configurations WHERE isActive = 0 AND updatedAt < :cutoffTime")
    suspend fun cleanupInactiveConfigurations(cutoffTime: Long)
}

@Entity(tableName = "role_configurations")
data class RoleConfigurationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val level: Int,
    val parentRoleId: String?,
    val permissions: String, // JSON string of permissions
    val navigationConfig: String, // JSON string of navigation config
    val isActive: Boolean = true,
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)