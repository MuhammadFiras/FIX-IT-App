package com.example.fixit.ui.screens.subcategory

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.category_cleaning),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = padding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                SubCategoryItem( // Just Placeholder first
                    title = stringResource(R.string.cleaning_full),
                    price = "Rp750.000", // Added price
                    iconRes = R.drawable.rumah,
                    onClick = {
                        navController.navigate(
                            Screen.Detail.passArgs(
                                name = "John Doe",
                                phone = "081234567890",
                                desc = "Pembersihan full rumah 3 kamar dan dapur, mohon dibawa alat pel dan vakum."
                            )
                        )
                    }
                )
            }
            item {
                SubCategoryItem( // Just placeholder first
                    title = stringResource(R.string.cleaning_outdoor),
                    price = "Rp750.000", // Added price
                    iconRes = R.drawable.rumah,
                    onClick = {
                        navController.navigate(
                            Screen.Detail.passArgs(
                                name = "Jane Smith",
                                phone = "089876543210",
                                desc = "Tolong bersihkan halaman belakang dan depan rumah."
                            )
                        )
                    }
                )
            }
            item {
                SubCategoryItem( // Placeholder aja dulu
                    title = stringResource(R.string.cleaning_room),
                    price = "Rp750.000", // Added price
                    iconRes = R.drawable.rumah,
                    onClick = {
                        navController.navigate(
                            Screen.Detail.passArgs(
                                name = "Michael Jordan",
                                phone = "085678123456",
                                desc = "Hanya perlu membersihkan kamar utama dan kamar anak."
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SubCategoryItem(title: String, price: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium, // Rounded corners for better appearance
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between image, title, and price
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(80.dp) // Image size increased for better visibility
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 4.dp) // Space between title and price
            )
            Text(
                text = price,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
            )
        }
    }
}
