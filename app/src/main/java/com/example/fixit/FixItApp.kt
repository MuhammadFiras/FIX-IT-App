package com.example.fixit

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.fixit.ui.navigation.FixItNavGraph

@Composable
fun FixItApp() {
    val navController = rememberNavController()

    MaterialTheme {
        FixItNavGraph(
            navController = navController,
            modifier = Modifier.Companion.padding()
        )
    }
}
