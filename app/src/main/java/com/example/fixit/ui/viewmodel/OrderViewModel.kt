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
import kotlinx.coroutines.flow.onCompletion // Tambahkan ini jika belum ada

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

    private var refreshJob: Job? = null

    init {
        fetchActiveOrders() // Mengobservasi Room
        // triggerRefresh() // Sudah dihapus dari init, hanya dipanggil manual atau dari LaunchedEffect di screen
    }

    // Fungsi untuk memicu refresh
    fun triggerRefresh() {
        if (refreshJob?.isActive == true) {
            Log.d("OrderViewModel", "Refresh job already active, skipping trigger.")
            return // Hindari multiple refreshes
        }
        refreshJob = viewModelScope.launch {
            Log.d("OrderViewModel", "Starting remote refresh of all orders (triggered by user/manual)...")
            _uiState.value = _uiState.value.copy(isLoading = true) // Tampilkan loading saat refresh manual
            try {
                serviceOrderUseCases.getServiceOrders()
                    .catch { e ->
                        Log.e("OrderViewModel", "Error collecting orders from remote for refresh: ${e.message}")
                    }
                    .onCompletion { cause ->
                        if (cause == null) {
                            Log.d("OrderViewModel", "Remote orders Flow completed normally.")
                        } else {
                            Log.e("OrderViewModel", "Remote orders Flow terminated with: ${cause.message}")
                        }
                    }
                    .collect { allRemoteOrders ->
                        serviceOrderUseCases.insertAllOrdersToLocal(allRemoteOrders)
                        Log.d("OrderViewModel", "Finished inserting/updating ${allRemoteOrders.size} orders to local DB via Flow.")
                        _uiState.value = _uiState.value.copy(isLoading = false) // Sembunyikan loading
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to refresh orders from remote")
                Log.e("OrderViewModel", "Outer catch: Error refreshing orders from remote: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false) // Sembunyikan loading
            }
        }
    }

    private fun fetchActiveOrders() { // Ini tetap mengobservasi Room
        viewModelScope.launch {
            Log.d("OrderViewModel", "Fetching active orders from Room (fetchActiveOrders)...")
            // _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null) // Loading handle di triggerRefresh
            serviceOrderUseCases.getActiveServiceOrders()
                .catch { e ->
                    Log.e("OrderViewModel", "Error collecting active orders from Room UseCase: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, // Sembunyikan loading
                        errorMessage = e.message ?: "Failed to load active orders"
                    )
                }
                .collect { orders ->
                    Log.d("OrderViewModel", "UI State updated with ${orders.size} active orders from Room.")
                    _uiState.value = _uiState.value.copy(
                        activeOrders = orders,
                        // isLoading = false // Sembunyikan loading
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