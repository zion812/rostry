package com.rio.rostry.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rio.rostry.ui.auth.LoginScreen
import com.rio.rostry.ui.auth.RegisterScreen
import com.rio.rostry.ui.auth.ForgotPasswordScreen
import com.rio.rostry.ui.home.HomeScreen
import com.rio.rostry.ui.marketplace.MarketplaceScreen
import com.rio.rostry.ui.fowls.MyFowlsScreen
import com.rio.rostry.ui.chat.ChatListScreen
import com.rio.rostry.ui.profile.ProfileScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RostryApp() {
    val navController = rememberNavController()
    var isAuthenticated by remember { mutableStateOf(false) }
    
    if (isAuthenticated) {
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
            // Main screens with simplified navigation
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToMyFowls = { navController.navigate(Screen.MyFowls.route) },
                    onNavigateToCreatePost = { /* TODO: Navigate to create post */ }
                )
            }
            
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    onNavigateToFowlDetail = { fowlId ->
                        // TODO: Navigate to fowl detail
                    },
                    onNavigateToCart = { /* TODO: Navigate to cart */ }
                )
            }
            
            composable(Screen.MyFowls.route) {
                MyFowlsScreen(
                    onNavigateToAddFowl = { /* TODO: Navigate to add fowl */ },
                    onNavigateToFowlDetail = { fowlId ->
                        // TODO: Navigate to fowl detail
                    },
                    onNavigateToEditFowl = { fowlId ->
                        // TODO: Navigate to edit fowl
                    }
                )
            }
            
            composable(Screen.Chat.route) {
                ChatListScreen(
                    onNavigateToChat = { chatId ->
                        // TODO: Navigate to chat detail
                    }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = { /* TODO: Navigate to edit profile */ },
                    onNavigateToLogin = onLogout
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