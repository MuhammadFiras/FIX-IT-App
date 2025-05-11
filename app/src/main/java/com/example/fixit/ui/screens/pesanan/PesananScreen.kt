package com.example.fixit.ui.screens.pesanan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.fixit.R
import com.example.fixit.ui.components.FixItBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesananScreen(navController: NavHostController) {
    // Sample orders with different statuses
    val orders = listOf(
        Order("Servis Elektronik", "Menunggu Teknisi", "Servis Kulkas LG, ada kerusakan di bagian radiator, dan perlu penggantian", "Jl. Contoh No. 123", "10 Mei 2025, 10:00", 1),
        Order("Pengecatan Ruangan 10x10", "Survey", "Mengecat ruang 10x10, membutuhkan pengecekan", "Jl. Contoh No. 124", "10 Mei 2025, 14:00", 2),
        Order("Perbaikan Pintu", "Bekerja", "Perbaikan pintu rusak di lantai 2", "Jl. Contoh No. 125", "11 Mei 2025, 09:00", 3),
        Order("Pembersihan Rumah", "Selesai", "Pembersihan rumah selesai dilakukan", "Jl. Contoh No. 126", "12 Mei 2025, 12:00", 4)
    )

    Scaffold(
        bottomBar = { FixItBottomBar(navController) },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.booking_status),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Loop through orders and display them
            orders.forEach { order ->
                OrderCard(order = order)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val statusColor = when (order.OrderStatus) {
        1 -> Color.Gray
        2 -> Color.Yellow
        3 -> Color.Red
        4 -> Color.Green
        else -> { Color.Black }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = order.title, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Alamat: ${order.address}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Waktu: ${order.time}", style = MaterialTheme.typography.bodySmall)
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .wrapContentSize(Alignment.CenterEnd)
                    .background(statusColor, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                )
            }
        }

        // Action Button (Cancel or Update based on status)
        Spacer(modifier = Modifier.height(8.dp))
        if (order.status == "Menunggu Teknisi" || order.status == "Survey") {
            Button(
                onClick = { /* Handle Cancel or Change Order Action */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_order))
            }
        } else {
            Button(
                onClick = { /* Handle Order Completion */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF37C8B2)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Order Complete")
            }
        }
    }
}

// Data class for the order and the status
data class Order(
    val title: String,
    val status: String,
    val description: String,
    val address: String,
    val time: String,
    val OrderStatus: Int
)

enum class OrderStatus {
    PENDING, SURVEY, IN_PROGRESS, COMPLETED
}
