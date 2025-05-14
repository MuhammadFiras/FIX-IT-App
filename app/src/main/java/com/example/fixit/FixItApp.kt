package com.example.fixit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.fixit.ui.navigation.FixItNavGraph
import com.example.fixit.ui.components.BottomNavigation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fixit.ui.navigation.Screen

@Composable
fun FixItApp() {
    val navController = rememberNavController()
    // ViewModel for managing BottomBar state
    val bottomBarViewModel: BottomBarViewModel = viewModel()
    // Get the selectedIndex from the ViewModel
    val selectedIndex = bottomBarViewModel.selectedIndex.collectAsState().value

    // Get current route
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // Define screens where bottom nav should show
    val bottomNavScreens = listOf(
        Screen.Home.route,
        Screen.Pesanan.route,
        Screen.Riwayat.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomNavScreens) {
                BottomNavigation(
                    navController = navController,
                    selectedIndex = selectedIndex,
                    onPageSelected = {
                        bottomBarViewModel.setSelectedIndex(it)
                    }
                )
            }
        }
    ) { innerPadding ->
        FixItNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}