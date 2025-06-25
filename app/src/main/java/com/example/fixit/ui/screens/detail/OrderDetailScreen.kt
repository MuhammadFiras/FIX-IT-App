package com.example.fixit.ui.screens.detail

import android.Manifest
import android.app.Activity
import android.app.Application // Pastikan ini diimpor
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.example.fixit.R
import com.example.fixit.app.FixItApplication
import com.example.fixit.domain.usecase.*
import com.example.fixit.ui.navigation.Screen
import com.example.fixit.ui.viewmodel.OrderDetailEvent
import com.example.fixit.ui.viewmodel.OrderDetailViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Arrays
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope // Import ini
import android.Manifest.permission.ACCESS_COARSE_LOCATION // <-- TAMBAHKAN IMPORT INI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavHostController,
    serviceCategory: String,
) {
    val application = LocalContext.current.applicationContext as FixItApplication
    val serviceOrderUseCases = application.serviceOrderUseCases
    val context = LocalContext.current
    val activity = context.findActivity()

    // Inisialisasi Places client
    val placesClient = remember { Places.createClient(context) }

    val orderDetailViewModel: OrderDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                OrderDetailViewModel(
                    serviceOrderUseCases = serviceOrderUseCases,
                    savedStateHandle = createSavedStateHandle(),
                    application = application
                )
            }
        }
    )

    val uiState by orderDetailViewModel.uiState.collectAsState()

    var currentLatLng by remember { mutableStateOf(LatLng(uiState.latitude, uiState.longitude)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
    }
    val markerState = rememberMarkerState(position = currentLatLng)

    // Pindahkan deklarasi scope di sini, di tingkat Composable utama
    val composableScope = rememberCoroutineScope() // Pindahkan ke sini

    LaunchedEffect(uiState.latitude, uiState.longitude) {
        val newLatLng = LatLng(uiState.latitude, uiState.longitude)
        if (newLatLng != currentLatLng) {
            currentLatLng = newLatLng
            markerState.position = newLatLng
            composableScope.launch { // Gunakan composableScope di sini
                cameraPositionState.animate(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(newLatLng, 15f)
                )
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    orderDetailViewModel.updateLocationCoordinates(location.latitude, location.longitude)
                    Log.d("Location", "Location updated: ${location.latitude}, ${location.longitude}")
                    break
                }
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Toast.makeText(context, "Izin lokasi presisi diberikan", Toast.LENGTH_SHORT).show()
                startLocationUpdates(fusedLocationClient, locationCallback, context)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Toast.makeText(context, "Izin lokasi kasar diberikan", Toast.LENGTH_SHORT).show()
                startLocationUpdates(fusedLocationClient, locationCallback, context)
            }
            else -> {
                Toast.makeText(context, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        orderDetailViewModel.setServiceCategory(serviceCategory)
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates(fusedLocationClient, locationCallback, context)
            }
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(context, "Aplikasi membutuhkan akses lokasi untuk menentukan lokasi layanan.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
            orderDetailViewModel.updateUiState { it.copy(errorMessage = null) }
        }
    }

    LaunchedEffect(Unit) {
        orderDetailViewModel.events.collect { event ->
            when (event) {
                is OrderDetailEvent.NavigateToOrderSuccess -> {
                    navController.navigate(Screen.OrderSuccess.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.order_detail_title), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray, shape = RoundedCornerShape(8.dp)),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        orderDetailViewModel.updateLocationCoordinates(latLng.latitude, latLng.longitude)
                        getAddressFromLatLng(context, latLng, orderDetailViewModel)
                    }
                ) {
                    Marker(state = markerState)
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.location), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TextField(
                            value = uiState.locationText,
                            onValueChange = { newValue ->
                                orderDetailViewModel.updateLocationText(newValue)
                                if (newValue.isNotEmpty()) {
                                    findAutocompletePredictions(placesClient, newValue, orderDetailViewModel)
                                } else {
                                    orderDetailViewModel.clearAutocompletePredictions()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.location)) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color(0xFF37C8B2),
                                unfocusedIndicatorColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    if (uiState.autocompletePredictions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                uiState.autocompletePredictions.forEach { prediction ->
                                    Text(
                                        text = prediction.getFullText(null).toString(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Gunakan composableScope yang sudah dideklarasikan di atas
                                                composableScope.launch { // Gunakan composableScope di sini
                                                    fetchPlaceDetails(
                                                        placesClient,
                                                        prediction.placeId,
                                                        orderDetailViewModel,
                                                        currentLatLng,
                                                        markerState,
                                                        cameraPositionState
                                                    )
                                                }
                                                orderDetailViewModel.updateLocationText(prediction.getFullText(null).toString())
                                                orderDetailViewModel.clearAutocompletePredictions()
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(R.string.customer_detail), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.name), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), shape = RoundedCornerShape(8.dp)) {
                        TextField(value = uiState.customerName, onValueChange = { orderDetailViewModel.updateCustomerName(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.name)) }, colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFF37C8B2), unfocusedIndicatorColor = Color.Gray), shape = RoundedCornerShape(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.phone_number), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), shape = RoundedCornerShape(8.dp)) {
                        TextField(value = uiState.customerPhone, onValueChange = { orderDetailViewModel.updateCustomerPhone(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.phone_number)) }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone), colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFF37C8B2), unfocusedIndicatorColor = Color.Gray), shape = RoundedCornerShape(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.service_description), fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), shape = RoundedCornerShape(8.dp)) {
                        TextField(value = uiState.serviceDescription, onValueChange = { orderDetailViewModel.updateServiceDescription(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(text = stringResource(R.string.description)) }, colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFF37C8B2), unfocusedIndicatorColor = Color.Gray), shape = RoundedCornerShape(8.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { orderDetailViewModel.submitOrder() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37C8B2)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(text = stringResource(R.string.order), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =====================================================================
// HELPER FUNCTIONS UNTUK LOKASI DAN PLACES API
// =====================================================================

private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    locationCallback: LocationCallback,
    context: Context
) {
    val locationRequest = LocationRequest.Builder(
        LocationRequest.PRIORITY_HIGH_ACCURACY,
        10000L // durationMillis
    )
        .setWaitForAccurateLocation(true)
        .build()

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
}

private fun getAddressFromLatLng(
    context: Context,
    latLng: LatLng,
    viewModel: OrderDetailViewModel
) {
    val geocoder = android.location.Geocoder(context, Locale.getDefault())
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val fullAddress = (0..address.maxAddressLineIndex).joinToString(", ") {
                    address.getAddressLine(it)
                }
                viewModel.updateLocationText(fullAddress)
            } else {
                viewModel.updateLocationText("Alamat tidak ditemukan")
            }
        }
    } else {
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val fullAddress = (0..address.maxAddressLineIndex).joinToString(", ") {
                address.getAddressLine(it)
            }
            viewModel.updateLocationText(fullAddress)
        } else {
            viewModel.updateLocationText("Alamat tidak ditemukan")
        }
    }
}

private fun findAutocompletePredictions(
    placesClient: PlacesClient,
    query: String,
    viewModel: OrderDetailViewModel
) {
    val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
        viewModel.updateAutocompletePredictions(response.autocompletePredictions)
    }.addOnFailureListener { exception ->
        Log.e("PlacesAPI", "Place autocomplete failed: ${exception.message}")
        viewModel.updateAutocompletePredictions(emptyList())
    }
}

private suspend fun fetchPlaceDetails(
    placesClient: PlacesClient,
    placeId: String,
    viewModel: OrderDetailViewModel,
    fallbackLatLng: LatLng,
    markerState: MarkerState,
    cameraPositionState: CameraPositionState
) {
    val placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
    val request = FetchPlaceRequest.newInstance(placeId, placeFields)

    try {
        val response = placesClient.fetchPlace(request).await()
        val place = response.place
        val latLng = place.latLng ?: fallbackLatLng
        val address = place.address ?: viewModel.uiState.value.locationText

        viewModel.updateLocationText(address)
        viewModel.updateLocationCoordinates(latLng.latitude, latLng.longitude)

        markerState.position = latLng
        cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        Log.d("PlacesAPI", "Place details fetched: ${place.name}, ${place.latLng}")

    } catch (exception: Exception) {
        Log.e("PlacesAPI", "Place details failed: ${exception.message}")
        Toast.makeText(viewModel.getApplication<Application>().applicationContext, "Gagal mendapatkan detail lokasi.", Toast.LENGTH_SHORT).show()
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}