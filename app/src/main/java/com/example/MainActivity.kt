package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.AddGoalScreen
import com.example.ui.ProfileScreen
import com.example.ui.AnalyticsScreen
import com.example.ui.AuthScreen
import com.example.ui.HomeScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          TakaTrekApp(viewModel)
        }
      }
    }
  }
}

@Composable
fun TakaTrekApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val isBiometricAuthenticated by viewModel.isBiometricAuthenticated.collectAsStateWithLifecycle()
    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.fragment.app.FragmentActivity

    LaunchedEffect(isUserLoggedIn) {
        if (!isUserLoggedIn) {
            navController.navigate("auth") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    LaunchedEffect(isUserLoggedIn, isBiometricAuthenticated) {
        if (isUserLoggedIn && !isBiometricAuthenticated && activity != null) {
            com.example.util.BiometricHelper.authenticate(
                activity = activity,
                onSuccess = { viewModel.setBiometricAuthenticated(true) },
                onError = { /* fallback or retry */ }
            )
        }
    }

    if (isUserLoggedIn && !isBiometricAuthenticated) {
        // Show a blank or loading screen until biometrics completes
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) "home" else "auth"
    ) {
        composable("auth") {
            AuthScreen(onLoginSuccess = {
                viewModel.login()
                navController.navigate("home") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddGoal = { navController.navigate("add_goal") },
                onNavigateToAnalytics = { goalId -> navController.navigate("analytics/$goalId") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("add_goal") {
            AddGoalScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            "analytics/{goalId}",
            arguments = listOf(navArgument("goalId") { type = NavType.StringType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
            AnalyticsScreen(
                goalId = goalId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

