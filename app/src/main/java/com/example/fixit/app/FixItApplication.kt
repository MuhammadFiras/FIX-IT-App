package com.example.fixit.app

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore // Import ini
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource // Import ini
import com.example.fixit.data.repository.impl.ServiceOrderRepositoryImpl // Import ini
import com.example.fixit.domain.repository.ServiceOrderRepository // Import ini
import com.example.fixit.domain.usecase.* // Import semua use case
import androidx.room.Room // Import ini
import com.example.fixit.data.local.database.AppDatabase // Import ini
import com.example.fixit.data.local.dao.ServiceOrderDao // Import ini
import com.google.android.libraries.places.api.Places // <-- TAMBAHKAN INI

class FixItApplication : Application() {

    // Variabel untuk menyimpan instance database Room
    lateinit var database: AppDatabase
        private set // Hanya bisa di-set di dalam kelas ini

    // Variabel untuk menyimpan instance DAO (Data Access Object)
    lateinit var serviceOrderDao: ServiceOrderDao
        private set

    // Variabel untuk menyimpan instance data source Firebase
    lateinit var firebaseServiceOrderDataSource: FirebaseServiceOrderDataSource
        private set

    // Variabel untuk menyimpan instance repository
    lateinit var serviceOrderRepository: ServiceOrderRepository
        private set

    // Variabel untuk menyimpan instance use cases
    lateinit var serviceOrderUseCases: ServiceOrderUseCases
        private set

    companion object { // Tambahkan companion object ini
        lateinit var instance: FixItApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Inisialisasi Firebase
        FirebaseApp.initializeApp(this)
        Log.d("FixItApp", "Firebase Initialized in FixItApplication")

        // Inisialisasi Google Places API <-- TAMBAHKAN BLOK INI
        // Pastikan API Key Anda ada di AndroidManifest.xml
        val apiKey = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getString("com.google.android.geo.API_KEY")

        if (apiKey != null && !Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
            Log.d("FixItApp", "Google Places API Initialized.")
        } else {
            Log.e("FixItApp", "Google Places API Key not found or Places already initialized.")
        }

        // 1. Inisialisasi Room Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fixit_database" // Nama database Anda
        ).build()

        // 2. Dapatkan DAO dari database
        serviceOrderDao = database.serviceOrderDao()

        // 3. Inisialisasi Data Source Firebase
        firebaseServiceOrderDataSource = FirebaseServiceOrderDataSource(FirebaseFirestore.getInstance())

        // 4. Inisialisasi Repository dengan kedua data source
        serviceOrderRepository = ServiceOrderRepositoryImpl(firebaseServiceOrderDataSource, serviceOrderDao)

        // 5. Inisialisasi Use Cases dengan repository
        serviceOrderUseCases = ServiceOrderUseCases(
            createServiceOrder = CreateServiceOrderUseCase(serviceOrderRepository),
            getServiceOrders = GetServiceOrdersUseCase(serviceOrderRepository),
            getServiceOrderById = GetServiceOrderByIdUseCase(serviceOrderRepository),
            updateServiceOrder = UpdateServiceOrderUseCase(serviceOrderRepository),
            deleteServiceOrder = DeleteServiceOrderUseCase(serviceOrderRepository),
            getActiveServiceOrders = GetActiveServiceOrdersUseCase(serviceOrderRepository), // Tambahkan ini
                    insertAllOrdersToLocal = InsertAllOrdersToLocalUseCase(serviceOrderRepository) // <-- TAMBAHKAN INI
        )
    }
}