package com.example.fixit.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Import ini
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.app.FixItApplication
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.ui.viewmodel.HistoryViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fixit.domain.usecase.ServiceOrderUseCases
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {

    val historyViewModel: HistoryViewModel = hiltViewModel()


    val uiState by historyViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
            historyViewModel.updateUiState { it.copy(errorMessage = null) }
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.completedOrders.isEmpty()) {
                Text(
                    text = "Tidak ada riwayat pesanan selesai.",
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(top = 32.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.completedOrders, key = { it.id }) { item -> // Gunakan items dari ServiceOrder dan tambahkan key
                        HistoryCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(order: ServiceOrder) { // Menerima ServiceOrder
    val statusColor = when (order.status) {
        "Completed" -> Color(0xFF388E3C)
        "Cancelled" -> Color.Red
        else -> Color.Gray
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Anda bisa menyesuaikan ikon berdasarkan serviceCategory jika mau
                Image(
                    painter = painterResource(id = R.drawable.cleaning_icon), // Placeholder, sesuaikan dengan ikon kategori asli
                    contentDescription = order.serviceCategory,
                    modifier = Modifier.height(75.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = order.serviceCategory, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedTime = remember(order.timestamp) {
                        val date = Date(order.timestamp)
                        SimpleDateFormat("dd MMM.yyyy, HH:mm", Locale.getDefault()).format(date) // Format tanggal
                    }
                    Text(text = formattedTime, style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                }
            }

            Button(
                onClick = { /* Riwayat biasanya tidak bisa diklik untuk aksi */ },
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = order.status,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}