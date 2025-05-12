package com.example.fixit.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        topBar = { FixItSearchBar(modifier = Modifier) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "FIX IT",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 45.sp,
                    color = Color(0xFF00B2B2),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = stringResource(id = R.string.home_description),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CategoryButton(
                        label = stringResource(R.string.category_cleaning),
                        iconRes = R.drawable.cleaning_icon,
                        backgroundColor = Color(0xFFFFF2CC),
                        onClick = { navController.navigate(Screen.SubCategory.route) }
                    )
                }
                item {
                    CategoryButton(
                        label = stringResource(R.string.category_repair),
                        iconRes = R.drawable.repair_icon,
                        backgroundColor = Color(0xFFD4F8D2),
                        onClick = { /* TODO */ }
                    )
                }
                item {
                    CategoryButton(
                        label = stringResource(R.string.category_painting),
                        iconRes = R.drawable.paint_icon,
                        backgroundColor = Color(0xFFD9C3E5),
                        onClick = { /* TODO */ }
                    )
                }
                item {
                    CategoryButton(
                        label = stringResource(R.string.category_service),
                        iconRes = R.drawable.servis_icon,
                        backgroundColor = Color(0xFFFFC1D9),
                        onClick = { /* TODO */ }
                    )
                }
            }
        }
    }
}

// The Multiple Category button
@Composable
fun CategoryButton(label: String, iconRes: Int, backgroundColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(150.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                )
            }
        }
    }
}

// Search Bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixItSearchBar (modifier: Modifier) {
    var text by remember { mutableStateOf("") }
    var Active by remember { mutableStateOf(false)}
    var items = remember { mutableListOf(
        "Pembersihan",
        "Pengecatan",
        "Perbaikan",
        "Servis",
    ) }

    SearchBar(
        query = text,
        onQueryChange = { text = it },
        onSearch = { Active = false },
        active = Active,
        onActiveChange = { Active = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(text = stringResource(R.string.search))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (Active) {
                Icon(
                    modifier = Modifier.clickable() {
                        text = ""
                    },
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )

            }
        }
    ) {
        items.forEach {
            Row(
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Icon (
                    modifier = Modifier.padding(end = 10.dp),
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF00B2B2)
                )
                Text(text = it)
            }
        }
    }
}

