package com.example.fixit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fixit.ui.screens.splash.SplashScreen
import com.example.fixit.ui.screens.onboarding.OnboardingScreen
import com.example.fixit.ui.navigation.Screen
import com.example.fixit.ui.screens.editprofile.EditProfileScreen
import com.example.fixit.ui.screens.profile.ProfileScreen
import com.example.fixit.ui.theme.FIXITTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FIXITTheme  {
                MainNavGraph()
            }
        }
    }
}

@Composable
fun MainNavGraph() {
    val navController = rememberNavController()

    // Starting from the splash screen
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }
        composable(Screen.Home.route) {
            FixItApp()
        }
    }
}

@Preview
@Composable
fun preview () {
    FIXITTheme {
        ProfileScreen(navController = rememberNavController())
    }
}
