package com.example.fixit.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fixit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPesananScreen(
    name: String,
    phone: String,
    description: String
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.order_detail)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Google Map Here")
            }

            Text(text = "${stringResource(R.string.customer_name)}: $name")
            Text(text = "${stringResource(R.string.phone_number)}: $phone")
            Text(text = "${stringResource(R.string.service_description)}:")
            Text(text = description)
        }
    }
}

