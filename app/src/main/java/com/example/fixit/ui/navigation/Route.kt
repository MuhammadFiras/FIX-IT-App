package com.example.fixit.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Detail : Screen("detail/{serviceCategory}") {
        fun passArgs(serviceCategory: String): String {
            return "detail/$serviceCategory"
        }
    }
    object Order : Screen("pesanan")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object Register : Screen("register")
    object EditProfile : Screen("edit_profile")
    object Setting : Screen("setting")
    object OrderSuccess : Screen("order_success")
}
