package com.example.fixit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.usecase.ServiceOrderUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.Job // Import Job

data class OrderUiState(
    val activeOrders: List<ServiceOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val orderToDelete: ServiceOrder? = null
)

class OrderViewModel(
    private val serviceOrderUseCases: ServiceOrderUseCases,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null // Untuk mengelola job refresh

    init {
        fetchActiveOrders() // Ini akan mengobservasi Room, yang sekarang akan memicu sync
        // HAPUS PANGGILAN triggerRefresh() DARI SINI
        // triggerRefresh() // <-- HAPUS BARIS INI
    }

    // Fungsi triggerRefresh() tidak lagi dibutuhkan di sini karena sudah dipindahkan ke Repository
    // HAPUS JUGA FUNGSI triggerRefresh() INI DARI OrderViewModel jika tidak ada tempat lain yang menggunakannya.
    // fun triggerRefresh() { ... }

    private fun fetchActiveOrders() { // Ini tetap mengobservasi Room
        viewModelScope.launch {
            Log.d("OrderViewModel", "Fetching active orders from Room (fetchActiveOrders)...")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            serviceOrderUseCases.getActiveServiceOrders() // Ini akan memicu .onEach di Repository
                .catch { e ->
                    Log.e("OrderViewModel", "Error collecting active orders from Room UseCase: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load active orders"
                    )
                }
                .collect { orders ->
                    Log.d("OrderViewModel", "UI State updated with ${orders.size} active orders from Room.")
                    _uiState.value = _uiState.value.copy(
                        activeOrders = orders,
                        isLoading = false
                    )
                }
        }
    }

    fun updateOrderStatus(order: ServiceOrder, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val updatedOrder = order.copy(status = newStatus)
            val result = serviceOrderUseCases.updateServiceOrder(updatedOrder)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                Log.d("OrderViewModel", "Order ${order.id} status updated to $newStatus successfully.")
                // Setelah update berhasil, Flow Room akan otomatis memperbarui UI.
                // Kita juga memicu sync penuh dari RepositoryImpl
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update order status")
                Log.e("OrderViewModel", "Failed to update order status: ${e.message}")
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            Log.d("OrderViewModel", "Attempting to delete order with ID: $orderId.")
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = serviceOrderUseCases.deleteServiceOrder(orderId)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                Log.d("OrderViewModel", "Order $orderId deleted successfully.")
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = false, orderToDelete = null)
                // Setelah delete berhasil, Flow Room akan otomatis memperbarui UI.
                // Kita juga memicu sync penuh dari RepositoryImpl
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to delete order")
                Log.e("OrderViewModel", "Failed to delete order $orderId: ${e.message}")
            }
        }
    }

    fun showDeleteConfirmation(order: ServiceOrder) {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true, orderToDelete = order)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false, orderToDelete = null)
    }

    fun updateUiState(updater: (OrderUiState) -> OrderUiState) {
        _uiState.value = updater(_uiState.value)
    }
}