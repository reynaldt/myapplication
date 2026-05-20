package com.example.myapplication.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.data.repository.ExportRepository
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.ui.about.AboutScreen
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.inventory.InventoryScreen
import com.example.myapplication.ui.inventory.InventoryViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Home      : BottomNavItem("home", "Inventory", Icons.Default.Inventory)
    object Inventory : BottomNavItem("inventory", "Actions", Icons.Default.Tune)
    object Profile   : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
    sharedViewModel: InventoryViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val sessionManager: SessionManager = koinInject()
    val exportRepository: ExportRepository = koinInject()
    val currentUser by sessionManager.currentUser.collectAsState()
    val role = currentUser?.role ?: UserRole.VIEWER
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Build tabs based on role (VIEWER has no Actions tab)
    val items = buildList {
        add(BottomNavItem.Dashboard)
        add(BottomNavItem.Home)
        if (role.canCheckIn()) add(BottomNavItem.Inventory)
        add(BottomNavItem.Profile)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    onExportCsv = {
                        scope.launch {
                            exportRepository.exportInventoryCsv(context)
                                .onSuccess { uri ->
                                    exportRepository.shareFile(context, uri)
                                }
                                .onFailure { e ->
                                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                )
            }
            composable(BottomNavItem.Home.route) {
                HomeScreen(sharedViewModel)
            }
            composable(BottomNavItem.Inventory.route) {
                InventoryScreen(
                    viewModel = sharedViewModel,
                    onLogout = {
                        sessionManager.clearSession()
                        onLogout()
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        onLogout()
                    }
                )
            }
        }
    }
}
