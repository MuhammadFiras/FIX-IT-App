package com.example.fixit.ui.screens.ordersuccess

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen

@Composable
fun OrderSuccessScreen(navController: NavHostController) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success_animation)) // GANTI DENGAN LOTTIE ANDA
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center, // Pusatkan konten
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Lottie animation for success (gunakan lottie Anda di R.raw.nama_lottie_anda)
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(250.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.order_success_title), // Tambahkan string ini di strings.xml
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.order_success_desc), // Tambahkan string ini di strings.xml
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Tombol "Lihat Pesanan Aktif" atau ikon centang untuk navigasi
        Button(
            onClick = {
                navController.navigate(Screen.Order.route) {
                    // Pop up to Home screen to clear the back stack from OrderDetail
                    popUpTo(Screen.Home.route) { inclusive = false } // Keep Home in back stack
                    launchSingleTop = true // Avoid multiple copies of OrderScreen
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF37C8B2), // Warna hijau yang cerah
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Lihat Pesanan")
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.view_active_orders)) // Tambahkan string ini
        }
    }
}