package com.example.fixit.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixit.domain.model.ServiceOrder
import com.example.fixit.domain.usecase.ServiceOrderUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.google.android.libraries.places.api.model.AutocompletePrediction
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.AndroidViewModel
import com.example.fixit.app.FixItApplication
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fixit.R
import com.example.fixit.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// State untuk UI OrderDetailScreen
data class OrderDetailUiState(
    val locationText: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceDescription: String = "",
    val serviceCategory: String = "",
    val isLoading: Boolean = false,
    val orderSubmissionSuccess: Boolean = false,
    val errorMessage: String? = null,
    val autocompletePredictions: List<AutocompletePrediction> = emptyList()
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val serviceOrderUseCases: ServiceOrderUseCases,
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val analytics: FirebaseAnalytics = Firebase.analytics // Dapatkan instance Analytics
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<OrderDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Inisialisasi state dari SavedStateHandle atau default
    init {
        _uiState.value = OrderDetailUiState(
            customerName = savedStateHandle.get<String>("customerName") ?: "",
            customerPhone = savedStateHandle.get<String>("customerPhone") ?: "",
            serviceDescription = savedStateHandle.get<String>("serviceDescription") ?: "",
            locationText = savedStateHandle.get<String>("locationText") ?: "",
            latitude = savedStateHandle.get<Double>("latitude") ?: 0.0, // Tambahkan ini
            longitude = savedStateHandle.get<Double>("longitude") ?: 0.0, // Tambahkan ini
            serviceCategory = savedStateHandle.get<String>("serviceCategory") ?: ""
        )
    }

    // --- Fungsi untuk memperbarui state UI ---
    fun updateLocationText(text: String) {
        _uiState.value = _uiState.value.copy(locationText = text)
        savedStateHandle["locationText"] = text
    }

    fun updateLocationCoordinates(lat: Double, lng: Double) { // Tambahkan ini
        _uiState.value = _uiState.value.copy(latitude = lat, longitude = lng)
        savedStateHandle["latitude"] = lat
        savedStateHandle["longitude"] = lng
    }

    fun updateCustomerName(name: String) {
        _uiState.value = _uiState.value.copy(customerName = name)
        savedStateHandle["customerName"] = name
    }

    fun updateCustomerPhone(phone: String) {
        _uiState.value = _uiState.value.copy(customerPhone = phone)
        savedStateHandle["customerPhone"] = phone
    }

    fun updateServiceDescription(description: String) {
        _uiState.value = _uiState.value.copy(serviceDescription = description)
        savedStateHandle["serviceDescription"] = description
    }

    fun setServiceCategory(category: String) {
        _uiState.value = _uiState.value.copy(serviceCategory = category)
        savedStateHandle["serviceCategory"] = category
    }

    fun updateAutocompletePredictions(predictions: List<AutocompletePrediction>) { // Tambahkan ini
        _uiState.value = _uiState.value.copy(autocompletePredictions = predictions)
    }

    fun clearAutocompletePredictions() { // Tambahkan ini
        _uiState.value = _uiState.value.copy(autocompletePredictions = emptyList())
    }

    // Fungsi untuk mengirim pesanan baru
    fun submitOrder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, orderSubmissionSuccess = false)

            val currentState = _uiState.value
            val newOrder = ServiceOrder(
                customerName = currentState.customerName,
                customerPhone = currentState.customerPhone,
                serviceCategory = currentState.serviceCategory,
                serviceDescription = currentState.serviceDescription,
                locationText = currentState.locationText,
                latitude = currentState.latitude, // Gunakan latitude dari state
                longitude = currentState.longitude // Gunakan longitude dari state
            )

            val result = serviceOrderUseCases.createServiceOrder(newOrder)

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { createdOrder -> // <-- TERIMA createdOrder DI SINI
                _uiState.value = _uiState.value.copy(orderSubmissionSuccess = true)
                _events.send(OrderDetailEvent.NavigateToOrderSuccess)
                resetForm()
                showOrderCreatedNotification(createdOrder)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    // Fungsi untuk mereset form setelah submit
    private fun resetForm() {
        _uiState.value = OrderDetailUiState(serviceCategory = _uiState.value.serviceCategory)
        savedStateHandle["customerName"] = ""
        savedStateHandle["customerPhone"] = ""
        savedStateHandle["serviceDescription"] = ""
        savedStateHandle["locationText"] = ""
        savedStateHandle["latitude"] = 0.0 // Reset juga
        savedStateHandle["longitude"] = 0.0 // Reset juga
    }

    fun updateUiState(updater: (OrderDetailUiState) -> OrderDetailUiState) {
        _uiState.value = updater(_uiState.value)
    }

    // Helper untuk mendapatkan context dari ViewModel untuk Toast di helper functions
    fun getApplicationContext(): Context {
        return (this.javaClass.simpleName.let {
            // This is a hacky way to get context from ViewModel, typically done via Hilt/DI
            (this.javaClass.classLoader?.loadClass("com.example.fixit.app.FixItApplication") as? Class<FixItApplication>)?.let {
                it.getMethod("getInstance").invoke(null) as? FixItApplication
            }
        } ?: throw IllegalStateException("Application not found")).applicationContext
    }

    // --- FUNGSI UNTUK MENAMPILKAN NOTIFIKASI ---
    private fun showOrderCreatedNotification(order: ServiceOrder) {
        // Dapatkan NotificationManager dari sistem melalui application context
        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Bangun notifikasi menggunakan NotificationCompat.Builder (dari AndroidX Core)
        val notification = NotificationCompat.Builder(getApplication(), Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.fixit_logo) // <-- GANTI DENGAN IKON NOTIFIKASI ANDA (di res/drawable)
            .setContentTitle("Order Baru Dibuat!") // Judul notifikasi
            .setContentText("Jasa '${order.serviceCategory}': ${order.serviceDescription} telah berhasil dibuat.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioritas notifikasi (HIGH akan membuat pop-up)
            .setAutoCancel(true) // Notifikasi akan otomatis hilang saat pengguna mengkliknya
            .build()

        // Tampilkan notifikasi
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
        Log.d("Notification", "Order Created Notification shown for ID: ${order.id}.")
    }
}

sealed interface OrderDetailEvent {
    object NavigateToOrderSuccess : OrderDetailEvent
}

// Helper function to check if shouldShowRequestPermissionRationale
// (This is usually a method of Activity/Fragment, need context for composable)
fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    val activity = context.findActivity()
    return if (activity != null) {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    } else {
        false // Cannot determine without an Activity context
    }
}

// Extension function to find the Activity from a Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}