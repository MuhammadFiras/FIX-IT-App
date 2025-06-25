package com.example.fixit.app

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.fixit.data.remote.service.FirebaseServiceOrderDataSource
import com.example.fixit.data.repository.impl.ServiceOrderRepositoryImpl
import com.example.fixit.domain.repository.ServiceOrderRepository
import com.example.fixit.domain.usecase.*
import androidx.room.Room
import com.example.fixit.data.local.database.AppDatabase
import com.example.fixit.data.local.dao.ServiceOrderDao
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.FirebaseFirestoreSettings // <-- TAMBAHKAN INI
import com.google.firebase.firestore.MemoryCacheSettings // <-- TAMBAHKAN INI
import com.google.firebase.firestore.PersistentCacheSettings // <-- TAMBAHKAN INI

class FixItApplication : Application() {

    // Variabel untuk menyimpan instance database Room
    lateinit var database: AppDatabase
        private set

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

    companion object {
        lateinit var instance: FixItApplication
            private set
    }

    lateinit var serviceOrderRepositoryImpl: ServiceOrderRepositoryImpl // <-- TAMBAHKAN INI

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Inisialisasi Firebase (jika belum)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("FixItApp", "FirebaseApp Initialized in FixItApplication")
        } else {
            Log.d("FixItApp", "FirebaseApp already initialized.")
        }

        // Inisialisasi Firestore dengan pengaturan kustom untuk memastikan persistensi dan perilaku
        val firestoreInstance = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            // Ganti setCacheSettings dengan setLocalCacheSettings
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()) // <-- PERBAIKI DI SINI
            // Atau jika ingin persistensi disk (offline):
            // .setLocalCacheSettings(PersistentCacheSettings.newBuilder() // <-- JIKA MAU DISK PERSISTENCE
            //     .setSizeBytes(PersistentCacheSettings.CACHE_SIZE_UNLIMITED)
            //     .build())
            .build()
        firestoreInstance.firestoreSettings = settings

        Log.d("FixItApp", "Firestore instance configured.")

        // Inisialisasi Google Places API
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
            "fixit_database"
        ).build()

        // 2. Dapatkan DAO dari database
        serviceOrderDao = database.serviceOrderDao()

        // 3. Inisialisasi Data Source Firebase
        firebaseServiceOrderDataSource = FirebaseServiceOrderDataSource(firestoreInstance) // <-- GUNAKAN firestoreInstance INI

        // 4. Inisialisasi Repository dengan kedua data source
        serviceOrderRepository = ServiceOrderRepositoryImpl(firebaseServiceOrderDataSource, serviceOrderDao)

        // 5. Inisialisasi Use Cases dengan repository
        serviceOrderUseCases = ServiceOrderUseCases(
            createServiceOrder = CreateServiceOrderUseCase(serviceOrderRepository),
            getServiceOrders = GetServiceOrdersUseCase(serviceOrderRepository),
            getServiceOrderById = GetServiceOrderByIdUseCase(serviceOrderRepository),
            updateServiceOrder = UpdateServiceOrderUseCase(serviceOrderRepository),
            deleteServiceOrder = DeleteServiceOrderUseCase(serviceOrderRepository),
            getActiveServiceOrders = GetActiveServiceOrdersUseCase(serviceOrderRepository),
            insertAllOrdersToLocal = InsertAllOrdersToLocalUseCase(serviceOrderRepository),
        )
    }
    override fun onTerminate() { // <-- TAMBAHKAN METODE INI
        super.onTerminate()
        // Batalkan scope repository saat aplikasi dimatikan untuk mencegah memory leak
        serviceOrderRepositoryImpl.cancelScope()
    }
}