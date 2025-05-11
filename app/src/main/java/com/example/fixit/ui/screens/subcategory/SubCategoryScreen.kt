package com.example.fixit.ui.screens.subcategory

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.category_cleaning))
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                SubCategoryItem( //Placeholder aja dulu
                    title = stringResource(R.string.cleaning_full),
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
                SubCategoryItem( //Placeholder aja dulu
                    title = stringResource(R.string.cleaning_outdoor),
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
                SubCategoryItem( //Placeholder aja dulu
                    title = stringResource(R.string.cleaning_room),
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
fun SubCategoryItem(title: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
