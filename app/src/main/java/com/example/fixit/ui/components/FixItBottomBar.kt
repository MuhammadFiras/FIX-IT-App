package com.example.fixit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FixItBottomBar(navController: NavHostController) {
    // Get the ViewModel instance
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    // Collect the state of selectedIndex as a state
    val selectedIndex = bottomBarViewModel.selectedIndex.collectAsState().value

    NavigationBar(
        containerColor = Color(0xFFE5FBFF), // Light background color for the bottom bar
        contentColor = Color(0xFF37C8B2) // Dark text color for the bottom bar
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = {
                bottomBarViewModel.setSelectedIndex(0)
                navController.navigate(Screen.Home.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = if (selectedIndex == 0) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            },
            label = {
                Text(
                    stringResource(id = R.string.home_title), // Add the appropriate string resource for the new screen
                    color = if (selectedIndex == 0) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            }
        )
        NavigationBarItem(
            selected = selectedIndex == 1,  // Check if it's selected
            onClick = {
                bottomBarViewModel.setSelectedIndex(1)
                navController.navigate(Screen.Pesanan.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                    tint = if (selectedIndex == 1) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            },
            label = {
                Text(
                    stringResource(id = R.string.booking_status),
                    color = if (selectedIndex == 1) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            }
        )
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = {
                bottomBarViewModel.setSelectedIndex(2)
                navController.navigate(Screen.Riwayat.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = if (selectedIndex == 2) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            },
            label = {
                Text(
                    stringResource(id = R.string.order_history),
                    color = if (selectedIndex == 2) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            }
        )
        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = {
                bottomBarViewModel.setSelectedIndex(3)
                navController.navigate(Screen.Profile.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (selectedIndex == 3) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            },
            label = {
                Text(
                    stringResource(id = R.string.profile_title),
                    color = if (selectedIndex == 3) Color(0xFF92D26D) else Color(0xFF0095B3)
                )
            }
        )
    }
}
