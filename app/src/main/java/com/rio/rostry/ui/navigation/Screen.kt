package com.rio.rostry.ui.navigation

sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    // Main App Screens
    object Home : Screen("home")
    object Marketplace : Screen("marketplace")
    object MyFowls : Screen("my_fowls")
    object Profile : Screen("profile")
    object Chat : Screen("chat")
    
    // Detail Screens
    object FowlDetail : Screen("fowl_detail/{fowlId}") {
        fun createRoute(fowlId: String) = "fowl_detail/$fowlId"
    }
    object AddFowl : Screen("add_fowl")
    object EditFowl : Screen("edit_fowl/{fowlId}") {
        fun createRoute(fowlId: String) = "edit_fowl/$fowlId"
    }
    object EditProfile : Screen("edit_profile")
    object Cart : Screen("cart")
    object CreatePost : Screen("create_post")
    object ChatDetail : Screen("chat_detail/{chatId}") {
        fun createRoute(chatId: String) = "chat_detail/$chatId"
    }
    
    // Transfer screens
    object TransferOwnership : Screen("transfer_ownership/{fowlId}/{fowlName}") {
        fun createRoute(fowlId: String, fowlName: String) = "transfer_ownership/$fowlId/$fowlName"
    }
    object TransferVerification : Screen("transfer_verification/{transferId}") {
        fun createRoute(transferId: String) = "transfer_verification/$transferId"
    }
    object FowlProfile : Screen("fowl_profile/{fowlId}") {
        fun createRoute(fowlId: String) = "fowl_profile/$fowlId"
    }
    object AddRecord : Screen("add_record/{fowlId}") {
        fun createRoute(fowlId: String) = "add_record/$fowlId"
    }
    
    // Monetization screens
    object Verification : Screen("verification")
    object Wallet : Screen("wallet")
    object Showcase : Screen("showcase")
    object Checkout : Screen("checkout/{fowlId}/{quantity}") {
        fun createRoute(fowlId: String, quantity: Int) = "checkout/$fowlId/$quantity"
    }
}