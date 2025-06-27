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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.onCompletion
// Hapus import delay, flow
// import kotlinx.coroutines.delay
// import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collectLatest // Tambahkan ini jika belum ada

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

    private var refreshJob: Job? = null // Untuk manual refresh/one-time
    // HAPUS periodicPullJob
    // private var periodicPullJob: Job? = null
    private var realTimeCollectionJob: Job? = null // JOB BARU UNTUK REAL-TIME FOREGROUND

    init {
        fetchActiveOrders() // Mengobservasi Room (untuk UI)
        // HAPUS PANGGILAN startPeriodicPull()
        // startPeriodicPull()
        startRealTimeCollection() // <-- MULAI KOLEKSI REAL-TIME DI FOREGROUND
    }

    // --- FUNGSI BARU UNTUK KOLEKSI REAL-TIME DI FOREGROUND ---
    private fun startRealTimeCollection() {
        // Hentikan job sebelumnya jika ada untuk menghindari duplikasi listener
        realTimeCollectionJob?.cancel()
        realTimeCollectionJob = viewModelScope.launch {
            Log.d("OrderViewModel", "Starting real-time collection from FirebaseDataSource.allServiceOrders.")
            // MENGAMBIL FLOW REAL-TIME DARI FirebaseDataSource
            serviceOrderUseCases.getServiceOrdersRealTime() // <-- KUNCI: AMBIL DARI REAL-TIME FLOW
                .catch { e ->
                    Log.e("OrderViewModel", "Error in real-time collection Flow: ${e.message}", e)
                }
                .collect { allRemoteOrders ->
                    // Setiap kali ada emisi baru dari Firebase (real-time), update Room
                    Log.d("OrderViewModel", "Real-time collected ${allRemoteOrders.size} orders from Firebase. Updating Room via insertAllOrdersToLocal.")
                    // Ini akan memicu ServiceOrderRepositoryImpl.insertAllOrdersToLocal()
                    // yang akan melakukan deleteAll then insertAll
                    serviceOrderUseCases.insertAllOrdersToLocal(allRemoteOrders)
                }
        }
    }


    // Fungsi untuk memicu refresh manual (jika tombol refresh ada)
    fun triggerManualRefresh() {
        if (refreshJob?.isActive == true) {
            Log.d("OrderViewModel", "Manual refresh job already active, skipping.")
            return
        }
        refreshJob = viewModelScope.launch {
            Log.d("OrderViewModel", "Starting manual refresh of all orders (triggered by user/init)...")
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Panggil sync penuh dari Repository
                // Ini akan memicu ServiceOrderRepositoryImpl.syncAllOrdersFromFirebaseToRoom()
                val result = serviceOrderUseCases.syncAllOrdersFromFirebaseToRoom()
                result.onSuccess {
                    Log.d("OrderViewModel", "Manual refresh completed successfully. UI should update from Room's Flow.")
                }.onFailure { e ->
                    Log.e("OrderViewModel", "Manual refresh failed in UseCase: ${e.message}")
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to perform manual refresh")
                Log.e("OrderViewModel", "Error during manual refresh: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    // HAPUS FUNGSI startPeriodicPull() INI
    // private fun startPeriodicPull() { ... }

    private fun fetchActiveOrders() { // Ini tetap mengobservasi Room
        viewModelScope.launch {
            Log.d("OrderViewModel", "Fetching active orders from Room (fetchActiveOrders)...")
            serviceOrderUseCases.getActiveServiceOrders()
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

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        realTimeCollectionJob?.cancel() // <-- BATALKAN JOB BARU
        Log.d("OrderViewModel", "OrderViewModel cleared. All jobs cancelled.")
    }
}