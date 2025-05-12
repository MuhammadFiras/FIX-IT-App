package com.example.fixit.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen
import kotlinx.coroutines.delay
import com.example.fixit.ui.components.LoadingScreen

@Composable
fun OnboardingScreen(navController: NavHostController) {
    var showLoading by rememberSaveable { mutableStateOf(false) }

    if (showLoading) {
        LoadingScreen()
        LaunchedEffect(Unit) {
            delay(2000) // simulate loading before navigating to Home
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    } else {
        OnboardingContent {
            showLoading = true
        }
    }
}

@Composable
fun OnboardingContent(onGetStartedClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Lottie animation
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cleaning_animation))

        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever // This makes the animation loop forever
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(250.dp),
        )

        Text(
            text = stringResource(id = R.string.onboarding_title),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 25.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.onboarding_desc),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            onClick = onGetStartedClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0095B3),
                contentColor = Color.White
            )
        ) {
            Text(text = stringResource(id = R.string.get_started))
        }
    }
}
