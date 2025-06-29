package com.example.fixit.ui.screens.order

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.app.FixItApplication
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.ui.viewmodel.OrderViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fixit.domain.usecase.ServiceOrderUseCases
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavHostController) {

    val orderViewModel: OrderViewModel = hiltViewModel()

    val uiState by orderViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
            orderViewModel.updateUiState { it.copy(errorMessage = null) }
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
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.activeOrders.isEmpty()) {
                Text(
                    text = "Tidak ada pesanan aktif saat ini.",
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(top = 32.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(uiState.activeOrders) { order ->
                        OrderCard(
                            order = order,
                            navController = navController,
                            onUpdateStatus = { updatedOrder, newStatus ->
                                orderViewModel.updateOrderStatus(updatedOrder, newStatus)
                            },
                            onDeleteOrder = { orderToDelete ->
                                orderViewModel.showDeleteConfirmation(orderToDelete)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    if (uiState.showDeleteConfirmation && uiState.orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderViewModel.hideDeleteConfirmation() },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Warning") },
            title = { Text(text = "Konfirmasi Pembatalan") },
            text = { Text("Apakah Anda yakin ingin membatalkan pesanan '${uiState.orderToDelete?.serviceCategory}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val orderToDelete = uiState.orderToDelete
                        val orderIdToDelete = orderToDelete?.id
                        if (orderIdToDelete != null && orderIdToDelete.isNotEmpty()) {
                            orderViewModel.deleteOrder(orderIdToDelete)
                            Log.d("OrderScreen", "Attempting to delete order with ID: $orderIdToDelete. Order details: ${orderToDelete?.serviceCategory}")
                        } else {
                            Log.e("OrderScreen", "Cannot delete order: ID is empty or null for order: ${orderToDelete?.serviceCategory}")
                            Toast.makeText(context, "Gagal membatalkan: ID pesanan tidak ditemukan.", Toast.LENGTH_SHORT).show()
                        }
                        orderViewModel.hideDeleteConfirmation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Ya, Batalkan")
                }
            }
        )
    }
}

@Composable
fun OrderCard(
    order: ServiceOrder,
    navController: NavHostController,
    onUpdateStatus: (ServiceOrder, String) -> Unit,
    onDeleteOrder: (ServiceOrder) -> Unit
) {
    Log.d("OrderCardDebug", "Order ID: ${order.id}, Status received: ${order.status}")

    val statusColor = when (order.status) {
        "Pending" -> Color.Gray
        "Survey" -> Color(0xFFE5CC4B)
        "In Progress" -> Color(0xFFD32F2F)
        "Completed" -> Color(0xFF388E3C)
        else -> Color.Black
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = order.serviceCategory, style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Alamat: ${order.locationText}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    val formattedTime = remember(order.timestamp) {
                        val date = java.util.Date(order.timestamp)
                        java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(date)
                    }
                    Text(text = "Waktu: $formattedTime", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Deskripsi: ${order.serviceDescription}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Black))
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentSize(Alignment.CenterEnd)
                        .background(statusColor, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (order.status == "Pending" || order.status == "Survey") {
                Button(
                    onClick = { onDeleteOrder(order) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(40.dp)
                ) {
                    Text(text = stringResource(id = R.string.cancel_order))
                }
            } else if (order.status == "In Progress") {
                Button(
                    onClick = { onUpdateStatus(order, "Completed") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF37C8B2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Order Complete")
                }
            }
        }
    }
}