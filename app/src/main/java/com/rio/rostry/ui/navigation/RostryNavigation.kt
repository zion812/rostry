package com.rio.rostry.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rio.rostry.ui.auth.LoginScreen
import com.rio.rostry.ui.auth.RegisterScreen
import com.rio.rostry.ui.auth.ForgotPasswordScreen
import com.rio.rostry.ui.home.HomeScreen
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.fowls.MyFowlsScreen
import com.rio.rostry.ui.fowls.AddFowlScreen
import com.rio.rostry.ui.fowls.EditFowlScreen
import com.rio.rostry.ui.fowls.FowlDetailScreen
import com.rio.rostry.ui.fowls.FowlProfileScreen
import com.rio.rostry.ui.fowls.AddRecordScreen
import com.rio.rostry.ui.fowls.TransferOwnershipScreen
import com.rio.rostry.ui.fowls.TransferVerificationScreen
import com.rio.rostry.ui.chat.ChatListScreen
import com.rio.rostry.ui.chat.ChatScreen
import com.rio.rostry.ui.profile.ProfileScreen
import com.rio.rostry.ui.profile.EditProfileScreen
import com.rio.rostry.ui.posts.CreatePostScreen
import com.rio.rostry.ui.cart.CartScreen
import com.rio.rostry.ui.verification.VerificationScreen
import com.rio.rostry.ui.wallet.WalletScreen
import com.rio.rostry.ui.showcase.ShowcaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RostryApp() {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Check authentication state on app start
    LaunchedEffect(Unit) {
        try {
            // Add a small delay to ensure Firebase is initialized
            kotlinx.coroutines.delay(500)
            // For now, we'll start with authentication screen
            // In a real app, you'd check Firebase auth state here
            isAuthenticated = false
            isLoading = false
        } catch (e: Exception) {
            // If there's an error, go to auth screen
            isAuthenticated = false
            isLoading = false
        }
    }
    
    if (isLoading) {
        // Show loading screen while checking auth state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading Rostry...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else if (isAuthenticated) {
        MainApp(navController = navController, onLogout = { isAuthenticated = false })
    } else {
        AuthNavigation(
            navController = navController,
            onAuthSuccess = { isAuthenticated = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine if we should show bottom navigation
    val showBottomNav = when (currentDestination?.route) {
        Screen.Home.route,
        Screen.Marketplace.route,
        Screen.MyFowls.route,
        Screen.Chat.route,
        Screen.Profile.route -> true
        else -> false
    }
    
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main screens
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToMyFowls = { navController.navigate(Screen.MyFowls.route) },
                    onNavigateToCreatePost = { navController.navigate(Screen.CreatePost.route) }
                )
            }
            
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateToFowlDetail = { fowlId ->
                        navController.navigate(Screen.FowlDetail.createRoute(fowlId))
                    },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }
            
            composable(Screen.MyFowls.route) {
                MyFowlsScreen(
                    onNavigateToAddFowl = { navController.navigate(Screen.AddFowl.route) },
                    onNavigateToFowlDetail = { fowlId ->
                        navController.navigate(Screen.FowlDetail.createRoute(fowlId))
                    },
                    onNavigateToEditFowl = { fowlId ->
                        navController.navigate(Screen.EditFowl.createRoute(fowlId))
                    }
                )
            }
            
            composable(Screen.Chat.route) {
                ChatListScreen(
                    onNavigateToChat = { chatId ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatId))
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToLogin = onLogout
                )
            }
            
            // Detail and secondary screens
            composable(Screen.CreatePost.route) {
                CreatePostScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPostCreated = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.AddFowl.route) {
                AddFowlScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onFowlAdded = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.FowlDetail.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fowlId = backStackEntry.arguments?.getString("fowlId") ?: ""
                FowlDetailScreen(
                    fowlId = fowlId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate(Screen.EditFowl.createRoute(fowlId)) },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                )
            }
            
            composable(
                route = Screen.EditFowl.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fowlId = backStackEntry.arguments?.getString("fowlId") ?: ""
                EditFowlScreen(
                    fowlId = fowlId,
                    onNavigateBack = { navController.popBackStack() },
                    onFowlUpdated = { navController.popBackStack() },
                    onFowlDeleted = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.FowlProfile.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fowlId = backStackEntry.arguments?.getString("fowlId") ?: ""
                FowlProfileScreen(
                    fowlId = fowlId,
                    onNavigateBack = { navController.popBackStack() },
                    onEditFowl = { fowlId -> navController.navigate(Screen.EditFowl.createRoute(fowlId)) },
                    onAddRecord = { fowlId -> navController.navigate(Screen.AddRecord.createRoute(fowlId)) }
                )
            }
            
            composable(
                route = Screen.AddRecord.route,
                arguments = listOf(navArgument("fowlId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fowlId = backStackEntry.arguments?.getString("fowlId") ?: ""
                AddRecordScreen(
                    fowlId = fowlId,
                    onNavigateBack = { navController.popBackStack() },
                    onRecordAdded = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.ChatDetail.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(
                    chatId = chatId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileUpdated = { navController.popBackStack() }
                )
            }
            
            // Monetization screens
            composable(Screen.Verification.route) {
                VerificationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
                )
            }
            
            composable(Screen.Wallet.route) {
                WalletScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Showcase.route) {
                ShowcaseScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFowlDetail = { fowlId ->
                        navController.navigate(Screen.FowlDetail.createRoute(fowlId))
                    },
                    onNavigateToWallet = { navController.navigate(Screen.Wallet.route) }
                )
            }
        }
    }
}

@Composable
fun AuthNavigation(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onLoginSuccess = onAuthSuccess
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = onAuthSuccess
            )
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onResetSent = { navController.popBackStack() }
            )
        }
    }
}