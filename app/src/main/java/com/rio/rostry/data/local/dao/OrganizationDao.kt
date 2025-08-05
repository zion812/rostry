package com.rio.rostry.data.local.dao

import androidx.room.*
import com.rio.rostry.data.model.organization.Organization
import com.rio.rostry.data.model.organization.OrganizationMembership
import com.rio.rostry.data.model.organization.OrganizationInvitation
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {

    // Organization CRUD operations
    @Query("SELECT * FROM organizations WHERE id = :id")
    suspend fun getOrganization(id: String): Organization?

    @Query("SELECT * FROM organizations WHERE ownerId = :userId")
    suspend fun getOrganizationsByOwner(userId: String): List<Organization>

    @Query("SELECT * FROM organizations WHERE isActive = 1")
    fun getAllActiveOrganizations(): Flow<List<Organization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: Organization)

    @Update
    suspend fun updateOrganization(organization: Organization)

    @Delete
    suspend fun deleteOrganization(organization: Organization)

    @Query("DELETE FROM organizations WHERE id = :id")
    suspend fun deleteOrganizationById(id: String)

    // Organization membership operations
    @Query("""
        SELECT o.* FROM organizations o 
        INNER JOIN organization_memberships m ON o.id = m.organizationId 
        WHERE m.userId = :userId AND m.isActive = 1
    """)
    suspend fun getUserOrganizations(userId: String): List<Organization>

    @Query("""
        SELECT o.* FROM organizations o 
        INNER JOIN organization_memberships m ON o.id = m.organizationId 
        WHERE m.userId = :userId AND m.isActive = 1
    """)
    fun getUserOrganizationsFlow(userId: String): Flow<List<Organization>>

    @Query("SELECT * FROM organization_memberships WHERE userId = :userId AND organizationId = :organizationId")
    suspend fun getMembership(userId: String, organizationId: String): OrganizationMembership?

    @Query("SELECT * FROM organization_memberships WHERE organizationId = :organizationId AND isActive = 1")
    suspend fun getOrganizationMembers(organizationId: String): List<OrganizationMembership>

    @Query("SELECT * FROM organization_memberships WHERE organizationId = :organizationId AND isActive = 1")
    fun getOrganizationMembersFlow(organizationId: String): Flow<List<OrganizationMembership>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(membership: OrganizationMembership)

    @Update
    suspend fun updateMembership(membership: OrganizationMembership)

    @Delete
    suspend fun deleteMembership(membership: OrganizationMembership)

    @Query("DELETE FROM organization_memberships WHERE userId = :userId AND organizationId = :organizationId")
    suspend fun removeMembership(userId: String, organizationId: String)

    // Organization invitation operations
    @Query("SELECT * FROM organization_invitations WHERE id = :id")
    suspend fun getInvitation(id: String): OrganizationInvitation?

    @Query("SELECT * FROM organization_invitations WHERE inviteeEmail = :email AND status = 'PENDING'")
    suspend fun getPendingInvitations(email: String): List<OrganizationInvitation>

    @Query("SELECT * FROM organization_invitations WHERE organizationId = :organizationId")
    suspend fun getOrganizationInvitations(organizationId: String): List<OrganizationInvitation>

    @Query("SELECT * FROM organization_invitations WHERE organizationId = :organizationId")
    fun getOrganizationInvitationsFlow(organizationId: String): Flow<List<OrganizationInvitation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: OrganizationInvitation)

    @Update
    suspend fun updateInvitation(invitation: OrganizationInvitation)

    @Delete
    suspend fun deleteInvitation(invitation: OrganizationInvitation)

    @Query("DELETE FROM organization_invitations WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredInvitations(currentTime: Long = System.currentTimeMillis())

    // Analytics and statistics
    @Query("SELECT COUNT(*) FROM organization_memberships WHERE organizationId = :organizationId AND isActive = 1")
    suspend fun getMemberCount(organizationId: String): Int

    @Query("SELECT COUNT(*) FROM organizations WHERE ownerId = :userId AND isActive = 1")
    suspend fun getOwnedOrganizationCount(userId: String): Int

    @Query("""
        SELECT COUNT(*) FROM organization_memberships 
        WHERE organizationId = :organizationId AND isActive = 1 AND roleId = :roleId
    """)
    suspend fun getMemberCountByRole(organizationId: String, roleId: String): Int

    @Query("""
        SELECT COUNT(*) FROM organization_invitations 
        WHERE organizationId = :organizationId AND status = 'PENDING'
    """)
    suspend fun getPendingInvitationCount(organizationId: String): Int

    // Search and filtering
    @Query("SELECT * FROM organizations WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    suspend fun searchOrganizations(query: String): List<Organization>

    @Query("SELECT * FROM organizations WHERE type = :type AND isActive = 1")
    suspend fun getOrganizationsByType(type: String): List<Organization>

    @Query("SELECT * FROM organizations WHERE subscription = :tier AND isActive = 1")
    suspend fun getOrganizationsByTier(tier: String): List<Organization>

    // Cleanup operations
    @Query("DELETE FROM organization_memberships WHERE isActive = 0 AND lastActiveAt < :cutoffTime")
    suspend fun cleanupInactiveMemberships(cutoffTime: Long)

    @Query("DELETE FROM organization_invitations WHERE status IN ('REJECTED', 'EXPIRED', 'CANCELLED') AND respondedAt < :cutoffTime")
    suspend fun cleanupOldInvitations(cutoffTime: Long)
}