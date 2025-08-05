package com.rio.rostry.ui.navigation

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import com.rio.rostry.data.manager.FeatureFlagManager
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Main app composable that determines which navigation system to use
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RostryApp(
    featureFlagManager: FeatureFlagManager = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val windowSizeClass = calculateWindowSizeClass(activity)

    // Use the corrected role-based navigation system
    RoleBasedNavigationSystem(
        windowSizeClass = windowSizeClass
    )
}