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

data class HistoryUiState(
    val completedOrders: List<ServiceOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HistoryViewModel(
    private val serviceOrderUseCases: ServiceOrderUseCases,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchCompletedOrders()
        // Optional: Anda bisa panggil sync penuh di sini juga jika mau memastikan data terbaru
        // serviceOrderUseCases.syncAllOrdersFromFirebaseToRoom()
    }

    private fun fetchCompletedOrders() {
        viewModelScope.launch {
            Log.d("HistoryViewModel", "Fetching completed orders from Room.")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            serviceOrderUseCases.getCompletedServiceOrders()
                .catch { e ->
                    Log.e("HistoryViewModel", "Error collecting completed orders from Room UseCase: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load completed orders"
                    )
                }
                .collect { orders ->
                    Log.d("HistoryViewModel", "History UI State updated with ${orders.size} completed orders.")
                    _uiState.value = _uiState.value.copy(
                        completedOrders = orders,
                        isLoading = false
                    )
                }
        }
    }

    fun updateUiState(updater: (HistoryUiState) -> HistoryUiState) {
        _uiState.value = updater(_uiState.value)
    }
}