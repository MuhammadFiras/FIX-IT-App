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
import android.util.Log // Pastikan ini ada
import kotlinx.coroutines.flow.first // Pastikan ini ada jika digunakan

data class OrderUiState(
    val activeOrders: List<ServiceOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Boolean = false, // Untuk konfirmasi dialog hapus
    val orderToDelete: ServiceOrder? = null // Order yang akan dihapus
)

class OrderViewModel(
    private val serviceOrderUseCases: ServiceOrderUseCases,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        fetchActiveOrders()
        refreshOrdersFromRemote()
    }

    private fun fetchActiveOrders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            serviceOrderUseCases.getActiveServiceOrders()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load active orders"
                    )
                    Log.e("OrderViewModel", "Error fetching active orders from Room: ${e.message}")
                }
                .collect { orders ->
                    _uiState.value = _uiState.value.copy(
                        activeOrders = orders,
                        isLoading = false
                    )
                    Log.d("OrderViewModel", "UI State updated with ${orders.size} active orders.")
                }
        }
    }

    private fun refreshOrdersFromRemote() {
        viewModelScope.launch {
            try {
                Log.d("OrderViewModel", "Starting remote refresh of all orders...")
                // serviceOrderUseCases.getServiceOrders()
                //     .catch { e ->
                //         Log.e("OrderViewModel", "Error collecting orders from remote for refresh: ${e.message}")
                //     }
                //     .collect { allRemoteOrders ->
                //         serviceOrderUseCases.insertAllOrdersToLocal(allRemoteOrders)
                //         Log.d("OrderViewModel", "Finished inserting/updating ${allRemoteOrders.size} orders to local DB.")
                //     }
                // HACK UNTUK BUG SAAT INI: JIKA COLLECT DI ATAS BERMASALAH, Coba ini:
                val allRemoteOrders = serviceOrderUseCases.getServiceOrders().first()
                serviceOrderUseCases.insertAllOrdersToLocal(allRemoteOrders)
                Log.d("OrderViewModel", "Successfully refreshed cache with ${allRemoteOrders.size} orders from remote.")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to refresh orders from remote")
                Log.e("OrderViewModel", "Outer catch: Error refreshing orders from remote: ${e.message}")
            }
        }
    }

    fun updateOrderStatus(order: ServiceOrder, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val updatedOrder = order.copy(status = newStatus)
            val result = serviceOrderUseCases.updateServiceOrder(updatedOrder)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update order status")
                Log.e("OrderViewModel", "Failed to update order status: ${e.message}")
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            Log.d("OrderViewModel", "Attempting to delete order with ID: $orderId.") // Tambahkan log ini
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = serviceOrderUseCases.deleteServiceOrder(orderId)
            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess {
                Log.d("OrderViewModel", "Order $orderId deleted successfully.")
                _uiState.value = _uiState.value.copy(showDeleteConfirmation = false, orderToDelete = null) // Tutup dialog
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to delete order")
                Log.e("OrderViewModel", "Failed to delete order $orderId: ${e.message}")
            }
        }
    }

    // Fungsi untuk menampilkan dialog konfirmasi hapus
    fun showDeleteConfirmation(order: ServiceOrder) {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true, orderToDelete = order)
    }

    // Fungsi untuk menyembunyikan dialog konfirmasi hapus
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false, orderToDelete = null)
    }

    fun updateUiState(updater: (OrderUiState) -> OrderUiState) {
        _uiState.value = updater(_uiState.value)
    }
}