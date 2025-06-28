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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log

@AndroidEntryPoint // <-- TAMBAHKAN ANOTASI INI
class MainActivity : ComponentActivity() {
    // Register ActivityResultLauncher untuk meminta izin notifikasi
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted.")
            // Izin diberikan, Anda bisa melanjutkan
        } else {
            Log.w("MainActivity", "POST_NOTIFICATIONS permission denied.")
            // Izin ditolak, mungkin tampilkan pesan kepada pengguna
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Panggil fungsi untuk meminta izin notifikasi di sini ---
        askNotificationPermission()
        setContent {
            FIXITTheme  {
                MainNavGraph()
            }
        }
    }

    // Fungsi untuk meminta izin notifikasi
    private fun askNotificationPermission() {
        // Hanya diperlukan untuk Android 13 (API 33) atau lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU = API 33
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Izin sudah diberikan
                Log.d("MainActivity", "POST_NOTIFICATIONS permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Berikan penjelasan kepada pengguna mengapa izin diperlukan
                // (Opsional: Anda bisa menampilkan AlertDialog di sini)
                Log.d("MainActivity", "Showing rationale for POST_NOTIFICATIONS permission.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Minta izin secara langsung
                Log.d("MainActivity", "Requesting POST_NOTIFICATIONS permission directly.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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