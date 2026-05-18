package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.main.MainScreen
import org.koin.compose.koinInject

private object AppRoute {
    const val Login = "login"
    const val Main = "main"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionManager: SessionManager = koinInject()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(isLoggedIn) {
        // Commented out to force offline mode
        /*
        if (isLoggedIn) {
            if (currentRoute != AppRoute.Main) {
                navController.navigate(AppRoute.Main) {
                    popUpTo(AppRoute.Login) { inclusive = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        } else {
            if (currentRoute != AppRoute.Login) {
                navController.navigate(AppRoute.Login) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
        */
    }

    val startDestination = AppRoute.Main // if (isLoggedIn) AppRoute.Main else AppRoute.Login

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoute.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(AppRoute.Login) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoute.Main) {
            MainScreen()
        }
    }
}
