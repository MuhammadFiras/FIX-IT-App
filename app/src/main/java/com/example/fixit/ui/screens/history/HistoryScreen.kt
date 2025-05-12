package com.example.fixit.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
    val mockHistory = listOf(
        HistoryItem("Pembersihan Full Rumah", "Selesai", "10 Mei 2025, 10:00"),
        HistoryItem("Pengecatan Ruangan", "Dibatalkan", "8 Mei 2025, 13:00")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.order_history),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(mockHistory.size) { index ->
                val item = mockHistory[index]
                HistoryCard(item)
            }
        }
    }
}

data class HistoryItem(val title: String, val status: String, val date: String)

@Composable
fun HistoryCard(item: HistoryItem) {
    // Set the color based on the item status
    val statusColor = when (item.status) {
        "Selesai" -> Color.Green
        "Dibatalkan" -> Color.Red
        else -> Color.Gray
    }

    // The main Card that holds the content
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Icon/Image and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cleaning_icon),
                    contentDescription = item.title,
                    modifier = Modifier.height(75.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.date, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                }
            }

            // Right side: Status Button
            Button(
                onClick = { /* Handle status click here */ },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = item.status,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
