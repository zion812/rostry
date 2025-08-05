package com.rio.rostry.role

import com.rio.rostry.data.model.role.Permission
import com.rio.rostry.data.model.role.UserRole
import org.junit.Test
import org.junit.Assert.*

class RoleHierarchyTest {

    @Test
    fun `consumer role has basic permissions`() {
        val consumer = UserRole.Consumer
        
        // Consumer should have marketplace access
        assertTrue("Consumer should have marketplace view access", 
            consumer.hasPermission(Permission.Marketplace.VIEW))
        assertTrue("Consumer should have marketplace purchase access", 
            consumer.hasPermission(Permission.Marketplace.PURCHASE))
        
        // Consumer should NOT have farm management permissions
        assertFalse("Consumer should not have farm view access", 
            consumer.hasPermission(Permission.Farm.VIEW_OWN))
        assertFalse("Consumer should not have analytics access", 
            consumer.hasPermission(Permission.Farm.ANALYTICS_BASIC))
    }

    @Test
    fun `basic farmer inherits consumer permissions plus farm access`() {
        val basicFarmer = UserRole.Producer.BasicFarmer
        
        // Should inherit consumer permissions
        assertTrue("Basic farmer should have marketplace view access", 
            basicFarmer.hasPermission(Permission.Marketplace.VIEW))
        
        // Should have farm permissions
        assertTrue("Basic farmer should have farm view access", 
            basicFarmer.hasPermission(Permission.Farm.VIEW_OWN))
        assertTrue("Basic farmer should have farm manage basic access", 
            basicFarmer.hasPermission(Permission.Farm.MANAGE_BASIC))
        
        // Should NOT have advanced permissions
        assertFalse("Basic farmer should not have analytics access", 
            basicFarmer.hasPermission(Permission.Farm.ANALYTICS_BASIC))
        assertFalse("Basic farmer should not have team management access", 
            basicFarmer.hasPermission(Permission.Team.MANAGE))
    }

    @Test
    fun `role levels are correctly ordered`() {
        assertEquals("Consumer should be level 1", 1, UserRole.Consumer.level)
        assertEquals("Basic farmer should be level 2", 2, UserRole.Producer.BasicFarmer.level)
        assertEquals("Verified farmer should be level 3", 3, UserRole.Producer.VerifiedFarmer.level)
        assertEquals("Premium breeder should be level 4", 4, UserRole.Producer.PremiumBreeder.level)
        assertEquals("Farm manager should be level 5", 5, UserRole.Producer.FarmManager.level)
        assertEquals("Moderator should be level 6", 6, UserRole.Administrator.Moderator.level)
        assertEquals("Super admin should be level 7", 7, UserRole.Administrator.SuperAdmin.level)
    }

    @Test
    fun `role display names are correct`() {
        assertEquals("Consumer", UserRole.Consumer.displayName)
        assertEquals("Basic Farmer", UserRole.Producer.BasicFarmer.displayName)
        assertEquals("Verified Farmer", UserRole.Producer.VerifiedFarmer.displayName)
        assertEquals("Premium Breeder", UserRole.Producer.PremiumBreeder.displayName)
        assertEquals("Farm Manager", UserRole.Producer.FarmManager.displayName)
        assertEquals("Moderator", UserRole.Administrator.Moderator.displayName)
        assertEquals("Super Admin", UserRole.Administrator.SuperAdmin.displayName)
    }

    @Test
    fun `verified farmer has analytics access but basic farmer does not`() {
        val basicFarmer = UserRole.Producer.BasicFarmer
        val verifiedFarmer = UserRole.Producer.VerifiedFarmer
        
        // Basic farmer should NOT have analytics
        assertFalse("Basic farmer should not have analytics access", 
            basicFarmer.hasPermission(Permission.Farm.ANALYTICS_BASIC))
        
        // Verified farmer should have analytics
        assertTrue("Verified farmer should have analytics access", 
            verifiedFarmer.hasPermission(Permission.Farm.ANALYTICS_BASIC))
    }

    @Test
    fun `premium breeder has advanced permissions`() {
        val premiumBreeder = UserRole.Producer.PremiumBreeder
        
        // Should have all previous permissions
        assertTrue("Premium breeder should have marketplace view", 
            premiumBreeder.hasPermission(Permission.Marketplace.VIEW))
        assertTrue("Premium breeder should have farm management", 
            premiumBreeder.hasPermission(Permission.Farm.MANAGE_BASIC))
        assertTrue("Premium breeder should have basic analytics", 
            premiumBreeder.hasPermission(Permission.Farm.ANALYTICS_BASIC))
        
        // Should have advanced permissions
        assertTrue("Premium breeder should have advanced analytics", 
            premiumBreeder.hasPermission(Permission.Analytics.ADVANCED))
        assertTrue("Premium breeder should have breeding tracking", 
            premiumBreeder.hasPermission(Permission.Breeding.ADVANCED_TRACKING))
        assertTrue("Premium breeder should have lineage management", 
            premiumBreeder.hasPermission(Permission.Breeding.LINEAGE_MANAGEMENT))
    }

    @Test
    fun `farm manager can manage teams but lower roles cannot`() {
        val basicFarmer = UserRole.Producer.BasicFarmer
        val verifiedFarmer = UserRole.Producer.VerifiedFarmer
        val premiumBreeder = UserRole.Producer.PremiumBreeder
        val farmManager = UserRole.Producer.FarmManager
        
        // Lower roles should NOT have team management
        assertFalse("Basic farmer should not manage teams", 
            basicFarmer.hasPermission(Permission.Team.MANAGE))
        assertFalse("Verified farmer should not manage teams", 
            verifiedFarmer.hasPermission(Permission.Team.MANAGE))
        assertFalse("Premium breeder should not manage teams", 
            premiumBreeder.hasPermission(Permission.Team.MANAGE))
        
        // Farm manager should have team management
        assertTrue("Farm manager should manage teams", 
            farmManager.hasPermission(Permission.Team.MANAGE))
    }

    @Test
    fun `admin roles have moderation permissions`() {
        val moderator = UserRole.Administrator.Moderator
        val superAdmin = UserRole.Administrator.SuperAdmin
        
        // Moderator should have moderation permissions
        assertTrue("Moderator should have content moderation", 
            moderator.hasPermission(Permission.Moderation.CONTENT))
        assertTrue("Moderator should have user moderation", 
            moderator.hasPermission(Permission.Moderation.USERS))
        
        // Super admin should have all permissions including system access
        assertTrue("Super admin should have full access", 
            superAdmin.hasPermission(Permission.Admin.FULL_ACCESS))
        assertTrue("Super admin should have system configuration", 
            superAdmin.hasPermission(Permission.System.CONFIGURE))
    }

    @Test
    fun `role hierarchy management works correctly`() {
        val consumer = UserRole.Consumer
        val basicFarmer = UserRole.Producer.BasicFarmer
        val farmManager = UserRole.Producer.FarmManager
        val superAdmin = UserRole.Administrator.SuperAdmin
        
        // Higher roles can manage lower roles
        assertTrue("Farm manager should manage basic farmer", 
            farmManager.canManage(basicFarmer))
        assertTrue("Super admin should manage farm manager", 
            superAdmin.canManage(farmManager))
        assertTrue("Super admin should manage consumer", 
            superAdmin.canManage(consumer))
        
        // Lower roles cannot manage higher roles
        assertFalse("Basic farmer should not manage farm manager", 
            basicFarmer.canManage(farmManager))
        assertFalse("Consumer should not manage basic farmer", 
            consumer.canManage(basicFarmer))
    }

    @Test
    fun `role upgrade paths are correct`() {
        val consumer = UserRole.Consumer
        val basicFarmer = UserRole.Producer.BasicFarmer
        val verifiedFarmer = UserRole.Producer.VerifiedFarmer
        val superAdmin = UserRole.Administrator.SuperAdmin
        
        // Consumer can upgrade to basic farmer
        val consumerUpgrades = consumer.getUpgradePath()
        assertEquals("Consumer should have one upgrade option", 1, consumerUpgrades.size)
        assertEquals("Consumer should upgrade to basic farmer", 
            UserRole.Producer.BasicFarmer, consumerUpgrades.first())
        
        // Basic farmer can upgrade to verified farmer
        val basicFarmerUpgrades = basicFarmer.getUpgradePath()
        assertEquals("Basic farmer should have one upgrade option", 1, basicFarmerUpgrades.size)
        assertEquals("Basic farmer should upgrade to verified farmer", 
            UserRole.Producer.VerifiedFarmer, basicFarmerUpgrades.first())
        
        // Super admin has no upgrades
        val superAdminUpgrades = superAdmin.getUpgradePath()
        assertTrue("Super admin should have no upgrades", superAdminUpgrades.isEmpty())
    }

    @Test
    fun `role IDs match expected values`() {
        assertEquals("consumer", UserRole.Consumer.id)
        assertEquals("basic_farmer", UserRole.Producer.BasicFarmer.id)
        assertEquals("verified_farmer", UserRole.Producer.VerifiedFarmer.id)
        assertEquals("premium_breeder", UserRole.Producer.PremiumBreeder.id)
        assertEquals("farm_manager", UserRole.Producer.FarmManager.id)
        assertEquals("moderator", UserRole.Administrator.Moderator.id)
        assertEquals("super_admin", UserRole.Administrator.SuperAdmin.id)
    }
}