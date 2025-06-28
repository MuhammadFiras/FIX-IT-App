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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.fixit.util.Constants
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.fixit.worker.SyncWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FixItApplication : Application() {

    // --- KEMBALIKAN VARIABEL LATEINIT INI UNTUK AKSES MANUAL ---
    // Hilt akan tetap menginjeksinya ke komponen yang di-Hilt,
    // tetapi ini memungkinkan akses manual dari luar.
    lateinit var database: AppDatabase
        private set
    lateinit var serviceOrderDao: ServiceOrderDao
        private set
    lateinit var firebaseServiceOrderDataSource: FirebaseServiceOrderDataSource
        private set
    lateinit var serviceOrderRepository: ServiceOrderRepository
        private set
    lateinit var serviceOrderUseCases: ServiceOrderUseCases
        private set
    lateinit var serviceOrderRepositoryImpl: ServiceOrderRepositoryImpl


    // --- KEMBALIKAN COMPANION OBJECT INI ---
    companion object {
        lateinit var instance: FixItApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("FixItApp", "FirebaseApp Initialized in FixItApplication")
        } else {
            Log.d("FixItApp", "FirebaseApp already initialized.")
        }

        // Inisialisasi Firestore dengan pengaturan kustom untuk memastikan persistensi dan perilaku
        val firestoreInstance = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()) // <-- PERBAIKI DI SINI
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

        createNotificationChannel()

        // --- KEMBALIKAN SEMUA BLOK INISIALISASI MANUAL INI ---
        // Ini memastikan variabel lateinit di atas mendapatkan nilai
        // dan bisa diakses oleh SyncWorker yang tidak di-Hilt
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fixit_database"
        ).build()

        serviceOrderDao = database.serviceOrderDao()

        firebaseServiceOrderDataSource = FirebaseServiceOrderDataSource(firestoreInstance)

        serviceOrderRepositoryImpl = ServiceOrderRepositoryImpl(firebaseServiceOrderDataSource, serviceOrderDao)
        serviceOrderRepository = serviceOrderRepositoryImpl

        serviceOrderUseCases = ServiceOrderUseCases(
            createServiceOrder = CreateServiceOrderUseCase(serviceOrderRepository),
            getServiceOrders = GetServiceOrdersUseCase(serviceOrderRepository),
            getServiceOrderById = GetServiceOrderByIdUseCase(serviceOrderRepository),
            updateServiceOrder = UpdateServiceOrderUseCase(serviceOrderRepository),
            deleteServiceOrder = DeleteServiceOrderUseCase(serviceOrderRepository),
            getActiveServiceOrders = GetActiveServiceOrdersUseCase(serviceOrderRepository),
            insertAllOrdersToLocal = InsertAllOrdersToLocalUseCase(serviceOrderRepository),
            getCompletedServiceOrders = GetCompletedServiceOrdersUseCase(serviceOrderRepository),
            syncAllOrdersFromFirebaseToRoom = SyncAllOrdersFromFirebaseToRoomUseCase(serviceOrderRepository),
            getServiceOrdersRealTime = GetServiceOrdersRealTimeUseCase(serviceOrderRepository)
        )
        // --- AKHIR BLOK INISIALISASI MANUAL YANG DIKEMBALIKAN ---

        // Jadwalkan Background Task (WorkManager)
        schedulePeriodicSyncWork()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Ini akan tetap ada, karena instance kita sendiri yang mengelola scope
        serviceOrderRepositoryImpl.cancelScope()
        if (this::firebaseServiceOrderDataSource.isInitialized) {
            firebaseServiceOrderDataSource.cancelListenerScope()
        }
    }

    private fun createNotificationChannel() {
        // Periksa versi Android. Notification Channel hanya diperlukan untuk Android 8.0 (Oreo) ke atas.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Ambil nama dan ID channel dari file Constants yang sudah kita buat
            val name = Constants.NOTIFICATION_CHANNEL_NAME // "FixIT Order Updates"
            val descriptionText = "Notifikasi untuk update status pesanan FixIT." // Deskripsi yang akan terlihat di pengaturan notifikasi
            val importance = NotificationManager.IMPORTANCE_HIGH // Tingkat pentingnya notifikasi. HIGH berarti notifikasi akan muncul di atas (pop-up).

            // Buat objek NotificationChannel
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText // Set deskripsi channel
            }

            // Dapatkan NotificationManager dari sistem
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daftarkan channel ini dengan sistem Android
            notificationManager.createNotificationChannel(channel)

            // Log untuk memastikan channel berhasil dibuat (akan terlihat di Logcat)
            Log.d("FixItApp", "Notification Channel created.")
        }
    }

    private fun schedulePeriodicSyncWork() {
        // Definisikan request untuk PeriodicWork
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES // Ulangi setiap 15 menit (untuk demo/pengujian)
        )
            .build() // Bangun objek work request

        // Dapatkan instance WorkManager dan antrekan tugas unik
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FixIT_Periodic_Sync_Work", // Nama unik untuk tugas ini. Pastikan ini unik di seluruh aplikasi Anda.
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        // Log untuk memastikan tugas berhasil dijadwalkan (akan terlihat di Logcat)
        Log.d("FixItApp", "Periodic sync work scheduled.")
    }
}