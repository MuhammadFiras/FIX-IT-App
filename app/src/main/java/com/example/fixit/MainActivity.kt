package com.example.fixit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.example.fixit.ui.theme.FIXITTheme
import androidx.navigation.compose.rememberNavController
import com.example.fixit.ui.navigation.FixItNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FIXITTheme {
                FixItApp()
            }
        }
    }
}
