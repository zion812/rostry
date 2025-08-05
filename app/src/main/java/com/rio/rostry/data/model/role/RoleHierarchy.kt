package com.rio.rostry.data.model.role

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Hierarchical role system with inheritance and dynamic permissions
 */
sealed class UserRole(
    val id: String,
    val displayName: String,
    val description: String,
    val level: Int,
    val parentRole: UserRole? = null,
    val basePermissions: Set<Permission>,
    val navigationConfig: NavigationConfig
) {
    // Base Consumer Role
    object Consumer : UserRole(
        id = "consumer",
        displayName = "Consumer",
        description = "Urban & Semi-Urban Consumers",
        level = 1,
        basePermissions = setOf(
            Permission.Marketplace.VIEW,
            Permission.Marketplace.PURCHASE,
            Permission.Social.CREATE_POST,
            Permission.Social.INTERACT,
            Permission.Profile.VIEW_OWN,
            Permission.Profile.EDIT_OWN
        ),
        navigationConfig = NavigationConfig.consumer()
    )

    // Producer Hierarchy
    sealed class Producer(
        id: String,
        displayName: String,
        description: String,
        level: Int,
        parentRole: UserRole?,
        additionalPermissions: Set<Permission>
    ) : UserRole(
        id = id,
        displayName = displayName,
        description = description,
        level = level,
        parentRole = parentRole,
        basePermissions = (parentRole?.getAllPermissions() ?: emptySet()) + additionalPermissions,
        navigationConfig = NavigationConfig.producer(level)
    ) {
        object BasicFarmer : Producer(
            id = "basic_farmer",
            displayName = "Basic Farmer",
            description = "Small-scale farmers with basic selling capabilities",
            level = 2,
            parentRole = Consumer,
            additionalPermissions = setOf(
                Permission.Marketplace.LIST_BASIC,
                Permission.Farm.VIEW_OWN,
                Permission.Farm.MANAGE_BASIC,
                Permission.Community.PARTICIPATE
            )
        )

        object VerifiedFarmer : Producer(
            id = "verified_farmer",
            displayName = "Verified Farmer",
            description = "Verified farmers with enhanced features",
            level = 3,
            parentRole = BasicFarmer,
            additionalPermissions = setOf(
                Permission.Marketplace.LIST_PREMIUM,
                Permission.Farm.ANALYTICS_BASIC,
                Permission.Health.MANAGE_RECORDS,
                Permission.Community.CREATE_GROUPS
            )
        )

        object PremiumBreeder : Producer(
            id = "premium_breeder",
            displayName = "Premium Breeder",
            description = "Professional breeders with advanced features",
            level = 4,
            parentRole = VerifiedFarmer,
            additionalPermissions = setOf(
                Permission.Breeding.ADVANCED_TRACKING,
                Permission.Breeding.LINEAGE_MANAGEMENT,
                Permission.Analytics.ADVANCED,
                Permission.Transfer.MANAGE,
                Permission.Broadcast.LIVE_STREAMING
            )
        )

        object FarmManager : Producer(
            id = "farm_manager",
            displayName = "Farm Manager",
            description = "Manages multiple farms and teams",
            level = 5,
            parentRole = PremiumBreeder,
            additionalPermissions = setOf(
                Permission.Farm.MANAGE_MULTIPLE,
                Permission.Team.MANAGE,
                Permission.Analytics.ENTERPRISE,
                Permission.API.ACCESS
            )
        )
    }

    // Administrative Roles
    sealed class Administrator(
        id: String,
        displayName: String,
        description: String,
        level: Int,
        additionalPermissions: Set<Permission>
    ) : UserRole(
        id = id,
        displayName = displayName,
        description = description,
        level = level,
        parentRole = Producer.FarmManager,
        basePermissions = Producer.FarmManager.getAllPermissions() + additionalPermissions,
        navigationConfig = NavigationConfig.admin(level)
    ) {
        object Moderator : Administrator(
            id = "moderator",
            displayName = "Moderator",
            description = "Community and content moderation",
            level = 6,
            additionalPermissions = setOf(
                Permission.Moderation.CONTENT,
                Permission.Moderation.USERS,
                Permission.Reports.VIEW
            )
        )

        object SuperAdmin : Administrator(
            id = "super_admin",
            displayName = "Super Admin",
            description = "Full system administration",
            level = 7,
            additionalPermissions = setOf(
                Permission.Admin.FULL_ACCESS,
                Permission.System.CONFIGURE,
                Permission.Analytics.GLOBAL
            )
        )
    }

    /**
     * Get all permissions including inherited ones
     */
    fun getAllPermissions(): Set<Permission> {
        return basePermissions + (parentRole?.getAllPermissions() ?: emptySet())
    }

    /**
     * Check if this role can access a specific permission
     */
    fun hasPermission(permission: Permission): Boolean {
        return getAllPermissions().contains(permission)
    }

    /**
     * Check if this role can manage another role
     */
    fun canManage(otherRole: UserRole): Boolean {
        return this.level > otherRole.level
    }

    /**
     * Get upgrade path to next role
     */
    fun getUpgradePath(): List<UserRole> {
        return when (this) {
            is Consumer -> listOf(Producer.BasicFarmer)
            is Producer.BasicFarmer -> listOf(Producer.VerifiedFarmer)
            is Producer.VerifiedFarmer -> listOf(Producer.PremiumBreeder)
            is Producer.PremiumBreeder -> listOf(Producer.FarmManager)
            is Producer.FarmManager -> listOf(Administrator.Moderator)
            is Administrator.Moderator -> listOf(Administrator.SuperAdmin)
            is Administrator.SuperAdmin -> emptyList()
        }
    }
}

/**
 * Granular permission system with categories
 */
sealed class Permission(val id: String, val category: String, val description: String) {
    sealed class Marketplace(id: String, description: String) : Permission(id, "marketplace", description) {
        object VIEW : Marketplace("marketplace.view", "View marketplace listings")
        object PURCHASE : Marketplace("marketplace.purchase", "Purchase products")
        object LIST_BASIC : Marketplace("marketplace.list.basic", "List basic products")
        object LIST_PREMIUM : Marketplace("marketplace.list.premium", "List premium products")
        object MANAGE_LISTINGS : Marketplace("marketplace.manage", "Manage own listings")
    }

    sealed class Farm(id: String, description: String) : Permission(id, "farm", description) {
        object VIEW_OWN : Farm("farm.view.own", "View own farm")
        object MANAGE_BASIC : Farm("farm.manage.basic", "Basic farm management")
        object MANAGE_MULTIPLE : Farm("farm.manage.multiple", "Manage multiple farms")
        object ANALYTICS_BASIC : Farm("farm.analytics.basic", "Basic farm analytics")
    }

    sealed class Social(id: String, description: String) : Permission(id, "social", description) {
        object CREATE_POST : Social("social.post.create", "Create social posts")
        object INTERACT : Social("social.interact", "Like, comment, share")
        object MODERATE : Social("social.moderate", "Moderate content")
    }

    sealed class Breeding(id: String, description: String) : Permission(id, "breeding", description) {
        object BASIC_TRACKING : Breeding("breeding.track.basic", "Basic breeding tracking")
        object ADVANCED_TRACKING : Breeding("breeding.track.advanced", "Advanced breeding analytics")
        object LINEAGE_MANAGEMENT : Breeding("breeding.lineage", "Manage lineage records")
    }

    sealed class Analytics(id: String, description: String) : Permission(id, "analytics", description) {
        object BASIC : Analytics("analytics.basic", "Basic analytics")
        object ADVANCED : Analytics("analytics.advanced", "Advanced analytics")
        object ENTERPRISE : Analytics("analytics.enterprise", "Enterprise analytics")
        object GLOBAL : Analytics("analytics.global", "Global system analytics")
    }

    sealed class Team(id: String, description: String) : Permission(id, "team", description) {
        object VIEW : Team("team.view", "View team members")
        object MANAGE : Team("team.manage", "Manage team members")
        object INVITE : Team("team.invite", "Invite team members")
    }

    sealed class Transfer(id: String, description: String) : Permission(id, "transfer", description) {
        object VIEW : Transfer("transfer.view", "View transfers")
        object MANAGE : Transfer("transfer.manage", "Manage transfers")
        object VERIFY : Transfer("transfer.verify", "Verify transfers")
    }

    sealed class Health(id: String, description: String) : Permission(id, "health", description) {
        object VIEW_RECORDS : Health("health.view", "View health records")
        object MANAGE_RECORDS : Health("health.manage", "Manage health records")
    }

    sealed class Community(id: String, description: String) : Permission(id, "community", description) {
        object PARTICIPATE : Community("community.participate", "Participate in community")
        object CREATE_GROUPS : Community("community.groups.create", "Create community groups")
        object MODERATE : Community("community.moderate", "Moderate community")
    }

    sealed class Broadcast(id: String, description: String) : Permission(id, "broadcast", description) {
        object LIVE_STREAMING : Broadcast("broadcast.live", "Live streaming")
        object SCHEDULED : Broadcast("broadcast.scheduled", "Scheduled broadcasts")
    }

    sealed class Profile(id: String, description: String) : Permission(id, "profile", description) {
        object VIEW_OWN : Profile("profile.view.own", "View own profile")
        object EDIT_OWN : Profile("profile.edit.own", "Edit own profile")
        object VIEW_OTHERS : Profile("profile.view.others", "View other profiles")
    }

    sealed class Moderation(id: String, description: String) : Permission(id, "moderation", description) {
        object CONTENT : Moderation("moderation.content", "Moderate content")
        object USERS : Moderation("moderation.users", "Moderate users")
        object REPORTS : Moderation("moderation.reports", "Handle reports")
    }

    sealed class Admin(id: String, description: String) : Permission(id, "admin", description) {
        object FULL_ACCESS : Admin("admin.full", "Full administrative access")
        object USER_MANAGEMENT : Admin("admin.users", "User management")
        object SYSTEM_CONFIG : Admin("admin.system", "System configuration")
    }

    sealed class System(id: String, description: String) : Permission(id, "system", description) {
        object CONFIGURE : System("system.configure", "Configure system settings")
        object MONITOR : System("system.monitor", "Monitor system health")
    }

    sealed class Reports(id: String, description: String) : Permission(id, "reports", description) {
        object VIEW : Reports("reports.view", "View reports")
        object GENERATE : Reports("reports.generate", "Generate reports")
    }

    sealed class API(id: String, description: String) : Permission(id, "api", description) {
        object ACCESS : API("api.access", "API access")
        object ADMIN : API("api.admin", "Administrative API access")
    }
}

/**
 * Navigation configuration for different roles
 */
data class NavigationConfig(
    val items: List<NavigationItem>,
    val startDestination: String,
    val bottomNavEnabled: Boolean = true,
    val drawerEnabled: Boolean = false
) {
    companion object {
        fun consumer() = NavigationConfig(
            items = listOf(
                NavigationItem("marketplace", "Market", "marketplace", Icons.Default.Store),
                NavigationItem("explore", "Explore", "explore", Icons.Default.Explore),
                NavigationItem("create", "Create", "create_social", Icons.Default.Add),
                NavigationItem("cart", "Cart", "cart", Icons.Default.ShoppingCart),
                NavigationItem("profile", "Profile", "profile", Icons.Default.Person)
            ),
            startDestination = "marketplace"
        )

        fun producer(level: Int) = NavigationConfig(
            items = when (level) {
                2, 3 -> listOf( // Basic/Verified Farmer
                    NavigationItem("home", "Home", "farmer_home", Icons.Default.Home),
                    NavigationItem("marketplace", "Market", "marketplace", Icons.Default.Store),
                    NavigationItem("create", "Create", "create_farmer", Icons.Default.Add),
                    NavigationItem("community", "Community", "community", Icons.Default.Groups),
                    NavigationItem("profile", "Profile", "farmer_profile", Icons.Default.Person)
                )
                else -> listOf( // Premium Breeder+
                    NavigationItem("home", "Home", "breeder_home", Icons.Default.Home),
                    NavigationItem("explore", "Explore", "explore", Icons.Default.Explore),
                    NavigationItem("create", "Create", "create_breeder", Icons.Default.Add),
                    NavigationItem("dashboard", "Dashboard", "farm_dashboard", Icons.Default.Dashboard),
                    NavigationItem("transfers", "Transfers", "transfers", Icons.Default.SwapHoriz)
                )
            },
            startDestination = "farmer_home"
        )

        fun admin(level: Int) = NavigationConfig(
            items = listOf(
                NavigationItem("dashboard", "Dashboard", "admin_dashboard", Icons.Default.Dashboard),
                NavigationItem("users", "Users", "user_management", Icons.Default.People),
                NavigationItem("reports", "Reports", "reports", Icons.Default.Assessment),
                NavigationItem("settings", "Settings", "admin_settings", Icons.Default.Settings)
            ),
            startDestination = "admin_dashboard",
            drawerEnabled = true
        )
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val route: String,
    val icon: ImageVector,
    val requiredPermissions: List<Permission> = emptyList(),
    val badgeCount: Int = 0,
    val isEnabled: Boolean = true
)