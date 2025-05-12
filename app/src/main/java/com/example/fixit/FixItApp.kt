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

@Composable
fun FixItApp() {
    val navController = rememberNavController()

    // ViewModel for managing BottomBar state
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    // Get the selectedIndex from the ViewModel
    val selectedIndex = bottomBarViewModel.selectedIndex.collectAsState().value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation(
                navController = navController,
                selectedIndex = selectedIndex,
                onPageSelected = {
                    bottomBarViewModel.setSelectedIndex(it)
                }
            )
        }
    ) { innerPadding ->
        FixItNavGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
