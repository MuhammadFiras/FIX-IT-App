package com.example.fixit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fixit.BottomBarViewModel
import com.example.fixit.ui.screens.detail.OrderDetailScreen
import com.example.fixit.ui.screens.editprofile.EditProfileScreen
import com.example.fixit.ui.screens.history.HistoryScreen
import com.example.fixit.ui.screens.home.HomeScreen
import com.example.fixit.ui.screens.login.LoginScreen
import com.example.fixit.ui.screens.onboarding.OnboardingScreen
import com.example.fixit.ui.screens.order.OrderScreen
import com.example.fixit.ui.screens.ordersuccess.OrderSuccessScreen
import com.example.fixit.ui.screens.profile.ProfileScreen
import com.example.fixit.ui.screens.register.RegisterScreen
import com.example.fixit.ui.screens.setting.SettingScreen
import com.example.fixit.ui.screens.splash.SplashScreen

@Composable
fun FixItNavGraph(navController: NavHostController, modifier: Modifier) {
    val bottomBarViewModel: BottomBarViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("serviceCategory") { type = NavType.StringType }
                // Hapus argumen lama: navArgument("name"), navArgument("phone"), navArgument("desc")
            )
        ) { backStackEntry ->
            val serviceCategory = backStackEntry.arguments?.getString("serviceCategory") ?: ""
            OrderDetailScreen(navController, serviceCategory = serviceCategory)
        }
        composable(Screen.OrderSuccess.route) { OrderSuccessScreen(navController) }
        composable(Screen.Order.route) { OrderScreen(navController) }
        composable(Screen.History.route) { HistoryScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        composable(Screen.Setting.route) { SettingScreen(navController) }
    }
}
