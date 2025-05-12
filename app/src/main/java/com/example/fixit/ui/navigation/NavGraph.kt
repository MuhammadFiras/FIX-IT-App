package com.example.fixit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fixit.ui.screens.splash.SplashScreen
import com.example.fixit.ui.screens.onboarding.OnboardingScreen
import com.example.fixit.ui.screens.home.HomeScreen
import com.example.fixit.ui.screens.subcategory.SubCategoryScreen
import com.example.fixit.ui.screens.detail.DetailPesananScreen
import com.example.fixit.ui.screens.login.LoginScreen
import com.example.fixit.ui.screens.register.RegisterScreen
import com.example.fixit.ui.screens.editprofile.EditProfileScreen
import com.example.fixit.ui.screens.setting.SettingScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fixit.BottomBarViewModel
import com.example.fixit.ui.screens.order.PesananScreen
import com.example.fixit.ui.screens.profile.ProfileScreen
import com.example.fixit.ui.screens.history.HistoryScreen

@Composable
fun FixItNavGraph(navController: NavHostController, modifier: Modifier) {
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // Change this to start with Onboarding or Home, not splash
    ) {
        // Splash and onboarding screen should be independent and don't show bottom navigation yet
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }

        // These screens will now show after the onboarding/splash flow
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.SubCategory.route) { SubCategoryScreen(navController) }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType },
                navArgument("desc") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val desc = backStackEntry.arguments?.getString("desc") ?: ""
            DetailPesananScreen(name = name, phone = phone, description = desc)
        }
        composable(Screen.Pesanan.route) { PesananScreen(navController) }
        composable(Screen.Riwayat.route) { HistoryScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        composable(Screen.Setting.route) { SettingScreen(navController) }
    }
}
