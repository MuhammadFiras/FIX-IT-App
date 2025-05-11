package com.example.fixit.ui.screens.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fixit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavHostController) {
    var selectedLanguage by remember { mutableStateOf("id") }
    var darkMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RadioButton(
                    selected = selectedLanguage == "id",
                    onClick = { selectedLanguage = "id" }
                )
                Text("Indonesia")

                RadioButton(
                    selected = selectedLanguage == "en",
                    onClick = { selectedLanguage = "en" }
                )
                Text("English")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode")
                Switch(checked = darkMode, onCheckedChange = { darkMode = it })
            }
        }
    }
}
