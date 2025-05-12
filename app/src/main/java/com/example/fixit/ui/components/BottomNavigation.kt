package com.example.fixit.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen

@Composable
fun BottomNavigation(
    navController: NavController,
    selectedIndex: Int,
    onPageSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFE5FBFF),
        contentColor = Color(0xFF37C8B2)
    ) {
        // Home Item
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = {
                onPageSelected(0)
                navController.navigate(Screen.Home.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (selectedIndex == 0) Color(0xFF92D26D) else Color(0xFF0095B3))
            },
            label = { Text(stringResource(id = R.string.home_title),
                    color = if (selectedIndex == 0) Color(0xFF92D26D) else Color(0xFF0095B3)) }
        )

        // Pesanan Item
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = {
                onPageSelected(1)
                navController.navigate(Screen.Pesanan.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Pesanan",
                    tint = if (selectedIndex == 1) Color(0xFF92D26D) else Color(0xFF0095B3))
            },
            label = { Text(stringResource(id = R.string.booking_status),
                color = if (selectedIndex == 1) Color(0xFF92D26D) else Color(0xFF0095B3)) }
        )

        // Riwayat Item
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = {
                onPageSelected(2)
                navController.navigate(Screen.Riwayat.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Riwayat",
                    tint = if (selectedIndex == 2) Color(0xFF92D26D) else Color(0xFF0095B3))
            },
            label = { Text(stringResource(id = R.string.order_history),
                color = if (selectedIndex == 2) Color(0xFF92D26D) else Color(0xFF0095B3)) }
        )

        // Profile Item
        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = {
                onPageSelected(3)
                navController.navigate(Screen.Profile.route)
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = if (selectedIndex == 3) Color(0xFF92D26D) else Color(0xFF0095B3))
            },
            label = { Text(stringResource(id = R.string.profile_title),
                color = if (selectedIndex == 3) Color(0xFF92D26D) else Color(0xFF0095B3)) }
        )
    }
}
