package com.example.fixit.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object SubCategory : Screen("subcategory")
    object Detail : Screen("detail/{name}/{phone}/{desc}") {
        fun passArgs(name: String, phone: String, desc: String): String {
            return "detail/${name}/${phone}/${desc}"
        }
    }
    object Pesanan : Screen("pesanan")
    object Riwayat : Screen("history")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object Register : Screen("register")
    object EditProfile : Screen("edit_profile")
    object Setting : Screen("setting")
}
