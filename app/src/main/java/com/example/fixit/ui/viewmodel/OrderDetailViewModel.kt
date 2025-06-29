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
import com.google.firebase.analytics.analytics
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fixit.R
import com.example.fixit.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

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
    val autocompletePredictions: List<AutocompletePrediction> = emptyList(),
    val isOnline: Boolean = false
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val serviceOrderUseCases: ServiceOrderUseCases,
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    private val analytics: FirebaseAnalytics = Firebase.analytics
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<OrderDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Inisialisasi state dari SavedStateHandle atau default
    init {
        _uiState.value = OrderDetailUiState(
            customerName = savedStateHandle.get<String>("customerName") ?: "",
            customerPhone = savedStateHandle.get<String>("customerPhone") ?: "",
            serviceDescription = savedStateHandle.get<String>("serviceDescription") ?: "",
            locationText = savedStateHandle.get<String>("locationText") ?: "",
            latitude = savedStateHandle.get<Double>("latitude") ?: 0.0,
            longitude = savedStateHandle.get<Double>("longitude") ?: 0.0,
            serviceCategory = savedStateHandle.get<String>("serviceCategory") ?: ""
        )
        monitorNetworkConnectivity()
    }

    private fun monitorNetworkConnectivity() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _uiState.value = _uiState.value.copy(isOnline = true, errorMessage = null)
                Log.d("NetworkMonitor", "Network is AVAILABLE. isOnline: true")
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _uiState.value = _uiState.value.copy(isOnline = false, errorMessage = "Anda Offline")
                Log.d("NetworkMonitor", "Network is LOST. isOnline: false")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val wasOnline = _uiState.value.isOnline
                if (isConnected != wasOnline) {
                    _uiState.value = _uiState.value.copy(isOnline = isConnected, errorMessage = if (!isConnected) "Anda Offline" else null)
                    Log.d("NetworkMonitor", "Network capabilities changed. isOnline: $isConnected")
                }
            }
        })

        val initialNetworkStatus = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        _uiState.value = _uiState.value.copy(isOnline = initialNetworkStatus, errorMessage = if (!initialNetworkStatus) "Anda Offline" else null)
        Log.d("NetworkMonitor", "Initial network status: $initialNetworkStatus")
    }

    fun updateLocationText(text: String) {
        _uiState.value = _uiState.value.copy(locationText = text)
        savedStateHandle["locationText"] = text
    }

    fun updateLocationCoordinates(lat: Double, lng: Double) {
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

    fun updateAutocompletePredictions(predictions: List<AutocompletePrediction>) {
        _uiState.value = _uiState.value.copy(autocompletePredictions = predictions)
    }

    fun clearAutocompletePredictions() {
        _uiState.value = _uiState.value.copy(autocompletePredictions = emptyList())
    }

    fun submitOrder() {
        viewModelScope.launch {
            if (!uiState.value.isOnline) {
                _uiState.value = _uiState.value.copy(errorMessage = "Anda Offline")
                Log.d("OrderDetailVM", "Submit blocked: App is offline.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, orderSubmissionSuccess = false)

            val currentState = _uiState.value
            val newOrder = ServiceOrder(
                customerName = currentState.customerName,
                customerPhone = currentState.customerPhone,
                serviceCategory = currentState.serviceCategory,
                serviceDescription = currentState.serviceDescription,
                locationText = currentState.locationText,
                latitude = currentState.latitude,
                longitude = currentState.longitude
            )

            val result = serviceOrderUseCases.createServiceOrder(newOrder)

            _uiState.value = _uiState.value.copy(isLoading = false)

            result.onSuccess { createdOrder ->
                _uiState.value = _uiState.value.copy(orderSubmissionSuccess = true)
                _events.send(OrderDetailEvent.NavigateToOrderSuccess)
                resetForm()
                showOrderCreatedNotification(createdOrder)
                analytics.logEvent("order_created") {
                    param("order_id", createdOrder.id)
                    param("service_category", createdOrder.serviceCategory)
                    param("customer_name", createdOrder.customerName)
                    param("location_text", createdOrder.locationText)
                    param("description_length", createdOrder.serviceDescription.length.toLong())
                    param("timestamp_ms", createdOrder.timestamp)
                }
                Log.d("FirebaseAnalytics", "Event 'order_created' logged with details for order ID: ${createdOrder.id}.")
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    private fun resetForm() {
        _uiState.value = OrderDetailUiState(serviceCategory = _uiState.value.serviceCategory)
        savedStateHandle["customerName"] = ""
        savedStateHandle["customerPhone"] = ""
        savedStateHandle["serviceDescription"] = ""
        savedStateHandle["locationText"] = ""
        savedStateHandle["latitude"] = 0.0
        savedStateHandle["longitude"] = 0.0
    }

    fun updateUiState(updater: (OrderDetailUiState) -> OrderDetailUiState) {
        _uiState.value = updater(_uiState.value)
    }

    // Helper untuk mendapatkan context dari ViewModel untuk Toast di helper functions
    fun getApplicationContext(): Context {
        return (this.javaClass.simpleName.let {
            (this.javaClass.classLoader?.loadClass("com.example.fixit.app.FixItApplication") as? Class<FixItApplication>)?.let {
                it.getMethod("getInstance").invoke(null) as? FixItApplication
            }
        } ?: throw IllegalStateException("Application not found")).applicationContext
    }

    private fun showOrderCreatedNotification(order: ServiceOrder) {
        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(getApplication(), Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.fixit_logo)
            .setContentTitle("Order Baru Dibuat!")
            .setContentText("Jasa '${order.serviceCategory}': ${order.serviceDescription} telah berhasil dibuat.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
        Log.d("Notification", "Order Created Notification shown for ID: ${order.id}.")
    }
}

sealed interface OrderDetailEvent {
    object NavigateToOrderSuccess : OrderDetailEvent
}

fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    val activity = context.findActivity()
    return if (activity != null) {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    } else {
        false
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}